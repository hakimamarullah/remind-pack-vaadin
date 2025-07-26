package com.starline.resetpassword.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.OTPService;
import com.starline.base.api.users.ResetPasswordService;
import com.starline.base.api.users.dto.ResetPasswordRequest;
import com.starline.base.ui.component.CountDownTask;
import com.starline.base.ui.component.ReactiveCountDownTask;
import com.starline.base.ui.constant.StyleSheet;
import com.vaadin.flow.component.Key;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@PageTitle("Forgot Password")
@AnonymousAllowed
@PermitAll
@Slf4j
public class ForgotPasswordView extends Main implements BeforeEnterObserver {

    private final transient ResetPasswordService passwordResetService;
    private final transient OTPService otpService;

    private final TextField phoneField = new TextField("Mobile Phone");
    private final TextField otpField = new TextField("OTP");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");

    private final Button sendOtpBtn = new Button("Send OTP");
    private final Button resetPasswordBtn = new Button("Reset Password");

    private final transient CountDownTask countDownTask;
    private final Binder<PasswordReset> binder = new Binder<>();
    private final transient PasswordReset passwordReset = new PasswordReset();
    private final transient AuthenticationContext authenticationContext;


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
        resetPasswordBtn.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);
        resetPasswordBtn.setEnabled(false);


        Button backToLoginBtn = new Button("Back to Login");
        backToLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginBtn.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);

        sendOtpBtn.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);

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


        // Set focus
        phoneField.addKeyDownListener(Key.ENTER, e -> e.getSource().getUI().ifPresent(ui -> ui.access(otpField::focus)));
        otpField.addKeyDownListener(Key.ENTER, e -> e.getSource().getUI().ifPresent(ui -> ui.access(newPasswordField::focus)));
        newPasswordField.addKeyDownListener(Key.ENTER, e -> e.getSource().getUI().ifPresent(ui -> ui.access(confirmPasswordField::focus)));

        countDownTask = new ReactiveCountDownTask(30);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is already logged in
        if (authenticationContext.isAuthenticated()) {
            // Go back to previous page or home if no previous page
            getCurrentUI().ifPresent(ui -> ui.getPage().getHistory().back());
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


    private void setFormEnabled(boolean enabled) {
        getCurrentUI().ifPresent(ui -> ui.access(() -> {
            phoneField.setEnabled(enabled);
            otpField.setEnabled(enabled);
            newPasswordField.setEnabled(enabled);
            confirmPasswordField.setEnabled(enabled);
            resetPasswordBtn.setEnabled(enabled);

            if (!enabled) {
                resetPasswordBtn.setText("Submitting...");
                resetPasswordBtn.setIcon(VaadinIcon.SPINNER.create());
            } else {
                resetPasswordBtn.setText("Reset Password");
                resetPasswordBtn.setIcon(null);
            }
        }));
    }

    private void handleResetPassword() {
        try {
            binder.writeBean(passwordReset);

            String phone = passwordReset.getPhone();
            String otp = passwordReset.getOtp();
            String newPassword = passwordReset.getNewPassword();

            ResetPasswordRequest payload = ResetPasswordRequest.builder()
                    .phoneNumber(phone)
                    .otp(otp)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPassword)
                    .build();
            setFormEnabled(false);
            var apiResponse = passwordResetService.resetPassword(payload).blockOptional(Duration.ofSeconds(40));
            apiResponse.ifPresent(this::handleResetPasswordResponse);
        } catch (ValidationException e) {
            Notification.show("Please fix the validation errors", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            setFormEnabled(true);
        } catch (Exception e) {
            handleResetPasswordError(e);
        }
    }

    private void handleResetPasswordResponse(ApiResponse<String> response) {
        getCurrentUI().ifPresent(ui -> ui.access(() -> {
            Notification.show(response.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            setFormEnabled(true);
            ui.navigate("/login");
        }));
    }

    private void handleResetPasswordError(Throwable ex) {
        try {
            if (ex instanceof WebClientRequestException) {
                getCurrentUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Something went wrong. Please try again later!", 3000, Notification.Position.TOP_CENTER);
                    setFormEnabled(true);
                }));
                return;
            }
            WebClientLoggingFilter.ApiClientException exception = (WebClientLoggingFilter.ApiClientException) ex;
            if (HttpStatus.BAD_REQUEST.value() == exception.getHttpStatusCode()) {
                getCurrentUI().ifPresent(ui -> ui.access(() -> handleFieldErrors(exception.getFieldErrors())));
                return;
            }
            setFormEnabled(true);
            show4xxError(exception.getErrorMessage());
        } catch (Exception e) {
            handleUnexpectedErrorResetPassword(e);
        }
    }

    private void handleUnexpectedErrorResetPassword(Throwable ex) {
        log.warn("Error resetting password: {}", ex.getMessage());
        getCurrentUI().ifPresent(ui -> ui.access(() ->
                Notification.show("Something went wrong. Please try again later!", 3000, Notification.Position.TOP_CENTER)
        ));
        setFormEnabled(true);
    }

    private void show4xxError(String message) {
        getCurrentUI().ifPresent(ui -> ui.access(() -> Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING)));
    }

    private void handleFieldErrors(List<ApiResponse.FieldError> fieldErrors) {
        if (!fieldErrors.isEmpty()) {
            clearFieldErrors();
        }
        fieldErrors.forEach(it -> {
            switch (it.getName()) {
                case "phoneNumber":
                    phoneField.setErrorMessage(it.getMessage());
                    phoneField.setInvalid(true);
                    break;
                case "otp":
                    otpField.setErrorMessage(it.getMessage());
                    otpField.setInvalid(true);
                    break;
                case "newPassword":
                    newPasswordField.setErrorMessage(it.getMessage());
                    newPasswordField.setInvalid(true);
                    break;
                case "confirmNewPassword":
                    confirmPasswordField.setErrorMessage(it.getMessage());
                    confirmPasswordField.setInvalid(true);
                    break;
                default:
                    log.warn("Unknown field error: {}", it.getName());
                    break;
            }
        });
    }

    private void clearFieldErrors() {
        getCurrentUI().ifPresent(ui -> ui.access(() -> {
            phoneField.setErrorMessage(null);
            phoneField.setInvalid(false);
            otpField.setErrorMessage(null);
            otpField.setInvalid(false);
            newPasswordField.setErrorMessage(null);
            newPasswordField.setInvalid(false);
            confirmPasswordField.setErrorMessage(null);
            confirmPasswordField.setInvalid(false);
        }));
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
        countDownTask.startCountdown(
                getCurrentUI().orElse(null),
                () -> {
                    sendOtpBtn.setText("Resend OTP");
                    sendOtpBtn.setEnabled(true);
                },
                counter -> {
                    sendOtpBtn.setText("Resend in " + counter + "s");
                    sendOtpBtn.setEnabled(false);
                }
        );

    }


    private Optional<UI> getCurrentUI() {
        UI ui = UI.getCurrent();
        if (Objects.isNull(ui)) {
            log.warn("Current UI is null. Trying to get from getUI() instead");
            return getUI();
        }
        return Optional.of(ui);
    }

    @Getter
    @Setter
    @RegisterReflectionForBinding(PasswordReset.class)
    public static class PasswordReset {
        private String phone;
        private String otp;
        private String newPassword;
        private String confirmPassword;
    }
}