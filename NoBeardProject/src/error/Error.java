/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package error;

/**
 *
 * @author peter
 */
public class Error {

    public enum ErrorClass {

        LEXICAL, SYNTAX, SEMANTICAL
    }

    public enum ErrorType {

        INTEGER_OVERFLOW(1, ErrorClass.LEXICAL, "Integer overflow"),
        INVALID_STRING(2, ErrorClass.LEXICAL, "Non terminated string constant"),
        SYMBOL_EXPECTED(21, ErrorClass.SYNTAX, "%s0 expected but found %s1"),
        IDENTIFIER_EXPECTED(22, ErrorClass.SYNTAX, "Identifier expected"),
        GENERAL_SYN_ERROR(49, ErrorClass.SYNTAX, "General syntax error: %s"),
        // TODO: following message should be replaced by "Block %s1 can't can't end with name %s2"
        BLOCK_NAME_MISSMATCH(50, ErrorClass.SEMANTICAL, "Block name start differs from block name end: %s"),
        OPERAND_KIND_EXPECTED(52, ErrorClass.SEMANTICAL, "%s expected"),
        // TODO: following message should be replaced by "%s can't be converted to %s"
        INCOMPATIBLE_TYPES(53, ErrorClass.SEMANTICAL, "The following types are not compatible: %s"),
        NAME_ALREADY_DEFINED(54, ErrorClass.SEMANTICAL, "Identifier %s already defined"),
        TYPES_EXPECTED(55, ErrorClass.SEMANTICAL, "Types %l expected"),
        POSITIVE_ARRAY_SIZE_EXPECTED(56, ErrorClass.SEMANTICAL, "Array size spezifier has to be a positive integer."),
        NO_NESTED_MODULES(57, ErrorClass.SEMANTICAL, "Nested modules are not allowed"),
        OPERATOR_OPERAND_TYPE_MISMATCH(58, ErrorClass.SEMANTICAL, "Operator %s1 requires a %s2 operand"),
        GENERAL_SEM_ERROR(99, ErrorClass.SEMANTICAL, "General sem error: %s");

        private final int errorNumber;
        private final ErrorClass errorClass;
        private final String errorMessage;

        ErrorType(int errorNumber, ErrorClass errorClass, String errorMessage) {
            this.errorNumber = errorNumber;
            this.errorClass = errorClass;
            this.errorMessage = errorMessage;
        }

        public Error.ErrorClass getErrorClass() {
            return errorClass;
        }

        public int getNumber() {
            return errorNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private final ErrorType errorType;

    private int lineNumber;
    private String[] parameters;

    /**
     * Instantiates a simple error with no extra information.
     * This constructor can only be used with ErrorTypes providing no %s
     * format string.
     * @param errorType Type of error to be instantiated.
     */
    public Error(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Instantiates an error with one additional information parameter.
     * This constructor of error can only be used with ErrorTypes providing one
     * %s format string.
     * @param errorType Type of error to be instantiated.
     * @param parameter Additional info parameter.
     */
    public Error(ErrorType errorType, String parameter) {
        this.errorType = errorType;
        this.parameters = new String[]{parameter};
    }

    /**
     * Instantiates an error with a list of additional information parameters.
     * This constructor can only be used with ErrorTypes providing a %l format string.
     * @param errorType Type of error to be instantiated.
     * @param parameters List of additional info parameters
     */
    public Error(ErrorType errorType, String[] parameters) {
        this.errorType = errorType;
        this.parameters = parameters;
    }

    /**
     * Instantiates an error with more than one additional information parameters.
     * This constructor can only be used with ErrorTypes providing more than
     * one %s format strings.
     * @param errorType Type of error to be instantiated.
     * @param param1 First additional info parameter.
     * @param params Further info parameters.
     */
    public Error(ErrorType errorType, String param1, String... params) {
        this.errorType = errorType;
        this.parameters = new String[1 + params.length];
        parameters[0] = param1;
        System.arraycopy(params, 0, parameters, 1, params.length);
    }

    void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Errors can be separated in different classes. The ErrorHandler
     * distinguishes the error classes via the string returned by
     * getErrorClass().
     *
     * @return String which uniquely identifies the error class to which the
     * error belongs.
     */
    public ErrorClass getErrorClass() {
        return errorType.getErrorClass();
    }

    /**
     * Returns the message to be printed in case of the occurrence of this
     * error.
     *
     * @return The error message.
     */
    public String getMessage() {
        String msg = errorType.getErrorMessage();
        if (msg.contains("%s")) {
            return compileErrorMessage(msg);
        } else if (msg.contains("%l")) {
            return compileErrorMessageWithList(msg);
        } else {
            return errorType.getErrorMessage();
        }
    }

    private String compileErrorMessage(String msg) {
        if (parameters.length == 1) {
            return replaceOneParameter(msg);
        } else {
            return replaceMoreParameters(msg);
        }
    }

    private String replaceOneParameter(String msg) {
        if (msg.contains("%s0")) {
            return msg.replaceAll("%s0", parameters[0]);
        } else {
            return msg.replaceAll("%s", parameters[0]);
        }
    }

    private String replaceMoreParameters(String msg) {
        for (int i = 0; i < parameters.length; i++) {
            String formatString = "%s" + i;
            msg = msg.replaceAll(formatString, parameters[i]);
        }
        return msg;
    }

    private String compileErrorMessageWithList(String msg) {
        StringBuilder sb = new StringBuilder(parameters[0]);
        compileList(sb);
        return msg.replaceAll("%l", sb.toString());
    }

    private void compileList(StringBuilder sb) {
        for (int i = 1; i < parameters.length; i++) {
            appendSeparator(i, sb);
            appendParameter(sb, i);
        }
    }

    private void appendSeparator(int i, StringBuilder sb) {
        if (i == parameters.length - 1) {
            sb.append(" or ");
        } else {
            sb.append(", ");
        }
    }

    private void appendParameter(StringBuilder sb, int i) {
        sb.append(parameters[i]);
    }

    /**
     * 
     * @return The error number.
     */
    public int getNumber() {
        return errorType.getNumber();
    }

    /**
     * 
     * @return The line number, where the error occurred.
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
