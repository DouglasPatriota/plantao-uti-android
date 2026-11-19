package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro imutável (append-only) de toda alteração de dose ou taxa de infusão.
 * Nunca sobrescreve registros anteriores.
 */
@Entity(
    tableName = "infusion_history",
    foreignKeys = [
        ForeignKey(
            entity = InfusionEntity::class,
            parentColumns = ["id"],
            childColumns = ["infusionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("infusionId")]
)
data class InfusionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val infusionId: Long,
    val dose: String,
    val rateMlH: String,
    val unit: String,
    val recordedAtMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
