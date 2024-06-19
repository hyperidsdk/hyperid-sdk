import Foundation
import HyperIDBase

//**************************************************************************************************
//	HyperIDStorageAPI
//--------------------------------------------------------------------------------------------------
public class HyperIDStorageAPI : HyperIDBaseAPI {
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public override init(providerInfo: ProviderInfo? = ProviderInfo.production,
						 openIDConfiguration: OpenIDConfiguration? = nil,
						 urlSession: URLSession! = URLSession.shared) async throws {
		try await super.init(providerInfo: providerInfo, openIDConfiguration: openIDConfiguration, urlSession: urlSession)
	}
	//==================================================================================================
	//	getUserKeysList
	//--------------------------------------------------------------------------------------------------
	public func getUserWallets(accessToken	: String) async throws -> (walletsPrivate: [Wallet], walletsPublic: [Wallet])
	{
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		let urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent("/user-wallets/get"),
																  accessToken: accessToken)
		do {
			let (data, response)	= try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			guard let userWalletsGetResponse = try? JSONDecoder().decode(StorageUserWalletsGetResponse.self, from: data) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			try userWalletsGetResponse.validate()
			return (walletsPrivate: userWalletsGetResponse.walletsPrivate, walletsPublic: userWalletsGetResponse.walletsPublic)
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch let error as HyperIDStorageAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: "\(error)")
		}
	}
	//==================================================================================================
	//	getUserKeysList
	//--------------------------------------------------------------------------------------------------
	public func getUserKeysList(storage:		HyperIDStorage,
								accessToken:	String) async throws -> (keysPrivate: [String], keysPublic: [String]) {
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent(storage.userDataKeysListGetEndpointSuffix),
																  accessToken: accessToken)
		do {
			urlRequest.httpBody		= try storage.factoryUserDataKeysListGetRequestHTTPBody()
			let (data, response)	= try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			return try storage.processUserDataKeysListGetResponseData(data: data)
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch let error as HyperIDStorageAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: "\(error)")
		}
	}
	//==================================================================================================
	//	getUserSharedKeyList
	//--------------------------------------------------------------------------------------------------
	public func getUserSharedKeysList(storage:		HyperIDStorage,
									  accessToken:	String) async throws -> [String] {
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		var searchId : String?
		var result : [String] = []
		repeat {
			var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent(storage.userDataSharedKeysListGetEndpointSuffix),
																	  accessToken: accessToken)
			do {
				urlRequest.httpBody = try storage.factoryUserDataSharedKeysListGetRequestHTTPBody(pageSize: 100, searchId: searchId)
				let (data, response)	= try await urlSession.data(for: urlRequest)
				guard let httpResponse = response as? HTTPURLResponse,
					  (200..<300).contains(httpResponse.statusCode) else {
					throw HyperIDBaseAPIError.serverMaintenance
				}
				let queryResullt = try storage.processUserDataSharedKeysListGetResponseData(data: data)
				searchId = queryResullt.nextSearchId
				result.append(contentsOf: queryResullt.keys)
			} catch let error as HyperIDBaseAPIError {
				throw error
			} catch let error as HyperIDStorageAPIError {
			 throw error
			} catch {
			 throw HyperIDBaseAPIError.networkingError(description: "\(error)")
			}
		} while searchId != nil
		return result
	}
	//==================================================================================================
	//	setUserData
	//--------------------------------------------------------------------------------------------------
	public func setUserData(_ value		: (key: String, value: String),
							dataScope	: UserDataAccessScope = .public,
							storage		: HyperIDStorage,
							accessToken : String) async throws {
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		guard !value.key.isEmpty else { throw HyperIDStorageAPIError.keyInvalid }
		var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent(storage.userDataValueSetEndpointSuffix),
																  accessToken: accessToken)
		do {
			urlRequest.httpBody		= try storage.factoryUserDataValueSetRequestHTTPBody(value, dataScope: dataScope)
			let (data, response)	= try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			try storage.processUserDataValueSetResponseData(data: data)
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch let error as HyperIDStorageAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: error.localizedDescription)
		}
	}
	//==================================================================================================
	//	getUserData
	//--------------------------------------------------------------------------------------------------
	public func getUserData(_ key		: String,
							storage		: HyperIDStorage,
							accessToken	: String) async throws -> String? {
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		guard !key.isEmpty else { throw HyperIDStorageAPIError.keyInvalid }
		var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent(storage.userDataValueGetEndpointSuffix),
																  accessToken: accessToken)
		do {
			urlRequest.httpBody		= try storage.factoryUserDataValueGetRequestHTTPBody(key)
			let (data, response)	= try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			return try storage.processUserDataValueGetResponseData(data: data)
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch let error as HyperIDStorageAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: error.localizedDescription)
		}
	}
	//==================================================================================================
	//	deleteUserData
	//--------------------------------------------------------------------------------------------------
	public func deleteUserData(_ key		: String,
							   storage		: HyperIDStorage,
							   accessToken	: String) async throws {
		if accessToken.isEmpty {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		guard !key.isEmpty else { throw HyperIDStorageAPIError.keyInvalid }
		var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent(storage.userDataValueDeleteEndpointSuffix),
																  accessToken: accessToken)
		do {
			urlRequest.httpBody		= try storage.factoryUserDataValueDeleteRequestHTTPBody(key)
			let (data, response)	= try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			try storage.processUserDataValueDeleteResponseData(data: data)
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch let error as HyperIDStorageAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: error.localizedDescription)
		}
	}
}
