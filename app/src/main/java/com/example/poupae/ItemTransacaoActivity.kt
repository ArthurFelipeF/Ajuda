package com.example.poupae

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.poupae.databinding.ActivityItemTransacaoBinding
import com.example.poupae.model.ItemTransacao
import com.example.poupae.model.TipoTransacao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.poupae.model.TransacaoDao // Importe TransacaoDao
import com.example.poupae.AppDatabase // Importe AppDatabase
import kotlinx.coroutines.launch // Importe launch
import androidx.lifecycle.lifecycleScope // Importe lifecycleScope

class ItemTransacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemTransacaoBinding
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var originalTransacao: ItemTransacao? = null // Para armazenar o item original em modo de edição
    private lateinit var transacaoDao: TransacaoDao // Declarar o DAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityItemTransacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialize o DAO
        transacaoDao = AppDatabase.getDatabase(this).transacaoDao()

        // Verifica se veio um ItemTransacao para edição
        originalTransacao = intent.getParcelableExtra("itemParaEditar") // Chave usada para passar o item
        originalTransacao?.let { item ->
            // Se um item foi passado, estamos em modo de edição
            binding.edtNome.setText(item.title)
            binding.edtValor.setText(item.valor.toString())
            selectedDateMillis = item.date // Carrega a data do item original
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            updateDateTextView(calendar) // Atualiza o TextView da data

            if (item.tip == TipoTransacao.RENDA) {
                binding.radioRenda.isChecked = true
            } else {
                binding.radioDespesa.isChecked = true
            }
            binding.btnAddItem.text = "Salvar Alterações" // Muda o texto do botão
        } ?: run {
            // Se nenhum item foi passado, estamos em modo de adição, define a data atual como padrão
            val calendar = Calendar.getInstance()
            selectedDateMillis = calendar.timeInMillis
            updateDateTextView(calendar)
        }


        // Lógica para o botão de voltar
        binding.btnVoltar.setOnClickListener {
            finish()
        }

        // Lógica para o campo de data
        binding.txtDataSelecionada.setOnClickListener {
            showDatePickerDialog()
        }

        // Lógica para o botão de adicionar/salvar item
        binding.btnAddItem.setOnClickListener {
            val title = binding.edtNome.text.toString().trim()
            val valorText = binding.edtValor.text.toString().trim()
            val selectedRadioId = binding.radioGroup.checkedRadioButtonId

            if (title.isEmpty()) {
                Toast.makeText(this, "Por favor, digite o nome da transação.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (valorText.isEmpty()) {
                Toast.makeText(this, "Por favor, digite o valor da transação.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val valor = valorText.toDoubleOrNull()
            if (valor == null) {
                Toast.makeText(this, "Valor inválido. Use apenas números e ponto decimal.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedRadioId == -1) {
                Toast.makeText(this, "Por favor, selecione o tipo de transação (Renda/Despesa).", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.txtDataSelecionada.text.toString() == "Selecione a Data" || binding.txtDataSelecionada.text.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione a data da transação.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipoTransacao = when (selectedRadioId) {
                R.id.radioRenda -> TipoTransacao.RENDA
                R.id.radioDespesa -> TipoTransacao.DESPESA
                else -> {
                    Toast.makeText(this, "Erro ao determinar o tipo de transação.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val transacaoFinal: ItemTransacao

            if (originalTransacao != null) {
                // Modo de edição: cria uma nova instância com os dados atualizados e o ID original
                transacaoFinal = originalTransacao!!.copy(
                    title = title,
                    valor = valor,
                    tip = tipoTransacao,
                    date = selectedDateMillis
                )
                lifecycleScope.launch {
                    transacaoDao.update(transacaoFinal)
                    Toast.makeText(this@ItemTransacaoActivity, "Transação atualizada!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Apenas sinaliza que houve uma atualização
                    finish()
                }
            } else {
                // Modo de adição: cria uma nova transação
                transacaoFinal = ItemTransacao(
                    title = title,
                    valor = valor,
                    tip = tipoTransacao,
                    date = selectedDateMillis
                )
                lifecycleScope.launch {
                    transacaoDao.insert(transacaoFinal)
                    Toast.makeText(this@ItemTransacaoActivity, "Transação adicionada!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Sinaliza que houve uma adição
                    finish()
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateMillis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDateMillis = newCalendar.timeInMillis
                updateDateTextView(newCalendar)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateDateTextView(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        binding.txtDataSelecionada.text = dateFormat.format(calendar.time)
    }
}