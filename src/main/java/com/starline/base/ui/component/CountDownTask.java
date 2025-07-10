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

    private ScheduledFuture<?> scheduledCountDown;

    private final AtomicInteger counter = new AtomicInteger(30);

    private int countDownPeriod = 30;

    public CountDownTask() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public CountDownTask(int count) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.counter.set(count);
        this.countDownPeriod = count;

    }


    public void startCountdown(@Nullable UI ui, Runnable action, IntConsumer pauseAction) {
        counter.set(countDownPeriod);
        final AtomicInteger[] countdown = {counter};

        // Cancel existing task if running
        if (scheduledCountDown != null && !scheduledCountDown.isCancelled()) {
            scheduledCountDown.cancel(true);
        }

        scheduledCountDown = scheduler.scheduleAtFixedRate(() -> {
            if (countdown[0].get() <= 0) {
                Optional.ofNullable(ui).ifPresent(it -> it.access(action::run));
                scheduledCountDown.cancel(true);

            } else {
                Optional.ofNullable(ui).ifPresent(it -> it.access(() -> pauseAction.accept(countdown[0].getAndDecrement())));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (scheduledCountDown != null && !scheduledCountDown.isCancelled()) {
            scheduledCountDown.cancel(true);
        }
        if (scheduledCountDown != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
