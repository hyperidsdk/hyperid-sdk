import Foundation

//**************************************************************************************************
//	MARK: HyperIDErrorProtocol
//--------------------------------------------------------------------------------------------------
public protocol HyperIDErrorProtocol : Error, LocalizedError {}

//**************************************************************************************************
//	MARK: HyperIDAPIBaseError
//--------------------------------------------------------------------------------------------------
public enum HyperIDBaseAPIError : HyperIDErrorProtocol {
	case invalidProviderInfo
	case invalidKYCVerificationLevel
	case invalidAccessToken
	case serverMaintenance
	case networkingError(description: String)
	//==================================================================================================
	//	errorDescription
	//--------------------------------------------------------------------------------------------------
	public var errorDescription: String? {
		switch self {
		case .invalidProviderInfo:						"Invalid provider info"
		case .invalidKYCVerificationLevel:				"Unknown KYC verification level"
		case .invalidAccessToken:						"HyperID access token invalid"
		case .serverMaintenance:						"HyperID in maintenance. Please try again later"
		case .networkingError(description: let desc):	"Unknown request networking error: \(desc)"
		}
	}
}
