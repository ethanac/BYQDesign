package Scanner;

import java.util.Scanner;

/**
 * Created by Hao on 2017-01-28.
 */
public class Main {
    public static void main(String[] args){
        //LexicalAnalyzer la = new LexicalAnalyzer();
        Parser parser = new Parser();

        Scanner sc =new Scanner(System.in);
        System.out.println("------Welcome to Parser!------");
        while(true) {
            System.out.println("Please select how to output(enter 1 or 2):\n 1. Print to screen.\n 2. Output to a file.");
            int userChoice = sc.nextInt();
            if (userChoice == 1) {
                parser.toFile = false;
                break;
            }
            else if (userChoice == 2) {
                parser.toFile = true;
                break;
            }
        }

//        la.extractTokens();
//        String t = "!";
//        try {
//            while (!t.equals("$"))
//                System.out.println(t = la.getToken());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        System.out.println("Scanning is finished.");

        parser.parse();
        parser.out.close();
        sc.close();
    }

}
