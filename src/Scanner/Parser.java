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
    int lineNum = 0;
    public boolean toFile = false;
    PrintWriter out = null;

    public Parser(){
        la = new LexicalAnalyzer();
        la.writeToFile = true;
        la.extractTokens();
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
            while(line!= null && line.equals("comment")) {
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

    //Output
    private void writer(String s){
        if(toFile) {
            out.println(s);
        }
        else{
            System.out.println(s);
        }
    }
    //E-> T21'T20
    private boolean startSymbol(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("E")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("E")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T21p() && T20()){
                    String o = "E-> classDecl* progBody";
                    writer(o);
//                    lookAhead = nextToken();
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

    //T21'-> # | T21T21'
    private boolean T21p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T21p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T21p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T21() && T21p()){
                    writer("classDecl*-> classDecl classDecl*");
                    //lookAhead = nextToken();
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

    //T20-> program{T17'T16'};T19'
    private boolean T20(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T20")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T20")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("program") && match("{") && T17p() && T16p() && match("}")
                        && match(";") && T19p()){
                    writer("progBody-> program{varDecl*statement*};funcDef*");
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

    //T21-> class id {T17'T19'};
    private boolean T21(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T21")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T21")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                isClass = true;
                if(match("class") && match("id") && match("{")
                        && T17p() && T19p() && match("}") && match(";")){
                    writer("classDecl-> class id {varDecl*funcDef*};");
                    //lookAhead = nextToken();
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

    //T17'-> # | T17T17'
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
                    writer("varDecl*-> varDecl varDecl*");
                    //lookAhead = nextToken();
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

    //T16'-> # | T16T16'
    private boolean T16p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T16p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T16p")];
        for(String s : first) {
            if (lookAhead.equals(s) || isFuncBody){
                if(T16() && T16p()){
                    writer("statement*-> statement statement*");
//                    lookAhead = nextToken();
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

    //T19'-> # | T19T19'
    private boolean T19p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T19p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T19p")];
        for(String s : first) {
            if (lookAhead.equals(s) || T17to19){
                if(T19() && T19p()){
                    writer("funcDef*-> funcDef funcDef*");
                    //lookAhead = nextToken();
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

    //T17-> F2idF3';
    private boolean T17(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T17")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T17")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("id")) {
                    if (match("=")) {
                        isFuncBody = true;
                        return true;
                    }
                    else if (match("id")){
                        if(isClass && lookAhead.equals("(")){
                            T17to19 = true;
                            return true;
                        }
                        else if(F3p() && match(";")) {
                            writer("varDecl-> type id arraySize*;");
                            return true;
                        }
                    }
                }
                else if(F2() && match("id")){
                    if(isClass && lookAhead.equals("(")){
                        T17to19 = true;
                        return true;
                    }
                    else if(F3p() && match(";")) {
                        writer("varDecl-> type id arraySize*;");
                        return true;
                    }
                }
                else{
                    return false;
                }
            }
        }

        return false;
    }

    //F3'-> # | F3F3';
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

    //F2-> int | float | id
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

    //F3-> [ integer ]
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

    //T19-> T11T18;
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

    //T11-> F2id(T12)
    private boolean T11(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T11")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T11")];
        for(String s : first) {
            if (lookAhead.equals(s)) {
                if (F2() && match("id") && match("(")
                        && T12() && match(")")) {
                    writer("funcHead-> type id(T12)");
                    //lookAhead = nextToken();
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

    //T12-> F2idF3'T13' | #
    private boolean T12(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T12")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T12")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(F2() && match("id") && F3p() && T13p()){
                    writer("fParams-> type id arraySize* fParamsTail");
                    //lookAhead = nextToken();
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

    //T13'-> # | T13T13'
    private boolean T13p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T13p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T13p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T13() && T13p()){
                    writer("fParamsTail*-> fParamsTail fParamsTail*");
                    //lookAhead = nextToken();
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

    //T13-> ,F2idF3'
    private boolean T13(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T13")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T13")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",") && F2() && match("id") && F3p()){
                    writer("fParamsTail-> ,type id arraySize*");
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

    //T18-> {T17'T16'}
    private boolean T18(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T18")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T18")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("{") && T17p() && T16p() && match("}")){
                    writer("funcBody-> {varDecl*statement*}");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T16-> T15 | if(T7)thenT10elseT10; | for(F2id=T7;T9;T15)T10;
    private boolean T16(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T16")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T16")];
        for(String s : first) {
            if (lookAhead.equals(s) || isFuncBody){
                if(lookAhead.equals("id")){
                    if(T15() && match(";")){
                        writer("statement-> assignStat;");
                        return true;
                    }
                }
                else if(isFuncBody){
                    if(T15() && match(";")){
                        writer("statement-> assignStat;");
                        return true;
                    }
                }
                if((match("if") && match("(") && T14() && F1() && T14() && match(")")
                                && match("then") && T10() && match("else")
                                && T10() && match(";")) ||
                        (match("for") && match("(") && F2() && match("id")
                                && match("=") && T7() && match(";")
                                && T9() && match(";") && T15() && match(")")
                                && T10() && match(";")) ||
                        (match("get") && match("(") && T3() && match(")") && match(";")) ||
                        (match("put") && match("(") && T7() && match(")") && match(";")) ||
                        (match("return") && match("(") && T7() && match(")") && match(";"))
                        ){
                    writer("statement-> if(expr)then statBlock else statBlock; | for(type id=expr;relExpr;assignStat)statBlock;");
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

    //T10-> {T15'} | T15 | #
    private boolean T10(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T10")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T10")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if((match("{") && T16p() && match("}")) || T16()){
                    writer("statBlock-> {statement*} | statement ");
//                    lookAhead = nextToken();
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

        return false;
    }

    //T15'-> # | T15T15'
    private boolean T15p(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T15p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T15p")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T15() && T15p() ){
                    writer("assignStat*-> assignStat assignStat*");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        for(String s : follow){
            if(lookAhead.equals(s)){
                writer("assignStat*-> #");
                return true;
            }
        }

        return false;
    }

    //T15-> T3=T7
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
                }
            }
            else if (lookAhead.equals(s)){
                if(T3() && match("=") && T7()){
                    writer("assignStat-> variable=expr");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T3-> T4'idT6'
    private boolean T3(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T3")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T3")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("id") && T6p()){
                    if(match(".")){
                        isFactor = true;
                        if(match("id")){
                            if(match("(") && T5() && match(")")) {
                                writer("factor-> idnest*id(aParams)");
                                return true;
                            }
                            else if(T6p()) {
                                writer("factor-> idnest*id indice*");
                                return true;
                            }
                        }
                    }
                    else {
                        writer("factor-> id indice*");
//                    lookAhead = nextToken();
                        return true;
                    }
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T4'-> # | T4T4'
    private boolean T4p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T4p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T4p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if (T4() && T4p()) {
                    writer("idnest*-> idnest idnest*");
//                    lookAhead = nextToken();
                    return true;
                } else {
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

    //T6'-> # | T6T6'
    private boolean T6p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T6p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T6p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if (T6() && T6p()) {
                    writer("indice*-> indice indice*");
//                    lookAhead = nextToken();
                    return true;
                } else {
                    return false;
                }
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

    //T7-> T14 | T9
    private boolean T7(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T7")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T7")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T14() || T9()){
                    writer("expr-> arithExpr | relExpr");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T9-> T14F1T14
    private boolean T9(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T9")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T9")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T14() && F1() && T14()){
                    writer("relExpr-> arithExpr relOp arithExpr");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T14-> T1T14'
    private boolean T14(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T14")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T14")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T1() && T14p()){
                    writer("arithExpr-> term arithExpr*");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T14'-> # | +T1T14' | -T1T14' | orT1T14'
    private boolean T14p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T14p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T14p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((match("+") && T1() && T14p()) ||
                        (match("-") && T1() && T14p()) ||
                        (match("or") && T1() && T14p())) {
                    writer("arithExpr*-> +term arithExpr* | -term arithExpr* | or term arithExpr*");
//                    lookAhead = nextToken();
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

    //T1-> T2T1'
    private boolean T1(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T1")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T1")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T2() && T1p()){
                    writer("term-> factor term*");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T1'-> # | xT2T1' | /T2T1' | andT2T1'
    private boolean T1p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T1p")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T1p")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((match("*") && T2() && T1p()) ||
                        (match("/") && T2() && T1p()) ||
                        (match("and") && T2() && T1p())) {
                    writer("arithExpr*-> *factor term* | /facotr term* | and factor term*");
//                    lookAhead = nextToken();
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

    //T2-> T3 | T4'id(T5) | num | (T14) | notT2 | +T2 | -T2
    private boolean T2(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T2")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(T3() ||
                        (T4p() && match("id") && match("(")
                                && T5() && match(")")) ||
                        match("integer") || match("nfloat") ||
                        (match("(") && T14() && match(")")) ||
                        (match("not") && T2()) ||
                        (match("+") && T2()) ||
                        ( match("-") && T1p())){
                    writer("factor-> variable | idnest'id(aParams) | num | (arithExpr) | notfacot | +factor | -factor");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    //T4-> idT6'.
    private boolean T4(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T4")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T4")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("id") && T6p()){
                    writer("idnest-> id indice*");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T6-> [T14]
    private boolean T6(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T6")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T6")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("[") && T14() && match("]")){
                    writer("indice-> [arithExpr]");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        return false;
    }

    //T5-> T7T8' | #
    private boolean T5() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T5")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T5")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((T7() && T8p())) {
                    writer("aParams-> expr aParamsTail*");
//                    lookAhead = nextToken();
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

    //T8'-> # | T8T8'
    private boolean T8p() {
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T8")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T8")];
        for (String s : first) {
            if (lookAhead.equals(s)) {
                if ((T8() && T8p())) {
                    writer("aParamsTail*-> aParamsTail aParamsTail*");
//                    lookAhead = nextToken();
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

    //T8-> ,T7
    private boolean T8(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("T8")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("T8")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match(",") && T7()){
                    writer("aParamsTail-> ,expr");
//                    lookAhead = nextToken();
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    //F1: == | <= | >= | < | > |<>
    private boolean F1(){
        String[] first = FirstNFollow.FIRST[FirstNFollow.ORDER.get("F1")];
        String[] follow = FirstNFollow.FOLLOW[FirstNFollow.ORDER.get("F1")];
        for(String s : first) {
            if (lookAhead.equals(s)){
                if(match("==") || match("<=")
                        || match(">=") || match("<")
                        || match(">") || match("<>")){
                    writer("relOp-> == | <= | >= | < | > | <>");
//                    lookAhead = nextToken();
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
