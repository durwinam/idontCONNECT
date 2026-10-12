package ir.idontconnect.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.idontconnect.R
import ir.idontconnect.data.settings.ThemeMode
import ir.idontconnect.data.settings.UserSettings
import ir.idontconnect.ui.components.GlassCard

@Composable
fun SettingsScreen(
    settings: UserSettings,
    modifier: Modifier = Modifier,
    onTheme: (ThemeMode) -> Unit,
    onLanguage: (String) -> Unit,
    onAccent: (Int) -> Unit,
    onGlow: (Float) -> Unit,
    onAnimation: (Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    SettingsList(modifier) {
        item {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChoice(
                            label = stringResource(R.string.theme_system),
                            selected = settings.theme == ThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f),
                        ) { onTheme(ThemeMode.SYSTEM) }
                        ThemeChoice(
                            label = stringResource(R.string.theme_light),
                            selected = settings.theme == ThemeMode.LIGHT,
                            modifier = Modifier.weight(1f),
                        ) { onTheme(ThemeMode.LIGHT) }
                        ThemeChoice(
                            label = stringResource(R.string.theme_dark),
                            selected = settings.theme == ThemeMode.DARK,
                            modifier = Modifier.weight(1f),
                        ) { onTheme(ThemeMode.DARK) }
                    }
                }
            }
        }
        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val languages = listOf(
                        "system" to stringResource(R.string.language_system),
                        "fa" to "فارسی",
                        "en" to "English",
                        "ru" to "Русский",
                        "zh" to "中文",
                        "es" to "Español",
                        "de" to "Deutsch",
                    )
                    languages.forEach { (tag, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLanguage(tag) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = label, modifier = Modifier.weight(1f))
                            if (settings.languageTag == tag) {
                                Text(text = "✓", color = colors.primary)
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.accent_color),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val accentColors = listOf(
                        Color(0xFF53E6D0),
                        Color(0xFFB69CFF),
                        Color(0xFF6AA8FF),
                        Color(0xFFFF9DCA),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        accentColors.forEachIndexed { index, color ->
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onAccent(index) },
                                shape = CircleShape,
                                color = color,
                                border = if (settings.accentIndex == index) {
                                    BorderStroke(3.dp, colors.onSurface)
                                } else {
                                    null
                                },
                            ) {
                                Box(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                    SliderSetting(
                        label = stringResource(R.string.glow_intensity),
                        value = settings.glowIntensity,
                        onChange = onGlow,
                    )
                    SliderSetting(
                        label = stringResource(R.string.animation_intensity),
                        value = settings.animationIntensity,
                        onChange = onAnimation,
                    )
                }
            }
        }
        item {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.advanced_features),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    listOf(
                        R.string.smart_server,
                        R.string.network_health,
                        R.string.kill_switch,
                        R.string.split_tunneling,
                        R.string.safe_diagnostics,
                    ).forEach { resourceId ->
                        Text(
                            text = "• ${stringResource(resourceId)}",
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = stringResource(R.string.engine_integration_pending),
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsList(
    modifier: Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun ThemeChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (selected) colors.primary.copy(alpha = 0.14f) else colors.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 10.dp),
            color = if (selected) colors.primary else colors.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..1f,
        )
    }
}
