package timing;

public class Timer {
    public static double time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        long end = System.nanoTime();

        return (end - start) / 1000000.0;
    }

    public static double time(int trials, Runnable r) {
        double avg = 0;

        for (int i = 0; i < trials; i++) {
            avg += time(r) / trials;
        }

        return avg;
    }

    public static void main(String[] args) {


    }
}
