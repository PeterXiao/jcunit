# JCUnit
JCUnit is a framework to perform combinatorial tests using 'pairwise'(or more generally 't-wise') 
technique.
About what combinatorial testings are, articles below are useful as a starting point.

* [All-pairs testing](http://en.wikipedia.org/wiki/All-pairs_testing)
* [Introduction to Combinatorial Testing](http://mse.isri.cmu.edu/software-engineering/documents/faculty-publications/miranda/kuhnintroductioncombinatorialtesting.pdf)

Very roughly to say, it's a technique to generate test cases with good 'coverage' 
without making the number of test cases explode.

# Installation
## Maven coordinate
First of all, you will need to link JCUnit to your project.
Below is a pom.xml fragment to describe jcunit's dependency.
Please add it to your project's pom.xml 

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>jcunit</artifactId>
      <version>0.5.5</version>
    </dependency>
    
```


## Building from source
You can build ```combinatoradix``` by getting the source code from github.

```

    $ git clone https://github.com/dakusui/jcunit.git
    ...
    $ cd combinatoradix
    ...
    $ mvn package
    ...
    $
```

You will find a compiled jar file ```jcunit-{X.Y.Z}-SNAPSHOT.jar``` under
 ```target/``` directory. Place the file somewhere handy and include it in your classpath.
 
# First test with JCUnit
Below is JCUnit's most basic example 'QuadraticEquationSolver.java'.
Just by running QuadraticEquationSolverTest.java as a usual JUnit test, JCUnit will 
automatically generate test cases based on '@FactorField' annotations.

## QuadraticEquationSolver program example
To understand JCUnit's functions, let's test 'QuadraticEquationSolver.java' program, which solves 'quadratic equations' using a formula.
The program contains some intentional bugs and unclear specifications (or behaviors).
The formula it uses is,

```java

    {x1, x2} = { (-b + Math.sqrt(b*b - 4*c*a)) / 2*a, (-b - Math.sqrt(b*b - 4*c*a)) / 2*a }

```

where {x1, x2} are the solutions of an equation, 

```java

    a * x^2 + b * x + c = 0

```

### QuadraticEquationSolver.java (Main class, SUT)
'QuadraticEquationSolver' is the SUT (Software under test) in this example.
The class provides a function to solve a quadratic equation using a quadratic formula and returns the solutions.

```java

    //QuadraticEquationSolver.java
    public class QuadraticEquationSolver {
      private final double a;
      private final double b;
      private final double c;
    
      public static class Solutions {
        public final double x1;
        public final double x2;
    
        public Solutions(double x1, double x2) {
          this.x1 = x1;
          this.x2 = x2;
        }
    
        public String toString() {
          return String.format("(%f,%f)", x1, x2);
        }
      }
    
      public QuadraticEquationSolver(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
      }
    
      public Solutions solve() {
        return new Solutions(
            (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
            (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
        );
      }
    }
```

Did you already notice the bugs that this program has?

* It doesn't consider equations that do not have solutions in real.
* If it's not a quadratic equation but a linear one, how should it behave?
* Errors. How should it handle errors? To what extent error is acceptable?
* Overflows. If b * b, 4 * c * a, etc become bigger than Double.MAX_VALUE (or 
smaller than Double.MIN_VALUE), how should it handle them?
* Shouldn't we set some limits for a, b, and c? Both to make errors small enough 
and prevent overflows from happening.
* etc. (maybe)

Try to find (and reproduce) these bugs using JCUnit and fix them.

### QuadraticEquationSolverTest.java (Test)
QuadraticEquationSolverTest1 is a test class for QuadraticEquationSolver class.

```java

    // QuadraticEquationSolverTest1.java
    @RunWith(JCUnit.class)
    public class QuadraticEquationSolverTest1 {
        @FactorField
        public int a;
        @FactorField
        public int b;
        @FactorField
        public int c;
    
        @Test
        public void test() {
            QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
                    c).solve();
            assertEquals(0.0, a * s.x1 * s.x1 + b * s.x1 + c);
            assertEquals(0.0, a * s.x2 * s.x2 + b * s.x2 + c);
        }
    }
    
```

If you run this test class, JCUnit generates about fifty test cases and run them.
By default, it generates the test cases by using 'all-pairs' technique.

# Features, tips, and examples
## FSM support feature
FSM support of JCUnit (FSM/JCUnit) is a feature that allows you to model your software as a finite state machine, and JCUnit generates and executes a test suite for it.
The test suite generation can be done by JCUnit's tuple generators.

This is really fun feature. Please try. Documentation is found [here](/src/site/markdown/FSM.md).

## Tips
When you learn pairwise technique, probably you get excited that "oh I can balance a size of test cases and coverage by this! Its idea is intuitive and looks straightforward. A test case is essentially attibutes and their values. Nice!"
But when you start testing your software using the technique, you will almost immediately come across a lot of questions.

* "A test case that uses Internet Explorer on Linux platform doesn't make sense. But this test case contributes to cover Linux platform + Apache, Apache + PostgreSQL, etc, at the same time.
* "If expectations for test cases can be different depending on a test case's values, how can I define test methods?"
* etc.

Best practices for those problems might be found [here](/src/site/markdown/TIPS.md).

Also how you can customize how test cases should be generated, e.g., how to configure possible values for a certain parameter, in case you want to use non-primitive values what you can do, etc, can be found.

## Examples
### Quadratic equation solver
* **[session1](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session1/QuadraticEquationSolverTest1.java)**:
  Test initial version of quadratic equation solver.
* **[session2](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session2/QuadraticEquationSolverTest2.java)**:
  Exclude invalid test cases, whose ```a``` is 0. Because they are not quadratic equations.
  And allow tests to accept solutions which make the equation less than 0.01.
* **[session3](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session3/QuadraticEquationSolverTest3.java)**:
  Exclude tests cases whose absolute values of ```a```, ```b```, and ```c``` because they
  cause overflows.
* **[session4](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session4/QuadraticEquationSolverTest4.java)**:
  The SUT, ```QuadraticEquationSolver``` is now enhanced to throw ```IllegalArgumentException``` when solutions become imaginary.
  Tests need to be enhanced to handle this new behaviour, too. ```@When``` annotation will be introduced to switch test methods
  to be executed.
* **[session5](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session5/QuadraticEquationSolverTest5.java)**: 
* **[session6](src/test/java/com/github/dakusui/jcunit/examples/quadraticequation/session6/QuadraticEquationSolverTest6.java)**: 


# Refefences
* [1] "Pairwise Testing", A website for pairwise technique
* [2] "PICT", A most known powerful pairwise tool by microsoft, which is now open source.

[1]: http://www.pairwise.org/
[2]: https://github.com/microsoft/pict

# Copyright and license #

Copyright 2013 Hiroshi Ukai.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
