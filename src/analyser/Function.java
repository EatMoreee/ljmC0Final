package analyser;

import instruction.InstructionEntry;

public class Function {
    int nameLoc;
    int ret_slots;
    int param_slots;
    int loc_slots;
    int body_count;
    InstructionEntry[] instructions;
    public Function(int nameLoc, int ret_slots, int param_slots, int loc_slots, int body_count, InstructionEntry[] instructions){
        this.nameLoc = nameLoc;
        this.ret_slots = ret_slots;
        this.param_slots = param_slots;
        this.loc_slots = loc_slots;
        this.body_count = body_count;
        this.instructions = instructions;
    }

    public int getNameLoc() {
        return nameLoc;
    }

    public void setNameLoc(int nameLoc) {
        this.nameLoc = nameLoc;
    }

    public int getRet_slots() {
        return ret_slots;
    }

    public void setRet_slots(int ret_slots) {
        this.ret_slots = ret_slots;
    }

    public int getParam_slots() {
        return param_slots;
    }

    public void setParam_slots(int param_slots) {
        this.param_slots = param_slots;
    }

    public int getLoc_slots() {
        return loc_slots;
    }

    public void setLoc_slots(int loc_slots) {
        this.loc_slots = loc_slots;
    }

    public int getBody_count() {
        return body_count;
    }

    public void setBody_count(int body_count) {
        this.body_count = body_count;
    }

    public InstructionEntry[] getInstructions() {
        return instructions;
    }

    public void setInstructions(InstructionEntry[] instructions) {
        this.instructions = instructions;
    }
}
