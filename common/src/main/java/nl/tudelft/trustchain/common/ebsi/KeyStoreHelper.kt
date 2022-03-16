package nl.tudelft.trustchain.common.ebsi

import android.util.Log
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import nl.tudelft.ipv8.android.util.AndroidEncodingUtils
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1String
import org.spongycastle.asn1.DEROctetString
import org.spongycastle.asn1.DLSequence
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec
import org.spongycastle.math.ec.ECCurve
import org.spongycastle.util.io.pem.PemObject
import org.spongycastle.util.io.pem.PemReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringReader
import java.lang.Exception
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.*


class KeyStoreHelper(
    wallet: EBSIWallet
) {

    private val ebsiKeyFile by lazy { File(wallet.ebsiWalletDir, EBSI_KEY_FILE) }

    fun getKeyPair(): KeyPair? {

        if (ebsiKeyFile.exists()) {
            Log.e(TAG, "key files exist")

            try {
                val serialized = ebsiKeyFile.readBytes()

                var offset = 0
                val (p, pSize) = deserializeVarLen(serialized, offset)
                offset += pSize
                val (gX, gXSize) = deserializeVarLen(serialized, offset)
                offset += gXSize
                val (gY, gYSize) = deserializeVarLen(serialized, offset)
                offset += gYSize
                val (curveA, curveASize) = deserializeVarLen(serialized, offset)
                offset += curveASize
                val (curveB, curveBSize) = deserializeVarLen(serialized, offset)
                offset += curveBSize
                val (s, sSize) = deserializeVarLen(serialized, offset)
                offset += sSize
                val (n, nSize) = deserializeVarLen(serialized, offset)
                offset += nSize
                val (h, hSize) = deserializeVarLen(serialized, offset)
                offset += hSize
                val (wX, wXSize) = deserializeVarLen(serialized, offset)
                offset += wXSize
                val (wY, wYSize) = deserializeVarLen(serialized, offset)
                offset += wYSize

                val parsedP = BigInteger(p)
                val ecField = ECFieldFp(parsedP)
                val x = BigInteger(gX)
                val y = BigInteger(gY)
                val g = ECPoint(x, y)

                val a = BigInteger(curveA)
                val b = BigInteger(curveB)
                val curve = EllipticCurve(ecField, a, b)

                val bN = BigInteger(n)
                val bH = Integer.parseInt(h.toString(Charset.defaultCharset()))
                val spec = ECParameterSpec(curve, g, bN, bH)

                val bS = BigInteger(s)
                val ecPrivateKeySpec = ECPrivateKeySpec(bS, spec)

                val bWX = BigInteger(wX)
                val bWY = BigInteger(wY)
                val w = ECPoint(bWX, bWY)

                val ecPublicKeySpec = ECPublicKeySpec(w, spec)

                val kf = KeyFactory.getInstance("EC")
                val privateKey = kf.generatePrivate(ecPrivateKeySpec) as ECPrivateKey
                val publicKey = kf.generatePublic(ecPublicKeySpec) as ECPublicKey

                return KeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading key files", e)
            }
        }

        return null
    }

    /**
     * Stores the Keys to the keystore fle.
     * @param keyPair
     */
    fun storeKey(keyPair: KeyPair) {
        Log.e(TAG, "key file: ${ebsiKeyFile.absolutePath}")
        ebsiKeyFile.createNewFile()
        ebsiKeyFile.writeBytes(keyPair.serialize())

        Log.e(TAG, "Key pair stored")
    }

    companion object {
        private val TAG = KeyStoreHelper::class.simpleName
        const val EBSI_KEY_FILE = "EBSI_KEY"

        /**
         * The Spongy Castle Provider needs to be inserted as a provider in list of providers.
         */
        fun initProvider() {
            Security.insertProviderAt(BouncyCastleProvider(), 1)
        }

        fun decodeDerPublicKey(data: ByteArray) {
            val inStream = ByteArrayInputStream(data)
            val asn1InputStream = ASN1InputStream(inStream)
            val derObject = asn1InputStream.readObject() as DLSequence
            derObject.objects.iterator().forEach {
                Log.e(TAG, "$it")
            }
            //Log.e(TAG, "DER string: ${derObject.string}")
        }

        fun decodePemPublicKey(s: String): ECPublicKey {
            val pemString = AndroidEncodingUtils.decodeBase64FromString(s).toString(Charset.defaultCharset())
            val pemReader = PemReader(StringReader(pemString))
            val pem = pemReader.readPemObject()
            val publicKeySpec = X509EncodedKeySpec(pem.content)
            val kf = KeyFactory.getInstance("EC")
            return kf.generatePublic(publicKeySpec) as ECPublicKey
        }

        fun decodePublicKey(s: String): ByteArray {
            return AndroidEncodingUtils.decodeBase64FromString(s)
        }

        fun loadPublicKey(data: ByteArray): PublicKey? {
            //val factory = KeyFactory.getInstance("ECDSA", "SC")

            val curve = "secp256k1"
            val kf = KeyFactory.getInstance("EC")

            // ECNamedCurveTable.getByName(curve)
            val spec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(curve)
            val eccCurve: ECCurve = spec.curve
            Log.e(TAG, "Curve: $eccCurve")
            val ellipticCurve: EllipticCurve = EC5Util.convertCurve(eccCurve, spec.seed)
            Log.e(TAG, "ECCurve: $ellipticCurve, seed: ${spec.seed}")

            // decoding point fails,
            // line no 66.
            Log.e(TAG, "Field: ${ellipticCurve.field}")
            val point: ECPoint = ECPointUtil.decodePoint(ellipticCurve, data)
            val params: ECParameterSpec = EC5Util.convertSpec(ellipticCurve, spec)
            val keySpec = ECPublicKeySpec(point, params)
            return kf.generatePublic(keySpec)
        }
    }
}

fun KeyPair.jwk(): JWK {
    return ECKey.Builder(Curve.SECP256K1, this.public as ECPublicKey)
        .privateKey(this.private as ECPrivateKey)
        .build()
}

fun KeyPair.serialize(): ByteArray {
    val privateKey = this.private as ECPrivateKey

    val field = privateKey.params.curve.field as ECFieldFp
    val p = field.p
    val gX = privateKey.params.generator.affineX
    val gY = privateKey.params.generator.affineY
    val curveA = privateKey.params.curve.a
    val curveB = privateKey.params.curve.b
    val s = privateKey.s
    val n = privateKey.params.order
    val h = privateKey.params.cofactor
    val publicKey = this.public as ECPublicKey
    val wX = publicKey.w.affineX
    val wY = publicKey.w.affineY

    val serialized = serializeVarLen(p.toByteArray()) +
        serializeVarLen(gX.toByteArray()) +
        serializeVarLen(gY.toByteArray()) +
        serializeVarLen(curveA.toByteArray()) +
        serializeVarLen(curveB.toByteArray()) +
        serializeVarLen(s.toByteArray()) +
        serializeVarLen(n.toByteArray()) +
        serializeVarLen("$h".toByteArray()) +
        serializeVarLen(wX.toByteArray()) +
        serializeVarLen(wY.toByteArray())

    return serialized
}
