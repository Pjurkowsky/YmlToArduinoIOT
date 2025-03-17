import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import essa.ExprLexer
import essa.ExprParser
import java.io.BufferedReader
import java.io.File

fun main() {
    val charStream = readFile("test")
    val lexer = ExprLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = ExprParser(tokens)
    val parseTree = parser.config()
    val visitor = ArduinoVisitor()

    val platform = visitor.visit(parseTree.boardDecl().boardPlatform())
    val type = visitor.visit(parseTree.boardDecl().boardType())
    val port = visitor.visit(parseTree.boardDecl().boardPort())
    println(platform)
    println(type)
    println(port)

    val basicCode = """
        void setup() {}
        void loop() {}
    """.trimIndent()

    val filename = "test"
    createSketchFile(filename, basicCode)

    println("Installing platform...")
    println(runCommand("arduino-cli core install $platform"))
    println("Compiling sketch...")
    println(runCommand("arduino-cli compile --fqbn $platform:$type build/sketches/$filename"))
    println("Uploading sketch...")
    println(runCommand("arduino-cli upload -p $port --fqbn $platform:$type build/sketches/$filename"))
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

