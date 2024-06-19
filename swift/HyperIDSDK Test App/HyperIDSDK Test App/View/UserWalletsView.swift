import SwiftUI
import HyperIDSDK

//**************************************************************************************************
//	MARK: UserWalletsView
//--------------------------------------------------------------------------------------------------
struct UserWalletsView: View {
	var walletsPublic 	: [Wallet]?
	var walletsPrivate	: [Wallet]?
	
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			if let walletsPrivate = walletsPrivate,
			   walletsPrivate.count > 0 {
				Section(header: Text("Private")) {
					ForEach(walletsPrivate) {
						DisplayItemView(title: $0.address, value: "")
					}
				}
			}
			if let walletsPublic = walletsPublic,
			   walletsPublic.count > 0 {
				Section(header: Text("Public")) {
					ForEach(walletsPublic) {
						DisplayItemView(title: $0.address, value: "")
					}
				}
			}
		}
	}
}
