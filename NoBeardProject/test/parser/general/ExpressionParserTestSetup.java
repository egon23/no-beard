/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser.general;

import parser.ExpressionParser;
import parser.ParserFactory;
import symlist.SymListManager;

/**
 *
 * @author peter
 */
public class ExpressionParserTestSetup extends ParserTestSetup {
    
    public static ExpressionParser getSimpleRel() {
        return setupTestObjects("a < b");
    }
        
    private static ExpressionParser setupTestObjects(String srcLine) {
        setupInfraStructure(srcLine);
        fillSymList(SymListManager.ElementType.INT);
        return ParserFactory.create(ExpressionParser.class);
    }
    
    public static ExpressionParser getAndRel() {
        return setupTestObjects("(a <= b) && (b == 1)");
    }
    
    public static ExpressionParser getOrRel() {
        return setupTestObjects("(a != b) || (b >= 1)");
    }
    
    public static ExpressionParser getAndOrRel() {
        return setupTestObjects("((a < b) && (b > 1)) || (c < 0)");
    }
}