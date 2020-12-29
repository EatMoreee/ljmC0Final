package analyser;

public class Global {
    private int isConst;
    private int valueCount;
    private String valueItem;
    public Global(int isConst, int valueCount, String valueItem){
        this.isConst = isConst;
        this.valueCount = valueCount;
        this.valueItem = valueItem;
    }

    public int getIsConst() {
        return isConst;
    }

    public void setIsConst(int isConst) {
        this.isConst = isConst;
    }

    public int getValueCount() {
        return valueCount;
    }

    public void setValueCount(int valueCount) {
        this.valueCount = valueCount;
    }

    public String getValueItem() {
        return valueItem;
    }

    public void setValueItem(String valueItem) {
        this.valueItem = valueItem;
    }
}

