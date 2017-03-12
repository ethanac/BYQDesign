package Scanner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ethan on 2017-02-21.
 */
public class Parser {
    String lookAhead = "";
    LexicalAnalyzer la = null;
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
    public boolean toFile = false;
    PrintWriter out = null;

    public Parser(){
//        la = new LexicalAnalyzer();
//        la.writeToFile = true;
//        la.extractTokens();
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
        try {
            line = br.readLine();
            lineNum++;
            while(line!= null && line.contains("comment:")) {
                line = br.readLine();
                lineNum++;
            }
            if(line!=null) {
                writer(line);
                line = line.split(":")[0];
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return line;
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

    // start symbol
    // E-> T21'T20
    private boolean startSymbol(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("E")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("E")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T21p() && T20()){
                    String o = "E-> classDecl* progBody";
                    writer(o);
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
        return false;
    }

    // progBody
    // T20-> program{T17'T16'};T19'
    private boolean T20(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T20")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T20")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("program") && match("{") && T17p() && T16p() && match("}")
                        && match(";") && T19p()){
                    writer("progBody-> program{varDecl*statement*};funcDef*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
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
                if(match("class") && match("id") && match("{")
                        && T17p() && T19p() && match("}") && match(";")){
                    writer("classDecl-> class id {varDecl*funcDef*};");
                    isClass = false;
                    return true;
                }
                else {
                    isClass = false;
                    return false;
                }
            }
        }
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
        return false;
    }

    // varDecl
    // T17-> F2idF3';
    private boolean T17(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T17")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T17")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("id")) {
                    if (lookAhead.equals("=")) {
                        match("=");
                        isFuncBody = true;
                        return true;
                    }
                    else if (match("id")){
                        if(ifSwitchTo19())
                            return true;
                    }
                }
                else if(F2() && match("id")){
                    if(ifSwitchTo19())
                        return true;
                }
                else{
                    return false;
                }
            }
        }
        return false;
    }

    private boolean ifSwitchTo19() {
        if(isClass && lookAhead.equals("(")){
            T17to19 = true;
            return true;
        }
        else if(F3p() && match(";")) {
            writer("varDecl-> type id arraySize*;");
            return true;
        }
        else {
            return false;
        }
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

        return false;
    }

    // Type
    // F2-> int | float | id
    private boolean F2(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F2")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F2")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("int") || match("float") || match("id")){
                    writer("type-> int | float | id");
                    return true;
                }
                else {
                    return false;
                }
            }
        }

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

        return false;
    }

    // funcDef
    // T19-> T11T18;
    private boolean T19(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T19")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T19")];
        for(String s : first) {
            if (lookAhead.equals(s) || T17to19){
                if(T11() && T18() && match(";")){
                    writer("funcDef-> funcHead funcBody");
                    //lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    // funcHead
    // T11-> F2id(T12)
    private boolean T11(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T11")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T11")];
        for(String s : first) {
            if (lookAhead.equals(s)) {
                if (F2() && match("id") && match("(")
                        && T12() && match(")")) {
                    writer("funcHead-> type id(fParams)");
                    return true;
                } else {
                    return false;
                }
            }
            else if(T17to19 && lookAhead.equals("(")){
                if(match("(") && T12() && match(")")){
                    writer("funcHead-> type id(fParams)");
                    T17to19 = false;
                    return true;
                }else {
                    T17to19 = false;
                    return false;
                }
            }
        }

        return false;
    }

    // fParams
    // T12-> F2idF3'T13' | #
    private boolean T12(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T12")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T12")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(F2() && match("id") && F3p() && T13p()){
                    writer("fParams-> type id arraySize* fParamsTail");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("fParams-> #");
                return true;
            }
        }

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

        return false;
    }

    // fParamsTail
    // T13-> ,F2idF3'
    private boolean T13(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T13")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T13")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",") && F2() && match("id") && F3p()){
                    writer("fParamsTail-> ,type id arraySize*");
                    return true;
                }
                else {
                    return false;
                }
            }
        }

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
        writer("Error: missing funcBody at line " + lineNum);
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
                    if(T15() && match(";")){
                        writer("statement-> assignStat;");
                        return true;
                    }
                }
//                else if(isFuncBody){
//                    if(T15() && match(";")){
//                        writer("statement-> assignStat;");
//                        return true;
//                    }
//                }
                else if(lookAhead.equals("if")) {
                    if(match("if") && match("(")) {
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
                            if (match("id")) {
                                if (match("=")) {
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
                else{
                    return false;
                }
            }
        }
        writer("Error: missing statements at line " + lineNum);
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
                    if(match("{") && T16p() && match("}")){
                        writer("statBlock-> {statement*}");
                        return true;
                    }
                }
                else if(T16()){
                    writer("statBlock-> statement ");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("statBlock-> #");
                return true;
            }
        }
        writer("Error: missing statBlock at line " + lineNum);
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
        writer("Error: missing assignStat at line " + lineNum);
        return false;
    }

    // variable
    // T3-> T4'idT6'
    private boolean T3(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T3")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T3")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T4p()) {
//                    if(match("id")){
//                        if(T6p()){
                            writer("variable-> idnest* id indice*");
                            return true;
//                        }
//                    }
//                    else {
//                        writer("Error: missing id at line " + lineNum);
//                    }
                }
                else {
                    if(isIdnest && var2factor) {
                        isFactor = false;
                        isIdnest = false;
                    }
                    return false;
                }
            }
        }
        if(!isFactor)
            writer("Error: missing variable at line " + lineNum);
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
        writer("Error: missing expr at line " + lineNum);
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
        return false;
    }

    // arithExpr*
    // T14'-> # | +T1T14' | -T1T14' | orT1T14'
    private boolean T14p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T14p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T14p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((match("+") && T1() && T14p()) ||
                        (match("-") && T1() && T14p()) ||
                        (match("or") && T1() && T14p())) {
                    writer("arithExpr*-> +term arithExpr* | -term arithExpr* | or term arithExpr*");
                    return true;
                } else {
                    return false;
                }
            }
        }
        for (String s : follow) {
            if (lookAhead.equals(s)) {
                writer("arithExpr*-> #");
                return true;
            }
        }

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

        return false;
    }

    // term*
    // T1'-> # | xT2T1' | /T2T1' | andT2T1'
    private boolean T1p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T1p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T1p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((match("*") && T2() && T1p()) ||
                        (match("/") && T2() && T1p()) ||
                        (match("and") && T2() && T1p())) {
                    writer("term*-> *factor term* | /facotr term* | and factor term*");
                    return true;
                } else {
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

        return false;
    }

    // factor
    // T2-> T3 | T4'id(T5) | num | (T14) | notT2 | +T2 | -T2
    private boolean T2(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T2")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                isFactor = true;
                if(T3()) {
                    writer("factor-> variable");
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
                    return true;
                }
                else if(match("(")) {
                    if(T14()) {
                        if(match(")")) {
                            writer("factor-> (arithExpr)");
                            return true;
                        }
                        else {
                            writer("Error: missing ')' at line " + lineNum);
                        }
                    }
                }
                else if(match("not") && T2()) {
                    writer("factor-> not factor");
                    return true;
                }
                else if(match("+") && T2()) {
                    writer("factor-> +factor");
                    return true;
                }
                else if( match("-") && T1p()){
                    writer("factor-> -factor");
                    return true;
                }
                return false;

            }
        }
        writer("Error: missing factor at line " + lineNum);
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
        writer("Error: missing idnest at line " + lineNum);
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
        writer("Error: missing indice at line " + lineNum);
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
        return false;
    }

    // aParamsTail
    // T8-> ,T7
    private boolean T8(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T8")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T8")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",") && T7()){
                    writer("aParamsTail-> ,expr");
                    return true;
                }
                else {
                    return false;
                }
            }
        }
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
