import Scanner.LexicalAnalyzer;
import Scanner.Parser;
import Scanner.SymbolTableGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ethan on 2017-03-16.
 */
public class SymbolTableGeneratorTest {
    Parser parser = new Parser();

    @Before
    public void Before(){
        parser.stg = new SymbolTableGenerator();
    }

    @Test
    public void testCreateCorrect(){
        assertEquals(true, parser.stg.create("Global"));
        assertEquals(true, parser.stg.create("program"));
        assertEquals(true, parser.stg.create("classA"));
        assertEquals(true, parser.stg.create("classB"));
        assertEquals(true, parser.stg.create("functionA"));
        assertEquals(true, parser.stg.create("functionB"));
    }

    @Test
    public void testCreateIncorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("functionA");
        parser.stg.create("functionB");

        assertEquals(false, parser.stg.create("Global"));
        assertEquals(false, parser.stg.create("program"));
        assertEquals(false, parser.stg.create("classA"));
        assertEquals(false, parser.stg.create("classB"));
        assertEquals(false, parser.stg.create("functionA"));
        assertEquals(false, parser.stg.create("functionB"));
    }

    @Test
    public void testInsertCorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("functionA");
        parser.stg.create("functionB");

        assertEquals(true, parser.stg.insert("Global", "program", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("Global", "classA", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("Global", "classB", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("Global", "func1", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("Global", "func2", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("program", "var1", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("program", "var2", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("classA", "funcA", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("classB", "funcB", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("classB", "funcC", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("functionA", "var3", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("functionB", "var4", new ArrayList<String>()));
        assertEquals(true, parser.stg.insert("functionB", "var5", new ArrayList<String>()));
    }

    @Test
    public void testInsertIncorrect(){
        // Table not found
        assertEquals(false, parser.stg.insert("Global", "program", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("Global", "classA", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("Global", "classB", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("Global", "func1", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("Global", "func2", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("program", "var1", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("program", "var2", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("classA", "funcA", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("classB", "funcB", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("classB", "funcC", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("functionA", "var3", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("functionB", "var4", new ArrayList<String>()));
        assertEquals(false, parser.stg.insert("functionB", "var5", new ArrayList<String>()));

        // Create tables
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("functionA");
        parser.stg.create("functionB");

        // duplicate records
        parser.stg.insert("Global", "program", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("Global", "program", new ArrayList<String>()));
        parser.stg.insert("Global", "classA", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("Global", "classA", new ArrayList<String>()));
        parser.stg.insert("Global", "classB", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("Global", "classB", new ArrayList<String>()));
        parser.stg.insert("Global", "func1", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("Global", "func1", new ArrayList<String>()));
        parser.stg.insert("program", "var1", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("program", "var1", new ArrayList<String>()));
        parser.stg.insert("classA", "funcA", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("classA", "funcA", new ArrayList<String>()));
        parser.stg.insert("classB", "funcB", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("classB", "funcB", new ArrayList<String>()));
        parser.stg.insert("functionA", "var3", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("functionA", "var3", new ArrayList<String>()));
        parser.stg.insert("functionB", "var4", new ArrayList<String>());
        assertEquals(false, parser.stg.insert("functionB", "var4", new ArrayList<String>()));
    }

    @Test
    public void testSearchDirectlyCorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("funcA");
        parser.stg.create("functionB");

        parser.stg.insert("Global", "program", new ArrayList<String>());
        parser.stg.insert("Global", "var", new ArrayList<String>());
        parser.stg.insert("Global", "classA", new ArrayList<String>());
        parser.stg.insert("Global", "classB", new ArrayList<String>());
        parser.stg.insert("Global", "func1", new ArrayList<String>());
        parser.stg.insert("Global", "func2", new ArrayList<String>());
        parser.stg.insert("program", "var1", new ArrayList<String>());
        parser.stg.insert("program", "var2", new ArrayList<String>());
        parser.stg.insert("classA", "varA", new ArrayList<String>());
        parser.stg.insert("classA", "funcA", new ArrayList<String>());
        parser.stg.insert("classB", "funcB", new ArrayList<String>());
        parser.stg.insert("classB", "funcC", new ArrayList<String>());
        parser.stg.insert("funcA", "var3", new ArrayList<String>());
        parser.stg.insert("functionB", "var4", new ArrayList<String>());
        parser.stg.insert("functionB", "var5", new ArrayList<String>());
        parser.stg.insert("func1", "var6", new ArrayList<String>());

        assertEquals(true, parser.stg.search("Global", "program"));
        assertEquals(true, parser.stg.search("Global", "var"));
        assertEquals(true, parser.stg.search("Global", "func1"));
        assertEquals(true, parser.stg.search("program", "var1"));
        assertEquals(true, parser.stg.search("classA", "varA"));
        assertEquals(true, parser.stg.search("classA", "funcA"));
        assertEquals(true, parser.stg.search("classB", "funcB"));
        assertEquals(true, parser.stg.search("funcA", "var3"));
        assertEquals(true, parser.stg.search("functionB", "var4"));
    }

    @Test
    public void testSearchRecursivelyCorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("funcA");
        parser.stg.create("func1");

        ArrayList<String> programRec = new ArrayList<>(3);
        programRec.add("pr");
        programRec.add("pr");
        programRec.add(2, "program");
        parser.stg.insert("Global", "program", programRec);
        ArrayList<String> varRec = new ArrayList<>(3);
        varRec.add(" ");
        varRec.add(" ");
        parser.stg.insert("Global", "var", varRec);
        ArrayList<String> classARec = new ArrayList<>(3);
        classARec.add("cA");
        classARec.add("cA");
        classARec.add(2, "classA");
        parser.stg.insert("Global", "classA", classARec);
        ArrayList<String> func1Rec = new ArrayList<>(3);
        func1Rec.add("f1");
        func1Rec.add("f1");
        func1Rec.add(2, "func1");
        parser.stg.insert("Global", "func1", func1Rec);

        parser.stg.insert("program", "var1", new ArrayList<String>());

        parser.stg.insert("classA", "varA", new ArrayList<String>());
        ArrayList<String> funcARec = new ArrayList<>(3);
        funcARec.add("fA");
        funcARec.add("fA");
        funcARec.add(2, "funcA");
        parser.stg.insert("classA", "funcA", funcARec);

        parser.stg.insert("funcA", "var3", new ArrayList<String>());

        parser.stg.insert("func1", "var6", new ArrayList<String>());

        assertEquals(true, parser.stg.search("Global", "var1"));
        assertEquals(true, parser.stg.search("Global", "varA"));
        assertEquals(true, parser.stg.search("Global", "var6"));
        assertEquals(true, parser.stg.search("Global", "var3"));
        assertEquals(true, parser.stg.search("program", "var1"));
        assertEquals(true, parser.stg.search("classA", "var3"));
    }

    @Test
    public void testSearchIncorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("funcA");
        parser.stg.create("functionB");

        parser.stg.insert("Global", "program", new ArrayList<String>());
        parser.stg.insert("Global", "var", new ArrayList<String>());
        parser.stg.insert("Global", "classA", new ArrayList<String>());
        parser.stg.insert("Global", "classB", new ArrayList<String>());
        parser.stg.insert("Global", "func1", new ArrayList<String>());
        parser.stg.insert("Global", "func2", new ArrayList<String>());
        parser.stg.insert("program", "var1", new ArrayList<String>());
        parser.stg.insert("program", "var2", new ArrayList<String>());
        parser.stg.insert("classA", "varA", new ArrayList<String>());
        parser.stg.insert("classA", "funcA", new ArrayList<String>());
        parser.stg.insert("classB", "funcB", new ArrayList<String>());
        parser.stg.insert("classB", "funcC", new ArrayList<String>());
        parser.stg.insert("funcA", "var3", new ArrayList<String>());
        parser.stg.insert("functionB", "var4", new ArrayList<String>());
        parser.stg.insert("functionB", "var5", new ArrayList<String>());
        parser.stg.insert("func1", "var6", new ArrayList<String>());

        assertEquals(false, parser.stg.search("Global", "dog"));
        assertEquals(false, parser.stg.search("Global", "cat"));
        assertEquals(false, parser.stg.search("program", "frog"));
        assertEquals(false, parser.stg.search("classA", "mouse"));
        assertEquals(false, parser.stg.search("classA", "horse"));
        assertEquals(false, parser.stg.search("classB", "panda"));
        assertEquals(false, parser.stg.search("funcA", "bear"));
        assertEquals(false, parser.stg.search("functionB", "rabbit"));
    }

    @Test
    public void testDeleteCorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("funcA");
        parser.stg.create("functionB");

        assertEquals(true, parser.stg.delete("functionB"));
        assertEquals(true, parser.stg.delete("funcA"));
        assertEquals(true, parser.stg.delete("classB"));
        assertEquals(true, parser.stg.delete("classA"));
        assertEquals(true, parser.stg.delete("program"));
        assertEquals(true, parser.stg.delete("Global"));
    }

    @Test
    public void testDeleteIncorrect(){
        parser.stg.create("Global");
        parser.stg.create("program");
        parser.stg.create("classA");
        parser.stg.create("classB");
        parser.stg.create("funcA");
        parser.stg.create("functionB");

        parser.stg.delete("functionB");
        assertEquals(false, parser.stg.delete("functionB"));
        parser.stg.delete("funcA");
        assertEquals(false, parser.stg.delete("funcA"));
        parser.stg.delete("classB");
        assertEquals(false, parser.stg.delete("classB"));
        parser.stg.delete("classA");
        assertEquals(false, parser.stg.delete("classA"));
        parser.stg.delete("program");
        assertEquals(false, parser.stg.delete("program"));
        parser.stg.delete("Global");
        assertEquals(false, parser.stg.delete("Global"));
    }
}
