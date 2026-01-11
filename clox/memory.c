#include <stdlib.h>

#include "memory.h"

/*
 * reallocate
 * ----------
 * A thin wrapper around realloc/free that centralizes memory allocation
 * behavior for this project.
 *
 * - If newSize is zero the function frees the pointer and returns NULL.
 * - Otherwise it attempts to realloc to the new size and exits the process
 *   on allocation failure. (The book's implementation requests a simple
 *   failure path early in development.)
 *
 * Parameters:
 *  - pointer: pointer to previously allocated block (or NULL).
 *  - oldSize: previous size in bytes (unused by this simple wrapper but
 *             useful if you later add bookkeeping.
 *  - newSize: desired new size in bytes; if zero the block is freed.
 *
 * Returns:
 *  - pointer to the newly allocated memory (or NULL if newSize == 0).
 */
void *reallocate(void *pointer, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        free(pointer);
        return NULL;
    }
    void *result = realloc(pointer, newSize);
    if(result == NULL)  exit(1);
    return result;
}
