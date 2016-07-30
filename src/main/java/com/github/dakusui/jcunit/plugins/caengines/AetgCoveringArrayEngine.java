package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.combinatoradix.Enumerator;
import com.github.dakusui.combinatoradix.Enumerators;
import com.github.dakusui.combinatoradix.Permutator;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintBundle;

import java.util.*;

/**
 * A covering array generator that implements following algorithm.
 * <p/>
 * <pre>
 * Assume that we have a system with k test parameters and
 * that the i^th parameter has l_i different values. Assume that we
 * have already generated r test cases. We generate the (r + 1)^th by first
 * generating M different candidate test cases and then choosing
 * one that covers the most new pairs (or t-wise tuples). Each candidate test
 * case is generated by the following greedy algorithm:
 * 1) Choose a parameter f and a value l for f such that that
 *    parameter value appears in the greatest number of
 *    uncovered pairs.
 * 2) Let f_1 = f. Then choose a random order for the re-
 *    maining parameters. Then, we have an order for all k
 *    parameters f_1 , ... f_k .
 * 3) Assume that values have been selected for parameters
 *    f_1 , ..., f_j . For 1 <= i <= j, let the selected value for f_i be
 *    called v_i . Then, choose a value v_(j+1) for f_(j+1) as follows.
 *    For each possible value v for f_(j+1) , find the number of
 *    new pairs in the set of pairs {f_(j+1) = v and f_i = v_i for 1 <= i <= j}.
 *    Then, let v_(j+1) be one of the values that appeared in
 *    the greatest number of new pairs.
 *    Note that, in this step, each parameter value is considered
 *    only once for inclusion in a candidate test case.
 *    Also, that when choosing a value for parameter f_(j+1) ,
 *    the possible values are compared with only the j
 *    values already chosen for parameters f_1 , ..., f_j .
 *
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
  @SuppressWarnings("FieldCanBeLocal")
  private static int TRIES = 50;
  private final int  strength;
  private final Random random;

  public AetgCoveringArrayEngine(
      @Param(source = Param.Source.CONFIG, defaultValue = "2") int strength,
      @Param(source = Param.Source.CONFIG, defaultValue = "2") int randomSeed
  ) {
    this.strength = strength;
    this.random = new Random(randomSeed);
  }

  @Override
  protected List<Tuple> generate(Factors factors, final ConstraintBundle constraintBundle) {
    List<Tuple> allPossibleTuples = new LinkedList<Tuple>();
    allPossibleTuples.addAll(factors.generateAllPossibleTuples(this.strength, new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        try {
          ////
          // SmartConstraintChecker is stateful. I need to come up with a solution
          // to handle it seamlessly with the other checkers.
          // Or it maybe is a responsibility of a caller.
          return constraintBundle.newConstraintChecker().check(in);
        } catch (UndefinedSymbol undefinedSymbol) {
          ////
          // If constraintChecker is present and throws an undefined symbol, the tuple
          // cannot be removed. In this case we should return true.
          return true;
        }
      }
    }));
    List<Tuple> ret = new LinkedList<Tuple>();
    Set<Tuple> remainingTuples = new HashSet<Tuple>(allPossibleTuples);
    long numTries;
    Enumerator<String> factorNames;
    ////
    // 14! > Integer.MAX_VALUE > 13! : If we have 14 or more factors, possible permutations will become
    // larger than Integer.MAX_VALUE, which is equal to or larger than TRIES,
    if (factors.size() >= 14 || TRIES > com.github.dakusui.combinatoradix.Utils.nPk(factors.size(), factors.size())) {
      numTries = TRIES;
      factorNames = Enumerators.shuffler(
          Utils.transform(
              factors, new Utils.Form<Factor, String>() {
                @Override
                public String apply(Factor in) {
                  return in.name;
                }
              }
          ),
          TRIES,
          this.random
      );
    } else {
      factorNames = new Permutator<String>(
          Utils.transform(factors, new Utils.Form<Factor, String>() {
            @Override
            public String apply(Factor in) {
              return in.name;
            }
          }),
          factors.size()
      );
      numTries = factorNames.size();
    }

    while (!remainingTuples.isEmpty()) {
      int newlyCoveredTuples = -1; // If no new tuple can be covered, new test case shouldn't be added.
      Tuple chosenTestCase = null;
      Map<String, List<Object>> factorsMap = createFactorsMap(factors);
      for (int i = 0; i < numTries; i++) {
        Tuple newTestCaseCandidate = createNewTestCase(
            factorsMap,
            this.strength,
            remainingTuples,
            factorNames.get(i),
            null /* Right now "allRandom" mode is suppressed. To enable it, give "this.random" instead of null. */
        );
        int numCoveredByNewCandidate = countTuplesNewlyCoveredBy(newTestCaseCandidate, remainingTuples, strength);
        if (numCoveredByNewCandidate > newlyCoveredTuples) {
          newlyCoveredTuples = numCoveredByNewCandidate;
          chosenTestCase = newTestCaseCandidate;
        }
      }

      if (chosenTestCase == null) {
        Checks.checkcond(remainingTuples.isEmpty());
        ////
        // Time to give up;
        return ret;
      }

      if (!remainingTuples.removeAll(TupleUtils.subtuplesOf(chosenTestCase, strength))) {
        ////
        // Give up. Because coverage didn't get any better.
        System.out.println("[exception] chosenTestCase doesn't cover more tuples.");
      }

      ret.add(chosenTestCase);
    }

    return ret;
  }

  private Map<String, List<Object>> createFactorsMap(Factors factors) {
    Map<String, List<Object>> ret = new HashMap<String, List<Object>>();
    for (Factor each : factors) {
      ret.put(each.name, new LinkedList<Object>(each.levels));
    }
    return ret;
  }


  private static Map<String, Object> selectFirstFactorVal(Map<String, ? extends List<?>> factors, Set<Tuple> remainingTuples, List<String> orderedFactorNames) {
    Map<Map<String, Object>, Integer> factorValUncoveredCnt = new HashMap<Map<String, Object>, Integer>();
    for (String eachFactorName : orderedFactorNames) {
      for (Object eachValue : factors.get(eachFactorName)) {
        Map<String, Object> factorVal = new HashMap<String, Object>();
        factorVal.put(eachFactorName, eachValue);
        factorValUncoveredCnt.put(factorVal, 0);
      }
    }

    for (Tuple eachRemainingTuple : remainingTuples) {
      for (String eachFactorName : orderedFactorNames) {
        Object Value = eachRemainingTuple.get(eachFactorName);
        if (null != Value) {
          Map<String, Object> factorVal = new HashMap<String, Object>();
          factorVal.put(eachFactorName, Value);
          factorValUncoveredCnt.put(factorVal, factorValUncoveredCnt.get(factorVal) + 1);
        }
      }
    }

    Map<String, Object> mostUncoveredFactorVal = new HashMap<String, Object>();
    int mostUncoveredCnt = 0;
    for (String eachFactorName : orderedFactorNames) {
      for (Object eachValue : factors.get(eachFactorName)) {
        Map<String, Object> factorVal = new HashMap<String, Object>();
        factorVal.put(eachFactorName, eachValue);
        int uncoveredCnt = factorValUncoveredCnt.get(factorVal);
        if (uncoveredCnt > mostUncoveredCnt) {
          mostUncoveredCnt = uncoveredCnt;
          mostUncoveredFactorVal = factorVal;
        }
      }
    }

    return mostUncoveredFactorVal;
  }

  private static Tuple createNewTestCase(Map<String, ? extends List<?>> factors, int strength, Set<Tuple> remainingTuples, List<String> orderedFactorNames, Random random) {

    Tuple.Builder builder = new Tuple.Builder();

    /* step 1): choose a parameter f and a value l for f such that that parameter value appears in the greatest number of uncovered pairs.
      i.e., among the "remainingTuples", look for the factor value appears in the greatest number */
    if (random != null) {
      Map<String, Object> mostUncoveredFactorVal = selectFirstFactorVal(factors, remainingTuples, orderedFactorNames);
      String mostUncoveredFactor = mostUncoveredFactorVal.keySet().iterator().next();
      builder.put(mostUncoveredFactor, mostUncoveredFactorVal.get(mostUncoveredFactor));
      orderedFactorNames.remove(mostUncoveredFactor);
      Collections.shuffle(orderedFactorNames, random);
    }

    /* steps 2)+3): select values for the remaining factors in the list */
    for (String eachFactorName : orderedFactorNames) {
      int newlyCoveredTuples = -1;
      Object valueForCurrentFactor = null;

      for (Object eachValue : factors.get(eachFactorName)) {
        builder.put(eachFactorName, eachValue);
        Tuple newTuple = builder.build();
        int coveredByCurrentTuple = countTuplesNewlyCoveredBy(newTuple, remainingTuples, strength);
        if (coveredByCurrentTuple > newlyCoveredTuples) {
          newlyCoveredTuples = coveredByCurrentTuple;
          valueForCurrentFactor = eachValue;
        }
        //        else if (coveredByCurrentTuple == newlyCoveredTuples)  // to increase randomness in cases different choices lead to same # of uncovered tuples
        //        {
        //        	Random random = new Random();
        //            if(random.nextBoolean()) // true: exchange to the current selection
        //            	valueForCurrentFactor = eachValue;
        //        }
      }

      builder.put(eachFactorName, valueForCurrentFactor);
    }
    return builder.build();
  }

  private static int countTuplesNewlyCoveredBy(Tuple tuple, Set<Tuple> tuplesYetToBeCovered, int strength) {
    int ret = 0;
    if (tuple.size() < strength) {
      // Even when the tuple size is less than strength, its potential to cover remaining tuples matters.
      for (Tuple eachTuple : tuplesYetToBeCovered) {
        for (Tuple eachSubtuple : TupleUtils.subtuplesOf(eachTuple, tuple.size())) {
          if (tuple.equals(eachSubtuple))
            ret++;
        }
      }
    } else {
      for (Tuple eachSubtuple : TupleUtils.subtuplesOf(tuple, strength)) {
        if (tuplesYetToBeCovered.contains(eachSubtuple)) {
          ret++;
        }
      }
    }

    return ret;
  }

}
