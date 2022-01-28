package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Supplier;

import static rsb.synchronicity.Utils.logAfter;
import static rsb.synchronicity.Utils.logBefore;

@Slf4j
@Component
record SyncRunner(FibonacciService fibonacci) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		execute("calculate", () -> fibonacci.calculate(60));
	}

	private void execute(String func, Supplier<long[]> supplier) {
		logBefore(func);
		var results = supplier.get();
		log.info(func + " : " + results.length + " : " + Arrays.toString(results));
		logAfter(func);
	}

}
