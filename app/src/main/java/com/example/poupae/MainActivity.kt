package com.example.poupae

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.poupae.databinding.ActivityMainBinding
import com.example.poupae.model.ItemTransacao
import com.example.poupae.model.TipoTransacao
import com.example.poupae.recyclerview.adapter.PostsListAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import com.example.poupae.model.TransacaoDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat // Importe ContextCompat para cores


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val transacoes = mutableListOf<ItemTransacao>()
    private lateinit var adapter: PostsListAdapter
    private lateinit var database: AppDatabase
    private lateinit var transacaoDao: TransacaoDao

    // Launcher para adicionar/editar transações
    private val transacaoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Não precisamos mais do extra "novaTransacao" aqui,
                // pois as operações (inserir/atualizar) já são feitas na ItemTransacaoActivity
                // e o Room Flow vai automaticamente atualizar a lista.
                Toast.makeText(this, "Operação de transação concluída!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Operação de transação cancelada.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getDatabase(this)
        transacaoDao = database.transacaoDao()

        // Inicialize o adapter passando o novo listener de clique longo
        adapter = PostsListAdapter(this, transacoes) { itemTransacaoClicado ->
            // Listener de clique longo: Mostrar opções Editar/Excluir
            val options = arrayOf("Editar", "Excluir")
            MaterialAlertDialogBuilder(this)
                .setTitle("Escolha uma opção")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> { // Clicou em "Editar"
                            val intent = Intent(this, ItemTransacaoActivity::class.java).apply {
                                putExtra("itemParaEditar", itemTransacaoClicado) // Passa o item para edição
                            }
                            transacaoResultLauncher.launch(intent)
                        }
                        1 -> { // Clicou em "Excluir"
                            // Agora mostra o diálogo de confirmação de exclusão
                            showDeleteConfirmationDialog(itemTransacaoClicado)
                        }
                    }
                }
                .show()
        }
        binding.recycleview.adapter = adapter

        lifecycleScope.launch {
            transacaoDao.getAllTransacoes().collectLatest { fetchedTransacoes ->
                transacoes.clear()
                transacoes.addAll(fetchedTransacoes)
                adapter.notifyDataSetChanged()
                atualizarSaldos()
            }
        }

        binding.btnItem.setOnClickListener {
            val intent = Intent(this, ItemTransacaoActivity::class.java)
            transacaoResultLauncher.launch(intent)
        }
    }

    private fun showDeleteConfirmationDialog(itemTransacao: ItemTransacao) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir a transação '${itemTransacao.title}'?")
            .setPositiveButton("Sim") { dialog, which ->
                lifecycleScope.launch {
                    transacaoDao.delete(itemTransacao)
                    Toast.makeText(this@MainActivity, "Transação excluída!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun atualizarSaldos() {
        val rendaTotal = transacoes.filter { it.tip == TipoTransacao.RENDA }.sumOf { it.valor }
        val despesaTotal = transacoes.filter { it.tip == TipoTransacao.DESPESA }.sumOf { it.valor }
        val saldoGeral = rendaTotal - despesaTotal

        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        binding.txtRenda.text = formatoMoeda.format(rendaTotal)
        binding.txtDespesa.text = formatoMoeda.format(-despesaTotal)
        binding.txtGeral.text = formatoMoeda.format(saldoGeral)

        if (saldoGeral >= 0) {
            binding.txtGeral.setTextColor(ContextCompat.getColor(this, R.color.renda_color))
        } else {
            binding.txtGeral.setTextColor(ContextCompat.getColor(this, R.color.despesa_color))
        }
    }
}