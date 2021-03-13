package demo.java.bytecode;

public class HelloByteCode {
  public HelloByteCode() {
  }

  public HelloByteCode(String str) {
 
  }
  public static void main(String[] args) {
    HelloByteCode obj = new HelloByteCode("123");
  }

  public void A() {
    B("123");
  }

  public void B(String strB) {
    C();
  }

  public static void C() {

  }

}
