package Scanner;

import java.io.*;
import java.util.HashMap;

/**
 * Created by Hao on 2017-01-19.
 */
public class LexicalAnalyzer {

    private String newLine;
    private String fileName = "file.txt";
    String tableFileName = "STT_Alpha.csv";
    private int rowNum = 120;
    private int colNum = 40;
    HashMap<Character, Integer> charMap = null;
    int[][] table = new int[rowNum][colNum];
    public int cmtCounter = 0;
    int nextPosition = 0;
    int currentPosition = 0;
    boolean inlineCmt = false;
    public int numOfLine = 1;
    BufferedReader br = null;
    public boolean writeToFile = false;
    PrintWriter out = null;
    boolean startSymbol = true;
    String rawLine = null;

    String[] tokens = {
            "id", "num_integer", "num_float", "operator_equal", "operator_lessThan", "operator_greaterThan",
            "operator_greaterEqual", "operator_lessEqual", "angleBrackets", "semicolon", "comma", "dot", "plus",
            "minus", "underline", "star", "slash", "assign", "rw_and", "rw_not", "rw_or", "openPar", "closePar",
            "openCurlyBracket", "closeCurlyBracket", "openSquareBracket", "closeSquareBracket", "comment", "inlineComment",
            "rw_if", "rw_then", "rw_else", "rw_for", "rw_class", "rw_int", "rw_float", "rw_get", "rw_put", "rw_return",
            "rw_program"
    };

    public LexicalAnalyzer(){
        importStateTransitionTable();
        setColumnNumber();
        try{
            br = new BufferedReader(new FileReader(fileName));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getToken() throws IOException{
        String result = "$";
        if(startSymbol) {
            startSymbol = false;
            rawLine = br.readLine();
        }
        if(rawLine != null) {
            if (nextPosition < rawLine.length()) {
            }
            else {
                currentPosition = 0;
                nextPosition = 0;
                try {
                    rawLine = br.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(rawLine == null)
                    return result;
                numOfLine++;
            }
            String token = nextToken(rawLine);
            while (token != null && token.equals("sp"))
                token = nextToken(rawLine);
            if (token == null && cmtCounter > 0) {
                result = "comment:comment" + numOfLine;
            }
            else {
                if (token.toLowerCase().contains("comment"))
                    token = "comment";
                else if (token.toLowerCase().contains("num_integer"))
                    token = "integer";
                else if (token.toLowerCase().contains("num_float"))
                    token = "nfloat";
                else if (!token.equals("id"))
                    token = rawLine.substring(currentPosition, nextPosition);
                result = token + ":" + rawLine.substring(currentPosition, nextPosition) + ":" + numOfLine;
                currentPosition = nextPosition;
            }
            //return result;
        }
        return result;
    }

    public void extractTokens(){
        String line = "";

        if(writeToFile){
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter("Output.txt", true)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            br = new BufferedReader(new FileReader(fileName));
            line = br.readLine();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        while(line != null){
            while (nextPosition < line.length()) {
                String token = nextToken(line);
                if (token == null && cmtCounter > 0) {
                    if(writeToFile)
                        out.println("comment:" + numOfLine);
                    else
                        System.out.println("comment:" + numOfLine);
                }
                else if(!token.equals("sp")) {
                    if(writeToFile) {
                        if(token.toLowerCase().contains("comment"))
                            token = "comment";
                        else if(token.toLowerCase().contains("num_integer"))
                            token = "integer";
                        else if(token.toLowerCase().contains("num_float"))
                            token = "nfloat";
                        else if(!token.equals("id"))
                            token = line.substring(currentPosition, nextPosition);
                        out.println(token + ":" + line.substring(currentPosition, nextPosition));// + "     at line " + numOfLine);
                    }
                    else
                        System.out.println(token + ": " + line.substring(currentPosition, nextPosition) + "     at line " + numOfLine);
                    currentPosition = nextPosition;
                }
            }

            currentPosition = 0;
            nextPosition = 0;
            try{
                line = br.readLine();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            numOfLine++;
        }
        out.println("$");
        if(cmtCounter > 0){
            if(writeToFile)
                out.println("Error: " + cmtCounter + " \"*/\" is missing.");
            else
                System.out.println("Error: " + cmtCounter + " \"*/\" is missing.");
        }
        try {
            br.close();
            if(writeToFile) {
                out.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void importStateTransitionTable(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(tableFileName));
            String line = br.readLine();
            int row = 0;
            while(line != null) {
                String[] states = line.split(",");
                for(int i = 0; i < states.length; i++)
                    table[row][i] = Integer.parseInt(states[i]);
                row++;
                line = br.readLine();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String nextToken(String s){
        if(s.length() == 0 && cmtCounter > 0)
            return null;
        s = s.replace("\t", " ");
        int state = 0;
        if(cmtCounter > 0)
            state = 37;
        String token = null;
        while(token == null){
            char c;
            //System.out.println(nextPosition);
            if(nextPosition >= s.length())
                c = ';';
            else
                c = s.charAt(nextPosition);
            //System.out.println(c);
            int col = -1;
            if(charMap.containsKey(c)){
                col = charMap.get(c);
            }
            else {
                if(cmtCounter > 0 || inlineCmt)
                    col = charMap.get(';');
                else {
                    nextPosition++;
                    return "Unknown token";
                }
            }
            //System.out.println(col);
            state = table[state][col] - 1;
            //System.out.println(state);

            if(state == 120) {
                currentPosition++;

                if(currentPosition == s.length()) {
                    nextPosition = s.length();
                    return "sp";
                }
                state = 0;
            }
            else {
                if (state == 36)    // When got a '/*', counter increment.
                    cmtCounter++;
                if (state == 41)
                    inlineCmt = true;
                if (table[state][0] > -1) {     // Being greater than -1 means it is a final token.
                    token = tokens[table[state][0]];
                    if (table[state][1] == 1)
                        backupChar(state);
                } else if (nextPosition == s.length()) {
                    if (inlineCmt) {
                        token = "inlineComment";
                        inlineCmt = false;
                    } else
                        token = "------";
                    nextPosition--;
                }
            }
            nextPosition++;
        }
        if(token.equals("comment") && cmtCounter > 0) {
            cmtCounter--;
            if(cmtCounter > 0) {
                if(nextPosition >= s.length())
                    token = null;
                else
                    token = "sp";
            }
        }
        //System.out.println(token);
        if(token == null)
            return token;
        return token.trim();
    }

    public void backupChar(int key){
        nextPosition--;
        if(key == 10)
            nextPosition--;
    }

    public void setColumnNumber(){
        charMap = new HashMap<Character, Integer>();
        char[] chars = {'.','_','=','<','>',';',',','+','-','*','/','(',')','{','}','[',']','a','c','d','e','f','g','h',
                'i','l','m','n','o','p','r','s','t','u',' '};

        int col = 5;
        for(char c : chars) {
            charMap.put(c, col);
            col++;
        }

        char[] noneZeros = {'1','2','3','4','5','6','7','8','9'};
        for(char c: noneZeros){
            charMap.put(c, 4);
        }

        char[] letters = {'b','j','k','q','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N',
                'O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        for(char c : letters){
            charMap.put(c, 2);
        }
        charMap.put('0', 3);

    }

    /*
     * For testing
     */
    public String extractTokens(String line){
        String result = "";

        while (nextPosition < line.length()) {
            String token = nextToken(line);
            if (token == null && cmtCounter > 0) {
                //System.out.println("------: " + line + "     at line " + numOfLine);
                result = "------: " + line + ": " + numOfLine + "\n";
            }
            else if(!token.equals("sp")) {
                //System.out.println(token + ": " + line.substring(currentPosition, nextPosition) + "     at line " + numOfLine);
                result += token + ": " + line.substring(currentPosition, nextPosition) + ": " + numOfLine +"\n";
                currentPosition = nextPosition;
            }
        }
        currentPosition = 0;
        nextPosition = 0;

        numOfLine++;
        return result;
    }

}
