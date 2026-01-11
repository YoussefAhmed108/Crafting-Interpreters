#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "memory.h"
#include "vm.h"

/*
 * repl
 * ----
 * A simple read-eval-print loop for interactive use.
 *
 * It repeatedly prints a prompt, reads a line from stdin, and passes the
 * input to the interpreter. The loop ends when EOF is read (e.g., Ctrl-D), at
 * which point it prints a newline and returns. This matches the book's simple
 * REPL used while developing the interpreter.
 */
static void repl() {
    char line[1024];
    for (;;) {
        printf("> ");
        if (!fgets(line, sizeof(line), stdin)){
            printf("\n");
            break;
        }
        interpret(line);
    }
}

/*
 * readFile
 * --------
 * Read the entire contents of a file into a newly allocated, null-terminated
 * buffer and return it. The caller is responsible for freeing the returned
 * buffer.
 *
 * This utility is used when running a script from a file: we need the whole
 * source to pass to the compiler/interpreter. On failure it returns NULL.
 *
 * Parameters:
 *  - path: the filesystem path to the source file to load.
 *
 * Returns:
 *  - a malloc'd buffer containing the file contents (null-terminated), or
 *    NULL on failure.
 */
static char* readFile(const char* path) {
    FILE* file = fopen(path, "rb");
    if (file == NULL) {
        fprintf(stderr, "Error: Could not open file %s\n", path);
        return NULL;
    }

    fseek(file, 0, SEEK_END);
    size_t fileSize = ftell(file);
    fseek(file, 0, SEEK_SET);

    char* buffer = (char*) malloc(fileSize + 1);
    if (buffer == NULL) {
        fprintf(stderr , "Not Enough Memory to read %s\n", path);
        exit(74); // EXIT_CODE_NOT_ENOUGH_MEMORY
    }

    size_t bytesRead = fread(buffer, sizeof(char), fileSize, file);
    if (bytesRead < fileSize) {
        fprintf(stderr, "Could not read file %s\n", path);
        free(buffer);
        exit(74); // EXIT_CODE_NOT_ENOUGH_MEMORY
    }
    buffer[bytesRead] = '\0';

    fclose(file);
    return buffer;
}

/*
 * runFile
 * -------
 * Load and execute a single source file. The function reads the file into
 * memory, calls the interpreter, frees the loaded source, and exits the
 * process with a specific code if a compile or runtime error occurred. This
 * mirrors the command-line behavior shown in the Crafting Interpreters book.
 */
static void runFile(const char* path) {
    char* source = readFile(path);
    InterpretResult result = interpret(source);
    free(source);

    if (result == INTERPRET_COMPILE_ERROR) {
        exit(65);
    } else if (result == INTERPRET_RUNTIME_ERROR) {
        exit(70);
    }
}


int main(int argc, const char* argv[]) {
    initVM();
    if(argc == 1){
        repl();
    }else if(argc == 2){
        runFile(argv[1]);
    } else {
        fprintf(stderr, "Usage: clox [path]\n");
        exit(64);
    }
    freeVM();
    return 0;
}