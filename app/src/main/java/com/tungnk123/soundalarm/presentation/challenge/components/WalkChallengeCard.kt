package com.tungnk123.soundalarm.presentation.challenge.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WalkChallengeCard(
    walkStepGoal: Int,
    onStepGoalSelected: (Int) -> Unit,
    onTestClick: () -> Unit,
) {
    ChallengeCard(
        icon = Icons.Default.DirectionsWalk,
        title = stringResource(R.string.challenge_walk_title),
        description = stringResource(R.string.challenge_walk_description),
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        titleColor = MaterialTheme.colorScheme.secondary,
        onTestClick = onTestClick,
    ) {
        Text(
            text = stringResource(R.string.challenge_label_step_goal),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 20, 50, 100).forEach { steps ->
                FilterChip(
                    selected = walkStepGoal == steps,
                    onClick = { onStepGoalSelected(steps) },
                    label = { Text(steps.toString()) },
                )
            }
        }
    }
}
