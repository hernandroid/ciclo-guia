package com.cicloguia.app.feature.map.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cicloguia.app.core.designsystem.theme.LocalSpacing
import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyclewayDetailSheet(
    cycleway: SelectedCyclewayUi,
    onDismiss: () -> Unit,
    onViewRouteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            SheetHandle()
        }
    ) {
        CyclewayDetailContent(
            cycleway = cycleway,
            onDismiss = onDismiss,
            onViewRouteClick = onViewRouteClick
        )
    }
}

@Composable
private fun SheetHandle() {
    Spacer(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .width(48.dp)
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
    )
}

@Composable
private fun CyclewayDetailContent(
    cycleway: SelectedCyclewayUi,
    onDismiss: () -> Unit,
    onViewRouteClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg)
            .padding(bottom = spacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsBike,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.md)
            ) {
                Text(
                    text = cycleway.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = cycleway.district,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar"
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.size(spacing.lg)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 1.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DetailRow(
                    icon = Icons.Outlined.Straighten,
                    title = "Longitud",
                    value = cycleway.lengthKm
                )

                DetailRow(
                    icon = Icons.Outlined.Route,
                    title = "Tipo de segregación",
                    value = cycleway.segregationType
                )

                DetailRow(
                    icon = Icons.Outlined.Construction,
                    title = "Estado",
                    value = cycleway.status
                )

                DetailRow(
                    icon = Icons.Outlined.LightMode,
                    title = "Iluminación",
                    value = cycleway.lighting
                )

                DetailRow(
                    icon = Icons.Outlined.Security,
                    title = "Vigilancia",
                    value = cycleway.surveillance
                )

                DetailRow(
                    icon = Icons.Outlined.SwapHoriz,
                    title = "Sentido",
                    value = cycleway.direction
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.size(spacing.lg)
        )

        Button(
            onClick = onViewRouteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Outlined.Navigation,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(spacing.sm))

            Text(text = "Iniciar ruta")
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}