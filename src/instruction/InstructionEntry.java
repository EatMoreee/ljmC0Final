package instruction;

public class InstructionEntry {
    private String ins;
    private int op = -100010;

    public InstructionEntry(String ins) {
        this.ins = ins;
    }
    public InstructionEntry(String ins, int op){
        this.ins = ins;
        this.op = op;
    }

    public String getIns() {
        return ins;
    }

    public void setIns(String ins) {
        this.ins = ins;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }
}
