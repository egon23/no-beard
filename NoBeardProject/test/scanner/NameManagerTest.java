/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scanner;

import scanner.Scanner.Symbol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class NameManagerTest {

    private SrcReader sr;
    private SrcReader srMany;
    private SrcReader srKeywords;
    private Token token;
    private NameManager nameManager;
    private NameManager nameManagerMany;
    private NameManager nmKeywords;

    public NameManagerTest() {
    }

    @Before
    public void setUp() {
        sr = new SrcStringReader("var1; var2;");
        srMany = new SrcStringReader("var1; var2; bla; blu; var2; blu;");
        srKeywords = new SrcStringReader("put, putln, unit, do, done, if, else, int, bool, char, true, false,");
        token = new Token();
        nameManager = new NameManager(sr);
        nameManagerMany = new NameManager(srMany);
        nmKeywords = new NameManager(srKeywords);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readName method, of class NameManager.
     */
    @Test
    public void testReadName() {
        sr.nextChar();
        nameManager.readName(token);

        assertEquals(Symbol.IDENTIFIER, token.getSymbol());
        assertEquals(0, token.getValue());
        assertEquals("var1", token.getClearName());
        assertEquals("Current char ", ';', sr.getCurrentChar());

        sr.nextChar();
        sr.nextChar();

        nameManager.readName(token);
        assertEquals("IDENT ", Symbol.IDENTIFIER, token.getSymbol());
        assertEquals("Spix ", 1, token.getValue());
        assertEquals("var2", token.getClearName());
        assertEquals("Current char ", ';', sr.getCurrentChar());
    }

    @Test
    public void testReadNameDouble() {
        System.out.println("testReadNameDouble");

        srMany.nextChar();
        nameManagerMany.readName(token); // var1
        srMany.nextChar();
        srMany.nextChar();

        nameManagerMany.readName(token); // var2
        int var2Spix = token.getValue();
        srMany.nextChar();
        srMany.nextChar();

        nameManagerMany.readName(token); // bla
        srMany.nextChar();
        srMany.nextChar();

        nameManagerMany.readName(token); // blu
        int bluSpix = token.getValue();
        srMany.nextChar();
        srMany.nextChar();

        nameManagerMany.readName(token); // var2
        srMany.nextChar();
        srMany.nextChar();
        assertEquals(var2Spix, token.getValue());

        nameManagerMany.readName(token); // blu
        srMany.nextChar();
        srMany.nextChar();
        assertEquals(bluSpix, token.getValue());
    }

    /**
     * Test of getStringName method, of class NameManager.
     */
    @Test
    public void testGetStringName() {
        System.out.println("getStringName");

        sr.nextChar();
        nameManager.readName(token);

        assertEquals("Name ", "var1", nameManager.getStringName(token.getValue()));

        sr.nextChar();
        sr.nextChar();

        nameManager.readName(token);

        assertEquals("Name ", "var2", nameManager.getStringName(token.getValue()));
    }

    @Test
    public void testKeywords() {
        System.out.println("testKeywords");

        srKeywords.nextChar();
        nmKeywords.readName(token);

        Symbol[] expTokens = {Symbol.PUT, Symbol.PUTLN, Symbol.UNIT, Symbol.DO,
            Symbol.DONE, Symbol.IF, Symbol.ELSE, Symbol.INT, Symbol.BOOL,
            Symbol.CHAR, Symbol.TRUE, Symbol.FALSE};

        for (Symbol s : expTokens) {
            assertEquals(s, token.getSymbol());
            assertEquals(',', srKeywords.getCurrentChar());
            srKeywords.nextChar();
            srKeywords.nextChar();
            nmKeywords.readName(token);
        }

    }
}
