package com.hyper_id.sdk.api

import ai.hyper_id.sdk.HyperIdSDKInstance
import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.IHyperIDSDKAuth
import ai.hyper_id.sdk.api.auth.model.AuthorizationMethod
import ai.hyper_id.sdk.api.auth.model.ClientInfo
import ai.hyper_id.sdk.api.auth.model.HyperIdProvider
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.auth.model.UserInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HyperIdSdkDemoApi
{
	enum class SdkAuthState
	{
		CREATED,
		INITIALISED,
		AUTHORIZING,
		AUTHORIZED,
	}

	//==================================================================================================
	//	ConnectionTypeUpdate
	//--------------------------------------------------------------------------------------------------
	fun ConnectionTypeUpdate(_authMethod : AuthorizationMethod)
	{
		connectionType_.value	= _authMethod
		sdkAuthState_.value		= SdkAuthState.CREATED

		clientInfo =	when(_authMethod)
						{
							AuthorizationMethod.CLIENT_SECRET       -> clientInfo.copy(clientId				= "android-sdk-test",				//access token 5 min live
																					   clientSecret			= "3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L",
																					   privateRSAKey		= null,
																					   authorizationMethod	= AuthorizationMethod.CLIENT_SECRET)
							AuthorizationMethod.CLIENT_SECRET_HS256 -> clientInfo.copy(clientId				= "android-sdk-test-hs",
																					   clientSecret			= "c9prKcovIJdEzofVe2tNgZlwW3rSDEdF",
																					   privateRSAKey		= null,
																					   authorizationMethod	= AuthorizationMethod.CLIENT_SECRET_HS256)
							AuthorizationMethod.CLIENT_RS256        -> clientInfo.copy(clientId				= "android-sdk-test-rsa",
																					   clientSecret			= null,
																					   // or provide another method to init cipher with private key
																					   privateRSAKey		= RSAKeyPrepare.PrivateKeyPrepareFromP12(),
																					   authorizationMethod	= AuthorizationMethod.CLIENT_RS256)
						}

		Log.d(TAG, "[ConnectionTypeUpdate] _connectionType/$_authMethod")
	}
	//==================================================================================================
	//	done
	//--------------------------------------------------------------------------------------------------
	fun done()
	{
		sdk.done()
	}
	//==================================================================================================
	//	sdkInit
	//--------------------------------------------------------------------------------------------------
	fun sdkInit()
	{
		val initCallback =	object : IHyperIdSDK.IRequestResultListener
							{
								override fun onRequestComplete(_result		: IHyperIdSDK.RequestResult,
															   _errorDesc	: String?)
								{
									Log.d(TAG, "[sdkAuthInit]: _result/$_result")
									Log.d(TAG, "[sdkAuthInit]: _serviceError/$_errorDesc")

									if(_result == IHyperIdSDK.RequestResult.SUCCESS)
									{
										sdkAuth						= sdk.getAuth()
										identityProviders_.value	= sdkAuth!!.identityProviders()

										sdkAuthState_.value			= SdkAuthState.INITIALISED
									}
									else
									{
										sdkAuthState_.value			= SdkAuthState.CREATED
									}
								}
							}

		sdk.init(HyperIdProvider.SANDBOX,
				 clientInfo,
				 completeListener = initCallback)
	}
	//==================================================================================================
	//	sdkAuthSignIn
	//--------------------------------------------------------------------------------------------------
	fun sdkAuthSignIn()	{ sdkAuth?.startSignInWeb2(completeListener = authCallback) }
	//==================================================================================================
	//	sdkAuthSignToSignIn
	//--------------------------------------------------------------------------------------------------
	fun sdkAuthSignToSignIn() { sdkAuth?.startSignInWeb3(completeListener = authCallback) }
	//==================================================================================================
	//	sdkAuthWalletGet
	//--------------------------------------------------------------------------------------------------
	fun sdkAuthWalletGet() { sdkAuth?.startSignInWalletGet(completeListener = authCallback) }
	//==================================================================================================
	//	sdkAuthGuestUpgrade
	//--------------------------------------------------------------------------------------------------
	fun sdkAuthGuestUpgrade() { sdkAuth?.startSignInGuestUpgrade(authCallback) }
	//==================================================================================================
	//	sdkAuthIdentityProvider
	//--------------------------------------------------------------------------------------------------
	fun sdkAuthIdentityProvider()
	{
		if(identityProvider != null)
		{
			sdkAuth?.startSignInIdentityProvider(identityProvider!!,
												 KycVerificationLevel.BASIC,
												 authCallback)
		}
	}
	fun sdkAuthTransaction()
	{
		isWaitingTransactionResult = true
		/*sdkAuth?.startSignInWithTransaction(/*from				= "0x93753983FF4508b75D2c4b92Ee6ceF49bE92b6E4",*/
											to					= "0x1C0287891dDf14a037E987E7323287f6Bc2bc974",
											chain				= "11155111",
											data				= "0x70a0823100000000000000000000000093753983ff4508b75d2c4b92ee6cef49be92b6e4",
											value				= "0",
											completeListener	= authCallback)*/
		sdkAuth?.startSignInWithTransaction(from				= "0x43D192d3eC9CaEFbc92385bED8508d87E566595f",
											to					= "0x1C0287891dDf14a037E987E7323287f6Bc2bc974",
											chain				= "11155111",
											data				= "0x70a0823100000000000000000000000043d192d3ec9caefbc92385bed8508d87e566595f",
											/*value				= "0.001",*/
											completeListener	= authCallback)
	}
	//==================================================================================================
	//	authCompleteWithRedirect
	//--------------------------------------------------------------------------------------------------
	fun authCompleteWithRedirect(_url : String)
	{
		Log.d(TAG, "[authCompleteWithRedirect] _url/$_url")

		if(isWaitingTransactionResult)
		{
			isWaitingTransactionResult = false
			val completeListener =	object : IHyperIDSDKAuth.IAuthorizationWithTransactionCompleteListener
									{
										override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
																	   _errorDesc		: String?,
																	   _transactionHash	: String?)
										{
											sdkAuthState_.value =	if(_result == IHyperIdSDK.RequestResult.SUCCESS)
																	{
																		Log.d(TAG, "[authCompleteWithRedirect][onRequestComplete] _transactionHash/$_transactionHash")

																		SdkAuthState.AUTHORIZED
																	}
																	else
																	{
																		SdkAuthState.INITIALISED
																	}
										}
									}
			sdkAuth?.completeSignInWithTransaction(_url, completeListener)
		}
		else
		{
			val completeListener =	object : IHyperIDSDKAuth.IAuthorizationCompleteListener
									{
										override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
																	   _errorDesc		: String?,
																	   _authRestoreInfo	: String?)
										{
											sdkAuthState_.value =	if(_result == IHyperIdSDK.RequestResult.SUCCESS)
											{
												SdkAuthState.AUTHORIZED
											}
											else
											{
												SdkAuthState.INITIALISED
											}
										}
									}
			sdkAuth?.completeSignIn(_url, completeListener)
		}
	}
	//==================================================================================================
	//	userInfo
	//--------------------------------------------------------------------------------------------------
	fun userInfo()
	{
		val completeListener = object : IHyperIDSDKAuth.IUserInfoGetListener
				{
					override fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
												   _serviceError	: String?,
												   _userInfo		: UserInfo?)
					{
						if(_result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							if(_userInfo != null)
							{
								Log.d(TAG, "[userInfo] userId/${_userInfo.userId}")
								Log.d(TAG, "[userInfo] isGuest/${_userInfo.isGuest}")
								//etc....
							}
						}
						else
						{
							Log.d(TAG, "[logout] failed with result/$_result($_serviceError) ")
						}
					}
				}
		sdkAuth?.userInfo(completeListener)
	}
	//==================================================================================================
	//	logout
	//--------------------------------------------------------------------------------------------------
	fun logout()
	{
		val completeListener = object : IHyperIdSDK.IRequestResultListener
					{
						override fun onRequestComplete(_result		: IHyperIdSDK.RequestResult,
													   _errorDesc	: String?)
						{
							Log.d(TAG, "[logout] complete with result/$_result($_errorDesc) ")

							if(_result == IHyperIdSDK.RequestResult.SUCCESS)
							{
								sdkAuthState_.value = SdkAuthState.INITIALISED
							}
							else
							{
								Log.d(TAG, "[logout] failed with result/$_result($_errorDesc) ")
							}
						}
					}
		sdkAuth?.signOut(completeListener)
	}

	private val authCallback =	object : IHyperIDSDKAuth.IAuthorizationStartResultListener
								{
									override fun onRequestComplete(_result		: IHyperIdSDK.RequestResult,
																   _errorDesc	: String?,
																   _redirectUrl	: String?)
									{
										Log.d(TAG, "[sdkAuthSignIn] _result/$_result")
										Log.d(TAG, "[sdkAuthSignIn] _uri/$_redirectUrl")

										authUrl				=	_redirectUrl
										sdkAuthState_.value =	if(_result == IHyperIdSDK.RequestResult.SUCCESS)
																{
																	SdkAuthState.AUTHORIZING
																}
																else
																{
																	SdkAuthState.INITIALISED
																}
									}
								}
//	private var clientInfo						= ClientInfo(clientId				= "android-sdk-test",
//															 clientSecret			= "3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L",
//															 redirectUri			= "ai.hypersphere.hyperid://localhost:4200/auth/hyper-id/callback",
//															 privateRSAKey			= null,
//															 authorizationMethod	= AuthorizationMethod.CLIENT_SECRET)
	private var clientInfo						= ClientInfo(clientId				= "android-sdk-test-hs",
															 clientSecret			= "c9prKcovIJdEzofVe2tNgZlwW3rSDEdF",
															 redirectUri			= "ai.hypersphere.hyperid://localhost:4200/auth/hyper-id/callback",
															 privateRSAKey			= null,
															 authorizationMethod	= AuthorizationMethod.CLIENT_SECRET_HS256)

	val sdk															= HyperIdSDKInstance()
	private var sdkAuth 				: IHyperIDSDKAuth?			= null
	var authUrl 						: String?					= null
		private set
	val kycDemoApi 						: HyperIdSdkKycDemoApi		= HyperIdSdkKycDemoApi(sdk.getKYC())
	val mfaDemoApi 						: HyperIdSdkMFADemoApi		= HyperIdSdkMFADemoApi(sdk.getMFA())
	val storageDemoApi					: HyperIdSdkStorageDemoApi	= HyperIdSdkStorageDemoApi(sdk.getStorage())

	var identityProvider				: String?					= null
		set(_value)
		{
			field					= _value
			identityProvider_.value	= field
		}
	private var isWaitingTransactionResult							= false

	private val sdkAuthState_			= MutableStateFlow(SdkAuthState.CREATED)
	val sdkAuthState					= sdkAuthState_.asStateFlow()

	private val connectionType_			= MutableStateFlow(AuthorizationMethod.CLIENT_SECRET)
	val connectionType					= connectionType_.asStateFlow()

	private val identityProviders_		= MutableStateFlow<List<String>>(emptyList())
	val identityProviders				= identityProviders_.asStateFlow()

	private val identityProvider_		= MutableStateFlow(identityProvider)
	val identityProviderStateFlow		= identityProvider_.asStateFlow()

	companion object
	{
		private const val TAG	= "HyperIdSdkAuthDemoApi"
	}
}
