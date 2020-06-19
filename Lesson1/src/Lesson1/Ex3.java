package Lesson1;

import java.util.ArrayList;

public class Ex3 {
    public abstract static class Fruit {
        public abstract float getWeight();
    }

    public static class Orange extends Fruit {
        public float getWeight() {
            return 1.5f;
        }
    }

    public static class Apple extends Fruit {
        public float getWeight() {
            return 1f;
        }
    }

    public static class Box<T extends Fruit> {
        private final ArrayList<T> content = new ArrayList<>();

        public ArrayList<T> getContent() {
            return this.content;
        }

        public void put(T element) {
            content.add(element);
        }

        public float getWeight() {
            if (content.size() == 0) {
                return 0f;
            }

            float ret = 0f;
            for (Fruit elem : content) {
                ret += elem.getWeight();
            }
            return ret;
        }

        public boolean compare(Box box) {
            return this.getWeight() == box.getWeight();
        }

        public void combine(Box<T> box) {
            content.addAll(box.getContent());
            box.getContent().clear();
        }
    }

    public static void main(String[] args) {
        Box<Apple> appleBox = new Box<>();
        appleBox.put(new Apple());
        appleBox.put(new Apple());


        Box<Apple> appleBox2 = new Box<>();
        appleBox2.put(new Apple());

        Box<Orange> orangeBox = new Box<>();
        orangeBox.put(new Orange());
        orangeBox.put(new Orange());

        Box<Fruit> fruitBox = new Box<>();
        fruitBox.put(new Apple());
        fruitBox.put(new Apple());
        fruitBox.put(new Orange());
        fruitBox.put(new Orange());

        System.out.println("Before Combine");
        System.out.printf("AppleBox1 weight - %f, AppleBox2 weight - %f\n",
                appleBox.getWeight(), appleBox2.getWeight());
        appleBox.combine(appleBox2);
        System.out.println("After combine");
        System.out.printf("AppleBox1 weight - %f, AppleBox2 weight - %f\n",
                appleBox.getWeight(), appleBox2.getWeight());

        System.out.printf("AppleBox weight = %f | OrangeBox weight = %f\n",
                appleBox.getWeight(), orangeBox.getWeight());
        System.out.printf("Are boxes equal? %s\n", appleBox.compare(orangeBox) ? "yes" : "no");

        System.out.printf("FruitBox weight = %f\n", fruitBox.getWeight());

    }
}
