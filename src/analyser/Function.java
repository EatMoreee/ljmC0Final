package analyser;

import instruction.InstructionEntry;

public class Function {
    public int nameLoc;
    public int ret_slots;
    public int param_slots;
    public int loc_slots;
    public int body_count;
    public InstructionEntry[] instructions;
    public Function(int nameLoc, int ret_slots, int param_slots, int loc_slots, int body_count, InstructionEntry[] instructions){
        this.nameLoc = nameLoc;
        this.ret_slots = ret_slots;
        this.param_slots = param_slots;
        this.loc_slots = loc_slots;
        this.body_count = body_count;
        this.instructions = instructions;
    }
}
