package Scanner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hao on 2017-04-05.
 */
public class SemanticRecords {

    public HashMap<Integer, ArrayList<String>> recordMap;

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

                if(sp.equals("(") || sp.equals(")")) {
                    continue;
                }
                if(sp.contains("class ") || sp.contains("program ")){
                    String[] arr = sp.split("\\.");
                    sp = arr[arr.length-1];
                    System.out.println("A class: " + sp + ", at " + fullName.split(",")[2]);
                    continue;
                }
                if(sp.contains("for") || sp.contains("if") || sp.contains("get") || sp.contains("put")){
                    System.out.println("A statement: " + sp + ", at " + fullName.split(",")[2]);
                    continue;
                }
                if(sp.contains("function")){
                    System.out.println("A function: " + sp + ", at " + fullName.split(",")[2]);
                    continue;
                }
                if("=+-*/]<>".contains("" + sp.charAt(sp.length()-1))){
                    continue;
                }
                if("0123456789".contains(sp.charAt(0)+"")) {
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
                // System.out.println("full name: " + fullName);
                if(fullName.split(",")[0].equals("11")){
                    String name = fullName.split(",")[1];
                    name = name.replace("Global.", "");
                    String[] names = name.split("\\.");
                    String pName = names[names.length-2];
                    String vName = names[names.length-1];
                    String ptName = "";
                    String vtName = "";
                    if(names.length == 2) {
                        ptName = "Global";
                        vtName = "Global." + names[0];
                    }
                    else if(names.length == 3) {
                        ptName = names[0];
                        vtName = names[0] + "." + names[1];
                    }
                    String tableType = stg.getRecord(ptName, pName).split(",")[1].split(":")[0];
                    String vType = stg.getRecord(vtName, vName).split(",")[1];
                    // System.out.println("t vs v: " + tableType + ", " + vType);
                    if(tableType.equals(vType))
                        System.out.println("OK. at " + fullName.split(",")[2]);
                    else
                        System.out.println("Semantic Error: incorrect type, at " + fullName.split(",")[2]);
                    continue;
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
                    // System.out.print(tableName + ": " + varName);
                    if (stg.tables.get("Global." + tableName) != null)
                        tableName = "Global." + tableName;
                }
                else {
                    tableName = "Global";
                    varName = fullName;
                }
                // System.out.println("Table: " + tableName + " || var: " + varName);

                // System.out.println("Record is: " + stg.getRecord(tableName, varName));
                String result = stg.getRecord(tableName, varName);
                if(result.equals("")){
                    result = stg.getRecord("Global", varName);
                    if(result.equals("")) {
                        System.out.println("Semantic Error: variable or function is undefined, at " + lineNum);
                        typeOK = false;
                        continue;
                    }
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
            System.out.println("Semantic checking is finished.");
        }
        System.out.println("------------------------------------");
        for(int k : recordMap.keySet()) {
            System.out.println(k + ":" + recordMap.get(k));
        }
        return typeOK;
    }

}
