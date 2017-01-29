package Scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by Ethan on 2017-01-19.
 */
public class LexicalAnalyzer {

    private String newLine;
    private String fileName = "file.txt";
    String tableFileName = "STT_Alpha.csv";
    private int rowNum = 120;
    private int colNum = 40;
    HashMap<Character, Integer> charMap = null;
    int[][] table = new int[rowNum][colNum];
    int cmtCounter = 0;
    int nextPosition = 0;
    int currentPosition = 0;
    boolean inlineCmt = false;
    int numOfLine = 1;
    BufferedReader br = null;

    String[] tokens = {
            "id", "num_integer", "num_float", "operator_equal", "operator_lessThan", "operator_greaterThan",
            "operator_greaterEqual", "operator_lessEqual", "angleBrackets", "semicolon", "comma", "dot", "plus",
            "minus", "underline", "star", "slash", "assign", "rw_and", "rw_not", "rw_or", "openPar", "closePar",
            "openCurlyBracket", "closeCurlyBracket", "squareBracket", "squareBracket", "comment", "inlineComment",
            "rw_if", "rw_then", "rw_else", "rw_for", "rw_class", "rw_int", "rw_float", "rw_get", "rw_put", "rw_return",
            "rw_program"
    };

    public LexicalAnalyzer(){
        importStateTransitionTable();
        setColumnNumber();
    }

    public void extractTokens(){
        String line = "";
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
                //System.out.println(cmtCounter);
                if (token == null && cmtCounter > 0) {
                    System.out.println("------: " + line + "     at line " + numOfLine);
                }
                else if(!token.equals("sp")) {
                    //System.out.println(numOfLine + ": " + token + currentPosition + ", " + nextPosition);
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
        try {
            br.close();
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
                nextPosition++;
                return "Unknown token";
            }
            //System.out.println(col);
            state = table[state][col] - 1;
            //System.out.println(state);

            if(state == 120) {
                currentPosition++;
//                System.out.println(currentPosition);
//                System.out.println("line: " + s.length());
                if(currentPosition == s.length()) {
                    nextPosition = s.length();
                    return "sp";
                }
                state = 0;
            }
            else {
                if (state == 36)
                    cmtCounter++;
                if (state == 41)
                    inlineCmt = true;
                if (table[state][0] > -1) {
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
            if(cmtCounter > 0)
                token = null;
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
                result = "------: " + line + ": " + numOfLine;
            }
            else if(!token.equals("sp")) {
                //System.out.println(token + ": " + line.substring(currentPosition, nextPosition) + "     at line " + numOfLine);
                result = token + ": " + line.substring(currentPosition, nextPosition) + ": " + numOfLine;
                currentPosition = nextPosition;
            }
        }
        currentPosition = 0;
        nextPosition = 0;

        numOfLine++;
        return result;
    }

}
