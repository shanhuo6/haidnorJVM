package haidnor.jvm.test.clazz;

public class Student extends Human {

    public static String school = "Hello World!";

    static {
        System.out.println(HUMAN_NAME);
        System.out.println("student 类被加载了");
    }

    public void method1() {
        System.out.println("method1");
        method2();
    }

    public void method2() {
        System.out.println("method2");
        method3();
    }

    public void method3() {
        System.out.println("method3");
    }

}
