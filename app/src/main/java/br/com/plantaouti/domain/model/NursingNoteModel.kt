package br.com.plantaouti.domain.model

enum class NursingCategory(val label: String) {
    TODOS("Todos"),
    ADMISSAO("Admissão na UTI"),
    EVOLUCAO("Evolução e Exame Físico"),
    VENTILACAO_RESPIRACAO("Ventilação & Vias Aéreas"),
    DISPOSITIVOS_CATETERES("Cateteres, CVC & Sondas"),
    MEDICAMENTOS("Drogas Vasoativas & BI"),
    BALANCO_ELIMINACOES("Balanço Hídrico & Drenos"),
    CURATIVOS_LPP("Curativos & Lesão por Pressão"),
    PROCEDIMENTOS("Procedimentos & Banho Leito"),
    SINAIS_ALTERADOS("Sinais Vitais & Alertas"),
    INTERCORRENCIAS("Intercorrências e PCR"),
    TROCA_PLANTAO("Passagem de Plantão"),
    TRANSFERENCIA("Transferência Interna"),
    ALTA("Alta da UTI")
}

data class NursingNoteTemplate(
    val id: String,
    val title: String,
    val category: NursingCategory,
    val content: String
)

object NursingNoteCatalog {
    val templates = listOf(
        // --- 1. ADMISSÃO ---
        NursingNoteTemplate(
            id = "101",
            title = "Admissão em UTI de Paciente Crítico (VM / Sedado)",
            category = NursingCategory.ADMISSAO,
            content = """Paciente admitido na Unidade de Terapia Intensiva proveniente do Pronto Socorro / Centro Cirúrgico / Enfermaria às [HH:mm], transportado em maca com grades elevadas sob monitorização multiparamétrica contínua. Encontra-se restrito ao leito, sedado sob escala RASS [Ex.: -4], com Tubo Orotraqueal (TOT) nº [X] fixado a [X] cm da comissura labial, acoplado a Ventilação Mecânica em modo [PCV / VCV], PEEP [X] cmH2O, FiO2 [X]%, FR [X] irpm. Acesso Venoso Central (CVC) em jugular / subclávia [D / E] salinizado / com infusão contínua por Bomba de Infusão (BI) de [Noradrenalina / Fentanil / Midazolam] a [X] mL/h. Sonda Nasoentérica (SNE) em narina [D / E] aberta / em refluxo. Sonda Vesical de Demora (SVD) drenando urina de aspecto [límpido / âmbar]. Sinais vitais de admissão checados e registrados na folha digital do leito. Familiares e acompanhantes orientados sobre normas, horários de boletim e visita da UTI."""
        ),
        NursingNoteTemplate(
            id = "102",
            title = "Admissão Pós-Operatória em UTI (Cirurgia de Grande Porte)",
            category = NursingCategory.ADMISSAO,
            content = """Admitido na UTI às [HH:mm] em pós-operatório imediato (PO1) de [Cirurgia / Laparotomia / Colecistectomia / Revascularização], proveniente do Centro Cirúrgico / SRPA, transportado em maca sob oxigenoterapia / monitorização contínua. Paciente consciente / sonolento, calmo, Glasgow [15], relata ausência de dor / dor leve (EVD [X]/10). Curativo cirúrgico abdominal / torácico limpo e seco sem sangramentos aparentes. Dreno de [Jackson-Pratt / Portovac] em região [X] drenando secreção [serosa / hemática] em quantidade de [X] mL. Mantém Acesso Venoso Periférico (AVP) em MSD / MSE salinizado / em venóclise de SF 0,9%. Sinais vitais estáveis. Segue sob monitorização e cuidados de enfermagem."""
        ),

        // --- 2. EVOLUÇÃO E EXAME FÍSICO ---
        NursingNoteTemplate(
            id = "201",
            title = "Evolução Diária de Enfermagem em UTI (Geral / Cefalocaudal)",
            category = NursingCategory.EVOLUCAO,
            content = """Paciente mantido em decúbito elevado a 30º, restrito ao leito, sob monitorização hemodinâmica contínua. Nível de consciência: sedado sob RASS [X] / lúcido e orientado (Glasgow 15). Pupilas isocóricas e fotorreagentes. Mucosas coradas e hidratadas. Ausculta pulmonar: murmúrios vesiculares presentes bilateralmente com / sem ruídos adventícios (estertores / roncos em bases). Ausculta cardíaca: bulhas normofonéticas em 2 tempos sem sopros. Abdome globoso / plano, flácido, ruídos hidroaéreos (RHA) presentes, indolor à palpação. Mantém CVC em jugular [D / E] com infusão de sedativo / vasoativo em BI, curativo com filme transparente sem sinais flogísticos. Diurese por SVD com débito adequado em bolsa coletora. Mantida mudança de decúbito a cada 2 horas conforme protocolo de prevenção de LPP. Segue sob cuidados intensivistas."""
        ),
        NursingNoteTemplate(
            id = "202",
            title = "Evolução de Paciente Prontado / Decúbito Ventral (SDR7 / Síndrome Respiratória)",
            category = NursingCategory.EVOLUCAO,
            content = """Paciente mantido em decúbito ventral (posição prona) devido protocolo de SDR7 / hipoxemia grave, sedado e bloqueado sob RASS -5. Traqueostomia (TQT) / TOT nº [X] em VM com PEEP [X] cmH2O e FiO2 [X]%. Monitorização cardíaca contínua e oximetria de pulso mantidas. Realizada higienização corporal posterior com lenços antissépticos e curativo de lesão por pressão (LPP) em região sacra / occipital com antissepsia por PHMB e aplicação de cobertura hidrocoloide / Dersani / Cavilon spray. Realizada hidratação ocular com Epitezan e labial com Dersani. Alinhamento corporal e proteção de proeminências ósseas checados. Sem intercorrências durante a permanência no decúbito."""
        ),

        // --- 3. VENTILAÇÃO E VIAS AÉREAS ---
        NursingNoteTemplate(
            id = "301",
            title = "Manejo de Ventilação Mecânica, Cânula de TQT / TOT e Aspiração",
            category = NursingCategory.VENTILACAO_RESPIRACAO,
            content = """Paciente em Ventilação Mecânica invasiva via TOT nº [X] / Traqueostomia (TQT) cânula nº [X]. Pressão do cuff conferida com cufômetro mantida em [25 mmHg]. Parâmetros do ventilador: Modo [PCV], PEEP [X] cmH2O, FiO2 [X]%, FR [X] irpm. Realizada aspiração de vias aéreas por sistema fechado (sistema de aspiração fechado) com saída de secreção [mucóide / purulenta / sanguinolenta] em [pequena / média / grande] quantidade. Troca do cadarço / fixação do TOT realizada com mudança da posição do tubo na comissura labial para prevenção de LPP labial. Filtro HMEF / HEPA checado e trocado conforme validade."""
        ),
        NursingNoteTemplate(
            id = "302",
            title = "Oxigenoterapia e Desmame de Ventilação Mecânica (TRE)",
            category = NursingCategory.VENTILACAO_RESPIRACAO,
            content = """Iniciado Teste de Respiração Espontânea (TRE) em modo PSV / Tubo T com FiO2 [X]% conforme indicação médica. Paciente mantido vigilante, calmo, sem sinais de desconforto respiratório, sem uso de musculatura acessória, mantendo SpO2 > 94% e FR < 24 irpm. Sinais vitais checados durante o teste: PA [X] mmHg, FC [X] bpm. Teste concluído com sucesso às [HH:mm]. Aguarda avaliação médica para extubação orotraqueal."""
        ),

        // --- 4. CATETERES, CVC E SONDAS ---
        NursingNoteTemplate(
            id = "401",
            title = "Manutenção de Cateter Venoso Central (CVC) e Linha de PAI",
            category = NursingCategory.DISPOSITIVOS_CATETERES,
            content = """Realizada inspecção e manutenção de Acesso Venoso Central (CVC) em jugular / subclávia [D / E]. Óstio de inserção limpo, seco, sem secreções, hiperemia ou edema. Vias do cateter pervas com fluxo e refluxo preservados. Realizada manutenção de linha de Pressão Arterial Invasiva (PAI) em artéria radial [D / E] com bolsa pressurizada mantida a 300 mmHg e sistema zerado no nível do eixo flebostático. Curativo de PAI limpo e seco. Onda de curva arterial no monitor adequada sem amortecimento."""
        ),
        NursingNoteTemplate(
            id = "402",
            title = "Manutenção e Administração de Dieta por Sonda Nasoentérica (SNE)",
            category = NursingCategory.DISPOSITIVOS_CATETERES,
            content = """Posicionamento da Sonda Nasoentérica (SNE) em narina [D / E] conferido por teste de ausculta epigástrica / raio-X de controle. Fixação da SNE trocada e pele da aleta nasal limpa e hidratada. Infusão de dieta enteral [Nome da Dieta] mantida em Bomba de Infusão (BI) a [X] mL/h. Realizada lavagem prévia e posterior da via com 30 mL de água destilada. Paciente sem distensão abdominal ou episódios de vômitos / estase gástrica."""
        ),

        // --- 5. DROGAS VASOATIVAS E MEDICAMENTOS ---
        NursingNoteTemplate(
            id = "501",
            title = "Ajuste de Drogas Vasoativas / Sedação em Bomba de Infusão (BI)",
            category = NursingCategory.MEDICAMENTOS,
            content = """Realizado ajuste na taxa de infusão contínua de [Noradrenalina / Vasopressina / Dobutamina / Fentanil / Propofol / Precedex] em Bomba de Infusão (BI) por CVC de [X] mL/h para [X] mL/h (Dose: [X] mcg/kg/min) conforme ordem médica / protocolo de titulação hemodinâmica. Dupla checagem de droga, concentração e velocidade realizada. Monitorização contínua de PAM e FC mantida. Resposta hemodinâmica observada com elevação/estabilização da PAM em [X] mmHg."""
        ),
        NursingNoteTemplate(
            id = "502",
            title = "Administração de Medicamentos de Alto Risco / Eletrólitos EV",
            category = NursingCategory.MEDICAMENTOS,
            content = """Administrada infusão de [Cloreto de Potássio / Gluconato de Cálcio / Sulfato de Magnésio / Insulina Regular] via CVC / AVP por Bomba de Infusão Contínua conforme prescrição médica. Dupla checagem de identificação do paciente, via e velocidade de infusão realizada antes da conexão. Monitorização eletrocardiográfica mantida durante a infusão. Sem episódios de flebite ou dor no local da aplicação."""
        ),

        // --- 6. BALANÇO HÍDRICO E ELIMINAÇÕES ---
        NursingNoteTemplate(
            id = "601",
            title = "Controle de Balanço Hídrico, SVD e Débito Urinário",
            category = NursingCategory.BALANCO_ELIMINACOES,
            content = """Mantido controle rigoroso de balanço hídrico do leito. Sonda Vesical de Demora (SVD) nº [X] em sistema fechado drenando urina de coloração âmbar / citrina e aspecto límpido. Débito urinário acumulado no período: [X] mL (Média de [X] mL/kg/h). Bolsa coletora mantida abaixo do nível da bexiga e sem dobras na extensão. Balanço hídrico parcial do turno: Entradas [X] mL, Saídas [X] mL (Balanço [Positivo / Negativo] de [X] mL)."""
        ),
        NursingNoteTemplate(
            id = "602",
            title = "Registro de Eliminações Intestinais / Diurese em Fralda / Estomas",
            category = NursingCategory.BALANCO_ELIMINACOES,
            content = """Realizada troca de fralda descartável às [HH:mm]. Apresenta eliminação intestinal de aspecto [pastoso / semipastoso / líquido], em [pequena / média / grande] quantidade, sem sangue ou muco. Pele perianal e glútea limpa, higienizada e hidratada com pomada barreira (Dersani / Cavilon). Sonda de Jejunostomia / Ostomia em QSE pérvia, com bolsa coletora higienizada e sem vazamentos periféricos."""
        ),

        // --- 7. CURATIVOS E LESÃO POR PRESSÃO ---
        NursingNoteTemplate(
            id = "701",
            title = "Troca de Curativo de Cateter Venoso Central (CVC) com Clorexidina",
            category = NursingCategory.CURATIVOS_LPP,
            content = """Realizada troca do curativo de Acesso Venoso Central (CVC) em região jugular / subclávia [D / E] utilizando técnica estéril. Antissepsia de pele realizada com clorexidina alcoólica 0,5% e gaze estéril. Óstio de inserção sem sinais de infecção, secreções ou hiperemia. Aplicada cobertura transparente de filme de poliuretano com pad impregnado com CHG / curativo oculta estéril. Identificado com data [DD/MM/AAAA], hora e nome do responsável."""
        ),
        NursingNoteTemplate(
            id = "702",
            title = "Tratamento e Curativo de Lesão por Pressão (LPP Sacra / Occhipital)",
            category = NursingCategory.CURATIVOS_LPP,
            content = """Realizado curativo de Lesão por Pressão (LPP) em região sacra / calcânea / occipital. Antissepsia da lesão com solução de PHMB líquido e gaze estéril. Aplicada cobertura primária de pomada desbridante / hidrogel em tecido de necrose e Cavilon spray em pele perilesional. Cobertura secundária fixada com placa de hidrocoloide / espuma de poliuretano. Paciente mantido em protocolo de mudança de decúbito a cada 2 horas com colchão pneumático ativo."""
        ),

        // --- 8. PROCEDIMENTOS E BANHO NO LEITO ---
        NursingNoteTemplate(
            id = "801",
            title = "Banho no Leito Descartável / Antisséptico em Paciente Crítico",
            category = NursingCategory.PROCEDIMENTOS,
            content = """Realizado banho no leito completo às [HH:mm] utilizando lenços umedecidos antissépticos / clorexidina 2%. Realizada higiene oral com clorexidina 0,12% em gaze, hidratação ocular com Epitezan e labial com Dersani. Mudança e troca da fixação do TOT / SNE realizadas. Lençóis e fronhas trocados. Sinais vitais checados pós-procedimento com estabilidade hemodinâmica mantida."""
        ),
        NursingNoteTemplate(
            id = "802",
            title = "Coleta de Exames Laboratoriais, Gasometria Arterial e Hemoculturas",
            category = NursingCategory.PROCEDIMENTOS,
            content = """Realizada coleta de exames laboratoriais de rotina e controle microbiológico às [HH:mm]. Gasometria arterial coletada via linha de PAI em radial [D / E] com seringa luer-lock heparinizada. Coletados 2 pares de hemoculturas (aeróbia e anaeróbia) por punção venosa periférica e via CVC sob estrita técnica asséptica e antissepsia com clorexidina alcoólica. Amostras identificadas e encaminhadas imediatamente ao laboratório central."""
        ),

        // --- 9. SINAIS ALTERADOS ---
        NursingNoteTemplate(
            id = "901",
            title = "Instabilidade Hemodinâmica / Hipotensão Severa em UTI",
            category = NursingCategory.SINAIS_ALTERADOS,
            content = """Às [HH:mm], identificado episódio de hipotensão arterial e queda na PAM: PA [80x40] mmHg (PAM [53] mmHg), FC [118] bpm, SpO2 [93]%. Perfusão periférica lentificada (> 3 segundos). Comunicado imediatamente o médico plantonista Dr.(a) [NOME]. Realizado aumento na taxa de infusão de Noradrenalina em BI de [X] mL/h para [X] mL/h e administrada alíquota de SF 0,9% 500 mL rápida conforme ordem médica. PAM restabelecida em [68] mmHg às [HH:mm]."""
        ),
        NursingNoteTemplate(
            id = "902",
            title = "Registro de Hipertermia / Pico Febril em Paciente Crítico",
            category = NursingCategory.SINAIS_ALTERADOS,
            content = """Às [HH:mm], paciente apresentou pico febril de Temp [38,4] ºC, acompanhado de taquicardia (FC [112] bpm) e sudorese. Comunicado o médico plantonista Dr.(a) [NOME]. Administrada medicação antipirética [Dipirona / Paracetamol] via SNE / EV conforme prescrição. Instaladas medidas físicas de resfriamento (bolsas térmicas e compressas mornas em axilas). Coletadas novas hemoculturas e urocultura conforme protocolo de sepse."""
        ),

        // --- 10. INTERCORRÊNCIAS ---
        NursingNoteTemplate(
            id = "1001",
            title = "Parada Cardiorrespiratória (PCR) em UTI / Atendimento ACLS",
            category = NursingCategory.INTERCORRENCIAS,
            content = """Às [HH:mm], paciente apresentou assistolia / FV / TV sem pulso no monitor cardíaco com ausência de pulso central e pausa respiratória. Acionada equipe médica intensivista e iniciadas manobras de Reanimação Cardiopulmonar (RCP) imediatamente com compressões torácicas de alta qualidade. Instaladas pás do desfibrilador. Realizada intubação orotraqueal de emergência pelo Dr.(a) [NOME] com TOT nº [8,0] fixado a 22 cm. Administradas medicações de emergência em bolus por CVC (Adrenalina 1 mg EV a cada 3 minutos, total [X] ampolas). Retorno à circulação espontânea (RCE) obtido às [HH:mm]. Sinais vitais pós-PCR: PA [110x70] mmHg, FC [105] bpm. Mantida sedação e vasoativo em BI."""
        ),
        NursingNoteTemplate(
            id = "1002",
            title = "Extubação Acidental / Insuficiência Respiratória Aguda",
            category = NursingCategory.INTERCORRENCIAS,
            content = """Às [HH:mm], paciente apresentou agitação psicomotora e extubação orotraqueal acidental. Imediatamente ofertado suporte de oxigênio a 100% via máscara de VNI / bolsa-válvula-máscara (Ambu) e acionada equipe médica. Paciente mantido em decúbito elevado, realizada aspiração de cavidade oral. Auxiliada a equipe médica na reintubação orotraqueal com TOT nº [7,5] e confirmação por capnografia e ausculta pulmonar simétrica. Estabilizada oxigenação com SpO2 em 96%."""
        ),

        // --- 11. TROCA DE PLANTÃO ---
        NursingNoteTemplate(
            id = "1101",
            title = "Passagem e Troca de Plantão de Enfermagem (Beira do Leito)",
            category = NursingCategory.TROCA_PLANTAO,
            content = """Passagem de plantão realizada à beira do leito ao técnico/enfermeiro [NOME]. Paciente calmo, sedado sob RASS [-3], mantendo ventilação mecânica em modo PCV sem alarmes. Infusões contínuas ativas em BI: Noradrenalina a [X] mL/h e Fentanil a [X] mL/h. Dispositivos pérvios e checados: CVC jugular D, SVD e linha de PAI. Balanço hídrico parcial do turno zerado e folha digital atualizada sem pendências operacionais."""
        ),

        // --- 12. TRANSFERÊNCIA E ALTA ---
        NursingNoteTemplate(
            id = "1201",
            title = "Transferência Interna de Paciente da UTI para Enfermaria",
            category = NursingCategory.TRANSFERENCIA,
            content = """Paciente transferido da UTI para a Unidade de Internação / Enfermaria (Leito [X]) às [HH:mm], em maca com grades elevadas sob acompanhamento da equipe de enfermagem. Encontra-se consciente, orientado, eupnéico em ar ambiente / cateter nasal O2 a 2 L/min. Sinais vitais de saída registrados: PA [120x80] mmHg, FC [78] bpm, SpO2 [96]%. Entregue prontuário impresso, folha de balanço hídrico e medicamentos ao enfermeiro responsável pela unidade."""
        ),
        NursingNoteTemplate(
            id = "1202",
            title = "Alta Hospitalar / Saída da Unidade de Terapia Intensiva",
            category = NursingCategory.ALTA,
            content = """Paciente recebe alta médica da Unidade de Terapia Intensiva às [HH:mm]. Encontra-se em bom estado geral, eupnéico, estável hemodinamicamente. Retirados cateter venoso central e sonda vesical de demora conforme ordem médica. Mantido Acesso Venoso Periférico salinizado. Orientados paciente e acompanhante quanto aos cuidados e medicação prescrita."""
        )
    )
}
