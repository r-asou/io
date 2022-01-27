package rsb.synchronicity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FibonacciCalculator {

	@SneakyThrows
	public static long[] calculateSeries(int num) {
		Thread.sleep(1000);
		var i = 1;
		var t1 = 0;
		var t2 = 1;
		var results = new long[num + 1];
		while (i <= num) {
			int sum = t1 + t2;
			results[i] = sum;
			t1 = t2;
			t2 = sum;
			i++;
		}
		return results;

	}

}
