package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatePreset
import com.example.data.model.FilterDirection
import com.example.data.model.QueryFilter
import com.example.data.model.SortOption
import com.example.data.model.TimeWindowPreset
import com.example.ui.util.DateTimeFormatterUtil
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QueryFilterControls(
    currentFilter: QueryFilter,
    onFilterChange: (QueryFilter) -> Unit,
    onReset: () -> Unit,
    onShowResults: (() -> Unit)? = null,
    resultsCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 84.dp)
    ) {
        // 1. Keywords in SMS Body
        OutlinedTextField(
            value = currentFilter.keywords,
            onValueChange = { onFilterChange(currentFilter.copy(keywords = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Keywords in message") },
            placeholder = { Text("e.g. code, package, meeting, appointment") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Keywords",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (currentFilter.keywords.isNotBlank()) {
                    IconButton(onClick = { onFilterChange(currentFilter.copy(keywords = "")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Keywords")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Sender Name or Phone Number
        OutlinedTextField(
            value = currentFilter.senderQuery,
            onValueChange = { onFilterChange(currentFilter.copy(senderQuery = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Sender name or phone number") },
            placeholder = { Text("e.g. Sarah, Chase, +1 555...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Sender Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (currentFilter.senderQuery.isNotBlank()) {
                    IconButton(onClick = { onFilterChange(currentFilter.copy(senderQuery = "")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Sender")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Date Duration Presets & Range
        Text(
            text = "RECEIVED DATE DURATION",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePreset.values().forEach { preset ->
                FilterChip(
                    selected = currentFilter.datePreset == preset,
                    onClick = {
                        val updated = if (preset == DatePreset.CUSTOM_RANGE) {
                            val now = System.currentTimeMillis()
                            val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
                            currentFilter.copy(
                                datePreset = preset,
                                customStartDateMillis = currentFilter.customStartDateMillis ?: sevenDaysAgo,
                                customEndDateMillis = currentFilter.customEndDateMillis ?: now
                            )
                        } else {
                            currentFilter.copy(datePreset = preset)
                        }
                        onFilterChange(updated)
                    },
                    label = { Text(preset.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Custom Date Range Selectors
        AnimatedVisibility(visible = currentFilter.datePreset == DatePreset.CUSTOM_RANGE) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("FROM DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentFilter.customStartDateMillis?.let { DateTimeFormatterUtil.formatDate(it) } ?: "Select Date",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // End Date button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("TO DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentFilter.customEndDateMillis?.let { DateTimeFormatterUtil.formatDate(it) } ?: "Select Date",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Custom Specified Duration: TIME OF DAY WINDOW (e.g. 9am to 6pm)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentFilter.applyTimeOfDayWindow)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (currentFilter.applyTimeOfDayWindow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Time of Day Window",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "e.g. between 9am to 6pm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Switch(
                        checked = currentFilter.applyTimeOfDayWindow,
                        onCheckedChange = { enabled ->
                            onFilterChange(currentFilter.copy(applyTimeOfDayWindow = enabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                AnimatedVisibility(visible = currentFilter.applyTimeOfDayWindow) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "PRESET TIME WINDOWS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TimeWindowPreset.values().forEach { preset ->
                                FilterChip(
                                    selected = currentFilter.timePreset == preset,
                                    onClick = {
                                        val updated = if (preset == TimeWindowPreset.CUSTOM) {
                                            currentFilter.copy(timePreset = preset)
                                        } else {
                                            currentFilter.copy(
                                                timePreset = preset,
                                                timeStartHour = preset.startHour,
                                                timeStartMinute = preset.startMinute,
                                                timeEndHour = preset.endHour,
                                                timeEndMinute = preset.endMinute
                                            )
                                        }
                                        onFilterChange(updated)
                                    },
                                    label = { Text(preset.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Time Window Adjuster
                        TimeWindowAdjuster(
                            startHour = currentFilter.timeStartHour,
                            startMinute = currentFilter.timeStartMinute,
                            endHour = currentFilter.timeEndHour,
                            endMinute = currentFilter.timeEndMinute,
                            onTimeWindowChange = { sh, sm, eh, em ->
                                onFilterChange(
                                    currentFilter.copy(
                                        timePreset = TimeWindowPreset.CUSTOM,
                                        timeStartHour = sh,
                                        timeStartMinute = sm,
                                        timeEndHour = eh,
                                        timeEndMinute = em
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Message Direction (All, Received, Sent)
        Text(
            text = "MESSAGE TYPE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDirection.values().forEach { dir ->
                FilterChip(
                    selected = currentFilter.direction == dir,
                    onClick = { onFilterChange(currentFilter.copy(direction = dir)) },
                    label = { Text(dir.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Sort Option
        Text(
            text = "SORT BY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.values().forEach { sort ->
                FilterChip(
                    selected = currentFilter.sortBy == sort,
                    onClick = { onFilterChange(currentFilter.copy(sortBy = sort)) },
                    label = { Text(sort.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Reset Filters & Show Results
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reset_filters_button")
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Filters")
            }

            if (onShowResults != null) {
                Button(
                    onClick = onShowResults,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("show_results_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (resultsCount != null) "Show Results ($resultsCount)" else "Show Results"
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog for Start Date
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentFilter.customStartDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onFilterChange(currentFilter.copy(customStartDateMillis = millis))
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 Date Picker Dialog for End Date
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentFilter.customEndDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onFilterChange(currentFilter.copy(customEndDateMillis = millis))
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TimeWindowAdjuster(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onTimeWindowChange: (Int, Int, Int, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Time Box
        TimeStepper(
            label = "From",
            hour = startHour,
            minute = startMinute,
            onHourChange = { newHour ->
                onTimeWindowChange((newHour + 24) % 24, startMinute, endHour, endMinute)
            }
        )

        Text(
            text = "→",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // End Time Box
        TimeStepper(
            label = "To",
            hour = endHour,
            minute = endMinute,
            onHourChange = { newHour ->
                onTimeWindowChange(startHour, startMinute, (newHour + 24) % 24, endMinute)
            }
        )
    }
}

@Composable
private fun TimeStepper(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onHourChange(hour - 1) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Hour", modifier = Modifier.size(16.dp))
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = QueryFilter.formatHourMinute(hour, minute),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = { onHourChange(hour + 1) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Hour", modifier = Modifier.size(16.dp))
            }
        }
    }
}
