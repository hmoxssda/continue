package com.example.networkanalyzer.ui.main

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.networkanalyzer.databinding.ActivityMainBinding
import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.service.ProxyVpnService
import com.example.networkanalyzer.ui.detail.RequestDetailActivity
import com.example.networkanalyzer.util.TrafficLogger

class MainActivity : AppCompatActivity(), NetworkRequestAdapter.Callback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = NetworkRequestAdapter(this)

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startProxyService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TrafficLogger.init(applicationContext)

        setupRecyclerView()
        setupActions()
        observeRequests()
    }

    private fun setupRecyclerView() {
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.requestRecyclerView.adapter = adapter
    }

    private fun setupActions() {
        binding.toggleCaptureButton.setOnClickListener {
            val currentState = viewModel.proxyState.value
            if (currentState?.isRunning == true) {
                stopService(Intent(this, ProxyVpnService::class.java))
                viewModel.updateProxyState(isRunning = false)
            } else {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    startProxyService()
                }
            }
        }

        binding.filterEditText.setOnEditorActionListener { _, _, _ ->
            viewModel.setFilter(binding.filterEditText.text?.toString())
            true
        }
    }

    private fun startProxyService() {
        val intent = Intent(this, ProxyVpnService::class.java)
        startForegroundService(intent)
        viewModel.updateProxyState(isRunning = true)
    }

    private fun observeRequests() {
        viewModel.requests.observe(this) { requests ->
            binding.requestRecyclerView.isVisible = requests.isNotEmpty()
            adapter.submitList(requests)
        }

        viewModel.proxyState.observe(this) { state ->
            binding.toggleCaptureButton.text = if (state.isRunning) {
                getString(com.example.networkanalyzer.R.string.stop_capture)
            } else {
                getString(com.example.networkanalyzer.R.string.start_capture)
            }
        }
    }

    override fun onRequestSelected(request: NetworkRequest) {
        val intent = Intent(this, RequestDetailActivity::class.java)
        intent.putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, request.id)
        startActivity(intent)
    }
}
