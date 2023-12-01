package lab1.functions.function1;

import lab1.functions.Function;
import lab1.functions.FunctionComputation;

public class FunctionFComputation {    
  public static void main(String[] args) throws Exception {            
    int n = Integer.parseInt(args[0]);
    Function f = FunctionF::compfunc;
    FunctionComputation.compfunc(n, f, "f");
  }  
}