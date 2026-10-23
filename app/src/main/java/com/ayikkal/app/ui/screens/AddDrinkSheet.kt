package com.ayikkal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.data.db.DrinkCategory
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.theme.Ak

fun categoryIcon(c: DrinkCategory): ImageVector = when (c) {
    DrinkCategory.BEER -> Icons.Rounded.SportsBar
    DrinkCategory.WINE -> Icons.Rounded.WineBar
    DrinkCategory.COCKTAIL, DrinkCategory.GIN -> Icons.Rounded.LocalBar
    else -> Icons.Rounded.Liquor
}

fun categoryColor(c: DrinkCategory): Color = when (c) {
    DrinkCategory.BEER -> Ak.Secondary
    DrinkCategory.WINE -> Ak.Rose
    DrinkCategory.RAKI -> Color(0xFFE5E7EB)
    DrinkCategory.COCKTAIL, DrinkCategory.GIN -> Ak.Primary
    DrinkCategory.WHISKY -> Ak.Orange
    else -> Color(0xFF93C5FD)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrinkSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val types by vm.drinkTypes.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf(DrinkCategory.BEER) }
    var custom by remember { mutableStateOf(false) }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = Ak.Low) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text("Ne içiyorsun?", style = MaterialTheme.typography.headlineMedium, color = Ak.OnSurface)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DrinkCategory.entries.filter { it != DrinkCategory.OTHER }.forEach { c ->
                    CategoryChip(c, selected = c == category && !custom) {
                        category = c
                        custom = false
                    }
                }
                Chip("Özel", Icons.Rounded.Edit, Ak.OnVariant, custom) { custom = true }
            }
            Spacer(Modifier.height(14.dp))
            if (custom) {
                CustomDrinkForm(category) { name, cat, ml, abv ->
                    vm.addCustomDrink(name, cat, ml, abv)
                    onDismiss()
                }
            } else {
                LazyColumn(Modifier.heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(types.filter { it.category == category }, key = { it.id }) { t ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Ak.Container)
                                .clickable {
                                    vm.addDrink(t)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconBadge(categoryIcon(t.category), categoryColor(t.category), size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.name, style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface)
                                Text(
                                    "${t.servingLabel} · %${"%.1f".format(t.abv)} · ${"%.1f".format(BacCalculator.standardDrinks(t.volumeMl, t.abv))} standart içki",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Ak.Muted,
                                )
                            }
                            Icon(Icons.Rounded.Add, null, tint = Ak.Primary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CategoryChip(c: DrinkCategory, selected: Boolean, onClick: () -> Unit) =
    Chip(c.label, categoryIcon(c), categoryColor(c), selected, onClick)

@Composable
private fun Chip(text: String, icon: ImageVector, color: Color, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color else Ak.Container)
            .border(1.dp, if (selected) color else Ak.Hairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) Ak.Bg else color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) Ak.Bg else Ak.OnSurface)
    }
}

@Composable
private fun CustomDrinkForm(initial: DrinkCategory, onAdd: (String, DrinkCategory, Int, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var ml by remember { mutableStateOf("") }
    var abv by remember { mutableStateOf("") }
    val volume = ml.toIntOrNull()
    val percent = abv.replace(',', '.').toDoubleOrNull()
    val valid = name.isNotBlank() && volume != null && volume in 10..2000 && percent != null && percent in 0.5..96.0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Field(name, { name = it }, "İçki / marka adı", KeyboardType.Text)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field(ml, { ml = it.filter(Char::isDigit).take(4) }, "Hacim (ml)", KeyboardType.Number, Modifier.weight(1f))
            Field(abv, { abv = it.take(4) }, "Alkol %", KeyboardType.Decimal, Modifier.weight(1f))
        }
        if (volume != null && percent != null) {
            Text(
                "≈ ${"%.1f".format(BacCalculator.grams(volume, percent))} g saf alkol",
                style = MaterialTheme.typography.labelMedium,
                color = Ak.Muted,
            )
        }
        BigButton("Ekle ve Kaydet", Icons.Rounded.Add, { onAdd(name, initial, volume!!, percent!!) }, enabled = valid, height = 56.dp)
    }
}

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    type: KeyboardType,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Ak.Lowest,
            unfocusedContainerColor = Ak.Lowest,
            disabledContainerColor = Ak.Lowest,
            focusedBorderColor = Ak.Primary,
            unfocusedBorderColor = Ak.Hairline,
        ),
    )
}
