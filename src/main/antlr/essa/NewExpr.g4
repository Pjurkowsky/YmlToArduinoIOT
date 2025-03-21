grammar NewExpr;

// Lexer rules
INDENT              : '    ' ;
INDENT_ITEM_START   : '    -' ;
INDENT_ITEM_CONT    : '     ' ;
WS                  : [ \t] -> channel(HIDDEN) ;
NEWLINE             : [\r\n]+ -> channel(HIDDEN);
BOOL                : ('TRUE'|'FALSE') ;
TEXT                : [a-zA-Z_0-9-]+ ;
DEVICE_PATH         : ('/'? [a-zA-Z0-9/_]+) | ('COM' [0-9]+);
INT                 : [0-9]+ ;
FLOAT               : [0-9]+ '.' [0-9]* ;

// Parser
config          : section+ EOF;

section         : sectionName=sectionNames ':' itemsEntry+ #items_section
                | sectionName=sectionNames ':' boardSection+ #board_section;
sectionNames    : 'board'|'inputs'|'outputs'|'constants'|'signals'|'rules'|'events' ;

boardSection    : INDENT category=boardSubsection ':' insides=boardInsides;
itemsEntry      : INDENT_ITEM_START 'name:' TEXT (INDENT_ITEM_CONT specs+=itemSpec)* #entry_std
                | INDENT_ITEM_START 'if:' TEXT INDENT_ITEM_CONT 'then:' setOutput #entry_if_then;

boardSubsection : 'platform'|'port'|'type';
boardInsides    : platform+=TEXT (':'? platform+=TEXT)* #board_std
                | DEVICE_PATH #board_port;

itemSpec        : category=itemSubsection ':' (TEXT|INT|FLOAT) ;
itemSubsection  : 'mode'|'type'|'pin'|'value';

setOutput       : 'SET' TEXT 'ON' #set_on
                | 'SET' TEXT 'OFF' #set_off;