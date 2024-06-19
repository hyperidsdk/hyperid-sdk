package ai.hyper_id.sdk.internal.auth

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.IHyperIDSDKAuth
import ai.hyper_id.sdk.api.auth.model.ClientInfo
import ai.hyper_id.sdk.api.auth.model.HyperIdProvider
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.auth.model.WalletFamily
import ai.hyper_id.sdk.api.auth.model.WalletGetMode
import ai.hyper_id.sdk.internal.auth.enums.AuthorizationFlowMode
import ai.hyper_id.sdk.internal.auth.json.DiscoverJson
import ai.hyper_id.sdk.internal.auth.json.HelpersJson
import ai.hyper_id.sdk.internal.auth.json.TokenInfoJson
import ai.hyper_id.sdk.internal.auth.json.UserInfoJson
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiRequestResult
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequestResult
import ai.hyper_id.sdk.internal.auth.types.Discover
import ai.hyper_id.sdk.internal.auth.types.HTTPTransport
import ai.hyper_id.sdk.internal.auth.types.TokenValidator
import ai.hyper_id.sdk.internal.auth.types.UriBuilders
import ai.hyper_id.sdk.internal.auth.types.Utils
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.serialization.json.Json
import org.json.JSONObject

//**************************************************************************************************
//	HyperIDSDKAuthImpl
//--------------------------------------------------------------------------------------------------
internal class HyperIDSDKAuthImpl : IHyperIDSDKAuth, IRestApiInterface
{
	private interface IAccessTokenListener {
		fun onRequestComplete(_result		: IHyperIdSDK.RequestResult,
							  _errorDesc	: String?,
							  _accessToken	: String?,
							  _refreshToken : String?)
	}

	//==================================================================================================
	//	Init
	//--------------------------------------------------------------------------------------------------
	fun init(provider			: HyperIdProvider,
			 clientInfo			: ClientInfo,
			 authRestoreInfo	: String?,
			 completeListener	: IHyperIdSDK.IRequestResultListener)
	{
		providerUri		= Uri.parse(when(provider)
									{
										HyperIdProvider.PRODUCTION	-> "https://login.hypersecureid.com"
										HyperIdProvider.SANDBOX		-> "https://login-sandbox.hypersecureid.com"
									})
		this.clientInfo		= clientInfo
		this.refreshToken	= authRestoreInfo

		if(this.clientInfo?.isValid() == false)
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
											   "ClientInfo is not valid")
		}
		else
		{
			transport.requestGet(	UriBuilders.discover(providerUri!!),
									null,
									object : HTTPTransport.ITransportRequestResult
									{
										override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
																	   _requestResultCode	: Int,
																	   _requestAnswerBody	: String?)
										{
											when(_result)
											{
												HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION		->
												{
													completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_CONNECTION, null)
												}
												HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER			->
												{
													completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																					   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody))
												}
												HTTPTransport.TransportRequestResult.SUCCESS					->
												{
													if(_requestAnswerBody != null)
													{
														try
														{
															discover = jsonParser.decodeFromString<DiscoverJson>(_requestAnswerBody).toDiscover()
															if(discover?.isValid() == true)
															{
																Log.d(TAG, "[Discover] $discover")

																completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																								   null)
															}
															else
															{
																completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																								   "Discover failed")
															}
														}
														catch(_exception : Exception)
														{
															Log.d(TAG, "[Discover] _ex:${_exception.localizedMessage}")

															completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																							   "Service answer not valid. Answer: [$_requestAnswerBody]")
														}
													}
													else
													{
														completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																						   "Service answer not valid. Answer is empty")
													}
												}
											}
										}
									})
		}
	}
	//==================================================================================================
	//	Done
	//--------------------------------------------------------------------------------------------------
	fun done()
	{
		transport.done()

		discover			= null
		accessToken			= null
		refreshToken		= null
	}
	//==================================================================================================
	//	startSignInWeb2 - 0
	//--------------------------------------------------------------------------------------------------
	override fun startSignInWeb2(verificationLevel: KycVerificationLevel?,
								 completeListener : IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		AuthorizationStart(_flowMode			= AuthorizationFlowMode.SIGN_IN,
						   _verificationLevel	= verificationLevel,
						   _completeListener	= completeListener)
	}
	//==================================================================================================
	//	startSignInWeb3 - 3
	//--------------------------------------------------------------------------------------------------
	override fun startSignInWeb3(walletFamily			: WalletFamily?,
								 verificationLevel		: KycVerificationLevel?,
								 completeListener		: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		AuthorizationStart(_flowMode			= AuthorizationFlowMode.WALLET_GET,
						   _walletFamily		= walletFamily,
						   _verificationLevel	= verificationLevel,
						   _completeListener	= completeListener)
	}
	//==================================================================================================
	//	startSignInWalletGet - 4
	//--------------------------------------------------------------------------------------------------
	override fun startSignInWalletGet(walletGetMode		: WalletGetMode,
									  walletFamily		: WalletFamily?,
									  completeListener	: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		AuthorizationStart(_flowMode		= AuthorizationFlowMode.WALLET_GET,
						   _walletGetMode	= walletGetMode,
						   _walletFamily	= walletFamily,
						   _completeListener= completeListener)
	}
	//==================================================================================================
	//	startSignInGuestUpgrade - 6
	//--------------------------------------------------------------------------------------------------
	override fun startSignInGuestUpgrade(completeListener		: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		AuthorizationStart(_flowMode			= AuthorizationFlowMode.GUEST_UPGRADE,
						   _completeListener	= completeListener)
	}
	//==================================================================================================
	//	startSignInIdentityProvider - 9
	//--------------------------------------------------------------------------------------------------
	override fun startSignInIdentityProvider(identityProvider	: String,
											 verificationLevel	: KycVerificationLevel?,
											 completeListener	: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		AuthorizationStart(_flowMode			= AuthorizationFlowMode.IDENTITY_PROVIDER,
						   _identityProvider	= identityProvider,
						   _verificationLevel	= verificationLevel,
						   _completeListener	= completeListener)
	}
	override fun startSignInWithTransaction(from				: String?,
											to					: String,
											chain				: String,
											data				: String?,
											value				: String?,
											gas					: String?,
											nonce				: String?,
											completeListener	: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
											   null,
											   null)
			return
		}

		val json = JSONObject()
		if(!from.isNullOrBlank())
		{
			json.put("from", from)
		}

		if(to.isBlank())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
											   "Field \"to\" can not be empty",
											   null)
			return
		}
		json.put("to", to)

		if(chain.isBlank())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
											   "Field \"chain\" can not be empty",
											   null)
			return
		}
		json.put("chain", chain)

		if(data.isNullOrBlank() && value.isNullOrBlank())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
											   "Field \"data\" can not be empty",
											   null)
			return
		}

		if(data.isNullOrBlank())
		{
			json.put("data", "0x0")
		}
		else
		{
			json.put("data", data)
		}

		if(!value.isNullOrBlank())
		{
			json.put("value", value)
		}

		if(!gas.isNullOrBlank())
		{
			json.put("gas", gas)
		}

		if(!nonce.isNullOrBlank())
		{
			json.put("nonce", nonce)
		}

		val uri = UriBuilders.authorization(_endpoint			= discover!!.authEndpoint,
											_scopes				= discover!!.scopes,
											_clientInfo			= clientInfo!!,
											_transactionInfo	= json.toString())

		completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
										   null,
										   uri.toString())
	}
	//==================================================================================================
	//	completeSignIn
	//--------------------------------------------------------------------------------------------------
	override fun completeSignIn(redirectUrl			: String,
								completeListener	: IHyperIDSDKAuth.IAuthorizationCompleteListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
											   null,
											   null)
			return
		}

		try
		{
			val uri		= Uri.parse(redirectUrl)
			val code	= uri.getQueryParameter("code")

			Log.d(TAG, "[authorizationCompleteWithRedirect] code/$code")

			if(code == null)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "${uri.getQueryParameter("error")} : ${uri.getQueryParameter("error_description")}",
												   null)
			}
			else
			{
				if(discover?.tokenEndpoint == null)
				{
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
													   null,
													   null)
				}
				else
				{
					val requestContent = Utils.tokensObtainParametersPrepare(clientInfo!!,
																			 discover!!,
																			 code)
					transport.requestPost(Uri.parse(discover!!.tokenEndpoint),
										  requestContent,
										  null,
										  null,
										  object : HTTPTransport.ITransportRequestResult
									{
										override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
																	   _requestResultCode	: Int,
																	   _requestAnswerBody	: String?)
										{
											when(_result)
											{
												HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION ->
												{
													completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_CONNECTION,
																					   null,
																					   null)
												}
												HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER     ->
												{
													completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																					   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody),
																					   null)
												}
												HTTPTransport.TransportRequestResult.SUCCESS               ->
												{
													if(_requestAnswerBody != null)
													{
														try
														{
															jsonParser.decodeFromString<TokenInfoJson>(_requestAnswerBody).also()
															{
																accessToken		= it.accessTokenRaw
																refreshToken	= it.refreshTokenRaw
															}

															if(accessToken?.isNotBlank() == true && refreshToken?.isNotBlank() == true)
															{
																completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																								   null,
																								   refreshToken)
															}
															else
															{
																completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																								   "Can't receive tokens",
																								   null)
															}
														}
														catch(_exception : Exception)
														{
															Log.e(TAG, "token info is invalid")

															completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																							   "Service answer is not valid. Answer: [$_requestAnswerBody]",
																							   null)
														}
													}
													else
													{
														completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																						   "Service answer is not valid. Answer is empty",
																						   null)
													}
												}
											}
										}
									})
				}
			}
		}
		catch(_exception : Exception)
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
											   "_redirectURL is not valid URL",
											   null)
		}
	}
	override fun completeSignInWithTransaction(redirectUrl		: String,
											   completeListener	: IHyperIDSDKAuth.IAuthorizationWithTransactionCompleteListener)
	{
		val authCompleteListener =	object : IHyperIDSDKAuth.IAuthorizationCompleteListener
									{
										override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
																	   _errorDesc		: String?,
																	   _authRestoreInfo	: String?)
										{
											if(_result == IHyperIdSDK.RequestResult.SUCCESS)
											{
												completeListener.onRequestComplete(_result,
																				   _errorDesc,
																				   Uri.parse(redirectUrl).getQueryParameter("transaction_hash"))
											}
											else
											{
												completeListener.onRequestComplete(_result,
																				   _errorDesc,
																				   null)
											}
										}
									}
		completeSignIn(redirectUrl, authCompleteListener)
	}
	//==================================================================================================
	//	AuthorizationStart
	//--------------------------------------------------------------------------------------------------
	private fun AuthorizationStart(_flowMode				: AuthorizationFlowMode,
								   _walletGetMode			: WalletGetMode?					= null,
								   _walletFamily			: WalletFamily?						= null,
								   _walletAddress			: String?							= null,
								   _identityProvider		: String?							= null,
								   _verificationLevel		: KycVerificationLevel?				= null,
								   _completeListener		: IHyperIDSDKAuth.IAuthorizationStartResultListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			_completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
												null,
												null)
			return
		}
		else if(_identityProvider != null && discover?.identityProviders?.contains(_identityProvider) == false)
		{
			_completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												"Identity provider is not discovered",
												null)
		}
		else
		{
			val uri = UriBuilders.authorization(_endpoint				= discover!!.authEndpoint,
												_scopes					= discover!!.scopes,
												_clientInfo				= clientInfo!!,
												_flowMode				= _flowMode,
												_walletGetMode 			= _walletGetMode,
												_walletFamily			= _walletFamily,
												_walletAddress			= _walletAddress,
												_verificationLevel		= _verificationLevel,
												_identityProvider		= _identityProvider)

			_completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												null,
												uri.toString())
		}
	}
	//==================================================================================================
	//	accessToken
	//--------------------------------------------------------------------------------------------------
	private fun accessToken(completeListener : IAccessTokenListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
											   null,
											   null,
											   null)
			return
		}

		if(accessToken == null
		   || TokenValidator.isExpired(accessToken!!))
		{
			accessTokenRefresh(completeListener)
		}
		else
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
											   null,
											   accessToken,
											   refreshToken)
		}
	}
	//==================================================================================================
	//	accessTokenRefresh
	//--------------------------------------------------------------------------------------------------
	private fun accessTokenRefresh(completeListener : IAccessTokenListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
											   null,
											   null,
											   null)
			return
		}

		if(refreshToken == null
		   || TokenValidator.isExpired(refreshToken!!))
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED,
											   null,
											   null,
											   null)
		}
		else
		{
			val requestParams = Utils.accessTokenRefreshParametersPrepare(clientInfo!!,
																		  discover!!,
																		  refreshToken!!)
			transport.requestPost(Uri.parse(discover!!.tokenEndpoint),
								  requestParams,
								  null,
								  null,
								  object : HTTPTransport.ITransportRequestResult
							{
								override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
															   _requestResultCode	: Int,
															   _requestAnswerBody	: String?)
								{
									when(_result)
									{
										HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION ->
										{
											completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_CONNECTION,
																			   null,
																			   null,
																			   null)
										}
										HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER     ->
										{
											completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																			   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody),
																			   null,
																			   null)
										}
										HTTPTransport.TransportRequestResult.SUCCESS               ->
										{
											if(_requestAnswerBody != null)
											{
												try
												{
													jsonParser.decodeFromString<TokenInfoJson>(_requestAnswerBody).also()
													{
														accessToken		= it.accessTokenRaw
														refreshToken	= it.refreshTokenRaw
													}

													if(accessToken != null && refreshToken != null)
													{
														completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																						   null,
																						   accessToken,
																						   refreshToken)
													}
													else
													{
														completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																						   "Service answer not valid. Answer: [$_requestAnswerBody]",
																						   null,
																						   null)
													}
												}
												catch(_exception : Exception)
												{
													Log.e(TAG, "[TokenRefresh] token info is invalid")

													completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																					   "Service answer not valid. Answer: [$_requestAnswerBody]",
																					   null,
																					   null)
												}
											}
											else
											{
												completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																				   "Service answer not valid. Answer is empty",
																				   null,
																				   null)
											}
										}
									}
								}
							})
		}
	}
	//==================================================================================================
	//	signOut
	//--------------------------------------------------------------------------------------------------
	override fun signOut(completeListener : IHyperIdSDK.IRequestResultListener)
	{
		if(providerUri == null
		   || clientInfo == null
		   || !clientInfo!!.isValid()
		   || discover == null
		   || !discover!!.isValid())
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
											   null)
			return
		}

		if(refreshToken == null || TokenValidator.isExpired(refreshToken!!))
		{
			accessToken		= null
			refreshToken	= null

			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
											   null)
		}

		transport.requestPost(	Uri.parse(discover!!.endSessionEndpoint),
								Utils.logoutParametersPrepare(clientInfo!!,
															  discover!!,
															  refreshToken!!),
								null,
								null,
								object : HTTPTransport.ITransportRequestResult
								{
									override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
																   _requestResultCode	: Int,
																   _requestAnswerBody	: String?)
									{
										when(_result)
										{
											HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION	->
											{
												completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_CONNECTION,
																				   null)
											}
											HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER		->
											{
												completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
																				   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody))
											}
											HTTPTransport.TransportRequestResult.SUCCESS				->
											{
												accessToken		= null
												refreshToken	= null

											completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
																			   null)
										}
									}
								}
							})
	}
	//==================================================================================================
	//	UserInfo
	//--------------------------------------------------------------------------------------------------
	override fun userInfo(completeListener : IHyperIDSDKAuth.IUserInfoGetListener)
	{
		if(accessToken == null)
		{
			completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED,
											   null,
											   null)
		}
		else
		{
			val tokenParts = accessToken!!.split('.')
			if(tokenParts.size != 3)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED,
												   null,
												   null)
			}
			else
			{
				try
				{
					val tokenPayload = String(Base64.decode(tokenParts[1], Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE), Charsets.UTF_8)
					val userInfo = jsonParser.decodeFromString<UserInfoJson>(tokenPayload).toUserInfo()
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   userInfo)
				}
				catch(_exception : Exception)
				{
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED,
													   null,
													   null)
				}
			}
		}
	}
	override fun restApiRequestPost(urlPath			: String,
									queryParameters	: List<Pair<String, String>>,
									callback		: IRestApiRequestResult)
	{
		val accessTokenListener = object : IAccessTokenListener
						{
							//==================================================================================================
							//	onRequestComplete
							//--------------------------------------------------------------------------------------------------
							override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
														   _errorDesc		: String?,
														   _accessToken		: String?,
														   _refreshToken	: String?)
							{
								if(_result == IHyperIdSDK.RequestResult.SUCCESS)
								{
									val uri = Uri.parse(discover!!.restApiTokenEndpoint)

									transport.requestPost(	Uri.withAppendedPath(uri, urlPath),
															queryParameters,
															null,
															_accessToken!!,
															object : HTTPTransport.ITransportRequestResult
															{
																override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
																							   _requestResultCode	: Int,
																							   _requestAnswerBody	: String?)
																{
																	when(_result)
																	{
																		HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION	->
																		{
																			callback.OnRequestComplete(RestApiRequestResult.FAIL_CONNECTION,
																									   null,
																									   null)
																		}
																		HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER		->
																		{
																			callback.OnRequestComplete(RestApiRequestResult.FAIL_SERVICE,
																									   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody),
																									   null)
																		}
																		HTTPTransport.TransportRequestResult.SUCCESS				->
																		{
																			callback.OnRequestComplete(RestApiRequestResult.SUCCESS,
																									   null,
																									   _requestAnswerBody)
																		}
																	}
															  }
														  })
								}
								else
								{
									callback.OnRequestComplete(RestApiRequestResult.FAIL_AUTHORIZATION_REQUIRED,
															   null,
															   null)
								}
							}
						}
		accessToken(accessTokenListener)
	}

	override fun restApiRequestPost(urlPath			: String,
									jsonContent		: String,
									callback		: IRestApiRequestResult)
	{
		val accessTokenListener = object : IAccessTokenListener
		{
			//==================================================================================================
			//	onRequestComplete
			//--------------------------------------------------------------------------------------------------
			override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
										   _errorDesc		: String?,
										   _accessToken		: String?,
										   _refreshToken	: String?)
			{
				if(_result == IHyperIdSDK.RequestResult.SUCCESS)
				{
					val uri = Uri.parse(discover!!.restApiTokenEndpoint)
					transport.requestPost(	Uri.withAppendedPath(uri, urlPath),
											emptyList(),
											jsonContent,
											_accessToken!!,
											object : HTTPTransport.ITransportRequestResult
											{
												override fun OnRequestComplete(_result				: HTTPTransport.TransportRequestResult,
																			   _requestResultCode	: Int,
																			   _requestAnswerBody	: String?)
												{
													when(_result)
													{
														HTTPTransport.TransportRequestResult.ERROR_FAIL_CONNECTION	->
														{
															callback.OnRequestComplete(RestApiRequestResult.FAIL_CONNECTION,
																					   null,
																					   null)
														}
														HTTPTransport.TransportRequestResult.ERROR_FAIL_SERVER		->
														{
															callback.OnRequestComplete(RestApiRequestResult.FAIL_SERVICE,
																					   HelpersJson.errorExtract(jsonParser, _requestResultCode, _requestAnswerBody),
																					   null)
														}
														HTTPTransport.TransportRequestResult.SUCCESS				->
														{
															callback.OnRequestComplete(RestApiRequestResult.SUCCESS,
																					   null,
																					   _requestAnswerBody)
														}
													}
												}
											})
				}
				else
				{
					callback.OnRequestComplete(RestApiRequestResult.FAIL_AUTHORIZATION_REQUIRED,
											   null,
											   null)
				}
			}
		}
		accessToken(accessTokenListener)
	}

	//==================================================================================================
	//	getters
	//--------------------------------------------------------------------------------------------------
	override fun authRestoreInfo()		: String?				= refreshToken
	override fun identityProviders()	: List<String>			= discover?.identityProviders	?: emptyList()


	//user provided
	private var providerUri			: Uri?				= null
	private var clientInfo			: ClientInfo?		= null

	//internal fields
	private var discover			: Discover?			= null
	private var accessToken			: String?			= null
	private var refreshToken		: String?			= null
	private val transport								= HTTPTransport()
	private val jsonParser								= Json { ignoreUnknownKeys = true }

	companion object
	{
		@Suppress("unused")
		private const val TAG		= "eHyperIDSDKImpl"
	}
}
