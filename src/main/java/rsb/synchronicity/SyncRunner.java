package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Supplier;

import static rsb.synchronicity.Utils.logAfter;
import static rsb.synchronicity.Utils.logBefore;

@Component @Slf4j
record SyncRunner (Fibonacci fibonacci) {

    @EventListener(ApplicationReadyEvent.class)
    public void ready() throws Exception {
        var max = 60;
        execute("calculate", () -> fibonacci.calculate(max));
    }

    private void execute(String func, Supplier<long[]> supplier) {
        logBefore(func);
        var results = supplier.get();
        log.info(func + " : " + results.length + " : " + Arrays.toString(results));
        logAfter(func);
    }


}
