package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Arrays;

@Slf4j
abstract class Utils {

    static void handle(String func, long[] results, Throwable t) {
        log.info(func + " : " + results.length + " : " + Arrays.toString(results));
        logHandle(func);
    }

    static void logBefore(String func) {
        log.info("before {} @ {}", func, Instant.now().toString());
    }

    static void logAfter(String func) {
        log.info("after {} @ {}", func, Instant.now().toString());
    }

    static void logHandle(String func) {
        log.info("handle {} @ {}", func, Instant.now().toString());
    }
}
