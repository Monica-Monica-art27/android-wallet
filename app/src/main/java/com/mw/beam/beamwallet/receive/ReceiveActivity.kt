package com.mw.beam.beamwallet.receive

import android.support.v7.widget.Toolbar
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.activity_receive.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveActivity : BaseActivity<ReceivePresenter>(), ReceiveContract.View {
    private lateinit var presenter: ReceivePresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_receive

    override fun init() {
        val toolbar = toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        initToolbar(toolbar, getString(R.string.receive_title), true)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = ReceivePresenter(this, ReceiveRepository())
        return presenter
    }
}
