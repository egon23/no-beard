/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package error.synerr;

/**
 *
 * @author peter
 */
public class IdentifierExpected extends SynErr {
    public IdentifierExpected(int lineNumber) {
        super(20, "Identifier expected", lineNumber);
    }
}
