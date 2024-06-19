import Foundation
import SwiftJWT

//**************************************************************************************************
//	MARK: ClientInfo
//--------------------------------------------------------------------------------------------------
public struct ClientInfo {
	public let	clientId				: String
	public let	redirectURL				: String
	public let	authorizationMethod		: AuthorizationMethod
	public let	scopes					: [String]?
	
	//**************************************************************************************************
	//	MARK: AuthorizationMethod
	//--------------------------------------------------------------------------------------------------
	public enum AuthorizationMethod {
		case clientSecret(secret : String)
		case clientHS256(secret : String)
		case clientRS256(privateKey : Data)
		
		//==================================================================================================
		//	algorithmName
		//--------------------------------------------------------------------------------------------------
		var algorithmName : String {
			switch self {
			case .clientSecret(_):
				return "HA256"
			case .clientHS256(_):
				return "HS256"
			case .clientRS256(_):
				return "RS256"
			}
		}
	}
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public init(clientId: String, redirectURL: String, authorizationMethod: AuthorizationMethod, scopes: [String]? = nil) {
		self.clientId				= clientId
		self.redirectURL			= redirectURL
		self.authorizationMethod	= authorizationMethod
		self.scopes					= scopes
	}
	//==================================================================================================
	//	isValid
	//--------------------------------------------------------------------------------------------------
	public var isValid					: Bool {
		let isValid = !(clientId.isEmpty
					  || redirectURL.isEmpty)
		switch authorizationMethod {
		case .clientSecret(let secret):
			return isValid && !secret.isEmpty
		case .clientHS256(let secret):
			return isValid && !secret.isEmpty
		case .clientRS256(let privateKey):
			return isValid && !privateKey.isEmpty
		}
	}
	//==================================================================================================
	//	authorizationParameters
	//--------------------------------------------------------------------------------------------------
	func authorizationParameters(issuer: URL) throws -> String {
		var authorizationParameters = "redirect_uri=\(redirectURL)&"
		switch authorizationMethod {
		case .clientSecret(let secret):
			authorizationParameters += "client_id=\(clientId)&client_secret=\(secret)"
		case .clientHS256(let secret):
			do {
				let secretData = secret.data(using: .utf8)!
				let signer = JWTSigner.hs256(key: secretData)
				var jwt = generateAssertionToken(issuer: issuer)
				let signedJWT = try jwt.sign(using: signer)
				authorizationParameters += "client_assertion=\(signedJWT)&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
			} catch {
				throw HyperIDAuthAPIError.assertionTokenSignError(description: error.localizedDescription)
			}
		case .clientRS256(let privateKey):
			let signer = JWTSigner.rs256(privateKey: privateKey)
			var jwt = generateAssertionToken(issuer: issuer)
			do {
				let signedJWT = try jwt.sign(using: signer)
				authorizationParameters += "client_assertion=\(signedJWT)&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
			} catch {
				throw HyperIDAuthAPIError.assertionTokenSignError(description: error.localizedDescription)
			}
		}
		return authorizationParameters
	}
	//==================================================================================================
	//	generateAssertionToken
	//--------------------------------------------------------------------------------------------------
	private func generateAssertionToken(issuer: URL) -> JWT<ClaimsHyperID> {
		let jwtHeader = Header()
		let jwtClaim = ClaimsHyperID(jti: UUID().uuidString,
									 iss: clientId,
									 sub: clientId,
									 aud: issuer.absoluteString,
									 iat: Date.now,
									 exp: Date.now.addingTimeInterval(120))
		let jwt = JWT(header: jwtHeader,
					  claims: jwtClaim)
		return jwt
	}
}

//**************************************************************************************************
//	MARK: ClaimsHyperId
//--------------------------------------------------------------------------------------------------
class ClaimsHyperID : Claims {
	var jti : String
	var iss : String
	var sub : String
	var aud : String
	var iat : Date
	var exp : Date
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(jti: String, iss: String, sub: String, aud: String, iat: Date, exp: Date) {
		self.jti = jti
		self.iss = iss
		self.sub = sub
		self.aud = aud
		self.iat = iat
		self.exp = exp
	}
}
