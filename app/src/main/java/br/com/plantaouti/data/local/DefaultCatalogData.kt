package br.com.plantaouti.data.local

import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import br.com.plantaouti.data.local.entity.MaterialCatalogEntity

object DefaultCatalogData {

    /**
     * Lista padrão pré-cadastrada das 17 drogas vasoativas / infusões contínuas de UTI.
     * Editável pelo profissional nas Configurações, com opção de restaurar padrão.
     */
    val defaultDrugs: List<DrugCatalogEntity> = listOf(
        DrugCatalogEntity(name = "Amiodarona", unit = "mg/min", referenceRangeLabel = "1 mg/min por 6h, depois 0,5 mg/min", minDose = 0.5, maxDose = 1.0),
        DrugCatalogEntity(name = "Cetamina", unit = "mg/kg/h", referenceRangeLabel = "0,2 – 0,8", minDose = 0.2, maxDose = 0.8),
        DrugCatalogEntity(name = "Cisatracúrio", unit = "mg/h", referenceRangeLabel = "5 – 37,5", minDose = 5.0, maxDose = 37.5),
        DrugCatalogEntity(name = "Dexmedetomidina (Precedex)", unit = "mcg/kg/h", referenceRangeLabel = "0,2 – 1,4", minDose = 0.2, maxDose = 1.4),
        DrugCatalogEntity(name = "Dobutamina", unit = "mcg/kg/min", referenceRangeLabel = "2,5 – 20", minDose = 2.5, maxDose = 20.0),
        DrugCatalogEntity(name = "Epinefrina", unit = "mcg/kg/min", referenceRangeLabel = "0,01 – 1", minDose = 0.01, maxDose = 1.0),
        DrugCatalogEntity(name = "Fentanil", unit = "mcg/kg/h", referenceRangeLabel = "0,5 – 3", minDose = 0.5, maxDose = 3.0),
        DrugCatalogEntity(name = "Lidocaína", unit = "mg/min", referenceRangeLabel = "1 – 4", minDose = 1.0, maxDose = 4.0),
        DrugCatalogEntity(name = "Midazolam", unit = "mg/kg/h", referenceRangeLabel = "0,05 – 0,2", minDose = 0.05, maxDose = 0.2),
        DrugCatalogEntity(name = "Morfina", unit = "mg/h", referenceRangeLabel = "1 – 10", minDose = 1.0, maxDose = 10.0),
        DrugCatalogEntity(name = "Nitroglicerina (Tridil)", unit = "mcg/min", referenceRangeLabel = "5 – 200", minDose = 5.0, maxDose = 200.0),
        DrugCatalogEntity(name = "Nitroprussiato (Nipride)", unit = "mcg/kg/min", referenceRangeLabel = "0,5 – 10", minDose = 0.5, maxDose = 10.0),
        DrugCatalogEntity(name = "Norepinefrina (Noradrenalina)", unit = "mcg/kg/min", referenceRangeLabel = "0,01 – 10", minDose = 0.01, maxDose = 10.0),
        DrugCatalogEntity(name = "Propofol", unit = "mg/kg/h", referenceRangeLabel = "0,5 – 4", minDose = 0.5, maxDose = 4.0),
        DrugCatalogEntity(name = "Remifentanil", unit = "mcg/kg/min", referenceRangeLabel = "0,05 – 0,2", minDose = 0.05, maxDose = 0.2),
        DrugCatalogEntity(name = "Tirofiban", unit = "mcg/kg/min", referenceRangeLabel = "0,15 por 12 h", minDose = 0.15, maxDose = 0.15),
        DrugCatalogEntity(name = "Vasopressina (Encrise)", unit = "UI/min", referenceRangeLabel = "0,01 – 0,03", minDose = 0.01, maxDose = 0.03)
    )

    val defaultMaterials: List<MaterialCatalogEntity> = listOf(
        "Seringa 1 mL",
        "Seringa 3 mL",
        "Seringa 5 mL",
        "Seringa 10 mL",
        "Seringa 20 mL",
        "Agulha 40x12",
        "Agulha 25x7",
        "Equipo macrogotas",
        "Equipo bomba de infusão",
        "Equipo fotossensível",
        "Torneirinha 3 vias",
        "Luva de procedimento",
        "Luva estéril",
        "Gaze estéril (pacote)",
        "Cateter venoso periférico",
        "Soro fisiológico 0,9% 100 mL",
        "Soro fisiológico 0,9% 250 mL",
        "Soro fisiológico 0,9% 500 mL",
        "Soro glicosado 5% 250 mL",
        "Eletrodo cardíaco",
        "Sonda de aspiração traqueal",
        "Filtro HME",
        "Swab com álcool 70%",
        "Micropore / Esparadrapo",
        "Fralda descartável"
    ).map { MaterialCatalogEntity(name = it) }
}
