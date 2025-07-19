package com.starline.base.ui.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AppVerticalLayout extends VerticalLayout {

    private final transient ViewToolbar viewToolbar;

    public AppVerticalLayout(String title) {
        this.viewToolbar = new ViewToolbar(title);
        setWidthFull();
        setPadding(false);
        setHeightFull();
        add(viewToolbar);
    }

    public AppVerticalLayout() {
        this.viewToolbar = new ViewToolbar("");
        add(viewToolbar);
    }
}
