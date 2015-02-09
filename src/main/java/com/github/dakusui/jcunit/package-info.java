/**
 * JCUnit is a combinatorial testing framework for Java.
 * Below is a diagram which describes JCUnit's object model.
 *
 * {@code
 *                                                        +------------------------+
 *                                                        |JCUnitConfigurablePlugin|
 *                                                        +------------------------+
 *                                                                    |
 *                                                 +------------------A------------------+
 *     +-----------------------------+             |                  |                  |
 *     |                             |             |                  |                  |
 *     |                             |             |                  |                  |
 *     | +------+ 1 * +------------+ |    +-----------------+ +--------------+ +--------------------+
 *     | |JCUnit|<>-->|JCUnitRunner| |    |ConstraintManager| |TupleGenerator| |FactorLevelsProvider|
 *     | +------+     +------------+ |    +-----------------+ +--------------+ +--------------------+
 *     |                             |             A                  A                  A
 *     |     JCUnit framework                      |                  |                  |
 *     |                             |<>-----------+------------------+------------------+
 *     +-----------------------------+
 *                |1    |1
 *          +-----+     +------+
 *          |                  |   (Class: Test Suite,
 *          V * (Test case)    V *  Instance: Test Object)   (Class level precondition)
 *        +-----+          +--------+        +-------------------+
 *        |Tuple|          |YourTest|---+--->|Precondition method|
 *        +-----+          +--------+1  |  * +-------------------+
 *                                      |
 *                                      |           (Test)                     (Method level precondition)
 *                                      |    +-----------+       +-------------------+
 *                                      +--->|Test method|------>|Precondition method|
 *                                         * +-----------+1     *+-------------------+
 * }
 *
 */
package com.github.dakusui.jcunit;