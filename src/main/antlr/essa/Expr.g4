grammar Expr;

// Lexer rules
INDENT              : '    ' ;
INDENT_ITEM_START   : '    -' ;
INDENT_ITEM_CONT    : '     ' ;
WS                  : [ \t] -> channel(HIDDEN) ;
NEWLINE             : [\r\n]+ -> channel(HIDDEN);
TEXT            : [a-zA-Z_0-9-]+ ;
DEVICE_PATH     : ('/'? [a-zA-Z0-9/_]+) | ('COM' [0-9]+);
INT             : [0-9]+ ;
FLOAT           : [0-9]+ '.' [0-9]* ;
BOOL            : ('TRUE'|'FALSE');
// Parser rules
config          : section+ EOF;

section         : boardDecl
                | inputsDecl
                | outputsDecl
                | constantsDecl
                | signalsDecl
                | rulesDecl
                | eventsDecl;

// BOARD
boardDecl       : 'board:' boardSection+;

boardSection    : boardPlatform
                | boardType
                | boardPort;

boardPlatform   : INDENT 'platform:' TEXT ':' TEXT;
boardType       : INDENT 'type:' TEXT;

boardPort       : INDENT 'port:' DEVICE_PATH;


// CONSTANTS
constantsDecl   : 'constants:' constantEntry+;
constantEntry   : constantName
                  constantValue;

constantName    :  INDENT_ITEM_START 'name:' TEXT;
constantValue   :  INDENT_ITEM_CONT 'value:' (INT|FLOAT|TEXT);


// INPUTS
inputsDecl      : 'inputs:' inputEntry+;

inputEntry      : inputName
                  inputMode
                  inputType
                  inputSource;

inputName       :  INDENT_ITEM_START 'name:' TEXT;
inputMode       :  INDENT_ITEM_CONT 'mode:' TEXT;
inputType       :  INDENT_ITEM_CONT 'type:' TEXT;
inputSource     :  INDENT_ITEM_CONT 'pin:' TEXT;


// OUTPUTS
outputsDecl     : 'outputs:' outputEntry+;

outputEntry     : outputName
                  outputMode
                  outputPin;

outputName      :  INDENT_ITEM_START 'name:' TEXT;
outputMode      :  INDENT_ITEM_CONT 'mode:' TEXT;
outputPin       :  INDENT_ITEM_CONT 'pin:' TEXT;


// SIGNALS
signalsDecl     : 'signals:' signalEntry+;

signalEntry     : singalName
                  signalExpression;

singalName      : INDENT_ITEM_START 'name:' TEXT;
signalExpression: INDENT_ITEM_CONT 'expression:' varA=TEXT operand=('>'| '>=' | '<' | '<=' | '==' | '!=') varB=(INT | FLOAT | TEXT | BOOL);

// RULES

rulesDecl       : 'rules:' ruleEntry+;
ruleEntry       : ruleIf
                  ruleThen;

ruleIf          : INDENT_ITEM_START 'if:' donot='!'? variable=TEXT;
ruleThen        : INDENT_ITEM_CONT 'then:' do='SET' variable=TEXT state=('ON' | 'OFF');


// EVENTS
eventsDecl      : 'events:' eventEntry+;
eventEntry      : eventWhen
                  eventDo;

eventWhen       : INDENT_ITEM_START 'when:' TEXT;
eventDo         : INDENT_ITEM_CONT 'do:' TEXT;