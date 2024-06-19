package ai.hyper_id.sdk.internal.auth.types

import com.auth0.jwt.JWT
import java.util.*

//**************************************************************************************************
//	TokenValidator
//--------------------------------------------------------------------------------------------------
internal object TokenValidator
{
	//==================================================================================================
	//	isExpired
	//--------------------------------------------------------------------------------------------------
	internal fun isExpired(_token : String)	: Boolean
	{
		return	try
				{
					JWT().decodeJwt(_token).expiresAt.before(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
				}
				catch(_exception : Exception)
				{
					true
				}
	}
}
