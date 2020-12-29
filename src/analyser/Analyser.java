package analyser;

import error.*;
import instruction.Instruction;
import instruction.InstructionEntry;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;
import util.Pos;

import javax.print.DocFlavor;
import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    //    全局变量表
    ArrayList<String> globalVarList = new ArrayList<>();
    //    全局函数表
    ArrayList<String> funcList = new ArrayList<>();

    /**
     * 符号表
     */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();


    int level = 0;

    //    判断是否有一个名为 main 的函数作为程序入口
    boolean hasMainFuc = false;
    //    判断是否全部返回
    boolean allReturn = false;

    public int[][] SymbolMatrix = {
            //  *   /  +  -  >  <  >= <= == != as
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// *
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// /
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},// +
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},// -
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// >
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// <
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// >=
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// <=
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// ==
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},// !=
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},// as
    };


    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;
    //    当前读到的token
    Token currentToken = null;


    /**
     * 下一个变量的栈偏移
     */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }


    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    public Token peek() throws TokenizeError {
        if (peekedToken == null) {
            do {
                peekedToken = tokenizer.nextToken();
            } while (peekedToken.getTokenType().equals(TokenType.COMMENT));
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    public Token next() throws TokenizeError {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            currentToken = token;
            return token;
        } else {
            currentToken = tokenizer.nextToken();
            return currentToken;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    public boolean check(TokenType tt) throws TokenizeError {
        Token token = peek();
        while (token.getTokenType() == TokenType.COMMENT) {
            next();
            token = peek();
        }
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    public Token nextIf(TokenType tt) throws TokenizeError {
        Token token = peek();
        while (token.getTokenType() == TokenType.COMMENT) {
            next();
            token = peek();
        }
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    public Token expect(TokenType tt) throws CompileError {
        Token token = peek();
        while (token.getTokenType() == TokenType.COMMENT) {
            next();
            token = peek();
        }
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    public int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param kind
     * @param type
     * @param level
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    public void addSymbol(String name, String kind, TokenType type, int level, boolean isConstant, boolean isInitialized, Pos curPos) throws AnalyzeError {
        Iterator iter = symbolTable.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iter.next();
            String name1 = entry.getKey().toString();
            SymbolEntry symbolEntry1 = (SymbolEntry) entry.getValue();
            if (name1.equals(name) && symbolEntry1.getLevel() == level) {
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
            }
        }
        this.symbolTable.put(name, new SymbolEntry(kind, type, level, isConstant, isInitialized, getNextVariableOffset()));
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    public void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    public int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    public boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }


    public int getGlobalIndex(String name) {
        for (int i = 0; i < globalVarList.size(); i++) {
            if (globalVarList.get(i).equals(name))
                return i;
        }
        return -1;
    }

    public int getFuncIndex(String name) {
        for (int i = 0; i < funcList.size(); i++) {
            if (funcList.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }


    //    program -> (decl_stmt| function)*
    public void analyseProgram() throws CompileError {
        init();
        while (check(TokenType.FN_KW) || check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            if (check(TokenType.FN_KW)) {
                analyseFunction();
            } else {
                analyseDeclStmt("_start");
            }
        }
        if (!hasMainFuc) {
            throw new AnalyzeError(ErrorCode.NoEnd, peek().getStartPos());
        }
        SymbolEntry startSymbol = symbolTable.get("_start");
        InstructionEntry instructionEntry = new InstructionEntry("stackalloc", 0);
        ArrayList<InstructionEntry> instructionEntries = startSymbol.getInstructions();
        instructionEntries.add(instructionEntry);
        InstructionEntry instructionEntry1 = new InstructionEntry("call", getFuncIndex("main"));
        instructionEntries.add(instructionEntry1);
        startSymbol.setInstructions(instructionEntries);

        expect(TokenType.EOF);
    }

    public void init() throws AnalyzeError {
//        先将start函数加入符号表
//        addSymbol("_start", "func", TokenType.VOID, 0, true, true, peek().getStartPos());
        addSymbol("getint", "func", TokenType.INT, level, true, true, currentToken.getStartPos());
        funcList.add("getint");
        addSymbol("getdouble", "func", TokenType.DOUBLE, level, true, true, currentToken.getStartPos());
        funcList.add("getdouble");
        addSymbol("getchar", "func", TokenType.INT, level, true, true, currentToken.getStartPos());
        funcList.add("getchar");
        addSymbol("putint", "func", TokenType.INT, level, true, true, currentToken.getStartPos());
        funcList.add("getchar");
        addSymbol("getint", "func", TokenType.VOID, level, true, true, currentToken.getStartPos());
        funcList.add("getchar");
        addSymbol("putdouble", "func", TokenType.VOID, level, true, true, currentToken.getStartPos());
        funcList.add("putdouble");
        addSymbol("putstr", "func", TokenType.VOID, level, true, true, currentToken.getStartPos());
        funcList.add("putstr");
        addSymbol("putln", "func", TokenType.VOID, level, true, true, currentToken.getStartPos());
        funcList.add("putln");
        addSymbol("_start", "func", TokenType.VOID, level, true, true, currentToken.getStartPos());
        funcList.add("_start");
    }


    //    function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
    public void analyseFunction() throws CompileError {
        expect(TokenType.FN_KW);
        Token identToken = expect(TokenType.IDENT);
        String funcName = identToken.getValueString();
        if (symbolTable.get(funcName) != null) {
            throw new AnalyzeError(ErrorCode.ReDefine, identToken.getStartPos());
        }
        expect(TokenType.L_PAREN);
        if (!check(TokenType.R_PAREN)) {
            analyseFunctionParamList(funcName);
        }
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        TokenType type = analyseTy();
        if (type == TokenType.VOID) {
            allReturn = true;
        }

        if (funcName.equals("main")) {
            hasMainFuc = true;
        }
        addSymbol(funcName, "func", type, level++, true, true, identToken.getStartPos());
        funcList.add(funcName);

        analyseBlockStmt(funcName, false, 0, 0, 0);

        if (type == TokenType.VOID) {
            SymbolEntry funcSymbol = symbolTable.get(funcName);
            InstructionEntry instructionEntry = new InstructionEntry(("ret"));
            ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
        }

//        将当前函数中的变量弹出符号表
        Iterator iter = symbolTable.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iter.next();
            String varname = entry.getKey().toString();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            if (symbolEntry.getLevel() == level) {
                if (getGlobalIndex(varname) != -1) {
                    symbolEntry.setLevel(0);
                    symbolEntry.setGlobal(true);
                    symbolEntry.setKind("var");
                } else {
                    iter.remove();
                }
            }
        }
        level--;
    }


    //    function_param_list -> function_param (',' function_param)*
    public void analyseFunctionParamList(String funcName) throws CompileError {
        if (check(TokenType.CONST_KW) || check(TokenType.IDENT)) {
            analyseFunctionParam(funcName);
        }
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseFunctionParam(funcName);
        }
    }

    //    function_param -> 'const'? IDENT ':' ty
    public void analyseFunctionParam(String funcName) throws CompileError {
        boolean isConst = false;
        if (check(TokenType.CONST_KW)) {
            expect(TokenType.CONST_KW);
            isConst = true;
        }
        Token identToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        TokenType type = analyseTy();
        addSymbol(identToken.getValueString(), "param", type, level, isConst, false, identToken.getStartPos());
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        ArrayList<String> paramList = funcSymbol.getParamVars();
        paramList.add(identToken.getValueString());
        funcSymbol.setParamVars(paramList);

    }

    //    block_stmt -> '{' stmt* '}'
    public void analyseBlockStmt(String funcName, boolean isInLoop, int startLoc, int endLoc, int ifLayer) throws CompileError {
        expect(TokenType.L_BRACE);
        while (!check(TokenType.R_BRACE)) {
            analyseStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
        }
        expect(TokenType.R_BRACE);
    }


    //    stmt ->
//    expr_stmt
//    | decl_stmt
//    | if_stmt
//    | while_stmt
//    | return_stmt
//    | block_stmt
//    | empty_stmt
    public void analyseStmt(String funcName, boolean isInLoop, int startLoc, int endLoc, int ifLayer) throws CompileError {
//    empty_stmt -> ';'
        if (check(TokenType.SEMICOLON)) {
            expect(TokenType.SEMICOLON);
        }
        //    let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
        else if (check(TokenType.LET_KW)) {
            analyseLetDeclStmt(funcName);
//    const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
        } else if (check(TokenType.CONST_KW)) {
            analyseConstDeclStmt(funcName);
        }
//    if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
        else if (check(TokenType.IF_KW)) {
            analyseIfStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
        }
//    while_stmt -> 'while' expr block_stmt
        else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt(funcName);
        }
//    return_stmt -> 'return' expr? ';'
        else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt(funcName);
        }
//    block_stmt -> '{' stmt* '}'
        else if (check(TokenType.L_BRACE)) {
            analyseBlockStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
        } else if (check(TokenType.BREAK_KW)) {
            if (!isInLoop) {
                throw new AnalyzeError(ErrorCode.NoEnd, new Pos(0, 0));
            }
            analyseBreakStmt(funcName, endLoc, ifLayer);
        } else if (check(TokenType.CONTINUE_KW)) {
            if (!isInLoop) {
                throw new AnalyzeError(ErrorCode.NoEnd, new Pos(0, 0));
            }
            analyseContinueStmt(funcName, startLoc, ifLayer);
        }
//    expr_stmt -> expr ';'
        else {
            analyseExprStmt(funcName);
        }
    }

    public void analyseExprStmt(String funcName) throws CompileError {
        analyseExpr(funcName);
        expect(TokenType.SEMICOLON);
    }

    public void analyseContinueStmt(String funcName, int startLoc, int ifLayer) throws CompileError {
        expect(TokenType.CONTINUE_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        int currentLoc = instructionEntries.size();
        InstructionEntry instructionEntry = new InstructionEntry("br", startLoc - currentLoc - 3 - ifLayer);
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
        expect(TokenType.SEMICOLON);
    }

    public void analyseBreakStmt(String funcName, int endLoc, int ifLayer) throws CompileError {
        expect(TokenType.BREAK_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        int currentLoc = instructionEntries.size();
        InstructionEntry instructionEntry = new InstructionEntry("br", endLoc - currentLoc - 3 - ifLayer);
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
        expect(TokenType.SEMICOLON);
    }

    //    if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
    public void analyseIfStmt(String funcName, boolean isInLoop, int startLoc, int endLoc, int ifLayer) throws CompileError {
        expect(TokenType.IF_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        analyseExpr(funcName);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        int loc1 = instructionEntries.size();
        InstructionEntry endIns = instructionEntries.get(instructionEntries.size() - 1);
        if (!endIns.getIns().equals("brtrue") && !endIns.getIns().equals("brfalse")) {
            InstructionEntry instructionEntry = new InstructionEntry("brture", 1);
            instructionEntries.add(instructionEntry);
        }
        analyseBlockStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
        int loc2 = instructionEntries.size();
        InstructionEntry instructionEntry = new InstructionEntry("br", loc2 - loc1 + 1);
        instructionEntries.add(instructionEntry);
        boolean hasElse = false;
        if (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            hasElse = true;
            if (check(TokenType.L_BRACE)) {
                analyseBlockStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
            } else if (check(TokenType.IF_KW)) {
                ifLayer++;
                analyseIfStmt(funcName, isInLoop, startLoc, endLoc, ifLayer);
            } else {
                throw new AnalyzeError(ErrorCode.NoEnd, peek().getStartPos());
            }
        }
        int loc3 = instructionEntries.size();
        if (hasElse) {
            instructionEntry = new InstructionEntry("br", loc3 - loc2);
            instructionEntries.add(loc2 + 1, instructionEntry);
        }
        instructionEntry = new InstructionEntry("br", 0);
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
    }

    //    while_stmt -> 'while' expr block_stmt
    public void analyseWhileStmt(String funcName) throws CompileError {
        expect(TokenType.WHILE_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        InstructionEntry instructionEntry = new InstructionEntry("br", 0);
        instructionEntries.add(instructionEntry);
        int loc1 = instructionEntries.size();
        funcSymbol.setInstructions(instructionEntries);
        analyseExpr(funcName);
        instructionEntries = funcSymbol.getInstructions();
        InstructionEntry endIns = instructionEntries.get(instructionEntries.size() - 1);
        if (!endIns.getIns().equals("brtrue") && !endIns.getIns().equals("brfalse")) {
            instructionEntry = new InstructionEntry("brture", 1);
            instructionEntries.add(instructionEntry);
        }
        int loc2 = instructionEntries.size();
        funcSymbol.setInstructions(instructionEntries);
        analyseBlockStmt(funcName, true, loc1, loc2, 0);
        instructionEntries = funcSymbol.getInstructions();
        int loc3 = instructionEntries.size();
        instructionEntry = new InstructionEntry("br", loc3 - loc2 + 1);
        instructionEntries.add(loc2, instructionEntry);
        instructionEntry = new InstructionEntry("br", loc1 - loc3 - 2);
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
    }

    //    return_stmt -> 'return' expr? ';'
    public void analyseReturnStmt(String funcName) throws CompileError {
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        InstructionEntry instructionEntry;
        Token token = expect(TokenType.RETURN_KW);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
//        有返回值
        if (check(TokenType.SEMICOLON)) {
            expect(TokenType.SEMICOLON);
            if (funcSymbol.getType() != TokenType.VOID) {
                throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
            }
        } else {
            instructionEntry = new InstructionEntry("arga", 0);
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
            TokenType type = analyseExpr(funcName);
            instructionEntries = funcSymbol.getInstructions();
            instructionEntry = new InstructionEntry("store64");
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
            if (funcSymbol.getType() != type) {
                throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
            }
        }
        instructionEntries = funcSymbol.getInstructions();
        instructionEntry = new InstructionEntry("ret");
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
        expect(TokenType.SEMICOLON);
    }

    //    decl_stmt -> let_decl_stmt | const_decl_stmt
    public void analyseDeclStmt(String funcName) throws CompileError {
        if (check(TokenType.LET_KW)) {
            analyseLetDeclStmt(funcName);
        } else {
            analyseConstDeclStmt(funcName);
        }
    }

    //    let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
    public void analyseLetDeclStmt(String funcName) throws CompileError {
        expect(TokenType.LET_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        Token identToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        TokenType type = analyseTy();
        if (type == TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidAssignment, identToken.getStartPos());
        }
//        加入符号表
        addSymbol(identToken.getValueString(), "var", type, level, false, false, identToken.getStartPos());
        SymbolEntry varSymbol = symbolTable.get(identToken.getValueString());
        ArrayList<String> localVars = funcSymbol.getLocVars();
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        InstructionEntry instructionEntry;
        if (check(TokenType.ASSIGN)) {
            expect(TokenType.ASSIGN);
            varSymbol.setInitialized(true);
            if (level == 0) {
                varSymbol.setGlobal(true);
                globalVarList.add(identToken.getValueString());
                instructionEntry = new InstructionEntry("globa", localVars.size());
            } else {
                instructionEntry = new InstructionEntry("loca", localVars.size());
            }
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
            analyseExpr(funcName);
            instructionEntries = funcSymbol.getInstructions();
            instructionEntry = new InstructionEntry("store64");
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
        }
        localVars.add(identToken.getValueString());
        funcSymbol.setLocVars(localVars);
        expect(TokenType.SEMICOLON);
    }


    //    const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
    public void analyseConstDeclStmt(String funcName) throws CompileError {
        expect(TokenType.CONST_KW);
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        Token identToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        TokenType type = analyseTy();
        if (type == TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidAssignment, identToken.getStartPos());
        }
//        加入符号表
        addSymbol(identToken.getValueString(), "var", type, level, true, true, identToken.getStartPos());
        SymbolEntry varSymbol = symbolTable.get(identToken.getValueString());
        ArrayList<String> localVars = funcSymbol.getLocVars();
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        InstructionEntry instructionEntry;
        expect(TokenType.ASSIGN);
        varSymbol.setInitialized(true);
        if (level == 0) {
            varSymbol.setGlobal(true);
            globalVarList.add(identToken.getValueString());
            instructionEntry = new InstructionEntry("globa", localVars.size());
        } else {
            instructionEntry = new InstructionEntry("loca", localVars.size());
        }
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
        analyseExpr(funcName);
        instructionEntries = funcSymbol.getInstructions();
        instructionEntry = new InstructionEntry("store64");
        instructionEntries.add(instructionEntry);
        funcSymbol.setInstructions(instructionEntries);
        localVars.add(identToken.getValueString());
        funcSymbol.setLocVars(localVars);
        expect(TokenType.SEMICOLON);
    }


    public TokenType analyseTy() throws CompileError {
        if (check(TokenType.INT)) {
            return TokenType.INT;
        } else if (check(TokenType.VOID)) {
            return TokenType.VOID;
        } else {
            return TokenType.DOUBLE;
        }
    }


    /*
     * 改写表达式相关的产生式：
     * E -> C ( == | != | < | > | <= | >= C )
     * C -> T { + | - T}
     * T -> F { * | / F}
     * F -> A ( as int_ty | double_ty )
     * A -> ( - ) I
     * I -> IDENT | UNIT | DOUBLE | func_call | '(' E ')' | IDENT = E
     *  */
    public TokenType analyseExpr(String funcName) throws CompileError {
        TokenType type = analyseC(funcName);
        while (true) {
            // 预读可能是运算符的 token
            Token op = peek();
            if (op.getTokenType() != TokenType.EQ &&
                    op.getTokenType() != TokenType.NEQ &&
                    op.getTokenType() != TokenType.LT &&
                    op.getTokenType() != TokenType.GT &&
                    op.getTokenType() != TokenType.LE &&
                    op.getTokenType() != TokenType.GE) {
                break;
            }
            // 运算符
            next();
            analyseC(funcName);

            SymbolEntry funcSymbol = symbolTable.get(funcName);
            ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
            InstructionEntry instructionEntry1;
            InstructionEntry instructionEntry2;
            // 生成代码
            if (op.getTokenType() == TokenType.EQ) {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("brfalse", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
            } else if (op.getTokenType() == TokenType.NEQ) {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("brtrue", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
            } else if (op.getTokenType() == TokenType.LT) {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("setlt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brtrue", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
                instructionEntries.add(instructionEntry3);
            } else if (op.getTokenType() == TokenType.GT) {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("setgt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brtrue", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
                instructionEntries.add(instructionEntry3);
            } else if (op.getTokenType() == TokenType.LE) {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("setgt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brfalse", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
                instructionEntries.add(instructionEntry3);
            } else {
                instructionEntry1 = new InstructionEntry("cmpi");
                instructionEntry2 = new InstructionEntry("setlt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brfalse", 1);
                instructionEntries.add(instructionEntry1);
                instructionEntries.add(instructionEntry2);
                instructionEntries.add(instructionEntry3);
            }
            funcSymbol.setInstructions(instructionEntries);
        }
        return type;
    }

    public TokenType analyseC(String funcName) throws CompileError {
        TokenType type = analyseT(funcName);
        while (true) {
            // 预读可能是运算符的 token
            Token op = peek();
            if (op.getTokenType() != TokenType.PLUS &&
                    op.getTokenType() != TokenType.MINUS) {
                break;
            }
            // 运算符
            next();
            analyseT(funcName);
            SymbolEntry funcSymbol = symbolTable.get(funcName);
            ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
            InstructionEntry instructionEntry;
            // 生成代码
            if (op.getTokenType() == TokenType.PLUS) {
                instructionEntry = new InstructionEntry("addi");
            } else {
                instructionEntry = new InstructionEntry("subi");
            }
            instructionEntries.add(instructionEntry);
            funcSymbol.setInstructions(instructionEntries);
        }
        return type;
    }

    public TokenType analyseT(String funcName) throws CompileError {
        TokenType type = analyseF(funcName);
        while (true) {
            // 预读可能是运算符的 token
            Token op = peek();
            if (op.getTokenType() != TokenType.MUL &&
                    op.getTokenType() != TokenType.DIV) {
                break;
            }
            // 运算符
            next();
            analyseF(funcName);
            SymbolEntry funcSymbol = symbolTable.get(funcName);
            ArrayList<InstructionEntry> instructionEntries = new ArrayList<>();
            instructionEntries = funcSymbol.getInstructions();
            // 生成代码
            if (op.getTokenType() == TokenType.MUL) {
                InstructionEntry instructionEntry1 = new InstructionEntry("multi");
                instructionEntries.add(instructionEntry1);
            }
            {
                InstructionEntry instructionEntry1 = new InstructionEntry("divi");
                instructionEntries.add(instructionEntry1);
            }
            funcSymbol.setInstructions(instructionEntries);
        }
        return type;
    }

    public TokenType analyseF(String funcName) throws CompileError {
        TokenType type = analyseA(funcName);
        if (check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            expect(TokenType.IDENT);
        }
        return type;
    }

    public TokenType analyseA(String funcName) throws CompileError {
        TokenType type;
        int minusCount = 0;
        while (check(TokenType.MINUS)) {
            minusCount++;
            expect(TokenType.MINUS);
        }
        type = analyseI(funcName);
        for (int i = 0; i < minusCount; i++) {
            SymbolEntry funcSymbol = symbolTable.get(funcName);
            ArrayList<InstructionEntry> instructionEntries = new ArrayList<>();
            instructionEntries = funcSymbol.getInstructions();
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("negi");
            instructionEntries.add(instructionEntry1);
            funcSymbol.setInstructions(instructionEntries);
        }
        return type;
    }

    public TokenType analyseI(String funcName) throws CompileError {
        SymbolEntry funcSymbol = symbolTable.get(funcName);
        ArrayList<InstructionEntry> instructionEntries = funcSymbol.getInstructions();
        if (check(TokenType.IDENT)) {
            Token nameToken = expect(TokenType.IDENT);
            String name = nameToken.getValueString();
            SymbolEntry entry = this.symbolTable.get(name);
            if (entry == null) {
                throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
            }
            //调用函数（解决一下标准库的问题）
            if (check(TokenType.L_PAREN)) {
                if (!entry.getType().equals("func")) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
                }
                String callOrcallname = "call";
                boolean isLib = false;
                if (name.equals("getint") || name.equals("getdouble") || name.equals("getchar") || name.equals("putint") || name.equals("putchar") || name.equals("putdouble") || name.equals("putstr") || name.equals("putln")) {
                    callOrcallname = "callname";
                    isLib = true;
                }
                expect(TokenType.L_PAREN);
                boolean hasParam = false;
                //有参数
                if (!check(TokenType.R_PAREN)) {
                    hasParam = true;
                    InstructionEntry instructionEntry1;
                    if (entry.getType().equals("void")) {
                        instructionEntry1 = new InstructionEntry("stackalloc", 0);
                    } else {
                        instructionEntry1 = new InstructionEntry("stackalloc", 1);
                    }
                    instructionEntries.add(instructionEntry1);
                    funcSymbol.setInstructions(instructionEntries);
                    analyseCallParamList(funcName);

                }
                expect(TokenType.R_PAREN);
                TokenType returnType = entry.getType();
                if (returnType.equals("int") && !hasParam) {
                    // 生成代码
                    InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 1);
                    instructionEntries.add(instructionEntry1);
                    InstructionEntry instructionEntry2;
                    if (isLib) {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name));
                    } else {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name) - 8);
                    }
                    instructionEntries.add(instructionEntry2);
                    funcSymbol.setInstructions(instructionEntries);
                } else if (returnType.equals("void") && !hasParam) {
                    // 生成代码
                    InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 0);
                    instructionEntries.add(instructionEntry1);
                    InstructionEntry instructionEntry2;
                    if (isLib) {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name));
                    } else {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name) - 8);
                    }
                    instructionEntries.add(instructionEntry2);
                    funcSymbol.setInstructions(instructionEntries);
                } else {
                    // 生成代码
                    InstructionEntry instructionEntry2;
                    if (isLib) {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name));
                    } else {
                        instructionEntry2 = new InstructionEntry(callOrcallname, getFuncIndex(name) - 8);
                    }
                    instructionEntries.add(instructionEntry2);
                    funcSymbol.setInstructions(instructionEntries);
                }
                return returnType;
            }
            //赋值
            else if (check(TokenType.ASSIGN)) {
                if (!entry.getType().equals("int")) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
                }
                if (entry.isConstant()) {
                    throw new AnalyzeError(ErrorCode.AssignToConstant, nameToken.getStartPos());
                }
                expect(TokenType.ASSIGN);
                // 生成代码
                ArrayList<String> localVars = funcSymbol.getLocVars();
                ArrayList<String> paramVars = funcSymbol.getParamVars();
                if (entry.getType().equals("param")) {
                    int index = -1;
                    for (int i = 0; i < paramVars.size(); i++) {
                        if (paramVars.get(i).equals(name)) {
                            index = i;
                        }
                    }
                    if (index == -1) {
                        throw new AnalyzeError(ErrorCode.NotDeclared, peek().getStartPos());
                    }
                    if (funcSymbol.getType().equals("void")) {
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", index - 1);
                        instructionEntries.add(instructionEntry1);
                    } else {
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", index);
                        instructionEntries.add(instructionEntry1);
                    }
                } else {
                    int index = -1;
                    for (int i = 0; i < localVars.size(); i++) {
                        if (localVars.get(i).equals(name)) {
                            index = i;
                        }
                    }
                    InstructionEntry instructionEntry1;
                    if (index == -1) {
                        index = getGlobalIndex(name);
                        instructionEntry1 = new InstructionEntry("globa", index);
                    } else {
                        instructionEntry1 = new InstructionEntry("loca", index);
                    }
                    instructionEntries.add(instructionEntry1);
                }
                funcSymbol.setInstructions(instructionEntries);
                TokenType type = analyseExpr(funcName);
                if (type.equals("void")) {
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getStartPos());
                }
                // 生成代码
                InstructionEntry instructionEntry2 = new InstructionEntry("store64");
                instructionEntries.add(instructionEntry2);
                funcSymbol.setInstructions(instructionEntries);
                return TokenType.VOID;
            }
            //变量名
            else {
                // 生成代码
                ArrayList<String> localVars = funcSymbol.getLocVars();
                ArrayList<String> paramVars = funcSymbol.getParamVars();
                if (entry.getType().equals("param")) {
                    int index = -1;
                    for (int i = 0; i < paramVars.size(); i++) {
                        if (paramVars.get(i).equals(name)) {
                            index = i;
                        }
                    }
                    if (index == -1) {
                        throw new AnalyzeError(ErrorCode.NotDeclared, peek().getStartPos());
                    }
                    if (funcSymbol.getType().equals("void")) {
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", index - 1);
                        instructionEntries.add(instructionEntry1);
                    } else {
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", index);
                        instructionEntries.add(instructionEntry1);
                    }
                } else {
                    int index = -1;
                    for (int i = 0; i < localVars.size(); i++) {
                        if (localVars.get(i).equals(name)) {
                            index = i;
                        }
                    }
                    InstructionEntry instructionEntry1;
                    if (index == -1) {
                        index = getGlobalIndex(name);
                        instructionEntry1 = new InstructionEntry("globa", index);
                    } else {
                        instructionEntry1 = new InstructionEntry("loca", index);
                    }
                    instructionEntries.add(instructionEntry1);
                }
                InstructionEntry instructionEntry2 = new InstructionEntry("load64");
                instructionEntries.add(instructionEntry2);
                funcSymbol.setInstructions(instructionEntries);
                return entry.getType();
            }
        } else if (check(TokenType.UINT_LITERAL)) {
            Token token = expect(TokenType.UINT_LITERAL);
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("push", (int) token.getValue());
            instructionEntries.add(instructionEntry1);
            funcSymbol.setInstructions(instructionEntries);
            return TokenType.INT;
        } else if (check(TokenType.STRING_LITERAL)) {
            Token token = expect(TokenType.STRING_LITERAL);
            String value = (String) token.getValue();
            //计算全局变量数
            int globalVarsNum = calcGlobalVars();
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("push", globalVarsNum);
            instructionEntries.add(instructionEntry1);
            funcSymbol.setInstructions(instructionEntries);
            //加入符号表
            addSymbol(value, "string", TokenType.UINT_LITERAL,0,true,true,token.getStartPos());
            return TokenType.INT;
        } else if (check(TokenType.CHAR_LITERAL)) {
            Token token = expect(TokenType.CHAR_LITERAL);
            // 生成代码
            String charStr = (String) token.getValue();
            char charCh = 0;
            for (int i = 0; i < charStr.length(); i++) {
                charCh = charStr.charAt(i);
            }
            InstructionEntry instructionEntry1 = new InstructionEntry("push", charCh);
            instructionEntries.add(instructionEntry1);
            funcSymbol.setInstructions(instructionEntries);
            return TokenType.INT;
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            expect(TokenType.DOUBLE_LITERAL);
            return TokenType.DOUBLE;
        } else if (check(TokenType.L_PAREN)) {
            expect(TokenType.L_PAREN);
            TokenType type = analyseExpr(funcName);
            expect(TokenType.R_PAREN);
            return type;
        }
        return null;
    }

    public int calcGlobalVars() {
        int globalVars = 0;
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            if(!symbolEntry.getType().equals("func") && symbolEntry.getLevel() == 0){
                globalVars++;
            }
        }
        return globalVars;
    }

    public void analyseCallParamList(String funcName) throws CompileError {
        analyseExpr(funcName);
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseExpr(funcName);
        }
    }

    public HashMap<String,SymbolEntry> getSymbolTable() {
        return this.symbolTable;
    }


//    //    expr ->
////    operator_expr
////    | negate_expr
////    | assign_expr
////    | as_expr
////    | call_expr
////    | literal_expr
////    | ident_expr
////    | group_expr
//    public TokenType analyseExpr(String funcName) throws CompileError {
//        TokenType tokenType;
//        if (check(TokenType.MINUS)) {
//            tokenType = analyseNegateExpr(Ins, InsB);
//        } else if (check(TokenType.L_PAREN)) {
//            tokenType = analyseGroupExpr(Ins, InsB);
//        }
////        literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL
//        else if (check(TokenType.UINT_LITERAL)) {
//            Token token = expect(TokenType.UINT_LITERAL);
//            Ins.add(Instruction.push((Long) token.getValue()));
//            InsB.add(Instruction.pushB((Long) token.getValue()));
//            return TokenType.INT;
//        } else if (check(TokenType.DOUBLE_LITERAL)) {
//            Token token = expect(TokenType.DOUBLE_LITERAL);
//            Ins.add(Instruction.pushd((Double) token.getValue()));
//            InsB.add(Instruction.pushdB((Double) token.getValue()));
//            return TokenType.DOUBLE;
//        } else if (check(TokenType.STRING_LITERAL)) {
//            Token token = expect(TokenType.STRING_LITERAL);
//            String string = token.getValueString();
//            GlobalVar globalVar = new GlobalVar();
//            globalVar.setName(string);
//            globalVar.setIsConst(Instruction.toBinary(1, 8));
//            globalVar.setValue(convertToBin(string));
//            globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//            globalVar.setValueBytes(string.getBytes());
//            Integer globalIndex = getGlobalIndex(token.getValueString());
//            if (globalIndex < 0) {
//                globalVarList.add(globalVar);
//                globalIndex = globalVarList.size() - 1;
//            }
//            Ins.add(Instruction.push(globalIndex));
//            InsB.add(Instruction.pushB(globalIndex));
//            return TokenType.INT;
//        } else {
//            tokenType = analyseAssignOrCallOrIdentExpr(Ins, InsB);
//        }
//        return tokenType;
//    }
//
//
//    public TokenType analyseAssignOrCallOrIdentExpr(List<String> Ins, List<byte[]> InsB) throws CompileError {
//        Token identToken = expect(TokenType.IDENT);
//        Symbol symbol = new Symbol();
//        symbol.setToken(identToken);
//        Symbol identSymbol = getSymbol(symbol);
////        call_expr -> IDENT '(' call_param_list? ')'
////        call_param_list -> expr (',' expr)*
//        if (check(TokenType.L_PAREN)) {
//            expect(TokenType.L_PAREN);
//            if (identSymbol == null) {
//                String funcName = identToken.getValueString();
//                GlobalVar globalVar;
//                Integer globalIndex;
//                switch (funcName) {
//                    case "getint":
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("getint");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("getint"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.stackalloc(1));
//                        InsB.add(Instruction.stackallocB(1));
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.INT;
//                    case "getchar":
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("getchar");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("getchar"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.stackalloc(1));
//                        InsB.add(Instruction.stackallocB(1));
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.INT;
//                    case "getdouble":
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("getdouble");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("getdouble"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.stackalloc(1));
//                        InsB.add(Instruction.stackallocB(1));
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.DOUBLE;
//                    case "putint":
//                        Ins.add(Instruction.stackalloc(0));
//                        InsB.add(Instruction.stackallocB(0));
//                        TokenType ret = opg(Ins, InsB);
//                        if (ret != TokenType.INT)
//                            throw new AnalyzeError(ErrorCode.InvalidInput, identToken.getStartPos());
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("putint");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("putint"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.VOID;
//                    case "putdouble":
//                        Ins.add(Instruction.stackalloc(0));
//                        InsB.add(Instruction.stackallocB(0));
//                        ret = opg(Ins, InsB);
//                        if (ret != TokenType.INT)
//                            throw new AnalyzeError(ErrorCode.InvalidInput, identToken.getStartPos());
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("putdouble");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("putdouble"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.VOID;
//                    case "putchar":
//                        Ins.add(Instruction.stackalloc(0));
//                        InsB.add(Instruction.stackallocB(0));
//                        expect(TokenType.R_PAREN);
//                        ret = opg(Ins, InsB);
//                        if (ret != TokenType.INT)
//                            throw new AnalyzeError(ErrorCode.InvalidInput, identToken.getStartPos());
//                        globalVar = new GlobalVar();
//                        globalVar.setName("putchar");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("putchar"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.VOID;
//                    case "putstr":
//                        Ins.add(Instruction.stackalloc(0));
//                        InsB.add(Instruction.stackallocB(0));
//                        expect(TokenType.R_PAREN);
//                        ret = opg(Ins, InsB);
//                        if (ret != TokenType.INT)
//                            throw new AnalyzeError(ErrorCode.InvalidInput, identToken.getStartPos());
//                        globalVar = new GlobalVar();
//                        globalVar.setName("putstr");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("putstr"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.VOID;
//                    case "putln":
//                        Ins.add(Instruction.stackalloc(0));
//                        InsB.add(Instruction.stackallocB(0));
//                        expect(TokenType.R_PAREN);
//                        globalVar = new GlobalVar();
//                        globalVar.setName("putln");
//                        globalVar.setIsConst(Instruction.toBinary(1, 8));
//                        globalVar.setValue(convertToBin("putln"));
//                        globalVar.setConstBytes(Instruction.intToBytes(1, 1));
//                        globalVar.setValueBytes(funcName.getBytes());
//                        globalVarList.add(globalVar);
//                        globalIndex = globalVarList.size() - 1;
//                        Ins.add(Instruction.callname(globalIndex));
//                        InsB.add(Instruction.callnameB(globalIndex));
//                        return TokenType.VOID;
//                    default:
//                        throw new AnalyzeError(ErrorCode.InvalidIdentifier, identToken.getStartPos());
//                }
//            } else {
//                if (!identSymbol.getKind().equals("var")) {
//                    throw new AnalyzeError(ErrorCode.DuplicateDeclaration, identToken.getStartPos());
//                }
//                expect(TokenType.L_PAREN);
//                if (identSymbol.getRetType() == TokenType.VOID) {
//                    Ins.add(Instruction.stackalloc(0));
//                    InsB.add(Instruction.stackallocB(0));
//                } else {
//                    Ins.add(Instruction.stackalloc(1));
//                    InsB.add(Instruction.stackallocB(1));
//                }
//                if (check(TokenType.UINT_LITERAL) || check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.IDENT) || check(TokenType.L_PAREN) || check(TokenType.MINUS)) {
//                    int i = 0;
//                    List<TokenType> paramTypes = identSymbol.getParamsType();
//                    do {
//                        if (i == paramTypes.size())
//                            throw new AnalyzeError(ErrorCode.InvalidInput, peek().getStartPos());
//                        Token peekToken = peek();
//                        TokenType ret = opg(Ins, InsB);
//                        if (ret != paramTypes.get(i))
//                            throw new AnalyzeError(ErrorCode.InvalidInput, peekToken.getStartPos());
//                        i++;
//                    } while (nextIf(TokenType.COMMA) != null);
//                }
//                Integer funcIndex = funcList.indexOf((findFunc(identToken.getValueString())));
//                Ins.add(Instruction.call(funcIndex));
//                InsB.add(Instruction.callB(funcIndex));
//            }
//            expect(TokenType.R_PAREN);
//            return identSymbol.getRetType();
//        }
////        assign_expr -> l_expr '=' expr
////        l_expr -> IDENT
//        else if (check(TokenType.ASSIGN)) {
//            expect(TokenType.ASSIGN);
//            if (identSymbol == null) {
//                throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
//            }
//            if (identSymbol.isConstant()) {
//                throw new AnalyzeError(ErrorCode.AssignToConstant, identToken.getStartPos());
//            }
//            if (identSymbol.getKind().equals("func")) {
//                throw new AnalyzeError(ErrorCode.InvalidAssignment, identToken.getStartPos());
//            }
//            if (identSymbol.isGlobal()) {
//                Integer globalIndex = getGlobalIndex(identToken.getValueString());
//                Ins.add(Instruction.globa(globalIndex));
//                InsB.add(Instruction.globaB(globalIndex));
//            } else if (identSymbol.getKind().equals("param")) {
//                Integer paramIndex = paramList.indexOf(identSymbol);
//                if (funcType != TokenType.VOID) {
//                    Ins.add(Instruction.arga(paramIndex + 1));
//                    InsB.add(Instruction.argaB(paramIndex + 1));
//                } else {
//                    Ins.add(Instruction.arga(paramIndex));
//                    InsB.add(Instruction.argaB(paramIndex));
//                }
//            } else {
//                Integer localIndex = getLocalIndex(identSymbol.getLocalNum());
//                Ins.add(Instruction.loca(localIndex));
//                InsB.add(Instruction.locaB(localIndex));
//            }
//            TokenType retType = opg(insList, insListB);
//            if (identSymbol.getRetType() != retType) {
//                throw new AnalyzeError(ErrorCode.InvalidAssignment, identToken.getStartPos());
//            }
//            insList.add(Instruction.store(64));
//            insListB.add(Instruction.store64());
//            return TokenType.VOID;
//        } else {
//            if (identSymbol == null) {
//                throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
//            }
//            if (identSymbol.getKind().equals("func")) {
//                throw new AnalyzeError(ErrorCode.InvalidAssignment, identToken.getStartPos());
//            }
//            if (symbol.isGlobal()) {
//                Integer globalIndex = getGlobalIndex(identToken.getValueString());
//                Ins.add(Instruction.globa(globalIndex));
//                InsB.add(Instruction.globaB(globalIndex));
//            } else if (symbol.getKind().equals("param")) {
//                Integer paramIndex = paramList.indexOf(identSymbol);
//                if (funcType != TokenType.VOID) {
//                    Ins.add(Instruction.arga(paramIndex + 1));
//                    InsB.add(Instruction.argaB(paramIndex + 1));
//                } else {
//                    Ins.add(Instruction.arga(paramIndex));
//                    InsB.add(Instruction.argaB(paramIndex));
//                }
//            } else {
//                Integer localIndex = getLocalIndex(identSymbol.getLocalNum());
//                Ins.add(Instruction.loca(localIndex));
//                InsB.add(Instruction.locaB(localIndex));
//            }
//            Ins.add(Instruction.load(64));
//            InsB.add(Instruction.load64());
//            return identSymbol.getRetType();
//        }
//    }
//
//
//    //    group_expr -> '(' expr ')'
//    public TokenType analyseGroupExpr(List<String> Ins, List<byte[]> InsB) throws CompileError {
//        expect(TokenType.L_PAREN);
//        TokenType retType = opg(Ins, InsB);
//        expect(TokenType.R_PAREN);
//        return retType;
//    }
//
//    //    binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
//    public Token analyseBinaryOperator() throws CompileError {
//        if (check(TokenType.PLUS)) {
//            return expect(TokenType.PLUS);
//        } else if (check(TokenType.MINUS)) {
//            return expect(TokenType.MINUS);
//        } else if (check(TokenType.MUL)) {
//            return expect(TokenType.MUL);
//        } else if (check(TokenType.DIV)) {
//            return expect(TokenType.DIV);
//        } else if (check(TokenType.EQ)) {
//            return expect(TokenType.EQ);
//        } else if (check(TokenType.NEQ)) {
//            return expect(TokenType.NEQ);
//        } else if (check(TokenType.LT)) {
//            return expect(TokenType.LT);
//        } else if (check(TokenType.GT)) {
//            return expect(TokenType.GT);
//        } else if (check(TokenType.LE)) {
//            return expect(TokenType.LE);
//        } else if (check(TokenType.GE)) {
//            return expect(TokenType.GE);
//        } else if (check(TokenType.AS_KW)) {
//            return expect(TokenType.AS_KW);
//        } else {
//            return null;
//        }
//    }
//
//    public TokenType analyseNegateExpr(List<String> Ins, List<byte[]> InsB) throws CompileError {
//        Token token = expect(TokenType.MINUS);
//        TokenType retType = analyseExpr(Ins, InsB);
//        if (retType == TokenType.VOID)
//            throw new AnalyzeError(ErrorCode.InvalidAssignment, token.getEndPos());
//        if (retType == TokenType.INT) {
//            Ins.add(Instruction.negi());
//            InsB.add(Instruction.negiB());
//        } else if (retType == TokenType.DOUBLE) {
//            Ins.add(Instruction.negf());
//            InsB.add(Instruction.negfB());
//        }
//        return retType;
//    }

}
