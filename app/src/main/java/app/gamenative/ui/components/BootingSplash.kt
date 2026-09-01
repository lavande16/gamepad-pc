package app.gamenative.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.R
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldPcShellHost
import app.gamenative.gamefold.ui.GameFoldPcFrontendPolicy
import app.gamenative.gamefold.ui.GameFoldNsLoadingContract
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.gamefold.ui.gameFoldNsPanelSurface
import app.gamenative.gamefold.ui.GameFoldNsLoadingProgress
import app.gamenative.product.GameFoldPcPresentationPolicy
import app.gamenative.ui.theme.BrandGradient
import app.gamenative.ui.theme.PluviaTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun BootingSplash(
    visible: Boolean = true,
    text: String = "Initializing...",
    progress: Float = -1f, // -1 for indeterminate, 0-1 for determinate
    heroImageUrl: String = "",
) {
    val context = LocalContext.current
    val presentation = GameFoldPcPresentationPolicy.forProduct(BuildConfig.GAMEFOLD_PC_LOCAL_SHELL)
    val frontend = GameFoldPcFrontendPolicy.forProduct(BuildConfig.GAMEFOLD_PC_LOCAL_SHELL)

    // The local PC product uses the NS shell as a permanent frame. A loading state is therefore
    // another display-aperture state, never a full-screen window that can cover the controller.
    if (frontend.useRetroLoadingCard) {
        if (visible) {
            GameFoldPcShellHost {
                GameFoldRetroLoadingCard(
                    text = text,
                    progress = progress,
                    heroImageUrl = heroImageUrl,
                )
            }
        }
        return
    }

    // Tips rotation (no animation cost, safe outside visibility check)
    val tips = remember(context, presentation.showBootSplashFixedTips) {
        if (presentation.showBootSplashFixedTips) listOf(
            context.getString(R.string.game_launch_tip_1),
            context.getString(R.string.game_launch_tip_2, context.getString(R.string.option_open_container)),
            context.getString(R.string.game_launch_tip_3),
            context.getString(R.string.game_launch_tip_4),
            context.getString(R.string.game_launch_tip_5, context.getString(R.string.option_test_graphics)),
            context.getString(R.string.game_launch_tip_6),
            context.getString(R.string.game_launch_tip_7),
            context.getString(R.string.game_launch_tip_8),
            context.getString(R.string.game_launch_tip_9, context.getString(R.string.option_open_container)),
            context.getString(R.string.game_launch_tip_10),
            context.getString(R.string.game_launch_tip_11),
            context.getString(R.string.game_launch_tip_12),
            context.getString(R.string.game_launch_tip_13),
            context.getString(R.string.game_launch_tip_14),
            context.getString(R.string.game_launch_tip_15),
            context.getString(R.string.game_launch_tip_16),
            context.getString(R.string.game_launch_tip_17),
            context.getString(R.string.game_launch_tip_18),
            context.getString(R.string.game_launch_tip_19),
            context.getString(R.string.game_launch_tip_20),
            context.getString(R.string.game_launch_tip_21),
            context.getString(R.string.game_launch_tip_22),
            context.getString(R.string.game_launch_tip_23),
            context.getString(R.string.game_launch_tip_24, context.getString(R.string.option_test_graphics)),
            context.getString(R.string.game_launch_tip_25),
        ) else emptyList()
    }

    var tipIndex by remember { mutableStateOf(if (tips.isNotEmpty()) Random.nextInt(tips.size) else 0) }

    LaunchedEffect(visible, tips) {
        while (visible && tips.isNotEmpty()) {
            delay(8000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        // Animations only run while visible (inside AnimatedVisibility scope)
        val infiniteTransition = rememberInfiniteTransition(label = "bootSplash")
        val scrimColor = MaterialTheme.colorScheme.scrim
        var heroImageFailed by remember(heroImageUrl) { mutableStateOf(false) }

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "glowPulse",
        )

        val logoScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "logoScale",
        )

        val shimmerPosition by infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmer",
        )

        val particlePhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "particlePhase",
        )

        val useHeroBackdrop = heroImageUrl.isNotEmpty() && !heroImageFailed

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                PluviaTheme.colors.surfacePanel,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )

            AmbientParticles(phase = particlePhase)

            if (useHeroBackdrop) {
                val desaturate = remember {
                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                }

                CoilImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.06f
                            scaleY = 1.06f
                        }
                        .alpha(0.38f)
                        .blur(7.dp),
                    imageModel = { heroImageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        colorFilter = desaturate,
                    ),
                    loading = {},
                    failure = {
                        heroImageFailed = true
                    },
                    previewPlaceholder = painterResource(R.drawable.ic_logo_color),
                )

                // Single soft legibility scrim: light at the top, building toward the
                // bottom where the status/tips text sits. No heavy top/bottom bands.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to scrimColor.copy(alpha = 0.48f),
                                    0.4f to scrimColor.copy(alpha = 0.48f),
                                    1.0f to scrimColor.copy(alpha = 0.62f),
                                ),
                            ),
                        ),
                )
            }

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                Spacer(modifier = Modifier.weight(0.4f))

                // Logo with glow effect
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.scale(logoScale),
                ) {
                    // Glow layer (blurred behind)
                    Text(
                        text = presentation.bootSplashBrand,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = 2.sp,
                        ),
                        color = PluviaTheme.colors.accentCyan.copy(alpha = glowAlpha * 0.6f),
                        modifier = Modifier
                            .blur(20.dp)
                            .padding(20.dp)
                            .alpha(glowAlpha),
                    )

                    // Main logo text
                    Text(
                        text = presentation.bootSplashBrand,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = 2.sp,
                            shadow = Shadow(
                                color = PluviaTheme.colors.accentCyan.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 20f,
                            ),
                            brush = Brush.horizontalGradient(
                                colors = BrandGradient,
                            ),
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                ProgressBar(
                    progress = progress,
                    shimmerPosition = shimmerPosition,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(4.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status text
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        shadow = Shadow(
                            color = scrimColor.copy(alpha = if (useHeroBackdrop) 0.9f else 0f),
                            offset = Offset(0f, 1f),
                            blurRadius = 6f,
                        ),
                    ),
                    color = if (useHeroBackdrop) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(15.dp))
                Spacer(modifier = Modifier.weight(0.3f))

                // Tips section
                if (tips.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Crossfade(
                            targetState = tipIndex,
                            animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
                            label = "tipCrossfade",
                        ) { idx ->
                            Text(
                                text = tips[idx],
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 20.sp,
                                    shadow = Shadow(
                                        color = scrimColor.copy(alpha = if (useHeroBackdrop) 0.9f else 0f),
                                        offset = Offset(0f, 1f),
                                        blurRadius = 6f,
                                    ),
                                ),
                                color = if (useHeroBackdrop) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameFoldRetroLoadingCard(
    text: String,
    progress: Float,
    heroImageUrl: String,
    title: String = "GamePad PC",
) {
    val ink = GameFoldNsTokens.Ink

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            // The NS shell already owns the display aperture/background. Keep the loading card
            // as a transparent overlay so it cannot introduce a second full-screen black layer.
            .background(Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
            Row(
                modifier = Modifier
                // fragment_emulation.xml is wrap_content: 112dp cover + 16dp gap + 220dp text
                // column + 32dp panel padding. Keep the exact NS measured card instead of a
                // display-percentage approximation.
                .width(GameFoldNsLoadingContract.CardWidthDp.dp)
                .gameFoldNsPanelSurface(dialog = true)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoilImage(
                modifier = Modifier
                    .size(GameFoldNsLoadingContract.CoverSizeDp.dp)
                    .clip(RectangleShape),
                imageModel = { heroImageUrl.ifBlank { R.drawable.pixel_splash_mark } },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                ),
                loading = {
                    Image(
                        painter = painterResource(R.drawable.pixel_splash_mark),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                failure = {
                    Image(
                        painter = painterResource(R.drawable.pixel_splash_mark),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                previewPlaceholder = painterResource(R.drawable.pixel_splash_mark),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    color = ink,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = text.ifBlank { "正在准备运行环境" },
                    color = ink,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GameFoldNsLoadingProgress(progress = progress)
            }
        }
    }
}

@Composable
private fun AmbientParticles(
    phase: Float,
    modifier: Modifier = Modifier,
) {
    val particleColor = PluviaTheme.colors.accentCyan

    val particles = remember {
        List(12) {
            ParticleData(
                baseX = Random.nextFloat(),
                baseY = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                speed = Random.nextFloat() * 0.5f + 0.5f,
                phaseOffset = Random.nextFloat() * 360f,
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val animatedPhase = (phase + particle.phaseOffset) * particle.speed
            val radians = Math.toRadians(animatedPhase.toDouble())

            val offsetX = (sin(radians) * 30).toFloat()
            val offsetY = (sin(radians * 0.7) * 20).toFloat()

            val x = particle.baseX * size.width + offsetX
            val y = particle.baseY * size.height + offsetY
            val alpha = (0.15f + 0.15f * sin(radians * 2).toFloat()).coerceIn(0f, 0.3f)

            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = particle.size.dp.toPx(),
                center = Offset(x, y),
            )
        }
    }
}

private data class ParticleData(
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val speed: Float,
    val phaseOffset: Float,
)

@Composable
private fun ProgressBar(
    progress: Float,
    shimmerPosition: Float,
    modifier: Modifier = Modifier,
) {
    val isIndeterminate = progress < 0f
    val actualProgress = if (isIndeterminate) 1f else progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(PluviaTheme.colors.borderDefault.copy(alpha = 0.3f)),
    ) {
        // Progress fill with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(actualProgress)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = BrandGradient,
                    ),
                ),
        )

        // Shimmer overlay
        if (isIndeterminate || progress > 0f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp)),
            ) {
                val shimmerWidth = size.width * 0.3f
                val shimmerStart = (shimmerPosition * size.width) - shimmerWidth
                val shimmerEnd = shimmerStart + shimmerWidth

                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                        startX = shimmerStart,
                        endX = shimmerEnd,
                    ),
                )
            }
        }
    }
}


@Preview(name = "BootingSplash - Indeterminate")
@Composable
fun BootingSplashPreview() {
    PluviaTheme {
        BootingSplash(visible = true)
    }
}

@Preview(name = "BootingSplash - 50% Progress")
@Composable
fun BootingSplashProgressPreview() {
    PluviaTheme {
        BootingSplash(
            visible = true,
            text = "Loading game files...",
            progress = 0.5f,
        )
    }
}

@Preview(name = "BootingSplash - Dark", device = "spec:width=1920px,height=1080px,dpi=440")
@Composable
fun BootingSplashLandscapePreview() {
    PluviaTheme {
        BootingSplash(
            visible = true,
            text = "Preparing container...",
            progress = -1f,
        )
    }
}
