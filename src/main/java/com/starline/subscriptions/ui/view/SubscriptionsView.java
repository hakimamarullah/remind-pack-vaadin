package com.starline.subscriptions.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.subscriptions.SubscriptionService;
import com.starline.base.api.subscriptions.dto.payment.CreateOrderRequest;
import com.starline.base.api.subscriptions.dto.payment.OrderSummary;
import com.starline.base.api.subscriptions.dto.plan.PlanInfo;
import com.starline.base.api.subscriptions.dto.subscriptions.SubscriptionInfo;
import com.starline.base.api.subscriptions.dto.subscriptions.SubscriptionStatus;
import com.starline.base.ui.component.AppVerticalLayout;
import com.starline.base.ui.component.LoadingBar;
import com.starline.base.ui.view.MainLayout;
import com.starline.base.utils.Formatter;
import com.starline.security.AppUserInfo;
import com.starline.security.CurrentUser;
import com.starline.security.domain.UserId;
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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Route(value = "subscriptions", layout = MainLayout.class)
@PageTitle("Subscriptions | RemindPack")
@Menu(title = "Subscriptions", icon = "vaadin:credit-card", order = 2)
@CssImport("./themes/default/subscriptions-view.css")
@PermitAll
@Slf4j
public class SubscriptionsView extends AppVerticalLayout implements BeforeEnterObserver {

    private final transient SubscriptionService subscriptionService;
    private final transient AppUserInfo appUserInfo;

    private final VerticalLayout mainContent = new VerticalLayout();

    private Component subscriptionSection = new VerticalLayout();


    public SubscriptionsView(CurrentUser currentUser, SubscriptionService subscriptionService) {
        super("Subscriptions");
        this.appUserInfo = currentUser.require();
        this.subscriptionService = subscriptionService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("subscriptions-view");

        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);


        // Fetch data
        UI ui = UI.getCurrent();

        LoadingBar loadingBar = new LoadingBar();
        mainContent.addComponentAsFirst(loadingBar);
        loadingBar.start();

        subscriptionService.getAvailablePlans()
                .map(ApiResponse::getData)
                .defaultIfEmpty(Collections.emptyList())
                .map(plans -> plans.stream().map(this::toPlanDto).toList())
                .doOnNext(planDtos -> {
                    if (ui != null) {
                        ui.access(() -> mainContent.replace(loadingBar, createPlansSection(planDtos)));
                    }
                }).subscribe();

        subscriptionService.getSubscriptions(getCurrentUserId())
                .map(ApiResponse::getData)
                .defaultIfEmpty(Collections.emptyList())
                .map(subs -> subs.stream().map(this::toSubscriptionDto).toList())
                .doOnNext(subDtos -> {
                    if (ui != null) {
                        subscriptionSection = createSubscriptionSummary(subDtos);
                        ui.access(() -> mainContent.addComponentAtIndex(1, subscriptionSection));
                    }
                }).subscribe();
        add(mainContent);
    }

    private void refreshSubscriptionSection(UI ui) {
        subscriptionService.getSubscriptions(getCurrentUserId())
                .map(ApiResponse::getData)
                .defaultIfEmpty(Collections.emptyList())
                .map(subs -> subs.stream().map(this::toSubscriptionDto).toList())
                .doOnNext(subDtos -> {
                    if (ui != null) {
                        var tempComponent = createSubscriptionSummary(subDtos);
                        ui.access(() -> mainContent.replace(subscriptionSection, tempComponent));
                        subscriptionSection = tempComponent;
                    }
                }).subscribe();
    }


    private Component createPlansSection(List<PlanDto> plans) {
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

        for (int i = 0; i < plans.size(); i++) {
            PlanDto plan = plans.get(i);
            cardsLayout.add(createPlanCard(plan, i == 1));
        }

        section.add(sectionTitle, cardsLayout);
        return section;
    }

    private Component createPlanCard(PlanDto plan, boolean isPopular) {
        Card card = new Card();
        card.addClassName("plan-card");
        if (isPopular) card.addClassName("popular-plan");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("plan-content");
        content.setPadding(true);
        content.setSpacing(true);

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

        Div priceSection = new Div();
        priceSection.addClassName("price-section");

        Span priceAmount = new Span(plan.price());
        priceAmount.addClassName("price-amount");

        Span priceUnit = new Span("/" + plan.planCycle());
        priceUnit.addClassName("price-unit");

        priceSection.add(priceAmount, priceUnit);

        content.add(priceSection);

        VerticalLayout featuresLayout = new VerticalLayout();
        featuresLayout.addClassName("features-list");
        featuresLayout.setPadding(false);
        featuresLayout.setSpacing(false);
        for (String feature : plan.features()) {
            HorizontalLayout featureRow = createFeatureRow(feature);
            featuresLayout.add(featureRow);
        }

        content.add(featuresLayout);

        Button buyButton = new Button("Buy", e -> openOrderDialog(plan));
        buyButton.addClassName("buy-button");
        buyButton.addThemeVariants(isPopular ? ButtonVariant.LUMO_PRIMARY : ButtonVariant.LUMO_CONTRAST);
        if (isPopular) {
            buyButton.addClassName("primary-button");
        }
        content.add(buyButton);

        card.add(content);
        return card;
    }

    private static HorizontalLayout createFeatureRow(String feature) {
        HorizontalLayout featureRow = new HorizontalLayout();
        featureRow.addClassName("subscription-feature-row");
        featureRow.setAlignItems(Alignment.CENTER);
        featureRow.setSpacing(true);

        Icon checkIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        checkIcon.addClassName("subscription-feature-icon");

        Span featureText = new Span(feature);
        featureText.addClassName("subscription-feature-text");

        featureRow.add(checkIcon, featureText);
        return featureRow;
    }

    private void openOrderDialog(PlanDto plan) {
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

        Div orderDetails = new Div();
        orderDetails.addClassName("order-details");
        layout.add(orderDetails);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.END);

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button order = new Button("Create Order");
        order.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        order.setEnabled(false); // disable until summary is loaded

        buttons.add(cancel, order);
        layout.add(buttons);
        dialog.add(layout);
        dialog.open();

        UI ui = UI.getCurrent();
        // Call getOrderSummary and update UI
        subscriptionService.getOrderSummary(plan.id())
                .map(ApiResponse::getData)
                .doOnNext(summary -> handleDisplayOrderSummary(plan, summary, ui, orderDetails, order, dialog))
                .subscribe();
    }

    private void handleDisplayOrderSummary(PlanDto plan, OrderSummary summary, UI ui, Div orderDetails, Button order, Dialog dialog) {
        if (ui != null) {
            ui.access(() -> {
                orderDetails.removeAll();
                orderDetails.add(createOrderRow("Plan", summary.getPlanName()));
                orderDetails.add(createOrderRow("Price", summary.getPlanPrice()));
                orderDetails.add(createOrderRow("Tax (" + summary.getTaxRate() + ")", summary.getTaxTotal()));

                Div totalRow = new Div();
                totalRow.addClassName("total-row");
                totalRow.add(new Span("Total: " + summary.getGrandTotal()));
                orderDetails.add(totalRow);

                order.setEnabled(true);
                order.addClickListener(e -> handleCreateOrder(ui, dialog, plan.id()));

                ui.push(); // send updates to client
            });
        }
    }

    private void handleCreateOrder(UI ui, Dialog dialog, Long planId) {
        CreateOrderRequest req = CreateOrderRequest.builder().userId(getCurrentUserId()).planId(planId).build();

        subscriptionService.createOrder(req)
                .map(ApiResponse::getData)
                .subscribe(payment -> ui.access(() -> {
                    try {
                        dialog.close();
                        if (StringUtils.isBlank(payment.getSnapUrl())) {
                            showSuccessNotification("Subscription created successfully!");
                        } else {
                            ui.getPage().executeJs("window.open($0, '_blank')", payment.getSnapUrl());
                        }
                    } catch (Exception ex) {
                        log.error("Error handling payment response", ex);
                    }
                }), error -> ui.access(() -> {
                    try {
                        if (error instanceof WebClientLoggingFilter.ApiClientException apiEx) {
                            show4xxError(apiEx, ui);
                        } else {
                            showUnexpectedError(error, ui);
                        }
                    } catch (Exception uiEx) {
                        log.error("Error showing error notification", uiEx);
                    }
                }));
    }


    private void showSuccessNotification(String message) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }

    private void show4xxError(WebClientLoggingFilter.ApiClientException ex, UI ui) {
        String message = ex.getErrorMessage();
        if (ex.getHttpStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            message = "Something went wrong. Please try again later!";
        }
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        Optional.ofNullable(ui).ifPresent(u -> u.access(notification::open));
    }

    private void showUnexpectedError(Throwable ex, UI ui) {
        log.warn("Error getting subscriptions: {}", ex.getMessage());
        Optional.ofNullable(ui)
                .ifPresent(it -> it.access(() ->
                        Notification.show("Something went wrong. Please try again later!", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)));
    }

    private Component createSubscriptionSummary(List<SubscriptionDto> subscriptions) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("subscription-summary");
        section.setWidthFull();
        section.setAlignItems(Alignment.CENTER);
        section.setPadding(true);

        section.add();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);

        header.add(new H2("Subscriptions"));

        Button refresh = new Button();
        refresh.setIcon(VaadinIcon.REFRESH.create());
        refresh.addClickListener(e -> refreshSubscriptionSection(e.getSource().getUI().orElse(null)));
        header.add(refresh);

        section.add(header);

        if (subscriptions.isEmpty()) {
            section.add(new Paragraph("You have no active subscriptions."));
            return section;
        }


        VerticalLayout list = new VerticalLayout();
        list.addClassName("subscription-list");
        list.setPadding(false);
        list.setSpacing(true);
        list.setWidthFull();

        var filteredSubscriptions = subscriptions.stream()
                .filter(sub -> sub.status().equalsIgnoreCase(SubscriptionStatus.ACTIVE.name()) || sub.status().equalsIgnoreCase(SubscriptionStatus.PENDING.name()))
                .toList();
        for (SubscriptionDto sub : filteredSubscriptions) {
            list.add(createSubscriptionDetails(sub));
        }

        section.add(list);
        return section;
    }


    private Component createSubscriptionDetails(SubscriptionDto subscription) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("subscription-card");

        // Header with plan name and badges
        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.addClassName("subscription-header");
        cardHeader.setAlignItems(Alignment.CENTER);
        cardHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cardHeader.setWidthFull();

        // Plan name on the left
        Span planName = new Span(capitalize(subscription.planName()) + " Plan");
        planName.addClassName("subscription-plan-name");

        // Badge container on the right
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.addClassName("badges-container");
        badgesContainer.setSpacing(false);
        badgesContainer.setAlignItems(Alignment.CENTER);

        // Plan cycle badge
        Span planCycleBadge = new Span(capitalize(subscription.planCycle()));
        planCycleBadge.addClassName("badge");
        planCycleBadge.addClassName("badge-cycle");

        // Status badge
        Span statusBadge = new Span(capitalize(subscription.status()));
        statusBadge.addClassName("badge");
        statusBadge.addClassName("badge-status");
        statusBadge.addClassName("badge-status-" + subscription.status().toLowerCase());

        badgesContainer.add(planCycleBadge, statusBadge);
        cardHeader.add(planName, badgesContainer);

        // Compact details section with subtle blue background
        Div details = new Div();
        details.addClassName("subscription-details");

        // Create compact detail rows
        details.add(
                createDetailRow("Subscription ID", subscription.subscriptionId(), true),
                createDetailRow("Effective Date", subscription.effectiveDate()),
                createDetailRow("End Date", subscription.endDate())
        );

        card.add(cardHeader, details);

        // Payment button for pending subscriptions
        if (SubscriptionStatus.PENDING.name().equalsIgnoreCase(subscription.status())) {
            Button payBtn = new Button("Complete Payment");
            payBtn.addClassName("subscription-pay-btn");
            payBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            payBtn.setWidthFull();
            payBtn.addClickListener(event -> event.getSource()
                    .getUI()
                    .ifPresent(ui -> ui.getPage().executeJs("window.open($0, '_blank')", subscription.paymentUrl())));
            card.add(payBtn);
        }

        return card;
    }


    public Component createDetailRow(String label, String value) {
        return createDetailRow(label, value, false);
    }

    private Component createDetailRow(String label, String value, boolean addCopyButton) {
        HorizontalLayout row = new HorizontalLayout();
        row.addClassName("detail-row");
        row.setAlignItems(Alignment.CENTER);
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
        row.setWidthFull();
        row.setPadding(false);
        row.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.addClassName("detail-label");

        Span valueSpan = new Span(StringUtils.defaultIfBlank(value, "N/A"));
        valueSpan.addClassName("detail-value");

        if (addCopyButton) {
            Button copyBtn = new Button();
            copyBtn.setIcon(VaadinIcon.COPY.create());
            copyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            copyBtn.addClickListener(e -> e.getSource().getUI()
                    .ifPresent(ui ->
                    {
                        ui.getPage()
                                .executeJs("navigator.clipboard.writeText($0)", value);
                        Notification.show("Subscription ID Copied!", 2000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                    }));
            HorizontalLayout valueLayout = new HorizontalLayout(copyBtn, valueSpan);
            valueLayout.addClassName("detail-value-copy-layout");
            valueLayout.setAlignItems(Alignment.CENTER);
            valueLayout.setSpacing(true);
            row.add(labelSpan, valueLayout);
            return row;
        }

        row.add(labelSpan, valueSpan);
        return row;
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UI.getCurrent().getPage().executeJs("history.replaceState(null, '', window.location.pathname)");
    }

    // Local DTOs
    public record PlanDto(Long id, String name, String description, String price, List<String> features,
                          String planCycle) {
    }

    public record SubscriptionDto(String subscriptionId, String planName, String status, String paymentUrl,
                                  String effectiveDate,
                                  String endDate, String planCycle) {
    }

    private PlanDto toPlanDto(PlanInfo p) {
        String[] parts = p.getDescription().split("\\|");
        String summary = parts.length > 0 ? parts[0] : "";
        List<String> features = parts.length > 1 ? List.of(parts).subList(1, parts.length) : List.of();
        return new PlanDto(p.getId(), p.getName(), summary, p.getPriceDisplay(), features, p.getValidityDisplay());
    }

    private Long getCurrentUserId() {
        return Optional.ofNullable(appUserInfo).map(AppUserInfo::getUserId)
                .map(UserId::toString)
                .map(Long::valueOf).orElse(null);
    }

    private SubscriptionDto toSubscriptionDto(SubscriptionInfo s) {
        return new SubscriptionDto(
                s.getId(),
                s.getPlanName(),
                s.getStatus().name(),
                Optional.ofNullable(s.getPaymentUrl()).orElse(""),
                Formatter.formatDate(s.getEffectiveDate(), Formatter.DD_MMM_YYYY),
                Formatter.formatDate(s.getExpiryDate(), Formatter.DD_MMM_YYYY),
                StringUtils.capitalize(s.getPlanCycle())
        );
    }


}
