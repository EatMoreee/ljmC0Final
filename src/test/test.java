package test;

//import analyser.Analyser;
//import error.CompileError;
//import error.TokenizeError;
//import instruction.Instruction;
//import org.junit.Test;

import analyser.Analyser;
import analyser.SymbolEntry;
import error.AnalyzeError;
import error.CompileError;
import error.ErrorCode;
import error.TokenizeError;
import instruction.OutPut;
import org.junit.Test;
import tokenizer.StringIter;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;
import util.Pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class test {
    @Test
    public void tokenizer() throws IOException, TokenizeError {
        File file = new File("src/test/whileIns.txt");
        Scanner sc = new Scanner(file);
        StringIter it = new StringIter(sc);
        Tokenizer tokenizer = new Tokenizer(it);
        while (true) {
            Token token = tokenizer.nextToken();
            if (token.getTokenType() == TokenType.EOF)
                break;
            System.out.println(token.getValueString());
            System.out.println(token.toString());
        }
    }


    @Test
    public void simpleCompile() throws IOException, CompileError {
        OutPut outPut = new OutPut();
        outPut.setInPath("src/test/ljm.txt");
        outPut.setOutPath("src/test/result.txt");
        outPut.output();
    }
    @Test
    public void symbolTableTest() throws FileNotFoundException, CompileError{
        Scanner sc = new Scanner(new File("src/test/atoi"));
        StringIter it = new StringIter(sc);
        Tokenizer tn = new Tokenizer(it);
        Analyser an = new Analyser(tn);
        an.analyse();

        HashMap<String, SymbolEntry> symbolTable = an.getSymbolTable();
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name = entry.getKey().toString();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            //SymbolEntry symbolEntry = symbolTable.get(symbolEntryIterator.next());
            System.out.print(String.format("%s %s %s %d %s\n", name, symbolEntry.getKind(), symbolEntry.getType(), symbolEntry.getLevel(),symbolEntry.isGlobal()));
        }
    }
}

