package Scanner;

import java.util.Scanner;

/**
 * Created by Hao on 2017-01-28.
 */
public class Main {
    public static void main(String[] args){
        LexicalAnalyzer la = new LexicalAnalyzer();
        Scanner sc =new Scanner(System.in);
        System.out.println("------Welcome to Lexical Analyzer!------");
        while(true) {
            System.out.println("Please select how to output(enter 1 or 2):\n 1. Print to screen.\n 2. Output to a file.");
            int userChoice = sc.nextInt();
            if (userChoice == 1) {
                la.writeToFile = false;
                break;
            }
            else if (userChoice == 2) {
                la.writeToFile = true;
                break;
            }
        }
        la.extractTokens();
        sc.close();
        System.out.println("Scanning is finished.");
    }

}
