import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Main {
    public static void start(Class<?> aClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object instance = aClass.getConstructor().newInstance();
        Method[] methods = aClass.getMethods();
        Method[] edgeMethods = handleEdgeMethods(methods);
        edgeMethods[0].invoke(instance);
        Method[] testMethods = getSortedTestMethods(methods);
        for (Method method : testMethods) {
            method.invoke(instance);
        }
        edgeMethods[1].invoke(instance);
    }

    private static Method[] handleEdgeMethods(Method[] methods) {
        Method[] beforeAfterMethods = new Method[2];
        int beforeMethodsCount = 0;
        int afterMethodsCount = 0;

        for (Method method : methods) {
            if (method.getAnnotation(BeforeSuite.class) != null) {
                beforeAfterMethods[0] = method;
                beforeMethodsCount++;
            }
            if (method.getAnnotation(AfterSuite.class) != null) {
                beforeAfterMethods[1] = method;
                afterMethodsCount++;
            }
        }
        if (afterMethodsCount > 1 || beforeMethodsCount > 1) {
            throw new RuntimeException();
        }
        return beforeAfterMethods;
    }

    private static Method[] getSortedTestMethods(Method[] methods) {
        Map<Integer, List<Method>> methodMap = new TreeMap<>();

        for (Method method : methods) {
            Test test = (method.getAnnotation(Test.class));
            if (test != null) {
                int priority = test.getPriority();
                if (methodMap.containsKey(priority)) {
                    methodMap.get(priority).add(method);
                } else {
                    List<Method> list = new ArrayList<>();
                    list.add(method);
                    methodMap.put(priority, list);
                }
            }
        }
        List<Method> linkedMethods = new LinkedList<>();
        for (Map.Entry<Integer, List<Method>> entry : methodMap.entrySet()) {
            linkedMethods.addAll(entry.getValue());
        }

        Method[] ret = new Method[linkedMethods.size()];
        ret = linkedMethods.toArray(ret);
        return ret;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeSuite {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterSuite {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Test {
        int getPriority();
    }
}
