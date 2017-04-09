package Scanner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Hao on 2017-02-21.
 */
public class Parser {
    String lookAhead = "";
    LexicalAnalyzer la;
    public SymbolTableGenerator stg;
    String fileName = "Output.txt";
    BufferedReader br = null;
    HashMap<String, ArrayList<String>> firstSets = null;
    HashMap<String, ArrayList<String>> followSets = null;
    boolean isClass = false;
    boolean isFuncBody = false;
    boolean T17to19 = false;
    boolean isFactor = false;
    boolean isIdnest = false;
    boolean var2factor = false;
    int lineNum = 0;
    public boolean toFile = true;
    boolean ifRecord = false;
    boolean isIndice = false;
    public String record = "";
    String tokenString = "";
    PrintWriter out = null;
    boolean funcParams = false;
    boolean isArraySize = false;
    boolean isFuncStart = false;
    String tmpFuncHead = "";
    String typeDim = "";
    String arraySize = "";
    String vType = "";
    private String vName = "";
    private Stack<String> scope;
    private String semRecord = "";
    private boolean startRec = false;
    private Stack<String> semStack = new Stack<>();
    private SemanticRecords sr = new SemanticRecords();
    private String paraName = "";
    private ArrayList<String> parentName = new ArrayList<>();

    public Parser(){
        la = new LexicalAnalyzer();
        stg = new SymbolTableGenerator();
        try{
            br = new BufferedReader(new FileReader(fileName));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter("ParserOutput.txt", true)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new FirstNFollow();
        scope = new Stack<String>();
    }

    public boolean parse(){
        lookAhead = nextToken();
        //System.out.println(lookAhead);
        if(startSymbol() && match("$")) {
            System.out.println("Parsing completed.");
            return true;
        }
        else {
            System.out.println("Parsing failed.");
            return false;
        }
    }

    public boolean match(String token){
        if(lookAhead.equals(token)){
            lookAhead = nextToken();
            return true;
        }
        else{
            //lookAhead = nextToken();
            return false;
        }
    }

    public String nextToken(){
        String line = "$";
        String token = null;
        try {
            line = la.getToken();
            while(line != null && (line.contains("comment:") || line.contains("::"))) {
                line = la.getToken();
            }
            if(line != null) {
                writer(line);
                token = line.split(":")[0];
                if(!line.equals("$")) {
                    tokenString = line.split(":")[1];
                    lineNum = Integer.parseInt(line.split(":")[2]);
                }
                if(funcParams) {
                    typeDim += tokenString + "!";
                }
                if(isArraySize) {
                    if(token.equals("int") || token.equals("float") || token.equals("id"));
                    else
                        arraySize += tokenString;
                }
                if(startRec) {
                    semRecord += tokenString;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return token;
    }

    // Method for Output
    private void writer(String s){
        if(toFile) {
            out.println(s);
        }
        else{
            System.out.println(s);
        }
    }

    private ArrayList<String> createRecord(String type, String kind, String link){
        ArrayList<String> rec = new ArrayList<>(3);
        rec.add(type);
        rec.add(kind);
        rec.add(link);
        return rec;
    }

    private String createSemanticRec(String rec) {
        String[] foo;
        String semRec;
        String[] previousRec;
        boolean finished = false;
        int flag = 0;
        if (rec.contains("factor:")) {
            foo = rec.replace("factor:", "").split(",");
            flag = 1;
        }
        else {
            foo = rec.split(",");
        }
        if("();".contains("" + foo[0].charAt(foo[0].length()-1))) {
            foo[0] = foo[0].replace("" + foo[0].charAt(foo[0].length()-1), "");
        }
        // <>=+-*/
        if("<>=+-*/".contains("" + foo[0].charAt(foo[0].length()-1))) {
            semRec = flag + "," + foo[0].charAt(foo[0].length()-1) + "," + foo[foo.length-1];
            System.out.println("Operator semantic record: " + semRec);
            sr.addRecord(lineNum, semRec);
            foo[0] = foo[0].replace("" + foo[0].charAt(foo[0].length()-1), "");
        }

        if (foo[0].equals(""))
            semRec = "";
        else {
            semRec = flag + "," + foo[0] + "," + foo[foo.length - 1];
            if (!semStack.empty()) {
                previousRec = semStack.peek().split(",");
                String[] currentRec = semRec.split(",");
                if (currentRec[2].equals(previousRec[2])) {
                    if (currentRec[1].equals(previousRec[1])) {
                        if (!currentRec[0].equals(previousRec[0])) {
                            finished = true;
                        }
                    }
                }
            }
        }
        if (!semStack.empty()) {
            semStack.pop();
        }
        semStack.push(semRec);

        if (!finished && !semRec.equals("")) {
            if("0123456789".contains(""+foo[0].charAt(0))) {
                semRec = flag + "," + foo[0] + "," + foo[foo.length-1];
                System.out.println("Final semantic record: " + semRec);
                sr.addRecord(lineNum, semRec);
            }
            else {
                if (!foo[0].contains(".")) {
                    String p = "";
                    for (String s : scope) {
                        if (s.contains(".")) {
                            s = s.split("\\.")[1];
                        }
                        p += s + ".";
                    }
                    foo[0] = p + foo[0];
                    // System.out.println("parent name: " + foo[0]);
                }
                else {
                    foo[0] = foo[0].replace(".", ":");
                    // System.out.println("parent name: " + foo[0]);
                }
                semRec = flag + "," + foo[0] + "," + foo[foo.length - 1];
                System.out.println("Final semantic record: " + semRec);
                sr.addRecord(lineNum, semRec);
            }
        }
        return semRec;
    }

    public void semanticCheck() {
        sr.checkType(stg);
    }

    // start symbol : prog
    // E-> T21'T20
    private boolean startSymbol(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("E")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("E")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(!stg.create("Global")) {
                    System.out.println(" at line " + lineNum);
                }
                System.out.println("Table: Global created.");
                System.out.println("class push: " + scope.push("Global"));
                if(T21p() && T20()){
                    String o = "E-> classDecl* progBody";
                    writer(o);
                    System.out.println("global pop: " + scope.pop());
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        skipErrors(first, follow);
        return false;
    }

    // classDecl*
    // T21'-> # | T21T21'
    private boolean T21p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T21p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T21p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T21() && T21p()){
                    writer("classDecl*-> classDecl classDecl*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("classDecl-> #");
                return true;
            }
        }
        writer("Error: incorrect classDecl at line " + lineNum);
        return false;
    }

    // progBody
    // T20-> program{T17'T16'};T19'
    private boolean T20(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T20")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T20")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("program")) {
                    if(!stg.create("program")) {
                        System.out.println(" at line " + lineNum);
                    }
                    else {
                        ArrayList<String> rec = createRecord("function", "NA", "program");
                        System.out.println(scope.peek());
                        if (!stg.insert(scope.peek(), "program", rec)) {
                            System.out.println(" at line " + lineNum);
                        }
                        System.out.println("Table: program function created.");
                        System.out.println("program push: " + scope.push("program"));
                    }
                    if(match("{")) {
                        if(T17p() && T16p()) {
                            if(match("}")) {
                                if(match(";")) {
                                    System.out.println("program pop: " + scope.pop());    // pop program table
                                    if(T19p()) {
                                        writer("progBody-> program{varDecl*statement*};funcDef*");
                                        return true;
                                    }
                                }
                                else {
                                    writer("Error: missing ';' at line " + lineNum);
                                }
                            }
                            else {
                                writer("Error: missing '}' at line " + lineNum);
                            }
                        }
                    }
                    else {
                        writer("Error: missing '{' at line " + lineNum);
                    }
                }
                else {
                    writer("Error: missing 'program' at line " + lineNum);
                }
                return false;
            }
        }
        writer("Error: incorrect progBody at line " + lineNum);
        return false;
    }

    // classDecl
    // T21-> class id {T17'T19'};
    private boolean T21(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T21")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T21")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                isClass = true;
                if(match("class")) {
                    ArrayList<String> rec = createRecord("class", "NA", tokenString);
                    if(!stg.create(tokenString)) {
                        System.out.println(" at line " + lineNum);
                    }
                    else {
                        if (!stg.insert(scope.peek(), tokenString, rec)) {
                            System.out.println(" at line " + lineNum);
                        }
                        else {
                            System.out.println("Table: class " + tokenString + " created.");
                            System.out.println("class push: " + scope.push(tokenString));    // push class table
                        }
                    }
                    if(match("id")) {
                        if(match("{")) {
                            if(T17p() && T19p()) {
                                if(match("}")) {
                                    if(match(";")) {
                                        writer("classDecl-> class id {varDecl*funcDef*};");
                                        isClass = false;
                                        System.out.println("class pop: " + scope.pop());     // pop class table
                                        return true;
                                    }
                                    else {
                                        writer("Error: missing ';' at line " + lineNum);
                                    }
                                }
                                else {
                                    writer("Error: missing '}' at line " + lineNum);
                                }
                            }
                        }
                        else {
                            writer("Error: missing '{' at line " + lineNum);
                        }
                    }
                    else {
                        writer("Error: missing id at line " + lineNum);
                    }
                }
                else {
                    writer("Error: missing 'class' at line " + lineNum);
                }
                isClass = false;
                return false;
            }
        }
        writer("Error: incorrect classDecl at line " + lineNum);
        return false;
    }

    // varDecl*
    // T17'-> # | T17T17'
    private boolean T17p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T17p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T17p")];
        if(T17to19){
            return true;
        }
        if(isFuncBody){
            return true;
        }
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T17() && T17p()){
                    if(!isFuncBody)
                        writer("varDecl*-> varDecl varDecl*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("varDecl*-> #");
                return true;
            }
        }
        writer("Error: incorrect varDecl at line " + lineNum);
        return false;
    }

    // statement*
    // T16'-> # | T16T16'
    private boolean T16p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T16p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T16p")];
        for(String s : first) {
            if (lookAhead.equals(s) || isFuncBody){
                if(T16() && T16p()){
                    writer("statement*-> statement statement*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("statement*-> #");
                return true;
            }
        }
        writer("Error: incorrect statement at line " + lineNum);
        return false;
    }

    // funcDef*
    // T19'-> # | T19T19'
    private boolean T19p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T19p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T19p")];
        for(String s : first) {
            if (lookAhead.equals(s) || T17to19){
                if(T19() && T19p()){
                    writer("funcDef*-> funcDef funcDef*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("funcDef*-> #");
                return true;
            }
        }
        writer("Error: incorrect funcDef at line " + lineNum);
        return false;
    }

    // varDecl
    // T17-> F2idF3';
    private boolean T17(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T17")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T17")];
        for(String s : first) {
            if (lookAhead.equals(s)) {
                if (lookAhead.equals("id")) {
                    startRec = true;
                    semRecord = tokenString;

                    vType = tokenString;
                    tmpFuncHead = tokenString;
                    if(match("id")) {
                        if (lookAhead.equals("=")) {
                            startRec = false;
                            System.out.println(semRecord + ", assign_left id at ," + lineNum);
                            createSemanticRec(semRecord + ", assign_left id at ," + lineNum);
                            match("=");
                            isFuncBody = true;
                            return true;
                        }
                        else if(lookAhead.equals("id")) {
                            vName = tokenString;
                            tmpFuncHead += " " + tokenString;
                            if(match("id")) {
                                if (ifSwitchTo19(vType + " " + vName))
                                    return true;
                            }
                        }
                    }
                }
                else if(lookAhead.equals("int") || lookAhead.equals("float")){
                    tmpFuncHead = tokenString;
                    vType = tokenString;
                    if(F2()){
                        if(lookAhead.equals("id")) {
                            vName = tokenString;
                            tmpFuncHead += " " + tokenString;
                            if (match("id")) {
                                if (ifSwitchTo19(vType + " " + vName))
                                    return true;
                            }
                        }
                    }
                }
                else{
                    writer("Error: unknown type or id at line " + lineNum);
                    return false;
                }
            }
        }
        writer("Error: incorrect varDecl at line " + lineNum);
        return false;
    }

    private boolean ifSwitchTo19(String var) {
        if(isClass && lookAhead.equals("(")){
            // System.out.println(var + ", func id at " + lineNum);
            T17to19 = true;
            return true;
        }
        else {
            isArraySize = true;
            if (F3p() && match(";")) {
                arraySize = arraySize.split(";")[0];
                if(arraySize.contains("]"))
                    arraySize = "[" + arraySize;

                String p = var.split(" ")[0] + arraySize;
                ArrayList<String> rec = createRecord("variable", p, "NA");
                if(!stg.insert(scope.peek(), var.split(" ")[1], rec)) {
                    System.out.println(" at line " + (lineNum-1));
                }
                else {
                    System.out.println("Variable " + var + arraySize + " inserted.");
                }
                isArraySize = false;
                arraySize = "";
                writer("varDecl-> type id arraySize*;");
                return true;
            }
        }
        return false;
    }

    // arraySize*
    // F3'-> # | F3F3';
    private boolean F3p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F3p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F3p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(F3() && F3p()){
                    writer("arraySize*-> arraySize arraySize*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("arraySize*-> #");
                return true;
            }
        }
        writer("Error: incorrect arraySize at line " + lineNum);
        return false;
    }

    // Type
    // F2-> int | float | id
    private boolean F2(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F2")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F2")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                String varType = "";
                if(lookAhead.equals("int")) {
                    varType = "int";
                }
                else if(lookAhead.equals("float")) {
                    varType = "float";
                }
                else if(lookAhead.equals("id")) {
                    varType = tokenString;
                }
                if(match("int") || match("float") || match("id")){
                    writer("type-> " + varType);
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: unknown type at line " + lineNum);
        return false;
    }

    // arraySize
    // F3-> [ integer ]
    private boolean F3(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F3")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F3")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("[") && match("integer") && match("]")){
                    writer("arraySize-> [ integer ]");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: incorrect arraySize at line " + lineNum);
        return false;
    }

    // funcDef
    // T19-> T11T18;
    private boolean T19(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T19")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T19")];
        for(String s : first) {
            if (lookAhead.equals(s) || T17to19){
                if(T11() && T18()) {
                    if(match(";")){
                        writer("funcDef-> funcHead funcBody");
                        return true;
                    }
                    else {
                        writer("Error: missing ';' at line " + lineNum);
                    }
                }
                return false;
            }
        }
        writer("Error: incorrect funcDef at line " + lineNum);
        return false;
    }

    // funcHead
    // T11-> F2id(T12)
    private boolean T11(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T11")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T11")];
        for(String s : first) {
            if (lookAhead.equals(s)) {
                isFuncStart = true;
                String fType = tokenString;
                if (F2()) {
                    String fName = tokenString;
                    if(match("id")) {
                        funcParams = true;
                        if(match("(")) {
                            if (T12()) {
                                funcParams = false;
                                if (match(")")) {
                                    typeDim = trimParams(typeDim);

                                    String p = extractParamType(typeDim);
                                    ArrayList<String> rec = createRecord("function", fType+":"+p, scope.peek() + "." + fName);
                                    if(!stg.create(scope.peek() + "." + fName)) {
                                        System.out.println(" at line " + (lineNum-1));
                                    }
                                    else {
                                        if (!stg.insert(scope.peek(), fName, rec)) {
                                            System.out.println(" at line " + (lineNum - 1));
                                        } else {
                                            System.out.println("Table: function " + scope.peek() + "." + fName + " " + fType + ":" + typeDim + " created.");
                                            ArrayList<String> paraRec = new ArrayList<>();
                                            paraRec.add("variable");
                                            paraRec.add(typeDim.replace(" " + paraName, ""));
                                            paraRec.add("NA");
                                            stg.insert(scope.peek()+"."+fName, paraName, paraRec);
                                            System.out.println("Variable " + paraName + " inserted.");
                                        }
                                    }
                                    typeDim = "";
                                    writer("funcHead-> type id(fParams)");
                                    isFuncStart = false;
                                    System.out.println("function push: " + scope.push(scope.peek() + "." + fName));   // push function table
                                    return true;
                                } else {
                                    writer("Error: missing ')' at line " + lineNum);
                                }
                            }
                        }
                        else {
                            writer("Error: missing '(' at line " + lineNum);
                        }
                    }
                    else {
                        writer("Error: missing 'id' at line " + lineNum);
                    }
                }
                return false;
            }
            else if(T17to19) {
                if(lookAhead.equals("(")){
                    funcParams = true;
                    if(match("(") && T12()) {
                        funcParams = false;
                        if(match(")")) {
                            typeDim = trimParams(typeDim);

                            String p = extractParamType(typeDim);
                            ArrayList<String> rec = createRecord("function", tmpFuncHead.split(" ")[0] +":"+p, scope.peek() + "." + tmpFuncHead.split(" ")[1]);
                            if(!stg.create(scope.peek() + "." + tmpFuncHead.split(" ")[1])) {
                                System.out.println(" at line " + (lineNum-1));
                            }
                            else {
                                if (!stg.insert(scope.peek(), tmpFuncHead.split(" ")[1], rec)) {
                                    System.out.println(" at line " + (lineNum - 1));
                                }
                                else {
                                    System.out.println("Table: function " + tmpFuncHead + ":" + typeDim + " created.");
                                    ArrayList<String> paraRec = new ArrayList<>();
                                    paraRec.add("variable");
                                    paraRec.add(typeDim.replace(" " + paraName, ""));
                                    paraRec.add("NA");
                                    stg.insert(scope.peek()+"."+ tmpFuncHead.split(" ")[1], paraName, paraRec);
                                    System.out.println("Variable " + paraName + " inserted.");
                                }
                            }
                            typeDim = "";
                            writer("funcHead-> type id(fParams)");
                            T17to19 = false;
                            isFuncStart = false;
                            System.out.println("function push: " + scope.push(scope.peek() + "." + tmpFuncHead.split(" ")[1]));
                            return true;
                        }
                    }
                }else {
                    T17to19 = false;
                    isFuncStart = false;
                    return false;
                }
            }
        }
        writer("Error: incorrect funcHead at line " + lineNum);
        return false;
    }

    private String extractParamType(String params) {
        String[] sp = params.split(",");
        String result = "";
        boolean start = false;
        for(int j = 0; j < sp.length; j++) {
            if(!sp[j].equals("")) {
                result += sp[j].split(" ")[0];
                String d = sp[j].split(" ")[1];
                for(int i = 0; i < d.length(); i++) {
                    if(!start && d.charAt(i) == '[') {
                        start = true;
                    }
                    if(start){
                        result += d.charAt(i);
                    }
                }
            }
            start = false;
            if(j < sp.length - 1)
                result += ",";
        }
        return result;
    }

    private String trimParams(String params){
        String result = "";
        int startNum = 0;
        params = params.replace(")", "");
        String[] sp = params.split(",");
        for(String s : sp) {
            // System.out.println("params: " + s);
            String[] param;
            param = s.split("!");

            if(param.length > 0) {
                paraName = param[1];
                if (param[0].equals("")) {
                    startNum = 1;
                }
                String dimension = "";
                for (int i = startNum + 1; i < param.length; i++)
                    dimension += param[i];
                result += param[startNum] + " " + dimension + ",";
            }
        }
        return result;
    }

    // fParams
    // T12-> F2idF3'T13' | #
    private boolean T12(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T12")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T12")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(F2()) {
                    if(match("id")) {
                        if(F3p() && T13p()){
                            writer("fParams-> type id arraySize* fParamsTail");
                            return true;
                        }
                    }
                    else {
                        writer("Error: missing id at line " + lineNum);
                    }
                }
                return false;
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("fParams-> #");
                return true;
            }
        }
        writer("Error: incorrect fParams at line " + lineNum);
        return false;
    }

    // fParamsTail*
    // T13'-> # | T13T13'
    private boolean T13p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T13p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T13p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T13() && T13p()){
                    writer("fParamsTail*-> fParamsTail fParamsTail*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("fParamsTail*-> #");
                return true;
            }
        }
        writer("Error: incorrect fParamsTail at line " + lineNum);
        return false;
    }

    // fParamsTail
    // T13-> ,F2idF3'
    private boolean T13(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T13")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T13")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",")) {
                    if(F2()) {
                        if(match("id")) {
                            if(F3p()){
                                writer("fParamsTail-> ,type id arraySize*");
                                return true;
                            }
                        }
                        else {
                            writer("Error: missing id at line " + lineNum);
                        }
                    }
                }
                else {
                    writer("Error: missing ',' at line " + lineNum);
                }
                return false;
            }
        }
        writer("Error: incorrect fParamsTail at line " + lineNum);
        return false;
    }

    // funcBody
    // T18-> {T17'T16'}
    private boolean T18(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T18")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T18")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("{")) {
                    if(T17p() && T16p()) {
                        if(match("}")){
                            writer("funcBody-> {varDecl* statement*}");
                            System.out.println("function pop: " + scope.pop());    // pop function table
                            return true;
                        }
                        else {
                            writer("Error: missing '}' at line " + lineNum);
                        }
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        writer("Error: incorrect funcBody at line " + lineNum);
        return false;
    }

    // statement
    // T16-> T15 | if(T7)thenT10elseT10; | for(F2id=T7;T9;T15)T10;
    private boolean T16(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T16")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T16")];
        for(String s : first) {
            if (lookAhead.equals(s) || isFuncBody){
                if(lookAhead.equals("id") || isFuncBody){
                    if(isFuncBody) {
                        String ts = tokenString + ", first_after_switched id at ," + lineNum;
                        System.out.println(tokenString + ", first_after_switched id at ," + lineNum);
                        createSemanticRec(ts);
                    }
                    if(T15() && match(";")){
                        writer("statement-> assignStat;");
                        return true;
                    }
                }
                else if(lookAhead.equals("if")) {
                    if(match("if") && match("(")) {
                        String ts = tokenString + ", if id at ," + lineNum;
                        System.out.println(tokenString + ", if id at ," + lineNum);
                        createSemanticRec(ts);
                        if(T14() && F1() && T14()) {
                            if(match(")")) {
                                if (match("then")) {
                                    if (T10()) {
                                        if (match("else")) {
                                            if (T10()) {
                                                if (match(";")) {
                                                    writer("statement-> if(expr)then statBlock else statBlock;");
                                                    return true;
                                                }
                                            }
                                        } else {
                                            writer("Error: missing 'else' at line " + lineNum);
                                        }
                                    }
                                } else {
                                    writer("Error: missing 'then' at line " + lineNum);
                                }
                            }
                            else {
                                writer("Error: missing ')' at line " + lineNum);
                            }
                        }
                    }
                    else{
                        writer("Error: missing '(' at line " + lineNum);
                    }
                }
                else if(lookAhead.equals("for")) {
                    if (match("for") && match("(")) {
                        if (F2()) {
                            String ts = tokenString + ", for loop id at ," + lineNum;
                            System.out.println(tokenString + ", for loop id at ," + lineNum);
                            createSemanticRec(ts);
                            if (match("id")) {
                                if (match("=")) {
                                    // System.out.println(tokenString + ", value at ," + lineNum);
                                    if (T7()) {
                                        if (match(";")) {
                                            if (T9()) {
                                                if (match(";")) {
                                                    if (T15()) {
                                                        if (match(")")) {
                                                            if (T10()) {
                                                                if (match(";")) {
                                                                    writer("statement-> for(type id=expr;relExpr;assignStat)statBlock;");
                                                                    return true;
                                                                } else {
                                                                    writer("Error: missing ';' at line " + lineNum);
                                                                }
                                                            }
                                                        } else {
                                                            writer("Error: missing ')' at line " + lineNum);
                                                        }
                                                    }
                                                } else {
                                                    writer("Error: missing ';' at line " + lineNum);
                                                }
                                            }
                                        } else {
                                            writer("Error: missing ';' at line " + lineNum);
                                        }
                                    }
                                } else {
                                    writer("Error: missing '=' at line " + lineNum);
                                }
                            } else {
                                writer("Error: missing id at line " + lineNum);
                            }
                        } else {
                            writer("Error: missing '(' at line " + lineNum);
                        }
                    }
                }
                else if(lookAhead.equals("get")) {
                    if(match("get") && match("(")) {
                        if(T3()) {
                            if(match(")")) {
                                if(match(";")) {
                                    writer("statement-> get(T3);");
                                    return true;
                                }
                                else {
                                    writer("Error: missing ';' at line " + lineNum);
                                }
                            }
                            else {
                                writer("Error: missing ')' at line " + lineNum);
                            }
                        }
                    }
                    else {
                        writer("Error: missing '(' at line " + lineNum);
                    }
                }
                else if(lookAhead.equals("put")) {
                    if(match("put") && match("(")) {
                        if(T7()) {
                            if (match(")")) {
                                if(match(";")) {
                                    writer("statement-> put(expr);");
                                    return true;
                                }
                                else {
                                    writer("Error: missing ';' at line " + lineNum);
                                }
                            }
                            else {
                                writer("Error: missing ')' at line " + lineNum);
                            }
                        }
                    }
                    else {
                        writer("Error: missing '(' at line " + lineNum);
                    }
                }
                else if(lookAhead.equals("return")) {
                    if (match("return") && match("(")) {
                        if (T7()) {
                            if (match(")")) {
                                if (match(";")) {
                                    writer("statement-> return(expr);");
                                    return true;
                                } else {
                                    writer("Error: missing ';' at line " + lineNum);
                                }
                            } else {
                                writer("Error: missing ')' at line " + lineNum);
                            }
                        } else {
                            writer("Error: missing '(' at line " + lineNum);
                        }
                    }
                }
                writer("Error: incorrect statement at line " + lineNum);
                return false;
            }
        }
        writer("Error: incorrect statements at line " + lineNum);
        return false;
    }

    // statBlock
    // T10-> {T15'} | T15 | #
    private boolean T10(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T10")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T10")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(lookAhead.equals("{")){
                    if(match("{") && T16p()) {
                        if(match("}")){
                            writer("statBlock-> {statement*}");
                            return true;
                        }
                        else {
                            writer("Error: missing '}' at line " + lineNum);
                        }
                    }
                }
                else if(T16()){
                    writer("statBlock-> statement ");
                    return true;
                }
                return false;
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("statBlock-> #");
                return true;
            }
        }
        writer("Error: incorrect statBlock at line " + lineNum);
        return false;
    }

    // assignStat*
    // T15'-> # | T15T15'
    private boolean T15p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T15p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T15p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T15() && T15p() ){
                    writer("assignStat*-> assignStat assignStat*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow) {
            if (lookAhead.equals(s)) {
                writer("assignStat*-> #");
                return true;
            }
        }
        writer("Error: incorrect assignStat at line " + lineNum);
        return false;
    }

    // assignStat
    // T15-> T3=T7
    private boolean T15(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T15")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T15")];
        for(String s : first) {
            if(isFuncBody){
                if(T7()){
                    writer("assignStat-> variable=expr");
                    isFuncBody = false;
                    return true;
                }
                else{
                    isFuncBody = false;
                    return false;
                }
            }
            else if (lookAhead.equals(s)){
                if(T3()){
                    if(match("=")) {
                        if (T7()) {
                            writer("assignStat-> variable=expr");
                            return true;
                        }
                    }
                    else {
                        writer("Error: missing '=' at line " + lineNum);
                    }
                }
                return false;
            }
        }
        writer("Error: incorrect assignStat at line " + lineNum);
        return false;
    }

    // variable
    // T3-> T4'idT6'
    private boolean T3(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T3")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T3")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                semRecord = tokenString;
                startRec = true;
                if(T4p()) {
                    writer("variable-> idnest* id indice*");
                    startRec = false;
                    String ts = semRecord + ", var in assign at ," + lineNum;
                    System.out.println(semRecord + ", var in assign at ," + lineNum);
                    createSemanticRec(ts);
                    return true;
                }
                else {
                    if(isIdnest && var2factor) {
                        isFactor = false;
                        isIdnest = false;
                    }
                    startRec = false;
                    return false;
                }
            }
        }
        if(!isFactor)
            writer("Error: incorrect variable at line " + lineNum);
        return false;
    }

    // idnest*
    // T4'-> # | T4T4'
    private boolean T4p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T4p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T4p")];
        if(var2factor)
            return false;
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if (T4() && T4p()) {
                    writer("idnest*-> idnest idnest*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("idnest*-> #");
                return true;
            }
        }
//        writer("Error: incorrect Idnest at line " + lineNum);
        return false;
    }

    // indice*
    // T6'-> # | T6T6'
    private boolean T6p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T6p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T6p")];
        if(var2factor)
            return false;
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if (T6() && T6p()) {
                    writer("indice*-> indice indice*");
                    return true;
                }
                else {
                    return false;
                }
            }
            else if(lookAhead.equals("(")){
                var2factor = true;
                return false;
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("indice*-> #");
                return true;
            }
        }
        writer("Error: incorrect indice at line " + lineNum);
        return false;
    }

    // expr
    // T7-> T14 | T9
    // replace T9 with T14 F1 T14
    private boolean T7(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T7")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T7")];
        for(String s : first) {
            if (lookAhead.equals(s)) {
                if (T14()) {
                    if (F1()) {
                        if(T14()) {
                            writer("expr-> relExpr");
                            return true;
                        }
                    }
                    else {
                        writer("expr-> arithExpr");
                        return true;
                    }
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: incorrect expr at line " + lineNum);
        return false;
    }

    // relExpr
    // T9-> T14F1T14
    private boolean T9(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T9")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T9")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T14() && F1() && T14()){
                    writer("relExpr-> arithExpr relOp arithExpr");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: incorrect relExpr at line " + lineNum);
        return false;
    }

    // arithExpr
    // T14-> T1T14'
    private boolean T14(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T14")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T14")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T1() && T14p()){
                    writer("arithExpr-> term arithExpr*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: incorrect arithExpr at line " + lineNum);
        return false;
    }

    // arithExpr*
    // T14'-> # | +T1T14' | -T1T14' | orT1T14'
    private boolean T14p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T14p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T14p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                String addOp = "";
                if(lookAhead.equals("+"))
                    addOp = "+";
                else if(lookAhead.equals("-"))
                    addOp = "-";
                else if(lookAhead.equals("or"))
                    addOp = "or";
                if (match("+")  || match("-")  || match("or")) {
                    if(T1() && T14p()) {
                        writer("arithExpr*-> " + addOp + "term arithExpr*");
                        return true;
                    }
                }
                return false;
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("arithExpr*-> #");
                return true;
            }
        }
        writer("Error: incorrect arithExpr at line " + lineNum);
        return false;
    }

    // term
    // T1-> T2T1'
    private boolean T1(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T1")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T1")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T2() && T1p()){
                    writer("term-> factor term*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        writer("Error: incorrect term at line " + lineNum);
        return false;
    }

    // term*
    // T1'-> # | xT2T1' | /T2T1' | andT2T1'
    private boolean T1p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T1p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T1p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                String multOp = "";
                if(lookAhead.equals("*"))
                    multOp = "*";
                else if(lookAhead.equals("/"))
                    multOp = "/";
                else if(lookAhead.equals("and"))
                    multOp = "and";
                if (match("*") || match("/")  || match("and")) {
                    if(T2() && T1p()) {
                        writer("term*-> " + multOp + "factor term*");
                        return true;
                    }
                }
                else {
                    writer("Error: incorrect term at line " + lineNum);
                    return false;
                }
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("term*-> #");
                return true;
            }
        }
        writer("Error: incorrect terms at line " + lineNum);
        return false;
    }

    // factor
    // T2-> T3 | T4'id(T5) | num | (T14) | notT2 | +T2 | -T2
    private boolean T2(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T2")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                startRec = true;
                semRecord = tokenString;
                isFactor = true;
                if(T3()) {
                    writer("factor-> variable");
//                    startRec = false;
//                    System.out.println("factor: " + semRecord + ", at line " + lineNum);
                    return true;
                }
                else if(var2factor || T4p()) {
                    if(var2factor || match("id")) {
                        if(var2factor)
                            var2factor = false;
                        if(match("(")) {
                            if(T5()) {
                                if(match(")")){
                                    writer("factor-> idnest* id(aParams)");
                                    startRec = false;
                                    String ts = "factor:" + semRecord + "," + lineNum;
                                    System.out.println("factor:" + semRecord + "," + lineNum);
                                    createSemanticRec(ts);
                                    return true;
                                }
                                else {
                                    writer("Error: missing ')' at line " + lineNum);
                                }
                            }
                        }
                        else {
                            writer("Error: missing '(' at line " + lineNum);
                        }
                    }
                    else {
                        writer("Error: missing id at line " + lineNum);
                    }
                }
                else if(match("integer") || match("nfloat")) {
                    writer("factor-> number");
                    startRec = false;
                    String ts = "factor:" + semRecord + "," + lineNum;
                    System.out.println("factor:" + semRecord + "," + lineNum);
                    createSemanticRec(ts);
                    return true;
                }
                else if(match("(")) {
                    if(T14()) {
                        if(match(")")) {
                            writer("factor-> (arithExpr)");
                            startRec = false;
                            String ts = "factor:" + semRecord + "," + lineNum;
                            System.out.println("factor:" + semRecord + "," + lineNum);
                            createSemanticRec(ts);
                            return true;
                        }
                        else {
                            writer("Error: missing ')' at line " + lineNum);
                        }
                    }
                }
                else if(match("not") && T2()) {
                    writer("factor-> not factor");
                    startRec = false;
                    String ts = "factor:" + semRecord + "," + lineNum;
                    System.out.println("factor:" + semRecord + "," + lineNum);
                    createSemanticRec(ts);
                    return true;
                }
                else if(match("+") && T2()) {
                    writer("factor-> +factor");
                    startRec = false;
                    String ts = "factor:" + semRecord + "," + lineNum;
                    System.out.println("factor:" + semRecord + "," + lineNum);
                    createSemanticRec(ts);
                    return true;
                }
                else if( match("-") && T1p()){
                    writer("factor-> -factor");
                    startRec = false;
                    String ts = "factor:" + semRecord + "," + lineNum;
                    System.out.println("factor:" + semRecord + "," + lineNum);
                    createSemanticRec(ts);
                    return true;
                }
                writer("Error: incorrect term at line " + lineNum);
                return false;

            }
        }
        writer("Error: incorrect factor at line " + lineNum);
        return false;
    }

    // idnest
    // T4-> idT6'.
    private boolean T4(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T4")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T4")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("id")) {
                    if(T6p()){
                        if(match(".")) {
                            writer("idnest-> id indice* .");
                            return true;
                        }
                        else {
                            writer("idnest-> idnest* id indice*");
                            return true;
                        }
                    }
                    else {
                        if(var2factor)
                            return true;
                        else
                            return false;
                    }
                }
                else {
                    writer("Error: missing id at line " + lineNum);
                    return false;
                }
            }
        }
        writer("Error: incorrect idnest at line " + lineNum);
        return false;
    }

    // indice
    // T6-> [T14]
    private boolean T6(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T6")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T6")];
        for(String s : first) {
            if(lookAhead.equals("(") && isIdnest){
                var2factor = true;
                return true;
            }
            if (lookAhead.equals(s)){
                if(match("[")) {
                    if(T14()) {
                        if(match("]")){
                            writer("indice-> [arithExpr]");
                            return true;
                        }
                        else {
                            writer("Error: missing ']' at line " + lineNum);
                        }
                    }
                }
                return false;
            }
        }
        writer("Error: incorrect indice at line " + lineNum);
        return false;
    }

    // aParams
    // T5-> T7T8' | #
    private boolean T5() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T5")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T5")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((T7() && T8p())) {
                    writer("aParams-> expr aParamsTail*");
                    return true;
                } else {
                    return false;
                }
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("aParams-> #");
                return true;
            }
        }
        writer("Error: incorrect aParams at line " + lineNum);
        return false;
    }

    // aParamsTail*
    // T8'-> # | T8T8'
    private boolean T8p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T8")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T8")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((T8() && T8p())) {
                    writer("aParamsTail*-> aParamsTail aParamsTail*");
                    return true;
                } else {
                    return false;
                }
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("aParamsTail*-> #");
                return true;
            }
        }
        writer("Error: incorrect aParamsTails at line " + lineNum);
        return false;
    }

    // aParamsTail
    // T8-> ,T7
    private boolean T8(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T8")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T8")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",")) {
                    if(T7()){
                        writer("aParamsTail-> ,expr");
                        return true;
                    }
                }
                else {
                    writer("Error: missing ',' at line " + lineNum);
                }
                return false;
            }
        }
        writer("Error: incorrect aParamsTail at line " + lineNum);
        return false;
    }

    // relOp
    // F1: == | <= | >= | < | > |<>
    private boolean F1(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F1")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F1")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("==") || match("<=")
                        || match(">=") || match("<")
                        || match(">") || match("<>")){
                    writer("relOp-> == | <= | >= | < | > | <>");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
//        writer("Error: incorrect relOp at line " + lineNum);
        return false;
    }

    private boolean skipErrors(String[] first, String[] follow){
        boolean isError = false;
        if(lookAhead.equals("#")){
            for(String s : follow){
                if(lookAhead.equals(s))
                    return !isError;
            }
        }
        else{
            for(String s : first) {
                if (lookAhead.equals(s))
                    return !isError;
            }
        }
        isError = true;
        writer("Syntax error at line " + lineNum);
        while(isError){
            lookAhead = nextToken();
            if(lookAhead.equals(";")){
                lookAhead = nextToken();
                break;
            }
            if(lookAhead.equals("#")){
                for(String s : follow){
                    if(lookAhead.equals(s))
                        isError = false;
                }
            }
            else{
                for(String s : first) {
                    if (lookAhead.equals(s))
                        isError = false;
                }
            }
        }
        return isError;
    }
}
