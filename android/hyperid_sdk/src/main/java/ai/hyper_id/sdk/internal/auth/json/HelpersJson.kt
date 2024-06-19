package ai.hyper_id.sdk.internal.auth.json

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object HelpersJson
{
	//==================================================================================================
	//	errorExtract
	//--------------------------------------------------------------------------------------------------
	@JvmStatic
	fun errorExtract(jsonParser			: Json,
					 requestResultCode	: Int,
					 requestAnswerBody	: String?)	: String
	{
		if(requestAnswerBody?.isNotBlank() == true)
		{
			try
			{
				return "[$requestResultCode] - ${jsonParser.decodeFromString<ErrorJson>(requestAnswerBody)}"
			}
			catch(_exception : Exception)
			{
				Log.d("[HelpersJson]", "[ErrorExtract] failed: ${_exception.localizedMessage}")
			}
		}
		return "[$requestResultCode] Unknown error"
	}

	//**************************************************************************************************
	//	eErrorJson
	//--------------------------------------------------------------------------------------------------
	@Serializable
	private data class	ErrorJson
						(
								@SerialName("error")
								val error					: String?,

								@SerialName("error_description")
								val errorDesc				: String?
						)
	{
		//==================================================================================================
		//	toString
		//--------------------------------------------------------------------------------------------------
		override fun toString() : String
		{
			return	if(error?.isNotBlank() == true && errorDesc?.isNotBlank() == true)
			{
				"$error : $errorDesc"
			}
			else if(error.isNullOrBlank() && errorDesc?.isNotBlank() == true)
			{
				errorDesc
			}
			else if(errorDesc.isNullOrBlank() && error?.isNotBlank() == true)
			{
				error
			}
			else
			{
				"Unknown error"
			}
		}
	}
}
