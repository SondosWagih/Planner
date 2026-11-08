package app.planner.ui.study

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.planner.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import android.graphics.Color as AndroidColor

private fun hexes(vararg h: String): List<Int> = h.map { 0xFF000000.toInt() or it.toInt(16) }

/** Ready-made colours, grouped into themes so only a handful are on screen at once. */
enum class ColorTheme(@StringRes val label: Int, val colors: List<Int>) {
    PASTEL(R.string.palette_pastel, hexes("F8BBD0", "E1BEE7", "BBDEFB", "B2EBF2", "C8E6C9", "FFE0B2")),
    VIVID(R.string.palette_vivid, hexes("E53935", "FB8C00", "FDD835", "43A047", "1E88E5", "8E24AA")),
    EARTH(R.string.palette_earth, hexes("795548", "A1887F", "7D8F69", "C19A6B", "B7410E", "556B2F")),
    OCEAN(R.string.palette_ocean, hexes("0D47A1", "1976D2", "0097A7", "26A69A", "4DB6AC", "80DEEA")),
    SUNSET(R.string.palette_sunset, hexes("B71C1C", "E64A19", "F57C00", "FFB300", "C2185B", "7B1FA2")),
    NEUTRAL(R.string.palette_neutral, hexes("212121", "424242", "616161", "757575", "9E9E9E", "BDBDBD")),
}

val DefaultCourseColor: Int = ColorTheme.VIVID.colors[4] // blue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(label: String, selected: Int, onSelect: (Int) -> Unit) {
    var hsv by remember { mutableStateOf(selected.toHsv()) }
    var hex by remember { mutableStateOf(toHex(selected)) }
    var themeIndex by remember {
        mutableIntStateOf(ColorTheme.entries.indexOfFirst { selected in it.colors }.coerceAtLeast(0))
    }

    fun applyColor(c: Int) { hsv = c.toHsv(); hex = toHex(c); onSelect(c) }
    fun applyHsv(h: FloatArray) { hsv = h; val c = AndroidColor.HSVToColor(h); hex = toHex(c); onSelect(c) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)

        // 1) exact colour code
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(selected)).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
            OutlinedTextField(
                value = hex,
                onValueChange = { t ->
                    val cleaned = t.uppercase().filter { it in "0123456789ABCDEF" }.take(6)
                    hex = cleaned
                    parseHexColor(cleaned)?.let { hsv = it.toHsv(); onSelect(it) }
                },
                label = { Text(stringResource(R.string.color_code)) },
                prefix = { Text("#") },
                supportingText = { Text(stringResource(R.string.color_code_hint)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        // 2) themed palettes: pick a theme, then one of its few colours
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorTheme.entries.forEachIndexed { i, theme ->
                FilterChip(selected = i == themeIndex, onClick = { themeIndex = i }, label = { Text(stringResource(theme.label)) })
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorTheme.entries[themeIndex].colors.forEach { c ->
                Box(Modifier.size(48.dp).clickable(role = Role.RadioButton, onClick = { applyColor(c) }).padding(4.dp)) {
                    Box(
                        Modifier.fillMaxSize().clip(CircleShape).background(Color(c)).then(
                            if (c == selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                    )
                }
            }
        }

        // 3) colour wheel (+ brightness)
        Text(stringResource(R.string.color_wheel), style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ColorWheel(
                hue = hsv[0], saturation = hsv[1], value = hsv[2],
                onChange = { h, s -> applyHsv(floatArrayOf(h, s, hsv[2])) },
                modifier = Modifier.size(200.dp),
            )
        }
        Text(stringResource(R.string.color_brightness), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value = hsv[2], onValueChange = { applyHsv(floatArrayOf(hsv[0], hsv[1], it)) }, valueRange = 0.05f..1f)
    }
}

private fun wheelPick(pos: Offset, size: IntSize, onChange: (Float, Float) -> Unit) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val dx = pos.x - cx
    val dy = pos.y - cy
    var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
    if (angle < 0f) angle += 360f
    onChange(angle, (hypot(dx, dy) / minOf(cx, cy)).coerceIn(0f, 1f))
}

@Composable
private fun ColorWheel(hue: Float, saturation: Float, value: Float, onChange: (Float, Float) -> Unit, modifier: Modifier = Modifier) {
    val current by rememberUpdatedState(onChange)
    val sweep = remember {
        Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red))
    }
    Canvas(
        modifier
            .pointerInput(Unit) { detectTapGestures { wheelPick(it, size, current) } }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { wheelPick(it, size, current) },
                    onDrag = { change, _ -> wheelPick(change.position, size, current) },
                )
            }
    ) {
        val r = size.minDimension / 2f
        drawCircle(brush = sweep, radius = r, center = center)
        drawCircle(brush = Brush.radialGradient(listOf(Color.White, Color.Transparent), center = center, radius = r), radius = r, center = center)
        drawCircle(color = Color.Black.copy(alpha = 1f - value), radius = r, center = center)
        val rad = Math.toRadians(hue.toDouble())
        val dist = saturation * r
        val p = Offset(center.x + (cos(rad) * dist).toFloat(), center.y + (sin(rad) * dist).toFloat())
        drawCircle(Color.White, radius = 10.dp.toPx(), center = p, style = Stroke(3.dp.toPx()))
        drawCircle(Color.Black, radius = 12.dp.toPx(), center = p, style = Stroke(1.dp.toPx()))
    }
}
