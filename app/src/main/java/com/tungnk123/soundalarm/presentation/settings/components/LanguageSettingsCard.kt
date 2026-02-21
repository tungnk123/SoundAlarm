package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.util.LocaleManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsCard(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
) {
    val languages = listOf(
        LocaleManager.LANG_EN to stringResource(R.string.settings_language_en),
        LocaleManager.LANG_VI to stringResource(R.string.settings_language_vi),
    )

    SettingsCard(
        title = stringResource(R.string.settings_section_language),
        icon = Icons.Default.Language,
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            languages.forEachIndexed { index, (code, label) ->
                SegmentedButton(
                    selected = currentLanguage == code,
                    onClick = { onLanguageChange(code) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
                    label = { Text(label) },
                )
            }
        }
    }
}
