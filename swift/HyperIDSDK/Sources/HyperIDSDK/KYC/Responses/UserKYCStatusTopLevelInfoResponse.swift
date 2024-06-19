import Foundation
import HyperIDBase
import HyperIDAuth

//**************************************************************************************************
//	MARK: UserKYCStatusTopLevelInfoResponse
//--------------------------------------------------------------------------------------------------
struct UserKYCStatusTopLevelInfoResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw		: Int
	private var verificationLevelRaw	: Int
	private var userStatusRaw			: Int
	private var createDtRaw				: Double
	private var reviewCreateDtRaw		: Double
	private var reviewCompleteDtRaw		: Double
	
	var			requestResult			: Result				{ Result(rawValue: requestResultRaw)					}
	var			result					: Validatable			{ requestResult											}
	public var	verificationLevel		: KYCVerificationLevel	{ KYCVerificationLevel(rawValue: verificationLevelRaw)	}
	public var	userStatus				: UserKYCStatus			{ UserKYCStatus(rawValue: userStatusRaw)				}
	public var	createDt				: Date					{ Date(timeIntervalSince1970: createDtRaw)				}
	public var	reviewCreateDt			: Date					{ Date(timeIntervalSince1970: reviewCreateDtRaw)		}
	public var	reviewCompleteDt		: Date					{ Date(timeIntervalSince1970: reviewCompleteDtRaw)		}
	
	//**************************************************************************************************
	//	MARK: UserKYCStatusTopLevel.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case requestResultRaw		= "result"
		case verificationLevelRaw	= "verification_level"
		case userStatusRaw			= "user_status"
		case createDtRaw			= "create_dt"
		case reviewCreateDtRaw		= "review_create_dt"
		case reviewCompleteDtRaw	= "review_complete_dt"
	}
	//==================================================================================================
	//	factoryUserKYCTopLevelInfoResponse
	//--------------------------------------------------------------------------------------------------
	func factoryUserKYCTopLevelInfo() throws -> UserKYCStatusTopLevelInfo? {
		try self.validate()
		switch requestResult {
		case .success:
			return UserKYCStatusTopLevelInfo(verificationLevel:	verificationLevel,
											 userStatus:		userStatus,
											 createDt:			createDt,
											 reviewCreateDt:	reviewCreateDt,
											 reviewCompleteDt:	reviewCompleteDt)
		default:
			return nil
		}
	}
	//**************************************************************************************************
	//	MARK: UserKYCStatusTopLevelInfoResponse.Result
	//--------------------------------------------------------------------------------------------------
	public enum Result : Validatable {
		case unsupported(code : Int)
		
		case success
		case failByTokenInvalid
		case failByTokenExpired
		case failByAccessDenied
		case failByServiceTemporaryUnavailable
		case failByBilling
		case failByUserKYCDeleted
		case failByInvalidParameters
		
		//==================================================================================================
		//	init(rawValue : Int)
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
				self = .failByBilling
			case -6:
				self = .failByUserKYCDeleted
			case -7:
				self = .failByInvalidParameters
			default:
				self = .unsupported(code: rawValue)
			}
		}
		//==================================================================================================
		//	validate
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
			case .failByInvalidParameters,
				 .failByServiceTemporaryUnavailable:
				throw HyperIDBaseAPIError.serverMaintenance
			case .failByBilling,
				 .failByUserKYCDeleted:
				return
			}
		}
	}
}

