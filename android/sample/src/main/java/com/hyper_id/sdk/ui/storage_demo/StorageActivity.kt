package com.hyper_id.sdk.ui.storage_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hyper_id.sdk.ui.composable.BoxedButton
import com.hyper_id.sdk.ui.theme.Hyper_sdkTheme

class StorageActivity : ComponentActivity()
{
	@ExperimentalMaterial3Api
	override fun onCreate(savedInstanceState : Bundle?)
	{
		super.onCreate(savedInstanceState)

		val storageViewModel : ViewModelStorage by viewModels { ViewModelStorage.Factory }

		setContent()
		{
			Hyper_sdkTheme()
			{
				Scaffold(topBar = {
					Row(modifier = Modifier.fillMaxWidth().background(Color.Blue).height(56.dp))
					{
						IconButton(onClick = { finish() },
								   content = { Image(painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
													 contentDescription = "") },
								   modifier = Modifier.size(56.dp))
						Text(text		= "Storage Demo",
							 color		= Color.White,
							 textAlign	= TextAlign.Start,
							 modifier	= Modifier.height(56.dp).fillMaxWidth().wrapContentHeight(align = Alignment.CenterVertically))
					}
				})
				{
					Surface(modifier = Modifier
							.fillMaxSize()
							.padding(it))
					{
						UiSceneStorageApiDemo(_viewModel = storageViewModel)
					}
				}
			}
		}
	}
}
@Composable
fun UiSceneStorageApiDemo(_viewModel : ViewModelStorage)
{
	Column(modifier = Modifier.fillMaxWidth())
	{
		BoxedButton(text	= "Test storage by email",
					onClick	= { _viewModel.storageApi.testStorageByEmail() })
		BoxedButton(text	= "Test storage by userId",
					onClick	= { _viewModel.storageApi.testStorageByUserId() })
		BoxedButton(text	= "Test storage by wallet address",
					onClick	= { _viewModel.storageApi.testStorageByWalletAddress() })
		BoxedButton(text	= "Test storage by idp",
					onClick	= { _viewModel.storageApi.testStorageByIdp() })
	}
}

@Preview(name = "")
@Composable
fun PreviewUiSceneInit()
{
	//UiSceneInit(ViewModelStorage(HyperIdSdkStorageDemoApi(HyperIdSdkAuthCreate())))
}

@Preview(name = "")
@Composable
fun PreviewUiSceneStorageApiDemo()
{
//	UiSceneStorageApiDemo(ViewModelStorage(HyperIdSdkStorageDemoApi(HyperIdSdkAuthCreate())))
}
