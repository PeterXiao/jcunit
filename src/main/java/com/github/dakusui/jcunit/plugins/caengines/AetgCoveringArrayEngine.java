package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A covering array generator that implements following algorithm.
 * <p/>
 * <pre>
 * Assume that we have a system with k test parameters and
 * that the ith parameter has l i different values. Assume that we
 * have already selected r test cases. We select the r + 1 by first
 * generating M different candidate test cases and then choosing
 * one that covers the most new pairs. Each candidate test
 * case is selected by the following greedy algorithm:
 * 1) Choose a parameter f and a value l for f such that that
 *    parameter value appears in the greatest number of
 *    uncovered pairs.
 * 2) Let f 1 = f. Then choose a random order for the re-
 *    maining parameters. Then, we have an order for all k
 *    parameters f 1 , ... f k .
 * 3) Assume that values have been selected for parameters
 *    f 1 , ..., f j . For 1 <= i <= j, let the selected value for f i be
 *    called v i . Then, choose a value v j+1 for f j+1 as follows.
 *    For each possible value v for f j , find the number of
 *    new pairs in the set of pairs {f j+1 = v and f i = v i for 1 <= i <= j}.
 *    Then, let v j+1 be one of the values that appeared in
 *    the greatest number of new pairs.
 *    Note that, in this step, each parameter value is considered
 *    only once for inclusion in a candidate test case.
 *    Also, that when choosing a value for parameter f j+1 ,
 *    the possible values are compared with only the j
 *    values already chosen for parameters f 1 , ..., f j .
 * We did many experiments with this algorithm. When we
 * set M = 50, i.e., when we generated 50 candidate test cases
 * for each new test case, the number of generated test cases
 * grew logarithmically in the number of parameters (when all
 * the parameters had the same number of values).
 * </pre>
 * <p/>
 * This algorithm was first invented by Cohen, et al.
 *
 * @see "Cohen, et al., "The AETG System: An Approach to Testing Based on Combinatorial Design"in IEEE Trans. Softw. Eng., July 1997"
 */
public class AetgCoveringArrayEngine extends CoveringArrayEngine.Base {
  /**
   * A number M referred to in the paper.
   */
  private static int TRIES = 50;
  private final int         strength;
  private final int         randomSeed;

  public AetgCoveringArrayEngine(
      @Param(source = Param.Source.CONFIG, defaultValue = "2") int strength,
      @Param(source = Param.Source.CONFIG, defaultValue = "2") int randomSeed
  ) {
    this.strength = strength;
    this.randomSeed = randomSeed;
  }

  @Override
  protected List<Tuple> generate(Factors factors, final ConstraintChecker constraintChecker) {
    List<Tuple> allPossibleTuples = factors.generateAllPossibleTuples(this.strength, new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        try {
          ////
          // SmartConstraintChecker is stateful. I need to come up with a solution
          // to handle it seemlessly with the other checkers.
          // Or it maybe is a responsiility of a caller.
          return constraintChecker.check(in);
        } catch (UndefinedSymbol undefinedSymbol) {
          ////
          // If constraintChecker is present and throws an undefined symbol, the tuple
          // cannot be removed. In this case we should return true.
          return true;
        }
      }
    });
    List<Tuple> ret = new LinkedList<Tuple>();
    Set<Tuple> remainingTuples = new HashSet<Tuple>(allPossibleTuples);
    while (!remainingTuples.isEmpty()) {
      int newlyCoveredTuples = 0; // If no new tuple can be covered, new test case shouldn't be added.
      Tuple chosenTestCase = null;
      for (int i = 0; i < TRIES; i++) {
        Tuple newTestCaseCandidate = createNewTestCase(factors, remainingTuples);
        int numCoveredByNewCandidate = countTuplesNewlyCovered(newTestCaseCandidate, remainingTuples, strength);
        if (numCoveredByNewCandidate > newlyCoveredTuples) {
          newlyCoveredTuples = numCoveredByNewCandidate;
          chosenTestCase = newTestCaseCandidate;
        }
      }
      if (chosenTestCase == null) {
        assert newlyCoveredTuples == 0;
        ////
        // Time to give up;
        return ret;
      }
      ret.add(chosenTestCase);
    }
    return ret;
  }

  private Tuple createNewTestCase(Factors factors, Set<Tuple> remainingTuples) {
    Tuple ret = null;
    return ret;
  }

  private int countTuplesNewlyCovered(Tuple testCase, Set<Tuple> tuplesYetToBeCovered, int strength) {
    int ret = 0;
    for (Tuple eachSubtuple : TupleUtils.subtuplesOf(testCase, strength)) {
      if (tuplesYetToBeCovered.contains(eachSubtuple)) {
        ret++;
      }
    }
    return ret;
  }
}
