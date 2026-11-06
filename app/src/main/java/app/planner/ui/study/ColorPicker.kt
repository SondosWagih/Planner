package app.planner.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.planner.R
import android.graphics.Color as AndroidColor

/**
 * 54 ready-made colours: 12 hues x 4 tones (pastel, light, vivid, deep) plus 6 neutrals.
 * Anything else can be mixed with the sliders below the grid.
 */
val ColorPalette: List<Int> = buildList {
    val tones = listOf(0.35f to 1.00f, 0.65f to 1.00f, 0.85f to 0.85f, 0.90f to 0.55f)
    for ((s, v) in tones) for (i in 0 until 12) add(AndroidColor.HSVToColor(floatArrayOf(i * 30f, s, v)))
    for (g in listOf(0.15f, 0.30f, 0.45f, 0.60f, 0.75f, 0.90f)) add(AndroidColor.HSVToColor(floatArrayOf(0f, 0f, g)))
}

val DefaultCourseColor: Int = ColorPalette[12 + 8] // a vivid blue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(label: String, selected: Int, onSelect: (Int) -> Unit) {
    var hsv by remember { mutableStateOf(FloatArray(3).also { AndroidColor.colorToHSV(selected, it) }) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ColorPalette.forEach { c ->
                Box(
                    Modifier.size(44.dp).clickable(role = Role.RadioButton, onClick = {
                        val a = FloatArray(3)
                        AndroidColor.colorToHSV(c, a)
                        hsv = a
                        onSelect(c)
                    }).padding(4.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize().clip(CircleShape).background(Color(c))
                            .then(
                                if (c == selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.custom_color), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color(selected))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
        ColorSlider(stringResource(R.string.color_hue), hsv[0], 0f..360f) {
            hsv = floatArrayOf(it, hsv[1], hsv[2]); onSelect(AndroidColor.HSVToColor(hsv))
        }
        ColorSlider(stringResource(R.string.color_saturation), hsv[1], 0f..1f) {
            hsv = floatArrayOf(hsv[0], it, hsv[2]); onSelect(AndroidColor.HSVToColor(hsv))
        }
        ColorSlider(stringResource(R.string.color_brightness), hsv[2], 0f..1f) {
            hsv = floatArrayOf(hsv[0], hsv[1], it); onSelect(AndroidColor.HSVToColor(hsv))
        }
    }
}

@Composable
private fun ColorSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
