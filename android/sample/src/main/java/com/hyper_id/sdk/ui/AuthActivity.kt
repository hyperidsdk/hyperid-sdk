package com.hyper_id.sdk.ui

import ai.hyper_id.sdk.api.auth.model.AuthorizationMethod
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import com.hyper_id.sdk.api.HyperIdSdkDemoApi
import com.hyper_id.sdk.ui.composable.BoxedButton
import com.hyper_id.sdk.ui.kyc_demo.KycActivity
import com.hyper_id.sdk.ui.mfa_demo.MfaActivity
import com.hyper_id.sdk.ui.storage_demo.StorageActivity
import com.hyper_id.sdk.ui.theme.Hyper_sdkTheme

//**************************************************************************************************
//	AuthActivity
//--------------------------------------------------------------------------------------------------
class AuthActivity : ComponentActivity()
{
	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState : Bundle?)
	{
		super.onCreate(savedInstanceState)

		val sdkViewModel : eViewModel by viewModels { eViewModel.Factory }
		sdkViewModel.test()

		setContent()
		{
			Hyper_sdkTheme()
			{
				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize())
				{
					UiSceneAuthDemo(sdkViewModel)
				}
			}
		}
	}
	companion object
	{
		const val TAG = "AuthActivity"
	}
}
//==================================================================================================
//	UiSceneAuthSettings
//--------------------------------------------------------------------------------------------------
@ExperimentalMaterial3Api
@Composable
fun UiSceneAuthDemo(_viewModel : eViewModel)
{
	val authSDKState		= _viewModel.authApi.sdkAuthState.collectAsState()

	Column(modifier = Modifier
			.fillMaxSize()
			.background(Color.DarkGray))
	{
		SDKStatus(_viewModel)

		when(authSDKState.value)
		{
			HyperIdSdkDemoApi.SdkAuthState.CREATED		-> UiSceneAuthCreated(_viewModel)
			HyperIdSdkDemoApi.SdkAuthState.INITIALISED	-> UiSceneAuthInitialised(_viewModel)
			HyperIdSdkDemoApi.SdkAuthState.AUTHORIZING	-> UiSceneAuthWeb(_viewModel)
			HyperIdSdkDemoApi.SdkAuthState.AUTHORIZED	-> UiSceneAuthorized(_viewModel)
		}
	}
}
@ExperimentalMaterial3Api
@Composable
fun UiSceneAuthCreated(_viewModel : eViewModel)
{
	var isExpanded		by remember { mutableStateOf(false) }
	val authTypes		= listOf(
			Pair("Basic", AuthorizationMethod.CLIENT_SECRET),
			Pair("HS256", AuthorizationMethod.CLIENT_SECRET_HS256),
			Pair("RS256", AuthorizationMethod.CLIENT_RS256)
								 )
	var selectedItemIndex by remember { mutableStateOf(1) }

	Column(modifier = Modifier.fillMaxWidth())
	{
		Box(modifier = Modifier.padding(8.dp))
		{
			ExposedDropdownMenuBox(expanded = isExpanded,
								   onExpandedChange = { isExpanded = !isExpanded })
			{
				TextField(value = authTypes[selectedItemIndex].first,
						  onValueChange = {},
						  readOnly = true,
						  modifier = Modifier.menuAnchor(),
						  trailingIcon = { Image(painter = painterResource(id = android.R.drawable.ic_menu_more),
											   contentDescription = "") })
				ExposedDropdownMenu(expanded = isExpanded,
									onDismissRequest = { isExpanded = false })
				{
					authTypes.forEachIndexed()
					{ _index, _authType ->
						DropdownMenuItem(text = { Text(text = _authType.first) },
										 onClick =	{
														_viewModel.authApi.ConnectionTypeUpdate(_authType.second)
														selectedItemIndex = _index
														isExpanded = false
													},
										 )
					}
				}
			}
		}
		BoxedButton(text	= "Init",
					onClick	= { _viewModel.authApi.sdkInit() })
	}
}

@Composable
fun UiSceneAuthInitialised(_viewModel : eViewModel)
{
	Column(modifier = Modifier
			.fillMaxWidth())
	{
		BoxedButton(text	= "SignIn",
					onClick	= { _viewModel.authApi.sdkAuthSignIn() })

		BoxedButton(text	= "Sign to signIn",
					onClick	= { _viewModel.authApi.sdkAuthSignToSignIn() })

		BoxedButton(text	= "Wallet Get",
					onClick	= { _viewModel.authApi.sdkAuthWalletGet() })

		BoxedButton(text	= "Guest Upgrade",
					onClick	= { _viewModel.authApi.sdkAuthGuestUpgrade() })

		Box()
		{
			Column(modifier = Modifier.fillMaxWidth())
			{
				IdentityProviders(_viewModel)
				BoxedButton(text	= "Identity Provider",
							onClick	= { _viewModel.authApi.sdkAuthIdentityProvider() })
			}
		}

		BoxedButton(text	= "Transaction",
					onClick	= { _viewModel.authApi.sdkAuthTransaction() })
	}
}

@Composable
fun UiSceneAuthorized(_viewModel : eViewModel)
{
	val context = LocalContext.current

	Column()
	{
		Text(text = "Actions with current auth state",
			 color = Color.White,
			 modifier = Modifier.padding(8.dp))

		BoxedButton(text	= "User Info",
					onClick	= { _viewModel.authApi.userInfo() })
		BoxedButton(text	= "Logout",
					onClick	= { _viewModel.authApi.logout() })

		Text(text = "Nested SDK action possible",
			 color = Color.White,
			 modifier = Modifier.padding(8.dp))

		BoxedButton(text	= "Go to MFA test",
					onClick	= { startActivity(context, Intent(context, MfaActivity::class.java), null) })
		BoxedButton(text	= "Go to KYC test",
					onClick	= { startActivity(context, Intent(context, KycActivity::class.java), null) })
		BoxedButton(text	= "Go to Storage test",
					onClick	= { startActivity(context, Intent(context, StorageActivity::class.java), null) })
	}
}
//==================================================================================================
//	SDKStatus
//--------------------------------------------------------------------------------------------------
@Composable
fun SDKStatus(_viewModel : eViewModel)
{
	val sdkAuthState	= _viewModel.authApi.sdkAuthState.collectAsState()

	Log.d(AuthActivity.TAG, "[SDKStatus] sdkStatus/${sdkAuthState.value}")

	Text(text		=	when(sdkAuthState.value)
						{
							HyperIdSdkDemoApi.SdkAuthState.CREATED		-> "Auth SDK init required "
							HyperIdSdkDemoApi.SdkAuthState.INITIALISED	-> "Select auth flow"
							HyperIdSdkDemoApi.SdkAuthState.AUTHORIZING	-> "Authorizing"
							HyperIdSdkDemoApi.SdkAuthState.AUTHORIZED	-> "Authorization complete."
						},
		 color		= Color.White,
		 modifier	= Modifier
				 .fillMaxWidth()
				 .padding(16.dp))
}
//==================================================================================================
//	IdentityProviders
//--------------------------------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdentityProviders(_viewModel : eViewModel)
{
	val idp			= _viewModel.authApi.identityProviders.collectAsState()
	val provider	= _viewModel.authApi.identityProviderStateFlow.collectAsState()

	Log.d(AuthActivity.TAG, "[IdentityProviders] idp/${idp.value.joinToString { it }}")

//	LazyVerticalStaggeredGrid(columns				= StaggeredGridCells.Adaptive(120.dp),
//							  modifier				= Modifier.fillMaxWidth().padding(8.dp),
//							  verticalItemSpacing	= 4.dp,
//							  horizontalArrangement	= Arrangement.spacedBy(4.dp),
//							  content				=
//							  {
//								  items(idp.value)
//								  {
//									  RadioButtonWithText(_text = it,
//														  _isSelected = provider.value == it,
//														  { _viewModel.authApi.identityProvider = it })
//								  }
//							  })
}

//==================================================================================================
//	RadioButtonWithText
//--------------------------------------------------------------------------------------------------
@Composable
fun RadioButtonWithText(_text				: String,
						_isSelected			: Boolean,
						_onClickListener	: () -> Unit)
{
	Row(modifier = Modifier.height(32.dp))
	{
		RadioButton(selected = _isSelected,
					onClick = { _onClickListener.invoke() })
		Text(text		= _text,
			 color		= Color.White,
			 modifier	= Modifier.align(Alignment.CenterVertically))
	}
}
//**************************************************************************************************
//	UiSceneAuthWeb
//
// for google and microsoft identity provider must be user external browser(Android CustomTabs) to
// correct work
//--------------------------------------------------------------------------------------------------
@Composable
fun UiSceneAuthWeb(_viewModel : eViewModel)
{
	LaunchedEffect(Unit)
	{
		CookieManager.getInstance().removeAllCookies(null)
	}

	AndroidView(factory =
				{ context->
					WebView(context).apply()
					{
						setBackgroundColor(Color.Black.toArgb())

						layoutParams	= ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
																 ViewGroup.LayoutParams.MATCH_PARENT)

						clearCache(true)

						with(settings)
						{
							@SuppressLint("SetJavaScriptEnabled")
							javaScriptEnabled						= true
							blockNetworkLoads						= false
							settings.domStorageEnabled				= true
							cacheMode								= WebSettings.LOAD_CACHE_ELSE_NETWORK
							setLayerType(View.LAYER_TYPE_HARDWARE, null)
						}

						webViewClient = object : WebViewClient()
						{
							//==================================================================================================
							//	onPageFinished
							//--------------------------------------------------------------------------------------------------
							override fun onPageFinished(_view : WebView?, _url : String?)
							{
								super.onPageFinished(_view, _url)

								Log.d(javaClass.simpleName, "[onPageFinished] $_url")
							}
							//==================================================================================================
							//	shouldOverrideUrlLoading
							//--------------------------------------------------------------------------------------------------
							override fun shouldOverrideUrlLoading(_view : WebView?, _request : WebResourceRequest?) : Boolean
							{
								Log.d(javaClass.simpleName, "[shouldOverrideUrlLoading] _request url/${_request?.url}")

								_request?.url?.also()
								{
									if(it.path?.endsWith("registration", true) == true)
									{
										try
										{
											it.also()
											{
												val url = "${it.scheme}://${it.host}"

												//eLog.DEBUG(javaClass.simpleName, "[shouldOverrideUrlLoading] url/$url")

												val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
												context.packageManager
														.queryIntentActivities(intent,
																			   PackageManager.MATCH_ALL)
														.also()
														{ _activities ->
															if(_activities.isNotEmpty())
															{
																startActivity(context, intent, null)
															}
															else
															{
																//eCore.ShowAlarm(resources.getString(R.string.hyper_id_account_authorization_no_app_to_open_register_link))
															}
														}
												return false
											}
										}
										catch(_exception : Exception)
										{
											//eCore.ShowAlarm(resources.getString(R.string.hyper_id_account_authorization_no_app_to_open_register_link))
										}
									}
									else if(it.host?.contains("localhost") != true)
									{
										return false
									}
									else
									{
										_viewModel.authApi.authCompleteWithRedirect(it.toString())
									}
								}
								return true
							}
							//==================================================================================================
							//	shouldOverrideUrlLoading
							//--------------------------------------------------------------------------------------------------
							@Suppress("OverridingDeprecatedMember")
							override fun shouldOverrideUrlLoading(_view	: WebView?,
																  _url	: String?)	: Boolean
							{
								Log.d(javaClass.simpleName, "[shouldOverrideUrlLoading] 2")

								if(_url != null)
								{
									val uri : Uri
									try
									{
										uri = Uri.parse(_url)
									}
									catch(_exception : Exception)
									{
										return false
									}

									if(uri.path?.endsWith("registration", true) == true)
									{
										try
										{
											val url = "${uri.scheme}://${uri.host}"

											//eLog.DEBUG(javaClass.simpleName, "[shouldOverrideUrlLoading] url/$url")

											val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
											context.packageManager
													.queryIntentActivities(intent,
																		   PackageManager.MATCH_ALL)
													.also()
													{ _activities ->
														if(_activities.isNotEmpty())
														{
															startActivity(context, intent, null)
														}
														else
														{
//															eCore.ShowAlarm(resources.getString(R.string.hyper_id_account_authorization_no_app_to_open_register_link))
														}
													}
											return false
										}
										catch(_exception : Exception)
										{
//											eCore.ShowAlarm(resources.getString(R.string.hyper_id_account_authorization_no_app_to_open_register_link))
										}
									}
									else if(uri.host?.contains("localhost") != true)
									{
										return false
									}
									else
									{
										_viewModel.authApi.authCompleteWithRedirect(_url)
									}
								}
								return true
							}
						}
					}
				},
				update = {
					_viewModel.authApi.authUrl?.also()
					{ _authUrl ->
						it.loadUrl(_authUrl)
					}
				})
}

@ExperimentalMaterial3Api
@Preview(name = "Auth SDK not initialised", showBackground = true)
@Composable
fun PreviewUiSceneAuthCreated()
{
//	UiSceneAuthCreated(eViewModel(HyperIdSdkAuthDemoApi()))
}

@Preview(name = "Auth SDK initialised", showBackground = true)
@Composable
fun PreviewUiSceneAuthInitialised()
{
//	UiSceneAuthInitialised(eViewModel(HyperIdSdkAuthDemoApi()))
}

@Preview(name = "Auth complete show available sdk", showBackground = true)
@Composable
fun PreviewUiSceneAuthorized()
{
//	UiSceneAuthorized(eViewModel(HyperIdSdkAuthDemoApi()))
}
