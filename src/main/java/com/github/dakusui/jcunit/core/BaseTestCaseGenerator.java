package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class BaseTestCaseGenerator implements TestCaseGenerator {
  protected String[] params;
  private Factors factors = null;
  private long    size    = -1;
  private long    cur     = -1;

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNext() {
    if (size < 0 || this.cur < 0) {
      throw new IllegalStateException();
    }
    return cur < size;
  }

  @Override
  public Iterator<Tuple> iterator() {
    return this;
  }

  @Override
  public Tuple next() {
    if (cur >= size) {
      throw new NoSuchElementException();
    }
    Tuple ret = get(cur);
    cur++;
    return ret;
  }

  @Override
  final public void init(String[] params,
      Factors factors) {
    this.factors = factors;
    this.params = params;
    this.size = initializeTestCases(params, factors);
    this.cur = 0;
  }

  @Override
  public Tuple get(long cur) {
    Tuple ret = new Tuple();
    for (String f : this.factors.getFactorNames()) {
      ret.put(f, factors.get(f).levels.get(getIndex(f, cur)));
    }
    return ret;
  }

  @Override
  public List<String> getFactorNames() {
    List<String> ret = new ArrayList<String>(this.factors.size());
    for (String k : this.factors.getFactorNames()) {
      ret.add(k);
    }
    return ret;
  }

  @Override public Factor getFactor(String factorName) {
    return this.factors.get(factorName);
  }

  @Override
  public long size() {
    if (this.size < 0) {
      throw new IllegalStateException();
    }
    return this.size;
  }

  /**
   * Implementation of this method must return a number of test cases to be executed in total.
   *
   * @return A number of test cases
   */
  abstract protected long initializeTestCases(
      String[] params,
      Factors domains);
}