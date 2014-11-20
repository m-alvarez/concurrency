/**
 * A JUnit test class with two test methods for implementations of the
 * Set interface.
 */

package test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestRule;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

@RunWith(Parameterized.class)
@BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
public class SetTest {
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    /**
     * Number of test steps for each test (sequential and concurrent).
     * In the sequential test, the steps are executed sequentially.
     * In the concurrent test, the steps are executed in parallel.
     */
    private static final int NUM_STEPS = 10000;

    /**
     * Number of cores. Some versions of this file use this number to
     * determine the number of threads to run.
     */
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    /* IMPORTANT NUM_STEPS must be divisible by NUM_THREADS */
    private static final int NUM_THREADS = 4;
    private static final int STEPS_PER_THREAD = NUM_STEPS / NUM_THREADS;

    /**
     * Upper bound on integers that are used in test
     */
    private static final int m = 10000 ;

    /**
     * A set factory instance provides concrete objects that implement
     * the Set interface.
     */
    interface SetFactory {
        public Set getInstance();
    }

    /**
     * A string describing the current test.
     */
    private String configuration_;

    /**
     * A set factory that will provide the Set objects used for the
     * tests.
     */
    private SetFactory setFact_;
    private List<Integer> testData_;
    private List<Integer> expectedRes_;

    public SetTest(String configuration, SetFactory setFact,
                   List<Integer> testData, List<Integer> expectedRes) {
        configuration_ = configuration;
        setFact_ = setFact;
        testData_ = testData;
        expectedRes_ = expectedRes;
    }

    public void testStep(Set set, int a) {
        // add multiples <= m
        for (int h = 1; h * a <= m; ++h) {
            set.add(h * a);
        }

        // remove proper multiples <=m
        for (int k = 2; k * a <= m; ++k) {
            if (set.member(k * a)) {
                set.remove(k * a);
            }
        } //NB we only remove non-primes

        if (!isPrime(a)) {
            set.remove(a);
        } // remove a if not prime
    }

    @Test
    public void sequentialTest() {
        final Set set = setFact_.getInstance();
        final List<Integer> testData = testData_;

        Runnable code = new Runnable() {
            public void run() {
                for (Integer k : testData) {
                    try {
                        if (k%5 == 0) { Thread.sleep(0, 1); }
                        testStep(set, k);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(code);

        thread.start();
        try {
            thread.join();
            assertEquals(configuration_, expectedRes_, set.asList());
        } catch (InterruptedException e) {
            fail(configuration_ + ": a thread was interrupted");
        }
    }

    @Test
    public void concurrentTest() {
        final Set set = setFact_.getInstance();
        List<Thread> threads = new ArrayList<Thread>(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; ++i) {
            final List<Integer> section = testData_.subList(i * STEPS_PER_THREAD, (i + 1) * STEPS_PER_THREAD);
            Runnable code = new Runnable() {
                public void run() {
                    for (Integer i : section) {
                        try {
                            if (i % 5 == 0) { Thread.sleep(0, 1); }
                            testStep(set, i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            threads.add(new Thread(code));
        }
        for (Thread t : threads) {
            t.start();
        }
        try {
            for (Thread t : threads) {
                t.join();
            }
            assertEquals(configuration_, expectedRes_, set.asList());
        } catch (InterruptedException e) {
            fail(configuration_ + ": a thread was interrupted");
        }
    }

    private static boolean isPrime(int a) {
        for (int i = 2; i * i <= a; ++i) {
            if (a % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> expectedResult(List<Integer> testData) {
        SortedSet<Integer> set = new TreeSet<Integer>();
        for (int i = 0; i < NUM_STEPS; ++i) {
            if (isPrime(testData.get(i))) {
                set.add(testData.get(i));
            }
        }

        List<Integer> l = new ArrayList<Integer>(set.size());

        for (int elt : set) {
            l.add(elt);
        }

        return l;
    }

    @Parameters
    public static Collection<Object[]> data() {
        // We want to perform the same tests on a sequential.SetList
        // instance and on a concurrent.SetList instance

        List<Integer> testData = new ArrayList<Integer>(NUM_STEPS);

        {
            Random g = new Random();
            for (int i = 0; i < NUM_STEPS; ++i) {
                testData.add(2 + g.nextInt(m - 1));
            }  // generates n random numbers in [2,m]]
        }

        List<Integer> expectedRes = expectedResult(testData);

        SetFactory setListFactory = new SetFactory() {
            public Set getInstance() {
                return new sequential.SetList();
            }
        };

        SetFactory concurrentSetListFactory = new SetFactory() {
            public Set getInstance() {
                return new concurrent.SetList();
            }
        };

        return Arrays.asList(new Object[][]{
                {"Using sequential SetList factory", setListFactory, testData, expectedRes},
                {"Using concurrent SetList factory", concurrentSetListFactory, testData, expectedRes}
        });
    }

    /* Main method so we can create stand-alone jars */
    public static void main(String[] args) throws Exception {
        JUnitCore.main("test.SetTest");
    }
}
