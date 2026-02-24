package com.tungnk123.soundalarm.presentation.trigger

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.DismissMethod
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val alarmQuotes = listOf(
    "Rise and shine — today is full of possibilities.",
    "Every morning is a fresh start. Make it count.",
    "You have exactly one chance to live today. Use it.",
    "The secret of getting ahead is getting started.",
    "Don't watch the clock; do what it does. Keep going.",
    "Wake up with determination. Go to bed with satisfaction.",
    "Your only limit is your mind.",
    "Small steps every day lead to big results.",
    "Today's actions are tomorrow's results.",
    "Be so good they can't ignore you.",
    "The early bird gets the worm — and the quiet morning.",
    "You are stronger than you think.",
    "One day or day one — you decide.",
    "Good things come to those who hustle.",
    "Progress, not perfection.",
)

@Composable
fun AlarmTriggerScreen(
    alarmLabel: String,
    nextAlarmText: String?,
    challengeConfig: AlarmTriggerViewModel.ChallengeConfig,
    onStop: () -> Unit,
    onSnooze: () -> Unit,
) {
    val quote = remember { alarmQuotes.random() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM") }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "alarm")

    val bellScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bell_scale",
    )

    val bellRotation by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bell_rotation",
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    val arrowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "arrow_pulse",
    )

    // Swipe state — only relevant when dismissMethod == NONE
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = remember(density) { with(density) { 150.dp.toPx() } }
    val clampedOffset = dragOffsetY.coerceIn(-swipeThreshold, 0f)
    val swipeProgress = (-clampedOffset / swipeThreshold).coerceIn(0f, 1f)
    val stopColor = lerp(Color.White.copy(alpha = 0.7f), Color(0xFFFF4757), swipeProgress)

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060411)),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(90.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        ) {
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .align(Alignment.TopStart)
                    .offset((-90).dp, (-90).dp)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.85f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .align(Alignment.TopEnd)
                    .offset(60.dp, 90.dp)
                    .background(Color(0xFF1D4ED8).copy(alpha = 0.7f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
                    .offset(0.dp, (-70).dp)
                    .background(Color(0xFFBE185D).copy(alpha = 0.45f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .align(Alignment.BottomEnd)
                    .offset(75.dp, 75.dp)
                    .background(Color(0xFF0D9488).copy(alpha = 0.6f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.BottomStart)
                    .offset((-65).dp, 65.dp)
                    .background(Color(0xFF4338CA).copy(alpha = 0.65f), CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Black.copy(alpha = 0.44f),
                            Color.Black.copy(alpha = 0.64f),
                        ),
                    ),
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            Text(
                text = now.format(dateFormatter).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.38f),
                            Color.White.copy(alpha = 0.04f),
                        ),
                    ),
                ),
            ) {
                Text(
                    text = now.format(timeFormatter),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-4).sp,
                    modifier = Modifier.padding(vertical = 22.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (alarmLabel.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.11f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alarmLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }

            if (nextAlarmText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Next alarm in $nextAlarmText",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 0.3.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "\u201C$quote\u201D",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            Color(0xFF7C3AED).copy(alpha = glowPulse * 0.45f),
                            CircleShape,
                        )
                        .blur(30.dp),
                )
                Surface(
                    modifier = Modifier.size(104.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.09f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.22f)),
                ) {}
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = "Alarm ringing",
                    tint = Color.White,
                    modifier = Modifier
                        .size(54.dp)
                        .scale(bellScale)
                        .rotate(bellRotation),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                onClick = onSnooze,
                modifier = Modifier.padding(bottom = 20.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.24f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Snooze,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.trigger_snooze_button),
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            // Dismiss section — swipe for NONE, challenge overlay for other methods
            when (challengeConfig.dismissMethod) {
                DismissMethod.NONE -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(bottom = 52.dp)
                            .offset { IntOffset(0, clampedOffset.roundToInt()) }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (-dragOffsetY >= swipeThreshold) onStop()
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = { dragOffsetY = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtMost(0f)
                                    },
                                )
                            },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy((-10).dp),
                        ) {
                            repeat(3) { i ->
                                val baseAlpha = 1f - i * 0.28f
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = stopColor.copy(
                                        alpha = if (swipeProgress > 0.05f) baseAlpha
                                        else (arrowPulse * baseAlpha).coerceIn(0f, 1f),
                                    ),
                                    modifier = Modifier.size(34.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (swipeProgress >= 0.8f) stringResource(R.string.trigger_release_to_stop)
                            else stringResource(R.string.trigger_swipe_to_stop),
                            color = stopColor,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                DismissMethod.MATH -> MathChallengeSection(config = challengeConfig, onSolved = onStop)
                DismissMethod.SHAKE -> ShakeChallengeSection(config = challengeConfig, onSolved = onStop)
                DismissMethod.WALK -> WalkChallengeSection(config = challengeConfig, onSolved = onStop)
                DismissMethod.MEMORY -> MemoryChallengeSection(config = challengeConfig, onSolved = onStop)
            }
        }
    }
}
