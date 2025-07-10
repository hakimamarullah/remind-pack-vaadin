package com.starline.security.prod;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Login")
@AnonymousAllowed
public class ProdLoginView extends Main implements BeforeEnterObserver {

    static final String LOGIN_PATH = "login";

    private final transient AuthenticationContext authenticationContext;
    private final LoginForm login;

    public ProdLoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        // Create the login form
        login = new LoginForm();
        login.setAction(LOGIN_PATH);
        login.setForgotPasswordButtonVisible(true); // We'll use a custom link below
        login.addForgotPasswordListener(e -> e.getSource().getUI().ifPresent(ui -> ui.navigate("forgot-password")));


        // Branding
        Icon logo = VaadinIcon.PACKAGE.create();
        logo.setSize("40px");
        H2 appName = new H2("RemindPack");
        HorizontalLayout branding = new HorizontalLayout(logo, appName);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.XLARGE);

        // Links: Forgot password and Register
        Anchor register = new Anchor("/register", "Register");
        HorizontalLayout linkRow = new HorizontalLayout(register);
        linkRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        linkRow.setWidthFull();
        linkRow.addClassName(LumoUtility.FontSize.SMALL);

        // Main layout
        VerticalLayout loginWrapper = new VerticalLayout(branding, login, linkRow);
        loginWrapper.setWidth("360px"); // ⬅️ Slightly bigger than default
        loginWrapper.setPadding(true);
        loginWrapper.setSpacing(true);
        loginWrapper.setAlignItems(FlexComponent.Alignment.STRETCH);
        loginWrapper.addClassName(LumoUtility.Gap.MEDIUM);

        // Full page centering
        setSizeFull();
        addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Background.CONTRAST_5);

        add(loginWrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo("");
            return;
        }

        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}
