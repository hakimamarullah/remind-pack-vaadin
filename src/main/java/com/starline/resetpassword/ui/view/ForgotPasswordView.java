package com.starline.resetpassword.ui.view;

import com.starline.base.api.users.OTPService;
import com.starline.base.api.users.ResetPasswordService;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@PageTitle("Forgot Password")
@AnonymousAllowed
@PermitAll
public class ForgotPasswordView extends Main implements BeforeEnterObserver {

    private final transient ResetPasswordService passwordResetService;
    private final transient OTPService otpService;

    private final TextField phoneField = new TextField("Mobile Phone");
    private final TextField otpField = new TextField("OTP");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");

    private final Button sendOtpBtn = new Button("Send OTP");
    private final Button resetPasswordBtn = new Button("Reset Password");
    private final Button backToLoginBtn = new Button("Back to Login");

    private final Binder<PasswordReset> binder = new Binder<>();
    private final transient PasswordReset passwordReset = new PasswordReset();
    private final transient AuthenticationContext authenticationContext;

    private int countdown = 30;
    private final transient ScheduledExecutorService scheduler;
    private transient ScheduledFuture<?> countdownTask;


    public ForgotPasswordView(ResetPasswordService passwordResetService, OTPService otpService, AuthenticationContext authenticationContext) {
        this.passwordResetService = passwordResetService;
        this.otpService = otpService;
        this.authenticationContext = authenticationContext;

        setSizeFull();
        addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Background.CONTRAST_5);

        // Branding
        Icon logo = VaadinIcon.PACKAGE.create();
        logo.setSize("40px");
        H2 appName = new H2("RemindPack");
        H2 subtitle = new H2("Forgot Password");
        subtitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.NORMAL);

        HorizontalLayout branding = new HorizontalLayout(logo, appName);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.XLARGE);

        // Step 1: Phone and OTP input
        HorizontalLayout phoneOtpLayout = new HorizontalLayout(phoneField, sendOtpBtn);
        phoneOtpLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        phoneOtpLayout.setWidthFull();
        phoneField.setWidthFull();
        phoneField.setPlaceholder("e.g. 62812345678");



        HorizontalLayout otpLayout = new HorizontalLayout(otpField);
        otpLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        otpLayout.setWidthFull();
        otpField.setWidthFull();
        otpField.setHelperText("6-digit codes sent to your Whatsapp");
        otpField.setValueChangeMode(ValueChangeMode.EAGER);

        // Step 2: Password reset
        HorizontalLayout newPasswordLayout = createPasswordField(newPasswordField);
        HorizontalLayout confirmPasswordLayout = createPasswordField(confirmPasswordField);


        // Buttons
        resetPasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resetPasswordBtn.getStyle().set("cursor", "pointer");
        resetPasswordBtn.setEnabled(false);


        backToLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginBtn.getStyle().set("cursor", "pointer");

        // Form layout with white background
        VerticalLayout form = new VerticalLayout();
        form.add(branding,
                subtitle,
                phoneOtpLayout,
                otpLayout,
                newPasswordLayout,
                confirmPasswordLayout,
                resetPasswordBtn,
                backToLoginBtn);
        form.setWidth("360px");
        form.setAlignItems(FlexComponent.Alignment.STRETCH);
        form.setSpacing(true);
        form.addClassName(LumoUtility.Gap.MEDIUM);
        form.addClassName(LumoUtility.Padding.LARGE);
        form.addClassName(LumoUtility.BorderRadius.MEDIUM);
        form.getStyle().set("background-color", "white");


        add(form);

        // Setup validators
        setupValidators();

        // Add value change listeners for password fields
        newPasswordField.addValueChangeListener(e -> validateAndUpdateResetButton());
        newPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        phoneField.addValueChangeListener(e -> setSendOTPBtn());
        phoneField.setValueChangeMode(ValueChangeMode.EAGER);

        confirmPasswordField.addValueChangeListener(e -> validateAndUpdateResetButton());
        confirmPasswordField.setValueChangeMode(ValueChangeMode.EAGER);

        // Actions
        sendOtpBtn.addClickListener(e -> handleSendOtp());
        resetPasswordBtn.addClickListener(e -> handleResetPassword());
        backToLoginBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.access(() ->
                ui.navigate("/login")
        )));

        // Initialize scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is already logged in
        if (authenticationContext.isAuthenticated()) {
            // Go back to previous page or home if no previous page
            getUI().ifPresent(ui -> ui.getPage().getHistory().back());
        }
    }

    private void setSendOTPBtn() {
        if (phoneField.isInvalid()) {
            sendOtpBtn.setEnabled(false);
            return;
        }
        sendOtpBtn.setEnabled(true);
    }


    private void setupValidators() {
        // Phone field validator
        binder.forField(phoneField)
                .withValidator(new StringLengthValidator("Phone number must be at least 10 digits", 10, 15))
                .withValidator(new RegexpValidator("Phone number must contain only digits", "\\d+"))
                .bind(PasswordReset::getPhone, PasswordReset::setPhone);

        // OTP field validator
        binder.forField(otpField)
                .withValidator(new StringLengthValidator("OTP must be 6 digits", 6, 6))
                .withValidator(new RegexpValidator("OTP must contain only digits", "\\d+"))
                .bind(PasswordReset::getOtp, PasswordReset::setOtp);

        // New password field validator
        binder.forField(newPasswordField)
                .withValidator(new StringLengthValidator("Password must be at least 8 characters", 8, 30))
                .bind(PasswordReset::getNewPassword, PasswordReset::setNewPassword);

        // Confirm password field validator
        binder.forField(confirmPasswordField)
                .withValidator(value -> value.equals(newPasswordField.getValue()),
                        "Passwords do not match")
                .bind(PasswordReset::getConfirmPassword, PasswordReset::setConfirmPassword);

        binder.setBean(passwordReset);
    }

    private void handleSendOtp() {
        String phoneValue = phoneField.getValue();

       if (phoneValue == null || phoneValue.trim().isEmpty()) {
           Notification.show("Phone number is required", 3000, Notification.Position.TOP_CENTER)
                   .addThemeVariants(NotificationVariant.LUMO_WARNING);
           return;
       }

        // Call service to send OTP
        otpService.sendOTPAsync(phoneValue);

        Notification.show("OTP will be sent to Whatsapp at " + phoneValue, 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        startResendCountdown();
    }



    private void handleResetPassword() {
        try {
            binder.writeBean(passwordReset);

            String phone = passwordReset.getPhone();
            String otp = passwordReset.getOtp();
            String newPassword = passwordReset.getNewPassword();

            boolean success = passwordResetService.resetPassword(phone, otp, newPassword);
            if (success) {
                Notification.show("Password reset successful! Please login with your new password.");
                UI.getCurrent().navigate("login");
            } else {
                Notification.show("Password reset failed. Please try again.");
            }
        } catch (ValidationException e) {
            Notification.show("Please fix the validation errors", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void validateAndUpdateResetButton() {
        // Check if password fields have values and are valid
        boolean hasValues = !newPasswordField.isEmpty() && !confirmPasswordField.isEmpty();
        boolean passwordsMatch = newPasswordField.getValue().equals(confirmPasswordField.getValue());


        resetPasswordBtn.setEnabled(hasValues && passwordsMatch);
    }

    private HorizontalLayout createPasswordField(PasswordField field) {
        field.setWidthFull();
        HorizontalLayout layout = new HorizontalLayout(field);
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setWidthFull();
        layout.setFlexGrow(1, field);
        return layout;
    }

    private void startResendCountdown() {
        countdown = 30;

        // Cancel existing task if running
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel(true);
        }

        countdownTask = scheduler.scheduleAtFixedRate(() -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                if (countdown <= 0) {
                    sendOtpBtn.setText("Resend OTP");
                    sendOtpBtn.setEnabled(true);
                    if (countdownTask != null) {
                        countdownTask.cancel(true);
                    }
                } else {
                    sendOtpBtn.setText("Resend in " + countdown + "s");
                    countdown--;
                }
            }));
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Clean up scheduler when component is detached
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel(true);
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private String phone;
        private String otp;
        private String newPassword;
        private String confirmPassword;
    }
}