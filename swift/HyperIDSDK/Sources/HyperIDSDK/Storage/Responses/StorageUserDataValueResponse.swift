import Foundation
import HyperIDBase

//**************************************************************************************************
//	MARK: StorageUserDataResponse
//--------------------------------------------------------------------------------------------------
class StorageUserDataResponse<StorageResultType: StorageResultProtocol> : Codable, StorageUserDataResponseProtocol {
	private let	valuesStructs		: [StorageKeyValuePair]?
	private let	requestResultRaw	: Int64
	
	private var	requestResult		: StorageResultType		{ StorageResultType(rawValue: requestResultRaw)	}
	var			result				: Validatable			{ requestResult 								}
	var			values				: [String]				{ valuesStructs?.map({ $0.value }) ?? []		}
	//**************************************************************************************************
	//	StorageUserDataGetResponse.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
		case valuesStructs		= "values"
		case requestResultRaw	= "result"
	}
}

//**************************************************************************************************
//	MARK: StorageKeyValuePair
//--------------------------------------------------------------------------------------------------
class StorageKeyValuePair : Codable {
	var key		: String
	var value	: String
	//**************************************************************************************************
	//	MARK: StorageKeyValuePair.CodingKeys
	//--------------------------------------------------------------------------------------------------
	enum CodingKeys : String, CodingKey {
	case key	= "value_key"
	case value	= "value_data"
	}
}

//**************************************************************************************************
//	MARK: StorageUserDataResult
//--------------------------------------------------------------------------------------------------
enum StorageUserDataResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case successWithConflictKey
	case successNotFound
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByKeysSizeLimitReached
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(rawValue: Int64) {
		switch rawValue {
		case 2:
			self = .successWithConflictKey
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
			self = .failByKeysSizeLimitReached
		default:
			self = .unsupported(code: rawValue)
		}
	}
	//==================================================================================================
	//	validate()
	//--------------------------------------------------------------------------------------------------
	func validate() throws {
		switch self {
		case .unsupported(code: _):
			throw HyperIDBaseAPIError.serverMaintenance
		case .success,
			 .successWithConflictKey,
			 .successNotFound:
			return
		case .failByTokenInvalid,
			 .failByTokenExpired,
			 .failByAccessDenied:
			throw HyperIDBaseAPIError.invalidAccessToken
		case .failByServiceTemporaryNotValid,
			 .failByInvalidParameters:
			throw HyperIDBaseAPIError.serverMaintenance
		case .failByKeysSizeLimitReached:
			throw HyperIDStorageAPIError.keysSizeLimitReached
		}
	}
}

//**************************************************************************************************
//	MARK: StorageWalletUserDataResult
//--------------------------------------------------------------------------------------------------
enum StorageWalletUserDataResult : StorageResultProtocol {
	case unsupported(code: Int64)
	
	case successNotFound
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByWalletNotExists
	case failByKeysSizeLimitReached
	
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
		case -7:
			self = .failByKeysSizeLimitReached
		default:
			self = .unsupported(code: rawValue)
		}
	}
	//==================================================================================================
	//	init
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
		case .failByKeysSizeLimitReached:
			throw HyperIDStorageAPIError.keysSizeLimitReached
		case .failByWalletNotExists:
			throw HyperIDStorageAPIError.walletNotExists
		}
	}
}

//**************************************************************************************************
//	MARK: StorageIdentityProviderUserDataResult
//--------------------------------------------------------------------------------------------------
enum StorageIdentityProviderUserDataResult : StorageResultProtocol {
	case unsupported(code: Int64)

	case successNotFound
	case success
	case failByTokenInvalid
	case failByTokenExpired
	case failByAccessDenied
	case failByServiceTemporaryNotValid
	case failByInvalidParameters
	case failByIdentityProviderNotFound
	case failByKeysSizeLimitReached
	
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
		case -7:
			self = .failByKeysSizeLimitReached
		default:
			self = .unsupported(code: rawValue)
		}
	}
	//==================================================================================================
	//	init
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
		case .failByKeysSizeLimitReached:
			throw HyperIDStorageAPIError.keysSizeLimitReached
		case .failByIdentityProviderNotFound:
			throw HyperIDStorageAPIError.identityProviderNotFound
		}
	}
}
