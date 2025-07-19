package com.starline.base.ui.component;


import com.vaadin.flow.component.UI;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

@Slf4j
public class ReactiveCountDownTask implements CountDownTask {

    private final AtomicInteger counter = new AtomicInteger(30);
    private final int countDownPeriod;

    @Nullable
    private Disposable subscription;

    public ReactiveCountDownTask(int seconds) {
        this.countDownPeriod = seconds;
        this.counter.set(seconds);
    }

    @Override
    public void startCountdown(@Nullable UI ui, Runnable onComplete, IntConsumer onTick) {
        // Cancel previous if running
        cancel();

        counter.set(countDownPeriod);

        subscription = Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
                .take(countDownPeriod + 1L)
                .doOnNext(i -> {
                    int secondsLeft = counter.getAndDecrement();
                    log.debug("Countdown: {}s", secondsLeft);
                    if (ui != null) {
                        ui.access(() -> {
                            if (secondsLeft > 0) {
                                onTick.accept(secondsLeft);
                            } else {
                                onComplete.run();
                            }
                            ui.push(); // trigger client update
                        });
                    }
                })
                .doOnError(e -> log.error("Countdown error", e))
                .doOnTerminate(this::cancel)
                .subscribe();
    }

    @Override
    public void cancel() {
        Optional.ofNullable(subscription).ifPresent(s -> {
            if (!s.isDisposed()) {
                s.dispose();
            }
        });
        subscription = null;
    }

}

