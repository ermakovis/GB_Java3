public class Test {

    @Main.BeforeSuite
    public void before() {
        System.out.println("before");
    }

    @Main.Test(getPriority = 1)
    public void test1() {
        System.out.println(1);
    }

    @Main.Test(getPriority = 3)
    public void test2() {
        System.out.println(3);
    }

    @Main.Test(getPriority = 2)
    public void test3() {
        System.out.println(2);
    }

    @Main.Test(getPriority = 1)
    public void test4() {
        System.out.println(1);
    }

    @Main.AfterSuite
    public void after() {
        System.out.println("after");
    }

    public static void main(String[] args) {
        try {
            Main.start(Test.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
