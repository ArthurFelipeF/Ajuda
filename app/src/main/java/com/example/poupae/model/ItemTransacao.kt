package com.example.poupae.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "transacoes")
data class ItemTransacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val valor: Double,
    val tip: TipoTransacao,
    val date: Long
) : Parcelable

@Parcelize
enum class TipoTransacao : Parcelable {
    RENDA,
    DESPESA
}