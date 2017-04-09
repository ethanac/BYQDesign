package Scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hao on 2017-03-14.
 */
public class SymbolTableGenerator {
    HashMap<String, HashMap<String, ArrayList<String>>> tables = new HashMap<String, HashMap<String, ArrayList<String>>>();
    public String searchResult = "";
    public boolean toFile = false;
    public PrintWriter out;

    public SymbolTableGenerator() {
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter("SymbolTable.txt", true)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writer(String s) {
        if(toFile) {
            out.print(s);
        }
        else {
            System.out.print(s);
        }
    }

    public boolean create(String name) {
        if(!tables.containsKey(name)) {
            tables.put(name, new HashMap<String, ArrayList<String>>());
            return true;
        }
        else {
            System.out.print("Semantic Error: multiply declared table: table: " + name);
            return false;
        }
    }

    public boolean insert(String tableName, String id, ArrayList<String> record) {
        if(tables.containsKey(tableName)) {
            if (tables.get(tableName).containsKey(id)) {
                System.out.print("Semantic Error: multiply declared identifier: table: " + tableName + " id: " + id);
                return false;
            } else {
                tables.get(tableName).put(id, record);
                return true;
            }
        }
        else {
            System.out.print("Semantic Error: table not found: table: " + tableName);
            return false;
        }
    }

    public boolean search(String tableName, String id) {
        boolean isFound = false;
        if(tables.get(tableName).containsKey(id)) {
            searchResult = id;
            isFound = true;
        }
        else {
            for(String s : tables.get(tableName).keySet()) {
                ArrayList<String> list = tables.get(tableName).get(s);
                if(list.size() == 3 && !list.get(2).equals("NA")) {
                    if(search(list.get(2), id)) {
                        isFound = true;
                        break;
                    }
                }
            }
        }
        return isFound;
    }

    public String getRecord(String tableName, String id) {
        String result = "";
        if(tables.get(tableName).containsKey(id)) {
            ArrayList<String> list = tables.get(tableName).get(id);
            for(int i = 0; i < list.size(); i++) {
                result += list.get(i) + ",";
            }
        }
        return result;
    }

    public boolean delete(String tableName) {
        if(tables.containsKey(tableName)) {
            tables.remove(tableName);
            System.out.println("Table " + tableName + " is deleted.");
            return true;
        }
        else {
            System.out.println("Semantic Error: table to be deleted not found.");
            return false;
        }
    }

    public void printTable(String tableName) {
        HashMap<String, ArrayList<String>> table = tables.get(tableName);
        writer("-------------------\n");
        writer("The table " + tableName + " and its properties are showed below:\n");
        if(tables.containsKey(tableName)) {
            //System.out.println("Identifiers in table " + tableName);
            for(String s : table.keySet()) {
                writer(s + ": ");
                for(String t : table.get(s)) {
                    writer(t + " ");
                }
                writer("\n");
            }
        }
        else {
            writer("Semantic Error: table to be printed not found: " + tableName + "\n");
        }
    }
}
