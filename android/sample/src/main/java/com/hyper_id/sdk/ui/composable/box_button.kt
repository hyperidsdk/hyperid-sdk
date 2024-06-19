package com.hyper_id.sdk.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BoxedButton(text	: String,
				onClick : () -> Unit)
{
	Box(modifier = Modifier.padding(8.dp))
	{
		TextButton(onClick = { onClick() },
				   colors = ButtonDefaults.textButtonColors(containerColor = Color.Blue,
															contentColor = Color.White))
		{
			Text(text = text)
		}
	}
}


@Preview(name = "")
@Composable
fun PreviewUiSceneInit()
{
	BoxedButton(text = "test long text") {}
}
