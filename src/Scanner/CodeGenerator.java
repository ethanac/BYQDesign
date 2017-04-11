package Scanner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hao on 2017-04-09.
 */
public class CodeGenerator {
    private int counter = 4;
    public boolean toFile = true;
    public PrintWriter out;
    private HashMap<String, ArrayList<String>> offsetTable;

    public CodeGenerator() {
        try {
            File f = new File("Code.txt");
            f.delete();
            out = new PrintWriter(new BufferedWriter(new FileWriter("Code.txt", true)));
        }
        catch(Exception e){e.printStackTrace();}
        offsetTable = new HashMap<>();
    }
    public void generate(SymbolTableGenerator stg, SemanticRecords sr){
        HashMap<Integer, ArrayList<String>> records = sr.recordMap;
        generateDecl(stg);
        generateProg(records);
        writer("\t\thlt");
    }

    private void writer(String s) {
        if(toFile) {
            out.println(s);
        }
        else {
            // System.out.println(s);
        }
    }

    private void generateDecl(SymbolTableGenerator stg){
        for(String s : stg.tables.keySet()) {
            HashMap<String, ArrayList<String>> table = stg.tables.get(s);
            String tableName = s;
            int tableSize = 0;
            boolean first = true;
            for(String k : table.keySet()) {
                String recName = k;
                ArrayList<String> list = table.get(k);
                int reserve = 0;
                if(tableName.equals("program")) {
                    if(first)
                        writer("\t\t\tentry");
                    first = false;
                    if (list.get(0).equals("variable")) {
                        if (list.get(1).equals("int")) {
                            writer(recName + "\t\tdw\t" + 0);
                        }
                        else if(list.get(1).equals("float")) {
                            writer(recName + "\t\tres\t" + 2);
                        }
                    }
                }
                else{
                    String type = list.get(1).split(":")[0];
                    String kind = list.get(0);
                    String[] dimension = null;
                    int dim = 0;
                    if(type.equals("int")) {
                        reserve += 1;
                    }
                    else if(type.equals("float")) {
                        reserve += 2;
                    }
                    else if(type.contains("[")){
                        // writer("type: " + type);
                        dimension = type.split("\\[");
                        dim = 1;
                        for(String index : dimension) {
                            if(index.equals("int"))
                                reserve += 0;
                            else if(index.equals("float"))
                                reserve += 2;
                            else
                                dim *= Integer.parseInt(index.replace("]", "").replace(",", ""));
                        }
                        reserve += dim;
                    }
                    tableSize += reserve;
                }
            }
            if(!tableName.equals("program") && !tableName.equals("Global"))
                writer(tableName.replace(".", "2")  + "\t\tres\t" + tableSize);
        }
    }

    private void generateProg(HashMap<Integer, ArrayList<String>> records) {
        process();
        for(int k : records.keySet()){
            ArrayList<String> list = records.get(k);
            String name = "";
            String head = list.get(0);
            if(head.contains("class begin")){
                name = "class2" + list.get(0).split(",")[1].split(":")[1];
                writer(name);
            }
            else if(head.contains("function begin")){
                name = "function2" + list.get(0).split(":")[1].replace(".", "2");
                writer(name);
            }
            else if(head.equals(",=,")) {
                generateAssign(list);
            }
            else if(head.contains("if begin")) {
                generateCond(list);
            }
            else if(head.contains("if end")){
                writer("endif");
            }
            else if(head.contains("11,")){
                generateReturn(list);
            }
            else if(head.contains("class end")) {
                name = "class2" + list.get(0).split(",")[1].split(":")[1];
                writer(name);
            }
            else if(head.contains("function end")){
                name = "function2" + list.get(0).split(":")[1].replace(".", "2");
                writer(name);
            }
            else if(head.contains("get begin")){
                name = "get2" + list.get(0);
                writer("getbegin");
            }
            else if(head.contains("get end")) {
                name = "get2" + list.get(0).split(":")[1].replace(".", "2");
                writer("getend");
            }
            else if(head.contains("put begin")) {
                name = "put2" + list.get(0).replace(".", "2");
                writer("putbegin");
            }
            else if(head.contains("put end")) {
                name = "put2" + list.get(0).split(":")[1].replace(".", "2");
                writer("putend");
            }
            else if(head.contains("for begin")) {
                name = "get2" + list.get(0).replace(".", "2");
                writer("forbegin");
                generateFor(list);
            }
            else if(head.contains("for end")) {
                name = "for2" + list.get(0).replace(".", "2");
                writer("forend");
            }
            else if(head.contains(".function")) {
                name = "for2" + list.get(0).replace(".", "2");
                generateFunCall(list);
                writer("forend");
            }
        }
    }

    private void generateAssign(ArrayList<String> list) {
        String sw = list.get(1).split(",")[1].replace(".", "2");
        String e;
        ArrayList<String> expr = new ArrayList<>();
        for(int i = 2; i < list.size(); i++) {
            expr.add(list.get(i));
        }
        e = generateExpr(expr);
        writer("\t\tlw r1," + e);
        writer("\t\tsw " + sw + "[r0],r1");
    }

    private String generateExpr(ArrayList<String> list) {
        ArrayList<String> l = list;
        ArrayList<String> parList = new ArrayList<>();
        String mulOperand = "";
        String addOperand = "";
        String parRegName = "";
        String mulRegName = "";
        int k;
        boolean sub = false;
        int parEnd = 0;
        for(int i = 0; i < l.size(); i++) {
            if("(".equals(l.get(i).split(",")[1])){
                sub = true;
            }
            else if(")".equals(l.get(i).split(",")[1])) {
                sub = false;
                parEnd = i;
            }
            else if(sub){
                parList.add(l.get(i));
                l.remove(i);
            }
        }
        if(parList.size() > 0) {
            parRegName = generateExpr(parList);
            l.add(parEnd, parRegName);
        }
        for(int i = 0; i < l.size(); i++) {
            if("*/".equals(l.get(i).split(",")[1])) {
                mulOperand += l.get(i).split(",")[1] + ":" + l.get(i+1).split(",")[1] + ":" + l.get(i+2).split(",")[1];
                l.remove(i);
                l.remove(i+1);
                l.remove(i+2);
                l.add(i, generateOp(mulOperand));
            }
        }
        for(int i = 0; i < l.size(); i++) {
            if("+-".equals(l.get(i).split(",")[1])) {
                addOperand += l.get(i).split(",")[1] + ":" + l.get(i+1).split(",")[1] + ":" + l.get(i+2).split(",")[1];
                l.remove(i);
                l.remove(i+1);
                l.remove(i+2);
                l.add(i, generateOp(addOperand));
            }
        }
        k = counter++;
        return "t"+k;
    }

    private String generateOp(String s){
        String[] e = s.split(":");
        String operator = "";
        int k;
        if("0123456789".contains("" + e[1].charAt(0))){
            int n = counter++;
            writer("\t\tsub r1,r1,r1");
            writer("\t\taddi r1,r1," + e[1]);
            writer("\t\tsw t" + n + ",r1");
            writer("\t\tlw r1,t" + n);
        }
        else {
            writer("\t\tlw r1," + e[1].replace(".", "2") + "[r0]");
        }
        if("0123456789".contains("" + e[2].charAt(0))){
            int n = counter++;
            writer("\t\tsub r1,r1,r1");
            writer("\t\taddi r1,r1," + e[2]);
            writer("\t\tsw t" + n + ",r1");
            writer("\t\tlw r2,t" + n);
        }
        else {
            writer("\t\tlw r1," + e[2].replace(".", "2") + "[r0]");
        }
        if(e[0].equals("*"))
            operator = "mul";
        else if(e[0].equals("*"))
            operator = "div";
        else if(e[0].equals("+"))
            operator = "add";
        else if(e[0].equals("-"))
            operator = "sub";
        writer("\t\t" + operator + " r3,r1,r2");
        k = counter++;
        writer("t" + k + "\tdw 0");
        writer("\t\tsw t" + k + "[r0],r3");
        return "t"+k;
    }

    private void generateCond(ArrayList<String> list) {
        String sw = list.get(1).split(",")[1].replace(".", "2");
        ArrayList<String> l = list;
        String e;
        ArrayList<String> expr = new ArrayList<>();
        for(int i = 2; i < list.size(); i++) {
            expr.add(list.get(i));
        }
        for(int i = 0; i < l.size(); i++) {
            if("+-".equals(l.get(i).split(",")[1])) {
                String k = l.get(i).split(",")[1] + ":" + l.get(i+1).split(",")[1] + ":" + l.get(i+2).split(",")[1];
                l.remove(i);
                l.remove(i+1);
                l.remove(i+2);
                l.add(i, generateOp(k));
            }
        }
        e = generateExpr(l);
        writer("\t\tlw r1," + e);
        writer("\t\tsw " + sw + "[r0],r1");
    }

    private void generateReturn(ArrayList<String> list) {
        String sw = list.get(0).split(",")[1].replace(".", "2");
        ArrayList<String> expr = new ArrayList<>();
        for(int i = 2; i < list.size(); i++) {
            expr.add(list.get(i));
        }
        writer("\t\tlw r1," + "r2");
        writer("\t\tsw " + sw + "[r0],r1");
    }

    private void generateFor(ArrayList<String> list) {
        ArrayList<String> l = list;
        ArrayList<String> parList = new ArrayList<>();
        String regName = "";
        String s = "";
        String result = "";
        String[] e = list.get(0).split(",");
        int k;
        boolean sub = false;
        int parEnd = 0;
        for(int i = 0; i < l.size(); i++) {
            generateCond(l);
            writer("\t\t");
        }

        if(parList.size() > 0) {
            regName = generateExpr(parList);
            l.add(parEnd, regName);
        }
        result = generateExpr(l);
        for(int i = 0; i < l.size(); i++) {
            if(e[0].equals("*"))
                s += "lw r1," + list.get(i) + "(r0)";
            else if(e[0].equals("*"))
                s += "sw " + list.get(i) + "(r0),r1";
            else if(e[0].equals("+"))
                s += "lw r1," + list.get(i+1) + "(r0)";
            else if(e[0].equals("-"))
                s += "jl " + list.get(i-1) + "," + regName;
        }
        writer(s);
    }

    private void generateFunCall(ArrayList<String> list){
        ArrayList<String> l = list;
        ArrayList<String> parList = new ArrayList<>();
        String regName = "";
        String name = "function2";
        String s = "jl r15," + name;
        String e = generateExpr(list);
        s += "        jr r15\n" +
                "        jl r15," + name +
                "        lw r1,t5(r0)\n" +
                "        jr r15";

        String[] names = list.get(0).split(",");
        writer(s);
    }

    private void process() {
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader("job.txt"));
            String line = br.readLine();
            while(line != null) {
                writer(line);
                line = br.readLine();
            }
            br.close();
            out.close();
            toFile = false;
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
