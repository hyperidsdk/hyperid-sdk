package com.hyper_id.sdk.api

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.IHyperIDSDKStorage
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteByIdPResult
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteByWalletResult
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByIdPResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByWalletResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByUserId
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HyperIdSdkStorageDemoApi(private val sdkStorage : IHyperIDSDKStorage)
{
	fun testStorageByEmail()
	{
		Log.d(TAG, "Test start")

		MainScope().launch(Dispatchers.IO)
		{
			userDataByEmailKeysGet()
			delay(200L)
			userDataByEmailGet()
			delay(200L)
			userDataByEmailSet()
			delay(200L)
			userDataByEmailGet()
			delay(200L)
			userDataByEmailKeysGet()
			delay(200L)
			userDataByEmailKeysSharedGet()
			delay(200L)
			userDataByEmailDelete()
			delay(200L)
			userDataByEmailKeysGet()
		}
	}
	fun testStorageByUserId()
	{
		Log.d(TAG, "Test start")

		MainScope().launch(Dispatchers.IO)
		{
			userDataByUserIdKeysGet()
			delay(200L)
			userDataByUserIdGet()
			delay(200L)
			userDataByUserIdSet()
			delay(200L)
			userDataByUserIdGet()
			delay(200L)
			userDataByUserIdKeysGet()
			delay(200L)
			userDataByUserIdKeysSharedGet()
			delay(200L)
			userDataByUserIdDelete()
			delay(200L)
			userDataByUserIdKeysGet()
		}
	}
	fun testStorageByWalletAddress()
	{
		Log.d(TAG, "Test start")

		MainScope().launch(Dispatchers.IO)
		{
			userDataByWalletKeysGet()
			delay(200L)
			userDataByWalletGet()
			delay(200L)
			userDataByWalletSet()
			delay(200L)
			userDataByWalletGet()
			delay(200L)
			userDataByWalletKeysGet()
			delay(200L)
			userDataByWalletKeysSharedGet()
			delay(200L)
			userDataByWalletDelete()
			delay(200L)
			userDataByWalletKeysGet()
			delay(200L)
			walletsGet()
		}
	}
	fun testStorageByIdp()
	{
		Log.d(TAG, "Test start")

		MainScope().launch(Dispatchers.IO)
		{
			userDataByIdpKeysGet()
			delay(200L)
			userDataByIdpGet()
			delay(200L)
			userDataByIdpSet()
			delay(200L)
			userDataByIdpGet()
			delay(200L)
			userDataByIdpKeysGet()
			delay(200L)
			userDataByIdpKeysSharedGet()
			delay(200L)
			userDataByIdpDelete()
			delay(200L)
			userDataByIdpGet()
		}
	}

	private fun userDataByEmailSet()
	{
		val completeListener = object : IHyperIDSDKStorageByEmail.IDataSetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: UserDataSetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByEmailSet] complete with result/$storageResult")
						}
						else
						{
							Log.d(TAG, "[userDataByEmailSet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageEmail.dataSet(STORAGE_DEMO_KEY,
							 "STORAGE _DEMO_VALUE",
							 UserDataAccessScope.PUBLIC,
							 completeListener)
	}
	private fun userDataByEmailGet()
	{
		val completeListener = object : IHyperIDSDKStorageByEmail.IDataGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByEmail.DataGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							if(storageResult != null)
							{
								Log.d(TAG, "[userDataByEmailGet] complete with result/${storageResult.result}")
								Log.d(TAG, "[userDataByEmailGet] ${storageResult.key} = ${storageResult.value}")
							}
						}
						else
						{
							Log.d(TAG, "[userDataByEmailGet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageEmail.dataGet(STORAGE_DEMO_KEY, completeListener)
	}
	private fun userDataByEmailKeysGet()
	{
		val completeListener = object : IHyperIDSDKStorageByEmail.IKeysGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByEmail.KeysGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByEmailKeysGet] keys public ${storageResult?.keysPublic?.joinToString { it }}")
							Log.d(TAG, "[userDataByEmailKeysGet] keys private ${storageResult?.keysPrivate?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByEmailKeysGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageEmail.keysGet(completeListener)
	}
	private fun userDataByEmailKeysSharedGet()
	{
		val completeListener = object : IHyperIDSDKStorageByEmail.IKeysSharedGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByEmail.KeysSharedGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByEmailKeysSharedGet] keys public ${storageResult?.keysShared?.joinToString { it }}")
							Log.d(TAG, "[userDataByEmailKeysSharedGet] keys private ${storageResult?.keysShared?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByEmailKeysSharedGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageEmail.keysSharedGet(completeListener)
	}
	private fun userDataByEmailDelete()
	{
		val completeListener = object : IHyperIDSDKStorageByEmail.IDataDeleteResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: UserDataDeleteResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByEmailDelete] complete with storage result $storageResult")
						}
						else
						{
							Log.d(TAG, "[userDataByEmailDelete] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageEmail.dataDelete(listOf(STORAGE_DEMO_KEY), completeListener)
	}

	private fun userDataByUserIdSet()
	{
		val completeListener = object : IHyperIDSDKStorageByUserId.IDataSetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: UserDataSetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByUserSet] complete with result/$storageResult")
						}
						else
						{
							Log.d(TAG, "[userDataByUserSet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageUserId.dataSet(STORAGE_DEMO_KEY,
						   "STORAGE _DEMO_VALUE",
						   UserDataAccessScope.PRIVATE,
						   completeListener)
	}
	private fun userDataByUserIdGet()
	{
		val completeListener = object : IHyperIDSDKStorageByUserId.IDataGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByUserId.DataGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							if(storageResult != null)
							{
								Log.d(TAG, "[userDataByUserIdGet] complete with result/${storageResult.result}")
								Log.d(TAG, "[userDataByUserIdGet] ${storageResult.key} = ${storageResult.value}")
							}
						}
						else
						{
							Log.d(TAG, "[userDataByUserIdGet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageUserId.dataGet(STORAGE_DEMO_KEY, completeListener)
	}
	private fun userDataByUserIdKeysGet()
	{
		val completeListener = object : IHyperIDSDKStorageByUserId.IKeysGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByUserId.KeysGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByUserIdKeysGet] keys public ${storageResult?.keysPublic?.joinToString { it }}")
							Log.d(TAG, "[userDataByUserIdKeysGet] keys private ${storageResult?.keysPrivate?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByUserIdKeysGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageUserId.keysGet(completeListener)
	}
	private fun userDataByUserIdKeysSharedGet()
	{
		val completeListener = object : IHyperIDSDKStorageByUserId.IKeysSharedGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByUserId.KeysSharedGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByUserIdKeysSharedGet] keys public ${storageResult?.keysShared?.joinToString { it }}")
							Log.d(TAG, "[userDataByUserIdKeysSharedGet] keys private ${storageResult?.keysShared?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByUserIdKeysSharedGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageUserId.keysSharedGet(completeListener)
	}
	private fun userDataByUserIdDelete()
	{
		val completeListener = object : IHyperIDSDKStorageByUserId.IDataDeleteResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: UserDataDeleteResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByUserIdDelete] complete with storage result $storageResult")
						}
						else
						{
							Log.d(TAG, "[userDataByUserIdDelete] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageUserId.dataDelete(listOf(STORAGE_DEMO_KEY), completeListener)
	}

	private fun userDataByWalletSet()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IDataSetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: UserDataSetByWalletResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByWalletSet] complete with result/$serviceResult")
						}
						else
						{
							Log.d(TAG, "[userDataByWalletSet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageWallet.dataSet(WALLET_ADDRESS,
							  STORAGE_DEMO_KEY,
							  "STORAGE _DEMO_VALUE",
							  UserDataAccessScope.PRIVATE,
							  completeListener)
	}
	private fun userDataByWalletGet()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IDataGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: IHyperIDSDKStorageByWallet.DataGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							if(serviceResult != null)
							{
								Log.d(TAG, "[userDataByWalletGet] complete with result/${serviceResult.result}")
								Log.d(TAG, "[userDataByWalletGet] ${serviceResult.dataKey} = ${serviceResult.dataValue}")
							}
						}
						else
						{
							Log.d(TAG, "[userDataByWalletGet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageWallet.dataGet(WALLET_ADDRESS,
							  STORAGE_DEMO_KEY,
							  completeListener)
	}
	private fun userDataByWalletKeysGet()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IKeysGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: IHyperIDSDKStorageByWallet.KeysGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByWalletKeysGet] keys public ${serviceResult?.keysPublic?.joinToString { it }}")
							Log.d(TAG, "[userDataByWalletKeysGet] keys private ${serviceResult?.keysPrivate?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByWalletKeysGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageWallet.keysGet(WALLET_ADDRESS,
							  completeListener)
	}
	private fun userDataByWalletKeysSharedGet()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IKeysSharedGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByWallet.KeysSharedGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByWalletKeysSharedGet] keys public ${storageResult?.keysShared?.joinToString { it }}")
							Log.d(TAG, "[userDataByWalletKeysSharedGet] keys private ${storageResult?.keysShared?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByWalletKeysSharedGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageWallet.keysSharedGet(WALLET_ADDRESS,
									completeListener)
	}
	private fun userDataByWalletDelete()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IDataDeleteResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: UserDataDeleteByWalletResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByWalletDelete] complete with storage result $serviceResult")
						}
						else
						{
							Log.d(TAG, "[userDataByWalletDelete] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageWallet.dataDelete(WALLET_ADDRESS,
								 listOf (STORAGE_DEMO_KEY),
								 completeListener)
	}
	private fun walletsGet()
	{
		val completeListener = object : IHyperIDSDKStorageByWallet.IWalletsGetResultListener
		{
			override fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
										   errorDesc	: String?,
										   walletsInfo	: List<IHyperIDSDKStorageByWallet.WalletInfo>)
			{
				if(result == IHyperIdSDK.RequestResult.SUCCESS)
				{
					Log.d(TAG, "[walletsGet] complete with wallets:{ ${walletsInfo.joinToString { "${it.isPublic} ${it.address} ${it.chainId} ${it.family}\n "}} }")
				}
				else
				{
					Log.d(TAG, "[walletsGet] failed with error/${result}($errorDesc)")
				}
			}
		}

		storageWallet.walletsGet(completeListener)
	}

	private fun userDataByIdpSet()
	{
		val completeListener = object : IHyperIDSDKStorageByIdentityProvider.IDataSetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: UserDataSetByIdPResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByIdpSet] complete with result/$serviceResult")
						}
						else
						{
							Log.d(TAG, "[userDataByIdpSet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageIdp.dataSet(IDENTITY_PROVIDER,
						   STORAGE_DEMO_KEY,
						   "STORAGE _DEMO_VALUE",
						   UserDataAccessScope.PRIVATE,
						   completeListener)
	}
	private fun userDataByIdpGet()
	{
		val completeListener = object : IHyperIDSDKStorageByIdentityProvider.IDataGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: IHyperIDSDKStorageByIdentityProvider.DataGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							if(serviceResult != null)
							{
								Log.d(TAG, "[userDataByIdpGet] complete with result/${serviceResult.result}")
								Log.d(TAG, "[userDataByIdpGet] ${serviceResult.dataKey} = ${serviceResult.dataValue}")
							}
						}
						else
						{
							Log.d(TAG, "[userDataByIdpGet] failed with error/${result}($errorDesc)")
						}
					}
				}
		storageIdp.dataGet(IDENTITY_PROVIDER,
						   STORAGE_DEMO_KEY,
						   completeListener)
	}
	private fun userDataByIdpKeysGet()
	{
		val completeListener = object : IHyperIDSDKStorageByIdentityProvider.IKeysGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: IHyperIDSDKStorageByIdentityProvider.KeysGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByIdpKeysGet] keys public ${serviceResult?.keysPublic?.joinToString { it }}")
							Log.d(TAG, "[userDataByIdpKeysGet] keys private ${serviceResult?.keysPrivate?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByIdpKeysGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageIdp.keysGet(IDENTITY_PROVIDER,
						   completeListener)
	}
	private fun userDataByIdpKeysSharedGet()
	{
		val completeListener = object : IHyperIDSDKStorageByIdentityProvider.IKeysSharedGetResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   storageResult	: IHyperIDSDKStorageByIdentityProvider.KeysSharedGetResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByIdpKeysSharedGet] keys public ${storageResult?.keysShared?.joinToString { it }}")
							Log.d(TAG, "[userDataByIdpKeysSharedGet] keys private ${storageResult?.keysShared?.joinToString { it }}")
						}
						else
						{
							Log.d(TAG, "[userDataByIdpKeysSharedGet] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageIdp.keysSharedGet(IDENTITY_PROVIDER,
								 completeListener)
	}
	private fun userDataByIdpDelete()
	{
		val completeListener = object : IHyperIDSDKStorageByIdentityProvider.IDataDeleteResultListener
				{
					override fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
												   errorDesc		: String?,
												   serviceResult	: UserDataDeleteByIdPResult?)
					{
						if(result == IHyperIdSDK.RequestResult.SUCCESS)
						{
							Log.d(TAG, "[userDataByIdpDelete] complete with storage result $serviceResult")
						}
						else
						{
							Log.d(TAG, "[userDataByIdpDelete] failed with error/${result}($errorDesc)")
						}
					}
				}

		storageIdp.dataDelete(IDENTITY_PROVIDER,
							  listOf (STORAGE_DEMO_KEY),
							  completeListener)
	}

	private val storageEmail	= sdkStorage.StorageByEmail()
	private val storageUserId	= sdkStorage.StorageByUserId()
	private val storageWallet	= sdkStorage.StorageByWallet()
	private val storageIdp		= sdkStorage.StorageByIdentityProvider()

	companion object
	{
		private const val WALLET_ADDRESS		= "0xa0d6051556876Ff0eEE43E20885A632F360f2924"
		private const val IDENTITY_PROVIDER		= "google"
		private const val STORAGE_DEMO_KEY		= "STORAGE_DEMO_KEY"

		private const val TAG					= "HyperIdSdkStorageDemoApi"
	}
}
