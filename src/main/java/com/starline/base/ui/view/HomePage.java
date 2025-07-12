package com.starline.base.ui.view;

import com.starline.base.ui.constant.StyleSheet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;

import java.time.Year;

@AnonymousAllowed
@PermitAll
@CssImport("./themes/default/homepage.css")
public final class HomePage extends Main {

    public static final String NAV_ITEM = "nav-item";
    public static final String RESI_DASHBOARD_PATH = "resi-dashboard";
    public static final String FOOTER_LINK_COLUMN_CLASS = "footer-link-column";

    public HomePage() {
        addClassName("home-main");
        setSizeFull();

        // Navigation Header
        Div nav = createNavigation();

        // Main content container
        Div layout = new Div();
        layout.addClassName("home-layout");

        // Hero section
        Div hero = createHeroSection();

        // Features section
        Div features = createFeaturesSection();

        // Stats section
        Div stats = createStatsSection();

        // Testimonials section
        Div testimonials = createTestimonialsSection();

        // CTA section
        Div cta = createCtaSection();

        // Footer
        Footer footer = createFooter();

        layout.add(hero, features, stats, testimonials, cta, footer);
        add(nav, layout);
    }

    private Div createNavigation() {
        Div nav = new Div();
        nav.addClassName("home-nav");

        Div navContent = new Div();
        navContent.addClassName("nav-content");

        // Logo
        Span logo = new Span("RemindPack");
        logo.addClassName("nav-logo");

        // Navigation menu
        Div navMenu = new Div();
        navMenu.addClassName("nav-menu");

        Button features = new Button("Features", VaadinIcon.STAR.create());
        features.addClassName(NAV_ITEM);
        features.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button pricing = new Button("Pricing", VaadinIcon.DOLLAR.create());
        pricing.addClassName(NAV_ITEM);
        pricing.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button contact = new Button("Contact", VaadinIcon.PHONE.create());
        contact.addClassName(NAV_ITEM);
        contact.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button getStarted = new Button("Get Started", e -> UI.getCurrent().navigate(RESI_DASHBOARD_PATH));
        getStarted.addClassName("nav-cta");
        getStarted.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        navMenu.add(getStarted);
        navContent.add(logo, navMenu);
        nav.add(navContent);

        return nav;
    }

    private Div createHeroSection() {
        Div hero = new Div();
        hero.addClassName("home-hero");

        Div heroContent = new Div();
        heroContent.addClassName("hero-content");

        // Hero text
        Div heroText = new Div();
        heroText.addClassName("hero-text");

        H1 title = new H1("Track Every Package with Confidence");
        title.addClassName("hero-title");

        Paragraph subtitle = new Paragraph("RemindPack is your trusted package tracking platform. Monitor shipments, get real-time updates, and never miss a delivery again.");
        subtitle.addClassName("hero-subtitle");

        Div heroActions = new Div();
        heroActions.addClassName("hero-actions");

        Button getStarted = new Button("Start Tracking Free", VaadinIcon.ARROW_RIGHT.create());
        getStarted.addClassName("hero-button-primary");
        getStarted.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);
        getStarted.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        getStarted.addClickListener(e -> UI.getCurrent().navigate(RESI_DASHBOARD_PATH));

        Button watchDemo = new Button("Watch Demo", VaadinIcon.PLAY.create());
        watchDemo.addClassName("hero-button-secondary");
        watchDemo.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);

        heroActions.add(getStarted);
        heroText.add(title, subtitle, heroActions);

        // Hero visual
        Div heroVisual = new Div();
        heroVisual.addClassName("hero-visual");

        // Create a visual representation using icons
        Div visualGrid = new Div();
        visualGrid.addClassName("hero-visual-grid");

        for (int i = 0; i < 3; i++) {
            Div card = new Div();
            card.addClassName("hero-card");

            Icon icon = switch (i % 3) {
                case 0 -> VaadinIcon.PACKAGE.create();
                case 1 -> VaadinIcon.TRUCK.create();
                default -> VaadinIcon.CHECK_CIRCLE.create();
            };
            icon.addClassName("hero-card-icon");

            Span status = new Span(switch (i % 3) {
                case 0 -> "Processing";
                case 1 -> "In Transit";
                default -> "Delivered";
            });
            status.addClassName("hero-card-status");

            card.add(icon, status);
            visualGrid.add(card);
        }

        heroVisual.add(visualGrid);
        heroContent.add(heroText, heroVisual);
        hero.add(heroContent);

        return hero;
    }

    private Div createFeaturesSection() {
        Div features = new Div();
        features.addClassName("home-features");

        Div featuresContent = new Div();
        featuresContent.addClassName("features-content");

        H2 featuresTitle = new H2("Why Choose RemindPack?");
        featuresTitle.addClassName("section-title");

        Paragraph featuresSubtitle = new Paragraph("Powerful features designed to make package tracking effortless and reliable.");
        featuresSubtitle.addClassName("section-subtitle");

        Div featuresGrid = new Div();
        featuresGrid.addClassName("features-grid");

        featuresGrid.add(
                createFeatureCard(VaadinIcon.BELL, "Smart Notifications", "Get instant alerts when your packages are on the move or delivered."),
                //createFeatureCard(VaadinIcon.DASHBOARD, "Unified Dashboard", "Track all your shipments from multiple carriers in one place."),
                createFeatureCard(VaadinIcon.MOBILE, "Mobile Ready", "Access your tracking information anywhere, anytime on any device."),
                createFeatureCard(VaadinIcon.SHIELD, "Secure & Private", "Your data is protected with enterprise-grade security.")
                //createFeatureCard(VaadinIcon.CHART_LINE, "Analytics", "Get insights into your shipping patterns and delivery performance."),
                //createFeatureCard(VaadinIcon.USERS, "Team Collaboration", "Share tracking information with your team members seamlessly.")
        );

        featuresContent.add(featuresTitle, featuresSubtitle, featuresGrid);
        features.add(featuresContent);

        return features;
    }

    private Div createFeatureCard(VaadinIcon iconType, String title, String description) {
        Div card = new Div();
        card.addClassName("feature-card");

        Div iconContainer = new Div();
        iconContainer.addClassName("feature-icon-container");

        Icon icon = iconType.create();
        icon.addClassName("feature-icon");
        iconContainer.add(icon);

        H3 cardTitle = new H3(title);
        cardTitle.addClassName("feature-title");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.addClassName("feature-description");

        card.add(iconContainer, cardTitle, cardDescription);
        return card;
    }

    private Div createStatsSection() {
        Div stats = new Div();
        stats.addClassName("home-stats");

        Div statsContent = new Div();
        statsContent.addClassName("stats-content");

        Div statsGrid = new Div();
        statsGrid.addClassName("stats-grid");

        statsGrid.add(
                createStatCard("1M+", "Packages Tracked"),
                createStatCard("50K+", "Happy Customers"),
                createStatCard("99.9%", "Uptime"),
                createStatCard("24/7", "Support Available")
        );

        statsContent.add(statsGrid);
        stats.add(statsContent);

        return stats;
    }

    private Div createStatCard(String number, String label) {
        Div card = new Div();
        card.addClassName("stat-card");

        Span statNumber = new Span(number);
        statNumber.addClassName("stat-number");

        Span statLabel = new Span(label);
        statLabel.addClassName("stat-label");

        card.add(statNumber, statLabel);
        return card;
    }

    private Div createTestimonialsSection() {
        Div testimonials = new Div();
        testimonials.addClassName("home-testimonials");

        Div testimonialsContent = new Div();
        testimonialsContent.addClassName("testimonials-content");

        H2 testimonialsTitle = new H2("Trusted by Thousands");
        testimonialsTitle.addClassName("section-title");

        Paragraph testimonialsSubtitle = new Paragraph("See what our customers have to say about RemindPack.");
        testimonialsSubtitle.addClassName("section-subtitle");

        Div testimonialsGrid = new Div();
        testimonialsGrid.addClassName("testimonials-grid");

        testimonialsGrid.add(
                createTestimonialCard("RemindPack has transformed how I manage my online business. I can track 50+ shipments a week effortlessly.", "Steve Rogers.", "E-commerce Owner"),
                createTestimonialCard("Simple, fast, and effective. The notifications are incredibly helpful and never miss a beat.", "Natasha R.", "Small Business Owner"),
                createTestimonialCard("Best tracking dashboard I've ever used. The interface is intuitive and the data is always accurate.", "Asep K.", "Logistics Manager")
        );

        testimonialsContent.add(testimonialsTitle, testimonialsSubtitle, testimonialsGrid);
        testimonials.add(testimonialsContent);

        return testimonials;
    }

    private Div createTestimonialCard(String quote, String author, String role) {
        Div card = new Div();
        card.addClassName("testimonial-card");

        Icon quoteIcon = VaadinIcon.QUOTE_LEFT.create();
        quoteIcon.addClassName("testimonial-quote-icon");

        Paragraph quoteText = new Paragraph(quote);
        quoteText.addClassName("testimonial-text");

        Div authorInfo = new Div();
        authorInfo.addClassName("testimonial-author");

        Span authorName = new Span(author);
        authorName.addClassName("author-name");

        Span authorRole = new Span(role);
        authorRole.addClassName("author-role");

        authorInfo.add(authorName, authorRole);
        card.add(quoteIcon, quoteText, authorInfo);

        return card;
    }

    private Div createCtaSection() {
        Div cta = new Div();
        cta.addClassName("home-cta");

        Div ctaContent = new Div();
        ctaContent.addClassName("cta-content");

        H2 ctaTitle = new H2("Ready to Start Tracking?");
        ctaTitle.addClassName("cta-title");

        Paragraph ctaSubtitle = new Paragraph("Join thousands of satisfied customers and never lose track of your packages again.");
        ctaSubtitle.addClassName("cta-subtitle");

        Button ctaButton = new Button("Get Started for Free", VaadinIcon.ARROW_RIGHT.create());
        ctaButton.addClassName("cta-button");
        ctaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        ctaButton.addClickListener(e -> UI.getCurrent().navigate(RESI_DASHBOARD_PATH));

        ctaContent.add(ctaTitle, ctaSubtitle, ctaButton);
        cta.add(ctaContent);

        return cta;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassName("home-footer");

        Div footerContent = new Div();
        footerContent.addClassName("footer-content");

        // Footer brand
        Div footerBrand = new Div();
        footerBrand.addClassName("footer-brand");

        Span brandName = new Span("RemindPack");
        brandName.addClassName("footer-brand-name");

        Paragraph brandDescription = new Paragraph("Your trusted package tracking platform. Fast, simple, reliable.");
        brandDescription.addClassName("footer-brand-description");

        footerBrand.add(brandName, brandDescription);

        // Footer links
        Div footerLinks = new Div();
        footerLinks.addClassName("footer-links");

        Div linkColumn1 = new Div();
        linkColumn1.addClassName(FOOTER_LINK_COLUMN_CLASS);
        linkColumn1.add(
                new H4("Product"),
                new Anchor("#", "Features"),
                new Anchor("#", "Pricing"),
                new Anchor("#", "API"),
                new Anchor("#", "Integrations")
        );

        Div linkColumn2 = new Div();
        linkColumn2.addClassName(FOOTER_LINK_COLUMN_CLASS);
        linkColumn2.add(
                new H4("Company"),
                new Anchor("#", "About"),
                new Anchor("#", "Blog"),
                new Anchor("#", "Careers"),
                new Anchor("https://linkedin.com/in/hakimamarullah", "Contact")
        );

        Div linkColumn3 = new Div();
        linkColumn3.addClassName(FOOTER_LINK_COLUMN_CLASS);
        linkColumn3.add(
                new H4("Support"),
                new Anchor("https://linkedin.com/in/hakimamarullah", "Help Center"),
                new Anchor("#", "Documentation"),
                new Anchor("#", "Community"),
                new Anchor("#", "Status")
        );

        Div linkColumn4 = new Div();
        linkColumn4.addClassName(FOOTER_LINK_COLUMN_CLASS);
        linkColumn4.add(
                new H4("Legal"),
                new Anchor("#", "Privacy Policy"),
                new Anchor("#", "Terms of Service"),
                new Anchor("#", "Cookie Policy"),
                new Anchor("#", "GDPR")
        );

        footerLinks.add(linkColumn1, linkColumn2, linkColumn3, linkColumn4);
        footerContent.add(footerBrand, footerLinks);

        // Footer bottom
        Div footerBottom = new Div();
        footerBottom.addClassName("footer-bottom");

        Span copyright = new Span("© " + Year.now().getValue() + " RemindPack. All rights reserved.");
        copyright.addClassName("footer-copyright");

        footerBottom.add(copyright);
        footer.add(footerContent, footerBottom);

        return footer;
    }
}