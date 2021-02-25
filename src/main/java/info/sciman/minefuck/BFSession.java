package info.sciman.minefuck;

import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;

public class BFSession {

    // Default length of program tape
    private static final int TAPE_LENGTH = 256;

    // Jump table
    private HashMap<Integer,Integer> jumpTable = new HashMap<>();

    // 'Tape' used to hold memory
    private final byte[] tape;
    // Pointer
    private int ptr;
    // Code pointer
    private int cptr;

    // Actual code
    private String code;
    private int codeLen;
    private boolean error; // True if there was an error building the jump table

    /**
     * Default constructor
     */
    public BFSession() {
        tape = new byte[TAPE_LENGTH];
        ptr = 0;
        cptr = 0;
    }

    /**
     * Load BF code into the session
     * @param code the code to load
     */
    public void load(String code) {
        this.code = code;
        this.codeLen = code.length();
        error = false;

        // Compute jump table
        jumpTable.clear();
        for (int i=0;i<codeLen;i++) {
            char c = code.charAt(i);
            if (c == '[') {
                // Find corresponding closing bracket
                int bc = 0; // Bracket counter
                for (int j=i;j<codeLen;j++) {
                    char c2 = code.charAt(j);
                    if (c2 == '[') {
                        bc++;
                    }else if (c2 == ']') {
                        bc--;
                    }
                    if (bc == 0) {
                        // Add table values and break
                        jumpTable.put(i,j);
                        jumpTable.put(j,i);
                        break;
                    }
                }
                if (bc != 0) {
                    // Uh oh
                    error = true;
                    return;
                }
            }
        }
        // Verify jump table
        for (Map.Entry<Integer,Integer> pair : jumpTable.entrySet()) {
            int a = pair.getKey();
            int b = pair.getValue();
            // Check closing brackets
            if (code.charAt(a) == ']') {
                if (code.charAt(b) != '[') {
                    // Uh oh
                    error = true;
                    return;
                }
            }
        }


        reset();
    }

    /**
     * Reset the pointer and tape
     */
    public void reset() {
        ptr = 0;
        cptr = 0;
        // Clear tape
        for (int i=0;i<tape.length;i++) {
            tape[i] = 0;
        }
    }

    /**
     * Returns true if the pointer has not advanced beyond the end of the available code
     * @return
     */
    public boolean available() {
        return !error && cptr < codeLen && codeLen > 0;
    }

    /**
     * Run a single iteration and return the value currently at the pointer
     * @return
     */
    public int advance() {

        if (!available()) {
            return -1;
        }

        char instruction = code.charAt(cptr);
        switch (instruction) {

            case '>':
                // Move pointer right
                if (++ptr >= tape.length) {ptr = 0;}
                break;
            case '<':
                // Move pointer left
                if (--ptr < 0) {ptr = tape.length-1;}
                break;
            case '+':
                // Increment tape value
                tape[ptr]++;
                break;
            case '-':
                // Decrement tape value
                tape[ptr]--;
                break;
            case '.':
                // Print character
                break;
            case ',':
                // Input character
                break;

            case '[':
                // Jump to next ']' if pointer value is 0
                if (tape[ptr] == 0) {
                    // Jump
                    cptr = jumpTable.get(cptr);
                }
                break;
            case ']':
                // Jump to previous '[' if pointer is nonzero
                if (tape[ptr] != 0) {
                    // Jump
                    cptr = jumpTable.get(cptr);
                }
                break;

        }
        // Increment pointer
        cptr++;

        return tape[ptr];
    }


    /**
     * Appends data relevant to the BFSession to the tag
     * @param tag
     */
    public void toTag(CompoundTag tag) {

    }

    /**
     * Retrieves relevant data from the tag
     * @param tag
     */
    public void fromTag(CompoundTag tag) {

    }

}
