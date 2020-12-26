package analyser;

import tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class GlobalVar {
    private String name;
    private String isConst;
    private byte[] constBytes;
    private ArrayList<String> value = new ArrayList<>();
    private TokenType retType;
    private byte[] valueBytes;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsConst() {
        return isConst;
    }

    public void setIsConst(String isConst) {
        this.isConst = isConst;
    }

    public byte[] getConstBytes() {
        return constBytes;
    }

    public void setConstBytes(byte[] constBytes) {
        this.constBytes = constBytes;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(ArrayList<String> value) {
        this.value = value;
    }

    public TokenType getRetType() {
        return retType;
    }

    public void setRetType(TokenType retType) {
        this.retType = retType;
    }

    public byte[] getValueBytes() {
        return valueBytes;
    }

    public void setValueBytes(byte[] valueBytes) {
        this.valueBytes = valueBytes;
    }
}
