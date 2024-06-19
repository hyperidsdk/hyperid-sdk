import Foundation
import HyperIDBase
import HyperIDAuth

//**************************************************************************************************
//	MARK: UserKYCStatusInfoResponse
//--------------------------------------------------------------------------------------------------
struct UserKYCStatusInfoResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw		: Int
	private var	verificationLevelRaw	: Int
	private var	userStatusRaw			: Int
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
	private var	createDtRaw				: Double		= 0
	private var	reviewCreateDtRaw		: Double		= 0
	private var	reviewCompleteDtRaw		: Double		= 0
	private var	expirationDtRaw			: Double		= 0

	var			requestResult			: Result				{ Result(rawValue: requestResultRaw)					}
	var			result					: Validatable			{ requestResult											}
	public var	verificationLevel		: KYCVerificationLevel	{ KYCVerificationLevel(rawValue: verificationLevelRaw)	}
	public var	userStatus				: UserKYCStatus			{ UserKYCStatus(rawValue: userStatusRaw)				}
	public var	createDt				: Date					{ Date(timeIntervalSince1970: createDtRaw)				}
	public var	reviewCreateDt			: Date					{ Date(timeIntervalSince1970: reviewCreateDtRaw)		}
	public var	reviewCompleteDt		: Date					{ Date(timeIntervalSince1970: reviewCompleteDtRaw)		}
	public var	expirationDt			: Date					{ Date(timeIntervalSince1970: expirationDtRaw)			}
	
	//**************************************************************************************************
	//	MARK: UserKYCStatusInfoResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case requestResultRaw		= "result"
		case verificationLevelRaw	= "verification_level"
		case userStatusRaw			= "user_status"
		case kycId					= "kyc_id"
		case firstName				= "first_name"
		case lastName				= "last_name"
		case birthday				= "birthday"
		case countryA2				= "country_a2"
		case countryA3				= "country_a3"
		case providedCountryA2		= "provided_country_a2"
		case providedCountryA3		= "provided_country_a3"
		case addressCountryA2		= "address_country_a2"
		case addressCountryA3		= "address_country_a3"
		case phoneNumberCountryA2	= "phone_number_country_a2"
		case phoneNumberCountryA3	= "phone_number_country_a3"
		case phoneNumberCountryCode	= "phone_number_country_code"
		case ipCountriesA2			= "ip_countries_a2"
		case ipCountriesA3			= "ip_countries_a3"
		case moderationComment		= "moderation_comment"
		case rejectReasons			= "reject_reasons"
		case supportLink			= "support_link"
		case createDtRaw			= "create_dt"
		case reviewCreateDtRaw		= "review_create_dt"
		case reviewCompleteDtRaw	= "review_complete_dt"
		case expirationDtRaw		= "expiration_dt"
	}
	//==================================================================================================
	//	factoryUserKYCStatusInfo
	//--------------------------------------------------------------------------------------------------
	func factoryUserKYCStatusInfo() throws -> UserKYCStatusInfo? {
		try self.validate()
		switch requestResult {
		case .success:
			return UserKYCStatusInfo(verificationLevel: verificationLevel,
									 userStatus: userStatus,
									 kycId: kycId,
									 firstName: firstName,
									 lastName: lastName,
									 birthday: birthday,
									 countryA2: countryA2,
									 countryA3: countryA3,
									 providedCountryA2: providedCountryA2,
									 providedCountryA3: providedCountryA3,
									 addressCountryA2: addressCountryA2,
									 addressCountryA3: addressCountryA3,
									 phoneNumberCountryA2: phoneNumberCountryA2,
									 phoneNumberCountryA3: phoneNumberCountryA3,
									 phoneNumberCountryCode: phoneNumberCountryCode,
									 ipCountriesA2: ipCountriesA2,
									 ipCountriesA3: ipCountriesA3,
									 moderationComment: moderationComment,
									 rejectReasons: rejectReasons,
									 supportLink: supportLink,
									 createDt: createDt,
									 reviewCreateDt: reviewCreateDt,
									 reviewCompleteDt: reviewCompleteDt,
									 expirationDt: expirationDt)
		default:
			return nil
		}
	}
	//**************************************************************************************************
	//	MARK: UserKYCStatusInfoResponse.Result
	//--------------------------------------------------------------------------------------------------
	public enum Result : Validatable {
		case unsupported(code : Int)
		
		case success
		
		case failByTokenInvalid
		case failByTokenExpired
		case failByAccessDenied
		case failByServiceTemporaryUnavailable
		case failByInvalidParameters
		case failByBilling
		case faliByUserNotFound
		case failByUserKYCDeleted
		
		//==================================================================================================
		//	init
		//--------------------------------------------------------------------------------------------------
		init(rawValue : Int) {
			switch rawValue {
			case 0:
				self = .success
			case -1:
				self = .failByTokenInvalid
			case -2:
				self = .failByTokenExpired
			case -3:
				self = .failByAccessDenied
			case -4:
				self = .failByServiceTemporaryUnavailable
			case -5:
				self = .failByInvalidParameters
			case -6:
				self = .failByBilling
			case -7:
				self = .faliByUserNotFound
			case -8:
				self = .failByUserKYCDeleted
			default:
				self = .unsupported(code: rawValue)
			}
		}
		//==================================================================================================
		//	init
		//--------------------------------------------------------------------------------------------------
		public func validate() throws {
			switch self {
			case .unsupported(code: _):
				throw HyperIDBaseAPIError.serverMaintenance
			case .success:
				return
			case .failByTokenInvalid,
				 .failByTokenExpired,
				 .failByAccessDenied:
				throw HyperIDBaseAPIError.invalidAccessToken
			case .failByServiceTemporaryUnavailable,
				 .failByInvalidParameters:
				throw HyperIDBaseAPIError.serverMaintenance
			case .failByBilling,
				 .faliByUserNotFound,
				 .failByUserKYCDeleted:
				return
			}
		}
	}
}
