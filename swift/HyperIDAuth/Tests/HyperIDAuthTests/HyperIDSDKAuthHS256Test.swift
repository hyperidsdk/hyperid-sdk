import XCTest
import HyperIDAuth

//**************************************************************************************************
//	HyperIDSDKAuthSecretHS256Test
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKAuthHS256Test: HyperIDSDKAuthTestBase {
	//==================================================================================================
	//	FactoryHyperIDSDKAuth
	//--------------------------------------------------------------------------------------------------
	override func FactoryHyperIDAPIAuth() async throws -> HyperIDAuthAPI {
		try await HyperIDAuthAPI(clientInfo:	ClientInfo(clientId: 			"android-sdk-test-hs",
														   redirectURL:			"https://localhost:4200",
														   authorizationMethod:	.clientHS256(secret:	"c9prKcovIJdEzofVe2tNgZlwW3rSDEdF")),
								 refreshTokenUpdateCallback: { refreshToken in
		},
								 providerInfo:	ProviderInfo.sandbox)
	}
	//==================================================================================================
	//	testStartSignInWeb2
	//--------------------------------------------------------------------------------------------------
	func testStartSignInWeb2() throws {
		let url = try hyperIdAuth.startSignInWeb2()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartSignIn] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testStartSignInWeb3
	//--------------------------------------------------------------------------------------------------
	func testStartSignInWeb3() throws {
		let url = try hyperIdAuth.startSignInWeb3()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartSignToSignIn] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testStartSignInUsingWallet
	//--------------------------------------------------------------------------------------------------
	func testStartSignInUsingWallet() throws {
		let url = try hyperIdAuth.startSignInUsingWallet()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWalletGet] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testStartSignInGuestUpgrade
	//--------------------------------------------------------------------------------------------------
	func testStartSignInGuestUpgrade() throws {
		let url = try hyperIdAuth.startSignInGuestUpgrade()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartGuestUpgrade] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testStartSignInIdentityProvider
	//--------------------------------------------------------------------------------------------------
	func testStartSignInIdentityProvider() throws {
		let url = try hyperIdAuth.startSignInIdentityProvider(identityProvider: .google)
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWithIdentityProvider] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testExchangeToTokens
	//--------------------------------------------------------------------------------------------------
	func testExchangeToTokens() async throws {
		let redirectURL = "https://localhost:4200/?locale=en&session_state=73f78a78-9d12-4f2d-bccf-4d600df63d6b&code=b4db352f-87ff-491c-ba5a-6aea2a47ea6b.73f78a78-9d12-4f2d-bccf-4d600df63d6b.e75e8ad5-37aa-40dd-af4f-31139d82d94b.0"
		try await hyperIdAuth.exchangeToTokens(redirectURL: URL(string: redirectURL)!)
		let accessToken = hyperIdAuth.accessToken
		let refreshToken = hyperIdAuth.refreshToken
		let userInfo = try await hyperIdAuth.getUserInfo()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWithIdentityProvider] accessToken:\(accessToken ?? "[fail]")\nrefreshToken:\(refreshToken ?? "[fail]")")
		print("\(userInfo)")
		XCTAssert(hyperIdAuth.isAuthorized)
	}
}
