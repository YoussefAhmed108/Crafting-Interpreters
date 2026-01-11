#include "vm.h"
#include <stdio.h>
#include "chunk.h"
#include "debug.h"
#include "compiler.h"

VM vm;

/*
 * resetStack
 * ----------
 * Reset the VM's stack pointers to an empty stack state.
 *
 * This places `stackTop` at the base of the stack so subsequent push/pop
 * operations behave as if the VM was freshly started. It is used by
 * initVM to initialize runtime state.
 */
void resetStack() {
    vm.stackTop = vm.stack;
}

/*
 * initVM
 * ------
 * Initialize the VM runtime state.
 *
 * Currently this simply resets the value stack. In the book this is also
 * where you'd initialize globals, interned strings, and other runtime
 * subsystems when the language grows.
 */
void initVM() {
    resetStack();
}

/*
 * freeVM
 * ------
 * Free any resources owned by the VM. Presently a no-op because most
 * allocations are owned by chunks/values and freed elsewhere, but the
 * function is kept for symmetry and future cleanup needs.
 */
void freeVM() {
}

/*
 * run
 * ---
 * The core bytecode interpreter loop. It repeatedly fetches, decodes, and
 * executes opcodes from the current chunk until a return or runtime error
 * occurs.
 *
 * The function uses helper macros to read bytes and constants and to perform
 * binary arithmetic operations. When DEBUG_TRACE_EXECUTION is enabled it
 * prints the stack and current instruction for tracing.
 *
 * Returns an InterpretResult indicating success or the kind of failure.
 */
InterpretResult run() {
    #define READ_BYTE() (*vm.ip++)
    #define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
    #define BINARY_OP(op) \
        do { \
            Value b = pop(); \
            Value a = pop(); \
            push(a op b); \
        } while (false)

    for(;;) {
        #ifdef DEBUG_TRACE_EXECUTION
        printf("          ");
        for (Value *slot = vm.stack; slot < vm.stackTop; slot++) {
            printf("[ ");
            printValue(*slot);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
        #endif

        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                // printValue(constant);
                // printf("\n");
                push(constant);
                break;
            }
            case OP_RETURN: {
                Value result = pop();
                printValue(result);
                printf("\n");
                return INTERPRET_OK;
            }

            case OP_NEGATE: push(-pop());break;
            case OP_ADD: BINARY_OP(+); break;
            case OP_SUBTRACT: BINARY_OP(-); break;
            case OP_MULTIPLY: BINARY_OP(*); break;
            case OP_DIVIDE: BINARY_OP(/); break;
        }
    }

    #undef READ_BYTE
    #undef READ_CONSTANT
    #undef BINARY_OP
}

/*
 * interpret
 * ---------
 * Execute a single chunk through the VM.
 *
 * This sets the VM's current chunk and instruction pointer, then calls run
 * to perform the actual execution. It returns the result from run so callers
 * can react to compile/runtime errors.
 */
InterpretResult interpret(const char* source) {
    compiler(source);
    return INTERPRET_OK;
}

/*
 * push
 * ----
 * Push a Value onto the VM value stack.
 *
 * It writes the value at the current stackTop and increments the pointer.
 */
void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

/*
 * pop
 * ---
 * Pop and return the top Value from the VM value stack.
 *
 * It decrements the stackTop pointer and returns the value that was stored
 * at the old top location.
 */
Value pop() {
    vm.stackTop--;
    return *vm.stackTop;
}
