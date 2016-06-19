/*
 * Copyright ©2015. Created by P. Bauer (p.bauer@htl-leonding.ac.at), Department
 * of Informatics and Media Technique, HTBLA Leonding, Limesstr. 12 - 14,
 * 4060 Leonding, AUSTRIA. All Rights Reserved. Permission to use, copy, modify,
 * and distribute this software and its documentation for educational,
 * research, and not-for-profit purposes, without fee and without a signed
 * licensing agreement, is hereby granted, provided that the above copyright
 * notice, this paragraph and the following two paragraphs appear in all
 * copies, modifications, and distributions. Contact the Head of Informatics,
 * Media Technique and Design, HTBLA Leonding, Limesstr. 12 - 14, 4060 Leonding,
 * Austria, for commercial licensing opportunities.
 * 
 * IN NO EVENT SHALL HTBLA LEONDING BE  LIABLE TO ANY PARTY FOR DIRECT,
 * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST
 * PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION,
 * EVEN IF HTBLA LEONDING HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * HTBLA LEONDING SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 * PROVIDED HEREUNDER IS PROVIDED "AS IS". HTBLA LEONDING HAS NO OBLIGATION
 * TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package parser;

import symlist.Operand;

/**
 *
 * @author P. Bauer (p.bauer@htl-leonding.ac.at)
 */
public abstract class SimpleExpressionRelatedParser extends Parser {

    protected Operand exportedOperand;
    protected Operand op2;

    public final Operand getOperand() {
        return exportedOperand;
    }

    protected void checkOperandForBeing(final Operand op, final Operand.OperandType requestedType, String usedOperator) {
        where(op != null && op.getType() == requestedType,
                () -> getErrorHandler().throwOperatorOperandTypeMismatch(usedOperator, requestedType.toString()));
    }

    protected void fetchOperand(SimpleExpressionRelatedParser factorParser) {
        sem(() -> op2 = factorParser.getOperand());
    }

    protected final void emitCodeForLoadingValue() {
        sem(() -> exportedOperand = op2.emitLoadVal(code));
    }
}
