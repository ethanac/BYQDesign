package Scanner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ethan on 2017-02-25.
 */
public class FirstNFollow {
    public static String[][] FIRST= {
            {"class", "program"},
            {"(", "id", "integer", "nfloat", "not", "+", "-"},
            {"#", "*", "/", "and"},
            {"(", "id", "integer", "nfloat", "not", "+", "-"},
            {"id"},
            {"id"},
            {"#", "id"},
            {"#", "(", "id", "integer", "nfloat", "(", "not", "+", "-"},
            {"["},
            {"#", "["},
            {"(", "id", "integer", "nfloat", "not", "+", "-"},
            {","},
            {"#", ","},
            {"(", "id", "integer", "nfloat", "not", "+", "-"},
            {"{", "#", "id"},
            {"int", "float", "id"},
            {"int", "float", "id"},
            {","},
            {"#", ","},
            {"(", "id", "integer", "nfloat", "not", "+", "-"},
            {"#", "+", "-", "or"},
            {"id", "integer", "nfloat"},
            {"#", "id", "integer", "nfloat"},
            {"id", "if", "for", "get", "put", "return"},
            {"#", "id", "if", "for", "get", "put", "return"},
            {"int", "float", "id"},
            {"#", "int", "float", "id"},
            {"{"},
            {"int", "float", "id"},
            {"#", "int", "float", "id"},
            {"program"},
            {"class"},
            {"#", "class"},

            {"==", "<=", ">=", "<", ">", "<>"},
            {"int", "float", "id"},
            {"["},
            {"#", "["}
    };
    public static String[][] FOLLOW = {
            {"$"},
            {"$", "#", "+", "-", "or",")","}","==","<=",">=","<",">","<>"},
            {"$", "#", "+", "-", "or",";","}","]",")","==","<=",">=","<",">","<>"},
            {"$", "#", "+", "-", "*", "/", "and",")","}","==","<=",">=","<",">","<>"},
            {"$", "#", "+", "-", "*", "/", "and",")","}","=","==","<=",">=","<",">","<>"},
            {"$","id"},
            {"$","id"},
            {"$", ")"},
            {"$", ".", "#", "+", "-", "*", "/", "and",";","}",")","]","=","==","<=",">=","<",">","<>"},
            {"$", ".", "#", "+", "-", "*", "/", "and",";","}","]",")","=","==","<=",">=","<",">","<>"},
            {"$", ";", ")"},
            {"$", ")"},
            {"$", ")"},
            {"$", ";"},
            {"$", ";"},
            {"$", "{"},
            {"$", ")"},
            {"$", ")"},
            {"$", ")"},
            {"$", ")", "]", ";","}","+", "-","==","<=",">=","<",">","<>"},
            {"$", ")", "]", ";","}","+", "-","==","<=",">=","<",">","<>"},
            {"$", ";"},
            {"$", "}"},
            {"$", "}"},
            {"$", "}"},
            {"$", "int", "float", "id", "if", "for", "}"},
            {"$", "int", "float", "id", "if", "for", "}"},
            {"$",";"},
            {"$","}"},
            {"$","}"},
            {"$"},
            {"$","program"},
            {"$","program"},

            {"$","(", "id", "integer","nfloat", "not", "+", "-"},
            {"$","id"},
            {"$"},
            {"$", ";", ",", ")"}
    };

    public String[] symbols = {
            "E","T1","T1p","T2","T3","T4","T4p","T5","T6","T6p",
            "T7","T8","T8p","T9","T10","T11","T12","T13","T13p","T14",
            "T14p", "T15","T15p", "T16", "T16p", "T17","T17p","T18","T19","T19p",
            "T20","T21","T21p","F1","F2","F3","F3p"
    };

    public static HashMap<String, Integer> ORDER = new HashMap<String, Integer>();
    public FirstNFollow(){
        int order = 0;
        for(String s : symbols){
            ORDER.put(s, order);
            order++;
        }
    }
}
