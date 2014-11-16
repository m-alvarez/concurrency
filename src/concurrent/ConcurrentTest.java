package concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentTest {
    private static final int NUM_RUNS = 10000;
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final int NUM_THREADS = 2 * NUM_CORES;
    private static final int ELEMENTS_PER_THREAD = 100;
    private static final int NUM_ELEMENTS = ELEMENTS_PER_THREAD * NUM_THREADS;

    private static void box(String text) {
        String borders = "*=" + new String(new char[text.length()]).replace("\0", "=") + "=*";
        System.out.println(borders);
        System.out.println("| " + text + " |");
        System.out.println(borders);
    }

    private static class Inserter extends Thread {
        private final SetList s;
        private final List<Integer> numbers;
        public int errors = 0;
        public Inserter(SetList s, List<Integer> numbers) {
            this.s = s;
            this.numbers = numbers;
        }
        @Override
        public void run() {
            for (Integer i : this.numbers) {
                if (!this.s.add(i)) {
                    this.errors++;
                }
            }
        }
    }

    public static void insertTest() throws InterruptedException {
        boolean errors = false;
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
                if (t.errors != 0) {
                    System.out.println("Error: a thread could not insert all its elements");
                    errors = true;
                }
            }

            int numMissing = 0;
            for (Integer i : numbers) {
                if (!s.member(i)) {
                    System.out.println("Number " + i + " is missing");
                    numMissing++;
                    errors = true;
                }
            }
            if (numMissing != 0) {
                box("Problem: " + numMissing + " elements missing in run " + run);
            }
        }
        if (errors) {
            box("EXECUTION OF INSERTION TEST ENDS WITH ERRORS");
        } else {
            box("Execution of insertion test ends correctly");
        }
    }

    private static class Remover extends Thread {
        private final SetList s;
        private final List<Integer> numbers;
        public int errors = 0;
        public Remover(SetList s, List<Integer> numbers) {
            this.s = s;
            this.numbers = numbers;
        }
        @Override
        public void run() {
            for (Integer i : this.numbers) {
                if (!this.s.remove(i)) {
                    this.errors++;
                }
            }
        }
    }

    public static void removeTest() throws InterruptedException {
        boolean errors = false;
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
                if (t.errors != 0) {
                    System.out.println("Error: a thread could not remove all its elements");
                    errors = true;
                }
            }

            int numPresent = 0;
            for (Integer i : numbers) {
                if (s.member(i)) {
                    System.out.println("Number " + i + " is present");
                    numPresent++;
                    errors = true;
                }
            }
            if (numPresent != 0) {
                box("Problem: " + numPresent + " elements present in run " + run);
            }
        }
        if (errors) {
            box("EXECUTION OF REMOVAL TEST ENDS WITH ERRORS");
        } else {
            box("Execution of removal test ends correctly");
        }
    }

    private static void insertRemoveTest() throws InterruptedException {
        boolean errors = false;
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

            List<Inserter> insertThreads = new ArrayList<Inserter>(NUM_THREADS);
            List<Remover> removeThreads = new ArrayList<Remover>(NUM_THREADS);
            for (int i = 0; i < NUM_THREADS; i++) {
                List<Integer> removeNumbers =
                        numbers.subList(i * ELEMENTS_PER_THREAD, (i + 1) * ELEMENTS_PER_THREAD);
                List<Integer> insertNumbers =
                        numbers.subList(NUM_ELEMENTS + i * ELEMENTS_PER_THREAD,
                                NUM_ELEMENTS + (i + 1) * ELEMENTS_PER_THREAD);
                insertThreads.add(new Inserter(s, insertNumbers));
                removeThreads.add(new Remover(s, removeNumbers));
            }
            List<Thread> threads = new ArrayList<Thread>(2 * NUM_THREADS);
            threads.addAll(insertThreads);
            threads.addAll(removeThreads);
            Collections.shuffle(threads);

            for (Thread t : threads) {
                t.start();
            }
            for (Inserter t : insertThreads) {
                t.join();
                if (t.errors != 0) {
                    System.out.println("Error: a thread could not insert all its elements");
                    errors = true;
                }
            }
            for (Remover t : removeThreads) {
                t.join();
                if (t.errors != 0) {
                    System.out.println("Error: a thread could not remove all its elements");
                    errors = true;
                }
            }

            int numPresent = 0;
            int numMissing = 0;
            for (int i = 0; i < NUM_ELEMENTS; i++) {
                if (s.member(numbers.get(i))) {
                    System.out.println("Number " + numbers.get(i) + " is present");
                    numPresent++;
                    errors = true;
                }
                if (!s.member(numbers.get(i + NUM_ELEMENTS))) {
                    System.out.println("Number " + numbers.get(i + NUM_ELEMENTS) + " is missing");
                    numMissing++;
                    errors = true;
                }
            }
            if (numPresent != 0) {
                box("Problem: " + numPresent + " elements present in run " + run);
            }
            if (numMissing != 0) {
                box("Problem: " + numMissing + " elements missing in run " + run);
            }
        }
        if (errors) {
            box("EXECUTION OF INSERT/REMOVE TEST ENDS WITH ERRORS");
        } else {
            box("Execution of insert/remove test ends correctly");
        }

    }

    public static void main(String[] args) throws InterruptedException {
        insertTest();
        removeTest();
        insertRemoveTest();
    }
}
