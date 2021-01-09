package tokenizer;

import error.ErrorCode;
import error.TokenizeError;
import util.Pos;

public class Tokenizer {

    private StringIter it;
    StringBuilder token = new StringBuilder();

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if (peek == '"') {
            return lexString();
        } else if (peek == '\'') {
            return lexChar();
        } else if (Character.isAlphabetic(peek) || peek == '_') {
            return lexIdentOrKeyword();
        } else if (peek == '/') {
            return lexComment();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexComment() {
        Pos start = it.currentPos();
        char now = it.nextChar();
        if (it.peekChar() != '/') {
            return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
        }
        while (true) {
            token.append(now);
            now = it.nextChar();
            if (now == 10 || now == 13) {
                break;
            }
        }
        Token t = new Token(TokenType.COMMENT, token.toString(), start, it.currentPos());
        token.setLength(0);
        return t;
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        token.setLength(0);
        Pos start = it.currentPos();
        while (true) {// 直到查看下一个字符不是数字为止:
            char peek = it.peekChar();
            if (!Character.isDigit(peek)) {
                break;
            }
            char now = it.nextChar();
            token.append(now);
        }
        if (it.peekChar() != '.') {
            Pos tokenPos = new Pos(it.currentPos().row, it.currentPos().col - token.length());
            return new Token(TokenType.UINT_LITERAL, Integer.parseInt(token.toString()), tokenPos, it.currentPos());
        }
        token.append(it.nextChar());
        int i = 0;
        while (Character.isDigit(it.peekChar())) {
            token.append(it.nextChar());
            i++;
        }
        if (i == 0) {
            Pos tokenPos = new Pos(it.currentPos().row, it.currentPos().col - token.length());
            return null;
        }
        if (it.peekChar() == 'e' || it.peekChar() == 'E') {
            token.append(it.nextChar());
            if (it.peekChar() == '+' || it.peekChar() == '-')
                token.append(it.nextChar());
            int j = 0;
            while (Character.isDigit(it.peekChar())) {
                token.append(it.nextChar());
                j++;
            }
            Pos tokenPos = new Pos(it.currentPos().row, it.currentPos().col - token.length());
            if (j == 0) {
                return null;
            }
            return new Token(TokenType.DOUBLE_LITERAL, Double.valueOf(token.toString()), tokenPos, it.currentPos());
        }
        Pos tokenPos = new Pos(it.currentPos().row, it.currentPos().col - token.length());
        return new Token(TokenType.DOUBLE_LITERAL, Double.valueOf(token.toString()), tokenPos, it.currentPos());

    }

    private Token lexString() throws TokenizeError {
        Pos start = it.currentPos();
        char peek;
        char now;
        it.nextChar();
        while (true) {
            peek = it.peekChar();
            if (peek == '\n' || peek == '\t' || peek == '\r') {
                it.nextChar();
                break;
            } else if (peek == '"') {
                it.nextChar();
                break;
            } else if (peek == '\\') {
                it.nextChar();
                peek = it.peekChar();
//                if (peek == '\\' || peek == '"' || peek == '\'' || peek == 'n' || peek == 'r' || peek == 't') {
//                    token.append(now);
//                }
//                else {
//                    it.nextChar();
//                    break;
//                }
                if (peek == '\\' || peek == '"' || peek == '\'') {
                    token.append(peek);
                } else if (peek == 'r') {
                    token.append('\r');
                } else if (peek == 'n') {
                    token.append('\n');
                } else if (peek == 't') {
                    token.append('\t');
                } else {
                    it.nextChar();
                    break;
                }
                it.nextChar();
            } else {
                now = it.nextChar();
                token.append(now);
            }
        }
        if (token.length() != 0) {
            Token t = new Token(TokenType.STRING_LITERAL, token.toString(), start, it.currentPos());
            token.setLength(0);
            return t;
        }
        return null;
    }

    private Token lexChar() throws TokenizeError {
        Pos start = it.currentPos();
        char peek;
        char now;
        it.nextChar();
        while (true) {
            peek = it.peekChar();
            if (peek == '\'') {
                it.nextChar();
                break;
            }
            else if (peek == '\n' || peek == '\t' || peek == '\r') {
                it.nextChar();
                break;
            }
            else if (peek == '\\') {
                it.nextChar();
                peek = it.peekChar();
//                if (peek == '\\' || peek == '"' || peek == '\'' || peek == 'n' || peek == 'r' || peek == 't') {
//                    token.append(now);
//                }
//                else {
//                    it.nextChar();
//                    break;
//                }
                if (peek == '\\' || peek == '"' || peek == '\'') {
                    token.append(peek);
                } else if (peek == 'r') {
                    token.append('\r');
                } else if (peek == 'n') {
                    token.append('\n');
                } else if (peek == 't') {
                    token.append('\t');
                } else {
                    it.nextChar();
                    break;
                }
                it.nextChar();
            } else {
                now = it.nextChar();
                token.append(now);
            }
        }
        if (token.length() != 0) {
            Token t = new Token(TokenType.CHAR_LITERAL, token.toString(), start, it.currentPos());
            token.setLength(0);
            return t;
        }
        return null;
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        token.setLength(0);

        Pos start = it.currentPos();

        while (true) {
            char peek = it.peekChar();
            if (!Character.isLetterOrDigit(peek) && peek != '_') {
                break;
            }
            char now = it.nextChar();
            token.append(now);
        }
        if (token.length() != 0) {
            String str = token.toString();
            Token t;
            switch (str) {
                case "fn":
                    t = new Token(TokenType.FN_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "let":
                    t = new Token(TokenType.LET_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "const":
                    t = new Token(TokenType.CONST_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "as":
                    t = new Token(TokenType.AS_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "while":
                    t = new Token(TokenType.WHILE_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "if":
                    t = new Token(TokenType.IF_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "else":
                    t = new Token(TokenType.ELSE_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "return":
                    t = new Token(TokenType.RETURN_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "break":
                    t = new Token(TokenType.BREAK_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "continue":
                    t = new Token(TokenType.CONTINUE_KW, str, start, it.currentPos());
                    token.setLength(0);
                    return t;

                case "int":
                    t = new Token(TokenType.INT, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "void":
                    t = new Token(TokenType.VOID, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "double":
                    t = new Token(TokenType.DOUBLE, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
                case "boolean":
                    t = new Token(TokenType.DOUBLE, str, start, it.currentPos());
                    token.setLength(0);
                    return t;

                default:
                    t = new Token(TokenType.IDENT, str, start, it.currentPos());
                    token.setLength(0);
                    return t;
            }
        }
        return null;
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
            //区别 MINUS 和 ARROW
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
                }
            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
                }
                //判断 NEQ
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
                //判断 LT 和 LE
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
                }
                //判断 GT 和 GE
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
                }

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
