package Scanner;

/**
 * Created by Ethan on 2017-04-05.
 */
public class SemanticRecord {

    private String varName;
    private String varType;
    private int varValue;

    public SemanticRecord(String name, String type, int value) {
        varName = name;
        varType = type;
        varValue = value;
    }

    public String getVarName(){
        return varName;
    }

    public String getVarType(){
        return varType;
    }

    public int getVarValue(){
        return varValue;
    }
}