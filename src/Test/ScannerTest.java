package Test;

import Scanner.LexicalAnalyzer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Ethan on 2017-01-29.
 */
public class ScannerTest{
    LexicalAnalyzer la = new LexicalAnalyzer();
    char[] chars = {'.','_','=','<','>',';',',','+','-','*','/','(',')','{','}','[',']','a','c','d','e','f','g','h',
            'i','l','m','n','o','p','r','s','t','u',' '};
    char[] noneZeros = {'1','2','3','4','5','6','7','8','9'};
    char[] letters = {'b','j','k','q','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N',
            'O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    char[] undefinedLetters = {'~','`','!','@','#','$','%','^','&','?','|'};

    @BeforeClass
    static void BeforeClass(){
        System.out.println("Scanner test is running.");
    }

    @AfterClass
    static void AfterClass(){
        System.out.println("Scanner test is finished.");
    }

    @Test public void testLetters(){

    }
}
