
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

class ItemTransacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemTransacaoBinding
    private val calendar = Calendar.getInstance()

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

        binding.btnVoltar.setOnClickListener {
            finish()
        }


        updateDateTextView(calendar)

        binding.txtDataSelecionada.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnAddItem.setOnClickListener {

            val title = binding.edtNome.text.toString().trim()
            val valorStr = binding.edtValor.text.toString().trim()
            val selectedRadioId = binding.radioGroupTipo.checkedRadioButtonId

            if (title.isEmpty() || valorStr.isEmpty() || selectedRadioId == -1) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = try {
                valorStr.replace(",", ".").toDouble() // Garante que a conversão seja com ponto
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipoTransacao: TipoTransacao
            when (selectedRadioId) {
                binding.radioRenda.id -> tipoTransacao = TipoTransacao.RENDA
                binding.radioDespesa.id -> tipoTransacao = TipoTransacao.DESPESA
                else -> {
                    Toast.makeText(this, "Erro ao determinar o tipo de transação.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            val novaTransacao = ItemTransacao(
                title = title,
                valor = valor,
                tip = tipoTransacao,
                date = calendar.timeInMillis
            )


            val resultIntent = Intent().apply {
                putExtra("novaTransacao", novaTransacao)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateDateTextView(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        binding.txtDataSelecionada.text = dateFormat.format(calendar.time)
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                updateDateTextView(calendar)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}