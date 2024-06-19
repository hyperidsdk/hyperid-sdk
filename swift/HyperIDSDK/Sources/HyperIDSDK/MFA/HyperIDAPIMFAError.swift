import Foundation

//**************************************************************************************************
//	MARK: HyperIDMFAAPIError
//--------------------------------------------------------------------------------------------------
public enum HyperIDMFAAPIError : HyperIDErrorProtocol {
	case controlCodeInvalidValue
	case hyperIDAuthenticatorNotAttached
	case MFATransactionNotFound
	case MFATransactionAlreadyCompleted
	//==================================================================================================
	//	errorDescription
	//--------------------------------------------------------------------------------------------------
	public var errorDescription: String? {
		switch self {
		case .controlCodeInvalidValue:			"Controll code invalid vaule. Please use codes in range 0-99"
		case .hyperIDAuthenticatorNotAttached:	"Account have no Hyper ID Authenticator attached app."
		case .MFATransactionNotFound:			"MFA transaction not found"
		case .MFATransactionAlreadyCompleted:	"MFA transaction already completed"
		}
	}
}
