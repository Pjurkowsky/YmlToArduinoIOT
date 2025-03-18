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
    var inputSetup =
        config.inputs.mapNotNull {
            if (it.mode == "DIGITAL" || it.mode == "ANALOG") {
                "pinMode(${it.source}, INPUT);"
            } else null
        }.joinToString("\n")

    var outputSetup =
        config.outputs.mapNotNull {
            if (it.mode == "DIGITAL" || it.mode == "ANALOG") {
                "pinMode(${it.pin}, OUTPUT);"
            } else null
        }.joinToString("\n")


    return """
        void setup() {
        $inputSetup
        $outputSetup
        }

        void loop() {
        }
    """.trimIndent()
}
