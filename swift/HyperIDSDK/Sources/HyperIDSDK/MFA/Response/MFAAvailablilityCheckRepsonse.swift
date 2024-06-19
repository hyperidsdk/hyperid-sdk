import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: MFAAvailablilityCheckRepsonse
//--------------------------------------------------------------------------------------------------
struct MFAAvailablilityCheckRepsonse : HyperIDResponseBase, Codable {
	private var	requestResultRaw 	: Int64
	var			isAvailable			: Bool
	
	private var	requestResult		: Result		{ Result(rawValue: requestResultRaw)	}
	var			result				: Validatable	{ requestResult							}

	//**************************************************************************************************
	//	MARK: MFAAvailablilityCheckRepsonse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case requestResultRaw	= "result"
		case isAvailable		= "is_available"
	}
	//**************************************************************************************************
	//	MARK: MFAAvailablilityCheckRepsonse.Result
	//--------------------------------------------------------------------------------------------------
	private enum Result : Validatable {
		case unsupported(code: Int64)
		
		case success
		case failByServiceTemporaryNotValid
		case failByInvalidParameters
		case failByAccessDenied
		case failByTokenExpired
		case failByTokenInvalid
		
		//==================================================================================================
		//	init
		//--------------------------------------------------------------------------------------------------
		init(rawValue: Int64) {
			switch rawValue {
			case 0:
				self = .success
			case -1:
				self = .failByServiceTemporaryNotValid
			case -2:
				self = .failByInvalidParameters
			case -3:
				self = .failByAccessDenied
			case -4:
				self = .failByTokenExpired
			case -5:
				self = .failByTokenInvalid
			default:
				self = .unsupported(code: rawValue)
			}
		}
		//==================================================================================================
		//	validate
		//--------------------------------------------------------------------------------------------------
		func validate() throws {
			switch self {
			case .unsupported(code: _):
				throw HyperIDBaseAPIError.serverMaintenance
			case .success:
				return
			case .failByServiceTemporaryNotValid,
				 .failByInvalidParameters:
				throw HyperIDBaseAPIError.serverMaintenance
			case .failByAccessDenied,
				 .failByTokenExpired,
				 .failByTokenInvalid:
				throw HyperIDBaseAPIError.invalidAccessToken
			}
		}
	}
}
