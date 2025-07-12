package com.starline.base.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class ConfirmDeleteDialog<T> {

    private final Runnable onSuccess;

    public ConfirmDeleteDialog(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void show(T item,
                     Function<T, String> nameGetter,
                     Consumer<T> deleteFunction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete");
        dialog.add(new Paragraph("Are you sure you want to delete item: " + nameGetter.apply(item) + "?"));

        Button confirm = new Button("Delete", event -> {
            try {
                deleteFunction.accept(item);
                dialog.close();
                onSuccess.run();
            } catch (Exception e) {
                log.warn("Error deleting item: {}", nameGetter.apply(item));
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        Button cancel = new Button("Cancel", event -> dialog.close());

        dialog.add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }
}

