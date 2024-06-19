import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: StorageUserWalletsGetResponse
//--------------------------------------------------------------------------------------------------
struct StorageUserWalletsGetResponse : HyperIDResponseBase, Codable {
	private var	requestResultRaw 	: Int64
	var			walletsPrivate		: [Wallet]
	var			walletsPublic		: [Wallet]
	
	private var	requestResult		: Result		{ Result(rawValue: requestResultRaw)	}
	var			result				: Validatable	{ requestResult							}

	//**************************************************************************************************
	//	MARK: StorageUserWalletsGetResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case requestResultRaw	= "result"
		case walletsPrivate		= "wallets_private"
		case walletsPublic		= "wallets_public"
	}
	//**************************************************************************************************
	//	MARK: StorageUserWalletsGetResponse.Result
	//--------------------------------------------------------------------------------------------------
	private enum Result : Validatable {
		case unsupported(code: Int64)
		
		case success
		case failByTokenInvalid
		case failByTokenExpired
		case failByAccessDenied
		case failByServiceTemporaryNotValid
		case failByInvalidParameters

		//==================================================================================================
		//	init
		//--------------------------------------------------------------------------------------------------
		init(rawValue: Int64) {
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
				self = .failByServiceTemporaryNotValid
			case -5:
				self = .failByInvalidParameters
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
