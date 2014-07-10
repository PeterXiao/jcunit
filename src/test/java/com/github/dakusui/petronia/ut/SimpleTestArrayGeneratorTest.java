package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;

public class SimpleTestArrayGeneratorTest extends TestArrayGeneratorTest {

  @Override
  protected TestArrayGenerator<String> createTestArrayGenerator() {
    return new SimpleTestArrayGenerator<String>();
  }
}
