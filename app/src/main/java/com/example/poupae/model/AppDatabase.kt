package com.example.poupae

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration // Importe Migration
import androidx.sqlite.db.SupportSQLiteDatabase // Importe SupportSQLiteDatabase
import com.example.poupae.model.ItemTransacao
import com.example.poupae.model.TransacaoDao

// Mude a versão de 1 para 2
@Database(entities = [ItemTransacao::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transacaoDao(): TransacaoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Objeto de Migração para adicionar a coluna 'date'
        private val MIGRATION_1_2 = object : Migration(1, 2) { // De versão 1 para versão 2
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adiciona a nova coluna 'date' à tabela 'transacoes'
                // DEFAULT 0L significa que as transações existentes terão 0L como data por padrão
                // (ou seja, 1 de janeiro de 1970 00:00:00 GMT - Epoch)
                // Você pode escolher outro valor padrão se preferir, como a data atual do momento da migração.
                // Mas para simplicidade, 0L é um padrão comum para Long.
                database.execSQL("ALTER TABLE transacoes ADD COLUMN date INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poupae_database"
                )
                    .addMigrations(MIGRATION_1_2) // Adicione a migração aqui
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}