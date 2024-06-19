import Foundation
import HyperIDBase

//**************************************************************************************************
//	HyperIDStorage - base
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	baseEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	private var baseEndpointSuffix : String {
		switch self {
		case .email:
			"user-data/by-email/"
		case .userID:
			"user-data/by-user-id/"
		case .wallet(address: _):
			"user-data/by-wallet/"
		case .identityProvider(_):
			"user-data/by-idp/"
		}
	}
	//==================================================================================================
	//	storageIdentifierPair
	//--------------------------------------------------------------------------------------------------
	private var storageIdentifierPair : [String: Any] {
		switch self {
		case .wallet(address: let walletAddress):
			["wallet_address": walletAddress]
		case .identityProvider(let identityProvider):
			["identity_provider": identityProvider]
		default:
			[:]
		}
	}
}
//**************************************************************************************************
//	HyperIDStorage - list get
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	userDataSharedKeysListGetEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	var userDataSharedKeysListGetEndpointSuffix : String { "\(baseEndpointSuffix)shared-list-get" }
	//==================================================================================================
	//	factoryUserDataSharedKeysListGetRequestHTTPBody
	//--------------------------------------------------------------------------------------------------
	func factoryUserDataSharedKeysListGetRequestHTTPBody(pageSize: Int64, searchId: String?) throws -> Data {
		var httpBodyData : [String : Any] = [
			"page_size" : pageSize,
		]
		if let searchId = searchId {
			httpBodyData["search_id"] = searchId
		}
		self.storageIdentifierPair.forEach { (key: String, value: Any) in httpBodyData[key] = value }
		return try JSONSerialization.data(withJSONObject: httpBodyData)
	}
	//==================================================================================================
	//	processUserDataSharedKeysListGetResponseData
	//--------------------------------------------------------------------------------------------------
	func processUserDataSharedKeysListGetResponseData(data: Data) throws -> (keys: [String], nextSearchId: String?) {
		guard data.count > 0 else { throw HyperIDBaseAPIError.serverMaintenance }
		var responseDecoded : StorageSharedDataKeysListResponseProtocol!
		switch self {
		case .email,
			 .userID:
			responseDecoded = try? JSONDecoder().decode(StorageUserSharedKeysListResponse<StrorageSharedKeysListResult>.self, from: data)
		case .wallet(address: _):
			responseDecoded = try? JSONDecoder().decode(StorageUserSharedKeysListResponse<StrorageWalletSharedKeysListResult>.self, from: data)
		case .identityProvider(_):
			responseDecoded = try JSONDecoder().decode(StorageUserSharedKeysListResponse<StrorageIdentityProviderSharedKeysListResult>.self, from: data)
		}
		guard let responseDecoded = responseDecoded else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		try responseDecoded.validate()
		return (keys: responseDecoded.keys, nextSearchId: responseDecoded.nextSearchId.isEmpty ? nil : responseDecoded.nextSearchId)
	}
}
//**************************************************************************************************
//	HyperIDStorage - list get
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	userDataKeysListGetEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	var userDataKeysListGetEndpointSuffix : String { "\(baseEndpointSuffix)list-get" }
	//==================================================================================================
	//	factoryUserDataKeysListGetRequestHTTPBody
	//--------------------------------------------------------------------------------------------------
	func factoryUserDataKeysListGetRequestHTTPBody() throws -> Data {
		return try JSONSerialization.data(withJSONObject: storageIdentifierPair)
	}
	//==================================================================================================
	//	processUserDataKeysListGetResponseData
	//--------------------------------------------------------------------------------------------------
	func processUserDataKeysListGetResponseData(data: Data) throws -> (keysPrivate: [String], keysPublic: [String]) {
		guard data.count > 0 else { throw HyperIDBaseAPIError.serverMaintenance }
		var responseDecoded : StorageDataKeysListResponseProtocol!
		switch self {
		case .email,
			 .userID:
			responseDecoded = try? JSONDecoder().decode(StorageUserKeysListResponse<StorageUserKeysResult>.self, from: data)
		case .wallet(address: _):
			responseDecoded = try? JSONDecoder().decode(StorageUserKeysListResponse<StorageWalletUserKeysResult>.self, from: data)
		case .identityProvider(_):
			responseDecoded = try JSONDecoder().decode(StorageUserKeysListResponse<StorageIdentityProviderUserKeysResult>.self, from: data)
		}
		guard let responseDecoded = responseDecoded else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		try responseDecoded.validate()
		return (keysPrivate:		responseDecoded.keysPrivate,
				keysPublic:			responseDecoded.keysPublic)
	}
}
//**************************************************************************************************
//	HyperIDStorage - data set
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	userDataValueSetEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	var userDataValueSetEndpointSuffix : String { "\(baseEndpointSuffix)set" }
	//==================================================================================================
	//	factoryUserDataValueSetRequestHTTPBody
	//--------------------------------------------------------------------------------------------------
	func factoryUserDataValueSetRequestHTTPBody(_	value 	: (key: String, value: String),
												dataScope	: UserDataAccessScope) throws -> Data {
		var httpBodyData : [String : Any] = [
			"value_key"			: value.key,
			"value_data"		: value.value,
			"access_scope"		: dataScope.rawValue
		]
		self.storageIdentifierPair.forEach { (key: String, value: Any) in httpBodyData[key] = value }
		return try JSONSerialization.data(withJSONObject: httpBodyData)
	}
	//==================================================================================================
	//	processUserDataValueSetResponseData
	//--------------------------------------------------------------------------------------------------
	func processUserDataValueSetResponseData(data: Data) throws {
		guard data.count > 0 else { throw HyperIDBaseAPIError.serverMaintenance }
		var responseDecoded : HyperIDResponseBase!
		switch self {
		case .email,
			 .userID:
			responseDecoded = try? JSONDecoder().decode(StorageDataValueSetResponse<StorageUserDataSetResult>.self, from: data)
		case .wallet(address: _):
			responseDecoded = try? JSONDecoder().decode(StorageDataValueSetResponse<StorageWalletUserDataSetResult>.self, from: data)
		case .identityProvider(_):
			responseDecoded = try? JSONDecoder().decode(StorageDataValueSetResponse<StorageIdentityProviderUserDataSetResult>.self, from: data)
		}
		guard let responseDecoded = responseDecoded else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		try responseDecoded.validate()
	}
}
//**************************************************************************************************
//	HyperIDStorage - data get
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	userDataValueSetEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	var userDataValueGetEndpointSuffix : String { "\(baseEndpointSuffix)get" }
	//==================================================================================================
	//	factoryUserDataValueGetRequestHTTPBody
	//--------------------------------------------------------------------------------------------------
	func factoryUserDataValueGetRequestHTTPBody(_ key : String) throws -> Data{
		var httpBodyData : [String : Any] = [
			"value_keys": [key],
		]
		self.storageIdentifierPair.forEach { (key: String, value: Any) in httpBodyData[key] = value }
		return try JSONSerialization.data(withJSONObject: httpBodyData)
	}
	//==================================================================================================
	//	processUserDataValueGetResponseData
	//--------------------------------------------------------------------------------------------------
	func processUserDataValueGetResponseData(data: Data) throws -> String? {
		guard data.count > 0 else { throw HyperIDBaseAPIError.serverMaintenance }
		var responseDecoded : StorageUserDataResponseProtocol!
		switch self {
		case .email,
			 .userID:
			responseDecoded = try? JSONDecoder().decode(StorageUserDataResponse<StorageUserDataResult>.self, from: data)
		case .wallet(address: _):
			responseDecoded = try? JSONDecoder().decode(StorageUserDataResponse<StorageWalletUserDataResult>.self, from: data)
		case .identityProvider(_):
			responseDecoded = try? JSONDecoder().decode(StorageUserDataResponse<StorageIdentityProviderUserDataResult>.self, from: data)
		}
		guard let responseDecoded = responseDecoded else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		try responseDecoded.validate()
		return responseDecoded.values.first
	}
}
//**************************************************************************************************
//	HyperIDStorage - data delete
//--------------------------------------------------------------------------------------------------
extension HyperIDStorage {
	//==================================================================================================
	//	userDataValueDeleteEndpointSuffix
	//--------------------------------------------------------------------------------------------------
	var userDataValueDeleteEndpointSuffix : String { "\(baseEndpointSuffix)delete" }
	//==================================================================================================
	//	factoryUserDataValueGetRequestHTTPBody
	//--------------------------------------------------------------------------------------------------
	func factoryUserDataValueDeleteRequestHTTPBody(_ key : String) throws -> Data{
		var httpBodyData : [String : Any] = [
			"value_keys": [key],
		]
		self.storageIdentifierPair.forEach { (key: String, value: Any) in httpBodyData[key] = value }
		return try JSONSerialization.data(withJSONObject: httpBodyData)
	}
	//==================================================================================================
	//	processUserDataValueGetResponseData
	//--------------------------------------------------------------------------------------------------
	func processUserDataValueDeleteResponseData(data: Data) throws {
		guard data.count > 0 else { throw HyperIDBaseAPIError.serverMaintenance }
		var responseDecoded : HyperIDResponseBase!
		switch self {
		case .email,
			 .userID:
			responseDecoded = try? JSONDecoder().decode(StorageUserDataValueDeleteResponse<StorageUserDataDeleteResult>.self, from: data)
		case .wallet(address: _):
			responseDecoded = try? JSONDecoder().decode(StorageUserDataValueDeleteResponse<StorageWalletUserDataDeleteResult>.self, from: data)
		case .identityProvider(_):
			responseDecoded = try? JSONDecoder().decode(StorageUserDataValueDeleteResponse<StorageIdentityProviderUserDataDeleteResult>.self, from: data)
		}
		guard let responseDecoded = responseDecoded else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		try responseDecoded.validate()
	}
}
