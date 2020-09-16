package com.demo.app.speedlocation.screen.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.demo.app.speedlocation.MyFragment
import com.demo.app.speedlocation.data.HistoryModel
import com.demo.app.speedlocation.data.SharedPrefsStore
import com.demo.app.speedlocation.databinding.FragmentHistoryBinding
import com.demo.app.speedlocation.helper.*
import kotlinx.coroutines.delay

class HistoryFragment : MyFragment() {
    private var binding: FragmentHistoryBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = FragmentHistoryBinding.inflate(inflater)
            bindView()
            startFlow()
        }
        return binding?.root
    }

    private fun bindView() {
        binding?.run {
            setStatusBarSpace(viewStatusSpace)
            recyclerView.setVerticalLayout()
            recyclerView.adapter = HistoryAdapter()
            getAdapter()?.setFragmentManager(childFragmentManager)
            buttonRun.setOnClickListener {
                context?.vibrate()
                findNavController().navigate(HistoryFragmentDirections.goToRecord())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getAdapter()?.cancelLaunch()
        cancelLaunch()
        getAdapter()?.setFragmentManager(null)
        binding = null
    }

    private fun getAdapter(): HistoryAdapter? = binding?.recyclerView?.adapter as HistoryAdapter?

    private fun startFlow() {
        mainLaunch {
            delay(DELAY_LOADING_TIME)
            getAdapter()?.setData(requireContext(), inBackground {
                SharedPrefsStore.getRecordHistory().records.map { HistoryModel(it) }
            }) {
                binding?.progress?.hide()
            }
        }
    }
}