#include <stdlib.h>
#include <stdio.h>

#include "memory.h"
#include "value.h"

/*
 * initValueArray
 * --------------
 * Initialize an empty ValueArray which holds literal values used by the
 * chunk's constant table.
 *
 * This sets count/capacity to zero and the values pointer to NULL so the
 * array can be safely passed to writeValueArray which will grow it on demand.
 *
 * Parameters:
 *  - array: pointer to the ValueArray to initialize.
 */
void initValueArray(ValueArray *array) {
    array->count = 0;
    array->capacity = 0;
    array->values = NULL;
}

/*
 * writeValueArray
 * ----------------
 * Append a Value to the dynamic ValueArray, growing storage when needed.
 *
 * This function ensures there is enough capacity, grows the backing array
 * using the memory allocation helpers, stores `value` at the next slot, and
 * increments the count. It is used by the compiler to populate a chunk's
 * constants table.
 *
 * Parameters:
 *  - array: pointer to the ValueArray to append into.
 *  - value: the Value to append.
 */
void writeValueArray(ValueArray *array, Value value) {
    if (array->count == array->capacity) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->values = GROW_ARRAY(Value, array->values, oldCapacity, array->capacity);
    }
    array->values[array->count] = value;
    array->count++;
}

/*
 * freeValueArray
 * --------------
 * Free the memory used by a ValueArray and reset it to an initialized empty
 * state.
 *
 * This calls the memory macros to release the backing array and then
 * reinitializes the ValueArray fields so the structure is safe to reuse.
 */
void freeValueArray(ValueArray *array) {
    FREE_ARRAY(Value, array->values, array->capacity);
    initValueArray(array);
}

/*
 * printValue
 * ----------
 * Print a Value to stdout in a human-readable form. Currently this VM only
 * supports numbers, so it uses the %g format specifier.
 *
 * Parameters:
 *  - value: the Value to print.
 */
void printValue(Value value) {
    printf("%g", value);
}
