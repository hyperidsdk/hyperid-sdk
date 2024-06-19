package com.hyper_id.sdk.api

import android.util.Log
import com.hyper_id.sdk.DemoApplication
import java.io.File
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey

object RSAKeyPrepare
{
	fun PrivateKeyPrepareFromP12() : RSAPrivateKey?
	{
		var privateKey		: RSAPrivateKey?	= null

		//provide real path to p12 file
		val fileFolder				= DemoApplication.applicationInstance.getExternalFilesDir("keys")
		val certFile				= File(fileFolder,
										   "android_sdk_test_rsa.p12")

		try
		{
			val ks = KeyStore.getInstance("pkcs12")
			ks.load(certFile.inputStream(), "111111".toCharArray())
			ks.aliases().iterator().forEach()
			{ _keyAlias ->
				ks.getKey(_keyAlias, "111111".toCharArray())?.also()
				{
					if(it is RSAPrivateKey)
					{
						privateKey = it
					}
				}
			}
		}
		catch(_exception : Exception)
		{
			Log.d(TAG, "fail. ${_exception.localizedMessage}")
		}
		return privateKey
	}

	private const val TAG = "RSACipherPrepare"
}
