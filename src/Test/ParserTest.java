import Scanner.LexicalAnalyzer;
import Scanner.Parser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ethan on 2017-02-27.
 */
public class ParserTest {
    String fileName = "file_correct.txt";
    LexicalAnalyzer la = new LexicalAnalyzer(fileName);
    Parser parser = new Parser(fileName);
    //    char[] chars = {'.','_','=','>','<',';',',','+','-','*','/','(',')','{','}','[',']','a','n','d',' ','t','h','e','n',
//           ' ','i','f',' ','e','l','s','e',' ','f','o','r',' ','c','l','a','s','s',' ','o','r',' ','n','o','t',' ',
//            'i','n','t',' ','i','n','t','e','g','e','r',' ','f','l','o','a','t',' ','g','e','t',' ','p','u','t',' ',
//            'p','r','o','g','r','a','m',' ', 'r','e','t','u','r','n', ' ','=','=',};
    String[] reservedWords = {".","_","=",">","<",";",",","+","-","*","/","(",")","{","}","[","]","and ","then ","if ",
            "else ","for ","class ","or ","not ","int ","float ","get ","put ","program ","return",">=","<=","==","<>",
            "/**/","//"};
    String[] nameOfReservedWords = {"dot","underline","assign","operator_greaterThan","operator_lessThan","semicolon",
            "comma","plus","minus","star","slash","openPar", "closePar","openCurlyBracket","closeCurlyBracket",
            "openSquareBracket", "closeSquareBracket","rw_and", "rw_then", "rw_if","rw_else", "rw_for", "rw_class",
            "rw_or","rw_not","rw_int", "rw_float", "rw_get", "rw_put","rw_program","rw_return",
            "operator_greaterEqual", "operator_lessEqual","operator_equal","angleBrackets","comment", "inlineComment"
    };
    char[] noneZeros = {'1','2','3','4','5','6','7','8','9'};
    String[] nameOfNumbers = {"num_integer", "num_float"};
    char[] letters = {'b','j','k','q','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N',
            'O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    char[] undefinedLetters = {'~','`','!','@','#','$','%','^','&','?','|'};
    int length = reservedWords.length;

    @Before
    public void Before(){
            la.numOfLine = 1;
        }

    @Test
    public void testLetters(){
        String line = "";
        String ans = "";
        for(int i = 0; i < letters.length; i++)
            line = line + letters[i];
            ans = la.extractTokens(line);
            assertEquals("id" + ": " + line + ": " + 1 + "\n", ans);
        }

        @Test
        public void testReservedChars(){
            String line = "";
            String ans = "";
            String exp = "";
            for(int i = 0; i < reservedWords.length; i++) {
                line = line + reservedWords[i];
            }
            for(int i = 0; i < nameOfReservedWords.length; i++){
                exp += nameOfReservedWords[i] + ": " + reservedWords[i].replace(" ","") + ": " + 1 + "\n";
            }
            ans += la.extractTokens(line);
            assertEquals(exp, ans);
        }

        /*
            Simulate the case when each pair of reserved words exit continuously.
         */
        @Test
        public void testCombinationOfReservedChars(){
            String line = "";
            String ans = "";
            String exp = "";
            int lineNum = 1;
            for(int i = 0; i < reservedWords.length-1; i++) {
                for(int j = 0; j < reservedWords.length; j++){
                    if(i == 2) {
                        if(j != 2 && j != 3 && j != 4 && j != 5 && j != length - 3 && j != length - 4 && j != length - 5 && j != length - 6)
                            line = line + reservedWords[i] + reservedWords[j];
                    }
                }
                for(int k = 0; k < nameOfReservedWords.length; k++){
                    if(i == 2) {
                        if (k != 2 && k != 3 && k != 4 && k != 5 && k != length - 3 && k != length - 4 && k != length - 5 && k != length - 6) {
                            exp += nameOfReservedWords[i] + ": " + reservedWords[i].replace(" ", "") + ": " + lineNum + "\n";
                            exp += nameOfReservedWords[k] + ": " + reservedWords[k].replace(" ", "") + ": " + lineNum + "\n";
                        }
                    }
                }
                ans += la.extractTokens(line);
                assertEquals(exp, ans);
                lineNum++;
                line = "";
                ans = "";
                exp = "";
            }
        }

        @Test
        public void testComparisonOperators(){
            String co1 = "===";
            String co2 = "====";
            String co3 = ">>=<<=";
            String co4 = ">>==<<==";
            String co5 = ">>=<=";
            String co6 = "<>====><";
            String co7 = "><><=";
            String ans = "";
            String exp = "operator_equal: " + "==" + ": 1\n" +
                    "assign: " + "=" + ": 1\n"+
                    "operator_equal: " + "==" + ": 2\n" +
                    "operator_equal: " + "==" + ": 2\n" +
                    "operator_greaterThan: " + ">" + ": 3\n" +
                    "operator_greaterEqual: " + ">=" + ": 3\n" +
                    "operator_lessThan: " + "<" + ": 3\n" +
                    "operator_lessEqual: " + "<=" + ": 3\n" +
                    "operator_greaterThan: " + ">" + ": 4\n" +
                    "operator_greaterEqual: " + ">=" + ": 4\n" +
                    "assign: " + "=" + ": 4\n"+
                    "operator_lessThan: " + "<" + ": 4\n" +
                    "operator_lessEqual: " + "<=" + ": 4\n" +
                    "assign: " + "=" + ": 4\n"+
                    "operator_greaterThan: " + ">" + ": 5\n" +
                    "operator_greaterEqual: " + ">=" + ": 5\n" +
                    "operator_lessEqual: " + "<=" + ": 5\n" +
                    "angleBrackets: " + "<>" + ": 6\n" +
                    "operator_equal: " + "==" + ": 6\n" +
                    "operator_equal: " + "==" + ": 6\n" +
                    "operator_greaterThan: " + ">" + ": 6\n" +
                    "operator_lessThan: " + "<" + ": 6\n" +
                    "operator_greaterThan: " + ">" + ": 7\n" +
                    "angleBrackets: " + "<>" + ": 7\n" +
                    "operator_lessEqual: " + "<=" + ": 7\n";

            ans += la.extractTokens(co1);
            ans += la.extractTokens(co2);
            ans += la.extractTokens(co3);
            ans += la.extractTokens(co4);
            ans += la.extractTokens(co5);
            ans += la.extractTokens(co6);
            ans += la.extractTokens(co7);
            assertEquals(exp, ans);
        }

        @Test
        public void testCaseSensitivity(){
            String[] fakeReservedWords1 = new String[14];
            String[] fakeReservedWords2 = new String[14];
            String[] fakeReservedWords3 = new String[14];
            int lineNum = 1;
            for(int i = 0; i < 14; i++) {
                String word = reservedWords[i+17];
                String word1 = word.toUpperCase();
                char c1 = word.charAt(0);
                char c2 = word.charAt(word.length()-1);
                fakeReservedWords1[i] = word1;
                fakeReservedWords2[i] = word.charAt(0) + word1.substring(1, word1.length());
            }
            for(int i = 0; i < fakeReservedWords1.length; i++){
                String ans = la.extractTokens(fakeReservedWords1[i]);
                assertEquals("id: " + fakeReservedWords1[i].replace(" ","") + ": " + lineNum + "\n",ans);
                lineNum++;
                ans = la.extractTokens(fakeReservedWords2[i]);
                assertEquals("id: " + fakeReservedWords2[i].replace(" ","") + ": " + lineNum + "\n",ans);
                lineNum++;
            }

        }

        @Test
        public void testComment(){
            String comment1 = "/* sdfjsi1212--23[][][{{}///sd,.,.<>>>>==+=====__and;or@#@##@#!^&$~`~~";
            String comment2 = "  ";
            String comment3 = "";
            String comment4 = "if then else program";
            String comment5 = "// @@ 0.14552abc220-00789";
            String comment6 = "        */";
            String ans = "";
            String exp = "------: " + comment1 + ": 1"
                    + "\n------: " + comment2 + ": 2" + "\n------: " + comment4 + ": 4" +
                    "\n------: " + comment5 +": 5\n" +
                    nameOfReservedWords[length - 2] + ": " + comment6 + ": 6\n";

            ans += la.extractTokens(comment1);
            ans += la.extractTokens(comment2);
            ans += la.extractTokens(comment3);
            ans += la.extractTokens(comment4);
            ans += la.extractTokens(comment5);
            ans += la.extractTokens(comment6);
            assertEquals(exp, ans);
        }

        @Test
        public void testMultipleComments(){
            String cmt1 = "/* abcabcand/*";
            String cmt2 = "/**/return*/";
            String cmt3 = "not/*coco";
            String cmt4 = "if;*/ok";
            String cmt5 = "lotstodo*/";
            String cmt6 = "/*_;^not*/*/";
            String cmt7 = "/*sds/**/";
            String cmt8 = "and,if";

            String ans = "";
            String exp = "------: " + cmt1 + ": 1\n" +
                    "------: " + cmt2 + ": 2\n" +
                    "------: " + cmt3 + ": 3\n" +
                    "------: " + cmt4 + ": 4\n" +
                    nameOfReservedWords[length - 2] + ": " + cmt5 + ": 5\n";
            ans += la.extractTokens(cmt1);
            ans += la.extractTokens(cmt2);
            ans += la.extractTokens(cmt3);
            ans += la.extractTokens(cmt4);
            ans += la.extractTokens(cmt5);
            assertEquals(exp, ans);

            ans = "";
            exp = nameOfReservedWords[length - 2] + ": " + cmt6.substring(0, 10) + ": 6\n" +
                    "star: " + "*" + ": 6\n" +
                    "slash: " + "/" + ": 6\n" +
                    "------: " + cmt7 + ": 7\n" +
                    "------: " + cmt8 + ": 8\n";
            ans += la.extractTokens(cmt6);
            ans += la.extractTokens(cmt7);
            ans += la.extractTokens(cmt8);
            assertEquals(exp, ans);
        }

        @Test
        public void testInlineComment(){
            String ic1 = "// 1.20 == then return program";
            String ic2 = ".";
            String ic3 = "1.20//then put int<>25.33//0033";
            String ic4 = "and//not or/*sdsd22";
            String ic5 = "_";
            String ic6 = "//!@#$%^&*program_";
            String ans = "";
            String exp = nameOfReservedWords[length - 1] + ": " + ic1 + ": 1\n" +
                    nameOfReservedWords[0] + ": " + ic2 + ": 2\n" +
                    nameOfNumbers[1] + ": " + ic3.substring(0,4) + ": 3\n" +
                    nameOfReservedWords[length - 1] + ": " + ic3.substring(4, ic3.length()) + ": 3\n" +
                    nameOfReservedWords[17] + ": " + ic4.substring(0,3) + ": 4\n" +
                    nameOfReservedWords[length - 1] + ": " + ic4.substring(3, ic4.length()) + ": 4\n" +
                    nameOfReservedWords[1] + ": " + ic5 + ": 5\n" +
                    nameOfReservedWords[length - 1] + ": " + ic6 + ": 6\n";
            ans += la.extractTokens(ic1);
            ans += la.extractTokens(ic2);
            ans += la.extractTokens(ic3);
            ans += la.extractTokens(ic4);
            ans += la.extractTokens(ic5);
            ans += la.extractTokens(ic6);
            assertEquals(exp, ans);

        }

        @Test
        public void testNumbers(){
            String int1 = "111111111";
            String int2 = "0011";
            String int3 = "2222222222222222222";
            String int4 = "0011.and";
            String int5 = "0120.000000";
            String int6 = "120.001.25";
            String int7 = "555..002";
            String int8 = "10+(25-33)*99.02/87";
            String int9 = "then011.5500";

            String ans = "";
            String exp = nameOfNumbers[0] + ": " + int1 + ": 1\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 2\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 2\n" +
                    nameOfNumbers[0] + ": " + 11 + ": 2\n" +
                    nameOfNumbers[0] + ": " + int3 + ": 3\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 4\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 4\n" +
                    nameOfNumbers[0] + ": " + 11 + ": 4\n" +
                    nameOfReservedWords[0] + ": " + "." + ": 4\n" +
                    nameOfReservedWords[17] + ": " + "and" + ": 4\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 5\n" +
                    nameOfNumbers[1] + ": " + "120.000000" + ": 5\n" +
                    nameOfNumbers[1] + ": " + "120.001" + ": 6\n" +
                    nameOfReservedWords[0] + ": " + "." + ": 6\n" +
                    nameOfNumbers[0] + ": " + 25 + ": 6\n" +
                    nameOfNumbers[0] + ": " + 555 + ": 7\n" +
                    nameOfReservedWords[0] + ": " + "." + ": 7\n" +
                    nameOfReservedWords[0] + ": " + "." + ": 7\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 7\n" +
                    nameOfNumbers[0] + ": " + 0 + ": 7\n" +
                    nameOfNumbers[0] + ": " + 2 + ": 7\n" +
                    nameOfNumbers[0] + ": " + 10 + ": 8\n" +
                    nameOfReservedWords[7] + ": " + "+" + ": 8\n" +
                    nameOfReservedWords[11] + ": " + "(" + ": 8\n" +
                    nameOfNumbers[0] + ": " + 25 + ": 8\n" +
                    nameOfReservedWords[8] + ": " + "-" + ": 8\n" +
                    nameOfNumbers[0] + ": " + 33 + ": 8\n" +
                    nameOfReservedWords[12] + ": " + ")" + ": 8\n" +
                    nameOfReservedWords[9] + ": " + "*" + ": 8\n" +
                    nameOfNumbers[1] + ": " + 99.02 + ": 8\n" +
                    nameOfReservedWords[10] + ": " + "/" + ": 8\n" +
                    nameOfNumbers[0] + ": " + 87 + ": 8\n" +
                    "id" + ": " + "then011" + ": 9\n" +
                    nameOfReservedWords[0] + ": " + "." + ": 9\n" +
                    nameOfNumbers[0] + ": " + 5500 + ": 9\n";

            ans += la.extractTokens(int1);
            ans += la.extractTokens(int2);
            ans += la.extractTokens(int3);
            ans += la.extractTokens(int4);
            ans += la.extractTokens(int5);
            ans += la.extractTokens(int6);
            ans += la.extractTokens(int7);
            ans += la.extractTokens(int8);
            ans += la.extractTokens(int9);
            assertEquals(exp, ans);
        }
}
