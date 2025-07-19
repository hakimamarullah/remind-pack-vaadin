package com.starline.base.ui.component;

import com.vaadin.flow.component.UI;
import jakarta.annotation.Nullable;

import java.util.function.IntConsumer;

public interface CountDownTask {
    void startCountdown(@Nullable UI ui, Runnable onComplete, IntConsumer onTick);

    void cancel();
}
