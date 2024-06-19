import XCTest
import HyperIDAuth

//**************************************************************************************************
//	HyperIDSDKAuthAccountTest
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKAuthAccountTest: HyperIDSDKAuthTestBase {
	//==================================================================================================
	//	FactoryHyperIDSDKAuth
	//--------------------------------------------------------------------------------------------------
	override func FactoryHyperIDAPIAuth() async throws -> HyperIDAuthAPI {
		return try await HyperIDAuthAPI(clientInfo:		ClientInfo(clientId: 			"HyperID-Authenticator",
																   redirectURL:			"oauth.com.hyperid.client://localhost:4200/auth/hyper-id/callback",
																   authorizationMethod:	.clientSecret(secret:	"1056f934-40b0-49bc-8777-aaafdf74dc49")),
										refreshTokenUpdateCallback: { refreshToken in
			
		},
										providerInfo:	ProviderInfo.stage)
	}
	//==================================================================================================
	//	testAccessToken
	//--------------------------------------------------------------------------------------------------
	func testRefreshToken() async throws {
		try await hyperIdAuth.refreshTokens()
		XCTAssert(!(hyperIdAuth.accessToken?.isEmpty ?? true))
		XCTAssert(!(hyperIdAuth.refreshToken?.isEmpty ?? true))
	}
	//==================================================================================================
	//	testUserInfoGet
	//--------------------------------------------------------------------------------------------------
	func testUserInfoGet() async throws {
		let userInfo = try await hyperIdAuth.getUserInfo()
		print(userInfo)
	}
	//==================================================================================================
	//	logout()
	//--------------------------------------------------------------------------------------------------
	func testLogout() async throws {
		try await hyperIdAuth.logout()
	}
}
