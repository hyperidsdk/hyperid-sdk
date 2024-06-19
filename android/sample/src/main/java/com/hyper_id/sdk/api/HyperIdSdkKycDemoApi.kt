package com.hyper_id.sdk.api

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import android.util.Log

class HyperIdSdkKycDemoApi(private val sdkKyc : IHyperIDSDKKYC)
{
	fun userStatusGet(_verificationLevel	: KycVerificationLevel)
	{
		val completeListener = object : IHyperIDSDKKYC.IUserStatusGetCompleteListener
				{
					override fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
												   errorDesc	: String?,
												   response		: IHyperIDSDKKYC.UserStatus?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							response?.also()
							{
								Log.d(TAG, "[userStatusGet] result/${response.result}")
								Log.d(TAG, "[userStatusGet] verificationLevelRaw/${response.verificationLevel}")
								Log.d(TAG, "[userStatusGet] userStatus/${response.userStatus}")
								Log.d(TAG, "[userStatusGet] kycId/${response.kycId}")
								Log.d(TAG, "[userStatusGet] firstName/${response.firstName}")
								Log.d(TAG, "[userStatusGet] lastName/${response.lastName}")
								Log.d(TAG, "[userStatusGet] birthday/${response.birthday}")
								Log.d(TAG, "[userStatusGet] countryA2/${response.countryA2}")
								Log.d(TAG, "[userStatusGet] countryA3/${response.countryA3}")
								Log.d(TAG, "[userStatusGet] providedCountryA2/${response.providedCountryA2}")
								Log.d(TAG, "[userStatusGet] providedCountryA3/${response.providedCountryA3}")
								Log.d(TAG, "[userStatusGet] addressCountryA2/${response.addressCountryA2}")
								Log.d(TAG, "[userStatusGet] addressCountryA3/${response.addressCountryA3}")
								Log.d(TAG, "[userStatusGet] phoneNumberCountryA2/${response.phoneNumberCountryA2}")
								Log.d(TAG, "[userStatusGet] phoneNumberCountryA3/${response.phoneNumberCountryA3}")
								Log.d(TAG, "[userStatusGet] phoneNumberCountryCode/${response.phoneNumberCountryCode}")
								Log.d(TAG, "[userStatusGet] ipCountriesA2/${response.ipCountriesA2}")
								Log.d(TAG, "[userStatusGet] ipCountriesA3/${response.ipCountriesA3}")
								Log.d(TAG, "[userStatusGet] moderationComment/${response.moderationComment}")
								Log.d(TAG, "[userStatusGet] rejectReasons/${response.rejectReasons}")
								Log.d(TAG, "[userStatusGet] supportLink/${response.supportLink}")
								Log.d(TAG, "[userStatusGet] createDt/${response.createDt}")
								Log.d(TAG, "[userStatusGet] reviewCreateDt/${response.reviewCreateDt}")
								Log.d(TAG, "[userStatusGet] reviewCompleteDt/${response.reviewCompleteDt}")
								Log.d(TAG, "[userStatusGet] expirationDt/${response.expirationDt}")
							}
						}
						else
						{
							Log.d(TAG, "[userStatusGet] request failed with result/$result($errorDesc)")
						}
					}
				}

		sdkKyc.getUserStatus(_verificationLevel,
							 completeListener)
	}

	fun userStatusTopLevelGet()
	{
		val completeListener = object : IHyperIDSDKKYC.IUserStatusTopLevelGetCompleteListener
		{
			override fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
										   errorDesc	: String?,
										   response	: IHyperIDSDKKYC.UserStatusTopLevel?)
			{
				if(result == IHyperIdSDK.RequestResult.SUCCESS)
				{
					response?.also()
					{
						Log.d(TAG, "[userStatusGet] result/${response.result}")
						Log.d(TAG, "[userStatusGet] verificationLevel/${response.verificationLevel}")
						Log.d(TAG, "[userStatusGet] userStatus/${response.userStatus}")
						Log.d(TAG, "[userStatusGet] createDt/${response.createDt}")
						Log.d(TAG, "[userStatusGet] reviewCreateDt/${response.reviewCreateDt}")
						Log.d(TAG, "[userStatusGet] reviewCompleteDt/${response.reviewCompleteDt}")
					}
				}
				else
				{
					Log.d(TAG, "[userStatusGet] request failed with result/$result($errorDesc)")
				}
			}
		}

		sdkKyc.getUserStatusTopLevel(completeListener)
	}

	companion object
	{
		private const val TAG	= "HyperIdSdkKycDemoApi"
	}
}
