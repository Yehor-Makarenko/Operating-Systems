package lab1.functions;

public class FunctionError {
  private String functionName;
  private int nonCriticalCounter = 0;
  private boolean isCritical = false;
  private boolean isNonCriticalLimit = false;

  public FunctionError(String functionName) {
    this.functionName = functionName;
  }

  public void setCritical() {
    isCritical = true;
  }

  public void setNonCriticalLimit() {
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
