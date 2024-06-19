import Foundation

//**************************************************************************************************
//	MARK: TokenExchangeResponse
//--------------------------------------------------------------------------------------------------
class TokenExchangeResponse : Codable {
	var accessToken				: String
	var accessTokenExpiresIn	: Int
	var refreshToken			: String
	var refreshTokenExpiresIn	: Int
	var tokenType				: String
	var idToken					: String
	var notBeforePolicy			: Int
	var sessionState			: String
	var scopes					: String
	//**************************************************************************************************
	//	MARK: CodingKeys.TokenInfo
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case accessToken			= "access_token"
		case accessTokenExpiresIn	= "expires_in"
		case refreshToken			= "refresh_token"
		case refreshTokenExpiresIn	= "refresh_expires_in"
		case tokenType				= "token_type"
		case idToken				= "id_token"
		case notBeforePolicy		= "not-before-policy"
		case sessionState			= "session_state"
		case scopes					= "scope"
	}
}
