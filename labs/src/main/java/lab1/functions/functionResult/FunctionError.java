package lab1.functions.functionResult;

import java.io.Serializable;

public class FunctionError implements Serializable {
  private static final long serialVersionUID = 1L;
  private String functionName;
  private int nonCriticalCounter = 0;
  private boolean isCritical = false;
  private boolean isNonCriticalLimit = false;

  public FunctionError(String functionName) {
    this.functionName = functionName;
  }

  public void setIsCritical() {
    isCritical = true;
  }

  public void setIsNonCriticalLimit() {
    isNonCriticalLimit = true;
  }

  public void addNonCritical() {
    nonCriticalCounter++;
  }

  public int getNonCriticalCounter() {
    return nonCriticalCounter;
  }

  public void print() {
    if (isCritical) {
      System.out.println("Function " + functionName + ", critical error");
      return;
    }
    if (isNonCriticalLimit) {
      System.out.println("Function " + functionName + ", non critical errors limit: " + nonCriticalCounter + " errors");
      return;
    }
    System.out.println("Function " + functionName + ", non critical errors: " + nonCriticalCounter);
  }
}
