package lab1.functions.functionResult;

import java.io.Serializable;

public class FunctionResult implements Serializable {
  private static final long serialVersionUID = 1L;
  String functionName;
  private boolean isComputed;
  private boolean isResult;
  private Double result = null;
  private FunctionError error = null;

  public FunctionResult(String functionName, double result, FunctionError error, boolean isComputed) {
    this.functionName = functionName;
    this.isComputed = isComputed;
    isResult = true;
    this.result = result;
    this.error = error;
  }

  public FunctionResult(String functionName, FunctionError error, boolean isComputed) {
    this.functionName = functionName;
    this.isComputed = isComputed;
    isResult = false;
    this.error = error;
  }

  public String getFunctionName() {
    return functionName;
  }

  public boolean hasResult() {
    return isResult;
  }

  public double getResult() {
    return result;
  }

  public FunctionError getError() {
    return error;
  }

  public boolean getIsComputed() {
    return isComputed;
  }
}