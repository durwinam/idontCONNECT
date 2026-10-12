package ir.idontconnect.feature.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.idontconnect.R
import ir.idontconnect.core.model.ServerProfile

@Composable
fun ProfilesScreen(
    profiles: List<ServerProfile>,
    activeProfileId: String?,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onSelect: (ServerProfile) -> Unit,
    onRemove: (ServerProfile) -> Unit,
    onFavorite: (ServerProfile, Boolean) -> Unit,
    onScanQr: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nav_profiles),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.profile_count, profiles.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onScanQr) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = stringResource(R.string.scan_qr),
                    )
                }
                Button(onClick = onAdd) {
                    Text(stringResource(R.string.add_profile))
                }
            }
        }

        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.no_profiles),
                    color = colors.onSurfaceVariant,
                )
                OutlinedButton(onClick = onAdd) {
                    Text(stringResource(R.string.import_profile_title))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = profiles, key = ServerProfile::id) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(profile) },
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(profile.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = profile.protocol.name +
                                        if (profile.isSubscription) " · Subscription" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                )
                                if (profile.id == activeProfileId) {
                                    Text(
                                        text = stringResource(R.string.selected_profile),
                                        color = colors.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                Text(
                                    text = profile.folder,
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            IconButton(onClick = { onFavorite(profile, !profile.isFavorite) }) {
                                Icon(
                                    imageVector = if (profile.isFavorite) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = stringResource(R.string.favorite),
                                )
                            }
                            IconButton(onClick = { onRemove(profile) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.remove_profile),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
