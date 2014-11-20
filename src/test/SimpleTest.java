package test;

import concurrent.SetList;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleTest {
    private static final int NUM_RUNS = 10000;
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final int NUM_THREADS = 2 * NUM_CORES;
    private static final int ELEMENTS_PER_THREAD = 100;
    private static final int NUM_ELEMENTS = ELEMENTS_PER_THREAD * NUM_THREADS;

    private static class Inserter extends Thread {
        private final SetList s;
        private final List<Integer> numbers;
        public Inserter(SetList s, List<Integer> numbers) {
            this.s = s;
            this.numbers = numbers;
        }
        @Override
        public void run() {
            for (Integer i : this.numbers) {
                if (!this.s.add(i)) {
                    throw new RuntimeException("Could not insert element");
                }
            }
        }
    }

    @Test
    public void insertTest() throws InterruptedException {
        for (int run = 0; run < NUM_RUNS; run++) {
            SetList s = new SetList();
            List<Integer> numbers = new ArrayList<Integer>(NUM_ELEMENTS);
            for (int i = 0; i < NUM_ELEMENTS; i++) {
                numbers.add(i);
            }
            Collections.shuffle(numbers);

            List<Inserter> threads = new ArrayList<Inserter>(NUM_THREADS);
            for (int i = 0; i < NUM_THREADS; i++) {
                List<Integer> threadNumbers = numbers.subList(i * ELEMENTS_PER_THREAD, (i + 1) * ELEMENTS_PER_THREAD);
                threads.add(new Inserter(s, threadNumbers));
            }

            for (Inserter t : threads) {
                t.start();
            }
            for (Inserter t : threads) {
                t.join();
            }

            for (Integer i : numbers) {
                if (!s.member(i)) {
                    throw new RuntimeException("List is missing elements");
                }
            }
        }
    }

    private static class Remover extends Thread {
        private final SetList s;
        private final List<Integer> numbers;
        public Remover(SetList s, List<Integer> numbers) {
            this.s = s;
            this.numbers = numbers;
        }
        @Override
        public void run() {
            for (Integer i : this.numbers) {
                if (!this.s.remove(i)) {
                    throw new RuntimeException("Could not remove element");
                }
            }
        }
    }

    @Test
    public void removeTest() throws InterruptedException {
        for (int run = 0; run < NUM_RUNS; run++) {
            SetList s = new SetList();
            List<Integer> numbers = new ArrayList<Integer>(NUM_ELEMENTS);
            for (int i = 0; i < NUM_ELEMENTS; i++) {
                numbers.add(i);
                s.add(i);
            }
            Collections.shuffle(numbers);

            List<Remover> threads = new ArrayList<Remover>(NUM_THREADS);
            for (int i = 0; i < NUM_THREADS; i++) {
                List<Integer> threadNumbers = numbers.subList(i * ELEMENTS_PER_THREAD, (i + 1) * ELEMENTS_PER_THREAD);
                threads.add(new Remover(s, threadNumbers));
            }

            for (Remover t : threads) {
                t.start();
            }
            for (Remover t : threads) {
                t.join();
            }

            for (Integer i : numbers) {
                if (s.member(i)) {
                    throw new RuntimeException("List has too many elements");
                }
            }
        }
    }

    @Test
    public void insertRemoveTest() throws InterruptedException {
        for (int run = 0; run < NUM_RUNS; run++) {
            SetList s = new SetList();
            List<Integer> numbers = new ArrayList<Integer>(2 * NUM_ELEMENTS);
            for (int i = 0; i < 2 * NUM_ELEMENTS; i++) {
                numbers.add(i);
            }
            Collections.shuffle(numbers);
            for (int i = 0; i < NUM_ELEMENTS; i++) {
                s.add(numbers.get(i));
            }

            List<Thread> threads = new ArrayList<Thread>(NUM_THREADS);

            for (int i = 0; i < NUM_THREADS; i++) {
                List<Integer> removeNumbers =
                        numbers.subList(i * ELEMENTS_PER_THREAD, (i + 1) * ELEMENTS_PER_THREAD);
                List<Integer> insertNumbers =
                        numbers.subList(NUM_ELEMENTS + i * ELEMENTS_PER_THREAD,
                                NUM_ELEMENTS + (i + 1) * ELEMENTS_PER_THREAD);
                threads.add(new Inserter(s, insertNumbers));
                threads.add(new Remover(s, removeNumbers));
            }
            Collections.shuffle(threads);

            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }

            for (int i = 0; i < NUM_ELEMENTS; i++) {
                if (s.member(numbers.get(i))) {
                    throw new RuntimeException("List has too many elements");
                }
                if (!s.member(numbers.get(i + NUM_ELEMENTS))) {
                    throw new RuntimeException("List is missing elements");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JUnitCore.main("test.SimpleTest");
    }
}
