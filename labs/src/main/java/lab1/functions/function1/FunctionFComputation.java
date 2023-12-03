package lab1.functions.function1;

import lab1.functions.Function;
import lab1.functions.FunctionComputation;

public class FunctionFComputation {    
  public static void main(String[] args) throws Exception {     
    String functionName = args[0];       
    Function f = FunctionF::compfunc;
    FunctionComputation.compfunc(f, functionName);
  }  
}