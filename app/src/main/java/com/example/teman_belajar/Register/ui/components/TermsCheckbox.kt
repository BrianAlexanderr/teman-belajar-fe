package com.example.teman_belajar.Register.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationSpacing

@Composable
fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Terms and Agreements",
    error: String? = null,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCheckedChange(!checked) }
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor   = RegistrationColors.Purple,
                    uncheckedColor = if (error != null) RegistrationColors.Error
                    else RegistrationColors.TextSecondary
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RegistrationColors.Purple
                ),
                modifier = Modifier.padding(start = RegistrationSpacing.xs)
            )
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit  = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error.orEmpty(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = RegistrationColors.Error
                ),
                modifier = Modifier.padding(
                    start = RegistrationSpacing.md,
                    top   = RegistrationSpacing.xs
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TermsCheckboxPreview() {
    MaterialTheme {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            TermsCheckbox(checked = false, onCheckedChange = {})
            TermsCheckbox(checked = true,  onCheckedChange = {})
            TermsCheckbox(
                checked = false,
                onCheckedChange = {},
                error = "You must agree to the terms"
            )
        }
    }
}