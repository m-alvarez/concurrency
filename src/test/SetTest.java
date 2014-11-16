/**
 * A JUnit test class with two test methods for implementations of the
 * Set interface.
 */

package test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestRule;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class SetTest
{
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    /**
     * Number of test steps for each test (sequential and concurrent).
     * In the sequential test, the steps are executed sequentially.
     * In the concurrent test, the steps are executed in parallel.
     */
    private static final int n = 100;

    /**
     * Upper bound on integers that are used in tests.
     */
    private static final int m = 1000;

    /**
     * A set factory instance provides concrete objects that implement
     * the Set interface.
     */
    interface SetFactory
    {
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
    private int[] testData_;
    private List<Integer> expectedRes_;

    public SetTest(String configuration, SetFactory setFact,
                   int[] testData, List<Integer> expectedRes)
    {
        configuration_ = configuration;
        setFact_ = setFact;
        testData_ = testData;
        expectedRes_ = expectedRes;
    }

    public void testStep(Set set, int a)
    {
        // add multiples <= m
        for (int h = 1; h * a <= m; ++h) {
            set.add(h * a);
        }

        // remove proper multiples <=m
        for (int k = 2; k * a <= m; ++k) {
            if (set.member(k*a)) {
                set.remove(k*a);
            }
        } //NB we only remove non-primes

        if (!isPrime(a)) {
            set.remove(a);
        } // remove a if not prime
    }

    @Test
    public void sequentialTest()
    {
        Set set = setFact_.getInstance();
        for (int i = 0; i < n; ++i) {
            testStep(set, testData_[i]);
        }
        assertEquals(configuration_, expectedRes_, set.asList());
    }

    @Test
    public void concurrentTest()
    {
        final Set set = setFact_.getInstance();
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; ++i) {
            final int a = testData_[i];
            Runnable code = new Runnable() {
                    public void run() {
                        testStep(set, a);
                    }
                };
            threads[i] = new Thread(code);
        }
        for (int i = 0; i < n; ++i) {
            threads[i].start();
        }
        try {
            for (int i = 0; i < n; ++i) {
                threads[i].join();
            }
            assertEquals(configuration_, expectedRes_, set.asList());
        }
        catch (InterruptedException e) {
            fail(configuration_ + ": a thread was interrupted");
        }
    }

    public static boolean isPrime(int a)
    {
	for (int i = 2; i * i <= a; ++i) {
            if (a % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> expectedResult(int[] testData)
    {
        TreeSet<Integer> set = new TreeSet<Integer>();
        for (int i = 0; i < n; ++i) {
            if (isPrime(testData[i])) {
                set.add(testData[i]);
            }
        }

        List<Integer> l = new ArrayList<Integer>();

        {
            Iterator<Integer> iter = set.iterator();
            while (iter.hasNext()) {
                l.add(iter.next());
            }
        }

        return l;
    }

    @Parameters
    public static Collection<Object[]> data()
    {
        // We want to perform the same tests on a sequential.SetList
        // instance and on a concurrent.SetList instance

        int[] testData = new int[n];

        {
            Random g = new Random();
            for (int i = 0; i < n; ++i) {
                testData[i] = 2 + g.nextInt(m-1);
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

        return Arrays.asList(new Object[][] {
                { "Using sequential SetList factory", setListFactory, testData, expectedRes },
                { "Using concurrent SetList factory", concurrentSetListFactory, testData, expectedRes }
            });
    }
}
