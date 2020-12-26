package analyser;

import tokenizer.Token;
import tokenizer.TokenType;
import util.Pos;

import java.util.ArrayList;
import java.util.List;

public class Symbol {

    private Token token;

    //    种类：简单变量var，函数func，参数param等
    private String Kind;
    //    类型：int，double，string,void
    private TokenType Type;
    //    值,如果是函数，则存其返回值
    private Object value;
    //如果是函数，则是参数类型
    private ArrayList<TokenType> paramsType = new ArrayList<>();

    // 是否为函数调用
    private boolean isFuncCall = false;

    // 链域
    private int chain = 0;
    // 序号位置
    private int num = 0;

    //如果是局部变量，他的id;如果不是，-1
    private int localNum;

    // 是否常量
    private boolean isConstant;
    // 是否有值
    private boolean isInitialized;

    private boolean isGlobal = false;

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getKind() {
        return Kind;
    }

    public void setKind(String kind) {
        Kind = kind;
    }

    public TokenType getType() {
        return Type;
    }


    public void setType(TokenType type) {
        Type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ArrayList<TokenType> getParamsType() {
        return paramsType;
    }

    public void setParamsType(ArrayList<TokenType> paramsType) {
        this.paramsType = paramsType;
    }

    public boolean isFuncCall() {
        return isFuncCall;
    }

    public void setFuncCall(boolean funcCall) {
        isFuncCall = funcCall;
    }

    public int getChain() {
        return chain;
    }

    public void setChain(int chain) {
        this.chain = chain;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getLocalNum() {
        return localNum;
    }

    public void setLocalNum(int localNum) {
        this.localNum = localNum;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }
}


//package analyser;
//
//import instruction.InstructionEntry;
//import tokenizer.Token;
//import tokenizer.TokenType;
//
//import java.util.ArrayList;
//
//public class Symbol {
//    private Token token;
//
////    种类：简单变量var，函数func，参数param等
//    private String kind;
////    类型：int，double，string,void
//    private TokenType type;
////    值,如果是函数，则存其返回值
//    private Object value;
//    //如果是函数，则是参数类型
//    private ArrayList<TokenType> paramsType = new ArrayList<>();
//
//    private ArrayList<InstructionEntry> instructionEntries = new ArrayList<>();
//
//    // 是否为函数调用
//    private boolean isFuncCall = false;
//    // 参数/函数返回值的类型，为int，double，void
//    private TokenType retType;
//
//    int level;
//
//    // 链域
//    private int chain = 0;
//    // 序号位置
//    private int stackOffset = 0;
//
//    //如果是局部变量，他的id;如果不是，-1
//    private int localNum;
//
//    // 是否常量
//    private boolean isConstant;
//    // 是否有值
//    private boolean isInitialized;
//
//    private boolean isGlobal = false;
//
//    public Symbol(){
//
//    }
//
//
//    public Symbol(String kind, TokenType type, int level, boolean isConstant, boolean isDeclared, int stackOffset) {
//        this.isConstant = isConstant;
//        this.isInitialized = isDeclared;
//        this.retType = retType;
//        this.stackOffset = stackOffset;
//        this.type = type;
//        this.level = level;
//        this.kind = kind;
//    }
//
//
//    public String getKind() {
//        return kind;
//    }
//
//    public void setKind(String kind) {
//        this.kind = kind;
//    }
//
//    public TokenType getType() {
//        return type;
//    }
//
//    public void setType(TokenType type) {
//        this.type = type;
//    }
//
//    public Object getValue() {
//        return value;
//    }
//
//    public void setValue(Object value) {
//        this.value = value;
//    }
//
//    public ArrayList<TokenType> getParamsType() {
//        return paramsType;
//    }
//
//    public void setParamsType(ArrayList<TokenType> paramsType) {
//        this.paramsType = paramsType;
//    }
//
//    public ArrayList<InstructionEntry> getInstructionEntries() {
//        return instructionEntries;
//    }
//
//    public void setInstructionEntries(ArrayList<InstructionEntry> instructionEntries) {
//        this.instructionEntries = instructionEntries;
//    }
//
//    public boolean isFuncCall() {
//        return isFuncCall;
//    }
//
//    public void setFuncCall(boolean funcCall) {
//        isFuncCall = funcCall;
//    }
//
//    public TokenType getRetType() {
//        return retType;
//    }
//
//    public void setRetType(TokenType retType) {
//        this.retType = retType;
//    }
//
//    public int getChain() {
//        return chain;
//    }
//
//    public void setChain(int chain) {
//        this.chain = chain;
//    }
//
//    public int getLevel() {
//        return level;
//    }
//
//    public void setLevel(int level) {
//        this.level = level;
//    }
//
//    public int getStackOffset() {
//        return stackOffset;
//    }
//
//    public void setStackOffset(int stackOffset) {
//        this.stackOffset = stackOffset;
//    }
//
//    public int getLocalNum() {
//        return localNum;
//    }
//
//    public void setLocalNum(int localNum) {
//        this.localNum = localNum;
//    }
//
//    public boolean isConstant() {
//        return isConstant;
//    }
//
//    public void setConstant(boolean constant) {
//        isConstant = constant;
//    }
//
//    public boolean isInitialized() {
//        return isInitialized;
//    }
//
//    public void setInitialized(boolean initialized) {
//        isInitialized = initialized;
//    }
//
//    public boolean isGlobal() {
//        return isGlobal;
//    }
//
//    public void setGlobal(boolean global) {
//        isGlobal = global;
//    }
//}
