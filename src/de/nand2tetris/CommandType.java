package de.nand2tetris;

public enum CommandType {

    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_FUNCTION,
    C_RETURN,
    C_CALL;

    @Override
    public String toString() {

        if(this == C_ARITHMETIC) return "arithmetic";
        if(this == C_PUSH)       return "push";
        if(this == C_POP)        return "pop";
        if(this == C_LABEL)      return "label";
        if(this == C_GOTO)       return "goto";
        if(this == C_IF)         return "if-goto";
        if(this == C_FUNCTION)   return "function";
        if(this == C_RETURN)     return "return";
        if(this == C_CALL)       return "call";

        return super.toString();
    }
}
