package com.example.i_commerce.domain.member.tools;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerfTimer {

    private final MeterRegistry meterRegistry;

    public <T> T record(String api, String step, Supplier<T> supplier) {
        return Timer.builder("api_internal_step_duration")
            .tag("api", api)
            .tag("step", step)
            .register(meterRegistry)
            .record(supplier);
    }

    public void record(String api, String step, Runnable runnable) {
        Timer.builder("api_internal_step_duration")
            .tag("api", api)
            .tag("step", step)
            .register(meterRegistry)
            .record(runnable);
    }
}