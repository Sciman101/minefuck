package info.sciman.minefuck;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class BFSession {

    // Default length of program tape
    private static final int TAPE_LENGTH = 256;

    // Jump table
    private int[] jumpTable = new int[0];

    // 'Tape' used to hold memory
    private byte[] tape = new byte[TAPE_LENGTH];
    // Pointer
    private int ptr;
    // Program counter
    private int pc;

    // Actual code
    private String code = "";
    private int error = -1; // If non-negative, there is a bracket mismatch error at the specified position

    // Redstone interaction
    private int outputLevel;
    private int inputLevel;

    /**
     * Default constructor
     */
    public BFSession() {
        ptr = 0;
        pc = 0;
    }

    /**
     * Appends data relevant to the BFSession to the tag
     *
     * @param tag
     */
    public void toTag(CompoundTag tag) {
        tag.putByteArray("tape", tape);
        tag.putIntArray("jumpTable", jumpTable);
        tag.putInt("ptr", ptr);
        tag.putInt("cptr", pc);
        tag.putInt("output", outputLevel);
        tag.putInt("input", inputLevel);
        tag.putString("code", code);
        tag.putInt("error", error);
    }

    /**
     * Retrieves relevant data from the tag
     *
     * @param tag
     */
    public void fromTag(CompoundTag tag) {
        tape = tag.getByteArray("tape");
        jumpTable = tag.getIntArray("jumpTable");
        ptr = tag.getInt("ptr");
        pc = tag.getInt("cptr");
        code = tag.getString("code");
        error = tag.getInt("error");
        outputLevel = tag.getInt("output");
        inputLevel = tag.getInt("input");
    }

    // Check for an error in building the jump table
    public boolean checkError() {
        return error != -1;
    }
    public int getError() {
        return error;
    }
    // Handle redstone interaction
    public int getOutputLevel() {return outputLevel;}
    public void setInputLevel(int str) {inputLevel = str;}

    /**
     * Load BF code into the session
     * @param bookItem the book item to load code from
     */
    public void load(ItemStack bookItem) {

        this.code = "";
        reset();

        // Get code from book
        CompoundTag tag = bookItem.getTag();
        boolean needParseJson = bookItem.getItem() == Items.WRITTEN_BOOK;
        if (tag != null) {
            ListTag listTag = tag.getList("pages",8).copy();
            for (int i=0;i<listTag.size();i++) {
                String page = listTag.getString(i);
                if (needParseJson) {
                    this.code += Text.Serializer.fromJson(page).getString();
                }else{
                    this.code += page;
                }
            }
        }

        error = -1;

        // Compute jump table
        jumpTable = new int[code.length()];
        for (int i=0;i<code.length();i++) {
            char c = code.charAt(i);
            if (c == '[') {
                // Find corresponding closing bracket
                int bc = 0; // Bracket counter
                for (int j=i;j<code.length();j++) {
                    char c2 = code.charAt(j);
                    if (c2 == '[') {
                        bc++;
                    }else if (c2 == ']') {
                        bc--;
                    }
                    if (bc == 0) {
                        // Add table values and break
                        jumpTable[i] = j;
                        jumpTable[j] = i;
                        break;
                    }
                }
                if (bc != 0) {
                    // Uh oh
                    error = i;
                    return;
                }
            }
        }
        // Verify jump table
        for (int i=0;i<code.length();i++) {
            int a = jumpTable[i];
            // Check closing brackets
            if (code.charAt(i) == ']') {
                if (code.charAt(a) != '[') {
                    // Uh oh
                    error = a;
                    return;
                }
            }
        }

        System.out.println("New code: " + code);
    }

    /**
     * Reset the pointer and tape
     */
    public void reset() {
        ptr = 0;
        pc = 0;
        outputLevel = 0;
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
        return !checkError() && (pc < code.length() && code.length() > 0);
    }

    /**
     * Run a single iteration and return the value currently at the pointer
     * @return
     */
    public int step() {

        if (!available()) {
            return -1;
        }

        char instruction = code.charAt(pc);
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
                // Output redstone value
                outputLevel = MathHelper.clamp(Byte.toUnsignedInt(tape[ptr]),0,15);
                break;
            case ',':
                // Input redstone value
                tape[ptr] = (byte)(inputLevel & 0xFF);
                break;

            case '[':
                // Jump to next ']' if pointer value is 0
                if (tape[ptr] == 0) {
                    // Jump
                    pc = jumpTable[pc];
                }
                break;
            case ']':
                // Jump to previous '[' if pointer is nonzero
                if (tape[ptr] != 0) {
                    // Jump
                    pc = jumpTable[pc];
                }
                break;

        }
        // Increment pointer
        pc++;

        return tape[ptr];
    }

    public int getPC() {
        return pc;
    }

    public int getPointer() {
        return ptr;
    }

    public int getTapeValue(int pos) {
        return Byte.toUnsignedInt(tape[pos]);
    }
}
