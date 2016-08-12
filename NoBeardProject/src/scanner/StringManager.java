/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scanner;

import io.SourceReader;
import error.ErrorHandler;
import error.Error;

/**
 * NoBeard string manager assists the scanner to detect strings.
 * @author peter
 */
public class StringManager {
    
    private final int MAXSTRING = 2048;
    
    private final char[] stringStorage;
    private int firstFree;
    
    private int stringAddress;
    private int stringLength;
    
    private final SourceReader srcReader;
    private final ErrorHandler errorHandler;
    
    public StringManager(SourceReader srcReader, ErrorHandler errorHandler) {
        stringStorage = new char[MAXSTRING];
        this.srcReader = srcReader;
        this.errorHandler = errorHandler;
    }
    
    public char getCharAt(int addr) {
        return stringStorage[addr];
    }
    
    /**
     * readString is called if and only if the SourceReader.getCurrentChar()
 returns an apostrophe (') or a quote ("). readString() scans the string
 beginning with this character, stores it into the string storage.
 If the string is invalid (e.g., it doesn't end on the same line as it
 begins) or if a string storage overflow is encountered, a lexical error
 is reported.
 After a call of readString() SourceReader.getCurrentChar() returns the
 first character in the source code that follows the string's closing
 apostrophe or quote.
     */
    void readString() {
        int stringStart = srcReader.getCurrentChar();
        int strLen = 0;
        
        srcReader.nextChar();
        while (srcReader.getCurrentChar() != -1 && srcReader.getCurrentChar() != '\n' &&
                srcReader.getCurrentChar() != stringStart) {
            stringStorage[firstFree + strLen] = (char) srcReader.getCurrentChar();
            srcReader.nextChar();
            strLen++;
        }
        
        if (srcReader.getCurrentChar() == -1 || srcReader.getCurrentChar() == '\n') {
            errorHandler.throwInvalidString();
            stringAddress = 0;
            stringLength = 0;
        }
        else {
            stringAddress = firstFree;
            stringLength = strLen;
            firstFree += strLen;
        }
        srcReader.nextChar();
    }

    /**
     * 
     * @return The address of the last recently detected string.
     */
    public int getStringAddress() {
        return stringAddress;
    }

    /**
     * 
     * @return The length of the last recently detected string.
     */
    public int getStringLength() {
        return stringLength;
    }

    public byte[] getStringStorage() {
        char[] rv = new char[firstFree];
        System.arraycopy(stringStorage, 0, rv, 0, rv.length);
        return new String(rv).getBytes();
    }
    
}
