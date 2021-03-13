package demo.jvm;

public class ForLoopTest{
  
  public static int[] numbers = {1,2,6};

  public static void main(String[] args) {
    MovingAvgrage ma = new MovingAvgrage();

    for(int number : numbers){
      ma.submit(number);
    }
    double avg = ma.getAvg();
  }

}
