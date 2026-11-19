package br.com.plantaouti.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import br.com.plantaouti.security.BiometricAuthHelper
import br.com.plantaouti.security.PinHasher

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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Plantão UTI Bloqueado", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Proteção LGPD para dados clínicos dos pacientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            if (pinHash.isNotBlank()) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        pinInput = it.filter(Char::isDigit).take(6)
                        errorMsg = null
                    },
                    label = { Text("Digite o PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (PinHasher.verify(pinInput, pinHash)) {
                            onUnlocked()
                        } else {
                            errorMsg = "PIN incorreto"
                            pinInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Desbloquear com PIN")
                }
                Spacer(Modifier.height(12.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Usar Biometria / Senha do Aparelho")
                }
            } else if (pinHash.isBlank()) {
                Button(onClick = onUnlocked, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Entrar")
                }
            }
        }
    }
}
