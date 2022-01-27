package rsb.synchronicity;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static rsb.synchronicity.Utils.*;

@Component
record AsyncRunner(Fibonacci fibonacci) {

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        var max = 60;
        executeCompletableFuture("calculateWithAsync", () -> fibonacci.calculateWithAsync(max));
        executeCompletableFuture("calculateWithCompletableFuture", () -> fibonacci.calculateWithCompletableFuture(max));
    }

    private void executeCompletableFuture(String func, Supplier<CompletableFuture<long[]>> completableFuture) {
        logBefore(func);
        completableFuture.get().whenComplete((r, t) -> handle(func, r, t));
        logAfter(func);
    }

}
