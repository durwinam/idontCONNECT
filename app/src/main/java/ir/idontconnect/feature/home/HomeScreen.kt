package ir.idontconnect.feature.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.idontconnect.R
import ir.idontconnect.app.AppUiState
import ir.idontconnect.core.model.EngineStatus
import ir.idontconnect.ui.components.GlassCard
import ir.idontconnect.ui.components.TrafficChart
import java.util.Locale

@Composable
fun HomeScreen(
    state: AppUiState,
    modifier: Modifier = Modifier,
    onConnect: () -> Unit,
    onOpenProfiles: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "connect-halo")
    val haloAlpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.12f + 0.42f * state.settings.glowIntensity.coerceIn(0f, 1f),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (
                    2_600 - 1_400 * state.settings.animationIntensity.coerceIn(0f, 1f)
                ).toInt().coerceAtLeast(700),
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo-alpha",
    )
    val connected = state.engineStatus is EngineStatus.Connected

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "idontCONNECT",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = haloAlpha)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(colors.surface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                    Text(
                        text = if (connected) {
                            stringResource(R.string.status_connected)
                        } else {
                            stringResource(R.string.status_disconnected)
                        },
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                    )
                    Text(
                        text = if (state.engineStatus is EngineStatus.Unavailable) {
                            stringResource(R.string.engine_not_bundled)
                        } else {
                            stringResource(R.string.no_fake_traffic)
                        },
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = onConnect,
                        enabled = !connected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.connect),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = stringResource(R.string.download),
                    value = state.trafficSamples.lastOrNull()
                        ?.downloadBytesPerSecond?.toMegaBytesPerSecond() ?: "—",
                    unit = "MB/s",
                    isDownload = true,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = stringResource(R.string.upload),
                    value = state.trafficSamples.lastOrNull()
                        ?.uploadBytesPerSecond?.toMegaBytesPerSecond() ?: "—",
                    unit = "MB/s",
                    isDownload = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.live_traffic),
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.update_interval),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = colors.primary,
                        )
                    }
                    TrafficChart(state.trafficSamples)
                    Text(
                        text = if (state.trafficSamples.isEmpty()) {
                            stringResource(R.string.chart_waiting_for_core)
                        } else {
                            stringResource(R.string.chart_real_data)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AverageCard(
                            title = stringResource(R.string.average_download),
                            value = state.trafficSamples
                                .map { it.downloadBytesPerSecond }
                                .takeIf { values -> values.isNotEmpty() }
                                ?.average()?.toLong()?.toMegaBytesPerSecond() ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                        AverageCard(
                            title = stringResource(R.string.average_upload),
                            value = state.trafficSamples
                                .map { it.uploadBytesPerSecond }
                                .takeIf { values -> values.isNotEmpty() }
                                ?.average()?.toLong()?.toMegaBytesPerSecond() ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onOpenProfiles,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Rounded.Bolt, contentDescription = null)
                Text(
                    text = stringResource(R.string.manage_profiles),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    isDownload: Boolean,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    GlassCard(modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = if (isDownload) {
                        Icons.Rounded.ArrowDownward
                    } else {
                        Icons.Rounded.ArrowUpward
                    },
                    contentDescription = null,
                    tint = if (isDownload) colors.primary else colors.secondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AverageCard(
    title: String,
    value: String,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .background(colors.primary.copy(alpha = 0.07f))
            .padding(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = "$value MB/s",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Long.toMegaBytesPerSecond(): String =
    String.format(Locale.ROOT, "%.2f", coerceAtLeast(0L) / 1_000_000.0)
