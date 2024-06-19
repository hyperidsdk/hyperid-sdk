import SwiftUI

//**************************************************************************************************
//	MARK: StorageKeysView
//--------------------------------------------------------------------------------------------------
struct StorageKeysView: View {
	var privateKeys	: [String]?
	var publicKeys	: [String]?
	var sharedKeys	: [String]?
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			if let privateKeys = privateKeys,
			   privateKeys.count > 0 {
				Section(header: Text("Private")) {
					ForEach(privateKeys, id: \.self) {
						DisplayItemView(title: $0, value: "")
					}
				}
			}
			if let publicKeys = publicKeys,
			   publicKeys.count > 0 {
				Section(header: Text("Public")) {
					ForEach(publicKeys, id: \.self) {
						DisplayItemView(title: $0, value: "")
					}
				}
			}
			if let sharedKeys = sharedKeys,
			   sharedKeys.count > 0 {
				Section(header: Text("Shared")) {
					ForEach(sharedKeys, id: \.self) {
						DisplayItemView(title: $0, value: "")
					}
				}
			}
		}
	}
}
