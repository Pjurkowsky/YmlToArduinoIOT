package entities


data class Arduino(
    var board: Board? = null,
    var inputs: MutableList<Inputs> = mutableListOf(),
    var outputs: MutableList<Outputs> = mutableListOf()
) {}
