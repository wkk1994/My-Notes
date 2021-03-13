package demo.jvm;

public class LocalVariableTest {
  
  public static void main(String[] args) {
    MovingAvgrage ma = new MovingAvgrage();
    int num1 = 1;
    int num2 = 2;
    ma.submit(num1);
    ma.submit(num2);
    double avg = ma.getAvg();
  }

}
