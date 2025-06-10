package com.example.poupae

import android.app.DatePickerDialog
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
import com.example.poupae.model.TransacaoDao
import com.example.poupae.AppDatabase
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class ItemTransacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemTransacaoBinding
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var originalTransacao: ItemTransacao? = null
    private lateinit var transacaoDao: TransacaoDao

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


        transacaoDao = AppDatabase.getDatabase(this).transacaoDao()


        originalTransacao = intent.getParcelableExtra("itemParaEditar")
        originalTransacao?.let { item ->

            binding.edtNome.setText(item.title)
            binding.edtValor.setText(item.valor.toString())
            selectedDateMillis = item.date
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            updateDateTextView(calendar)

            if (item.tip == TipoTransacao.RENDA) {
                binding.radioRenda.isChecked = true
            } else {
                binding.radioDespesa.isChecked = true
            }
            binding.btnAddItem.text = "Salvar Alterações"
        } ?: run {

            val calendar = Calendar.getInstance()
            selectedDateMillis = calendar.timeInMillis
            updateDateTextView(calendar)
        }



        binding.btnVoltar.setOnClickListener {
            finish()
        }


        binding.txtDataSelecionada.setOnClickListener {
            showDatePickerDialog()
        }


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

                transacaoFinal = originalTransacao!!.copy(
                    title = title,
                    valor = valor,
                    tip = tipoTransacao,
                    date = selectedDateMillis
                )
                lifecycleScope.launch {
                    transacaoDao.update(transacaoFinal)
                    Toast.makeText(this@ItemTransacaoActivity, "Transação atualizada!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } else {

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