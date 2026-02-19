package com.tungnk123.soundalarm.presentation.alarm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.DayOfWeek

@Composable
fun ScheduleCard(
    repeatDays: Set<DayOfWeek>,
    isVibrate: Boolean,
    deleteAfterFired: Boolean,
    onToggleDay: (DayOfWeek) -> Unit,
    onSetOneTime: () -> Unit,
    onToggleVibrate: () -> Unit,
    onToggleDeleteAfterFired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            SectionHeader(
                icon = {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                title = stringResource(R.string.label_repeat),
            )

            DaySelector(
                repeatDays = repeatDays,
                onToggleDay = onToggleDay,
            )

            val isOneTime = repeatDays.isEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isOneTime) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isOneTime) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isOneTime) stringResource(R.string.label_one_time_alarm)
                    else stringResource(
                        R.string.label_repeats_days,
                        repeatDays.joinToString(", ") { it.shortName },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOneTime) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (!isOneTime) {
                    TextButton(
                        onClick = onSetOneTime,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.button_clear),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.label_vibrate),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = isVibrate,
                    onCheckedChange = { onToggleVibrate() },
                )
            }

            AnimatedVisibility(
                visible = repeatDays.isEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (deleteAfterFired)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_delete_after_alarm),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = stringResource(R.string.label_delete_after_alarm_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = deleteAfterFired,
                            onCheckedChange = { onToggleDeleteAfterFired() },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
