package org.example.staystylish.domain.localweather;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class service {
    private final MeterRegistry meterRegistry;

    @GetMapping("/test-metrics")
    public String testMetrics() throws InterruptedException {
        Timer.Sample sample = Timer.start(meterRegistry);
        Thread.sleep(500); // 0.5초 대기 (테스트용)
        sample.stop(meterRegistry.timer("test_timer_seconds", "result", "success"));
        meterRegistry.counter("test_requests_total", "result", "success").increment();
        return "ok";
    }
}