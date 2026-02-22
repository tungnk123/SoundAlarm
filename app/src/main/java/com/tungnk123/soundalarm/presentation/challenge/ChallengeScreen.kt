package com.tungnk123.soundalarm.presentation.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.presentation.challenge.components.ChallengeTestDialog
import com.tungnk123.soundalarm.presentation.challenge.components.MathChallengeCard
import com.tungnk123.soundalarm.presentation.challenge.components.ShakeChallengeCard
import com.tungnk123.soundalarm.presentation.challenge.components.WalkChallengeCard
import com.tungnk123.soundalarm.presentation.trigger.AlarmTriggerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(viewModel: ChallengeViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var testingChallenge by remember { mutableStateOf<DismissMethod?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_challenge)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.challenge_screen_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MathChallengeCard(
                settings = settings,
                onDifficultySelected = viewModel::updateMathDifficulty,
                onProblemCountSelected = viewModel::updateMathProblemCount,
                onTestClick = { testingChallenge = DismissMethod.MATH },
            )

            ShakeChallengeCard(
                shakeCount = settings.shakeCount,
                onShakeCountSelected = viewModel::updateShakeCount,
                onTestClick = { testingChallenge = DismissMethod.SHAKE },
            )

            WalkChallengeCard(
                walkStepGoal = settings.walkStepGoal,
                onStepGoalSelected = viewModel::updateWalkStepGoal,
                onTestClick = { testingChallenge = DismissMethod.WALK },
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    testingChallenge?.let { method ->
        val config = AlarmTriggerViewModel.ChallengeConfig(
            dismissMethod = method,
            mathDifficulty = settings.mathDifficulty,
            mathProblemCount = settings.mathProblemCount,
            shakeCount = settings.shakeCount,
            walkStepGoal = settings.walkStepGoal,
        )
        ChallengeTestDialog(
            method = method,
            config = config,
            onDismiss = { testingChallenge = null },
        )
    }
}
