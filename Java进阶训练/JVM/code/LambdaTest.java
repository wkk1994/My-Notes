package demo.jvm;

import java.lang.Runnable;

public class LambdaTest {
    public static void main(String[] args) {
        Runnable r = () -> System.out.println("123");
        r.run();
    }
}
