package com.hyper_id.sdk.ui.mfa_demo

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hyper_id.sdk.api.HyperIdSdkMFADemoApi
import com.hyper_id.sdk.ui.composable.BoxedButton
import com.hyper_id.sdk.ui.theme.Hyper_sdkTheme

class MfaActivity : ComponentActivity()
{
	@ExperimentalMaterial3Api
	override fun onCreate(savedInstanceState : Bundle?)
	{
		super.onCreate(savedInstanceState)

		val mfaViewModel : ViewModelMFADemo by viewModels { ViewModelMFADemo.Factory }

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
						Text(text		= "Close MFA Demo",
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
						MFADemoUi(_viewModel = mfaViewModel)
					}
				}
			}
		}
	}

	companion object
	{
		@Suppress("unused")
		const val TAG = "AuthActivity"
	}
}

@Composable
fun MFADemoUi(_viewModel : ViewModelMFADemo)
{
	val state = _viewModel.mfaApi.stateFlow.collectAsState()

	when(state.value)
	{
		HyperIdSdkMFADemoApi.HyperIdSDKMFAState.CREATED			-> UiSceneAvailabilityCheck(_viewModel)
		HyperIdSdkMFADemoApi.HyperIdSDKMFAState.AVAILABLE		-> UiSceneTransaction(_viewModel)
		HyperIdSdkMFADemoApi.HyperIdSDKMFAState.NOT_AVAILABLE	-> {}
	}
}

@Composable
fun UiSceneAvailabilityCheck(_viewModel : ViewModelMFADemo)
{
	Column(modifier = Modifier.fillMaxWidth())
	{
		BoxedButton(text	= "Check is MFA available",
					onClick	= { _viewModel.mfaApi.checkAvailability() })
	}
}
@Composable
fun UiSceneTransaction(_viewModel : ViewModelMFADemo)
{
	val transactionId = _viewModel.mfaApi.transactionIdFlow.collectAsState()

	Log.d(MfaActivity.TAG, "[UiSceneTransaction] transactionId/${transactionId.value}")

	if(transactionId.value == -1)
	{
		Column(modifier = Modifier.fillMaxWidth())
		{
			BoxedButton(text	= "Transaction start",
						onClick	= { _viewModel.mfaApi.transactionStart() })
		}
	}
	else
	{
		Column(modifier = Modifier.fillMaxWidth())
		{
			BoxedButton(text	= "Transaction status check",
						onClick	= { _viewModel.mfaApi.transactionStatusCheck() })

			BoxedButton(text	= "Transaction cancel",
						onClick	= { _viewModel.mfaApi.transactionCancel() })
		}
	}

}

@Preview(name = "")
@Composable
fun PreviewUiSceneInit()
{
//	UiSceneInit(ViewModelMFADemo(HyperIdSdkMFADemoApi(HyperIdSdkAuthCreate())))
}
@Preview(name = "")
@Composable
fun PreviewUiSceneAvailabilityCheck()
{
//	UiSceneAvailabilityCheck(ViewModelMFADemo(HyperIdSdkMFADemoApi(HyperIdSdkAuthCreate())))
}
@Preview(name = "")
@Composable
fun PreviewUiSceneTransaction()
{
//	UiSceneTransaction(ViewModelMFADemo(HyperIdSdkMFADemoApi(HyperIdSdkAuthCreate())))
}
