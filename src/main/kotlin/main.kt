import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import essa.ExprLexer
import essa.ExprParser

fun main() {
    val charStream = readFile("test")
    val lexer = ExprLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = ExprParser(tokens)
    println(parser.program().toStringTree())
}

private fun readFile(filename: String): CharStream {
    val contextClassLoader = Thread.currentThread().contextClassLoader
    return contextClassLoader.getResourceAsStream(filename).use { input ->
        CharStreams.fromStream(input)
    }
}