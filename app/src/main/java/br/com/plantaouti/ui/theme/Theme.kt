package br.com.plantaouti.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import br.com.plantaouti.data.datastore.AppThemeMode

private val ClinicalDarkColorScheme = darkColorScheme(
    primary = ClinicalPrimary,
    onPrimary = ClinicalOnPrimary,
    primaryContainer = ClinicalPrimaryContainer,
    onPrimaryContainer = ClinicalOnPrimaryContainer,
    secondary = ClinicalSecondary,
    onSecondary = ClinicalOnSecondary,
    secondaryContainer = ClinicalSecondaryContainer,
    onSecondaryContainer = ClinicalOnSecondaryContainer,
    tertiary = ClinicalTertiary,
    onTertiary = ClinicalOnTertiary,
    tertiaryContainer = ClinicalTertiaryContainer,
    onTertiaryContainer = ClinicalOnTertiaryContainer,
    background = ClinicalBackground,
    onBackground = ClinicalOnBackground,
    surface = ClinicalSurface,
    onSurface = ClinicalOnSurface,
    surfaceVariant = ClinicalSurfaceContainerHigh,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant,
    error = ClinicalError,
    onError = ClinicalOnError,
    errorContainer = ClinicalErrorContainer,
    onErrorContainer = ClinicalOnErrorContainer
)

private val ClinicalLightColorScheme = lightColorScheme(
    primary = ClinicalPrimary,
    onPrimary = ClinicalOnPrimary,
    primaryContainer = ClinicalPrimaryContainer,
    onPrimaryContainer = ClinicalOnPrimaryContainer,
    secondary = HospitalBlueSecondary,
    onSecondary = HospitalBlueOnSecondary,
    secondaryContainer = HospitalBlueContainer,
    onSecondaryContainer = HospitalBlueOnSecondaryContainer,
    tertiary = ConfirmGreenTertiary,
    onTertiary = ConfirmGreenOnTertiary,
    tertiaryContainer = ConfirmGreenContainer,
    onTertiaryContainer = ConfirmGreenOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun PlantaoUtiTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    dynamicColor: Boolean = false, // Obrigatoriamente false conforme especificacao do DESIGN.md
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // dynamicColor e fixado em false para garantir conformidade ao Clinical Dark Minimalist
    val colorScheme = if (isDark) ClinicalDarkColorScheme else ClinicalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HospitalTypography,
        content = content
    )
}
