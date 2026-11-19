# 🏥 Plantão UTI — Registro Digital de Enfermagem em UTI (Android)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Room + SQLCipher](https://img.shields.io/badge/Room%20%2B%20SQLCipher-Offline--First-006A6A?style=for-the-badge)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Hilt-DI-1F5D7A?style=for-the-badge)](https://dagger.dev/hilt/)
[![LGPD](https://img.shields.io/badge/Privacidade-LGPD%20Local-2E7D32?style=for-the-badge)](#privacidade-e-segurança-lgpd)

Aplicativo Android nativo desenvolvido em **Kotlin**, **Jetpack Compose** e **Material Design 3** para **Técnicos de Enfermagem de UTI** registrarem o plantão à beira do leito com uma mão, substituindo a folha de papel tradicional com segurança, agilidade e funcionamento 100% offline.

> **⚠️ Aviso Clínico Importante:** Este aplicativo é uma ferramenta auxiliar de registro de enfermagem. Ele **não calcula, não prescreve e não sugere doses** de medicamentos — apenas registra os valores informados pelo profissional de saúde e exibe alertas visuais de conferência.

---

## ✨ Funcionalidades Principais

- **Pacientes / Leitos**: Cadastro, edição e encerramento de plantão nos turnos **Diurno (`07h–19h`)** e **Noturno (`19h–07h`)**, com pré-preenchimento automático das 13 linhas horárias.
- **Sinais Vitais (Planilha + Tendência)**:
  - Campos: PA (`120x80`), **PAM calculada automaticamente** `(PAS + 2×PAD) / 3`, FC, FR, SpO₂, Temperatura, HGT, Dor (`0–10`) e Observações.
  - Destaque visual discreto para valores fora de faixas configuráveis (sem bloquear o registro).
  - Gráfico de tendência em Compose `Canvas` (PA/PAM, FC, SpO₂ e Temperatura).
- **Drogas Vasoativas / Infusões Contínuas**:
  - **17 drogas pré-cadastradas** (Amiodarona, Cetamina, Cisatracúrio, Dexmedetomidina, Dobutamina, Epinefrina, Fentanil, Lidocaína, Midazolam, Morfina, Nitroglicerina, Nitroprussiato, Norepinefrina, Propofol, Remifentanil, Tirofiban e Vasopressina) + cadastro manual.
  - Tabela de drogas, unidades e faixas 100% editável nas **Configurações** com botão **Restaurar padrão**.
  - Cálculo de **Término previsto** (`hora de início + tempo para acabar`) com tratamento de virada de meia-noite.
  - Contagem regressiva ao vivo e **alarmes locais** (`AlarmManager` exato + fallback `WorkManager` + reagendamento após reinício via `BootCompletedReceiver`), padrão 30 min e 10 min antes do fim (configurável).
  - **Histórico imutável** (*append-only*) de alterações de dose e taxa com data/hora.
  - Aviso claro com pedido de confirmação caso a dose esteja fora da faixa de referência (sem bloqueio).
- **Gastos com Materiais**: Lista simples (Material, Quantidade, Observação), **sem valores em R$**, autocomplete com materiais frequentes de UTI e botão rápido **+1**.
- **Balanço Hídrico / Eliminações**: Tabela horária com caixas de seleção para via de diurese (`SVD`, `Fralda`, `Vaso`, `Papagaio`), evacuações (quantidade + chips rápidos de aspecto), **sem volume em mL**, e linha de **TOTAL** por via no fim do plantão.
- **Passagem de Plantão e Exportação PDF A4**:
  - Resumo consolidado do plantão em tela única.
  - Exportação nativa (`PdfDocument`) em **PDF A4 vertical de página única** no layout da folha de papel (preenchido ou **em branco** para preencher à mão), com compartilhamento (`FileProvider`) e impressão (`PrintManager`).
- **UX Hospitalar & Layout Adaptativo**:
  - Tema Claro e Tema Escuro confortável para plantão noturno, células `>= 48dp`, teclado numérico automático e feedback tátil ao salvar.
  - Em smartphones usa barra inferior (`Sinais · Drogas · Materiais · Balanço · Resumo`); em tablets (`WindowWidthSizeClass.Expanded`) exibe **Sinais Vitais** lado a lado com **Drogas Vasoativas**.

---

## 🔒 Privacidade e Segurança (LGPD)

- **100% offline-first**, sem anúncios, sem rastreadores e sem envio de dados clínicos a servidores externos.
- Banco local Room criptografado com **SQLCipher** (`net.zetetic:sqlcipher-android`) e chave AES-256 armazenada no **Android Keystore** (`EncryptedSharedPreferences`).
- Bloqueio contra captura de tela (`FLAG_SECURE`) configurável nas telas com dados de pacientes.
- Bloqueio por **Biometria ou PIN** com timeout automático por inatividade e opção de apagar todos os dados com confirmação.

---

## 🏗️ Arquitetura e Tecnologias

| Camada | Tecnologias |
| :--- | :--- |
| **Linguagem & UI** | Kotlin 2.0.21, Jetpack Compose, Material Design 3, WindowSizeClass |
| **Arquitetura** | MVVM / Clean Architecture leve, `StateFlow`, `Navigation Compose` |
| **Banco & Preferências** | Room 2.6.1 + SQLCipher 4.6.1, Jetpack DataStore Preferences |
| **Injeção de Dependência** | Dagger Hilt + Hilt Work |
| **Alarmes & Background** | `AlarmManager` (`setExactAndAllowWhileIdle`), `WorkManager`, `BOOT_COMPLETED` |
| **PDF & Impressão** | Android `PdfDocument` nativo, `FileProvider`, `PrintManager` |
| **Testes** | JUnit 4 (`VitalSignCalculatorTest`, `InfusionTimeCalculatorTest`, `DoseRangeValidatorTest`) |

---

## 🚀 Como Compilar e Rodar no Android Studio

1. Clone ou descompacte a pasta `PlantaoUTI` e abra no **Android Studio Ladybug (2024.2)** ou superior.
2. Aguarde o **Gradle Sync** concluir o download das dependências.
3. Conecte um aparelho Android (**API 26+**) ou emulador e clique em **Run (`app`)**.
4. Para executar os testes unitários:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📤 Publicando no GitHub

```bash
cd PlantaoUTI
git remote add origin https://github.com/DouglasPatriota/plantao-uti-android.git
git push -u origin main
```

---

## 📝 Suposições Adotadas

1. **Tirofiban e Amiodarona na validação numérica**: Como possuem faixas com texto composto (`0,15 por 12 h` e `1 mg/min por 6h, depois 0,5 mg/min`), adotou-se `[0.15, 0.15]` para Tirofiban e `[0.5, 1.0]` para Amiodarona como limites numéricos de alerta, mantendo o texto completo da faixa visível ao lado do campo.
2. **Permissão de Alarmes Exatos (Android 12+)**: Caso o usuário ainda não tenha concedido `SCHEDULE_EXACT_ALARM`, o app faz fallback automático para `WorkManager` e oferece atalho direto nas Configurações.
3. **Timeout de Inatividade**: Configurado em 5 minutos por padrão quando o bloqueio por Biometria/PIN está ativo, verificado ao retornar ao primeiro plano (`onResume`).

---

## 👨‍💻 Autor

**Douglas Patriota**  
[![GitHub](https://img.shields.io/badge/GitHub-DouglasPatriota-181717?style=flat-square&logo=github)](https://github.com/DouglasPatriota)
[![Email](https://img.shields.io/badge/Email-douglaslopes0205%40gmail.com-D14836?style=flat-square&logo=gmail&logoColor=white)](mailto:douglaslopes0205@gmail.com)
