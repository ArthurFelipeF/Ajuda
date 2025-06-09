
package com.example.poupae

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private lateinit var adapter: PostsListAdapter
    private lateinit var database: AppDatabase
    private lateinit var transacaoDao: TransacaoDao

    private val adicionarTransacaoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

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


        adapter = PostsListAdapter()
        binding.recycleview.adapter = adapter

        lifecycleScope.launch {
            transacaoDao.getAllTransacoes().collectLatest { fetchedTransacoes ->

                adapter.submitList(fetchedTransacoes)

                atualizarSaldos(fetchedTransacoes)
            }
        }

        binding.btnItem.setOnClickListener {
            val intent = Intent(this, ItemTransacaoActivity::class.java)
            adicionarTransacaoLauncher.launch(intent)
        }
    }


    private fun atualizarSaldos(transacoes: List<ItemTransacao>) {
        val rendaTotal = transacoes.filter { it.tip == TipoTransacao.RENDA }.sumOf { it.valor }
        val despesaTotal = transacoes.filter { it.tip == TipoTransacao.DESPESA }.sumOf { it.valor }
        val saldoGeral = rendaTotal - despesaTotal

        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        binding.txtRenda.text = formatoMoeda.format(rendaTotal)
        binding.txtDespesa.text = formatoMoeda.format(-despesaTotal)
        binding.txtGeral.text = formatoMoeda.format(saldoGeral)
    }
}