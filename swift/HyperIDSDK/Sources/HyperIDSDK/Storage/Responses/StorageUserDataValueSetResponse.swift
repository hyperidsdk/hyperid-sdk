import Foundation
import HyperIDBase

//**************************************************************************************************
//	StorageDataValueSetResponse
//--------------------------------------------------------------------------------------------------
class StorageDataValueSetResponse<StorageResultType: StorageResultProtocol> : Codable, HyperIDResponseBase {
	private var	requestResultRaw	: Int64

	private var	requestResult		: StorageResultType	{ StorageResultType(rawValue: requestResultRaw)	}
	var			result				: Validatable		{ requestResult								}
	
	//**************************************************************************************************
	//	StorageDataSetResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case requestResultRaw = "result"
	}
}

//**************************************************************************************************
//	StorageUserDataSetResult
//--------------------------------------------------------------------------------------------------
enum StorageUserDataSetResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByKeyAccessDenied
	case failByKeyInvalid
	
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
		case -6:
			self = .failByKeyAccessDenied
		case -7:
			self = .failByKeyInvalid
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
		case .failByTokenInvalid,
			 .failByTokenExpired,
			 .failByAccessDenied:
			throw HyperIDBaseAPIError.invalidAccessToken
		case .failByServiceTemporaryNotValid,
			 .failByInvalidParameters:
			throw HyperIDBaseAPIError.serverMaintenance
		case .failByKeyAccessDenied:
			throw HyperIDStorageAPIError.keyAccessDenied
		case .failByKeyInvalid:
			throw HyperIDStorageAPIError.keyInvalid
		}
	}
}
//**************************************************************************************************
//	StorageWalletUserDataSetResult
//--------------------------------------------------------------------------------------------------
enum StorageWalletUserDataSetResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByWalletNotExists
	case failByKeyAccessDenied
	case failByKeyInvalid
	
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
		case -6:
			self = .failByWalletNotExists
		case -7:
			self = .failByKeyAccessDenied
		case -8:
			self = .failByKeyInvalid
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
		case .failByTokenInvalid,
			 .failByTokenExpired,
			 .failByAccessDenied:
			throw HyperIDBaseAPIError.invalidAccessToken
		case .failByServiceTemporaryNotValid,
			 .failByInvalidParameters:
			throw HyperIDBaseAPIError.serverMaintenance
		case .failByWalletNotExists:
			throw HyperIDStorageAPIError.walletNotExists
		case .failByKeyAccessDenied:
			throw HyperIDStorageAPIError.keyAccessDenied
		case .failByKeyInvalid:
			throw HyperIDStorageAPIError.keyInvalid
		}
	}
}
//**************************************************************************************************
//	StorageIdentityProviderUserDataSetResult
//--------------------------------------------------------------------------------------------------
enum StorageIdentityProviderUserDataSetResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByIdentityProviderNotFound
	case failByKeyAccessDenied
	case failByKeyInvalid
	
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
		case -6:
			self = .failByIdentityProviderNotFound
		case -7:
			self = .failByKeyAccessDenied
		case -8:
			self = .failByKeyInvalid
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
		case .failByTokenInvalid,
			 .failByTokenExpired,
			 .failByAccessDenied:
			throw HyperIDBaseAPIError.invalidAccessToken
		case .failByServiceTemporaryNotValid,
			 .failByInvalidParameters:
			throw HyperIDBaseAPIError.serverMaintenance
		case .failByIdentityProviderNotFound:
			throw HyperIDStorageAPIError.identityProviderNotFound
		case .failByKeyAccessDenied:
			throw HyperIDStorageAPIError.keyAccessDenied
		case .failByKeyInvalid:
			throw HyperIDStorageAPIError.keyInvalid
		}
	}
}
