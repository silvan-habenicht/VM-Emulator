package de.nand2tetris;

import java.io.*;

import static de.nand2tetris.CommandType.*;

public class CodeWriter extends BufferedWriter{

    private String fileName;
    private int jumpCounter = 0;
    private int functionCounter = 0;

    CodeWriter(File file) throws IOException {

        super(new OutputStreamWriter(new FileOutputStream(file)));

        writeInit();

    }

    private void writeInit() throws IOException {

        write("// Dem Stackpointer die Adresse 256 zuweisen"); // Kommentarbeschreibung
        newLine();
        write("@256");   // A=256
        newLine();
        write("D=A"); // D=256
        newLine();
        write("@SP");   // @RAM[0]
        newLine();
        write("M=D"); // SP=256
        newLine();

        writeCall("Sys.init",0);

    }

    // Project 7 methods

    public void setFileName(String fileName) {
        this.fileName = fileName.replace(".vm","");
    }

    public void writeArithmetic(String command) throws IOException {

        write("// " + command); // Kommentarbeschreibung
        newLine();

        if(command.equals("add")) { add(); return; }
        if(command.equals("sub")) { sub(); return; }
        if(command.equals("neg")) { neg(); return; }
        if(command.equals("eq"))  { eq();  return; }
        if(command.equals("gt"))  { gt();  return; }
        if(command.equals("lt"))  { lt();  return; }
        if(command.equals("and")) { and(); return; }
        if(command.equals("or"))  { or();  return; }
        if(command.equals("not")) { not(); }

    }

    public void writePushPop(CommandType commandType, String segment, int index) throws IOException {

        write("// " + commandType + " " + segment + " " + index); // Kommentarbeschreibung
        newLine();

        if(segment.equals("argument")) { writeArgument(commandType,index); return; }
        if(segment.equals("local"))    { writeLocal(commandType,index);    return; }
        if(segment.equals("static"))   { writeStatic(commandType,index);   return; }
        if(segment.equals("constant")) { writeConstant(commandType,index); return; }
        if(segment.equals("this"))     { writeThis(commandType,index);     return; }
        if(segment.equals("that"))     { writeThat(commandType,index);     return; }
        if(segment.equals("pointer"))  { writePointer(commandType,index);  return; }
        if(segment.equals("temp"))     { writeTemp(commandType,index); }

    }

    // Project 8 methods

    public void writeLabel(String label) throws IOException {

        write("(" + label + ")");

    }

    public void writeGoto(String label) throws IOException {

        write("@" + label);
        newLine();
        write("0;JMP");

    }

    public void writeIf(String label) throws IOException {

        write("@SP");
        newLine();
        write("M=M-1"); // erniedrige den Stackpointer
        newLine();
        write("A=M+1"); // gehe zum zuvor obersten Stack-Register
        newLine();
        write("D=M"); // speichere den Wert aus diesem Register in D
        newLine();
        write("@" + label);
        newLine();
        write("D;JNE"); // springe zum Label, falls D != 0

    }

    public void writeCall(String functionName, int numArgs) throws IOException {

        write("@" + "return_label_" + functionCounter); // save return-address in TMP
        newLine();
        write("D=A");
        newLine();
        write("@TMP");
        newLine();
        write("M=D");
        newLine();
        writePushPop(C_PUSH,"temp",0); // push TMP on stack

        newLine();
        writePushPop(C_PUSH,"local",0); // push LCL on stack

        newLine();
        writePushPop(C_PUSH,"argument",0); // push ARG on stack

        newLine();
        writePushPop(C_PUSH,"this",0); // push THIS on stack

        newLine();
        writePushPop(C_PUSH,"that",0); // push THAT on stack

        // ARG = SP - numArgs - 5

        int repositionArg = numArgs + 5;

        newLine();
        write("@SP");
        newLine();
        write("D=M");
        newLine();
        write("@" + repositionArg);
        newLine();
        write("D=D-A");
        newLine();
        write("@ARG");
        newLine();
        write("M=D");

        // LCL = SP

        newLine();
        write("@SP");
        newLine();
        write("D=M");
        newLine();
        write("@LCL");
        newLine();
        write("M=D");
        newLine();


        writeGoto(functionName); // goto f (fileName.functionName)


        newLine();
        write("(return_label_" + functionCounter++ + ")");

    }

    public void writeReturn() throws IOException {

        write("@LCL"); // store LCL in R13
        newLine();
        write("D=M");
        newLine();
        write("@R13");
        newLine();
        write("M=D");
        newLine();

        write("@5"); // store return address in R14
        newLine();
        write("D=D-A");
        newLine();
        write("@R14");
        newLine();
        write("M=D");
        newLine();

        writePushPop(C_POP,"argument",0);
        newLine();

        write("@ARG"); // SP = ARG + 1
        newLine();
        write("D=M");
        newLine();
        write("@SP");
        newLine();
        write("M=D+1");
        newLine();

        write("@R13"); // restore THAT
        newLine();
        write("D=M-1");
        newLine();
        write("@THAT");
        newLine();
        write("M=D");
        newLine();

        write("D=D-1"); // restore THIS
        newLine();
        write("@THIS");
        newLine();
        write("M=D");
        newLine();

        write("D=D-1"); // restore ARG
        newLine();
        write("@ARG");
        newLine();
        write("M=D");
        newLine();

        write("D=D-1"); // restore LCL
        newLine();
        write("@LCL");
        newLine();
        write("M=D");
        newLine();

        write("@R14"); // goto R14
        newLine();
        write("A=M");

    }

    public void writeFunction(String functionName, int numLocals) throws IOException {

        writeLabel(functionName);

        for (int i = 0; i < numLocals; i++) {
            newLine();
            writePushPop(C_PUSH,"constant",0);
        }

    }

    // private methods

    private void add() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("M=M-1"); // verringere den Stackpointer um 1
        newLine();
        write("A=M");   // gehe zur neuen Stackpointer-Adresse
        newLine();
        write("D=M");   // speichere den darin liegenden Wert in D
        newLine();
        write("A=A-1"); // gehe zur nächsten Stackpointer-Adresse
        newLine();
        write("M=D+M"); // schreibe das Produkt von D und M in M

    }

    private void sub() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("M=M-1"); // verringere den Stackpointer um 1
        newLine();
        write("A=M");   // gehe zur neuen Stackpointer-Adresse
        newLine();
        write("D=M");   // speichere den darin liegenden Wert in D
        newLine();
        write("A=A-1"); // gehe zur nächsten Stackpointer-Adresse
        newLine();
        write("M=D-M"); // schreibe die Differenz von D und M in M

    }

    private void neg() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("A=M-1"); // gehe zur Adresse direkt unter dem Stackpointer
        newLine();
        write("M=-M");  // kehre das Vorzeichen um

    }

    private void eq() throws IOException {

        sub();
        newLine();

        write("D=M"); // speichere Ergebnis der Subtraktion in D
        newLine();
        write("@TRUE" + jumpCounter);
        newLine();
        write("D;JEQ"); // falls das Ergebnis 0 ist, springe zu TRUE_index
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=0");  // setze oberes Stack-Element auf false
        newLine();
        write("@END" + jumpCounter);
        newLine();
        write("0;JMP"); // springe zum Ende
        newLine();
        write("(TRUE" + jumpCounter + ")");
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=-1"); // setze oberes Stack-Element auf true
        newLine();
        write("(END" + jumpCounter + ")");

        jumpCounter++;

    }

    private void gt() throws IOException{

        sub();
        newLine();

        write("D=M"); // speichere Ergebnis der Subtraktion in D
        newLine();
        write("@TRUE" + jumpCounter);
        newLine();
        write("D;JGT"); // falls das Ergebnis positiv ist, springe zu TRUE_index
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=0");  // setze oberes Stack-Element auf false
        newLine();
        write("@END" + jumpCounter);
        newLine();
        write("0;JMP"); // springe zum Ende
        newLine();
        write("(TRUE" + jumpCounter + ")");
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=-1"); // setze oberes Stack-Element auf true
        newLine();
        write("(END" + jumpCounter + ")");

        jumpCounter++;

    }

    private void lt() throws IOException {

        sub();
        newLine();

        write("D=M"); // speichere Ergebnis der Subtraktion in D
        newLine();
        write("@TRUE" + jumpCounter);
        newLine();
        write("D;JLT"); // falls das Ergebnis negativ ist, springe zu TRUE_index
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=0");  // setze oberes Stack-Element auf false
        newLine();
        write("@END" + jumpCounter);
        newLine();
        write("0;JMP"); // springe zum Ende
        newLine();
        write("(TRUE" + jumpCounter + ")");
        newLine();
        write("@SP");
        newLine();
        write("A=M-1");
        newLine();
        write("M=-1"); // setze oberes Stack-Element auf true
        newLine();
        write("(END" + jumpCounter + ")");

        jumpCounter++;

    }

    private void and() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("M=M-1"); // verringere den Stackpointer um 1
        newLine();
        write("A=M");   // gehe zur neuen Stackpointer-Adresse
        newLine();
        write("D=M");   // speichere den darin liegenden Wert in D
        newLine();
        write("A=A-1"); // gehe zur nächsten Stackpointer-Adresse
        newLine();
        write("M=D&M"); // schreibe die Konjunktion von D und M in M

    }

    private void or() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("M=M-1"); // verringere den Stackpointer um 1
        newLine();
        write("A=M");   // gehe zur neuen Stackpointer-Adresse
        newLine();
        write("D=M");   // speichere den darin liegenden Wert in D
        newLine();
        write("A=A-1"); // gehe zur nächsten Stackpointer-Adresse
        newLine();
        write("M=D|M"); // schreibe die Disjunktion von D und M in M

    }

    private void not() throws IOException {

        write("@SP");   // @RAM[0]
        newLine();
        write("A=M-1"); // gehe zur Adresse direkt unter dem Stackpointer
        newLine();
        write("M=!M");  // kehre negiere M

    }


    private void writeArgument(CommandType commandType, int index) throws IOException {

        if(commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@ARG");
            newLine();
            write("A=D+M"); // gehe zum Register ARG+index
            newLine();
            write("D=M"); // sichere den Wert aus ARG+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if(commandType == C_POP) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@ARG");
            newLine();
            write("D=D+M"); // speichere die Adresse ARG+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse ARG+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse ARG+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writeLocal(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@LCL");
            newLine();
            write("A=D+M"); // gehe zum Register LCL+index
            newLine();
            write("D=M"); // sichere den Wert aus LCL+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@LCL");
            newLine();
            write("D=D+M"); // speichere die Adresse LCL+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse LCL+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse LCL+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writeStatic(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + fileName + "." + index);
            newLine();
            write("D=M"); // sichere den Wert aus Static+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + fileName + "." + index);
            newLine();
            write("D=A"); // speichere den Wert von Static+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse Static+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse Static+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writeConstant(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        }
    }

    private void writeThis(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@THIS");
            newLine();
            write("A=D+M"); // gehe zum Register THIS+index
            newLine();
            write("D=M"); // sichere den Wert aus THIS+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@THIS");
            newLine();
            write("D=D+M"); // speichere die Adresse THIS+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse THIS+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse THIS+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writeThat(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@THAT");
            newLine();
            write("A=D+M"); // gehe zum Register THAT+index
            newLine();
            write("D=M"); // sichere den Wert aus THAT+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@THAT");
            newLine();
            write("D=D+M"); // speichere die Adresse THAT+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse THAT+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse THAT+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writePointer(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + (3 + index));
            newLine();
            write("D=M"); // speichere den Inhalt von THIS bzw. von THAT in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + (3 + index));
            newLine();
            write("D=A"); // speichere die Adresse von THIS bzw. von THAT in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse von THIS bzw. von THAT in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse von THIS bzw. von THAT
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

    private void writeTemp(CommandType commandType, int index) throws IOException {

        if (commandType == C_PUSH) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@R5");
            newLine();
            write("A=D+M"); // gehe zum Register R5+index
            newLine();
            write("D=M"); // sichere den Wert aus R5+index in D
            newLine();
            write("@SP");
            newLine();
            write("M=M+1"); // erhöhe den Stackpointer
            newLine();
            write("A=M-1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("M=D"); // schreibe D dort hinein

        } else if (commandType == C_POP) {

            write("@" + index);
            newLine();
            write("D=A"); // speichere den Wert von index in D
            newLine();
            write("@R5");
            newLine();
            write("D=D+M"); // speichere die Adresse R5+index in D
            newLine();
            write("@R13");
            newLine();
            write("M=D"); // speichere die Adresse R5+index in R13 zwischen
            newLine();
            write("@SP");
            newLine();
            write("M=M-1"); // erniedrige den Stackpointer
            newLine();
            write("A=M+1"); // gehe zum zuvor obersten Stack-Register
            newLine();
            write("D=M"); // speichere den Wert aus diesem Register in D
            newLine();
            write("@R13");
            newLine();
            write("A=M"); // gehe zurück zur Adresse R5+index
            newLine();
            write("M=D"); // schreibe den Wert aus dem Stack dort hinein

        }
    }

}