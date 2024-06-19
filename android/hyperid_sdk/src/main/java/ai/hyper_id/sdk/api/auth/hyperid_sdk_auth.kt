package ai.hyper_id.sdk.api.auth

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.auth.model.UserInfo
import ai.hyper_id.sdk.api.auth.model.WalletFamily
import ai.hyper_id.sdk.api.auth.model.WalletGetMode

//**************************************************************************************************
//	IHyperIDSDKAuth
//--------------------------------------------------------------------------------------------------
interface IHyperIDSDKAuth
{
	interface IAuthorizationStartResultListener	{
		fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
							  _errorDesc		: String?,
							  _redirectUrl		: String?)
	}
	interface IAuthorizationCompleteListener	{
		fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
							  _errorDesc		: String?,
							  _authRestoreInfo	: String?)
	}
	interface IAuthorizationWithTransactionCompleteListener	{
		fun onRequestComplete(_result			: IHyperIdSDK.RequestResult,
							  _errorDesc		: String?,
							  _transactionHash	: String?)
	}
	interface IUserInfoGetListener {
		fun onRequestComplete(_result		: IHyperIdSDK.RequestResult,
							  _serviceError	: String?,
							  _userInfo		: UserInfo?)
	}
	/**
	 * startSignInWeb2
	 *
	 * Provide HyperId authentication
	 *
	 * @param verificationLevel	- Optional. If this parameter is specified - HyperId authentication flow will
	 * 								require user to pass KYC in specified manner
	 * @param completeListener	- Callback with authentication URL
	 * */
	fun startSignInWeb2(verificationLevel					: KycVerificationLevel?	= null,
						completeListener					: IAuthorizationStartResultListener)
	fun startSignInWeb3(walletFamily						: WalletFamily?			= null,
						verificationLevel					: KycVerificationLevel?	= null,
						completeListener					: IAuthorizationStartResultListener)
	fun startSignInWalletGet(walletGetMode					: WalletGetMode = WalletGetMode.FAST,
							 walletFamily					: WalletFamily?		= null,
							 completeListener				: IAuthorizationStartResultListener)
	fun startSignInGuestUpgrade(completeListener			: IAuthorizationStartResultListener)
	fun startSignInIdentityProvider(identityProvider		: String,
									verificationLevel		: KycVerificationLevel?	= null,
									completeListener		: IAuthorizationStartResultListener)
	fun startSignInWithTransaction(from							: String? = null,
								   to							: String,
								   chain						: String,
								   data							: String? = null,
								   value						: String? = null,
								   gas							: String? = null,
								   nonce						: String? = null,
								   completeListener				: IAuthorizationStartResultListener)
	/**
	 * completeSignIn
	 *
	 * @param redirectUrl		- entire url received as redirect from HyperId service as result of authentication
	 * 							process
	 * @param completeListener	- result of request.
	 * */
	fun completeSignIn(redirectUrl		: String,
					   completeListener	: IAuthorizationCompleteListener)
	fun completeSignInWithTransaction(redirectUrl		: String,
									  completeListener	: IAuthorizationWithTransactionCompleteListener)
	/**
	 * signOut
	 *
	 * @param completeListener - result of request
	 */
	fun signOut(completeListener		: IHyperIdSDK.IRequestResultListener)

	/**
	 * @return - list of available identity providers if SDK was initialised successfully, otherwise
	 * 			empty list
	 */
	fun identityProviders()				: List<String>
	/**
	 * @return - string with token to restore SDK state for future launch.
	 * 			Return NULL if SDK not authorized
	 */
	fun authRestoreInfo()				: String?

	/**
	 * @param completeListener - Request result listener
	 */
	fun userInfo(completeListener		: IUserInfoGetListener)
}
