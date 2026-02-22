package com.tungnk123.soundalarm.presentation.challenge.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.MathDifficulty

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MathChallengeCard(
    settings: AppSettings,
    onDifficultySelected: (MathDifficulty) -> Unit,
    onProblemCountSelected: (Int) -> Unit,
    onTestClick: () -> Unit,
) {
    ChallengeCard(
        icon = Icons.Default.Calculate,
        title = stringResource(R.string.challenge_math_title),
        description = stringResource(R.string.challenge_math_description),
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        titleColor = MaterialTheme.colorScheme.secondary,
        onTestClick = onTestClick,
    ) {
        Text(
            text = stringResource(R.string.challenge_label_difficulty),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MathDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = settings.mathDifficulty == difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    label = {
                        Text(
                            difficulty.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.challenge_label_problem_count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 5).forEach { count ->
                FilterChip(
                    selected = settings.mathProblemCount == count,
                    onClick = { onProblemCountSelected(count) },
                    label = { Text(count.toString()) },
                )
            }
        }
    }
}
