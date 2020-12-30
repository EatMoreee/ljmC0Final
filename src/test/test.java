package test;

//import analyser.Analyser;
//import error.CompileError;
//import error.TokenizeError;
//import instruction.Instruction;
//import org.junit.Test;
import analyser.Analyser;
import error.CompileError;
import error.TokenizeError;
import instruction.OutPut;
import org.junit.Test;
import tokenizer.StringIter;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class test {
    @Test
    public void tokenizer() throws IOException, TokenizeError {
        File file = new File("src/test/whileIns.txt");
        Scanner sc = new Scanner(file);
        StringIter it = new StringIter(sc);
        Tokenizer tokenizer = new Tokenizer(it);
        while(true) {
            Token token = tokenizer.nextToken();
            if(token.getTokenType() == TokenType.EOF)
                break;
            System.out.println(token.getValueString());
            System.out.println(token.toString());
        }
    }




    @Test
    public void simpleCompile() throws IOException, CompileError {
        OutPut outPut = new OutPut();
        outPut.setInPath("src/test/whileIns.txt");
        outPut.setOutPath("src/test/result.txt");
        outPut.output();
//        File file = new File("src/test/whileIns.txt");
//        Scanner sc = new Scanner(file);
//        StringIter it = new StringIter(sc);
//        Tokenizer tokenizer = new Tokenizer(it);
//        Analyser analyser = new Analyser(tokenizer);
//        analyser.analyse("src/test/result.txt");
    }
}
