package de.nand2tetris;

import java.io.File;
import java.io.IOException;

import static de.nand2tetris.CommandType.*;

public class VMEmulator {


    public static void main(String[] args) throws IOException {

        if(args.length == 0){
            System.out.println("Es wurde keine Datei bzw. kein Dateipfad übergeben.");
            System.exit(0);
        }

        File file = new File(args[0]);
        File[] files;

        if(file.isDirectory()) {
            files = file.listFiles((dir, name) -> name.endsWith(".vm"));
            if(files == null) {
                System.out.println("Der übergebene Ordner enthält keine vm-Dateien.");
                System.exit(0);
            }
        } else if(!args[0].endsWith(".vm")) {
            files = new File[1];
            System.out.println("Die übergebene Datei ist kein Ordner bzw. keine vm-Datei.");
            System.exit(0);
        } else {
            files = new File[1];
            files[0] = file;
        }

        CodeWriter codeWriter = new CodeWriter(new File(file.getPath() + "/" + file.getName() + ".asm"));

        for (int i = 0; i < files.length; i++) {
            File vmfile = files[i];
            Parser parser = new Parser(vmfile);

            codeWriter.newLine();
            codeWriter.write("// from " + vmfile.getName());
            codeWriter.newLine();
            codeWriter.newLine();

            while(parser.hasMoreCommands()){

                codeWriter.setFileName(vmfile.getName());

                if((parser.commandType() == C_POP) || (parser.commandType() == C_PUSH)) {
                    codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                }
                else if(parser.commandType() == C_ARITHMETIC) {
                    codeWriter.writeArithmetic(parser.arg1());
                }
                else if(parser.commandType() == C_CALL) {
                    codeWriter.writeCall(parser.arg1(),parser.arg2());
                }
                else if(parser.commandType() == C_FUNCTION) {
                    codeWriter.writeFunction(parser.arg1(),parser.arg2());
                }
                else if(parser.commandType() == C_RETURN) {
                    codeWriter.writeReturn();
                }
                else if(parser.commandType() == C_GOTO) {
                    codeWriter.writeGoto(parser.arg1());
                }
                else if(parser.commandType() == C_IF) {
                    codeWriter.writeIf(parser.arg1());
                }
                else if(parser.commandType() == C_LABEL) {
                    codeWriter.writeLabel(parser.arg1());
                }

                parser.advance();

                if(parser.hasMoreCommands()){
                    codeWriter.newLine();
                    codeWriter.newLine();
                }

            }

            if(i < (files.length - 1)){
                codeWriter.newLine();
                codeWriter.newLine();
            }
        }
        codeWriter.close();
    }
}
