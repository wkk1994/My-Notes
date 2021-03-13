package demo.jvm;

public class MovingAvgrage{
  private int count = 0;

  private double sum = 0.0d;

  public void submit(double value) {
     this.count ++;
     this.sum += value;
  } 

  public double getAvg() {
     if(this.count == 0) {
       return this.sum; 
     }
     return this.sum / this.count;
  }
}

