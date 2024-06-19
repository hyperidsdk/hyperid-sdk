import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: MFATransactionCancelResponse
//--------------------------------------------------------------------------------------------------
struct MFATransactionCancelResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw	: Int64
	
	private var	requestResult		: Result		{ Result(rawValue: requestResultRaw)	}
	var			result				: Validatable	{ requestResult							}
	//==================================================================================================
	//	validate
	//--------------------------------------------------------------------------------------------------
	func validate()	throws	{ try result.validate() }
	//**************************************************************************************************
	//	MARK: MFATransactionStatusResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case requestResultRaw	= "result"
	}
	//**************************************************************************************************
	//	MARK: MFATransactionCancelResponse.Result
	//--------------------------------------------------------------------------------------------------
	private enum Result : Validatable {
		case unsupported(code: Int64)
		
		case success
		case failByServiceTemporaryNotValid
		case failByInvalidParameters
		case failByAccessDenied
		case failByTokenExpired
		case failByTokenInvalid
		case failByTransactionNotFound
		case failByTransactionExpired
		case failByTransactionCompleted
		case failByAlreadyCanceled

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
			case -6:
				self = .failByTransactionNotFound
			case -8:
				self = .failByTransactionExpired
			case -9:
				self = .failByTransactionCompleted
			case -10:
				self = .failByAlreadyCanceled
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
			case .success,
				 .failByTransactionExpired,
				 .failByAlreadyCanceled:
				return
			case .failByServiceTemporaryNotValid,
				 .failByInvalidParameters:
				throw HyperIDBaseAPIError.serverMaintenance
			case .failByAccessDenied,
				 .failByTokenExpired,
				 .failByTokenInvalid:
				throw HyperIDBaseAPIError.invalidAccessToken
			case .failByTransactionNotFound:
				throw HyperIDMFAAPIError.MFATransactionNotFound
			case .failByTransactionCompleted:
				throw HyperIDMFAAPIError.MFATransactionAlreadyCompleted
			}
		}
	}
}
