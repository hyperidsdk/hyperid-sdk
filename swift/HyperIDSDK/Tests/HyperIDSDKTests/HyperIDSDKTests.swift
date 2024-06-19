import XCTest
import HyperIDSDK

//**************************************************************************************************
//	HyperIDSDKTests
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKTests: XCTestCase {
	var hyperIdSDK : HyperIDSDK!
	//==================================================================================================
	//	setUpWithError
	//--------------------------------------------------------------------------------------------------
	override func setUpWithError() throws {
		print("[HyperIDSDKAuthTestBase] Starting async library init")
		var errorCatched : Error?
		let errorSaver = { errorCatched = $0 }
		let expectation = expectation(description: "setup hyperIdAuth")
		Task {
			do {
				let clientInfo = ClientInfo(clientId:				"android-sdk-test",
											redirectURL:			"https://localhost:4200",
											authorizationMethod:	.clientSecret(secret: "3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L"))
				hyperIdSDK = try await HyperIDSDK(clientInfo:		clientInfo,
												  authRestoreInfo: "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhMzQ3MzcyZS1mNjkwLTRiNmMtODQ4Yi0yY2I3NjM0NDdhNTMifQ.eyJleHAiOjE3MjQ5NDA3NjIsImlhdCI6MTcxNzE2NjMzMywianRpIjoiNTk4YWI1ZWYtOWIzNy00NTI4LWEyODQtYTc2ZmZjOGVkZDJiIiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi1zdGFnZS5oeXBlcnNlY3VyZWlkLmNvbS9hdXRoL3JlYWxtcy9IeXBlcklEIiwiYXVkIjoiaHR0cHM6Ly9sb2dpbi1zdGFnZS5oeXBlcnNlY3VyZWlkLmNvbS9hdXRoL3JlYWxtcy9IeXBlcklEIiwic3ViIjoiYWJlNDM1ODctOTFlZC00ZDczLTgxMGYtN2RiMjVkYjM1NzExIiwidHlwIjoiUmVmcmVzaCIsImF6cCI6ImFuZHJvaWQtc2RrLXRlc3QiLCJzZXNzaW9uX3N0YXRlIjoiNzY4OGFjOWItYjk4MC00OThmLWEzMDEtYTZjMGJhZTA2MTlkIiwicmVnaW9uX2lkIjowLCJzY29wZSI6Im9wZW5pZCB1c2VyLWRhdGEtc2V0IGt5Yy12ZXJpZmljYXRpb24ga2V5cyB1c2VyLWluZm8tZ2V0IGVtYWlsIHVzZXItd2FsbGV0LXB1YmxpYy1kYXRhIHNlY29uZC1mYWN0b3ItYXV0aC1jbGllbnQgYXV0aCBtZmEtY2xpZW50IHVzZXItZGF0YS1nZXQgd2ViLWNoYXQiLCJzaWQiOiI3Njg4YWM5Yi1iOTgwLTQ5OGYtYTMwMS1hNmMwYmFlMDYxOWQifQ.xyd17qR1-K7xmH1vfbeNhlRKu5pvk0sHl-6wmQcx5QM",
												  authRestoreInfoUpdateCallback: { authRestoreInfo in
					print("[HyperIDSDKAuthSecretTest][testAuthorizationStartWithIdentityProvider] refreshToken:\(authRestoreInfo ?? "nil")")
				},
												  providerInfo:		ProviderInfo.sandbox,
												  urlSession:		URLSession.shared)
			} catch {
				errorSaver(error)
			}
			expectation.fulfill()
			print("[HyperIDSDKAuthTestBase] async library init finished")
		}
		wait(for: [expectation])
		if let errorCatched = errorCatched
		{
			print("[HyperIDSDKAuthTestBase] init error occured: \(errorCatched.localizedDescription)")
		}
		else
		{
			print("[HyperIDSDKAuthTestBase] Starting test")
		}
	}
	//==================================================================================================
	//	testAuthorizationStartSignIn
	//--------------------------------------------------------------------------------------------------
	func testAuthorizationStartSignIn() throws {
		let url = try hyperIdSDK.startSignInWeb2()
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartSignIn] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testAuthorizationComplete
	//--------------------------------------------------------------------------------------------------
	func testCompleteSignIn() async throws {
		let redirectURL = "https://localhost:4200/?locale=en&session_state=e143f85c-afb3-44be-86fe-f9f41a0bf68b&code=1a51e1f7-ee0d-4242-8b89-fb49421f4f51.e143f85c-afb3-44be-86fe-f9f41a0bf68b.e75e8ad5-37aa-40dd-af4f-31139d82d94b.0"
		try await hyperIdSDK.completeSignIn(redirectURL: URL(string: redirectURL)!)
		let userInfo = try await hyperIdSDK.getUserInfo()
		XCTAssert(hyperIdSDK.isAuthorized)
	}
	//==================================================================================================
	//	testAuthorizationComplete
	//--------------------------------------------------------------------------------------------------
	func testAuthorizationStartSignInTransaction() async throws {
		let url = try hyperIdSDK.startSignInWithTransaction(from:	"0x43D192d3eC9CaEFbc92385bED8508d87E566595f",
															to:		"0x0AeB980AB115E45409D9bA33CCffcc75995E3dfA",
															chain:	"11155111",
															data:	"0x0",
															nonce:	"0",
															value:	"0x1")
		print("[HyperIDSDKAuthSecretTest][testAuthorizationStartSignIn] url:\(url.absoluteString)")
	}
	//==================================================================================================
	//	testCompleteSignInTransaction
	//--------------------------------------------------------------------------------------------------
	func testCompleteSignInTransaction() async throws {
		let redirectURL = "https://localhost:4200/?transaction_hash=0xa48ac72ff8b4a21a14144b5b38098890706c50e2ed3485d7e0d1c5a00476e3cc&transaction_result=0&transaction_result_description=success&locale=en&session_state=7688ac9b-b980-498f-a301-a6c0bae0619d&code=42393afc-3022-4b78-b403-2bdc2b87eb28.7688ac9b-b980-498f-a301-a6c0bae0619d.e75e8ad5-37aa-40dd-af4f-31139d82d94b.0"
		let hash = try await hyperIdSDK.completeSignInWithTransaction(redirectURL: URL(string: redirectURL)!)
		print("[HyperIDSDKAuthSecretTest][testCompleteSignInTransaction] hash:\(hash)")
		let userInfo = try await hyperIdSDK.getUserInfo()
		XCTAssert(hyperIdSDK.isAuthorized)
	}
	func testWalletsGet() async throws
	{
		let wallets = try await hyperIdSDK.getUserWallets()
		print("s")
	}
	//==================================================================================================
	//	testGetUserInfo
	//--------------------------------------------------------------------------------------------------
	func testGetUserInfo() async throws {
		let userInfo = try await hyperIdSDK.getUserInfo()
		let string = String(data: try JSONEncoder().encode(userInfo), encoding: .utf8)!
		XCTAssert(!string.isEmpty)
	}
	//==================================================================================================
	//	testLogout
	//--------------------------------------------------------------------------------------------------
	func testLogout() async throws {
		try await hyperIdSDK.signOut()
		XCTAssert(!hyperIdSDK.isAuthorized)
	}
	//==================================================================================================
	//	testUserKYCStatusInfoGet
	//--------------------------------------------------------------------------------------------------
	func testUserKYCStatusInfoGet() async throws {
		let result = try await hyperIdSDK.getUserKYCStatusInfo(kycVerificationLevel: .full)
		print(result!)
	}
	//==================================================================================================
	//	testUserKYCStatusTopLevelInfoGet
	//--------------------------------------------------------------------------------------------------
	func testUserKYCStatusTopLevelInfoGet() async throws {
		let result = try await hyperIdSDK.getUserKYCStatusTopLevelInfo()
	}
	//==================================================================================================
	//	testGetStorageKeys
	//--------------------------------------------------------------------------------------------------
	func testGetStorageKeys() async throws {
		var result = try await hyperIdSDK.getUserKeysList(storage: .email)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdSDK.getUserKeysList(storage: .userID)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdSDK.getUserKeysList(storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"))
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdSDK.getUserKeysList(storage: .identityProvider("google"))
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
	}
	//==================================================================================================
	//	testSetStorageData
	//--------------------------------------------------------------------------------------------------
	func testSetStorageData() async throws {
		try await hyperIdSDK.setUserData((key: "testKeyPrivateEmail", value: "testValuePrivate"), dataScope: .private, storage: .email)
		try await hyperIdSDK.setUserData((key: "testKeyPublicEmail", value: "testValuePublic"), dataScope: .public, storage: .email)
		try await hyperIdSDK.setUserData((key: "testKeyPrivateUserId", value: "testValuePrivate"), dataScope: .private, storage: .userID)
		try await hyperIdSDK.setUserData((key: "testKeyPublicUserId", value: "testValuePublic"), dataScope: .public, storage: .userID)
		try await hyperIdSDK.setUserData((key: "testKeyPrivateWallet", value: "testValuePrivate"), dataScope: .private, storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"))
		try await hyperIdSDK.setUserData((key: "testKeyPublicWallet", value: "testValuePublic"), dataScope: .public, storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"))
		try await hyperIdSDK.setUserData((key: "testKeyPrivateGoogle1", value: "testValuePrivate"), dataScope: .private, storage: .identityProvider("google"))
		try await hyperIdSDK.setUserData((key: "testKeyPublicGoogle1", value: "testValuePublic"), dataScope: .public, storage: .identityProvider("google"))
	}
	//==================================================================================================
	//	testGetStorageData
	//--------------------------------------------------------------------------------------------------
	func testGetStorageData() async throws {
		var string = try await hyperIdSDK.getUserData("testKeyPrivateEmail", storage: .email)
		print(string)
		string = try await hyperIdSDK.getUserData("testKeyPrivateUserId", storage: .userID)
		print(string)
		string = try await hyperIdSDK.getUserData("testKeyPrivateWallet", storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"))
		print(string)
		string = try await hyperIdSDK.getUserData("testKeyPublicGoogle", storage: .identityProvider("google"))
		print(string)
	}
	//==================================================================================================
	//	testDeleteStorageData
	//--------------------------------------------------------------------------------------------------
	func testDeleteStorageData() async throws {
		try await hyperIdSDK.deleteUserData("testKeyPrivateEmail", storage: .email)
		try await hyperIdSDK.deleteUserData("testKeyPrivateUserId", storage: .userID)
		try await hyperIdSDK.deleteUserData("testKeyPrivateWallet", storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"))
		try await hyperIdSDK.deleteUserData("testKeyPublicGoogle1", storage: .identityProvider("google"))
	}
	//==================================================================================================
	//	testCheckAvaibility
	//--------------------------------------------------------------------------------------------------
	func testCheckAvaibility() async throws {
		let result = try await hyperIdSDK.checkMFAAvailability()
		print(result)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testStartMFATransaction() async throws {
		let transactionId = try await hyperIdSDK.startMFATransaction(question: "Will you marry pig?", controlCode: 23)
		print(transactionId)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testGetMFATransactionStatus() async throws {
		let status = try await hyperIdSDK.getMFATransactionStatus(transactionId: 12150)
		print(status!)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testCancelMFATransaction() async throws{
		try await hyperIdSDK.cancelMFATransaction(transactionId: 12150)
	}
}
