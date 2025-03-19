package entities


data class Arduino(
    var board: Board? = null,
    var inputs: MutableList<Inputs> = mutableListOf(),
    var outputs: MutableList<Outputs> = mutableListOf(),
    var constants: MutableList<Constants> = mutableListOf(),
    var signals: MutableList<Signals> = mutableListOf(),
    var rules: MutableList<Rules> = mutableListOf(),
    var events: MutableList<Events> = mutableListOf(),

    ) {}
