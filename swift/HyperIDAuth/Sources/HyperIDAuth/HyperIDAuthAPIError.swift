import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: HyperIDAPIAuthError
//--------------------------------------------------------------------------------------------------
public enum HyperIDAuthAPIError : HyperIDErrorProtocol {
	case invalidClientInfo(description : String = "")
	
	case unknownIdentityProvider(provider : String)
	case unknownWalletFamily
	case assertionTokenSignError(description : String)
	
	case authorizationInvalidRedirectURLError(description : String)
	case transactionDataAndValueIsEmpty
	case transactionInvalidParameters(description : String)
	case transactionRejectedByUser(description : String)
	
	case tokenExchangeInvalidGrant(description : String)
	//==================================================================================================
	//	errorDescription
	//--------------------------------------------------------------------------------------------------
	public var errorDescription: String? {
		switch self {
		case .invalidClientInfo(let msg):									"Invalid ClientInfo.\(msg.isEmpty ? "" : "Details \(msg)")"
		case .unknownIdentityProvider:										"Unknown indentity provider. Please use providers presented OpenIDConfiguration"
		case .unknownWalletFamily:											"Unknown wallet family. Please use default or families presented OpenIDConfiguration"
		case .assertionTokenSignError(let msg):								"Assertion token sign error: \(msg)"
		case .tokenExchangeInvalidGrant(let msg):							"Token exchange error: invalid grant. Details: \(msg)"
		case .authorizationInvalidRedirectURLError(let msg):				"Token exchange error: invalid redirect url. Details: \(msg)"
		case .transactionDataAndValueIsEmpty:								"Transaction data and value is empty"
		case .transactionInvalidParameters(description: let msg):			"Transaction invalid parameters error. Details: \(msg)"
		case .transactionRejectedByUser(description: let msg):				"Transaction rejected by user. Details: \(msg)"
		}
	}
}
