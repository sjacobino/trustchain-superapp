package nl.tudelft.trustchain.eurotoken.ui.transfer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.common.contacts.ContactStore
import nl.tudelft.trustchain.common.eurotoken.TransactionRepository
import nl.tudelft.trustchain.common.util.QRCodeUtils
import nl.tudelft.trustchain.common.util.viewBinding
import nl.tudelft.trustchain.eurotoken.R
import nl.tudelft.trustchain.eurotoken.databinding.FragmentTransferEuroBinding
import nl.tudelft.trustchain.eurotoken.ui.EurotokenBaseFragment
import org.json.JSONException
import org.json.JSONObject

class TransferFragment : EurotokenBaseFragment(R.layout.fragment_transfer_euro) {
    private val binding by viewBinding(FragmentTransferEuroBinding::bind)

    private val qrCodeUtils by lazy {
        QRCodeUtils(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenResumed {
            while (isActive) {
                val ownKey = transactionRepository.trustChainCommunity.myPeer.publicKey
                val ownContact =
                    ContactStore.getInstance(requireContext()).getContactFromPublicKey(ownKey)

                binding.txtBalance.text =
                    TransactionRepository.prettyAmount(transactionRepository.getMyVerifiedBalance())
                if (ownContact?.name != null) {
                    binding.missingNameLayout.visibility = View.GONE
                    binding.txtOwnName.text = "Your balance (" + ownContact.name + ")"
                } else {
                    binding.missingNameLayout.visibility = View.VISIBLE
                    binding.txtOwnName.text = "Your balance"
                }
                delay(1000L)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ownKey = transactionRepository.trustChainCommunity.myPeer.publicKey
        val ownContact = ContactStore.getInstance(view.context).getContactFromPublicKey(ownKey)

        binding.txtBalance.text =
            TransactionRepository.prettyAmount(transactionRepository.getMyVerifiedBalance())
        binding.txtOwnPublicKey.text = ownKey.keyToHash().toHex()

        if (ownContact?.name != null) {
            binding.missingNameLayout.visibility = View.GONE
            binding.txtOwnName.text = "Your balance (" + ownContact.name + ")"
        }

        fun addName() {
            var newName = binding.edtMissingName.text.toString()
            if (newName.isNotEmpty()) {
                ContactStore.getInstance(requireContext())
                    .addContact(ownKey, newName)
                if (ownContact?.name != null) {
                    binding.missingNameLayout.visibility = View.GONE
                    binding.txtOwnName.text = "Your balance (" + ownContact.name + ")"
                }
                val inputMethodManager =
                    requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        binding.btnAdd.setOnClickListener {
            addName()
        }

        binding.edtMissingName.onSubmit {
            addName()
        }

        binding.edtAmount.addDecimalLimiter()

        binding.btnRequest.setOnClickListener {
            val amount = getAmount(binding.edtAmount.text.toString())
            if (amount > 0) {
                val myPeer = transactionRepository.trustChainCommunity.myPeer
                val requestOwnContact =
                    ContactStore.getInstance(view.context).getContactFromPublicKey(ownKey)

                val connectionData = JSONObject()
                connectionData.put("public_key", myPeer.publicKey.keyToBin().toHex())
                connectionData.put("amount", amount)
                connectionData.put("name", requestOwnContact?.name ?: "")
                connectionData.put("type", "transfer")

                val args = Bundle()

                args.putString(RequestMoneyFragment.ARG_DATA, connectionData.toString())

                findNavController().navigate(
                    R.id.action_transferFragment_to_requestMoneyFragment,
                    args
                )
            }
        }

        binding.btnSend.setOnClickListener {
            qrCodeUtils.startQRScanner(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        qrCodeUtils.parseActivityResult(requestCode, resultCode, data)?.let {
            try {
                val connectionData = ConnectionData(it)

                val args = Bundle()
                args.putString(SendMoneyFragment.ARG_PUBLIC_KEY, connectionData.public_key)
                args.putLong(SendMoneyFragment.ARG_AMOUNT, connectionData.amount)
                args.putString(SendMoneyFragment.ARG_NAME, connectionData.name)
                if (connectionData.type == "transfer") {
                    findNavController().navigate(
                        R.id.action_transferFragment_to_sendMoneyFragment,
                        args
                    )
                } else {
                    Toast.makeText(requireContext(), "Invalid QR", Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                Toast.makeText(requireContext(), "Scan failed, try again", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(requireContext(), "Scan failed", Toast.LENGTH_LONG).show()
        return
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_PUBLIC_KEY = "public_key"

        fun EditText.onSubmit(func: () -> Unit) {
            setOnEditorActionListener { _, actionId, _ ->

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    func()
                }

                true

            }
        }

        class ConnectionData(json: String) : JSONObject(json) {
            var public_key = this.optString("public_key")
            var amount = this.optLong("amount", -1L)
            var name = this.optString("name")
            var type = this.optString("type")
        }

        fun getAmount(amount: String): Long {
            val regex = """[^\d]""".toRegex()
            if (amount.isEmpty()) {
                return 0L
            }
            return regex.replace(amount, "").toLong()
        }

        fun Context.hideKeyboard(view: View) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun EditText.decimalLimiter(string: String): String {

            var amount = getAmount(string)

            if (amount == 0L) {
                return ""
            }

            //val amount = string.replace("[^\\d]", "").toLong()
            return (amount / 100).toString() + "." + (amount % 100).toString().padStart(2, '0')
        }

        fun EditText.addDecimalLimiter() {

            this.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable?) {
                    val str = this@addDecimalLimiter.text!!.toString()
                    if (str.isEmpty()) return
                    val str2 = decimalLimiter(str)

                    if (str2 != str) {
                        this@addDecimalLimiter.setText(str2)
                        val pos = this@addDecimalLimiter.text!!.length
                        this@addDecimalLimiter.setSelection(pos)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }
}

