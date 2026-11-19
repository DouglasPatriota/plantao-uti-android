# Plantão UTI — Registro Digital de Enfermagem em UTI (Android)

Aplicativo Android nativo desenvolvido em **Kotlin**, **Jetpack Compose** e **Material Design 3** para **Técnicos de Enfermagem de UTI** registrarem o plantão à beira do leito com uma mão, substituindo a folha de papel tradicional.

> **Aviso Clínico Importante:** Este aplicativo é uma ferramenta auxiliar de registro. Ele **não calcula, não prescreve e não sugere doses** de medicamentos — apenas registra os valores informados pelo profissional e exibe alertas visuais de conferência.

## Funcionalidades Implementadas

- **Pacientes / Leitos**: Cadastro, edição e encerramento de plantão (Diurno `07h–19h` e Noturno `19h–07h`), com pré-preenchimento automático das 13 linhas horárias.
- **Sinais Vitais (Planilha + Tendência)**: PA (`120x80`), PAM calculada automaticamente `(PAS + 2×PAD)/3`, FC, FR, SpO₂, Temperatura, HGT, Dor (0–10) e Observações; destaque visual discreto para valores fora de faixas configuráveis (sem bloquear registro) e gráfico de tendência em Compose `Canvas`.
- **Drogas Vasoativas / Infusões Contínuas**:
  - 17 drogas pré-cadastradas (Amiodarona, Cetamina, Cisatracúrio, Dexmedetomidina, Dobutamina, Epinefrina, Fentanil, Lidocaína, Midazolam, Morfina, Nitroglicerina, Nitroprussiato, Norepinefrina, Propofol, Remifentanil, Tirofiban e Vasopressina) + cadastro manual.
  - Catálogo 100% editável nas Configurações com botão **Restaurar padrão**.
  - Cálculo de término previsto (`início + duração h:min`) tratando a virada de meia-noite.
  - Contagem regressiva ao vivo e alertas locais (`AlarmManager` exato + fallback `WorkManager` + `BootCompletedReceiver` pós-reinício) 30 min e 10 min antes do fim (configurável).
  - Histórico imutável (append-only) de toda alteração de dose ou taxa com data/hora.
  - Alerta com confirmação quando a dose informada está fora da faixa de referência (nunca bloqueia).
- **Gastos com Materiais**: Lista simples (Material, Quantidade, Observação), **sem valores em R$**, autocomplete de materiais frequentes de UTI e botão rápido **+1**.
- **Balanço Hídrico / Eliminações**: Tabela horária com caixas de seleção para via de diurese (`SVD`, `Fralda`, `Vaso`, `Papagaio`), evacuações (quantidade + chips rápidos de aspecto), **sem volume em mL**, e linha de **TOTAL** por via.
- **Passagem de Plantão e Exportação PDF A4**: Resumo consolidado em uma tela, geração nativa (`PdfDocument`) em **PDF A4 vertical de página única** no layout da folha de papel (preenchido ou **em branco** para preencher à mão), compartilhamento (`FileProvider`) e impressão (`PrintManager`).
- **Layout Adaptativo**: Em smartphones utiliza barra inferior (`Sinais · Drogas · Materiais · Balanço · Resumo`); em tablets (`WindowWidthSizeClass.Expanded`) exibe **Sinais Vitais** lado a lado com **Drogas Vasoativas**.
- **Privacidade e Segurança (LGPD)**:
  - 100% offline-first, sem anúncios e sem rastreadores.
  - Banco Room criptografado com **SQLCipher** e chave AES-256 armazenada no **Android Keystore** (`EncryptedSharedPreferences`).
  - `FLAG_SECURE` configurável contra captura de tela.
  - Bloqueio por Biometria / PIN com timeout por inatividade e opção de apagar todos os dados com confirmação.

## Arquitetura e Stack

- **Arquitetura**: MVVM / Clean Architecture leve com `StateFlow` e persistência imediata (sem perda de dados em rotação ou morte de processo).
- **UI**: Jetpack Compose + Material 3 (Tema Claro e Tema Escuro confortável para plantão noturno).
- **Persistência**: Room + SQLCipher (`net.zetetic:sqlcipher-android`) + DataStore Preferences.
- **DI**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltWorker`).
- **Agendamento**: `AlarmManager` (`setExactAndAllowWhileIdle` com verificação Android 12+) + `WorkManager` + `BOOT_COMPLETED`.
- **Requisitos**: `minSdk = 26`, `targetSdk = 35`, Java 17, Kotlin 2.0.21.

## Como Compilar e Rodar no Android Studio

1. Abra a pasta `PlantaoUTI` no **Android Studio Ladybug (2024.2)** ou superior.
2. Aguarde a sincronização do Gradle (`Gradle Sync`) baixar as dependências do Maven Central / Google Maven.
3. Conecte um dispositivo Android (API 26+) ou inicie um emulador e clique em **Run (`app`)**.
4. Para rodar os testes unitários:
   ```bash
   ./gradlew testDebugUnitTest
   ```
   Ou clique com o botão direito em `app/src/test/java` → **Run Tests**.

## Como Publicar no Seu GitHub

Este projeto já vem inicializado com `git` e `.gitignore`. Para enviar ao seu repositório GitHub:

```bash
cd PlantaoUTI
git remote add origin https://github.com/SEU_USUARIO/plantao-uti-android.git
git branch -M main
git push -u origin main
```

## Suposições Adotadas

1. **Tirofiban e Amiodarona na validação numérica**: Como possuem textos compostos na faixa de referência (`0,15 por 12 h` e `1 mg/min por 6h, depois 0,5 mg/min`), adotou-se `[0.15, 0.15]` para Tirofiban e `[0.5, 1.0]` para Amiodarona como limites numéricos de alerta, mantendo o texto completo visível ao lado do campo.
2. **Android 12+ Alarmes Exatos**: Caso o usuário ainda não tenha concedido `SCHEDULE_EXACT_ALARM`, o app faz fallback automático para `WorkManager` e exibe atalho nas Configurações para liberar alarmes exatos.
3. **Timeout de Inatividade**: Configurado por padrão em 5 minutos quando o bloqueio do app está ativado, com verificação ao retornar ao primeiro plano (`onResume`).
