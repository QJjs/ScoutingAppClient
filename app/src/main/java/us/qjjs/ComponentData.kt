package us.qjjs

class ComponentData {

	val data: ArrayList<JSONProperty>

	constructor(data: ArrayList<JSONProperty>) {
		this.data = data
	}

	constructor(vararg data: JSONProperty) {
		this.data = data.toCollection(ArrayList())
	}

	operator fun get(i: Int): JSONProperty? {
		return if (i > data.size) null else data[i]
	}

	operator fun get(s: String): JSONProperty? {
		val candidates = data.filter { it.name == s }
		return if (candidates.isNotEmpty()) candidates[0] else null
	}

	operator fun set(i: Int, value: JSONProperty) {
		data[i] = value
	}
}