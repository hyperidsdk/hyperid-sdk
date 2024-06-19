import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: MFATransactionStatusResponse
//--------------------------------------------------------------------------------------------------
struct MFATransactionStatusResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw	: Int64
	private var	statusRaw			: Int64?
	private var	completeResult		: Int64?
	
	private var	requestResult		: Result				{ Result(rawValue: requestResultRaw)	}
	var			result				: Validatable			{ requestResult							}
	var			status				: MFATransactionStatus?	{
		switch requestResult {
		case .success:
			MFATransactionStatus(rawValue: statusRaw!, completeResult: MFACompleteResult(rawValue:completeResult))
		default:
			nil
		}
	}
	//**************************************************************************************************
	//	MARK: MFATransactionStatusResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case requestResultRaw	= "result"
		case statusRaw			= "transaction_status"
		case completeResult		= "transaction_complete_result"
	}
	//==================================================================================================
	//	validate
	//--------------------------------------------------------------------------------------------------
	func validate()	throws	{ try result.validate() }
	//**************************************************************************************************
	//	MARK: MFATransactionStatusResponse.Result
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
			case .failByTransactionNotFound:
				return
			}
		}
	}
}
