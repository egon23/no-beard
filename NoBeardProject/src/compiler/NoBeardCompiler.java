/*
 * Copyright ©2011 - 2016. Created by P. Bauer (p.bauer@htl-leonding.ac.at),
 * Department of Informatics and Media Technique, HTBLA Leonding,
 * Limesstr. 12 - 14, 4060 Leonding, AUSTRIA. All Rights Reserved. Permission
 * to use, copy, modify, and distribute this software and its documentation
 * for educational, research, and not-for-profit purposes, without fee and
 * without a signed licensing agreement, is hereby granted, provided that the
 * above copyright notice, this paragraph and the following two paragraphs
 * appear in all copies, modifications, and distributions. Contact the Head of
 * Informatics and Media Technique, HTBLA Leonding, Limesstr. 12 - 14,
 * 4060 Leonding, Austria, for commercial licensing opportunities.
 * 
 * IN NO EVENT SHALL HTBLA LEONDING BE LIABLE TO ANY PARTY FOR DIRECT,
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
package compiler;

import error.ErrorHandler;
import java.io.FileNotFoundException;
import nbm.CodeGenerator;
import parser.NoBeardParser;
import parser.Parser;
import parser.ParserFactory;
import scanner.Scanner;
import scanner.SrcFileReader;
import scanner.SrcReader;
import scanner.SrcStringReader;
import symboltable.Operand;
import symboltable.SymbolTable;

/**
 *
 * @author peter
 */
public class NoBeardCompiler {

    private static SrcReader sourceReader;
    private static Parser parserl;
    private static boolean sourceIsAvailable;

    public static void setSourceString(String source) {
        NoBeardCompiler.sourceReader = new SrcStringReader(source);
        sourceIsAvailable = true;
        ParserFactory.setup(sourceReader);
        parserl = ParserFactory.create(NoBeardParser.class);
    }

    public static boolean compile() {
        return sourceIsAvailable && parserl.parse();
    }

    public static byte[] getByteCode() {
        return ParserFactory.getCodeGenerator().getByteCode();
    }
    
    public static byte[] getStringStore() {
        return ParserFactory.getScanner().getStringManager().getStringStorage();
    }

    public static void setSourceFile(String sourceFilePath) {
        try {
            NoBeardCompiler.sourceReader = new SrcFileReader(sourceFilePath);
            sourceIsAvailable = true;
        } catch (FileNotFoundException ex) {
            NoBeardCompiler.sourceReader = new SrcStringReader("");
            sourceIsAvailable = false;
        } finally {
            ParserFactory.setup(sourceReader);
            parserl = ParserFactory.create(NoBeardParser.class);
        }
    }

    private final Scanner scanner;
    private final NoBeardParser parser;
    private final SymbolTable sym;
    private final CodeGenerator code;
    private final ErrorHandler errorHandler;

    public NoBeardCompiler(SrcReader srcReader) {

        errorHandler = new ErrorHandler(srcReader);
        scanner = new Scanner(srcReader, errorHandler);
        code = new CodeGenerator(nbm.Nbm.getMAXPROG());
        sym = new SymbolTable(scanner, errorHandler);
        Operand.setSymListManager(sym);
        Operand.setStringManager(scanner.getStringManager());
        parser = new NoBeardParser(scanner, sym, code, errorHandler);
        scanner.nextToken();
    }

    public CodeGenerator getCode() {
        return code;
    }

    public NoBeardParser getParser() {
        return parser;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public SymbolTable getSymListManager() {
        return sym;
    }

    public byte[] getStringStorage() {
        return scanner.getStringManager().getStringStorage();
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

}
