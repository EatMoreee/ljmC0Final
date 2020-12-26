package instruction;

import java.util.List;

public class Instruction {

    public static String nop() { return convertToBinary("00"); }
    public static String push(Long i) {
        return "push("+i+")";
    }
    public static String push(Integer i) {
        return "push("+i+")";
    }
    public static byte[] pushB(Long i) {
        byte[] bytes = new byte[9];
        bytes[0] = 0x01;
        return addBytes(bytes, longToBytes(i, 8), 1);
    }
    public static byte[] pushB(Integer i) {
        byte[] bytes = new byte[9];
        bytes[0] = 0x01;
        return addBytes(bytes, intToBytes(i, 8), 1);
    }
    public static String pushd(double d) {
        return "push("+Double.doubleToRawLongBits(d)+")";
    }
    public static byte[] pushdB(double d) {
        byte[] bytes = new byte[9];
        bytes[0] = 0x01;
        return addBytes(bytes, doubleToBytes(d), 1);
    }
    public static String pop() {
        return convertToBinary("02");
    }
    public static String popn(Integer i) {
        return "popn("+i+")";
    }
    public static byte[] popnB(Integer i) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x03;
        return addBytes(bytes, intToBytes(i, 4), 1);
    }
    public static String dup() {
        return convertToBinary("04");
    }
    public static String loca(int off) {
        return "loca("+off+")";
    }
    public static byte[] locaB(int off) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x0a;
        return addBytes(bytes, intToBytes(off, 4), 1);
    }
    public static String arga(int off) {
        return "arga("+off+")";
    }
    public static byte[] argaB(int off) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x0b;
        return addBytes(bytes, intToBytes(off, 4), 1);
    }
    public static String globa(int n) {
        return "globa("+n+")";
    }
    public static byte[] globaB(int n) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x0c;
        return addBytes(bytes, intToBytes(n, 4), 1);
    }
    public static String load(int n) {
        if (n == 8)
            return convertToBinary("10");
        if (n == 16)
            return convertToBinary("11");
        if (n == 32)
            return convertToBinary("12");
        if (n == 64)
            return "load64";
        return "";
    }
    public static byte[] load64() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x13;
        return bytes;
    }
    public static String store(int n) {
        if (n == 8)
            return convertToBinary("14");
        if (n == 16)
            return convertToBinary("15");
        if (n == 32)
            return convertToBinary("16");
        if (n == 64)
            return "store64";
        return "";
    }
    public static byte[] store64() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x17;
        return bytes;
    }
    public static String alloc() {
        return convertToBinary("18");
    }
    public static String free() {
        return convertToBinary("19");
    }
    public static String stackalloc(int size) {
        return "stackalloc("+size+")";
    }
    public static byte[] stackallocB(int size) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x1a;
        return addBytes(bytes, intToBytes(size, 4), 1);
    }
    public static String addi() {
        return "addi";
    }
    public static byte[] addiB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x20;
        return bytes;
    }
    public static String subi() {
        return "subi";
    }
    public static byte[] subiB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x21;
        return bytes;
    }
    public static String muli() {
        return "muli";
    }
    public static byte[] muliB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x22;
        return bytes;
    }
    public static String divi() {
        return "divi";
    }
    public static byte[] diviB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x23;
        return bytes;
    }
    public static String addf() {
        return "addf";
    }
    public static byte[] addfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x24;
        return bytes;
    }
    public static String subf() {
        return "subf";
    }
    public static byte[] subfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x25;
        return bytes;
    }
    public static String mulf() {
        return "mulf";
    }
    public static byte[] mulfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x26;
        return bytes;
    }
    public static String divf() {
        return "divf";
    }
    public static byte[] divfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x27;
        return bytes;
    }
    public static String divu() {
        return "divu";
    }
    public static byte[] divuB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x28;
        return bytes;
    }
    public static String shl() {
        return convertToBinary("29");
    }
    public static String shr() {
        return convertToBinary("2a");
    }
    public static String and() {
        return convertToBinary("2b");
    }
    public static String or() {
        return convertToBinary("2c");
    }
    public static String xor() {
        return convertToBinary("2d");
    }
    public static String not() {
        return "not";
    }
    public static byte[] notB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x2e;
        return bytes;
    }
    public static String cmpi() {
        return "cmpi";
    }
    public static byte[] cmpiB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x30;
        return bytes;
    }
    public static String cmpu() {
        return convertToBinary("31");
    }
    public static String cmpf() {
        return "cmpf";
    }
    public static byte[] cmpfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x32;
        return bytes;
    }
    public static String negi() {
        return "negi";
    }
    public static byte[] negiB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x34;
        return bytes;
    }
    public static String negf() {
        return "negf";
    }
    public static byte[] negfB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x35;
        return bytes;
    }
    public static String itof() {
        return "itof";
    }
    public static byte[] itofB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x36;
        return bytes;
    }
    public static String ftoi() {
        return "ftoi";
    }
    public static byte[] ftoiB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x37;
        return bytes;
    }
    public static String shrl() {
        return convertToBinary("38");
    }
    public static String setlt() {
        return "setlt";
    }
    public static byte[] setltB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x39;
        return bytes;
    }
    public static String setgt() {
        return "setgt";
    }
    public static byte[] setgtB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x3a;
        return bytes;
    }
    public static String br(int off) {
        return "br("+off+")";
    }
    public static byte[] brB(int off) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x41;
        return addBytes(bytes, intToBytes(off, 4), 1);
    }
    public static String brfalse(int off) {
        return convertToBinary("42")+toBinary(off, 32);
    }
    public static String brtrue(int off) {
        return "brtrue("+off+")";
    }
    public static byte[] brtrueB(int off) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x43;
        return addBytes(bytes, intToBytes(off, 4), 1);
    }
    public static String call(int id) {
        return "call("+id+")";
    }
    public static byte[] callB(int id) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x48;
        return addBytes(bytes, intToBytes(id, 4), 1);
    }
    public static String ret() {
        return "ret";
    }
    public static byte[] retB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x49;
        return bytes;
    }
    public static String callname(int id) {
        return "callname("+id+")";
    }
    public static byte[] callnameB(int id) {
        byte[] bytes = new byte[5];
        bytes[0] = 0x4a;
        return addBytes(bytes, intToBytes(id, 4), 1);
    }
    public static String scani() {
        return convertToBinary("50");
    }
    public static String scanc() {
        return convertToBinary("51");
    }
    public static String scanf() {
        return convertToBinary("52");
    }
    public static String printi() {
        return convertToBinary("54");
    }
    public static String printc() {
        return "printc";
    }
    public static byte[] printcB() {
        byte[] bytes = new byte[1];
        bytes[0] = 0x55;
        return bytes;
    }
    public static String printf() {
        return convertToBinary("56");
    }
    public static String prints() {
        return convertToBinary("57");
    }
    public static String println() {
        return convertToBinary("58");
    }
    public static String panic() {
        return convertToBinary("fe");
    }


    public static String toBinary(Integer i, int bytes) {
        return String.format("%"+bytes+"s", Integer.toBinaryString(i)).replaceAll(" ", "0");
    }
    public static String toBinary(Long i, int bytes) {
        return String.format("%"+bytes+"s", Long.toBinaryString(i)).replaceAll(" ", "0");
    }

    public static String doubleToBinary(double d) {
        return String.format("%64s", Long.toBinaryString(Double.doubleToRawLongBits(d))).replaceAll(" ", "0");
    }

    public static String convertToBinary(String cmd) {
        return toBinary(Integer.parseInt(cmd, 16), 8);
    }

    public static byte[] cmdToBytes(String cmd) {
        int i = Integer.parseInt(cmd);
        return intToBytes(i, 1);
    }

    public static byte[] intToBytes(int value, int len) {
        long LValue=(long) value;
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte)((LValue >> (8 * i)) & 0xff);
        }
        return b;
    }

    public static byte[] longToBytes(Long value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte)((value >> (8 * i)) & 0xff);
        }
        return b;
    }

    public static byte[] doubleToBytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[8-i-1] = (byte) ((value >> (8 * i)) & 0xff);
        }
        return byteRet;
    }

    public static byte[] addBytes(byte[] b1, byte[] b2, int off) {
        for (int i = off; i < off + b2.length ;i++) {
            b1[i] = b2[i-off];
        }
        return b1;
    }

}
