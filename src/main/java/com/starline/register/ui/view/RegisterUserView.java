package com.starline.register.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.OTPService;
import com.starline.base.api.users.RegistrationService;
import com.starline.base.api.users.dto.RegisterUserRequest;
import com.starline.base.ui.component.CountDownTask;
import com.vaadin.flow.component.DetachEvent;
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
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@PageTitle("Register")
@AnonymousAllowed
public class RegisterUserView extends Main implements BeforeEnterObserver {

    private final transient RegistrationService registrationService;
    private final transient OTPService otpService;

    private final TextField phoneField = new TextField("Mobile Phone");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");
    private final TextField otpField = new TextField("OTP");

    private final Button sendOtpBtn = new Button("Send OTP");
    private final Button registerBtn = new Button("Sign Up");

    private final Binder<UserRegistration> binder = new Binder<>();
    private final transient UserRegistration userRegistration = new UserRegistration();


    private final transient CountDownTask countDownTask;
    private final transient AuthenticationContext authenticationContext;


    public RegisterUserView(RegistrationService registrationService, OTPService otpService, AuthenticationContext authenticationContext) {
        this.otpService = otpService;
        this.authenticationContext = authenticationContext;
        this.registrationService = registrationService;

        setSizeFull();
        addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Background.CONTRAST_5);

        // Branding
        Icon logo = VaadinIcon.PACKAGE.create();
        logo.setSize("40px");
        H2 appName = new H2("RemindPack");
        HorizontalLayout branding = new HorizontalLayout(logo, appName);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.XLARGE);

        // Password field with toggle
        HorizontalLayout passwordLayout = createPasswordField(passwordField);
        HorizontalLayout confirmLayout = createPasswordField(confirmPasswordField);

        // OTP input - always visible
        HorizontalLayout otpLayout = new HorizontalLayout(otpField, sendOtpBtn);
        otpLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        // Register button
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.getStyle().set("cursor", "pointer");

        // Back to login button
        Button backToLoginBtn = new Button("Back to Login");
        backToLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backToLoginBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.access(() ->
                ui.navigate("/login")
        )));
        backToLoginBtn.getStyle().set("cursor", "pointer");

        // Form layout with white background
        VerticalLayout form = new VerticalLayout();
        form.add(branding,
                phoneField,
                passwordLayout,
                confirmLayout,
                otpLayout,
                registerBtn,
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

        // Add value change listeners to validate and enable/disable register button
        phoneField.addValueChangeListener(e -> validateAndUpdateRegisterButton(registerBtn));
        passwordField.addValueChangeListener(e -> validateAndUpdateRegisterButton(registerBtn));
        confirmPasswordField.addValueChangeListener(e -> validateAndUpdateRegisterButton(registerBtn));
        otpField.addValueChangeListener(e -> validateAndUpdateRegisterButton(registerBtn));

        phoneField.setValueChangeMode(ValueChangeMode.EAGER);
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);
        confirmPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        otpField.setValueChangeMode(ValueChangeMode.EAGER);

        // Actions
        sendOtpBtn.addClickListener(e -> handleSendOTP());

        registerBtn.setEnabled(false);
        registerBtn.addClickListener(e -> handleRegister());

        countDownTask = new CountDownTask(30);
    }

    private void handleSendOTP() {
        if (phoneField.isEmpty()) {
            Notification.show("Phone number is required", 3000, Notification.Position.TOP_CENTER, true);
            return;
        }

        otpService.sendOTPAsync(phoneField.getValue());
        Notification.show(String.format("OTP will be sent to Whatsapp: %s", phoneField.getValue()), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        sendOtpBtn.setEnabled(false);
        startResendCountdown();
    }

    private void validateAndUpdateRegisterButton(Button registerBtn) {
        // Check if all fields have values
        boolean hasAllValues = !phoneField.isEmpty() &&
                !passwordField.isEmpty() &&
                !confirmPasswordField.isEmpty() &&
                !otpField.isEmpty();

        // Check if binder validation passes
        boolean isValid = hasAllValues && !binder.validate().hasErrors();

        registerBtn.setEnabled(isValid);
    }

    private void setupValidators() {
        // Phone field validator
        binder.forField(phoneField)
                .withValidator(new StringLengthValidator("Phone number must be at least 10 digits", 10, 15))
                .withValidator(new RegexpValidator("Phone number must contain only digits", "\\d+"))
                .bind(UserRegistration::getPhone, UserRegistration::setPhone);

        // Password field validator
        binder.forField(passwordField)
                .withValidator(new StringLengthValidator("Password must be at least 8 characters", 8, 100))
                .bind(UserRegistration::getPassword, UserRegistration::setPassword);

        // Confirm password field validator
        binder.forField(confirmPasswordField)
                .withValidator(value -> value.equals(passwordField.getValue()),
                        "Passwords do not match")
                .bind(UserRegistration::getConfirmPassword, UserRegistration::setConfirmPassword);

        // OTP field validator
        binder.forField(otpField)
                .withValidator(new StringLengthValidator("OTP must be 6 digits", 6, 6))
                .withValidator(new RegexpValidator("OTP must contain only digits", "\\d+"))
                .bind(UserRegistration::getOtp, UserRegistration::setOtp);

        binder.setBean(userRegistration);
    }

    @SuppressWarnings("unchecked")
    private void handleRegister() {
        try {
            binder.writeBean(userRegistration);

            String phone = userRegistration.getPhone();
            String password = userRegistration.getPassword();
            String otp = userRegistration.getOtp();

            RegisterUserRequest payload = RegisterUserRequest.builder()
                    .phoneNumber(phone)
                    .password(password)
                    .confirmPassword(confirmPasswordField.getValue())
                    .otp(otp)
                    .build();

            setFormEnabled(false);
            registrationService.registerUser(payload)
                    .subscribe(this::registrationSuccessHandler,
                            error -> {
                                if (error instanceof WebClientLoggingFilter.ApiClientException ex) {
                                    handleRegistrationError(ex);
                                }
                            }
                    );

        } catch (ValidationException e) {
            Notification.show("Please make sure all fields are valid.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void registrationSuccessHandler(ApiResponse<String> apiResponse) {
        // Success case
        if (apiResponse.getCode() == HttpStatus.CREATED.value()) {
            getUI().ifPresent(ui -> ui.access(() -> {
                Notification.show("Registration successful!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                ui.navigate("/login");
            }));
        }

    }

    private void handleRegistrationError(WebClientLoggingFilter.ApiClientException ex) {
        List<ApiResponse.FieldError> fieldErrors = Optional.ofNullable(ex.getApiResponse())
                .map(ApiResponse::getFieldErrors)
                .orElse(new ArrayList<>());

        setFormEnabled(true);
        if (!fieldErrors.isEmpty()) {
            // Clear previous validation errors on all fields
            clearFieldErrors();

            // Apply field-specific errors
            fieldErrors.forEach(fieldError -> {
                String fieldName = fieldError.getName();
                String errorMessage = fieldError.getMessage();

                // Map API field names to your form field names if needed
                String mappedFieldName = mapApiFieldToFormField(fieldName);

                // Show the error directly on the field
                getUI().ifPresent(ui -> ui.access(() -> showFieldError(mappedFieldName, errorMessage)));

            });

            // Show general error message
            getUI().ifPresent(ui -> ui.access(() -> Notification.show("Please fix the highlighted errors and try again.", 3000,
                            Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)));
            return;
        }

        show4xxError(ex.getErrorMessage());
    }

    private void setFormEnabled(boolean enabled) {
        getUI().ifPresent(ui -> ui.access(() -> {
            phoneField.setEnabled(enabled);
            passwordField.setEnabled(enabled);
            confirmPasswordField.setEnabled(enabled);
            otpField.setEnabled(enabled);
            registerBtn.setEnabled(enabled);

            if (!enabled) {
                registerBtn.setText("Signing up...");
                registerBtn.setIcon(VaadinIcon.SPINNER.create());
            } else {
                registerBtn.setText("Sign Up");
                registerBtn.setIcon(null);
            }
        }));
    }

    private void show4xxError(String message) {
        getUI().ifPresent(ui -> ui.access(() -> Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING)));

    }

    private String mapApiFieldToFormField(String apiFieldName) {
        return switch (apiFieldName) {
            case "phoneNumber" -> "phone";
            case "otp" -> "otp";
            default -> apiFieldName;
        };
    }

    private void clearFieldErrors() {
        // Clear error states from all form fields
        getUI().ifPresent(ui -> ui.access(() -> {
            phoneField.setInvalid(false);
            phoneField.setErrorMessage(null);
            passwordField.setInvalid(false);
            passwordField.setErrorMessage(null);
            confirmPasswordField.setInvalid(false);
            confirmPasswordField.setErrorMessage(null);
            otpField.setInvalid(false);
            otpField.setErrorMessage(null);
        }));
    }

    private void showFieldError(String fieldName, String errorMessage) {
        // Apply error to specific field
        switch (fieldName) {
            case "phone":
                phoneField.setErrorMessage(errorMessage);
                phoneField.setInvalid(true);
                break;
            case "password":
                passwordField.setErrorMessage(errorMessage);
                passwordField.setInvalid(true);
                break;
            case "confirmPassword":
                confirmPasswordField.setErrorMessage(errorMessage);
                confirmPasswordField.setInvalid(true);
                break;
            case "otp":
                otpField.setErrorMessage(errorMessage);
                otpField.setInvalid(true);
                break;

            default:
                break;
        }
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
                getUI().orElse(null),
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

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Clean up scheduler when component is detached
        if (!Objects.isNull(countDownTask)) {
            countDownTask.shutdown();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo("");
        }
    }


    @Data
    public static class UserRegistration {
        private String phone;
        private String password;
        private String confirmPassword;
        private String otp;
    }
}