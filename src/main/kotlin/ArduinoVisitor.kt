import entities.Arduino
import entities.Board
import entities.Inputs
import entities.Outputs
import essa.ExprBaseVisitor
import essa.ExprParser

class ArduinoVisitor : ExprBaseVisitor<Arduino>() {
    var arduino: Arduino = Arduino()

    override fun visitConfig(ctx: ExprParser.ConfigContext?): Arduino {
        if (ctx != null) {
            for (section in ctx.section()) {
                visit(section)
            }
        }
        return arduino
    }

    override fun visitBoardDecl(ctx: ExprParser.BoardDeclContext?): Arduino {
        var platform: String? = null
        var type: String? = null
        var port: String? = null
        if (ctx != null) {
            for (section in ctx.boardSection()) {
                when {
                    section.boardPlatform() != null -> platform =
                        section.boardPlatform().TEXT(0).text + ":" + section.boardPlatform().TEXT(1).text

                    section.boardType() != null -> type = section.boardType().TEXT().text
                    section.boardPort() != null -> port = section.boardPort().DEVICE_PATH().text
                }
            }
        }
        arduino.board = Board(
            platform ?: throw Error("Board platform is missing!"),
            type ?: throw Error("Board type is missing!"),
            port ?: throw Error("Board port is missing!")
        )
        return arduino
    }

    override fun visitInputsDecl(ctx: ExprParser.InputsDeclContext?): Arduino {
        if (ctx != null) {
            for (input in ctx.inputEntry()) {
                var name: String? = null
                var mode: String? = null
                var type: String? = null
                var source: String? = null

                if (input.inputName() != null) {
                    name = input.inputName().TEXT().text
                }
                if (input.inputMode() != null) {
                    mode = input.inputMode().TEXT().text
                }
                if (input.inputType() != null) {
                    type = input.inputType().TEXT().text
                }
                if (input.inputSource() != null) {
                    source = input.inputSource().TEXT().text
                }

                arduino.inputs.add(
                    Inputs(
                        name ?: throw Error("Input name is missing!"),
                        mode ?: throw Error("Input mode is missing!"),
                        type,
                        source ?: throw Error("Input source is missing!")
                    )
                )
            }
        }
        return arduino
    }

    override fun visitOutputsDecl(ctx: ExprParser.OutputsDeclContext?): Arduino {
        if (ctx != null) {
            for (output in ctx.outputEntry()) {
                var name: String? = null
                var mode: String? = null
                var pin: String? = null

                if (output.outputName() != null) {
                    name = output.outputName().TEXT().text
                }
                if (output.outputMode() != null) {
                    mode = output.outputMode().TEXT().text
                }
                if (output.outputPin() != null) {
                    pin = output.outputPin().TEXT().text
                }
                arduino.outputs.add(
                    Outputs(
                        name ?: throw Error("Output name is missing!"),
                        mode ?: throw Error("Output mode is missing!"),
                        pin ?: throw Error("Output pin is missing!")
                    )
                )
            }
        }

        return arduino
    }
}