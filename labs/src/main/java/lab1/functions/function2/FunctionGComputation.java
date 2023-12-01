package lab1.functions.function2;

import lab1.functions.Function;
import lab1.functions.FunctionComputation;

public class FunctionGComputation {
  public static void main(String[] args) throws Exception {        
    int n = Integer.parseInt(args[0]);
    Function f = FunctionG::compfunc;
    FunctionComputation.compfunc(n, f, "g");
  }
}