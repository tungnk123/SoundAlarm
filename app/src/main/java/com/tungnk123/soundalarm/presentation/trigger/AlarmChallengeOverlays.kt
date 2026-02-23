package com.tungnk123.soundalarm.presentation.trigger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tungnk123.soundalarm.domain.model.MathDifficulty
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlin.math.sqrt

// ─── Sensor flow helper ──────────────────────────────────────────────────────

private fun SensorManager.sensorEventFlow(
    sensorType: Int,
    samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL,
): Flow<SensorEvent> = callbackFlow {
    val sensor = getDefaultSensor(sensorType) ?: run {
        close()
        return@callbackFlow
    }
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) { trySend(event) }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    registerListener(listener, sensor, samplingPeriodUs)
    awaitClose { unregisterListener(listener) }
}

// ─── Math ────────────────────────────────────────────────────────────────────

private data class MathProblem(val expression: String, val answer: Int)

private fun generateMathProblem(difficulty: MathDifficulty): MathProblem = when (difficulty) {
    MathDifficulty.EASY -> {
        val a = (1..9).random()
        val b = (1..9).random()
        if ((0..1).random() == 0) MathProblem("$a + $b", a + b)
        else if (a >= b) MathProblem("$a − $b", a - b)
        else MathProblem("$b − $a", b - a)
    }
    MathDifficulty.MEDIUM -> when ((0..2).random()) {
        0 -> { val a = (10..50).random(); val b = (1..20).random(); MathProblem("$a + $b", a + b) }
        1 -> { val a = (20..50).random(); val b = (1..a - 1).random(); MathProblem("$a − $b", a - b) }
        else -> { val a = (2..9).random(); val b = (2..9).random(); MathProblem("$a × $b", a * b) }
    }
    MathDifficulty.HARD -> when ((0..2).random()) {
        0 -> { val a = (50..99).random(); val b = (10..99).random(); MathProblem("$a + $b", a + b) }
        1 -> { val a = (50..99).random(); val b = (10..a - 1).random(); MathProblem("$a − $b", a - b) }
        else -> { val a = (10..20).random(); val b = (2..9).random(); MathProblem("$a × $b", a * b) }
    }
}

@Composable
fun MathChallengeSection(
    config: AlarmTriggerViewModel.ChallengeConfig,
    onSolved: () -> Unit,
) {
    val problems = remember {
        List(config.mathProblemCount.coerceAtLeast(1)) { generateMathProblem(config.mathDifficulty) }
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var isWrong by remember { mutableStateOf(false) }

    val problem = problems[currentIndex]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        if (config.mathProblemCount > 1) {
            Text(
                text = "Problem ${currentIndex + 1} / ${config.mathProblemCount}",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Text(
            text = "${problem.expression} = ?",
            fontSize = 34.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isWrong) Color(0xFFFF4757).copy(alpha = 0.22f)
                    else Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userInput.ifEmpty { "?" },
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isWrong -> Color(0xFFFF4757)
                    userInput.isEmpty() -> Color.White.copy(alpha = 0.3f)
                    else -> Color.White
                },
                textAlign = TextAlign.Center,
            )
        }

        if (isWrong) {
            Text(
                text = "Wrong answer — try again",
                color = Color(0xFFFF4757).copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
            )
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓")
        keys.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    Surface(
                        onClick = {
                            when (key) {
                                "⌫" -> { isWrong = false; userInput = userInput.dropLast(1) }
                                "✓" -> if (userInput.isNotEmpty()) {
                                    if (userInput.toIntOrNull() == problem.answer) {
                                        if (currentIndex < problems.size - 1) {
                                            currentIndex++; userInput = ""; isWrong = false
                                        } else {
                                            onSolved()
                                        }
                                    } else {
                                        isWrong = true; userInput = ""
                                    }
                                }
                                else -> if (userInput.length < 5) { isWrong = false; userInput += key }
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (key == "✓") Color(0xFF4CAF50).copy(alpha = 0.75f)
                        else Color.White.copy(alpha = 0.12f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ─── Shake ───────────────────────────────────────────────────────────────────

private const val SHAKE_THRESHOLD = 15f
private const val SHAKE_DEBOUNCE_MS = 500L

@Composable
fun ShakeChallengeSection(
    config: AlarmTriggerViewModel.ChallengeConfig,
    onSolved: () -> Unit,
) {
    val context = LocalContext.current
    var shakeCount by remember { mutableIntStateOf(0) }
    var solved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var lastShakeMs = 0L
        sensorManager
            .sensorEventFlow(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME)
            .conflate()
            .collect { event ->
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val now = System.currentTimeMillis()
                if (magnitude > SHAKE_THRESHOLD && now - lastShakeMs > SHAKE_DEBOUNCE_MS) {
                    lastShakeMs = now
                    if (!solved) {
                        shakeCount = (shakeCount + 1).coerceAtMost(config.shakeCount)
                        if (shakeCount >= config.shakeCount) {
                            solved = true
                            onSolved()
                        }
                    }
                }
            }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
            CircularProgressIndicator(
                progress = { shakeCount.toFloat() / config.shakeCount.toFloat() },
                modifier = Modifier.size(96.dp),
                color = Color(0xFF7C3AED),
                trackColor = Color.White.copy(alpha = 0.15f),
                strokeWidth = 6.dp,
            )
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(text = "$shakeCount / ${config.shakeCount}", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.White)
        Text(
            text = "Shake your phone to dismiss",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Walk ────────────────────────────────────────────────────────────────────

@Composable
fun WalkChallengeSection(
    config: AlarmTriggerViewModel.ChallengeConfig,
    onSolved: () -> Unit,
) {
    val context = LocalContext.current
    var stepsWalked by remember { mutableIntStateOf(0) }
    var solved by remember { mutableStateOf(false) }

    val sensorAvailable = remember {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> permissionGranted = granted }

    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) return@LaunchedEffect
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var baselineSteps = -1
        sensorManager
            .sensorEventFlow(Sensor.TYPE_STEP_COUNTER)
            .collect { event ->
                val total = event.values[0].toInt()
                if (baselineSteps == -1) baselineSteps = total
                val walked = (total - baselineSteps).coerceAtLeast(0)
                stepsWalked = walked.coerceAtMost(config.walkStepGoal)
                if (walked >= config.walkStepGoal && !solved) {
                    solved = true
                    onSolved()
                }
            }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
            CircularProgressIndicator(
                progress = { stepsWalked.toFloat() / config.walkStepGoal.toFloat() },
                modifier = Modifier.size(96.dp),
                color = Color(0xFF0D9488),
                trackColor = Color.White.copy(alpha = 0.15f),
                strokeWidth = 6.dp,
            )
            Icon(
                imageVector = Icons.Default.DirectionsWalk,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            text = "$stepsWalked / ${config.walkStepGoal}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
        )
        Text(
            text = when {
                !permissionGranted -> "Permission needed to count steps"
                sensorAvailable -> "Walk ${config.walkStepGoal} steps to dismiss"
                else -> "Step sensor unavailable on this device"
            },
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (!permissionGranted) {
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                },
                shape = RoundedCornerShape(50),
                color = Color(0xFF0D9488).copy(alpha = 0.4f),
            ) {
                Text(
                    text = "Grant Permission",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        } else if (!sensorAvailable) {
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = onSolved,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Text(
                    text = "Dismiss",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ─── Memory ──────────────────────────────────────────────────────────────────

private enum class MemoryPhase { SHOWING, TYPING }

private const val MEMORY_SHOW_SECONDS = 4

@Composable
fun MemoryChallengeSection(
    config: AlarmTriggerViewModel.ChallengeConfig,
    onSolved: () -> Unit,
) {
    val secretCode = remember {
        (0 until config.memoryCodeLength.coerceAtLeast(4)).map { (0..9).random() }.joinToString("")
    }
    var phase by remember { mutableStateOf(MemoryPhase.SHOWING) }
    var countdown by remember { mutableIntStateOf(MEMORY_SHOW_SECONDS) }
    var progress by remember { mutableFloatStateOf(1f) }
    var userInput by remember { mutableStateOf("") }
    var isWrong by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val totalMs = MEMORY_SHOW_SECONDS * 1000L
        val tickMs = 50L
        var elapsed = 0L
        while (elapsed < totalMs) {
            delay(tickMs)
            elapsed += tickMs
            progress = 1f - (elapsed.toFloat() / totalMs)
            countdown = ((totalMs - elapsed) / 1000L + 1).toInt().coerceAtLeast(1)
        }
        phase = MemoryPhase.TYPING
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = Color(0xFFE879F9),
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = if (phase == MemoryPhase.SHOWING) "Memorize this code" else "Type the code from memory",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "memory_phase",
        ) { currentPhase ->
            if (currentPhase == MemoryPhase.SHOWING) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = secretCode,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFFE879F9),
                        letterSpacing = 8.sp,
                        textAlign = TextAlign.Center,
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE879F9),
                        trackColor = Color.White.copy(alpha = 0.15f),
                    )
                    Text(
                        text = "Hiding in $countdown…",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isWrong) Color(0xFFFF4757).copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = userInput.ifEmpty { "?" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isWrong -> Color(0xFFFF4757)
                                userInput.isEmpty() -> Color.White.copy(alpha = 0.3f)
                                else -> Color.White
                            },
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (isWrong) {
                        Text(
                            text = "Wrong code — try again",
                            color = Color(0xFFFF4757).copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓")
                    keys.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { key ->
                                Surface(
                                    onClick = {
                                        when (key) {
                                            "⌫" -> { isWrong = false; userInput = userInput.dropLast(1) }
                                            "✓" -> if (userInput.isNotEmpty()) {
                                                if (userInput == secretCode) {
                                                    onSolved()
                                                } else {
                                                    isWrong = true; userInput = ""
                                                }
                                            }
                                            else -> if (userInput.length < config.memoryCodeLength) {
                                                isWrong = false; userInput += key
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (key == "✓") Color(0xFFE879F9).copy(alpha = 0.6f)
                                    else Color.White.copy(alpha = 0.12f),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
