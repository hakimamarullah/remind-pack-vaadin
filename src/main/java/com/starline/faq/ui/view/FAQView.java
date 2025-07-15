package com.starline.faq.ui.view;

import com.starline.base.ui.component.AppVerticalLayout;
import com.starline.base.ui.view.MainLayout;
import com.starline.faq.domain.FAQItem;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

@PageTitle("FAQ | RemindPack")
@Menu(title = "FAQ", icon = "vaadin:question-circle", order = 2)
@CssImport("./themes/default/faq.css")
@PermitAll
@Route(value = "faq", layout = MainLayout.class)
public class FAQView extends AppVerticalLayout {

    private Accordion accordion;

    public FAQView() {
        super("Frequently Asked Questions");
        initializeView();
        createHeader();
        createFaqAccordion();
    }

    private void initializeView() {
        addClassName("faq-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createHeader() {
        Div headerContainer = new Div();
        headerContainer.addClassName("faq-header");

        H1 title = new H1("Frequently Asked Questions");
        title.addClassName("faq-title");

        Span subtitle = new Span("Find answers to common questions about RemindPack");
        subtitle.addClassName("faq-subtitle");

        headerContainer.add(title, subtitle);
        add(headerContainer);
    }

    private void createFaqAccordion() {
        Div contentContainer = new Div();
        contentContainer.addClassName("faq-content");

        accordion = new Accordion();
        accordion.addClassName("faq-accordion");
        accordion.setWidthFull();

        populateAccordion(getStaticFAQItems());

        contentContainer.add(accordion);
        add(contentContainer);
    }

    private void populateAccordion(List<FAQItem> faqItems) {
        faqItems.forEach(this::createAccordionPanel);
    }

    private void createAccordionPanel(FAQItem faqItem) {
        Div answerContainer = new Div();
        answerContainer.addClassName("faq-answer");

        Span answer = new Span(faqItem.getAnswer());
        answer.getElement().setProperty("innerHTML", faqItem.getAnswer());

        answerContainer.add(answer);

        AccordionPanel panel = accordion.add(faqItem.getQuestion(), answerContainer);
        panel.addClassName("faq-panel");
    }

    // Method to refresh FAQ content (for future dynamic implementation)
    public void refreshFaqContent() {
        accordion.getChildren().forEach(accordion::remove);
        populateAccordion(getStaticFAQItems());
    }

    private List<FAQItem> getStaticFAQItems() {
        List<FAQItem> faqItems = new ArrayList<>();

        faqItems.add(new FAQItem(
                "What is RemindPack?",
                "RemindPack is a smart package tracking service that automatically monitors your shipments and sends you notifications when there are status changes or checkpoints in your package delivery journey.",
                "General",
                1
        ));

        faqItems.add(new FAQItem(
                "How does RemindPack work?",
                "Simply add your tracking numbers to RemindPack, and our system will continuously monitor your packages using our scheduler. You'll receive instant notifications via email or SMS whenever there's a status update, delivery attempt, or checkpoint change.",
                "General",
                2
        ));

        faqItems.add(new FAQItem(
                "Which shipping carriers does RemindPack support?",
                "RemindPack supports major shipping carriers including FedEx, UPS, DHL, USPS, and many regional carriers. We're constantly adding support for new carriers based on user demand.",
                "Supported Carriers",
                3
        ));

        faqItems.add(new FAQItem(
                "How often does RemindPack check for package updates?",
                "Our system checks for package updates every 30 minutes during business hours and every 2 hours during off-peak times. You can also manually refresh tracking information from your dashboard.",
                "Tracking",
                4
        ));

        faqItems.add(new FAQItem(
                "What types of notifications can I receive?",
                "You can receive notifications for package pickup, in-transit updates, delivery attempts, successful deliveries, delays, and exceptions. You can customize which notifications you want to receive in your account settings.",
                "Notifications",
                5
        ));

        faqItems.add(new FAQItem(
                "Is RemindPack free to use?",
                "RemindPack offers a free tier that allows you to track up to 5 packages simultaneously. For power users, we offer premium plans with unlimited tracking, advanced notifications, and additional features.",
                "Pricing",
                6
        ));

        faqItems.add(new FAQItem(
                "How do I add a package to track?",
                "To add a package, simply log into your dashboard, click 'Add Package', enter your tracking number, select the carrier, and optionally add a description. RemindPack will immediately start monitoring your package.",
                "Getting Started",
                7
        ));

        faqItems.add(new FAQItem(
                "Can I track international packages?",
                "Yes! RemindPack supports international package tracking for supported carriers. Keep in mind that international tracking may have different update frequencies and checkpoint availability depending on the origin and destination countries.",
                "International",
                8
        ));

        faqItems.add(new FAQItem(
                "What happens when my package is delivered?",
                "When your package is delivered, you'll receive a delivery confirmation notification and the package will be automatically moved to your 'Delivered' section. You can archive delivered packages to keep your dashboard organized.",
                "Delivery",
                9
        ));

        faqItems.add(new FAQItem(
                "How secure is my tracking information?",
                "We take security seriously. All tracking information is encrypted and stored securely. We only access publicly available tracking information and never store or access the contents of your packages.",
                "Security",
                10
        ));

        faqItems.add(new FAQItem(
                "Can I customize my notification preferences?",
                "Absolutely! In your account settings, you can choose which types of notifications to receive, how to receive them (email, SMS, or both), and set quiet hours when you don't want to be disturbed.",
                "Customization",
                11
        ));

        faqItems.add(new FAQItem(
                "What should I do if my package tracking isn't updating?",
                "If your package tracking isn't updating, first check if you've entered the correct tracking number and selected the right carrier. If the issue persists, try refreshing manually or contact our support team for assistance.",
                "Troubleshooting",
                12
        ));

        return faqItems;
    }
}