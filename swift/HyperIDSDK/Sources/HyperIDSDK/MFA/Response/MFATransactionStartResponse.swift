import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: MFATransactionStartResponse
//--------------------------------------------------------------------------------------------------
struct MFATransactionStartResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw	: Int64
	var			transactionId		: Int
	var			actionId			: String
	
	private var	requestResult		: Result		{ Result(rawValue: requestResultRaw)	}
	var			result				: Validatable	{ requestResult							}

	//==================================================================================================
	//	validate
	//--------------------------------------------------------------------------------------------------
	func validate()	throws	{ try result.validate() }
	//**************************************************************************************************
	//	MARK: MFATransactionStartResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case requestResultRaw		= "result"
		case transactionId			= "transaction_id"
		case actionId				= "action_id"
	}
	//**************************************************************************************************
	//	MARK: MFATransactionStartResponse.Result
	//--------------------------------------------------------------------------------------------------
	private enum Result : Validatable{
		case unsupported(code: Int64)
		
		case success
		case failByServiceTemporaryNotValid
		case failByInvalidParameters
		case failByAccessDenied
		case failByTokenExpired
		case failByTokenInvalid
		case failByUserDeviceNotFound
		case failByTemplateNotFound
		
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
			case -7:
				self = .failByUserDeviceNotFound
			case -8:
				self = .failByTemplateNotFound
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
				 .failByInvalidParameters,
				 .failByTemplateNotFound:
				throw HyperIDBaseAPIError.serverMaintenance
			case .failByAccessDenied,
				 .failByTokenExpired,
				 .failByTokenInvalid:
				throw HyperIDBaseAPIError.invalidAccessToken
			case .failByUserDeviceNotFound:
				throw HyperIDMFAAPIError.hyperIDAuthenticatorNotAttached
			}
		}
	}
}
