package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "material_catalog",
    indices = [Index(value = ["name"], unique = true)]
)
data class MaterialCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val isDefault: Boolean = true
)
