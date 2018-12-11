package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.AssetsActivity
import com.blockeq.stellarwallet.activities.BalanceSummaryActivity
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import com.blockeq.stellarwallet.activities.ReceiveActivity
import com.blockeq.stellarwallet.adapters.WalletRecyclerViewAdapter
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.models.WalletHeterogeneousArray
import com.blockeq.stellarwallet.mvvm.effects.EffectsViewModel
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse


class WalletFragment : BaseFragment(), OnLoadAccount {

    private var adapter : WalletRecyclerViewAdapter? = null
    private var effectsList : java.util.ArrayList<EffectResponse>? = null
    private lateinit var recyclerViewArrayList: WalletHeterogeneousArray
    private lateinit var viewModel : EffectsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EffectsViewModel::class.java)
    }

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindAdapter()

        receiveButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, ReceiveActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        sendButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, EnterAddressActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
        initViewModels()
    }

    //region User Interface

    private fun initViewModels() {
        viewModel.liveData.observe(viewLifecycleOwner, Observer { it ->
            if (it != null && walletProgressBar != null) {
                noTransactionsTextView.visibility = View.GONE
                walletProgressBar.visibility = View.VISIBLE

                effectsList = it

                doAsync {

                    recyclerViewArrayList.updateEffectsList(effectsList!!)
                    uiThread {
                        if (walletProgressBar != null) {
                            adapter!!.notifyDataSetChanged()
                            walletProgressBar.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }


    private fun bindAdapter() {
        val currAsset = WalletApplication.userSession.currAssetCode


        walletProgressBar.visibility = View.VISIBLE

        recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(AccountUtils.getTotalBalance(currAsset)),
                AvailableBalance(WalletApplication.localStore.availableBalance!!), Pair("Activity", "Amount"), effectsList)

        adapter = WalletRecyclerViewAdapter(activity!!, recyclerViewArrayList.array)

        adapter!!.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
            override fun onAssetDropdownClicked(view: View, position: Int) {
                val context = view.context
                startActivity(Intent(context, AssetsActivity::class.java))
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        adapter!!.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
            override fun onLearnMoreButtonClicked(view: View, position: Int) {
                val context = view.context
                startActivity(Intent(context, BalanceSummaryActivity::class.java))
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        walletRecyclerView.adapter = adapter
        walletRecyclerView.layoutManager = LinearLayoutManager(activity)

    }

    private fun updateAdapter() {
        val currAsset = WalletApplication.userSession.currAssetCode
        if (currAsset != Constants.LUMENS_ASSET_TYPE) {
            recyclerViewArrayList.hideAvailableBalance()
        } else {
            recyclerViewArrayList.showAvailableBalance(AvailableBalance(WalletApplication.localStore.availableBalance!!))
        }

        recyclerViewArrayList.updateTotalBalance(TotalBalance(AccountUtils.getTotalBalance(currAsset)))
        recyclerViewArrayList.updateEffectsList(effectsList)

        adapter!!.notifyDataSetChanged()
    }

    //endregion

    //region Call backs

    override fun onLoadAccount(result: AccountResponse?) {
        recyclerViewArrayList.updateTotalBalance(
                TotalBalance(AccountUtils.getTotalBalance(WalletApplication.userSession.currAssetCode)))
        recyclerViewArrayList.updateAvailableBalance(
                AvailableBalance(WalletApplication.localStore.availableBalance!!))
    }

    override fun onError(error: ErrorResponse) {
        if (error.code == Constants.SERVER_ERROR_NOT_FOUND && walletProgressBar != null) {
            val mainHandler = Handler(context!!.mainLooper)

            mainHandler.post {
                noTransactionsTextView.visibility = View.VISIBLE
                walletProgressBar.visibility = View.GONE
            }
        }
    }

    //endregion

}
