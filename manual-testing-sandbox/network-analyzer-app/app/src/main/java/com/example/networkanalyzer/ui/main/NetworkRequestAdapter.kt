package com.example.networkanalyzer.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.networkanalyzer.databinding.ItemRequestBinding
import com.example.networkanalyzer.model.NetworkRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkRequestAdapter(private val callback: Callback) :
    ListAdapter<NetworkRequest, NetworkRequestAdapter.RequestViewHolder>(DiffCallback) {

    interface Callback {
        fun onRequestSelected(request: NetworkRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRequestBinding.inflate(inflater, parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        fun bind(request: NetworkRequest) {
            binding.urlTextView.text = request.url
            binding.methodTextView.text = "${request.method} • ${request.responseCode ?: "--"}"
            binding.statusTextView.text = "${request.responseCode ?: "Pending"}"
            binding.timeTextView.text = formatter.format(Date(request.timestamp))
            binding.root.setOnClickListener { callback.onRequestSelected(request) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<NetworkRequest>() {
        override fun areItemsTheSame(oldItem: NetworkRequest, newItem: NetworkRequest): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NetworkRequest, newItem: NetworkRequest): Boolean =
            oldItem == newItem
    }
}
