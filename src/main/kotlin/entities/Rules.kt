package entities

data class Rules(
    var variable: String,
    var ruleDo: String,
    var ruleNot: String? = null,
    var thenVariable: String,
    var state: String
)