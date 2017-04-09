package Scanner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hao on 2017-04-05.
 */
public class SemanticRecords {

    private HashMap<Integer, ArrayList<String>> recordMap;

    public SemanticRecords() {
        recordMap = new HashMap<>();
    }

    public void addRecord(int num, String record){
        ArrayList<String> l;
        if(recordMap.containsKey(num)) {
            l = recordMap.get(num);
        }
        else {
            l = new ArrayList<>();
        }
        l.add(record);
        recordMap.put(num, l);
    }

    public boolean checkType(SymbolTableGenerator stg){
        System.out.println("------------Semantic Checking-------------");
        boolean typeOK = true;
        for(int k : recordMap.keySet()) {
            ArrayList<String> rec = recordMap.get(k);
            String varType = "";
            String lineNum = rec.get(0).split(",")[2];
            for(String s : rec) {
                String tableName = "";
                String varName;
                String fullName = s;
                String sp = fullName.split(",")[1];
                System.out.println("Rec: " + sp);
                if("=+-*/]".contains("" + sp.charAt(sp.length()-1))){
                    continue;
                }
                if("0123456789".contains(sp.charAt(sp.length()-1)+"")) {
                    if (!varType.equals("")) {
                        if (sp.contains(".")) {
                            if (varType.equals("float")) {
                                System.out.println("OK. " + lineNum);
                                continue;
                            }
                        } else if (varType.equals("int")) {
                            System.out.println("OK. " + lineNum);
                            continue;
                        }
                        System.out.println("Semantic Error: incorrect type, at " + lineNum);
                        continue;
                    }
                    else {
                        if (sp.contains(".")) {
                            varType = "float";
                            System.out.println("OK. " + lineNum);
                            continue;

                        }
                        else {
                            varType = "int";
                            System.out.println("OK. " + lineNum);
                            continue;
                        }
                    }
                }
                fullName = fullName.replace("Global.", "").split(",")[1];
                if(fullName.contains(":")){
                    tableName = stg.getRecord("program", fullName.split(":")[0]).split(",")[1];
                    varName = fullName.split(":")[1];
                }
                else if(fullName.contains(".")){
                    String[] name = fullName.split("\\.");
                    for(int i = 0; i < name.length-1; i++) {
                        if(i == name.length-2)
                            tableName += name[i];
                        else
                            tableName += name[i] + ".";
                    }
                    varName = name[name.length-1];
                    if (stg.tables.get("Global." + tableName) != null)
                        tableName = "Global." + tableName;
                }
                else {
                    tableName = "Global";
                    varName = fullName;
                }
                System.out.println("Table: " + tableName + " || var: " + varName);

                System.out.println("Record is: " + stg.getRecord(tableName, varName));
                String result = stg.getRecord(tableName, varName);
                if(result.equals("")){
                    result = stg.getRecord("Global", varName);
                    if(result.equals(""))
                        System.out.println("Semantic Error: variable or function is not defined, at " + lineNum);
                    else {
                        if(varType.equals("")) {
                            varType = result.split(",")[1].replace(":", "");
                            continue;
                        }
                    }
                }
                // System.out.println("Type is: " + varType + " || result is: " + result);
                result = result.split(",")[1];
                if(!varType.equals("")) {
                    if(result.contains(":"))
                        result = result.split(":")[0];
                    // System.out.println("Type: " + varType + " || res: " + result);
                    if(!varType.equals(result)) {
                        System.out.println("Semantic Error: incorrect type, at " + lineNum);
                        typeOK = false;
                    }
                    else{
                        System.out.println("OK. " + lineNum);
                    }
                }
                varType = result;
            }
        }
        if(typeOK) {
            System.out.println("Semantic checking is finished. Types are fine.");
        }
        System.out.println("------------------------------------");
        for(int k : recordMap.keySet()) {
            System.out.println(k + ":" + recordMap.get(k));
        }
        return typeOK;
    }

}
