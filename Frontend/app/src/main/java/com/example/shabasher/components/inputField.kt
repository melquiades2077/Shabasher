package com.example.shabasher.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    placeholder: String? = null,
    prefix: @Composable (() -> Unit)? = null, // 👈 НОВОЕ
    trailing: @Composable (() -> Unit)? = null
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { if (!readOnly) onValueChange(it) },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.error ) } },
        singleLine = singleLine,
        maxLines = maxLines,
        readOnly = readOnly,
        prefix = prefix, // 👈 сюда
        shape = RoundedCornerShape(20.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        visualTransformation =
            if (isPassword && !isPasswordVisible) PasswordVisualTransformation()
            else VisualTransformation.None,
        trailingIcon = trailing,
        modifier = modifier.fillMaxWidth(0.8f)
    )
}
