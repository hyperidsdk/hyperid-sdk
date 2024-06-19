// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

//**************************************************************************************************
//	MARK: Package
//--------------------------------------------------------------------------------------------------
let package = Package(
	name: "HyperIDAuth",
	platforms: [
		.macOS(.v12), .iOS(.v15), .tvOS(.v15)
	],
	products: [
		// Products define the executables and libraries a package produces, making them visible to other packages.
		.library(
			name: "HyperIDAuth",
			targets: ["HyperIDAuth"]),
	],
	dependencies: [
		//TODO: after publishing replace previous dependency with url to the package
		.package(name: "HyperIDBase", path: "../HyperIDBase"),
		.package(name: "SwiftJWT", url: "https://github.com/Kitura/Swift-JWT", from: "3.6.201"),
	],
	targets: [
		// Targets are the basic building blocks of a package, defining a module or a test suite.
		// Targets can depend on other targets in this package and products from dependencies.
		.target(
			name: "HyperIDAuth", dependencies: [
				"HyperIDBase",
				"SwiftJWT",
			]),
		.testTarget(
			name: "HyperIDAuthTests",
			dependencies: ["HyperIDAuth"]),
	]
)
