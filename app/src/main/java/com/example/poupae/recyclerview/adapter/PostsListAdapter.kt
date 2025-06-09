package com.example.poupae.recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Importe ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poupae.databinding.ValoresItemBinding
import com.example.poupae.model.ItemTransacao
import com.example.poupae.model.TipoTransacao
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PostsListAdapter : ListAdapter<ItemTransacao, PostsListAdapter.ViewHolder>(ItemTransacaoDiffCallback()) {


    class ViewHolder(
        private val valoresItemBinding: ValoresItemBinding
    ) : RecyclerView.ViewHolder(valoresItemBinding.root) {


        private val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        fun bind(itemTransacao: ItemTransacao) {
            valoresItemBinding.txtTitle.text = itemTransacao.title

            if (itemTransacao.tip == TipoTransacao.DESPESA) {
                valoresItemBinding.txtValor.text = formatoMoeda.format(-itemTransacao.valor)
            } else {
                valoresItemBinding.txtValor.text = formatoMoeda.format(itemTransacao.valor)
            }

            valoresItemBinding.txtData.text = dateFormat.format(Date(itemTransacao.date))
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(
            ValoresItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemTransacao = getItem(position)
        holder.bind(itemTransacao)
    }


    class ItemTransacaoDiffCallback : DiffUtil.ItemCallback<ItemTransacao>() {
        override fun areItemsTheSame(oldItem: ItemTransacao, newItem: ItemTransacao): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemTransacao, newItem: ItemTransacao): Boolean {
            return oldItem == newItem
        }
    }
}