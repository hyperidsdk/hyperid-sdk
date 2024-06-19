import Foundation
import HyperIDBase

public typealias ProviderInfo			= HyperIDBase.ProviderInfo
public typealias OpenIDConfiguration	= HyperIDBase.OpenIDConfiguration
public typealias TransactionHash		= String

//**************************************************************************************************
//	MARK: HyperIDAPIAuth
//--------------------------------------------------------------------------------------------------
public class HyperIDAuthAPI : HyperIDBase.HyperIDBaseAPI {
	public typealias RefreshTokenUpdateCallback	= (_ refreshToken	: String?) -> ()
	
	private let			clientInfo					: ClientInfo
	private var			accessToken_				: String?
	private var			refreshToken_				: String?

	
	public var			accessToken					: String?	{ accessToken_	}
	public var			refreshToken				: String?	{ refreshToken_	}
	var					refreshTokenUpdateCallback	: RefreshTokenUpdateCallback
	public var			isAuthorized				: Bool {
		guard let refreshToken = refreshToken else {
			return false
		}
		do
		{
			let jwtParts					= refreshToken.split(separator: ".")
			let base64URLEncodedPayload		= String(jwtParts[1])
			let data						= base64URLToData(base64URLEncodedPayload)!
			guard let jwtPayload = try? JSONSerialization.jsonObject(with: data),
				  let dictionary = jwtPayload as? [String : Any] else {
				return false
			}
			if let exp = dictionary["exp"] as? Double {
				return Date(timeIntervalSince1970: exp).timeIntervalSinceNow > 0
			}
			return true
		} catch {
			return false
		}
	}

	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public init(clientInfo					: ClientInfo,
				refreshToken				: String?								= nil,
				refreshTokenUpdateCallback	: @escaping RefreshTokenUpdateCallback,
				providerInfo				: ProviderInfo?							= ProviderInfo.production,
				openIDConfiguration			: OpenIDConfiguration?					= nil,
				urlSession					: URLSession! 							= URLSession.shared) async throws {
		self.clientInfo					= clientInfo
		self.refreshToken_				= refreshToken
		self.refreshTokenUpdateCallback	= refreshTokenUpdateCallback
		try await super.init(providerInfo: providerInfo, openIDConfiguration: openIDConfiguration, urlSession: urlSession)
	}
	//==================================================================================================
	//	startSignInWeb2()
	//--------------------------------------------------------------------------------------------------
	public func startSignInWeb2(kycVerificationLevel	: KYCVerificationLevel? = nil) throws -> URL {
		return try authorizationStart(flowMode:				.signInWeb2,
									  KYCVerificationLevel:	kycVerificationLevel)
	}
	//==================================================================================================
	//	startSignInWeb3()
	//--------------------------------------------------------------------------------------------------
	public func startSignInWeb3(walletFamily			: Int64?				= 0,
								kycVerificationLevel	: KYCVerificationLevel?	= nil) throws -> URL {
		return try authorizationStart(flowMode:		.signInWeb3,
									  walletFamily:	walletFamily)
	}
	//==================================================================================================
	//	startSignInUsingWallet
	//--------------------------------------------------------------------------------------------------
	public func startSignInUsingWallet(walletGetMode	: WalletGetMode	= .walletGetFast,
									   walletFamily		: Int64?		= 0) throws -> URL {
		return try authorizationStart(flowMode:			.signInWalletGet,
									  walletGetMode:	walletGetMode,
									  walletFamily: 	walletFamily)
	}
	//==================================================================================================
	//	startSignInGuestUpgrade
	//--------------------------------------------------------------------------------------------------
	public func startSignInGuestUpgrade() throws -> URL {
		return try authorizationStart(flowMode: .signInGuestUpgrade)
	}
	//==================================================================================================
	//	startSignInIdentityProvider
	//--------------------------------------------------------------------------------------------------
	public func startSignInIdentityProvider(identityProvider		: String,
											kycVerificationLevel	: KYCVerificationLevel? = nil) throws -> URL {
		return try authorizationStart(flowMode:			.signInIdentityProvider,
									  identityProvider:	identityProvider)
	}
	//==================================================================================================
	//	startSignInWithTransaction
	//--------------------------------------------------------------------------------------------------
	public func startSignInWithTransaction(from		: String? = nil,
										   to		: String,
										   chain	: String,
										   data		: String?,
										   gas		: String? = nil,
										   nonce	: String? = nil,
										   value	: String? = nil) throws -> URL {
		var transaction : [String : any Codable] = [
			"to"		: to,
			"chain"		: chain
		]
		if let data = data
		{
			transaction["data"] = data
		}
		else if value == nil
		{
			throw HyperIDAuthAPIError.transactionDataAndValueIsEmpty
		}
		if let from = from {
			transaction["from"] = from
		}
		if let gas = gas {
			transaction["gas"] = gas
		}
		if let nonce = nonce {
			transaction["nonce"] = nonce
		}
		if let value = value {
			transaction["value"] = value
		}
		return try authorizationStart(flowMode:			.noFlowMode,
									  transactionInfo:	String(data:		JSONSerialization.data(withJSONObject: transaction),
															   encoding:	.utf8))
	}
	//==================================================================================================
	//	exchangeToTokens(redirectURL : URL)
	//--------------------------------------------------------------------------------------------------
	public func exchangeToTokens(redirectURL : URL) async throws {
		let urlComponents = URLComponents(url: redirectURL, resolvingAgainstBaseURL: false)!
		guard let code = urlComponents.queryItems?.first(where: { $0.name == "code" })?.value else {
			let errorDescription = urlComponents.queryItems?.first(where: { $0.name == "error_description" })?.value
			throw HyperIDAuthAPIError.authorizationInvalidRedirectURLError(description: errorDescription ?? "Unknown error")
		}
		var request = URLRequest(url: openIDConfiguration.tokenEndpoint!)
		request.httpMethod	= "POST"
		request.httpBody	= try! "grant_type=authorization_code&code=\(code)&\(clientInfo.authorizationParameters(issuer: openIDConfiguration.issuer))".data(using: .utf8)
		do {
			let (data, response) = try await urlSession.data(for: request)
			try await exchangeResultProcess(httpResponse:	response as? HTTPURLResponse, data: data)
		} catch let error as HyperIDBaseAPIError { throw error }
		catch let error as HyperIDAuthAPIError { throw error }
		catch {
			throw HyperIDBaseAPIError.networkingError(description: String(describing: error))
		}
	}
	//==================================================================================================
	//	exchangeToTokensWithTransaction(redirectURL : URL)
	//--------------------------------------------------------------------------------------------------
	public func exchangeToTokensWithTransaction(redirectURL : URL) async throws -> TransactionHash {
		let urlComponents = URLComponents(url: redirectURL, resolvingAgainstBaseURL: false)!
		guard let code = urlComponents.queryItems?.first(where: { $0.name == "code" })?.value else {
			let errorDescription = urlComponents.queryItems?.first(where: { $0.name == "error_description" })?.value
			throw HyperIDAuthAPIError.authorizationInvalidRedirectURLError(description: errorDescription ?? "Unknown error")
		}
		guard let transactionHash = urlComponents.queryItems?.first(where: { $0.name == "transaction_hash" })?.value else
		{
			guard let transactionResultStr = urlComponents.queryItems?.first(where: { $0.name == "transaction_result" })?.value else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			let transactionErrorDescription = urlComponents.queryItems?.first(where: { $0.name == "transaction_result_description" })?.value
			switch TransactionResult(rawValue: (try? Int(value: transactionResultStr)) ?? -1)
			{
			case .failByInvalidParameters:
				throw HyperIDAuthAPIError.transactionInvalidParameters(description: transactionErrorDescription ?? "")
			case .rejectetByUser:
				throw HyperIDAuthAPIError.transactionRejectedByUser(description: transactionErrorDescription ?? "")
			default:
				throw HyperIDBaseAPIError.serverMaintenance
			}
		}
		var request = URLRequest(url: openIDConfiguration.tokenEndpoint!)
		request.httpMethod	= "POST"
		request.httpBody	= try! "grant_type=authorization_code&code=\(code)&\(clientInfo.authorizationParameters(issuer: openIDConfiguration.issuer))".data(using: .utf8)
		do {
			let (data, response) = try await urlSession.data(for: request)
			try await exchangeResultProcess(httpResponse:	response as? HTTPURLResponse, data: data)
		} catch let error as HyperIDBaseAPIError { throw error }
		catch let error as HyperIDAuthAPIError { throw error }
		catch {
			throw HyperIDBaseAPIError.networkingError(description: String(describing: error))
		}
		return transactionHash
	}
	//==================================================================================================
	//	refreshAccessToken
	//--------------------------------------------------------------------------------------------------
	public func refreshTokens() async throws {
		guard let refreshToken = refreshToken else {
			throw HyperIDAuthAPIError.tokenExchangeInvalidGrant(description: "no refresh token for credentials refresh")
		}
		var request = URLRequest(url: openIDConfiguration.tokenEndpoint!)
		request.httpMethod	= "POST"
		request.httpBody	= try! "grant_type=refresh_token&refresh_token=\(refreshToken)&\(clientInfo.authorizationParameters(issuer: openIDConfiguration.issuer))".data(using: .utf8)
		do {
			let (data, response) = try await urlSession.data(for: request)
			try await exchangeResultProcess(httpResponse: response as? HTTPURLResponse, data: data)
		} catch let error as HyperIDBaseAPIError { throw error }
		catch let error as HyperIDAuthAPIError { throw error }
		catch {
			throw HyperIDBaseAPIError.networkingError(description: String(describing: error))
		}
	}
	//==================================================================================================
	//	logout
	//--------------------------------------------------------------------------------------------------
	public func logout() async throws {
		let onLogoutCompleted = { [self] in
			invalidateTokens()
		}
		guard isAuthorized else {
			onLogoutCompleted()
			return
		}
		var request = URLRequest(url: openIDConfiguration.endSessionEndpoint)
		request.httpMethod	= "POST"
		request.httpBody	= try! "refresh_token=\(refreshToken!)&\(clientInfo.authorizationParameters(issuer: openIDConfiguration.issuer))".data(using: .utf8)
		do {
			let (_, response) = try await urlSession.data(for: request)
			guard let httpResponse = response as? HTTPURLResponse else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			switch httpResponse.statusCode {
			case 200..<300:
				onLogoutCompleted()
			default:
				throw HyperIDBaseAPIError.serverMaintenance
			}
		} catch let error as HyperIDBaseAPIError { throw error }
		catch let error as HyperIDAuthAPIError { throw error }
		catch {
			throw HyperIDBaseAPIError.networkingError(description: String(describing: error))
		}
	}
	//==================================================================================================
	//	getUserInfo
	//--------------------------------------------------------------------------------------------------
	public func getUserInfo() async throws -> UserInfo {
		guard let accessToken = accessToken else {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
		do {
			let jwtParts					= accessToken.split(separator: ".")
			let base64URLEncodedPayload		= String(jwtParts[1])
			let data						= base64URLToData(base64URLEncodedPayload)!
			return try JSONDecoder().decode(UserInfo.self, from: data)
		} catch {
			throw HyperIDBaseAPIError.invalidAccessToken
		}
	}
	//==================================================================================================
	//	invalidateTokens
	//--------------------------------------------------------------------------------------------------
	public func invalidateTokens()
	{
		accessToken_	= nil
		if refreshToken_ != nil
		{
			refreshToken_	= nil
			refreshTokenUpdateCallback(nil)
		}
	}
}

//**************************************************************************************************
//	MARK: HyperIDAPIAuth - private
//--------------------------------------------------------------------------------------------------
extension HyperIDAuthAPI {
	//**************************************************************************************************
	//	HyperIDAPIAuth.AuthorizationFlowMode
	//--------------------------------------------------------------------------------------------------
	enum AuthorizationFlowMode : String{
		case noFlowMode				= ""
		case signInWeb2				= "0"
		case signInWeb3				= "3"
		case signInWalletGet		= "4"
		case signInGuestUpgrade		= "6"
		case signInIdentityProvider	= "9"
	}
	//==================================================================================================
	//	authorizationStart
	//--------------------------------------------------------------------------------------------------
	private func authorizationStart(flowMode				: AuthorizationFlowMode,
									walletGetMode			: WalletGetMode?			= nil,
									walletFamily			: Int64?					= nil,
									identityProvider		: String?					= nil,
									KYCVerificationLevel	: KYCVerificationLevel?		= nil,
									transactionInfo			: String?					= nil) throws -> URL {
		if let identityProvider = identityProvider,
		   !openIDConfiguration.identityProviders.contains(where: { $0 == identityProvider }) {
			throw HyperIDAuthAPIError.unknownIdentityProvider(provider: identityProvider)
		}
		if let walletFamily = walletFamily,
		   !openIDConfiguration.walletFamilies.contains(where: { $0.value == walletFamily}) {
			throw HyperIDAuthAPIError.unknownWalletFamily
		}
		switch KYCVerificationLevel {
		case .unsupported(code: _):
			throw HyperIDBaseAPIError.invalidKYCVerificationLevel
		default:
			break;
		}
		var urlComponents = URLComponents(url: openIDConfiguration.authEndpoint, resolvingAgainstBaseURL: false)!
		var urlQueryItems = [
			URLQueryItem(name: "response_type", value: "code"),
			URLQueryItem(name: "scope",			value: clientInfo.scopes?.joined(separator: " ") ?? openIDConfiguration.scopes.joined(separator: " ")),
			URLQueryItem(name: "client_id",		value: clientInfo.clientId),
			URLQueryItem(name: "redirect_uri",	value: clientInfo.redirectURL),
		]
		if flowMode != .noFlowMode
		{
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "flow_mode",		value: flowMode.rawValue)
			])
		}
		if let walletGetMode = walletGetMode {
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "wallet_get_mode",			value: walletGetMode.rawValue)
			])
		}
		if let walletFamily = walletFamily {
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "wallet_family",				value: String(walletFamily))
			])
		}
		if let identityProvider = identityProvider {
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "identity_provider",			value: identityProvider)
			])
		}
		if let kycVerificationLevel = KYCVerificationLevel {
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "verification_level",		value: String(kycVerificationLevel.rawValue))
			])
		}
		if let transactionInfo = transactionInfo
		{
			urlQueryItems.append(contentsOf: [
				URLQueryItem(name: "transaction",				value: transactionInfo)
			])
		}
		urlComponents.queryItems = urlQueryItems
		return urlComponents.url!
	}
	//==================================================================================================
	//	exchangeResultProcess
	//--------------------------------------------------------------------------------------------------
	private func exchangeResultProcess(httpResponse	: HTTPURLResponse?,
									   data			: Data?) async throws {
		guard let httpResponse = httpResponse else {
			throw HyperIDBaseAPIError.serverMaintenance
		}
		switch httpResponse.statusCode {
		case 200..<300:
			guard let tokenInfo = try? JSONDecoder().decode(TokenExchangeResponse.self, from: data!) else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			accessToken_	= tokenInfo.accessToken
			refreshToken_	= tokenInfo.refreshToken
			refreshTokenUpdateCallback(tokenInfo.refreshToken)
		case 400..<500:
			guard let data = data else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			let errorDictionary	= try JSONSerialization.jsonObject(with: data) as? [String : Any]
			guard let errorDictionary = errorDictionary else {
				throw HyperIDBaseAPIError.serverMaintenance
			}
			switch errorDictionary["error"] as? String {
			case "invalid_request":
				throw HyperIDBaseAPIError.serverMaintenance
			case "invalid_grant":
				invalidateTokens()
				throw HyperIDAuthAPIError.tokenExchangeInvalidGrant(description: String(data: data, encoding: .utf8) ?? "")
			case "invalid_client":
				throw HyperIDAuthAPIError.invalidClientInfo(description: String(data: data, encoding: .utf8) ?? "")
			case "unauthorized_client":
				throw HyperIDAuthAPIError.invalidClientInfo(description: String(data: data, encoding: .utf8) ?? "")
			case "unsupported_grant_type":
				throw HyperIDAuthAPIError.invalidClientInfo(description: String(data: data, encoding: .utf8) ?? "")
			default:
				throw HyperIDBaseAPIError.serverMaintenance
			}
		default:
			throw HyperIDBaseAPIError.serverMaintenance
		}
	}
	//==================================================================================================
	//	base64URLToData
	//--------------------------------------------------------------------------------------------------
	private func base64URLToData(_ base64URLString: String) -> Data? {
		var base64 = base64URLString
			.replacingOccurrences(of: "-", with: "+")
			.replacingOccurrences(of: "_", with: "/")
		let paddingLength = 4 - (base64.count % 4)
		if paddingLength < 4 {
			base64 += String(repeating: "=", count: paddingLength)
		}
		return Data(base64Encoded: base64)
	}
}
