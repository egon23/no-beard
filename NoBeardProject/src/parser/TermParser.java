/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import error.ErrorHandler;
import error.Error;
import nbm.CodeGenerator;
import nbm.Nbm.Opcode;
import scanner.Scanner;
import scanner.Scanner.Symbol;
import symlist.Operand;
import symlist.Operand.OperandType;
import symlist.SymListManager;

/**
 *
 * @author peter
 */
public class TermParser extends Parser {

    private enum MulopType {

        NOMUL, TIMES, DIV, MOD
    }
    private MulopType mulOperator = MulopType.NOMUL;
    private Operand op;
    private int andChain;

    public TermParser(Scanner s, SymListManager sym, CodeGenerator c, ErrorHandler e) {
        super(s, sym, c, e);
    }

    public TermParser() {

    }

    @Override
    public void parseSpecificPart() {
        FactorParser factorParser = ParserFactory.create(FactorParser.class);
        parseSymbol(factorParser);
        sem(() -> op = factorParser.getOperand());

        while (currentTokenIsAMulOp()) {
            parseMulOp();
            parseSymbol(factorParser);
        }
    }

    private boolean currentTokenIsAMulOp() {
        Symbol sy = scanner.getCurrentToken().getSy();
        return (sy == Symbol.TIMES || sy == Symbol.DIV || sy == Symbol.MOD || sy == Symbol.AND);
    }

    private boolean parseMulOp() {
        switch (scanner.getCurrentToken().getSy()) {
            case TIMES:
                mulOperator = MulopType.TIMES;
                scanner.nextToken();
                break;

            case DIV:
                mulOperator = MulopType.DIV;
                scanner.nextToken();
                break;

            case MOD:
                mulOperator = MulopType.MOD;
                scanner.nextToken();
                break;
                
            case AND:
                scanner.nextToken();
                break;
        }
        return true;
    }

    @Override
    public boolean parseOldStyle() {
        // sem
        andChain = 0;
        // endsem

        FactorParser factP = new FactorParser(scanner, sym, code, getErrorHandler());
        if (!factP.parseOldStyle()) {
            return false;
        }
        op = factP.getOperand();

        while (currentTokenIsAMulOp()) {
            if (tokenIsA(Symbol.AND)) {
                // cc
                if (!operandIsA(op, OperandType.SIMPLEBOOL)) {
                    return false;
                }
                // endcc

                // sem
                op.emitLoadVal(code);
                code.emitOp(Opcode.FJMP);
                code.emitHalfWord(andChain);
                andChain = code.getPc() - 2;
                // endsem

                if (!factP.parseOldStyle()) {
                    return false;
                }
                Operand op2 = factP.getOperand();

                // cc
                if (!operandIsA(op2, OperandType.SIMPLEBOOL)) {
                    return false;
                }
                // endcc

                // sem
                op = op2.emitLoadVal(code);
                // ensem
            } else {
                if (!parseMulOp()) {
                    return false;
                }

                //cc
                if (op.getType() != OperandType.SIMPLEINT) {
                    getErrorHandler().raise(new Error(Error.ErrorType.TYPES_EXPECTED, OperandType.SIMPLEINT.toString()));
                    return false;
                }
                // endcc

                // sem
                op.emitLoadVal(code);
                // endsem
                if (!factP.parseOldStyle()) {
                    return false;
                }

                Operand op2 = factP.getOperand();

                // sem
                op = op2.emitLoadVal(code);

                switch (mulOperator) {
                    case TIMES:
                        code.emitOp(Opcode.MUL);
                        break;

                    case DIV:
                        code.emitOp(Opcode.DIV);
                        break;

                    case MOD:
                        code.emitOp(Opcode.MOD);
                        break;

                    default:
                        return false;
                }
                // endsem
            }
        }
        // sem
        if (andChain != 0) {
            code.emitOp(Opcode.JMP);
            code.emitHalfWord(code.getPc() + 5);
            while (andChain != 0) {
                int next = code.getCodeHalfWord(andChain);
                code.fixup(andChain, code.getPc());
                andChain = next;
            }
            code.emitOp(Opcode.LIT);
            code.emitHalfWord(0);
        }
        // endsem
        return true;
    }

    public Operand getOperand() {
        return op;
    }
}
