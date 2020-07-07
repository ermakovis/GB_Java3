import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

public class Tests {
    @Test
    public void lastIndexOfSubarrayTestGeneric() {
        int num = 4;
        int[] array = {1, 1, 1, 1, 4, 1, 1};
        int[] result = {1, 1};

        Assert.assertArrayEquals(result, Main.lastIndexOfSubarray(array, num));
    }

    @Test
    public void lastIndexOfSubarrayTestNumFirst() {
        int num = 4;
        int[] array = {4, 1, 1, 1, 1, 1, 1};
        int[] result = {1, 1, 1, 1, 1, 1};

        Assert.assertArrayEquals(result, Main.lastIndexOfSubarray(array, num));
    }

    @Test
    public void lastIndexOfSubarrayTestNumLast() {
        int num = 4;
        int[] array = {1, 1, 1, 1, 1, 1, 4};
        int[] result = new int[0];

        Assert.assertArrayEquals(result, Main.lastIndexOfSubarray(array, num));
    }

    @Test (expected = NoSuchElementException.class)
    public void lastIndexOfSubarrayTestNoNum() {
        int num = 4;
        int[] array = {1, 1, 1, 1, 1, 1, 1};

        Main.lastIndexOfSubarray(array, num);
    }

    @Test
    public void containsGeneric() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {2, 3};

        Assert.assertTrue(Main.contains(array, ref));
    }

    @Test
    public void containsGeneric2() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {2, 5};

        Assert.assertTrue(Main.contains(array, ref));
    }

    @Test
    public void containsGenericEdge1() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {6};

        Assert.assertTrue(Main.contains(array, ref));
    }

    @Test
    public void containsGenericEdge2() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {1};

        Assert.assertTrue(Main.contains(array, ref));
    }

    @Test
    public void containsGenericFail1() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {7};

        Assert.assertFalse(Main.contains(array, ref));
    }

    @Test
    public void containsGenericFail2() {
        int[] array = {1, 2, 3, 4, 5, 6};
        int[] ref = {};

        Assert.assertFalse(Main.contains(array, ref));
    }

}
