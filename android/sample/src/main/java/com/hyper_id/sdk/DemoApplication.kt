package com.hyper_id.sdk

import android.app.Application
import com.hyper_id.sdk.api.HyperIdSdkDemoApi

class DemoApplication : Application()
{
	override fun onCreate()
	{
		super.onCreate()

		applicationInstance = this
	}

	val hyperIdSDKDemo		= HyperIdSdkDemoApi()

	companion object
	{
		@JvmStatic
		lateinit var applicationInstance	: Application
	}
}
