package Scanner;

import java.util.Scanner;

/**
 * Created by Hao on 2017-01-28.
 */
public class Main {
    public static void main(String[] args){
        LexicalAnalyzer la;
        Parser parser;

        Scanner sc = new Scanner(System.in);

        System.out.println("------Welcome to Parser!------");
        while(true) {
            System.out.println("Please enter the file name:");
            String userInput = sc.next();
            System.out.println("Compile file: " + userInput);
            la = new LexicalAnalyzer(userInput);
            parser = new Parser(userInput);
            System.out.println("Please select how to output(enter 1 or 2):\n 1. Print to screen.\n 2. Output to a file.");
            int userChoice = sc.nextInt();
            if (userChoice == 1) {
                parser.stg.toFile = false;
                parser.toFile = false;
                la.writeToFile = false;
                break;
            }
            else if (userChoice == 2) {
                parser.stg.toFile = true;
                parser.toFile = true;
                la.writeToFile = true;
                break;
            }
        }

//        String t = "!";
//        try {
//            while (!t.equals("$"))
//                System.out.println(t = la.getToken());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        System.out.println("Scanning is finished.");
        la.extractTokens();
        parser.parse();
        parser.out.close();
        for(String s : parser.stg.tables.keySet()) {
            parser.stg.printTable(s);
        }
        parser.stg.out.close();
        parser.semanticCheck();
        CodeGenerator cg = new CodeGenerator();
        cg.generate(parser.stg, parser.sr);
        sc.close();
    }

}