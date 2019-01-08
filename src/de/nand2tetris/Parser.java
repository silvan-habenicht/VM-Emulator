package de.nand2tetris;

import java.io.*;
import java.util.ArrayList;

import static de.nand2tetris.CommandType.*;

public class Parser {

    private ArrayList<String> list = new ArrayList<>();  // Liste mit den Programmcode-Zeilen
    private int currentPosition = 0;                     // Aktuelle Programmcode-Zeile
    private String currentCommand;                       // Aktuelle Programmcode-Anweisung

    public Parser(File file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file)
                )
        );

        String line;
        while((line = bufferedReader.readLine()) != null) {
            if(!line.isEmpty() && !line.startsWith("//")) {
                if (line.contains("//")) {
                    list.add(line.substring(0, line.indexOf("//")));
                }
                else {
                    list.add(line);
                }
            }
        }
        bufferedReader.close();

        currentCommand = list.get(currentPosition);
    }

    // Liefert false genau dann,  wenn der Parser in der letzten Programmzeile angelangt ist.
    public boolean hasMoreCommands() {
        return list.size() > currentPosition;
    }

    // Lässt den Parser zur nächsten Programmzeile übergehen.
    public void advance(){
        assert hasMoreCommands();
        currentPosition += 1;
        if(hasMoreCommands())
            currentCommand = list.get(currentPosition);
    }

    // Gibt Auskunft über den Befehlstyp der aktuellen Anweisung.
    public CommandType commandType(){

        if(currentCommand.contains("if"))        return C_IF;
        if(currentCommand.contains("goto"))      return C_GOTO;
        if(currentCommand.contains("push"))      return C_PUSH;
        if(currentCommand.contains("pop"))       return C_POP;
        if(currentCommand.contains("label"))     return C_LABEL;
        if(currentCommand.contains("function"))  return C_FUNCTION;
        if(currentCommand.contains("call"))      return C_CALL;
        if(currentCommand.contains("return"))    return C_RETURN;

        return C_ARITHMETIC;
    }

    public String arg1() {

        assert commandType() != C_RETURN;

        String[] args = currentCommand.split("\\s");

        if(commandType() == C_ARITHMETIC) {
            return args[0];
        }
        else {
            return args[1];
        }
    }

    public int arg2() {

        assert (commandType() == C_PUSH) || (commandType() == C_POP) ||
                (commandType() == C_FUNCTION) || (commandType() == C_CALL);

        String[] args = currentCommand.split("\\s");

        return Integer.parseInt(args[2]);
    }
}