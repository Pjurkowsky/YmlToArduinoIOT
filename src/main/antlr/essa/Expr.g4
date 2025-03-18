grammar Expr;

// Lexer rules
INDENT          : '    ' ;
BIGINDENT       : '      ';
DEDENT          : ('\n'|'\r\n') ;
WS              : [ \t]+ -> skip ;
TEXT            : [a-zA-Z_0-9]+ ;
DEVICE_PATH     : ('/'? [a-zA-Z0-9/_]+) | ('COM' [0-9]+);
INT             : [0-9]+ ;
FLOAT           : [0-9]+ '.' [0-9]* ;
// Parser rules
config          : section+ EOF;

section         : boardDecl
                | inputsDecl
                | outputsDecl
                | constantsDecl
                | signalsDecl
                | rulesOrEvents;

rulesOrEvents   : (rulesDecl | eventsDecl | (rulesDecl eventsDecl));

// BOARD
boardDecl       : 'board:' DEDENT boardSection+;

boardSection    : boardPlatform
                | boardType
                | boardPort;

boardPlatform   : INDENT 'platform:' TEXT ':' TEXT DEDENT;
boardType       : INDENT 'type:' TEXT DEDENT;

boardPort       : INDENT 'port:' DEVICE_PATH DEDENT;


// CONSTANTS
constantsDecl   : 'constants:' DEDENT constantEntry+;
constantEntry   : constantName
                  constantValue;

constantName    :  INDENT '- name:' TEXT DEDENT;
constantValue   :  BIGINDENT 'value:' TEXT (INT | FLOAT);


// INPUTS
inputsDecl      : 'inputs:' DEDENT inputEntry+;

inputEntry      : inputName
                  inputMode
                  inputType
                  inputSource;

inputName       :  INDENT '- name:' TEXT DEDENT;
inputMode       :  BIGINDENT  'mode:' TEXT DEDENT;
inputType       :  BIGINDENT  'type:' TEXT DEDENT;
inputSource     :  BIGINDENT 'source:' TEXT DEDENT;


// OUTPUTS
outputsDecl     : 'outputs:' DEDENT outputEntry+;

outputEntry     : outputName
                  outputMode
                  outputPin;

outputName      : INDENT '- name:' TEXT DEDENT;
outputMode      :  BIGINDENT 'mode:' TEXT DEDENT;
outputPin       :  BIGINDENT 'pin:' TEXT DEDENT;


// SIGNALS
signalsDecl     : 'signals:' DEDENT signalEntry+;

signalEntry     : singalName
                  signalExpression;

singalName      : INDENT '- name:' TEXT DEDENT;
signalExpression: INDENT 'expression:' TEXT DEDENT;

// RULES

rulesDecl       : 'rules:' DEDENT ruleEntry+;
ruleEntry       : ruleIf
                  ruleThen;

ruleIf          : INDENT '- if:' TEXT DEDENT;
ruleThen        : INDENT 'then:' TEXT DEDENT;


// EVENTS
eventsDecl      : 'events:' DEDENT eventEntry+;
eventEntry      : eventWhen
                  eventDo;

eventWhen: INDENT '- when:' TEXT DEDENT;
eventDo: INDENT 'do:' TEXT DEDENT;