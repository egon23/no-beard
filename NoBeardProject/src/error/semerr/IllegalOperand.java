/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package error.semerr;

/**
 *
 * @author peter
 */
public class IllegalOperand extends SemErr {
    public IllegalOperand(int lineNumber) {
        super(52, "Illegal operand", lineNumber);
    }
}
