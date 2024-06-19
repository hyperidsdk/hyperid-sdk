import XCTest
import HyperIDSDK

//**************************************************************************************************
//	HyperIDSDKKYCUserStatusGetTest
//--------------------------------------------------------------------------------------------------
final class HyperIDSDKStorageTest : XCTestCase {
	var hyperIdStorage	: HyperIDStorageAPI!
	var accessToken 	: String = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhMzQ3MzcyZS1mNjkwLTRiNmMtODQ4Yi0yY2I3NjM0NDdhNTMifQ.eyJleHAiOjE2OTk2NDM0MDUsImlhdCI6MTY5OTYzOTgwNSwiYXV0aF90aW1lIjoxNjk5NjM5NzY4LCJqdGkiOiJjYjBjODZmMS0xYWIwLTQ5MzEtOGIxMC02YTRlY2U0ZTA3Y2EiLCJpc3MiOiJodHRwczovL2xvZ2luLXN0YWdlLmh5cGVyc2VjdXJlaWQuY29tL2F1dGgvcmVhbG1zL0h5cGVySUQiLCJzdWIiOiJhYmU0MzU4Ny05MWVkLTRkNzMtODEwZi03ZGIyNWRiMzU3MTEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhbmRyb2lkLXNkay10ZXN0Iiwic2Vzc2lvbl9zdGF0ZSI6ImRhODRhYjliLTcxYzUtNDQxMC04ZDU4LWVkMDdlMTk5YmI5OCIsInJlZ2lvbl9pZCI6MCwic2NvcGUiOiJvcGVuaWQgdXNlci1kYXRhLXNldCBreWMtdmVyaWZpY2F0aW9uIGtleXMgdXNlci1pbmZvLWdldCBlbWFpbCBzZWNvbmQtZmFjdG9yLWF1dGgtY2xpZW50IGF1dGggbWZhLWNsaWVudCB1c2VyLWRhdGEtZ2V0IHdlYi1jaGF0Iiwic2lkIjoiZGE4NGFiOWItNzFjNS00NDEwLThkNTgtZWQwN2UxOTliYjk4IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6IjE3OWVhOGNhYjE3MmRjOTg5NGNkZDU0NmRlNzM4MjdjOWExNGZlOTI0YWQ4Mzk3NjJmZDI1NTliNDYxZjQ0ZjAiLCJpcCI6IjMxLjEyOC4xNjIuODMiLCJkZXZpY2VfZGVzYyI6eyJjcHUiOnsiYXJjaGl0ZWN0dXJlIjoidW5rbm93biIsImNvcmVzIjo4fSwib3MiOnsidmVyc2lvbiI6Ik9TIFggMTBfMTVfNyIsInRpbWV6b25lIjotMTIwLCJ0eXBlIjoiRGVza3RvcCIsImRldmljZSI6eyJ2ZW5kb3IiOiJ1bmtub3duIiwibW9kZWwiOiJ1bmtub3duIn19LCJzY3JlZW4iOnsiZGVwdGgiOjI0LCJyYXRpbyI6MSwid2lkdGgiOjE5MjAsImhlaWdodCI6MTA4MCwic2l6ZSI6MjA3MzYwMH0sImJyb3dzZXIiOnsibmFtZSI6IlNhZmFyaSIsInZlcnNpb24iOiI2MDUuMS4xNSJ9LCJhZ2VudCI6Ik1vemlsbGEvNS4wIChNYWNpbnRvc2g7IEludGVsIE1hYyBPUyBYIDEwXzE1XzcpIEFwcGxlV2ViS2l0LzYwNS4xLjE1IChLSFRNTCwgbGlrZSBHZWNrbykgVmVyc2lvbi8xNy4xIFNhZmFyaS82MDUuMS4xNSJ9LCJlbWFpbCI6ImFuZHJpaS5idXRva0BsaXN0YXQuY29tLnVhIn0.6d9tD0qwAlVfzf730bLd1mFTXYJYOjEETsp_OW5WBYw"
	//==================================================================================================
	//	setUpWithError
	//--------------------------------------------------------------------------------------------------
	override func setUpWithError() throws {
		print("[HyperIDSDKKYCUserStatusGetTest] Starting async library init")
		var errorCatched : Error?
		let errorSaver = { errorCatched = $0 }
		let expectation = expectation(description: "setup hyperIdAuth")
		Task {
			do {
				hyperIdStorage = try await HyperIDStorageAPI(providerInfo: ProviderInfo.stage)
			} catch {
				errorSaver(error)
			}
			expectation.fulfill()
			print("[HyperIDSDKKYCUserStatusGetTest] async library init finished")
		}
		wait(for: [expectation])
		if let errorCatched = errorCatched
		{
			print("[HyperIDSDKKYCUserStatusGetTest] init error occured: \(errorCatched.localizedDescription)")
		}
		else
		{
			print("[HyperIDSDKKYCUserStatusGetTest] Starting test")
		}
	}
	//==================================================================================================
	//	testGetStorageKeys
	//--------------------------------------------------------------------------------------------------
	func testGetStorageKeys() async throws {
		var result = try await hyperIdStorage.getUserKeysList(storage: .email, accessToken: accessToken)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdStorage.getUserKeysList(storage: .userID, accessToken: accessToken)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdStorage.getUserKeysList(storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
		result = try await hyperIdStorage.getUserKeysList(storage: .identityProvider("google"), accessToken: accessToken)
		print("private: \(result.keysPrivate)")
		print("public: \(result.keysPublic)")
	}
	//==================================================================================================
	//	testGetStorageKeys
	//--------------------------------------------------------------------------------------------------
	func testGetStorageSharedKeys() async throws {
		
		var result = try await hyperIdStorage.getUserSharedKeysList(storage: .email, accessToken: accessToken)
		print("public shared: \(result)")
		result = try await hyperIdStorage.getUserSharedKeysList(storage: .userID, accessToken: accessToken)
		print("public shared: \(result)")
		result = try await hyperIdStorage.getUserSharedKeysList(storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		print("public shared: \(result)")
		result = try await hyperIdStorage.getUserSharedKeysList(storage: .identityProvider("google"), accessToken: accessToken)
		print("public shared: \(result)")
	}
	//==================================================================================================
	//	testSetStorageData
	//--------------------------------------------------------------------------------------------------
	func testSetStorageData() async throws {
		try await hyperIdStorage.setUserData((key: "testKeyPrivateEmail", value: "testValuePrivate"), dataScope: .private, storage: .email, accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPublicEmail", value: "testValuePublic"), dataScope: .public, storage: .email, accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPrivateUserId", value: "testValuePrivate"), dataScope: .private, storage: .userID, accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPublicUserId", value: "testValuePublic"), dataScope: .public, storage: .userID, accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPrivateWallet", value: "testValuePrivate"), dataScope: .private, storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPublicWallet", value: "testValuePublic"), dataScope: .public, storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPrivateGoogle1", value: "testValuePrivate"), dataScope: .private, storage: .identityProvider("google"), accessToken: accessToken)
		try await hyperIdStorage.setUserData((key: "testKeyPublicGoogle1", value: "testValuePublic"), dataScope: .public, storage: .identityProvider("google"), accessToken: accessToken)
	}
	//==================================================================================================
	//	testGetStorageData
	//--------------------------------------------------------------------------------------------------
	func testGetStorageData() async throws {
		var string = try await hyperIdStorage.getUserData("testKeyPrivateEmail", storage: .email, accessToken: accessToken)
		print(string)
		string = try await hyperIdStorage.getUserData("testKeyPrivateUserId", storage: .userID, accessToken: accessToken)
		print(string)
		string = try await hyperIdStorage.getUserData("testKeyPrivateWallet", storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		print(string)
		string = try await hyperIdStorage.getUserData("testKeyPublicGoogle", storage: .identityProvider("google"), accessToken: accessToken)
		print(string)
	}
	//==================================================================================================
	//	testDeleteStorageData
	//--------------------------------------------------------------------------------------------------
	func testDeleteStorageData() async throws {
		try await hyperIdStorage.deleteUserData("testKeyPrivateEmail", storage: .email, accessToken: accessToken)
		try await hyperIdStorage.deleteUserData("testKeyPrivateUserId", storage: .userID, accessToken: accessToken)
		try await hyperIdStorage.deleteUserData("testKeyPrivateWallet", storage: .wallet(address: "0xc8abaF03F2dD39A344a412478164cB3FA2dd5D0a"), accessToken: accessToken)
		try await hyperIdStorage.deleteUserData("testKeyPublicGoogle1", storage: .identityProvider("google"), accessToken: accessToken)
	}
}
