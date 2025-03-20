import entities.Arduino
import essa.ExprLexer
import essa.ExprParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.BufferedReader
import java.io.File

fun main() {
    val charStream = readFile("test")
    val lexer = ExprLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = ExprParser(tokens)
    val parseTree = parser.config()
    val arduinoVisitor = ArduinoVisitor()
    val arduinoConfig = arduinoVisitor.visit(parseTree)

    println(arduinoConfig)

    val filename = "test"
    createSketchFile(filename, generateCode(arduinoConfig))
    println("Installing platform...")
    println(runCommand("arduino-cli core install ${arduinoConfig.board?.platform}"))
    println("Compiling sketch...")
    println(runCommand("arduino-cli compile --fqbn ${arduinoConfig.board?.platform}:${arduinoConfig.board?.type} build/sketches/$filename"))
    println("Uploading sketch...")
    println(runCommand("arduino-cli upload -p ${arduinoConfig.board?.port} --fqbn ${arduinoConfig.board?.platform}:${arduinoConfig.board?.type} build/sketches/$filename"))
}

private fun readFile(filename: String): CharStream {
    val contextClassLoader = Thread.currentThread().contextClassLoader
    return contextClassLoader.getResourceAsStream(filename).use { input ->
        CharStreams.fromStream(input)
    }
}

fun createSketchFile(fileName: String, content: String) {
    val folder = File("build/sketches/$fileName")
    if (!folder.exists()) {
        folder.mkdirs()
    }

    val sketchFile = File("build/sketches/$fileName/$fileName.ino")
    sketchFile.writeText(content)

    println("Sketch file created: ${sketchFile.absolutePath}")
}

fun runCommand(command: String): String {
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()

    return process.inputStream.bufferedReader().use(BufferedReader::readText)
}

fun generateCode(config: Arduino): String {

    var inputDefine = config.inputs.map {
        "#define ${it.name} ${it.source}"
    }.joinToString("\n")
    var inputSetup =
        config.inputs.mapNotNull {
            if (it.mode == "DIGITAL" || it.mode == "ANALOG") {
                "\tpinMode(${it.name}, INPUT);"
            } else null
        }.joinToString("\n")

    var outputSetup =
        config.outputs.mapNotNull {
            if (it.mode == "DIGITAL" || it.mode == "ANALOG") {
                "\tpinMode(${it.pin}, OUTPUT);"
            } else null
        }.joinToString("\n")

    var constants = config.constants.map {
        "#define ${it.name} ${it.value}"
    }.joinToString("\n").trimIndent()

    var signalDeclaration = config.signals.map {
        "bool ${it.name} = false;"
    }.joinToString("\n").trimIndent()

    var signalProcess = config.signals.map {
        "\t${it.name} = (${it.variableA} ${it.operand} ${it.variableB});"
    }.joinToString("\n")

    val rulesProcessing = config.rules.map {
        println(it.ruleNot)
        val condition = "${if (it.ruleNot != null) "!" else ""}${it.variable}"
        val action = "digitalWrite(${it.thenVariable}, ${if (it.state == "ON") "HIGH" else "LOW"});"
        "\tif($condition) {\n" +
            "\t\t$action\n" +
        "\t}"
    }.joinToString("\n")

    return """
$inputDefine
$constants
$signalDeclaration
void setup() {
$inputSetup
$outputSetup
}

void loop() {
$signalProcess
$rulesProcessing
}""".trimIndent()
}
