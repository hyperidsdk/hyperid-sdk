import Foundation
import HyperIDAuth

//**************************************************************************************************
//	MARK: UserKYCStatus
//--------------------------------------------------------------------------------------------------
public enum UserKYCStatus {
	case unsupported(code : Int)
	
	case none
	case pending
	case completeSuccess
	case completeFailRetryable
	case completeFailFinal
	case deleted
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(rawValue : Int) {
		switch rawValue {
		case 0:
			self = .none
		case 1:
			self = .pending
		case 2:
			self = .completeSuccess
		case 3:
			self = .completeFailRetryable
		case 4:
			self = .completeFailFinal
		case 5:
			self = .deleted
		default:
			self = .unsupported(code: rawValue)
		}
	}
}

//**************************************************************************************************
//	MARK: UserKYCStatusInfo
//--------------------------------------------------------------------------------------------------
public struct UserKYCStatusInfo {
	public var	verificationLevel		: KYCVerificationLevel
	public var	userStatus				: UserKYCStatus
	public var	kycId					: String?		= nil
	public var	firstName				: String?		= nil
	public var	lastName				: String?		= nil
	public var	birthday				: String?		= nil
	public var	countryA2				: String?		= nil
	public var	countryA3				: String?		= nil
	public var	providedCountryA2		: String?		= nil
	public var	providedCountryA3		: String?		= nil
	public var	addressCountryA2		: String?		= nil
	public var	addressCountryA3		: String?		= nil
	public var	phoneNumberCountryA2	: String?		= nil
	public var	phoneNumberCountryA3	: String?		= nil
	public var	phoneNumberCountryCode	: String?		= nil
	public var	ipCountriesA2			: [String]?		= nil
	public var	ipCountriesA3			: [String]?		= nil
	public var	moderationComment		: String?		= nil
	public var	rejectReasons			: [String]?		= nil
	public var	supportLink				: URL?			= nil
	public var	createDt				: Date
	public var	reviewCreateDt			: Date
	public var	reviewCompleteDt		: Date
	public var	expirationDt			: Date
}

//**************************************************************************************************
//	MARK: UserKYCStatusTopLevelInfo
//--------------------------------------------------------------------------------------------------
public struct UserKYCStatusTopLevelInfo {
	public var	verificationLevel		: KYCVerificationLevel
	public var	userStatus				: UserKYCStatus
	public var	createDt				: Date
	public var	reviewCreateDt			: Date
	public var	reviewCompleteDt		: Date
}

