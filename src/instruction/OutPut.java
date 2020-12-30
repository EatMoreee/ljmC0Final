package instruction;

import analyser.*;
import error.CompileError;
import tokenizer.*;

import java.io.*;
import java.util.*;

public class OutPut {
    private String inPath;
    private String outPath;

    public String getInPath() {
        return inPath;
    }

    public void setInPath(String inPath) {
        this.inPath = inPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public void output() throws IOException {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(inPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringIter it = new StringIter(sc);
        Tokenizer tn = new Tokenizer(it);
        Analyser an = new Analyser(tn);
        try {
            an.analyse();
        } catch (CompileError compileError) {
            compileError.printStackTrace();
        }
        HashMap<String, SymbolEntry> symbolTable = an.getSymbolTable();
        Iterator iter = symbolTable.entrySet().iterator();
        int top = 0;
        int trueGlobalVarsCount = 0;
        Global[] globals = new Global[1000];
        int globalCount = 0;
        Function[] functions = new Function[1000];
        int functionCount = 0;
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            if(!symbolEntry.getType().equals("func")){
                trueGlobalVarsCount++;
                if(symbolEntry.getType().equals("string")){
                    String name = (String)entry.getKey();
                    System.out.println(name);
                    globals[top++] = new Global(symbolEntry.isConstant() ? 1 : 0, name.length(), name);
                }
                else{
                    globals[top++] = new Global(symbolEntry.isConstant() ? 1 : 0, 8, "0");
                }
            }
        }
        int globalVarsEnd = top;
        iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name = entry.getKey().toString();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            if(symbolEntry.getType().equals("func")){
                int funcIndex = an.getFuncIndex(name);
                globals[funcIndex + globalVarsEnd] = new Global(1, name.length(), name);
                top++;
            }
        }
        globalCount = top;
        System.out.println("全局变量表：");
        for(int i = 0;i < top;i++){
            System.out.println(globals[i].getIsConst() + " " + globals[i].getValueCount() + " " + globals[i].getValueItem());
        }
        int funcTableTop = 0;
        for(int i = globalVarsEnd + 8;i < globalCount;i++){
            String funcName = globals[i].getValueItem();
            SymbolEntry funcEntry = symbolTable.get(funcName);
            int ret_slots = 0;
            if(funcEntry.getType().equals("int")){
                ret_slots = 1;
            }
            int param_slots = funcEntry.getParamVars().size();
            int loc_slots = funcEntry.getLocVars().size();
            ArrayList<InstructionEntry> instructionEntries = funcEntry.getInstructions();
            int body_count = instructionEntries.size();
            if(funcName.equals("_start")){
                functions[funcTableTop++] = new Function(i, 0, 0, 0, body_count, instructionEntries);
            }
            else{
                functions[funcTableTop++] = new Function(i, ret_slots, param_slots - 1, loc_slots, body_count, instructionEntries);
            }
        }
        functionCount = funcTableTop;
        System.out.println("函数表：");
        for(int i = 0;i < funcTableTop;i++){
            System.out.println(functions[i].getNameLoc() + " " + functions[i].getRet_slots() + " " + functions[i].getParam_slots() + " " + functions[i].getLoc_slots() + " " + functions[i].getBody_count());
            for(int j = 0;j < functions[i].getBody_count();j++){
                System.out.println(functions[i].getInstructions().get(j).getIns() + "(" + functions[i].getInstructions().get(j).getOp() + ")");
            }
        }
        System.out.println(globalCount);
        System.out.println(functionCount);
        List<Byte> output = new ArrayList<>();
        //magic
        List<Byte> magic=int2bytes(4,0x72303b3e);
        output.addAll(magic);
        //version
        List<Byte> version=int2bytes(4,0x00000001);
        output.addAll(version);
        //globals.count
        List<Byte> globalCountByte=int2bytes(4, globalCount);
        output.addAll(globalCountByte);
        for(int i = 0;i < globalCount;i++){
            //isConst
            List<Byte> isConst=int2bytes(1, globals[i].getIsConst());
            output.addAll(isConst);
            // value count
            List<Byte> globalValueCountByte;
            //value items
            List<Byte> globalValueItemByte;
            if(globals[i].getValueItem().equals("0")){
                globalValueCountByte = int2bytes(4, 8);
                globalValueItemByte = long2bytes(8,0L);
            }
            else {
                globalValueItemByte = String2bytes(globals[i].getValueItem());
                globalValueCountByte = int2bytes(4, globals[i].getValueCount());
            }
            output.addAll(globalValueCountByte);
            output.addAll(globalValueItemByte);
        }
        //functions.count
        List<Byte> functionCountByte=int2bytes(4, functionCount);
        output.addAll(functionCountByte);
        //functions
        for(int i = 0;i < functionCount;i++){
            //name
            List<Byte> name = int2bytes(4,functions[i].getNameLoc());
            output.addAll(name);
            //retSlots
            List<Byte> retSlots = int2bytes(4,functions[i].getRet_slots());
            output.addAll(retSlots);
            //paramsSlots;
            List<Byte> paramsSlots=int2bytes(4,functions[i].getParam_slots());
            output.addAll(paramsSlots);
            //locSlots;
            List<Byte> locSlots=int2bytes(4,functions[i].getLoc_slots());
            output.addAll(locSlots);
            //bodyCount
            List<Byte> bodyCount=int2bytes(4, functions[i].getBody_count());
            output.addAll(bodyCount);
            //instructions
            for(int j = 0;j < functions[i].getBody_count();j++){
                InstructionEntry instructionEntry = functions[i].getInstructions().get(j);
                int intInstru = instruToInt(instructionEntry.getIns());
                List<Byte> instruByte = int2bytes(1, intInstru);
                output.addAll(instruByte);
                if(instructionEntry.getOp() != -100010){
                    int opera = instructionEntry.getOp();
                    if(intInstru == 0x4a){
                        //System.out.println(trueGlobalVarsCount);
                        opera = opera + trueGlobalVarsCount;
                        System.out.println();
                        System.out.println(opera);
                    }
                    boolean is64OrNot = is64(instructionEntry.getIns());
                    if(is64OrNot){
                        List<Byte> operaByte = long2bytes(8, opera);
                        output.addAll(operaByte);
                    }
                    else{
                        List<Byte> operaByte = int2bytes(4, opera);
                        output.addAll(operaByte);
                    }
                }
            }
        }
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(new File(outPath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Byte> bytes = output;
        byte[] resultBytes = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); ++i) {
            resultBytes[i] = bytes.get(i);
        }
        out.write(resultBytes);
    }
    private static List<Byte> Char2bytes(char value) {
        List<Byte>  AB=new ArrayList<>();
        AB.add((byte)(value&0xff));
        return AB;
    }

    private static List<Byte> String2bytes(String valueString) {
        List<Byte>  AB=new ArrayList<>();
        for (int i=0;i<valueString.length();i++){
            char ch=valueString.charAt(i);
            AB.add((byte)(ch&0xff));
        }
        return AB;
    }

    private static ArrayList<Byte> long2bytes(int length, long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }

    private static ArrayList<Byte> int2bytes(int length, int target){
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }
    private static int instruToInt(String name){
        switch (name){
            case "stackalloc":
                return 0x1a;
            case "call":
                return 0x48;
            case "callname":
                return 0x4a;
            case "loca":
                return 0x0a;
            case "store64":
                return 0x17;
            case "arga":
                return 0x0b;
            case "load64":
                return 0x13;
            case "push":
                return 0x01;
            case "ret":
                return 0x49;
            case "br":
                return 0x41;
            case "globa":
                return 0x0c;
            case "cmpi":
                return 0x30;
            case "brfalse":
                return 0x42;
            case "brtrue":
                return 0x43;
            case "setlt":
                return 0x39;
            case "setgt":
                return 0x3a;
            case "addi":
                return 0x20;
            case "subi":
                return 0x21;
            case "multi":
                return 0x22;
            case "divi":
                return 0x23;
            case "negi":
                return 0x34;
            case "itof":
                return 0x36;
            case "ftoi":
                return 0x37;
        }
        return 0;
    }
    private static boolean is64(String name){
        switch (name){
            case "stackalloc":
                return false;
            case "call":
                return false;
            case "callname":
                return false;
            case "loca":
                return false;
            case "arga":
                return false;
            case "push":
                return true;
            case "br":
                return false;
            case "globa":
                return false;
            case "brfalse":
                return false;
            case "brtrue":
                return false;
        }
        return false;
    }

}
