import java.util.Arrays;
import java.util.NoSuchElementException;

public class Main {

    private static int lastIndexOf(int[] array, int num) {
        for (int i = array.length - 1; i >=0; i--) {
            if (array[i] == num) {
                return i;
            }
        }
        return -1;
    }

    private static int[] subArray(int[] array, int from) {
        if (from == array.length - 1) {
            return new int[0];
        }
        return Arrays.copyOfRange(array, from + 1, array.length);
    }

    public static int[] lastIndexOfSubarray(int[] array, int num) {
        int lastIndex = 0;

        if ((lastIndex = lastIndexOf(array, num)) == -1) {
            throw new NoSuchElementException();
        }
        return subArray(array, lastIndex);
    }

    public static boolean contains(int[] array, int[] ref) {
        int trueCount = 0;

        for (int i = 0; i < ref.length; i++) {
            if (lastIndexOf(array, ref[i]) != -1) {
                trueCount++;
            }
        }
        return trueCount != 0 && trueCount == ref.length;
    }
}
