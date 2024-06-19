import XCTest
import HyperIDAuth

//**************************************************************************************************
//	HyperIDSDKAuthRS256Test
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKAuthRS256Test: HyperIDSDKAuthTestBase {
	//==================================================================================================
	//	FactoryHyperIDSDKAuth
	//--------------------------------------------------------------------------------------------------
	override func FactoryHyperIDAPIAuth() async throws -> HyperIDAuthAPI {
		let certName = "rs256Certificate"
		let bundle = Bundle(for: HyperIDSDKAuthRS256Test.self)
		let path = bundle.path(forResource: "rs256Certificate", ofType: "p12")
		let p12Data : NSData = try! NSData(contentsOf: NSURL(fileURLWithPath: path!) as URL)
		let options : NSDictionary = [kSecImportExportPassphrase : "111111"]
		var pkRef : SecKey? = nil
		var items : CFArray?
		let error = SecPKCS12Import(p12Data, options, &items)
		guard let items = items, error == noErr, CFArrayGetCount(items) > 0 else { throw HyperIDAuthAPIError.invalidClientInfo() }
		let array = items as [AnyObject] as NSArray
		let dictionary = array[0] as! NSDictionary
		let secIdentity = dictionary[kSecImportItemIdentity] as! SecIdentity
		let errorIdentitiyKeyGet = SecIdentityCopyPrivateKey(secIdentity,&pkRef)
		var err : Unmanaged<CFError>?
		let keyData = SecKeyCopyExternalRepresentation(pkRef!, &err)! as Data
		return try await HyperIDAuthAPI(clientInfo:		ClientInfo(clientId: 			"android-sdk-test-rsa",
																   redirectURL:			"https://localhost:4200",
																   authorizationMethod:	.clientRS256(privateKey: keyData)),
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
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWithIdentityProvider] accessToken:\(accessToken)\nrefreshToken:\(refreshToken)")
		XCTAssert(hyperIdAuth.isAuthorized)
	}
}
