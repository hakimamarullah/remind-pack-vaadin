package com.starline.subscriptions.ui.view;

import com.starline.base.ui.component.AppVerticalLayout;
import com.starline.base.ui.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

@Route(value = "subscriptions", layout = MainLayout.class)
@PageTitle("Subscriptions | RemindPack")
@Menu(title = "Subscriptions", icon = "vaadin:credit-card", order = 2)
@PermitAll
@CssImport("./themes/default/subscriptions-view.css")
public class SubscriptionsView extends AppVerticalLayout {

    private final List<PlanDto> dummyPlans = List.of(
            new PlanDto("basic", "Basic", "Perfect for personal reminders and individual use.", 15000,
                    List.of("Up to 10 reminders/month", "Email notifications", "Basic support", "Mobile app access")),
            new PlanDto("pro", "Pro", "Ideal for small teams and recurring deliveries.", 35000,
                    List.of("Up to 100 reminders/month", "SMS & Email notifications", "Priority support", "Team collaboration", "Advanced analytics")),
            new PlanDto("enterprise", "Enterprise", "For large scale operations with full support.", 75000,
                    List.of("Unlimited reminders", "All notification types", "24/7 dedicated support", "Custom integrations", "Advanced reporting", "SLA guarantee"))
    );

    private final List<SubscriptionDto> dummySubscriptions = List.of(
            new SubscriptionDto("Pro", "active", "", "2024-01-15", "2024-12-31", "monthly"),
            new SubscriptionDto("Enterprise", "pending", "https://midtrans.example.com/pay/abc123", "", "", "yearly")
    );

    public SubscriptionsView() {
        super("Subscriptions");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("subscriptions-view");

        // Create main content wrapper
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);
        mainContent.addClassName("main-content");

        mainContent.add(createPlansSection(), createSubscriptionSummary());
        add(mainContent);
    }



    private Component createPlansSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("plans-section");
        section.setWidthFull();
        section.setAlignItems(Alignment.CENTER);
        section.setPadding(true);

        H2 sectionTitle = new H2("Pricing Plans");
        sectionTitle.addClassName("section-title");

        FlexLayout cardsLayout = new FlexLayout();
        cardsLayout.addClassName("plans-grid");
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        cardsLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        cardsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        for (int i = 0; i < dummyPlans.size(); i++) {
            PlanDto plan = dummyPlans.get(i);
            Component card = createPlanCard(plan, i == 1); // Mark middle plan as popular
            cardsLayout.add(card);
        }

        section.add(sectionTitle, cardsLayout);
        return section;
    }

    private Component createPlanCard(PlanDto plan, boolean isPopular) {
        Card card = new Card();
        card.addClassName("plan-card");
        if (isPopular) {
            card.addClassName("popular-plan");
        }

        VerticalLayout content = new VerticalLayout();
        content.addClassName("plan-content");
        content.setPadding(true);
        content.setSpacing(true);

        // Popular badge
        if (isPopular) {
            Div badge = new Div();
            badge.addClassName("popular-badge");
            badge.setText("MOST POPULAR");
            content.add(badge);
        }

        // Plan name
        H3 planName = new H3(plan.name());
        planName.addClassName("plan-name");
        content.add(planName);

        // Plan description
        Paragraph description = new Paragraph(plan.description());
        description.addClassName("plan-description");
        content.add(description);

        // Price
        Div priceSection = new Div();
        priceSection.addClassName("price-section");

        Span priceAmount = new Span(formatIDR(plan.price()));
        priceAmount.addClassName("price-amount");

        Span priceUnit = new Span("/month");
        priceUnit.addClassName("price-unit");

        priceSection.add(priceAmount, priceUnit);
        content.add(priceSection);

        // Features list
        VerticalLayout featuresLayout = new VerticalLayout();
        featuresLayout.addClassName("features-list");
        featuresLayout.setPadding(false);
        featuresLayout.setSpacing(false);

        for (String feature : plan.features()) {
            HorizontalLayout featureRow = new HorizontalLayout();
            featureRow.addClassName("feature-row");
            featureRow.setAlignItems(Alignment.CENTER);
            featureRow.setSpacing(true);

            Icon checkIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
            checkIcon.addClassName("feature-icon");

            Span featureText = new Span(feature);
            featureText.addClassName("feature-text");

            featureRow.add(checkIcon, featureText);
            featuresLayout.add(featureRow);
        }

        content.add(featuresLayout);

        // Buy button
        Button buyButton = new Button("Get Started", e -> openOrderDialog(plan));
        buyButton.addClassName("buy-button");
        if (isPopular) {
            buyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            buyButton.addClassName("primary-button");
        } else {
            buyButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        }

        content.add(buyButton);

        card.add(content);
        return card;
    }

    private void openOrderDialog(PlanDto plan) {
        double tax = plan.price() * 0.12;
        double grandTotal = plan.price() + tax;

        Dialog dialog = new Dialog();
        dialog.addClassName("order-dialog");
        dialog.setCloseOnOutsideClick(true);
        dialog.setModal(true);

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("order-content");
        layout.setPadding(true);
        layout.setSpacing(true);

        // Dialog header
        H3 dialogTitle = new H3("Order Summary");
        dialogTitle.addClassName("dialog-title");
        layout.add(dialogTitle);

        // Order details
        Div orderDetails = new Div();
        orderDetails.addClassName("order-details");

        orderDetails.add(createOrderRow("Plan", plan.name()));
        orderDetails.add(createOrderRow("Price", formatIDR(plan.price())));
        orderDetails.add(createOrderRow("Tax (12%)", formatIDR(tax)));

        Div totalRow = new Div();
        totalRow.addClassName("total-row");
        totalRow.add(new Span("Total: " + formatIDR(grandTotal)));
        orderDetails.add(totalRow);

        layout.add(orderDetails);

        // Action buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("dialog-buttons");
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        Button cancelButton = new Button("Cancel", event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button orderButton = new Button("Create Order", event -> {
            // Simulate dummy payment response
            PaymentInfo payment = new PaymentInfo("https://midtrans.example.com/pay/order-" + plan.id());
            UI.getCurrent().getPage().setLocation(payment.redirectUrl());
            dialog.close();
        });
        orderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        buttonLayout.add(cancelButton, orderButton);
        layout.add(buttonLayout);

        dialog.add(layout);
        dialog.open();
    }

    private Div createOrderRow(String label, String value) {
        Div row = new Div();
        row.addClassName("order-row");

        Span labelSpan = new Span(label + ":");
        labelSpan.addClassName("order-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("order-value");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private Component createSubscriptionSummary() {
        if (dummySubscriptions.stream().noneMatch(sub ->
                sub.status().equalsIgnoreCase("active") || sub.status().equalsIgnoreCase("pending"))) {
            return new Div(); // Return empty div if no active/pending subscriptions
        }

        VerticalLayout section = new VerticalLayout();
        section.addClassName("subscription-summary");
        section.setWidthFull();
        section.setAlignItems(Alignment.CENTER);
        section.setPadding(true);

        H2 title = new H2("Your Subscriptions");
        title.addClassName("section-title");

        VerticalLayout list = new VerticalLayout();
        list.addClassName("subscription-list");
        list.setPadding(false);
        list.setSpacing(true);
        list.setWidthFull();
        list.setMaxWidth("800px");

        for (SubscriptionDto sub : dummySubscriptions) {
            if (!sub.status().equalsIgnoreCase("active") && !sub.status().equalsIgnoreCase("pending")) {
                continue;
            }

            Card subscriptionCard = new Card();
            subscriptionCard.addClassName("subscription-card");
            subscriptionCard.setWidthFull();

            HorizontalLayout row = new HorizontalLayout();
            row.addClassName("subscription-row");
            row.setWidthFull();
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            row.setAlignItems(Alignment.CENTER);
            row.setPadding(true);

            VerticalLayout info = new VerticalLayout();
            info.addClassName("subscription-info");
            info.setPadding(false);
            info.setSpacing(false);

            H4 planName = new H4(sub.planName() + " Plan");
            planName.addClassName("subscription-plan-name");

            // Status badge
            Div statusBadge = new Div();
            statusBadge.addClassName("status-badge");
            statusBadge.addClassName("status-" + sub.status().toLowerCase());
            statusBadge.setText(sub.status().substring(0, 1).toUpperCase() + sub.status().substring(1));

            // Subscription details
            VerticalLayout detailsLayout = new VerticalLayout();
            detailsLayout.addClassName("subscription-details");
            detailsLayout.setPadding(false);
            detailsLayout.setSpacing(false);

            if ("active".equalsIgnoreCase(sub.status())) {
                if (!sub.effectiveDate().isEmpty()) {
                    Span effectiveDate = new Span("Effective: " + sub.effectiveDate());
                    effectiveDate.addClassName("subscription-date");
                    detailsLayout.add(effectiveDate);
                }

                if (!sub.endDate().isEmpty()) {
                    Span endDate = new Span("Expires: " + sub.endDate());
                    endDate.addClassName("subscription-date");
                    detailsLayout.add(endDate);
                }

                if (!sub.planCycle().isEmpty()) {
                    Span planCycle = new Span("Billing: " + sub.planCycle().substring(0, 1).toUpperCase() + sub.planCycle().substring(1));
                    planCycle.addClassName("subscription-cycle");
                    detailsLayout.add(planCycle);
                }
            }

            info.add(planName, statusBadge, detailsLayout);

            row.add(info);

            if ("pending".equalsIgnoreCase(sub.status())) {
                Button payButton = new Button("Complete Payment", click -> {
                    UI.getCurrent().getPage().setLocation(sub.paymentUrl());
                });
                payButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                payButton.addClassName("pay-button");
                row.add(payButton);
            }

            subscriptionCard.add(row);
            list.add(subscriptionCard);
        }

        section.add(title, list);
        return section;
    }

    private String formatIDR(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        df.setGroupingUsed(true);
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        return "IDR " + df.format(amount);
    }

    // DTOs
    public record PlanDto(String id, String name, String description, double price, List<String> features) {}
    public record SubscriptionDto(String planName, String status, String paymentUrl, String effectiveDate, String endDate, String planCycle) {}
    public record PaymentInfo(String redirectUrl) {}
}