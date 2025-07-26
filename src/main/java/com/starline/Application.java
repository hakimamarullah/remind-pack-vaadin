package com.starline;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

@SpringBootApplication
@Theme("default")
@Push(transport = Transport.LONG_POLLING)
@EnableAsync
public class Application implements AppShellConfigurator {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone(); // You can also use Clock.systemUTC()
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
       settings.addMetaTag("description", "RemindPack helps you track and manage all your subscriptions with smart reminders. Never miss a renewal again.");
       settings.addMetaTag("keywords", "package tracker, reminder app, remindpack, subscription manager, remind me, remind paket, awb tracking");
    }
}
