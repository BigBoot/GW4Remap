package de.bigboot.watch4payswitch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.bigboot.watch4payswitch.databinding.ActivitySelectPredefinedItemBinding

class SelectPredefinedAdapter(private val items: List<Item>): RecyclerView.Adapter<SelectPredefinedAdapter.ViewHolder>() {
    data class Item(val name: String, val value: String)

    var onItemSelected: ((item: Item)->Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(ActivitySelectPredefinedItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.text.text = item.name
        binding.root.setOnClickListener { onItemSelected?.invoke(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ActivitySelectPredefinedItemBinding): RecyclerView.ViewHolder(binding.root)
}