package tokenizer;

public enum TokenType {
//    关键字
    FN_KW,
//    -> 'fn'
    LET_KW,
//    -> 'let'
    CONST_KW,
//    -> 'const'
    AS_KW,
//    -> 'as'
    WHILE_KW,
//    -> 'while'
    IF_KW,
//    -> 'if'
    ELSE_KW,
//    -> 'else'
    RETURN_KW,
//    -> 'return'

// 这两个是扩展 c0 的
    BREAK_KW,
//             -> 'break'
    CONTINUE_KW,
//            -> 'continue'

//    字面量
    UINT_LITERAL,
//    -> digit 无符号整数
    STRING_LITERAL,
//    -> '"' (string_regular_char | escape_sequence)* '"' 字符串常量

// 扩展 c0
    DOUBLE_LITERAL,
//    -> digit+ '.' digit+ ([eE] [+-]? digit+)? 无符号整数
    CHAR_LITERAL,
//    -> '\'' (char_regular_char | escape_sequence) '\'' 字符常量

//    标识符
    IDENT,

//    运算符
    PLUS,
//    -> '+'
    MINUS,
//    -> '-'
    MUL,
//    -> '*'
    DIV,
//    -> '/'
    ASSIGN,
//    -> '='
    EQ,
//    -> '=='
    NEQ,
//    -> '!='
    LT,
//    -> '<'
    GT,
//    -> '>'
    LE,
//    -> '<='
    GE,
//    -> '>='
    L_PAREN,
//    -> '('
    R_PAREN,
//    -> ')'
    L_BRACE,
//    -> '{'
    R_BRACE,
//    -> '}'
    ARROW,
//    -> '->'
    COMMA,
//    -> ','
    COLON,
//    -> ':'
    SEMICOLON,
//    -> ';'


//    类型
    INT,
//    64 位有符号整数 int
    VOID,
//    空类型 void
    DOUBLE,
//    64 位 IEEE-754 浮点数 double
    BOOLEAN,

//    注释
    COMMENT,
//    -> '//'

    EOF;
//    文件尾


    @Override
    public String toString() {
        switch (this) {
            case FN_KW:
                return "FN_KW";
            case LET_KW:
                return "LET_KW";
            case CONST_KW:
                return "CONST_KW";
            case AS_KW:
                return "AS_KW";
            case WHILE_KW:
                return "WHILE_KW";
            case IF_KW:
                return "IF_KW";
            case ELSE_KW:
                return "ELSE_KW";
            case RETURN_KW:
                return "RETURN_KW";
            case BREAK_KW:
                return "BREAK_KW";
            case CONTINUE_KW:
                return "CONTINUE_KW";

            case UINT_LITERAL:
                return "UINT_LITERAL";
            case STRING_LITERAL:
                return "STRING_LITERAL";
            case DOUBLE_LITERAL:
                return "DOUBLE_LITERAL";
            case CHAR_LITERAL:
                return "CHAR_LITERAL";

            case IDENT:
                return "IDENT";

            case PLUS:
                return "PLUS";
            case MINUS:
                return "MINUS";
            case MUL:
                return "MUL";
            case DIV:
                return "DIV";
            case ASSIGN:
                return "ASSIGN";
            case EQ:
                return "EQ";
            case NEQ:
                return "NEQ";
            case LT:
                return "LT";
            case GT:
                return "GT";
            case LE:
                return "LE";
            case GE:
                return "GE";
            case L_PAREN:
                return "L_PAREN";
            case R_PAREN:
                return "R_PAREN";
            case L_BRACE:
                return "L_BRACE";
            case R_BRACE:
                return "R_BRACE";
            case ARROW:
                return "ARROW";
            case COMMA:
                return "COMMA";
            case COLON:
                return "COLON";
            case SEMICOLON:
                return "SEMICOLON";

            case INT:
                return "INT";
            case VOID:
                return "VOID";
            case DOUBLE:
                return "DOUBLE";
            case BOOLEAN:
                return "BOOLEAN";

            case COMMENT:
                return "COMMENT";

            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }
}
