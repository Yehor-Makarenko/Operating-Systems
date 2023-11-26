package Java.Functions.Function2;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Function {
    final static int CASE1_ATTEMPTS = 3;
    static int attempt = CASE1_ATTEMPTS;
    public static Optional<Optional<Double>> compfunc(int n) { 
	switch (n) {
	case 0:
	    try { TimeUnit.SECONDS.sleep(2); }
	    catch (InterruptedException ie) { return Optional.of(Optional.empty()); }
	    return Optional.of(Optional.empty()); 

	case 1:
	    try { TimeUnit.SECONDS.sleep(1); }
	    catch (InterruptedException ie) { return Optional.of(Optional.empty()); }
	    attempt--;
	    if (attempt != 0)
		return Optional.empty();
	    attempt = CASE1_ATTEMPTS;
	    return Optional.of(Optional.of(5d));

	default:
	}

	try { Thread.currentThread().join(); } catch (InterruptedException ie) {} 
	return Optional.of(Optional.empty());
    }
}
