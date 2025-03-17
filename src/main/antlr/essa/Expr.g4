grammar Expr;

// Lexer rules
INDENT          : '    ' ;
DEDENT          : ('\n'|'\r\n');
FQBN            : [a-zA-Z_][a-zA-Z0-9_:]* ;
INT             : [0-9]+ ;
WS              : [ \t]+ -> skip ;
DEVICE_PATH     : ('/'? [a-zA-Z0-9/_]+) | ('COM' [0-9]+) ;

// Parser rules
config          : boardDecl EOF;

boardDecl       : 'board:' DEDENT boardPlatform DEDENT boardType DEDENT boardPort DEDENT;
boardPlatform   : INDENT 'platform:' FQBN;
boardType       : INDENT 'type:' FQBN;
boardPort       : INDENT 'port:' DEVICE_PATH;
