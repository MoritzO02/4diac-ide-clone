/*
 * generated by Xtext 2.25.0
 */
lexer grammar InternalSTCoreLexer;

@header {
package org.eclipse.fordiac.ide.structuredtextcore.parser.antlr.lexer;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

LDATE_AND_TIME : ('L'|'l')('D'|'d')('A'|'a')('T'|'t')('E'|'e')'_'('A'|'a')('N'|'n')('D'|'d')'_'('T'|'t')('I'|'i')('M'|'m')('E'|'e')'#';

DATE_AND_TIME : ('D'|'d')('A'|'a')('T'|'t')('E'|'e')'_'('A'|'a')('N'|'n')('D'|'d')'_'('T'|'t')('I'|'i')('M'|'m')('E'|'e')'#';

TIME_OF_DAY : ('T'|'t')('I'|'i')('M'|'m')('E'|'e')'_'('O'|'o')('F'|'f')'_'('D'|'d')('A'|'a')('Y'|'y')'#';

END_REPEAT : ('E'|'e')('N'|'n')('D'|'d')'_'('R'|'r')('E'|'e')('P'|'p')('E'|'e')('A'|'a')('T'|'t');

VAR_OUTPUT : ('V'|'v')('A'|'a')('R'|'r')'_'('O'|'o')('U'|'u')('T'|'t')('P'|'p')('U'|'u')('T'|'t');

END_WHILE : ('E'|'e')('N'|'n')('D'|'d')'_'('W'|'w')('H'|'h')('I'|'i')('L'|'l')('E'|'e');

VAR_INPUT : ('V'|'v')('A'|'a')('R'|'r')'_'('I'|'i')('N'|'n')('P'|'p')('U'|'u')('T'|'t');

CONSTANT : ('C'|'c')('O'|'o')('N'|'n')('S'|'s')('T'|'t')('A'|'a')('N'|'n')('T'|'t');

CONTINUE : ('C'|'c')('O'|'o')('N'|'n')('T'|'t')('I'|'i')('N'|'n')('U'|'u')('E'|'e');

END_CASE : ('E'|'e')('N'|'n')('D'|'d')'_'('C'|'c')('A'|'a')('S'|'s')('E'|'e');

VAR_TEMP : ('V'|'v')('A'|'a')('R'|'r')'_'('T'|'t')('E'|'e')('M'|'m')('P'|'p');

WSTRING : ('W'|'w')('S'|'s')('T'|'t')('R'|'r')('I'|'i')('N'|'n')('G'|'g')'#';

END_FOR : ('E'|'e')('N'|'n')('D'|'d')'_'('F'|'f')('O'|'o')('R'|'r');

END_VAR : ('E'|'e')('N'|'n')('D'|'d')'_'('V'|'v')('A'|'a')('R'|'r');

STRING : ('S'|'s')('T'|'t')('R'|'r')('I'|'i')('N'|'n')('G'|'g')'#';

DWORD : ('D'|'d')('W'|'w')('O'|'o')('R'|'r')('D'|'d')'#';

END_IF : ('E'|'e')('N'|'n')('D'|'d')'_'('I'|'i')('F'|'f');

LDATE : ('L'|'l')('D'|'d')('A'|'a')('T'|'t')('E'|'e')'#';

LREAL : ('L'|'l')('R'|'r')('E'|'e')('A'|'a')('L'|'l')'#';

LTIME : ('L'|'l')('T'|'t')('I'|'i')('M'|'m')('E'|'e')'#';

LWORD : ('L'|'l')('W'|'w')('O'|'o')('R'|'r')('D'|'d')'#';

REPEAT : ('R'|'r')('E'|'e')('P'|'p')('E'|'e')('A'|'a')('T'|'t');

RETURN : ('R'|'r')('E'|'e')('T'|'t')('U'|'u')('R'|'r')('N'|'n');

UDINT : ('U'|'u')('D'|'d')('I'|'i')('N'|'n')('T'|'t')'#';

ULINT : ('U'|'u')('L'|'l')('I'|'i')('N'|'n')('T'|'t')'#';

USINT : ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('T'|'t')'#';

WCHAR : ('W'|'w')('C'|'c')('H'|'h')('A'|'a')('R'|'r')'#';

ARRAY : ('A'|'a')('R'|'r')('R'|'r')('A'|'a')('Y'|'y');

BOOL : ('B'|'b')('O'|'o')('O'|'o')('L'|'l')'#';

BYTE : ('B'|'b')('Y'|'y')('T'|'t')('E'|'e')'#';

CHAR : ('C'|'c')('H'|'h')('A'|'a')('R'|'r')'#';

DATE : ('D'|'d')('A'|'a')('T'|'t')('E'|'e')'#';

DINT : ('D'|'d')('I'|'i')('N'|'n')('T'|'t')'#';

ELSIF : ('E'|'e')('L'|'l')('S'|'s')('I'|'i')('F'|'f');

FALSE : ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

LINT : ('L'|'l')('I'|'i')('N'|'n')('T'|'t')'#';

LTOD : ('L'|'l')('T'|'t')('O'|'o')('D'|'d')'#';

REAL : ('R'|'r')('E'|'e')('A'|'a')('L'|'l')'#';

SINT : ('S'|'s')('I'|'i')('N'|'n')('T'|'t')'#';

TIME : ('T'|'t')('I'|'i')('M'|'m')('E'|'e')'#';

UINT : ('U'|'u')('I'|'i')('N'|'n')('T'|'t')'#';

UNTIL : ('U'|'u')('N'|'n')('T'|'t')('I'|'i')('L'|'l');

WHILE : ('W'|'w')('H'|'h')('I'|'i')('L'|'l')('E'|'e');

WORD : ('W'|'w')('O'|'o')('R'|'r')('D'|'d')'#';

CASE : ('C'|'c')('A'|'a')('S'|'s')('E'|'e');

ELSE : ('E'|'e')('L'|'l')('S'|'s')('E'|'e');

EXIT : ('E'|'e')('X'|'x')('I'|'i')('T'|'t');

INT : ('I'|'i')('N'|'n')('T'|'t')'#';

LDT : ('L'|'l')('D'|'d')('T'|'t')'#';

THEN : ('T'|'t')('H'|'h')('E'|'e')('N'|'n');

TOD : ('T'|'t')('O'|'o')('D'|'d')'#';

TRUE : ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

B : '.''%'('B'|'b');

D_2 : '.''%'('D'|'d');

L : '.''%'('L'|'l');

W : '.''%'('W'|'w');

X : '.''%'('X'|'x');

AND : ('A'|'a')('N'|'n')('D'|'d');

DT : ('D'|'d')('T'|'t')'#';

FOR : ('F'|'f')('O'|'o')('R'|'r');

LD : ('L'|'l')('D'|'d')'#';

LT : ('L'|'l')('T'|'t')'#';

MOD : ('M'|'m')('O'|'o')('D'|'d');

NOT : ('N'|'n')('O'|'o')('T'|'t');

VAR : ('V'|'v')('A'|'a')('R'|'r');

XOR : ('X'|'x')('O'|'o')('R'|'r');

AsteriskAsterisk : '*''*';

FullStopFullStop : '.''.';

ColonEqualsSign : ':''=';

LessThanSignEqualsSign : '<''=';

LessThanSignGreaterThanSign : '<''>';

GreaterThanSignEqualsSign : '>''=';

AT : ('A'|'a')('T'|'t');

BY : ('B'|'b')('Y'|'y');

D_1 : ('D'|'d')'#';

DO : ('D'|'d')('O'|'o');

IF : ('I'|'i')('F'|'f');

MS : ('M'|'m')('S'|'s');

NS : ('N'|'n')('S'|'s');

OF : ('O'|'o')('F'|'f');

OR : ('O'|'o')('R'|'r');

T : ('T'|'t')'#';

TO : ('T'|'t')('O'|'o');

US : ('U'|'u')('S'|'s');

NumberSign : '#';

Ampersand : '&';

LeftParenthesis : '(';

RightParenthesis : ')';

Asterisk : '*';

PlusSign : '+';

Comma : ',';

HyphenMinus : '-';

FullStop : '.';

Solidus : '/';

Colon : ':';

Semicolon : ';';

LessThanSign : '<';

EqualsSign : '=';

GreaterThanSign : '>';

D : ('D'|'d');

H : ('H'|'h');

M : ('M'|'m');

S : ('S'|'s');

LeftSquareBracket : '[';

RightSquareBracket : ']';

KW__ : '_';

fragment RULE_HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F'|'_');

RULE_NON_DECIMAL : ('2#'|'8#'|'16#') RULE_HEX_DIGIT+;

RULE_EXT_INT : RULE_INT ('e'|'E') ('-'|'+')? RULE_INT;

RULE_INT : '0'..'9' ('_'? '0'..'9')*;

RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

RULE_STRING : '"' ('$' .|~(('$'|'"')))* '"';

RULE_WSTRING : '\'' ('$' .|~(('$'|'\'')))* '\'';

RULE_ML_COMMENT : ('/*' ( options {greedy=false;} : . )*'*/'|'(*' ( options {greedy=false;} : . )*'*)');

RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

RULE_WS : (' '|'\t'|'\r'|'\n')+;

RULE_ANY_OTHER : .;
