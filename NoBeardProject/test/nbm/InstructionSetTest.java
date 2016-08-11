/*
 * Copyright ©2016. Created by P. Bauer (p.bauer@htl-leonding.ac.at),
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
package nbm;

import error.ErrorHandler;
import nbm.InstructionSet.Instruction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author P. Bauer (p.bauer@htl-leonding.ac.at)
 */
public class InstructionSetTest {

    public InstructionSetTest() {
    }

    private InstructionSet instructionSet;
    private ControlUnit controlUnit;
    private ProgramMemory programMemory;
    private DataMemory dataMemory;
    private CallStack callStack;
    private final ErrorHandler errorHandler = new ErrorHandler(new FakeSourceCodeInfo());

    @Before
    public void setUp() {
        programMemory = new ProgramMemory(1024, errorHandler);
        dataMemory = new DataMemory(1024, errorHandler);
        callStack = new CallStack(dataMemory, 0, 28);
        controlUnit = new ControlUnit(programMemory, dataMemory, callStack, errorHandler);
        instructionSet = new InstructionSet();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetInstructionById() {
        Instruction i = instructionSet.getInstructionById(0x00);
        assertEquals(InstructionSet.Instruction.NOP, i);
        assertEquals(0x00, i.getId());
        assertEquals(1, i.getSize());
    }

    @Test
    public void testGetLit() {
        byte[] program = {
            Instruction.LIT.getId(), 1, 0
        };
        checkInstruction(program, 1, InstructionSet.Instruction.LIT, 3);
        assertEquals(256, callStack.peek());
    }

    private void checkInstruction(byte[] program, final int instructionId, final Instruction opCode, final int size) {
        programMemory.store(0, program);
        Instruction i = instructionSet.getInstructionById(instructionId);
        controlUnit.fetchInstruction();
        assertEquals(opCode, i);
        assertEquals(instructionId, i.getId());
        assertEquals(size, i.getSize());
        i.execute(controlUnit);
    }

    @Test
    public void testLa() {
        byte[] program = {
            Instruction.LA.getId(), 0, 0, 32
        };
        checkInstruction(program, 0x02, Instruction.LA, 4);
        assertEquals(32, callStack.peek());
    }

    @Test
    public void testLocalLaInNestedFrame() {
        byte[] program = {
            Instruction.LA.getId(), 0, 0, 32
        };
        setupThreeFrames();
        checkInstruction(program, 0x02, Instruction.LA, 4);
        assertEquals(108, callStack.peek());
    }

    private void setupThreeFrames() {
        callStack.push(13); // push a local int value
        saveImmediateScope();
        openNewFrame();
        callStack.push(17);
        callStack.push(42); // push some local int values
        saveImmediateScope();
        openNewFrame();
        int currentStackTop = callStack.getStackPointer();
        dataMemory.storeByte(currentStackTop + 4, (byte) '5'); // push a local char value
        callStack.setStackPointer(currentStackTop + 1);
        callStack.push(61); // and a final one
    }

    private void saveImmediateScope() {
        int immediateScope = callStack.getFramePointer();
        callStack.push(immediateScope);
    }

    private void openNewFrame() {
        int newFrameStart = callStack.getStackPointer();
        callStack.setCurrentFramePointer(newFrameStart);
    }

    @Test
    public void testNonLocalLaInNestedFrame() {
        byte[] program = {
            Instruction.LA.getId(), 1, 0, 32
        };

        setupThreeFrames();
        checkInstruction(program, 0x02, Instruction.LA, 4);
        assertEquals(68, callStack.peek());
    }

    @Test
    public void testGlobalLaInNestedFrame() {
        byte[] program = {
            Instruction.LA.getId(), 2, 0, 32
        };

        setupThreeFrames();
        checkInstruction(program, 0x02, Instruction.LA, 4);
        assertEquals(32, callStack.peek());
    }

    @Test
    public void testLv() {
        byte[] program = {
            Instruction.LV.getId(), 1, 0, 36
        };
        setupThreeFrames();
        checkInstruction(program, 0x03, Instruction.LV, 4);
        assertEquals(42, callStack.peek());
    }

    @Test
    public void testLc() {
        byte[] program = {
            Instruction.LC.getId(), 0, 0, 32
        };
        setupThreeFrames();
        checkInstruction(program, 0x04, Instruction.LC, 4);
        assertEquals('5', (char) callStack.peek());
    }

    @Test
    public void testSto() {
        byte[] program = {
            Instruction.STO.getId()
        };
        setupThreeFrames();

        callStack.push(68); // destination address
        callStack.push(513); // value to be stored
        checkInstruction(program, 0x07, Instruction.STO, 1);
        assertEquals(513, dataMemory.loadWord(68));
    }

    @Test
    public void testStc() {
        byte[] program = {
            Instruction.STC.getId()
        };
        setupThreeFrames();

        callStack.push(108); // destination address
        callStack.push(65); // ascii value of 'A'

        checkInstruction(program, 0x08, Instruction.STC, 1);
        assertEquals(65, dataMemory.loadByte(108));
        assertEquals(61, dataMemory.loadWord(109)); // check whether following value wasn't damaged
    }

    @Test
    public void testAssn() {
        byte[] program = {
            Instruction.ASSN.getId()
        };
        byte[] exampleString = {
            (byte) 'f', (byte) 'o', (byte) 'o', (byte) ' ', (byte) 'b', (byte) 'a', (byte) 'r'
        };
        int nextFreeAddress = dataMemory.storeStringConstants(exampleString);
        callStack.setCurrentFramePointer(nextFreeAddress); // frame starts after string constants
        callStack.push(17); // first local value in frame
        int dest = callStack.getStackPointer() + 4; // destination is the second value in frame
        callStack.setStackPointer(dest + 7 - 4); // reserve 7 byte on stack (reduce by 4 since stack pointer points to the first byte of the last word
        callStack.push(42); // third value in frame
        callStack.push(dest); // copy to reserved space
        callStack.push(0); // copy from start of const memory
        callStack.push(exampleString.length); // copy all characters
        checkInstruction(program, 0x0A, Instruction.ASSN, 1);
        assertEquals(17, dataMemory.loadWord(dest - 4));
        assertEquals((byte) 'f', dataMemory.loadByte(dest));
        assertEquals((byte) 'o', dataMemory.loadByte(dest + 1));
        assertEquals((byte) 'o', dataMemory.loadByte(dest + 2));
        assertEquals((byte) ' ', dataMemory.loadByte(dest + 3));
        assertEquals((byte) 'b', dataMemory.loadByte(dest + 4));
        assertEquals((byte) 'a', dataMemory.loadByte(dest + 5));
        assertEquals((byte) 'r', dataMemory.loadByte(dest + 6));
        assertEquals(42, dataMemory.loadWord(dest + 7));
    }

    @Test
    public void testNeg() {
        byte[] program = {
            Instruction.NEG.getId()
        };
        callStack.push(42);
        checkInstruction(program, 0x0B, Instruction.NEG, 1);
        assertEquals(-42, callStack.peek());
    }
    
    @Test
    public void testAdd() {
        byte[] program = {
            Instruction.ADD.getId()
        };
        callStack.push(42);
        callStack.push(-17);
        checkInstruction(program, 0x0C, Instruction.ADD, 1);
        assertEquals(25, callStack.peek());
    }
    
    @Test
    public void testAddSub() {
        byte[] program = {
            Instruction.SUB.getId()
        };
        callStack.push(42);
        callStack.push(-17);
        checkInstruction(program, 0x0D, Instruction.SUB, 1);
        assertEquals(59, callStack.peek());
    }
    
    @Test
    public void testMul() {
        byte[] program = {
            Instruction.MUL.getId()
        };
        callStack.push(42);
        callStack.push(-17);
        checkInstruction(program, 0x0E, Instruction.MUL, 1);
        assertEquals(42 * -17, callStack.peek());
    }
    
    @Test
    public void testDiv() {
        byte[] program = {
            Instruction.DIV.getId()
        };
        callStack.push(42);
        callStack.push(-17);
        checkInstruction(program, 0x0F, Instruction.DIV, 1);
        assertEquals(42 / -17, callStack.peek());
    }
    
    @Test
    public void testDivByZero() {
        byte[] program = {
            Instruction.DIV.getId()
        };
        callStack.push(42);
        callStack.push(0);
        programMemory.store(0, program);
        Instruction i = instructionSet.getInstructionById(program[0]);
        controlUnit.fetchInstruction();
        i.execute(controlUnit);
        assertEquals(ControlUnit.MachineState.ERROR, controlUnit.getMachineState());
        assertEquals(error.Error.ErrorType.DIVISION_BY_ZERO.getNumber(), errorHandler.getLastError().getNumber());
    }
}