package br.com.plantaouti.ui.security

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import br.com.plantaouti.security.BiometricAuthHelper
import br.com.plantaouti.security.PinHasher
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FullWidthActionButton

@Composable
fun LockScreen(
    activity: FragmentActivity,
    pinHash: String,
    onUnlocked: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val canBiometric = remember { BiometricAuthHelper.canAuthenticate(activity) }

    LaunchedEffect(Unit) {
        if (canBiometric) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { /* usuário pode usar PIN */ }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Plantão UTI Bloqueado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Proteção LGPD para dados clínicos dos pacientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (pinHash.isNotBlank()) {
                        FormOutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it.filter(Char::isDigit).take(6)
                                errorMsg = null
                            },
                            label = "Digite o PIN de Segurança",
                            leadingIcon = Icons.Rounded.Lock,
                            keyboardType = KeyboardType.NumberPassword,
                            isError = errorMsg != null,
                            errorMessage = errorMsg
                        )

                        FullWidthActionButton(
                            text = "Desbloquear com PIN",
                            onClick = {
                                if (PinHasher.verify(pinInput, pinHash)) {
                                    onUnlocked()
                                } else {
                                    errorMsg = "PIN incorreto"
                                    pinInput = ""
                                }
                            }
                        )
                    }

                    if (canBiometric) {
                        OutlinedButton(
                            onClick = {
                                BiometricAuthHelper.authenticate(
                                    activity = activity,
                                    onSuccess = onUnlocked,
                                    onError = { errorMsg = it }
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Usar Biometria / Senha do Aparelho", fontWeight = FontWeight.Bold)
                        }
                    } else if (pinHash.isBlank()) {
                        FullWidthActionButton(
                            text = "Acessar Plantão",
                            onClick = onUnlocked
                        )
                    }
                }
            }
        }
    }
}
