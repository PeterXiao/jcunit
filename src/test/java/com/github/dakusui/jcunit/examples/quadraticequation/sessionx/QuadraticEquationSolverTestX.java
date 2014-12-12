package com.github.dakusui.jcunit.examples.quadraticequation.sessionx;

import com.github.dakusui.jcunit.constraint.constraintmanagers.TypedConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
@TupleGeneration(
    constraint = @Constraint(
        value = QuadraticEquationSolverTestX.CM.class,
        params = { }))
public class QuadraticEquationSolverTestX {
  @Rule
  public TestName   name = new TestName();
  @Rule
  public JCUnitDesc desc = new JCUnitDesc();
  @FactorField(intLevels = { 0, 1, -2, 100, -100, 101, -101 })
  public int a;
  @FactorField(intLevels = { 0, 1, -2, 100, -100, 101, -101 })
  public int b;
  @FactorField(intLevels = { 0, 1, -2, 100, -100, 101, -101 })
  public int c;

  public QuadraticEquationSolverTestX() {
  }

  @SuppressWarnings("unchecked")
  @CustomTestCases
  public static Iterable<QuadraticEquationSolverTestX> customTestCases() {
    return Arrays.asList(
        create(1, 2, 1),
        create(0, 200, 1)
    );
  }

  private static QuadraticEquationSolverTestX create(int a, int b, int c) {
    QuadraticEquationSolverTestX ret = new QuadraticEquationSolverTestX();
    ret.a = a;
    ret.b = b;
    ret.c = c;
    return ret;
  }

  //@Precondition
  public boolean aIsNonZero() {
    return this.a != 0;
  }

  public boolean discriminantIsNonNegative() {
    int a = this.a;
    int b = this.b;
    int c = this.c;
    return b * b - 4 * c * a >= 0;
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!aIsNonZero", "!discriminantIsNonNegative" })
  public void solveEquation$ThenIllegalArgumentExceptionWillBeThrown() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test
  @When({ "aIsNonZero&&discriminantIsNonNegative" })
  public void solveEquation$ThenSolutionsArePreciseEnough() {
    try {
      System.out.println(String
          .format("desc=(%s,%s)", desc.getTestName(), desc.getTestCaseType()));
      QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
          c).solve();
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
    } catch (IllegalArgumentException e) {
      throw e;
    }
  }

  public static class CM extends
      TypedConstraintManager<QuadraticEquationSolverTestX> {
    @Override
    public boolean check(QuadraticEquationSolverTestX o, Tuple testCase)
        throws UndefinedSymbol {
      if (!testCase.containsKey("a") || !testCase.containsKey("b") || !testCase
          .containsKey("c")) {
        throw new UndefinedSymbol();
      }
      return o.a != 0 && o.b * o.b - 4 * o.c * o.a >= 0;
    }

    @Override
    protected List<QuadraticEquationSolverTestX> getViolationTestObjects() {
      List<QuadraticEquationSolverTestX> ret = new LinkedList<QuadraticEquationSolverTestX>();
      ret.add(create(0, 1, 1));
      ret.add(create(100, 1, 100));
      ret.add(create(0, 0, 1));
      return ret;
    }
  }
}