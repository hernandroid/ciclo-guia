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
import androidx.compose.ui.text.font.FontWeight
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
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
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
        Header(
            cycleway = cycleway,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        SummaryIndicators(
            cycleway = cycleway
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        DetailCard(
            cycleway = cycleway
        )

        Spacer(modifier = Modifier.height(spacing.lg))

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

            Text(
                text = "Iniciar ruta",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun Header(
    cycleway: SelectedCyclewayUi,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsBike,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md)
        ) {
            Text(
                text = cycleway.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = cycleway.district,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Cerrar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummaryIndicators(
    cycleway: SelectedCyclewayUi
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Security,
            title = "Vigilancia",
            value = cycleway.surveillance,
            color = qualityColor(cycleway.surveillance)
        )

        SummaryChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LightMode,
            title = "Iluminación",
            value = cycleway.lighting,
            color = qualityColor(cycleway.lighting)
        )

        SummaryChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.SwapHoriz,
            title = "Sentido",
            value = cycleway.direction,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SummaryChip(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(color = color)

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formatChipValue(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    cycleway: SelectedCyclewayUi
) {
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
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            DetailRow(
                icon = Icons.Outlined.Straighten,
                title = "Longitud",
                value = cycleway.lengthKm
            )

            DetailDivider()

            DetailRow(
                icon = Icons.Outlined.Route,
                title = "Tipo de segregación",
                value = cycleway.segregationType
            )

            DetailDivider()

            DetailRow(
                icon = Icons.Outlined.Construction,
                title = "Estado",
                value = cycleway.status,
                valueContent = {
                    StatusPill(
                        text = cycleway.status,
                        color = statusColor(cycleway.status)
                    )
                }
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (valueContent != null) {
            valueContent()
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 18.dp)
            .height(1.dp)
            .background(
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
    )
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(color = color)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusDot(
    color: Color
) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun qualityColor(value: String): Color {
    return when (value.lowercase()) {
        "buena", "alto", "alta", "existente", "bidireccional", "unidireccional" ->
            MaterialTheme.colorScheme.primary

        "regular", "media", "medio" ->
            Color(0xFFFFA000)

        "mala", "baja", "necesaria" ->
            MaterialTheme.colorScheme.error

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun statusColor(value: String): Color {
    return when (value.lowercase()) {
        "existente" -> MaterialTheme.colorScheme.primary
        "en proyecto" -> Color(0xFFFFA000)
        "en ejecución", "en ejecucion" -> Color(0xFF1976D2)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun formatChipValue(value: String): String {
    return when (value.lowercase()) {
        "no especificado" -> "N/D"
        "unidireccional" -> "Una vía"
        "bidireccional" -> "Doble vía"
        else -> value
    }
}