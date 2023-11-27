package lab1.functions;

import java.io.Serializable;

public class FunctionResult implements Serializable {
  private static final long serialVersionUID = 1L;
  private boolean isResult;
  private double result;

  public FunctionResult(double result) {
    isResult = true;
    this.result = result;
  }

  public boolean getIsResult() {
    return isResult;
  }

  public double getResult() {
    return result;
  }
}