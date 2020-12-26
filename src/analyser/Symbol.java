package analyser;

import java.util.HashMap;
import instruction.InstructionEntry;

public class Symbol {
    String type;
    int layer;
    boolean isConstant;
    String returnType = null;

    public InstructionEntry[] getInstructions() {
        return instructions;
    }

    public void setInstructions(InstructionEntry[] instrucions) {
        this.instructions = instrucions;
    }

    boolean isValid = true;
    InstructionEntry[] instructions = new InstructionEntry[1000];
    int instructionLen = 0;
    int locaVarCount = 0;
    int argVarCount = 1;
    HashMap<String, Integer> localVars = new HashMap<>();
    HashMap<String, Integer> argVars = new HashMap<>();
    boolean isParam = false;

    public int getArgVarCount() {
        return argVarCount;
    }

    public void setArgVarCount(int argVarCount) {
        this.argVarCount = argVarCount;
    }

    public HashMap<String, Integer> getArgVars() {
        return argVars;
    }

    public void setArgVars(HashMap<String, Integer> argVars) {
        this.argVars = argVars;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    public HashMap<String, Integer> getLocalVars() {
        return localVars;
    }

    public void setLocalVars(HashMap<String, Integer> localVars) {
        this.localVars = localVars;
    }

    public int getLocaVarCount() {
        return locaVarCount;
    }

    public void setLocaVarCount(int locaVarCount) {
        this.locaVarCount = locaVarCount;
    }

    public int getInstructionLen() {
        return instructionLen;
    }

    public void setInstructionLen(int instructionLen) {
        this.instructionLen = instructionLen;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    boolean isInitialized;
    int stackOffset;

    /**
     * @param isConstant
     * @param isDeclared
     * @param stackOffset
     */
    public Symbol(String type, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;
    }
    public Symbol(String type, String returnType, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;
        this.returnType = returnType;
    }
    public Symbol(String type, String returnType, InstructionEntry[] instructions, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;
        this.returnType = returnType;
        this.instructions = instructions;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
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
