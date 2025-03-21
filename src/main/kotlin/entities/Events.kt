package entities

data class Events(
    var id: Int,
    var variableA: String? = null,
    var operand: String? = null,
    var variableB: String? = null,
    var variableC: String? = null,
    var state: String? = null
)
