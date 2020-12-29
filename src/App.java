import error.*;
import instruction.OutPut;

import java.io.*;

public class App {
    public static void main(String[] args) throws IOException, CompileError {
        OutPut outPut = new OutPut();
        outPut.setInPath(args[0]);
        outPut.setOutPath(args[1]);
        outPut.output();
    }
}
