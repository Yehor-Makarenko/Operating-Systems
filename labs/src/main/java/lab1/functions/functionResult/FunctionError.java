package lab1.functions.functionResult;

import java.io.Serializable;

public class FunctionError implements Serializable {
  private static final long serialVersionUID = 1L;
  private int nonCriticalCounter = 0;
  private boolean isCritical = false;
  private boolean isNonCriticalLimit = false;

  public boolean getIsCritical() {
    return isCritical;
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

  @Override
  public String toString() {    
    if (isCritical) {      
      return "\nError: critical error";
    }
    if (isNonCriticalLimit) {      
      return "\nError: non critical errors limit, " + nonCriticalCounter + " errors";
    }
    return "\nError: no critical error. Non critical errors: " + nonCriticalCounter;
  }
}
