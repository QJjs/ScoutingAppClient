package us.qjjs

class JSONProperty(var name: String, var value: Any) {
	override fun toString(): String {
		return "$name: $value"
	}
}