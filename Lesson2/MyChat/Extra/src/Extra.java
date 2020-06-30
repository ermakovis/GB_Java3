public class Extra {
    public static final char LETTER_START = 'A';
    public static final char LETTER_FINISH = 'C';

    public static class letterPrinter implements Runnable {
        private final char letter;

        public letterPrinter (char letter) {
            this.letter = letter;
        }

        @Override
        public void run() {
            synchronized (Monitor.class) {
                try {
                    for (int i = 0; i < 5; i++ ) {
                        while (!Monitor.checkPhase(letter)) {
                            Monitor.class.wait();
                        }
                        System.out.println(letter);
                        Monitor.nextPhase();
                        Monitor.class.notifyAll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Monitor {
        private static char phase = LETTER_START;

        public static boolean checkPhase(char letter) {
            return letter == phase;
        }

        public static void nextPhase() {
            if (phase != LETTER_FINISH) {
                ++phase;
            } else {
                phase = LETTER_START;
            }
        }

    }

    public static void main(String[] args) {
        new Thread(new letterPrinter('A')).start();
        new Thread(new letterPrinter('B')).start();
        new Thread(new letterPrinter('C')).start();
    }
}
