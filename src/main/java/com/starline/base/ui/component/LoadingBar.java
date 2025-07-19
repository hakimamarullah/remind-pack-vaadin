package com.starline.base.ui.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class LoadingBar extends VerticalLayout {

    private final transient CountDownTask countDownTask;
    private final HorizontalLayout failedToLoadLayout = new HorizontalLayout();
    private final UI ui;

    public LoadingBar() {
        this("Hang on, Comrade!....", "Failed to load page", 10);
    }

    public LoadingBar(String upperLabel, String failedMessage, int timeOut) {
        this.ui = UI.getCurrent();
        this.countDownTask = new ReactiveCountDownTask(timeOut);
        NativeLabel progressBarLabel = new NativeLabel(upperLabel);
        setWidthFull();
        setHeightFull();
        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.CENTER);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);


        progressBarLabel.setId("pblbl");
        progressBarLabel.addClassName(LumoUtility.TextColor.SECONDARY);
        progressBar.getElement().setAttribute("aria-labelledby", "pblbl");


        // Failed to load
        H2 failedToLoad = new H2(failedMessage);
        failedToLoadLayout.setWidth("50%");
        failedToLoadLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        failedToLoadLayout.setAlignItems(Alignment.CENTER);
        failedToLoadLayout.add(failedToLoad, VaadinIcon.SMILEY_O.create());

        add(progressBarLabel, progressBar);
    }

    public void start() {
        countDownTask.startCountdown(ui, () -> {
            removeAll();
            setAlignItems(Alignment.CENTER);
            add(failedToLoadLayout);
        }, counter -> {
        });
    }
}
