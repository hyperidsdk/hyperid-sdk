import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: StorageDataKeyListResponse
//--------------------------------------------------------------------------------------------------
class StorageUserKeysListResponse<StorageResultType: StorageResultProtocol> : Codable, StorageDataKeysListResponseProtocol {
	private var	requestResultRaw	: Int64
	
	var 		keysPrivate			: [String] = []
	var 		keysPublic			: [String] = []
	
	private var	requestResult		: StorageResultType	{ StorageResultType(rawValue: requestResultRaw)	}
	var			result				: Validatable		{ requestResult									}
	
	//**************************************************************************************************
	//	MARK: StorageDataKeyListResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case requestResultRaw	= "result"
		case keysPrivate		= "keys_private"
		case keysPublic			= "keys_public"
	}
}

//**************************************************************************************************
//	MARK: StorageUserKeysResult
//--------------------------------------------------------------------------------------------------
enum StorageUserKeysResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case successNotFound
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
		switch rawValue{
		case 1:
			self = .successNotFound
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
		case .success,
			 .successNotFound:
			return
		case .failByTokenInvalid,
			 .failByTokenExpired,
			 .failByAccessDenied:
			throw HyperIDBaseAPIError.invalidAccessToken
		case .failByServiceTemporaryNotValid,
			 .failByInvalidParameters:
			throw HyperIDBaseAPIError.serverMaintenance
		}
	}
}

//**************************************************************************************************
//	MARK: StorageWalletUserKeysResult
//--------------------------------------------------------------------------------------------------
enum StorageWalletUserKeysResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case successNotFound
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByWalletNotExists

	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(rawValue: Int64) {
		switch rawValue {
		case 1:
			self = .successNotFound
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
		case .success, .successNotFound:
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
		}
	}
}

//**************************************************************************************************
//	MARK: StorageWalletUserDataResult
//--------------------------------------------------------------------------------------------------
enum StorageIdentityProviderUserKeysResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case successNotFound
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByIdentityProviderNotFound

	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(rawValue: Int64) {
		switch rawValue {
		case 1:
			self = .successNotFound
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
			 .successNotFound:
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
		}
	}
}
