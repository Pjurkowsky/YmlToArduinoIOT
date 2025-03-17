import essa.ExprBaseVisitor
import essa.ExprParser

class ArduinoVisitor : ExprBaseVisitor<String>() {

    override fun visitBoardPort(ctx: ExprParser.BoardPortContext?): String {
        return ctx?.DEVICE_PATH()?.text ?: throw Error("dupa")
    }
    override fun visitBoardType(ctx: ExprParser.BoardTypeContext?): String {
        return ctx?.FQBN()?.text ?: throw Error("dupa")
    }
    override fun visitBoardPlatform(ctx: ExprParser.BoardPlatformContext?): String {
        return ctx?.FQBN()?.text ?: throw Error("dupa")
    }
}

