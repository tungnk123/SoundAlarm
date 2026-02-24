package com.tungnk123.soundalarm.presentation.challenge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment    
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.presentation.trigger.AlarmTriggerViewModel
import com.tungnk123.soundalarm.presentation.trigger.MathChallengeSection
import com.tungnk123.soundalarm.presentation.trigger.MemoryChallengeSection
import com.tungnk123.soundalarm.presentation.trigger.ShakeChallengeSection
import com.tungnk123.soundalarm.presentation.trigger.WalkChallengeSection

@Composable
fun ChallengeTestDialog(
    method: DismissMethod,
    config: AlarmTriggerViewModel.ChallengeConfig,
    onDismiss: () -> Unit,
) {
    var solved by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1035), Color(0xFF0D1B4B)),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Text(
                        text = stringResource(
                            when (method) {
                                DismissMethod.MATH -> R.string.challenge_test_math_title
                                DismissMethod.SHAKE -> R.string.challenge_test_shake_title
                                DismissMethod.WALK -> R.string.challenge_test_walk_title
                                DismissMethod.MEMORY -> R.string.challenge_test_memory_title
                                DismissMethod.NONE -> R.string.app_name
                            },
                        ),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (solved) {
                        ChallengeSuccessContent(onDone = onDismiss)
                    } else {
                        when (method) {
                            DismissMethod.MATH -> MathChallengeSection(
                                config = config,
                                onSolved = { solved = true },
                            )
                            DismissMethod.SHAKE -> ShakeChallengeSection(
                                config = config,
                                onSolved = { solved = true },
                            )
                            DismissMethod.WALK -> WalkChallengeSection(
                                config = config,
                                onSolved = { solved = true },
                            )
                            DismissMethod.MEMORY -> MemoryChallengeSection(
                                config = config,
                                onSolved = { solved = true },
                            )
                            DismissMethod.NONE -> {}
                        }
                    }
                }
            }
        }
    }
}
