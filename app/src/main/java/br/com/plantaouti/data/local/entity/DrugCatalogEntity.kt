package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Catálogo editável de drogas vasoativas e sedativos/analgésicos em infusão contínua.
 */
@Entity(tableName = "drug_catalog")
data class DrugCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val unit: String,
    val referenceRangeLabel: String,
    val minDose: Double?,
    val maxDose: Double?,
    val isDefault: Boolean = true
)
