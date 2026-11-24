import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class ParallelMonteCarloPi {

    public static final long DEFAULT_ITERATIONS = 1_000_000_000L;

    public static class MonteCarloWorker extends Thread {
        private final long iterations;
        private long countInside = 0;

        public MonteCarloWorker(long iterations) {
            this.iterations = iterations;
        }

        @Override
        public void run() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            long inside = 0;
            for (long i = 0; i < iterations; i++) {
                double x = random.nextDouble();
                double y = random.nextDouble();
                if (x * x + y * y <= 1.0) {
                    inside++;
                }
            }
            this.countInside = inside;
        }

        public long getCountInside() {
            return countInside;
        }
    }

    public static void main(String[] args) {
        int threads = (args.length > 0) ? Integer.parseInt(args[0]) : Runtime.getRuntime().availableProcessors();
        long iterations = DEFAULT_ITERATIONS;
        if (args.length > 1) {
            iterations = Long.parseLong(args[1]);
        }

        long baseIterations = iterations / threads;
        long remainder = iterations % threads;

        MonteCarloWorker[] workers = new MonteCarloWorker[threads];
        long startTime = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            long iters = baseIterations + (i < remainder ? 1 : 0);
            workers[i] = new MonteCarloWorker(iters);
            workers[i].start();
        }

        long totalInside = 0;
        for (int i = 0; i < threads; i++) {
            try {
                workers[i].join();
                totalInside += workers[i].getCountInside();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        double pi = 4.0 * totalInside / (double) iterations;

        System.out.printf(Locale.US, "PI is %.5f%n", pi);
        System.out.println("THREADS " + threads);
        System.out.printf(Locale.US, "ITERATIONS %,d%n", iterations);
        System.out.printf(Locale.US, "TIME %.2fms%n", timeMs);
    }
}
