package com.starline.base.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MainErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(MainErrorHandler.class);

    @Bean
    public VaadinServiceInitListener errorHandlerInitializer() {
        return (event) -> event.getSource().addSessionInitListener(
                sessionInitEvent -> sessionInitEvent.getSession().setErrorHandler(errorEvent -> {
                    log.error("An unexpected error occurred", errorEvent.getThrowable());
                    errorEvent.getComponent().flatMap(Component::getUI).ifPresent(ui -> {
                        if (errorEvent.getThrowable() instanceof WebClientLoggingFilter.ApiClientException ex) {
                            var notification = show4xxError(ex);
                            ui.access(notification::open);
                            return;
                        }
                        var notification = new Notification(
                                "An unexpected error has occurred. Please try again later.");
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        notification.setPosition(Notification.Position.TOP_CENTER);
                        notification.setDuration(3000);
                        ui.access(notification::open);
                    });
                }));
    }

    private Notification show4xxError(WebClientLoggingFilter.ApiClientException ex) {
        var notification = new Notification(StringUtils.capitalize(StringUtils.defaultIfBlank(ex.getErrorMessage(), "System Unavailable")), 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        return notification;
    }



}
