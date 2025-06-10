package com.example.poupae.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransacaoDao {
    @Query("SELECT * FROM transacoes ORDER BY id DESC")
    fun getAllTransacoes(): Flow<List<ItemTransacao>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(itemTransacao: ItemTransacao)

    @Delete
    suspend fun delete(itemTransacao: ItemTransacao)

    @Update // Adicione este método para atualizar transações existentes
    suspend fun update(itemTransacao: ItemTransacao)

    @Query("DELETE FROM transacoes WHERE id = :transacaoId")
    suspend fun deleteById(transacaoId: Long)
}