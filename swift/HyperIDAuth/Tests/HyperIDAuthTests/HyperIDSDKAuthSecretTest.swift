import XCTest
import HyperIDAuth

//**************************************************************************************************
//	HyperIDSDKAuthSecretTest
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKAuthSecretTest: HyperIDSDKAuthTestBase {
	//==================================================================================================
	//	FactoryHyperIDSDKAuth
	//--------------------------------------------------------------------------------------------------
	override func FactoryHyperIDAPIAuth() async throws -> HyperIDAuthAPI {
		try await HyperIDAuthAPI(clientInfo:	ClientInfo(clientId: 			"android-sdk-test",
														   redirectURL:			"https://localhost:4200",
														   authorizationMethod:	.clientSecret(secret:	"3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L")),
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
		let redirectURL = "https://localhost:4200/?locale=en&session_state=da84ab9b-71c5-4410-8d58-ed07e199bb98&code=429af9cf-b6d9-42ed-a3e8-4ee798fe50de.da84ab9b-71c5-4410-8d58-ed07e199bb98.e75e8ad5-37aa-40dd-af4f-31139d82d94b.0"
		try await hyperIdAuth.exchangeToTokens(redirectURL: URL(string: redirectURL)!)
		let accessToken = hyperIdAuth.accessToken
		let refreshToken = hyperIdAuth.refreshToken
		let userInfo = try await hyperIdAuth.getUserInfo()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWithIdentityProvider] accessToken:\(accessToken ?? "[fail]")\nrefreshToken:\(refreshToken ?? "[fail]")")
		print(userInfo)
		XCTAssert(hyperIdAuth.isAuthorized)
	}
}
