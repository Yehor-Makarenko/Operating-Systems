package lab1.functions;

import java.io.Serializable;

public class FunctionResult implements Serializable {
  private static final long serialVersionUID = 1L;
  private boolean isResult;
  private Double result = null;
  private FunctionError error = null;

  public FunctionResult(double result) {
    isResult = true;
    this.result = result;
  }

  public FunctionResult(FunctionError error) {
    isResult = false;
    this.error = error;
  }

  public boolean getIsResult() {
    return isResult;
  }

  public double getResult() {
    return result;
  }

  public FunctionError getError() {
    return error;
  }
}