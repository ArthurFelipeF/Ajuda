package com.example.poupae.recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.poupae.databinding.ValoresItemBinding
import com.example.poupae.model.ItemTransacao
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.poupae.model.TipoTransacao
import androidx.core.content.ContextCompat
import com.example.poupae.R

class PostsListAdapter(
    private val context: Context,
    private val transacoes: List<ItemTransacao>,
    private val onItemLongClickListener: (ItemTransacao) -> Unit
) : RecyclerView.Adapter<PostsListAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val valoresItemBinding: ValoresItemBinding
    ) : RecyclerView.ViewHolder(valoresItemBinding.root) {

        init {
            valoresItemBinding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClickListener.invoke(transacoes[position])
                }
                true
            }
        }

        fun bind(itemTransacao: ItemTransacao) {
            valoresItemBinding.txtTitle.text = itemTransacao.title

            val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            valoresItemBinding.txtValor.text = formatoMoeda.format(itemTransacao.valor)


            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            valoresItemBinding.txtData.text = dateFormat.format(Date(itemTransacao.date))

            if (itemTransacao.tip == TipoTransacao.DESPESA) {
                valoresItemBinding.txtValor.setTextColor(ContextCompat.getColor(valoresItemBinding.root.context, R.color.despesa_color))
            } else {
                valoresItemBinding.txtValor.setTextColor(ContextCompat.getColor(valoresItemBinding.root.context, R.color.renda_color))
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(
            ValoresItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = transacoes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemTransacao = transacoes[position]
        holder.bind(itemTransacao)
    }
}