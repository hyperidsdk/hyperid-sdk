package com.hyper_id.sdk.ui.kyc_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.hyper_id.sdk.DemoApplication
import com.hyper_id.sdk.api.HyperIdSdkKycDemoApi

class ViewModelKycDemo(val kycApi : HyperIdSdkKycDemoApi) : ViewModel()
{
	companion object
	{
		val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory
		{
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass	: Class<T>,
												extras		: CreationExtras)		: T
			{
				val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

				return ViewModelKycDemo((application as DemoApplication).hyperIdSDKDemo.kycDemoApi) as T
			}
		}

		@Suppress("unused")
		private const val TAG	= "ViewModelMFADemo"
	}
}
