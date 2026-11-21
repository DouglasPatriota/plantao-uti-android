package br.com.plantaouti.domain.model

enum class NursingCategory(val label: String) {
    TODOS("Todos"),
    ADMISSAO("Admissão"),
    EVOLUCAO("Evolução diária"),
    MEDICAMENTOS("Medicamentos"),
    CURATIVOS("Curativos"),
    SINAIS_ALTERADOS("Sinais alterados"),
    ALTA("Alta hospitalar"),
    TRANSFERENCIA("Transferência"),
    INTERCORRENCIAS("Intercorrências"),
    TROCA_PLANTAO("Troca de plantão")
}

data class NursingNoteTemplate(
    val id: String,
    val title: String,
    val category: NursingCategory,
    val content: String
)

object NursingNoteCatalog {
    val templates = listOf(
        NursingNoteTemplate(
            id = "1",
            title = "Admissão em UTI de Paciente Crítico",
            category = NursingCategory.ADMISSAO,
            content = """Paciente admitido na UTI proveniente do Pronto Socorro / Centro Cirúrgico, transportado em maca, sob monitorização contínua. Consciente / sedado sob Ramsay X. Vias aéreas pervas / sob Ventilação Mecânica em modo [MODO], parâmetros conforme prescrição. Acesso venoso central / periférico em [LOCAL], salinizado / em infusão contínua de [DROGA]. Sinais vitais de admissão checados e registrados no leito. Mencionados familiares orientados sobre normas da unidade."""
        ),
        NursingNoteTemplate(
            id = "2",
            title = "Evolução Diária de Enfermagem (Geral UTI)",
            category = NursingCategory.EVOLUCAO,
            content = """Paciente mantido em decúbito elevado a 30º, sob monitorização hemodinâmica contínua. Ritmo cardíaco sinusal / alerta sem arritmias agudas. Ausculta pulmonar com murmúrio vesicular presente bilateralmente, sem ruídos adventícios. Acesso venoso pérvio sem sinais flogísticos em local de inserção. Diurese clara por SVD / Fralda / Vaso em volume adequado. Balanço hídrico parcial mantido. Realizada mudança de decúbito a cada 2h conforme protocolo de LPP."""
        ),
        NursingNoteTemplate(
            id = "3",
            title = "Administração de Drogas Vasoativas / Medicamentos de Alta Vigilância",
            category = NursingCategory.MEDICAMENTOS,
            content = """Iniciada / ajustada infusão de [Norepinefrina / Dobutamina / Midazolam] em Bomba de Infusão Contínua (BIC) a [X] mL/h por Acesso Venoso Central em MEE / MSD. Dupla checagem realizada antes da administração (droga, concentração, dose e paciente). Monitorização contínua de Pressão Arterial Média (PAM) e Frequência Cardíaca mantida. Sem episódios de extravasamento no local da punção."""
        ),
        NursingNoteTemplate(
            id = "4",
            title = "Realização de Curativo de Cateter Venoso Central (CVC)",
            category = NursingCategory.CURATIVOS,
            content = """Realizada troca de curativo de CVC em região subclávia / jugular direita / esquerda com técnica asséptica. Limpeza de óstio realizada com clorexidina alcoólica 0,5%. Óstio de inserção sem hiperemia, sem secreções ou sinais flogísticos. Fixação e cobertura filme transparente estéril aplicadas com identificação de data, hora e responsável."""
        ),
        NursingNoteTemplate(
            id = "5",
            title = "Registro de Sinais Vitais Alterados (Hipotensão / Febre)",
            category = NursingCategory.SINAIS_ALTERADOS,
            content = """Identificada alteração em sinais vitais no horário de [HH:mm]: PA [PASxPAD] mmHg (PAM [X] mmHg), Temp [X] ºC, FC [X] bpm, SpO₂ [X]%. Paciente assintomático / manifestando [queixa]. Comunicado ao médico plantor Dr.(a) [NOME] às [HH:mm]. Solicitada medicação conforme prescrição / coleta de hemoculturas e instaladas medidas físicas conforme orientação médica."""
        ),
        NursingNoteTemplate(
            id = "6",
            title = "Transferência Interna de Paciente para Unidade de Internação",
            category = NursingCategory.TRANSFERENCIA,
            content = """Paciente transferido da UTI para a Unidade de Internação (Leito [X]), em maca com grades elevadas, sob acompanhamento da equipe de enfermagem. Estável hemodinamicamente, em ar ambiente / cateter nasal O₂ a [X] L/min. Acesso venoso salinizado pérvio. Entregue prontuário impresso, folha de balanço hídrico e medicamentos pessoais ao enfermeiro responsável pelo setor."""
        ),
        NursingNoteTemplate(
            id = "7",
            title = "Registro de Intercorrência (Parada Cardiorrespiratória / Extubação)",
            category = NursingCategory.INTERCORRENCIAS,
            content = """Às [HH:mm], paciente apresentou rebaixamento do nível de consciência / PCR / insuficiência respiratória aguda. Chamada equipe médica imediatamente. Iniciadas manobras de RCP conforme protocolo ACLS / auxílio na intubação orotraqueal com tubo nº [X]. Administradas medicações de emergência conforme orientação verbal do líder da equipe. Retorno à circulação espontânea às [HH:mm]."""
        ),
        NursingNoteTemplate(
            id = "8",
            title = "Passagem e Troca de Plantão de Enfermagem",
            category = NursingCategory.TROCA_PLANTAO,
            content = """Passagem de plantão realizada à beira do leito ao técnico/enfermeiro [NOME]. Paciente calmo, sedado / orientado, mantendo infusões contínuas de [DROGAS] em BIC. Sinais vitais checados e dentro da meta. Dispositivos pérvios: CVC, SVD e AVP. Balanço hídrico zerado e folha de registro atualizada sem pendências operacionais."""
        ),
        NursingNoteTemplate(
            id = "9",
            title = "Alta Hospitalar / Alta da Unidade de Terapia Intensiva",
            category = NursingCategory.ALTA,
            content = """Paciente recebe alta da Unidade de Terapia Intensiva às [HH:mm] conforme relatório médico. Consciente, orientado, eupnéico em ar ambiente. Sinais vitais de saída registrados. Retirados dispositivos invasivos desnecessários conforme ordem médica. Orientados paciente e acompanhante sobre cuidados de alta."""
        )
    )
}
