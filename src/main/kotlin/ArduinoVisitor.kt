import entities.*
import essa.ExprBaseVisitor
import essa.ExprParser

class ArduinoVisitor : ExprBaseVisitor<Arduino>() {
    var arduino: Arduino = Arduino()
    var memory: HashMap<String, Any> = HashMap()

    override fun visitConfig(ctx: ExprParser.ConfigContext?): Arduino {
        if (ctx == null) {
            throw Error("Config section is missing!")
        }

        for (section in ctx.section()) {
            visit(section)
        }

        return arduino
    }

    override fun visitBoardDecl(ctx: ExprParser.BoardDeclContext?): Arduino {
        if (ctx == null) {
            throw Error("Board section is missing!")
        }

        var platform: String? = null
        var type: String? = null
        var port: String? = null

        for (section in ctx.boardSection()) {
            when {
                section.boardPlatform() != null -> platform =
                    section.boardPlatform().TEXT(0).text + ":" + section.boardPlatform().TEXT(1).text

                section.boardType() != null -> type = section.boardType().TEXT().text
                section.boardPort() != null -> port = section.boardPort().DEVICE_PATH().text
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
        if (ctx == null) {
            throw Error("Inputs section is missing!")
        }

        for (input in ctx.inputEntry()) {
            var name: String? = null
            var mode: String? = null
            var type: String? = null
            var source: String? = null

            if (input.inputName() != null) {
                name = when {
                    memory[input.inputName().TEXT().text] != null ->
                        throw Error("Input ${input.inputName().TEXT().text} is redeclared!")
                    else -> input.inputName().TEXT().text
                }
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
            memory[name] = source
        }

        return arduino
    }

    override fun visitOutputsDecl(ctx: ExprParser.OutputsDeclContext?): Arduino {
        if (ctx == null) {
            throw Error("Outputs section is missing!")
        }

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

        return arduino
    }

    override fun visitConstantsDecl(ctx: ExprParser.ConstantsDeclContext?): Arduino {
        if (ctx == null) {
            return arduino
        }

        for (constant in ctx.constantEntry()) {
            var name: String? = null
            var value: String? = null

            if (constant.constantName() != null) {
                name = when {
                    memory[constant.constantName().TEXT().text] != null ->
                        throw Error("Constant ${constant.constantName().TEXT().text} is redeclared!")
                    else -> constant.constantName().TEXT().text
                }
            }
            if (constant.constantValue() != null) {
                value =
                    when {
                        constant.constantValue().TEXT() != null -> constant.constantValue().TEXT().text
                        constant.constantValue().INT() != null -> constant.constantValue().INT().text
                        constant.constantValue().FLOAT() != null -> constant.constantValue().FLOAT().text
                        else -> throw Error("Unexpected constant value!")
                    }
            }

            arduino.constants.add(
                Constants(
                    name ?: throw Error("Constant name is missing!"),
                    value ?: throw Error("Constant value is missing!")
                )
            )
            memory[name] = value
        }

        return arduino
    }

    override fun visitSignalsDecl(ctx: ExprParser.SignalsDeclContext?): Arduino {
        if (ctx == null) {
            return arduino
        }

        for (signal in ctx.signalEntry()) {
            var name: String? = null
            var varA: String? = null
            var varB: String? = null
            var operand: String? = null

            if (signal.singalName() != null) {
                name = when {
                    memory[signal.singalName().TEXT().text] != null ->
                        throw Error("Signal ${signal.singalName().TEXT().text} is redeclared!")
                    else -> signal.singalName().TEXT().text
                }
            }
            if (signal.signalExpression() != null) {
                varA = signal.signalExpression().varA.text
                varB = signal.signalExpression().varB.text
                operand = signal.signalExpression().operand.text
            }

            arduino.signals.add(
                Signals(
                    name ?: throw Error("Signal name is missing!"),
                    varA ?: throw Error("Signal varA is missing!"),
                    varB ?: throw Error("Signal varB is missing!"),
                    operand ?: throw Error("Signal operand is missing!")
                )
            )
            memory[name] = false
        }

        return arduino
    }

    override fun visitRulesDecl(ctx: ExprParser.RulesDeclContext?): Arduino {
        if (ctx == null) {
            return arduino
        }

        var ruleVariable: String? = null
        var ruleDo: String? = null
        var ruleVar: String? = null
        var ruleNot: String? = null
        var ruleState: String? = null
        for (rule in ctx.ruleEntry()) {
            if (rule.ruleIf() != null) {
                ruleVariable = rule.ruleIf().variable.text
                if (rule.ruleIf().donot != null)
                    ruleNot = rule.ruleIf().donot.text
            }
            if (rule.ruleThen() != null) {
                ruleDo = rule.ruleThen().do_.text
                ruleVar = rule.ruleThen().variable.text
                ruleState = rule.ruleThen().state.text
            }

            arduino.rules.add(
                Rules(
                    ruleVariable ?: throw Error("Rule if is missing!"),
                    ruleDo ?: throw Error("Rule do is missing!"),
                    ruleNot,
                    ruleVar ?: throw Error("Rule var is missing!"),
                    ruleState ?: throw Error("Rule state is missing!")
                )
            )
        }

        return arduino
    }

    override fun visitEventsDecl(ctx: ExprParser.EventsDeclContext?): Arduino {
        if (ctx == null) {
            return arduino
        }

        var eventWhen: String? = null
        var eventDo: String? = null
        for (event in ctx.eventEntry()) {
            if (event.eventWhen() != null) {
                eventWhen = event.eventWhen().TEXT().text
            }
            if (event.eventDo() != null) {
                eventDo = event.eventDo().TEXT().text
            }
            arduino.events.add(
                Events(
                    eventWhen ?: throw Error("Event when is missing!"),
                    eventDo ?: throw Error("Event do is missing!")
                )
            )
        }

        return arduino
    }
}

