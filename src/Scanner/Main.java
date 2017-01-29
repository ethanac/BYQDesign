package Scanner;

/**
 * Created by Ethan on 2017-01-28.
 */
public class Main {
    public static void main(String[] args){
        LexicalAnalyzer la = new LexicalAnalyzer();
//        la.importStateTransitionTable();
//        la.setColumnNumber();
//        int j = 0;
//        for(int[] ia : la.table) {
//            for(int i : ia) {
//                System.out.print(i + ",");
//            }
//            System.out.println();
//        }
        la.extractTokens();

    }

}
