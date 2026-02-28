package com.tungnk123.soundalarm.presentation.alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R

private val NOTIFY_PRESETS = listOf(0, 5, 15, 30, 60, 120)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotifyBeforeCard(
    notifyBeforeMinutes: Int,
    onNotifyBeforeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomNotifyDialog(
            initialMinutes = if (notifyBeforeMinutes !in NOTIFY_PRESETS) notifyBeforeMinutes else 0,
            onConfirm = { minutes ->
                onNotifyBeforeChange(minutes)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            SectionHeader(
                icon = {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                },
                title = stringResource(R.string.label_notify_before),
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                titleColor = MaterialTheme.colorScheme.tertiary,
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.label_notify_before_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (notifyBeforeMinutes > 0) {
                    Text(
                        text = stringResource(R.string.label_notify_before_active, notifyBeforeMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                val isCustomSelected = notifyBeforeMinutes !in NOTIFY_PRESETS
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NOTIFY_PRESETS.forEach { minutes ->
                        FilterChip(
                            selected = notifyBeforeMinutes == minutes,
                            onClick = { onNotifyBeforeChange(minutes) },
                            label = {
                                Text(
                                    text = notifyBeforeLabel(minutes),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                    FilterChip(
                        selected = isCustomSelected,
                        onClick = { showCustomDialog = true },
                        label = {
                            Text(
                                text = if (isCustomSelected)
                                    stringResource(R.string.label_notify_before_minutes, notifyBeforeMinutes)
                                else
                                    stringResource(R.string.label_notify_before_custom),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CustomNotifyDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(if (initialMinutes > 0) initialMinutes.toString() else "") }
    val parsed = text.toIntOrNull()
    val isValid = parsed != null && parsed > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_notify_before_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.dialog_notify_before_label)) },
                suffix = { Text(stringResource(R.string.dialog_notify_before_suffix)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = text.isNotEmpty() && !isValid,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onConfirm(parsed!!) },
                enabled = isValid,
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
    )
}

@Composable
private fun notifyBeforeLabel(minutes: Int): String = when (minutes) {
    0 -> stringResource(R.string.label_notify_before_off)
    60 -> stringResource(R.string.label_notify_before_1h)
    120 -> stringResource(R.string.label_notify_before_2h)
    else -> stringResource(R.string.label_notify_before_minutes, minutes)
}
