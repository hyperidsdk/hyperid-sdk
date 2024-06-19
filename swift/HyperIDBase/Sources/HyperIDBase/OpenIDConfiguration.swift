import Foundation

//**************************************************************************************************
//	MARK: OpenIDConfiguration
//--------------------------------------------------------------------------------------------------
public class OpenIDConfiguration : Codable {
	public let		authEndpoint			: URL!
	public let		tokenEndpoint			: URL!
	public let		introspectionEndpoint	: URL!
	public let		userInfoEndpoint		: URL!
	public let		revokeEndpoint			: URL!
	public let		endSessionEndpoint		: URL!
	public let		restApiTokenEndpoint	: URL!
	public let		restApiPublicEndpoint	: URL!
	public let		issuer					: URL!
	public let		walletSources			: [String : Int64]
	public let		walletFamilies			: [String : Int64]
	public let		identityProviders		: [String]
	private var		walletChains_			: [String]
	private let		scopesDefault			: [String]
	private let		scopesOptional			: [String]

	public lazy var scopes					: [String]				= { scopesDefault + scopesOptional 											}()
	public lazy var walletChains			: [Int64]				= { walletChains_.map		{ Int64($0)!}									}()
	
	//**************************************************************************************************
	//	OpenIDConfiguration.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case authEndpoint			= "authorization_endpoint"
		case tokenEndpoint			= "token_endpoint"
		case introspectionEndpoint	= "introspection_endpoint"
		case userInfoEndpoint		= "userinfo_endpoint"
		case revokeEndpoint			= "revocation_endpoint"
		case endSessionEndpoint		= "end_session_endpoint"
		case restApiTokenEndpoint	= "rest_api_token_endpoint"
		case restApiPublicEndpoint	= "rest_api_public_endpoint"
		case issuer					= "issuer"
		case walletChains_			= "wallet_chain"
		case scopesDefault			= "client_scopes_default"
		case scopesOptional			= "client_scopes_optional"
		case walletFamilies			= "wallet_family"
		case walletSources			= "wallet_source"
		case identityProviders		= "identity_providers"
	}
	//==================================================================================================
	//	isValid
	//--------------------------------------------------------------------------------------------------
	public var isValid : Bool {
		!(authEndpoint == nil
		|| tokenEndpoint == nil
		|| introspectionEndpoint == nil
		|| userInfoEndpoint == nil
		|| revokeEndpoint == nil
		|| restApiTokenEndpoint == nil
		|| restApiPublicEndpoint == nil
		|| issuer == nil
		|| scopes.count == 0)
	}
	//==================================================================================================
	//	LoadOpenIDConfiguration(ProviderInfo: ProviderInfo)
	//--------------------------------------------------------------------------------------------------
	public static func LoadOpenIDConfiguration(providerInfo:	ProviderInfo,
											   urlSession:		URLSession = URLSession.shared) async throws -> OpenIDConfiguration {
		var urlComponents		= URLComponents()
		urlComponents.scheme	= providerInfo.scheme
		urlComponents.host		= providerInfo.host
		urlComponents.port		= Int(providerInfo.port)
		urlComponents.path		= "/auth/realms/HyperID/.well-known/openid-configuration"
		do {
			let url				= urlComponents.url!
			let (data, _) = try await urlSession.data(from: url)
			guard !data.isEmpty else { throw HyperIDBaseAPIError.serverMaintenance }
			let jsonDecoder = JSONDecoder()
			do {
				let openIDConfiguration = try jsonDecoder.decode(OpenIDConfiguration.self, from: data)
				if !openIDConfiguration.isValid {
					throw HyperIDBaseAPIError.serverMaintenance
				}
				return openIDConfiguration
			} catch let error as HyperIDBaseAPIError { throw error }
			catch {
				throw HyperIDBaseAPIError.serverMaintenance
			}
		} catch let error as HyperIDBaseAPIError { throw error }
		catch {
			throw HyperIDBaseAPIError.networkingError(description: String(describing: error))
		}
	}
}
