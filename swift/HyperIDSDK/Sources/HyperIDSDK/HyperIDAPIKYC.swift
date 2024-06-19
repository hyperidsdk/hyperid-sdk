import Foundation
import HyperIDBase
import HyperIDAuth


//**************************************************************************************************
//	MARK: HyperIDKYCAPI
//--------------------------------------------------------------------------------------------------
public class HyperIDKYCAPI : HyperIDBaseAPI {
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public override init(providerInfo: ProviderInfo? = ProviderInfo.production,
						 openIDConfiguration: OpenIDConfiguration? = nil,
						 urlSession: URLSession! = URLSession.shared) async throws {
		try await super.init(providerInfo: providerInfo, openIDConfiguration: openIDConfiguration, urlSession: urlSession)
	}
	//==================================================================================================
	//	getUserKYCStatusInfo
	//--------------------------------------------------------------------------------------------------
	public func getUserKYCStatusInfo(kycVerificationLevel:	KYCVerificationLevel,
									 accessToken:			String) async throws -> UserKYCStatusInfo? {
		guard !accessToken.isEmpty else {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		var urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent("kyc/user/status-get"),
																  accessToken: accessToken)
		let httpBodyData : [String : Any] = [
			"verification_level":	kycVerificationLevel.rawValue
		]
		do {
			urlRequest.httpBody = try JSONSerialization.data(withJSONObject: httpBodyData)
			let (data, response) = try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			if data.count > 0 {
				guard let userKYCStatusInfoResponse = try? JSONDecoder().decode(UserKYCStatusInfoResponse.self, from: data) else {
					throw HyperIDBaseAPIError.serverMaintenance
				}
				return try userKYCStatusInfoResponse.factoryUserKYCStatusInfo()
			} else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: "\(error.localizedDescription)")
		}
	}
	//==================================================================================================
	//	getUserKYCStatusTopLevelInfo
	//--------------------------------------------------------------------------------------------------
	public func getUserKYCStatusTopLevelInfo(accessToken: String) async throws -> UserKYCStatusTopLevelInfo? {
		guard !accessToken.isEmpty else {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		let urlRequest = HyperIDRequestUtils.constructBaseRequest(openIDConfiguration.restApiTokenEndpoint.appendingPathComponent("kyc/user/status-top-level-get"),
																  accessToken: accessToken)
		do {
			let (data, response) = try await urlSession.data(for: urlRequest)
			guard let httpResponse = response as? HTTPURLResponse,
				  (200..<300).contains(httpResponse.statusCode) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			if data.count > 0 {
				guard let userKYCStatusTopLevelInfoResponse = try? JSONDecoder().decode(UserKYCStatusTopLevelInfoResponse.self, from: data) else {
					throw HyperIDBaseAPIError.serverMaintenance
				}
				return try userKYCStatusTopLevelInfoResponse.factoryUserKYCTopLevelInfo()
			} else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
		} catch let error as HyperIDBaseAPIError {
			throw error
		} catch {
			throw HyperIDBaseAPIError.networkingError(description: "\(error.localizedDescription)")
		}
	}
}
