package com.hyper_id.sdk.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.hyper_id.sdk.DemoApplication
import com.hyper_id.sdk.api.HyperIdSdkDemoApi
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

//**************************************************************************************************
//	eViewModel
//--------------------------------------------------------------------------------------------------
class eViewModel(val authApi	: HyperIdSdkDemoApi) : ViewModel()
{
	override fun onCleared()
	{
		authApi.done()

		super.onCleared()
	}
	fun test()
	{
		val text = "eyJhbGciOiJSUzI1NiJ9.eyJhenAiOiJhbmRyb2lkLXNkay10ZXN0LXJzYSIsInN1YiI6ImFuZHJvaWQtc2RrLXRlc3QtcnNhIiwidHlwIjoiUmVmcmVzaCIsImp0aSI6ImZkOThiZmZiLWUxNTgtNDk3Zi05N2Y0LThkOWVjOTQzNTg4YiIsImlhdCI6IjE2OTg3NTcwMTMiLCJleHAiOiIxNzAyMzU3MDEzIiwiaXNzIjoiYW5kcm9pZC1zZGstdGVzdC1yc2EiLCJhdWQiOiJodHRwczovL2xvZ2luLXN0YWdlLmh5cGVyc2VjdXJlaWQuY29tL2F1dGgvcmVhbG1zL0h5cGVySUQifQ"
		val header = "{\"alg\":\"RS256\"}"
		val payload = "{\"azp\":\"android-sdk-test-rsa\",\"sub\":\"android-sdk-test-rsa\",\"typ\":\"Refresh\",\"jti\":\"fd98bffb-e158-497f-97f4-8d9ec943588b\",\"iat\":\"1698757013\",\"exp\":\"1702357013\",\"iss\":\"android-sdk-test-rsa\",\"aud\":\"https://login-stage.hypersecureid.com/auth/realms/HyperID\"}"
		try
		{
			val fileFolder				= DemoApplication.applicationInstance.getExternalFilesDir("keys")
			val certFile				= File(fileFolder, "android_sdk_test_rsa.p12")

			val ks = KeyStore.getInstance("pkcs12")
			ks.load(certFile.inputStream(), "111111".toCharArray())
			ks.aliases().iterator().forEach()
			{ _keyAlias ->
				Log.d(TAG, _keyAlias)

				val cert = ks.getCertificate(_keyAlias)
				if(cert is X509Certificate)
				{
					val x509Cert	= cert
					val publicKey	= x509Cert.publicKey as RSAPublicKey

					Log.d(TAG, "format = ${publicKey.format}")
					Log.d(TAG, "getAlgorithm = ${publicKey.algorithm}")

					val privateKey	= ks.getKey(_keyAlias, "111111".toCharArray()) as RSAPrivateKey
					val algorithm = Algorithm.RSA256(publicKey, privateKey)

					val token = JWT.create().withHeader(header).withPayload(payload).sign(algorithm)

					Log.d(TAG, token)
				}
			}





//			RSACipherPrepare.rsaCipherPrepareFromP12()?.also()
//			{
//				if(text.length > it.blockSize)
//				{
//					var offset = 0
//					do
//					{
//						val block : String =if(offset + it.blockSize > (text.length - 1))
//											{
//												text.substring(offset)
//											}
//											else
//											{
//												text.substring(offset, offset + it.blockSize)
//											}
//						offset += it.blockSize
//
//						result.plus(String(it.update(block.toByteArray()), Charsets.UTF_8))
//
//					} while(offset < text.length)
//
//					Log.d(TAG, result)
//				}
//			}
		}
		catch(_exception : Exception)
		{
			Log.d(TAG, _exception.localizedMessage)
		}
	}

	companion object
	{
		val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory
				{
					@Suppress("UNCHECKED_CAST")
					override fun <T : ViewModel> create(modelClass	: Class<T>,
														extras		: CreationExtras)		: T
					{
						val application = checkNotNull(extras[APPLICATION_KEY])

						return eViewModel((application as DemoApplication).hyperIdSDKDemo) as T
					}
				}

		@Suppress("unused")
		private const val TAG = "eViewModel"
	}
}
