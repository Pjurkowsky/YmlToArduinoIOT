import entities.Arduino
import essa.ExprLexer
import essa.ExprParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.stringtemplate.v4.STGroupFile
import java.io.BufferedReader
import java.io.File


fun main() {
    val charStream = readFile("test.yml")
    val lexer = ExprLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = ExprParser(tokens)
    val parseTree = parser.config()
    val arduinoVisitor = ArduinoVisitor()
    val arduinoConfig = arduinoVisitor.visit(parseTree)

    println(arduinoConfig)

    val filename = "test.yml"
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

fun generateCode(arduinoConfig: Arduino): String {
    val group = STGroupFile("templates/cpp.stg")
    val template = group.getInstanceOf("arduino")
    template.add("arduinoConfig", arduinoConfig)
    template.add("analogOutputs", arduinoConfig.outputs.filter { it.mode == "ANALOG" })
    template.add("analogInputs", arduinoConfig.inputs.filter { it.mode == "ANALOG" })
    template.add("digitalInputs", arduinoConfig.inputs.filter { it.mode == "DIGITAL" })
    template.add("digitalOutputs", arduinoConfig.outputs.filter { it.mode == "DIGITAL" })

    return template.render()
}
