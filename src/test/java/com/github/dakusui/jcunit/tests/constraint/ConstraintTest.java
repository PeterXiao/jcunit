package com.github.dakusui.jcunit.tests.constraint;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.ConstraintObserver;
import com.github.dakusui.jcunit.constraint.constraintmanagers.TypedConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConstraintTest {
  private Factors loadFactorsFromClass(Class<?> testClass) {
    Factors.Builder b = new Factors.Builder();
    for (Field f : Utils.getAnnotatedFields(testClass, FactorField.class)) {
      Factor factor = new FactorLoader(f).getFactor();
      b.add(factor);
    }
    return b.build();
  }

  public static class TestClass {
    @FactorField(intLevels = {1024})
    public int f1;
  }
  @Test
  public void testConstraintManager() throws UndefinedSymbol {
    ConstraintManager manager = new TypedConstraintManager<TestClass>() {
      @Override protected boolean check(TestClass o, Tuple tuple)
          throws UndefinedSymbol {
        return true;
      }

      @Override protected List<TestClass> getViolationTestObjects() {
        List<TestClass> ret = new LinkedList<TestClass>();
        TestClass t = new TestClass();
        t.f1 = 128;
        ret.add(t);
        return ret;
      }
    };

    manager.setFactors(loadFactorsFromClass(TestClass.class));
    assertEquals(1, manager.getFactors().size());
    assertEquals(1, manager.getFactors().get("f1").levels.size());
    assertEquals(1024, manager.getFactors().get("f1").levels.get(0));

    ConstraintObserver observer = new ConstraintObserver() {
      @Override public void implicitConstraintFound(Tuple constraint) {
      }
    };
    manager.addObserver(observer);
    assertEquals(1, manager.observers().size());

    manager.removeObservers(observer);
    assertEquals(0, manager.observers().size());

    assertEquals(true,
        manager.check(new Tuple.Builder().put("f1", 100).build()));


    assertEquals(128, manager.getViolations().get(0).get("f1"));
  }

  public static class CM extends TypedConstraintManager<TestClass2> {
    @Override
    protected boolean check(TestClass2 o, Tuple tuple) throws UndefinedSymbol {
      return false;
    }
  }
  @RunWith(JCUnit.class)
  @TupleGeneration( constraint = @Constraint(CM.class) )
  public static class TestClass2 {
    @FactorField(intLevels = {1, 2, 3})
    public int f;
    @FactorField(intLevels = {1, 2, 3})
    public int g;
  }
  @Test
  public void givenCMthatRejectsAnyTestCase$whenJCUnitGeneratesTestCase$thenWhatHappens() {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertEquals(1, result.getRunCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("No runnable methods", result.getFailures().get(0).getMessage());
  }
}