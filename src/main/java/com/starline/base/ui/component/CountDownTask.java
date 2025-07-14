package com.starline.base.ui.component;

import com.vaadin.flow.component.UI;
import jakarta.annotation.Nullable;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;


public class CountDownTask {

    @Setter
    private ScheduledExecutorService scheduler;

    @Nullable
    private ScheduledFuture<?> scheduledCountDown;

    private final AtomicInteger counter = new AtomicInteger(30);

    private final int countDownPeriod;

    public CountDownTask(int count) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.counter.set(count);
        this.countDownPeriod = count;

    }


    public void startCountdown(@Nullable UI ui, Runnable action, IntConsumer pauseAction) {
        counter.set(countDownPeriod);
        final AtomicInteger[] countdown = {counter};

        // Cancel existing task if running
        Optional.ofNullable(scheduledCountDown)
                .ifPresent( it -> {
                    if (!it.isCancelled()) {
                        it.cancel(true);
                    }
                });


        scheduledCountDown = scheduler.scheduleAtFixedRate(() -> {
            if (countdown[0].get() <= 0) {
                Optional.ofNullable(ui).ifPresent(it -> it.access(action::run));
                cancelScheduled();

            } else {
                Optional.ofNullable(ui).ifPresent(it ->
                        it.access(() -> pauseAction.accept(countdown[0].getAndDecrement()))
                );
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
       cancelScheduled();
       cancelScheduler();
    }

    private void cancelScheduled() {
        Optional.ofNullable(scheduledCountDown).ifPresent(it -> {
            if (!it.isCancelled()) {
                it.cancel(true);
            }
        });
    }

    private void cancelScheduler() {
        Optional.of(scheduler).ifPresent(it -> {
            if (!it.isShutdown()) {
                it.shutdown();
            }
        });
    }
}
