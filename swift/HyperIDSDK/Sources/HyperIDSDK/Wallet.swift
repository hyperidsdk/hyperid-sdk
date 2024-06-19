import Foundation

//**************************************************************************************************
//	MARK: Wallet
//--------------------------------------------------------------------------------------------------
public struct Wallet : Codable, Identifiable
{
	public var id		: String { address }
	public var address	: String
	public var chain	: String
	public var family	: Int
	public var label	: String
	//**************************************************************************************************
	//	Wallet.CodingKeys
	//--------------------------------------------------------------------------------------------------
	private enum CodingKeys : String, CodingKey {
		case address	= "address"
		case chain		= "chain"
		case family		= "family"
		case label		= "label"
	}
}
