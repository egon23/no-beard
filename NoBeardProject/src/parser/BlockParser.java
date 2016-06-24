/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import error.ErrorHandler;
import nbm.CodeGenerator;
import nbm.Nbm.Opcode;
import scanner.Scanner;
import scanner.Scanner.Symbol;
import symlist.SymListEntry;
import symlist.SymListManager;

/**
 *
 * @author peter
 */
public class BlockParser extends Parser {

    private final SymListEntry obj;

    public BlockParser(Scanner scanner, SymListManager sym, CodeGenerator code, SymListEntry obj, ErrorHandler e) {
        super(scanner, sym, code, e);
        this.obj = obj;
    }

    @Override
    public boolean parseOldStyle() {
        if (!tokenIsA(Symbol.DO)) {
            return false;
        }

        // sem
        sym.defineFuncStart(obj, code.getPc());
        int incAddr = code.getPc() + 1;
        code.emitOp(Opcode.INC);
        code.emitHalfWord(0);  // tmp address will be fixed later
        // endsem

        if (!statSeq()) {
            return false;
        }

        // sem
        sym.fixINC(incAddr, obj);
        // endsem

        if (!tokenIsA(Symbol.DONE)) {
            return false;
        }
        return true;
    }

    private boolean statSeq() {
        StatParser statP = new StatParser(scanner, sym, code, getErrorHandler());
        if (!statP.parseOldStyle()) {
            return false;
        }

        while (scanner.getCurrentToken().getSy() != Symbol.DONE) {
            if (!statP.parseOldStyle()) {
                return false;
            }
        }
        return true;
    }

    private boolean isNamedBlock() {
        return (obj.isNamedBlockEntry());
    }

    @Override
    protected void parseSpecificPart() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
