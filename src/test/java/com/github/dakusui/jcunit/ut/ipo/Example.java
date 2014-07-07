package com.github.dakusui.jcunit.ut.ipo;

import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.generators.PairwiseTestArrayGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(PairwiseTestArrayGenerator.class)
public class Example {
  @In(
      domain = Domain.Method)
  public int a;

  public static int[] a() {
    return new int[] { 1, 2, 3 };
  }

  @In(
      domain = Domain.Method)
  public int b;

  public static int[] b() {
    return new int[] { 1, 2, 3 };
  }

  @In(
      domain = Domain.Method)
  public int c;

  public static int[] c() {
    return new int[] { 1, 2, 3 };
  }

  @In(
      domain = Domain.Method)
  public int d;

  public static int[] d() {
    return new int[] { 1, 2, 3 };
  }

  @Test
  public void test() {
    System.out.println(String.format("(%d,%d,%d,%d)", a, b, c, d));
  }
}
