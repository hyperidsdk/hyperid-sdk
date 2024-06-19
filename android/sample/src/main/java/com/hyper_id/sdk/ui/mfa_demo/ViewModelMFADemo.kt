package com.hyper_id.sdk.ui.mfa_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.hyper_id.sdk.DemoApplication
import com.hyper_id.sdk.api.HyperIdSdkMFADemoApi

class ViewModelMFADemo(val mfaApi : HyperIdSdkMFADemoApi) : ViewModel()
{
	override fun onCleared()
	{
		mfaApi.done()

		super.onCleared()
	}

	companion object
	{
		val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory
		{
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass	: Class<T>,
												extras		: CreationExtras)		: T
			{
				val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

				return  ViewModelMFADemo((application as DemoApplication).hyperIdSDKDemo.mfaDemoApi) as T
			}
		}

		@Suppress("unused")
		private const val TAG	= "ViewModelMFADemo"
	}
}
