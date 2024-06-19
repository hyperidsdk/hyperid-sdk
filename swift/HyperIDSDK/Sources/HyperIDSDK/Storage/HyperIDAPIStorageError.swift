import Foundation

//**************************************************************************************************
//	HyperIDAPIStorageError
//--------------------------------------------------------------------------------------------------
public enum HyperIDStorageAPIError : HyperIDErrorProtocol {
	case keyInvalid
	case keyAccessDenied
	case keysSizeLimitReached
	case walletNotExists
	case identityProviderNotFound
	//==================================================================================================
	//	errorDescription
	//--------------------------------------------------------------------------------------------------
	public var errorDescription: String? {
		switch self {
		case .keyInvalid:				"Key invalid"
		case .keyAccessDenied:			"Key access denied"
		case .keysSizeLimitReached:		"Keys size limit reached"
		case .walletNotExists:			"Wallet not exists"
		case .identityProviderNotFound:	"Identity provider not found. Please check it in provider OpenID configuration"
		}
	}
}
