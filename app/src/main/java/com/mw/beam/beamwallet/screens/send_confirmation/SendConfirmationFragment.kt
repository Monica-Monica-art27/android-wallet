/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.send_confirmation

import android.annotation.SuppressLint

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.app_activity.PendingSendInfo
import com.mw.beam.beamwallet.screens.confirm.PasswordConfirmDialog

import kotlinx.android.synthetic.main.fragment_send_confirmation.*
import kotlinx.android.synthetic.main.fragment_send_confirmation.contactCategory
import kotlinx.android.synthetic.main.fragment_send_confirmation.contactName
import kotlinx.android.synthetic.main.fragment_send_confirmation.outgoingAddress
import kotlinx.android.synthetic.main.fragment_send_confirmation.secondAvailableSum
import kotlinx.android.synthetic.main.fragment_send_confirmation.toolbarLayout

class SendConfirmationFragment : BaseFragment<SendConfirmationPresenter>(), SendConfirmationContract.View {
    private val args: SendConfirmationFragmentArgs by lazy {
        SendConfirmationFragmentArgs.fromBundle(requireArguments())
    }

    override fun getToolbarTitle(): String? = getString(R.string.confirmation)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_send_confirmation

    override fun getAddress(): String = args.sendAddress
    override fun getOutgoingAddress(): String = args.outgoingAddress
    override fun getAmount(): Long = args.sendAmount
    override fun getFee(): Long = args.fee
    override fun getComment(): String? = args.comment
    override fun getAddressType(): Int = args.addressType
    override fun getRemaining(): Int = args.remaining
    override fun getChange(): Long = args.change
    override fun getShieldedInputsFee(): Long = args.shieldedInputsFee

    private var typeAddress = BMAddressType.BMAddressTypeRegular

    @SuppressLint("SetTextI18n")
    override fun init(address: String, outgoingAddress: String, amount: Double, fee: Long, addressType: Int) {

        sendTo.text = address.trimAddress()

        this.outgoingAddress.text = outgoingAddress.trimAddress()

        amountToSend.text = "${amount.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondAvailableSum.text = amount.convertToCurrencyString()
        secondAvailableFeeSum.text = fee.convertToCurrencyGrothString()

        if(secondAvailableSum.text.isNullOrEmpty()) {
            secondAvailableSum.visibility = View.GONE
        }

        if(secondAvailableFeeSum.text.isNullOrEmpty()) {
            secondAvailableFeeSum.visibility = View.GONE
        }

        this.fee.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"

        typeAddress = BMAddressType.findByValue(addressType) ?: BMAddressType.BMAddressTypeRegular

        if (typeAddress == BMAddressType.BMAddressTypeMaxPrivacy) {
            transactionType.text = getString(R.string.max_privacy)
        }
        else if (typeAddress == BMAddressType.BMAddressTypeRegular) {
            transactionType.text = getString(R.string.regular)
        }
        else if (typeAddress == BMAddressType.BMAddressTypeOfflinePublic) {
            transactionType.text = getString(R.string.public_offline)
        }
        else if (typeAddress == BMAddressType.BMAddressTypeRegularPermanent) {
            transactionType.text = getString(R.string.regular) + ", " + getString(R.string.permanent).toLowerCase()
        }
        else if (typeAddress == BMAddressType.BMAddressTypeShielded) {
            val left = getString(R.string.payments_left).toLowerCase().replace("xx.","")
            transactionType.text = getString(R.string.offline) + ", " + left  + getRemaining()
        }
        else {
            transactionType.text = getString(R.string.regular)
        }
    }

    override fun onStart() {
        super.onStart()

        toolbarLayout.hasStatus = true
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.sent_color)
    }

    override fun configureOutAddress(walletAddress: WalletAddress, tags: List<Tag>) {
        if (!walletAddress.label.isBlank()) {
            outgoingAddressName.visibility = View.VISIBLE
            outgoingAddressName.text = walletAddress.label
        }

        if (tags.isNotEmpty()) {
            outgoingAddressCategory.visibility = View.VISIBLE
            outgoingAddressCategory.text = tags.createSpannableString(requireContext())
        }
    }

    override fun configureContact(walletAddress: WalletAddress, tags: List<Tag>) {
        if (!walletAddress.label.isBlank()) {
            contactName.visibility = View.VISIBLE
            contactName.text = walletAddress.label
        }

        if (tags.isNotEmpty()) {
            contactCategory.visibility = View.VISIBLE
            contactCategory.text = tags.createSpannableString(requireContext())
        }
    }

    override fun showConfirmDialog() {
       this.passwordDialog =  PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.SendBeam, {
            presenter?.onConfirmed()
        }, {

        })
        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter?.onSendPressed()
        }
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
    }

    @SuppressLint("SetTextI18n")
    override fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double) {
        totalUtxoTitle.visibility = View.VISIBLE
        totalUtxo.visibility = View.VISIBLE
        changeUtxoTitle.visibility = View.VISIBLE
        changeUtxo.visibility = View.VISIBLE
        secondAvailableUTXOSum.visibility = View.VISIBLE
        secondAvailableChangeSum.visibility = View.VISIBLE

        totalUtxo.text = "${usedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        changeUtxo.text = "${changedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"

        secondAvailableUTXOSum.text = usedUtxo.convertToCurrencyString()
        secondAvailableChangeSum.text = changedUtxo.convertToCurrencyString()

        if(secondAvailableUTXOSum.text.isNullOrEmpty()) {
            secondAvailableUTXOSum.visibility = View.GONE
        }

        if(secondAvailableChangeSum.text.isNullOrEmpty()) {
            secondAvailableChangeSum.visibility = View.GONE
        }
    }

    override fun delaySend(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        (activity as? AppActivity)?.pendingSend(PendingSendInfo(token, comment, amount, fee, outgoingAddress, typeAddress == BMAddressType.BMAddressTypeShielded))
    }

    override fun showSaveAddressFragment(address: String) {
        findNavController().navigate(SendConfirmationFragmentDirections.actionSendConfirmationFragmentToSaveAddressFragment(address))
    }

    override fun showWallet() {
        findNavController().popBackStack(R.id.walletFragment, false)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendConfirmationPresenter(this, SendConfirmationRepository(), SendConfirmationState())
    }
}