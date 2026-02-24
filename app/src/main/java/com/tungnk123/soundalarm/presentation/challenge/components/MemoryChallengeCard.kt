package com.tungnk123.soundalarm.presentation.challenge.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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
fun MemoryChallengeCard(
    memoryCodeLength: Int,
    onCodeLengthSelected: (Int) -> Unit,
    onTestClick: () -> Unit,
) {
    ChallengeCard(
        icon = Icons.Default.Memory,
        title = stringResource(R.string.challenge_memory_title),
        description = stringResource(R.string.challenge_memory_description),
        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
        iconTint = MaterialTheme.colorScheme.onErrorContainer,
        titleColor = MaterialTheme.colorScheme.error,
        onTestClick = onTestClick,
    ) {
        Text(
            text = stringResource(R.string.challenge_label_code_length),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8).forEach { length ->
                FilterChip(
                    selected = memoryCodeLength == length,
                    onClick = { onCodeLengthSelected(length) },
                    label = { Text("$length digits") },
                )
            }
        }
    }
}
