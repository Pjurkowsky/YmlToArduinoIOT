grammar NewExpr;

// Lexer rules
INDENT              : '    ' ;
INDENT_ITEM_START   : '    -' ;
INDENT_ITEM_CONT    : '     ' ;
//WS                  : [ \t\n]+ -> skip ;
WS                  : [ \t] -> channel(HIDDEN) ;
NEWLINE             : [\r\n]+ -> channel(HIDDEN);
BOARD_PLATFORM      : 'platform';
BOARD_TYPE          : 'type';
BOARD_PORT          : 'port' ;
BOOL                : ('TRUE'|'FALSE') ;
TEXT                : [a-zA-Z_0-9]+ ;
DEVICE_PATH         : ('/'? [a-zA-Z0-9/_]+) | ('COM' [0-9]+);
INT                 : [0-9]+ ;
FLOAT               : [0-9]+ '.' [0-9]* ;

// Parser
config          : section+ EOF;

section         : boardDecl
                | inputsDecl;
//                | outputsDecl
//                | constantsDecl
//                | signalsDecl
//                | rulesDecl
//                | eventsDecl;

boardDecl       : 'board:' boardSection+;

boardSection    : INDENT category=boardSubsection ':' insides=boardInsides;
boardSubsection : BOARD_PLATFORM|BOARD_PORT|BOARD_TYPE;
boardInsides    : platform+=TEXT (':'? platform+=TEXT)* #board_std
                | DEVICE_PATH #board_port;

inputsDecl      : 'inputs:' inputEntry+;

inputEntry      : INDENT_ITEM_START inputName (INDENT_ITEM_CONT specs+=inputSpec)*;

inputName       : 'name:' TEXT ;
inputSpec       : (TEXT|BOARD_TYPE) ':' (TEXT|INT|FLOAT) #input_std
                | 'source:' (TEXT|INT|FLOAT) #input_source;
//customer:
//    given:   Dorothy
//    family:  Gale
//
//items:
//    - part_no:   A4786
//      descrip:   Water Bucket (Filled)
//      price:     1.47
//      quantity:  4