package analyser;

import java.util.ArrayList;
import java.util.List;

public class Func {
    private String name;
    private int address;
    private byte[] addBytes;
    private int returnSlots;
    private byte[] returnBytes;
    private int paramSlots;
    private byte[] paramBytes;
    private int locSlots;
    private byte[] locBytes;
    private List<String> ins = new ArrayList<>();
    private List<byte[]> insB = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public byte[] getAddBytes() {
        return addBytes;
    }

    public void setAddBytes(byte[] addBytes) {
        this.addBytes = addBytes;
    }

    public int getReturnSlots() {
        return returnSlots;
    }

    public void setReturnSlots(int returnSlots) {
        this.returnSlots = returnSlots;
    }

    public byte[] getReturnBytes() {
        return returnBytes;
    }

    public void setReturnBytes(byte[] returnBytes) {
        this.returnBytes = returnBytes;
    }

    public int getParamSlots() {
        return paramSlots;
    }

    public void setParamSlots(int paramSlots) {
        this.paramSlots = paramSlots;
    }

    public byte[] getParamBytes() {
        return paramBytes;
    }

    public void setParamBytes(byte[] paramBytes) {
        this.paramBytes = paramBytes;
    }

    public int getLocSlots() {
        return locSlots;
    }

    public void setLocSlots(int locSlots) {
        this.locSlots = locSlots;
    }

    public byte[] getLocBytes() {
        return locBytes;
    }

    public void setLocBytes(byte[] locBytes) {
        this.locBytes = locBytes;
    }


    public List<byte[]> getInsB() {
        return insB;
    }

    public void setInsB(List<byte[]> insB) {
        this.insB = insB;
    }

    public List<String> getIns() {
        return ins;
    }

    public void setIns(List<String> ins) {
        this.ins = ins;
    }
}
