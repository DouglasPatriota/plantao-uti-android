package br.com.plantaouti.ui.theme

import androidx.compose.ui.graphics.Color

// Modo Claro (Light Mode)
val LightBackground = Color(0xFFF8FAFC)        // Fundo gelo
val LightSurface = Color(0xFFFFFFFF)           // Blocos/cards brancos
val LightOutline = Color(0xFFE2E8F0)           // Borda sutil (1.dp)
val LightOnBackground = Color(0xFF0F172A)      // Texto grafite
val LightOnSurface = Color(0xFF0F172A)         // Texto grafite
val LightOnSurfaceVariant = Color(0xFF64748B)  // Texto secundário
val LightPrimary = Color(0xFF0284C7)           // Azul clínico para ações
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0F2FE)
val LightOnPrimaryContainer = Color(0xFF0369A1)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOutlineVariant = Color(0xFFCBD5E1)

// Modo Escuro (Dark Mode)
val DarkBackground = Color(0xFF0B1120)         // Fundo grafite profundo
val DarkSurface = Color(0xFF1E293B)            // Blocos em azul-ardósia
val DarkOutline = Color(0xFF334155)            // Borda sutil (1.dp)
val DarkOnBackground = Color(0xFFF8FAFC)       // Texto claro
val DarkOnSurface = Color(0xFFF8FAFC)          // Texto claro
val DarkOnSurfaceVariant = Color(0xFF94A3B8)   // Texto secundário
val DarkPrimary = Color(0xFF38BDF8)            // Azul-celeste
val DarkOnPrimary = Color(0xFF0B1120)
val DarkPrimaryContainer = Color(0xFF0369A1)
val DarkOnPrimaryContainer = Color(0xFFE0F2FE)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOutlineVariant = Color(0xFF475569)

// Cores de Alerta Clínico (Soft Badges & Borders)
val ClinicalNormalGreen = Color(0xFF10B981)
val ClinicalNormalBgLight = Color(0xFFD1FAE5)
val ClinicalNormalTextLight = Color(0xFF065F46)
val ClinicalNormalBgDark = Color(0xFF064E3B)
val ClinicalNormalTextDark = Color(0xFFA7F3D0)

val ClinicalWarningAmber = Color(0xFFF59E0B)
val AlertWarningBgLight = Color(0xFFFEF3C7)
val AlertWarningTextLight = Color(0xFF92400E)
val AlertWarningBgDark = Color(0xFF451A03)
val AlertWarningTextDark = Color(0xFFFDE68A)

val ClinicalCriticalRed = Color(0xFFEF4444)
val AlertCriticalBgLight = Color(0xFFFEE2E2)
val AlertCriticalTextLight = Color(0xFF991B1B)
val AlertCriticalBgDark = Color(0xFF450A0A)
val AlertCriticalTextDark = Color(0xFFFECACA)

// Legacy compatibility fallbacks
val ClinicalSurface = DarkSurface
val ClinicalBackground = DarkBackground
val ClinicalOnSurface = DarkOnSurface
val ClinicalOnSurfaceVariant = DarkOnSurfaceVariant
val ClinicalOutline = DarkOutline
val ClinicalPrimary = DarkPrimary
val ClinicalOnPrimary = DarkOnPrimary
val ClinicalPrimaryContainer = DarkPrimaryContainer
val ClinicalOnPrimaryContainer = DarkOnPrimaryContainer
val ClinicalError = ClinicalCriticalRed
val ClinicalOnError = Color(0xFFFFFFFF)
val ClinicalErrorContainer = AlertCriticalBgDark
val ClinicalOnErrorContainer = AlertCriticalTextDark
val ClinicalMonitorGreen = ClinicalNormalGreen
val ClinicalVigilanceAmber = ClinicalWarningAmber
val ClinicalAlertRed = ClinicalCriticalRed
