package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.math.BigInteger;
import java.util.function.Supplier;

@Slf4j
record SyncRunner(LongRunningAlgorithm algorithm, int max) {

	// <1>
	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		Timer.before("calculate");
		var results = ((Supplier<BigInteger>) () -> algorithm.calculate(this.max)).get();
		Timer.after("calculate");
		Timer.result("calculate", results);
	}

}
