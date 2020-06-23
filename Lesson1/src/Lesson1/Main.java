package Lesson1;

import java.util.ArrayList;

public class Main {
    public static <T> void print(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    //Написать метод, который меняет два элемента массива местами.(массив может быть любого ссылочного типа);
    public static <T> void swap(T[] array) {
        if (array.length < 2) {
            return;
        }
        T temp = array[0];
        array[0] = array[1];
        array[1] = temp;
    }

    //Написать метод, который преобразует массив в ArrayList;
    public static <T> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> ret = new ArrayList<>();
        for (T element : array) {
            ret.add(element);
        }
        return ret;
    }

    public static void main(String[] args) {
        Integer[] array = {1, 2, 3, 4, 5};
        print(array);
        swap(array);
        print(array);

        ArrayList<Integer> list = toArrayList(array);
        for (Integer integer : list) {
            System.out.print(integer + "_");
        }
        System.out.println();


    }
}
