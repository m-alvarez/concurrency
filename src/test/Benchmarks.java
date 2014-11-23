/**
 * A JUnit test class with two test methods for implementations of the
 * Set interface.
 */

package test;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@BenchmarkOptions()
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmarks")
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY)
public class Benchmarks {
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

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

    private static final int NUM_STEPS = 10000;
    private static final int BOUND = 10000;
    private final SetFactory setFact_;
    private final List<Integer> testData_;
    /* IMPORTANT NUM_STEPS must be divisible by NUM_THREADS */
    private final int NUM_THREADS;
    private final int STEPS_PER_THREAD;
    private final int SLOWDOWN;

    public Benchmarks(String configuration, SetFactory setFact,
                      List<Integer> testData, int numThreads, int slowdown) {
        configuration_ = configuration;
        setFact_ = setFact;
        testData_ = testData;
        NUM_THREADS = numThreads;
        assert (NUM_STEPS % NUM_THREADS == 0);
        STEPS_PER_THREAD = NUM_STEPS / NUM_THREADS;
        SLOWDOWN = slowdown;
    }

    public void testStep(Set set, int a) {
        for (int h = 1; h * a <= BOUND; ++h) {
            set.add(h * a);
        }

        for (int k = 2; k * a <= BOUND; ++k) {
            if (set.member(k * a)) {
                set.remove(k * a);
            }
        }

        if (!isPrime(a)) {
            set.remove(a);
        }
    }

    @Test
    public void sequentialTest() {
        final Set set = setFact_.getInstance();
        final List<Integer> testData = testData_;

        Runnable code = new Runnable() {
            public void run() {
                int i = 0;
                for (Integer k : testData) {
                    try {
                        if (i % 5 == 0) {
                            Thread.sleep(0, SLOWDOWN);
                        }
                        testStep(set, k);
                        i++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Thread interrupted");
                    }
                }
            }
        };
        Thread thread = new Thread(code);

        thread.start();
        try {
            thread.join();
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
                    int i = 0;
                    for (Integer k : section) {
                        try {
                            if (i % 5 == 0) {
                                Thread.sleep(0, SLOWDOWN);
                            }
                            testStep(set, k);
                            i++;
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Thread interrupted");
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

    @Parameters
    public static Collection<Object[]> data() {
        // We want to perform the same tests on a sequential.SetList
        // instance and on a concurrent.SetList instance

        List<Integer> testData = new ArrayList<Integer>(NUM_STEPS);

        {
            Random g = new Random(0);
            for (int i = 0; i < NUM_STEPS; ++i) {
                testData.add(2 + g.nextInt(BOUND - 1));
            }  // generates n random numbers in [2,m]]
        }

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

        List<Object[]> arguments = new ArrayList<Object[]>();

        /*
        for (int slowdown : new int[]{0, 1}) {
            for (int numThreads : new int[]{1, 2, 4, 8}) { */
        String params = String.format(" threads %d slowdown %d", numThreads, slowdown);
        arguments.add(new Object[]{
                "synchronized" + params, setListFactory, testData, numThreads, slowdown
        });
        arguments.add(new Object[]{
                "transactional" + params, concurrentSetListFactory, testData, numThreads, slowdown
        });
        /*}
        }*/
        return arguments;
    }

    private static int numThreads, slowdown;

    /* Main method so we can create stand-alone jars */
    public static void main(String[] args) throws Exception {
        System.setProperty("jub.consumers", "CONSOLE,H2,XML");
        System.setProperty("jub.db.file", ".benchmarks");
        System.setProperty("jub.xml.file", "results");

        Benchmarks.numThreads = Integer.parseInt(args[0]);
        Benchmarks.slowdown = Integer.parseInt(args[1]);
        String name = String.format("Threads %d slowdown %d", numThreads, slowdown);
        System.setProperty("jub.customkey", name);
        JUnitCore.main("test.Benchmarks");
    }
}
