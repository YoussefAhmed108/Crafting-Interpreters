#include <stdlib.h>
#include <stdio.h>
#include "chunk.h"
#include "memory.h"

/*
 * initChunk
 * ----------
 * Initialize an empty Chunk structure.
 *
 * This sets the dynamic arrays to empty (NULL) and zeroes the counters so the
 * chunk is in a consistent, empty state. The chunk's constant table is
 * initialized via initValueArray. This mirrors the book's approach of
 * explicitly initializing heap-owned fields before use to avoid relying on
 * implicitly zeroed memory.
 *
 * Parameters:
 *  - chunk: pointer to the Chunk to initialize.
 */
void initChunk(Chunk *chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    chunk->lines = NULL;
    initValueArray(&chunk->constants);
}

/*
 * freeChunk
 * ---------
 * Release all memory owned by a Chunk and reset it back to an empty state.
 *
 * This frees the dynamic arrays used for bytecode and line number mapping,
 * then frees the constants array. Finally it calls initChunk to put the
 * structure back into a clean, initialized empty state. Doing this allows the
 * caller to safely reuse the Chunk struct after freeing.
 *
 * Parameters:
 *  - chunk: pointer to the Chunk to free.
 */
void freeChunk(Chunk *chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(int, chunk->lines, chunk->capacity);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

/*
 * writeChunk
 * ----------
 * Append a single bytecode byte and its source line number to the chunk.
 *
 * This helper grows the chunk's capacity as needed, writes the opcode byte
 * into the `code` array and the corresponding source `line` into the
 * `lines` array, then increments the `count`.
 *
 * It is used by the compiler to emit opcodes and to keep a parallel mapping
 * from bytecode offset to source line for better error reporting and
 * disassembly (see debug.c).
 *
 * Parameters:
 *  - chunk: pointer to the Chunk to append into.
 *  - byte:  the opcode or operand byte to write.
 *  - line:  the source line number associated with this byte.
 */
void writeChunk(Chunk* chunk, uint8_t byte , int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
        chunk->lines = GROW_ARRAY(int, chunk->lines, oldCapacity, chunk->capacity);
    }
    chunk->code[chunk->count] = byte;
    chunk->lines[chunk->count] = line;
    chunk->count++;
}

/*
 * addConstant
 * -----------
 * Append a value to the chunk's constant table and return its index.
 *
 * The compiler uses this to intern literal values (numbers, strings, etc.)
 * into a contiguous table. Bytecode instructions which reference constants
 * (e.g. OP_CONSTANT) store the returned index as their operand.
 *
 * Returns:
 *  - the integer index of the newly added constant within chunk->constants.
 */
int addConstant(Chunk *chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1; // Return the index of the new constant
}
