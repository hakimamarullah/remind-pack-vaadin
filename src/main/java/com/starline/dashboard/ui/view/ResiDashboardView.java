package com.starline.dashboard.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.resi.CourierService;
import com.starline.base.api.resi.ResiService;
import com.starline.base.api.resi.dto.AddResiRequest;
import com.starline.base.api.resi.dto.CourierInfo;
import com.starline.base.api.resi.dto.ResiInfo;
import com.starline.base.ui.component.AppVerticalLayout;
import com.starline.base.ui.component.ConfirmDeleteDialog;
import com.starline.base.ui.component.CountDownTask;
import com.starline.base.ui.component.ReactiveCountDownTask;
import com.starline.base.ui.constant.StyleSheet;
import com.starline.base.ui.view.MainLayout;
import com.starline.security.AppUserInfo;
import com.starline.security.CurrentUser;
import com.starline.security.domain.UserId;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@PageTitle("Dashboard")
@Menu(title = "Dashboard", icon = "vaadin:cube", order = 1)
@Route(value = "resi-dashboard", layout = MainLayout.class)
@CssImport("./themes/default/dashboard.css")
@PermitAll
@Slf4j
@RegisterReflectionForBinding({
        ResiInfo.class,
        AppUserInfo.class,
        CourierInfo.class,
        ResiDashboardView.ResiData.class
})
public class ResiDashboardView extends AppVerticalLayout {

    private static final String FORM_FIELD_CLASS_NAME = "form-field";
    private static final String GRID_CELL_CONTENT_CSS_CLASS = "grid-cell-content";
    private static final String GRID_CELL_ICON_CSS_CLASS = "grid-cell-icon";
    private static final String GRID_CELL_TEXT_CSS_CLASS = "grid-cell-text";
    private static final String ADD_PACKAGE_TEXT = "Add Package";

    @Value("${app.max-resi-count:5}")
    private int maxResiCount;

    private final transient AppUserInfo appUserInfo;
    private final transient ResiService resiService;
    private final transient CourierService courierService;
    private final transient TextField trackingNumberField = new TextField("Tracking Number");
    private final transient TextField additionalValue = new TextField("Additional Value");
    private final transient ComboBox<CourierInfo> courierComboBox = new ComboBox<>("Courier");
    private final transient Button btnRefreshList = new Button();
    private final transient Button addResiBtn = new Button(ADD_PACKAGE_TEXT);

    private final transient CountDownTask simpleCountDownTask;
    private Binder<ResiData> resiDataBinder = new Binder<>();
    private final Grid<ResiInfo> resiGrid = new Grid<>();
    private final transient ResiData resiData = new ResiData();
    private final VerticalLayout additionalFieldContainer = new VerticalLayout();

    public ResiDashboardView(CurrentUser currentUser, ResiService resiService, CourierService courierService) {
        super("Dashboard");
        this.appUserInfo = currentUser.require();
        this.resiService = resiService;
        this.courierService = courierService;
        this.simpleCountDownTask = new ReactiveCountDownTask(20);

        addClassName("dashboard-main");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        setupComponents();
        setupLayout();

        UI ui = UI.getCurrent();
        refreshResiList(ui);

    }

    @Getter
    static class CourierCode {
        private CourierCode() {
        }

        public static final String JNE = "JNE";
    }


    private void setupComponents() {
        setupCourierComboBox();
        setupTrackingNumberField();
        setupAdditionalValueField();
        setupAddResiButton();
        setupResiDataValidator();
        setupGrid();
    }

    private void setupLayout() {
        // Main container
        Div mainContainer = new Div();
        mainContainer.setWidthFull();
        mainContainer.addClassName("dashboard-container");

        // Add package section
        Div addPackageSection = createAddPackageSection();

        // Package list section
        Div packageListSection = createPackageListSection();

        mainContainer.add(packageListSection, addPackageSection);
        add(mainContainer);
    }


    private Div createAddPackageSection() {
        Div addPackageSection = new Div();
        addPackageSection.addClassName("add-package-section");

        // Section header
        Div sectionHeader = new Div();
        sectionHeader.addClassName("section-header");

        Paragraph sectionTitle = new Paragraph("Add AWB");
        sectionTitle.addClassName("section-title");

        sectionHeader.add(sectionTitle);

        // Form container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");

        // Form layout
        Div formLayout = new Div();
        formLayout.addClassName("form-layout");

        // First row
        Div firstRow = new Div();
        firstRow.addClassName("form-row");
        firstRow.add(courierComboBox);
        courierComboBox.getElement().getThemeList().add("custom-color");


        // Second row
        Div secondRow = new Div();
        secondRow.addClassName("form-row");
        secondRow.add(trackingNumberField);
        trackingNumberField.getElement().getThemeList().add("custom-color");


        // Additional fields container
        additionalFieldContainer.addClassName("additional-fields-container");
        additionalFieldContainer.setPadding(false);
        additionalFieldContainer.setSpacing(false);

        // Button container
        Div buttonContainer = new Div();
        buttonContainer.addClassName("button-container");
        buttonContainer.add(addResiBtn);

        formLayout.add(firstRow, secondRow, additionalFieldContainer, buttonContainer);
        formContainer.add(formLayout);

        addPackageSection.add(sectionHeader, formContainer);
        return addPackageSection;
    }

    private Div createPackageListSection() {
        Div packageListSection = new Div();
        packageListSection.addClassName("package-list-section");

        // Section header
        Div sectionHeader = new Div();
        sectionHeader.addClassName("section-header");

        H4 sectionTitle = new H4("AWB List");
        sectionTitle.addClassName("section-title");

        sectionHeader.add(sectionTitle);

        // Btn Refresh
        Div btnContainer = new Div();
        btnRefreshList.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);
        btnRefreshList.setIcon(VaadinIcon.REFRESH.create());
        btnRefreshList.addClickListener(it -> handleManualRefreshList(it.getSource().getUI().orElse(null)));
        btnContainer.add(btnRefreshList);
        btnContainer.addClassName("package-list-btn-container");
        btnContainer.setWidthFull();

        // Grid container
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.add(resiGrid);

        packageListSection.add(sectionHeader, btnContainer, gridContainer);
        return packageListSection;
    }

    private void handleManualRefreshList(UI ui) {
        refreshResiList(ui);
        simpleCountDownTask.startCountdown(ui, () -> {
            btnRefreshList.setText(null);
            btnRefreshList.setEnabled(true);
        }, counter -> {
            btnRefreshList.setText(counter + "s");
            btnRefreshList.setEnabled(false);
        });
    }

    private void setupCourierComboBox() {
        courierComboBox.addClassName(FORM_FIELD_CLASS_NAME);
        courierComboBox.setPlaceholder("Select a courier");
        courierComboBox.setPrefixComponent(VaadinIcon.TRUCK.create());
        courierComboBox.setAllowCustomValue(false);
        courierComboBox.setPageSize(5);
        courierComboBox.setItemsPageable(this::getCouriersInfo);
        courierComboBox.setItemLabelGenerator(CourierInfo::getName);
        courierComboBox.addValueChangeListener(it -> handleAdditionalValueInfo(it.getValue()));
        courierComboBox.addCustomValueSetListener(it -> trackingNumberField.setEnabled(false));
        courierComboBox.setRequiredIndicatorVisible(true);
    }

    private void setupTrackingNumberField() {
        trackingNumberField.addClassName(FORM_FIELD_CLASS_NAME);
        trackingNumberField.setPlaceholder("Enter tracking number");
        trackingNumberField.setPrefixComponent(VaadinIcon.BARCODE.create());
        trackingNumberField.setEnabled(false);
        trackingNumberField.setWidthFull();
        trackingNumberField.setRequiredIndicatorVisible(true);
        trackingNumberField.setHelperText("Select a courier first to enable this field");
    }

    private void setupAdditionalValueField() {
        additionalValue.addClassName(FORM_FIELD_CLASS_NAME);
        additionalValue.setPrefixComponent(VaadinIcon.PHONE.create());
        additionalValue.setVisible(false);
    }

    private void setupAddResiButton() {
        addResiBtn.addClassName("add-package-btn");
        addResiBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addResiBtn.addClickListener(it -> handlerAddResi(it.getSource().getUI().orElse(null)));
    }

    private void setupGrid() {
        resiGrid.addClassName("package-grid");
        resiGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        resiGrid.setAllRowsVisible(true);

        // Tracking Number column with icon
        resiGrid.addComponentColumn(resiInfo -> {
            Div container = new Div();
            container.addClassName(GRID_CELL_CONTENT_CSS_CLASS);

            Icon icon = VaadinIcon.BARCODE.create();
            icon.addClassName(GRID_CELL_ICON_CSS_CLASS);

            Span text = new Span(resiInfo.getTrackingNumber());
            text.addClassName(GRID_CELL_TEXT_CSS_CLASS);

            container.add(icon, text);
            return container;
        }).setHeader("Tracking Number").setAutoWidth(true).setFlexGrow(1);

        // Courier column with icon
        resiGrid.addComponentColumn(resiInfo -> {
            Div container = new Div();
            container.addClassName(GRID_CELL_CONTENT_CSS_CLASS);

            Icon icon = VaadinIcon.TRUCK.create();
            icon.addClassName(GRID_CELL_ICON_CSS_CLASS);

            Span text = new Span(resiInfo.getCourierName());
            text.addClassName(GRID_CELL_TEXT_CSS_CLASS);

            container.add(icon, text);
            return container;
        }).setHeader("Courier").setAutoWidth(true).setFlexGrow(1);


        // Last Update column with icon
        resiGrid.addComponentColumn(resiInfo -> {
            Div container = new Div();
            container.addClassName(GRID_CELL_CONTENT_CSS_CLASS);

            Icon icon = VaadinIcon.CLOCK.create();
            icon.addClassName(GRID_CELL_ICON_CSS_CLASS);

            Function<LocalDateTime, String> dateTimeFormatter = dateTime -> {
                if (dateTime == null) {
                    return "unknown";
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm a");
                return dateTime.format(formatter);
            };
            Span text = new Span(dateTimeFormatter.apply(resiInfo.getLastCheckpointUpdate()));
            text.addClassName(GRID_CELL_TEXT_CSS_CLASS);

            container.add(icon, text);
            return container;
        }).setHeader("Last Update").setAutoWidth(true).setFlexGrow(1);

        // Actions column
        resiGrid.addComponentColumn(resiInfo -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.addClassName("grid-actions");

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addClassName("action-btn");
            deleteBtn.addClassName("action-btn-danger");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            deleteBtn.getElement().setAttribute("title", "Delete Package");
            deleteBtn.addClickListener(e -> handleDeleteResi(resiInfo, e.getSource().getUI().orElse(null)));

            actions.add(deleteBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
    }

    private void handleDeleteResi(ResiInfo resiInfo, UI ui) {
        new ConfirmDeleteDialog<ResiInfo>(() -> {
        })
                .show(resiInfo, ResiInfo::getTrackingNumber, item -> callDeleteResi(item, ui));
    }

    private void callDeleteResi(ResiInfo resiInfo, UI ui) {
        try {

            resiService.deleteResiByTrackingNumberAndUserId(resiInfo.getTrackingNumber(), getCurrentUserId())
                    .doOnSuccess(it -> refreshResiList(ui))
                    .subscribe();
        } catch (Exception e) {
            log.warn("Failed to delete resi: {}", resiInfo.getTrackingNumber());
        }
    }


    private void setupResiDataValidator() {
        resiDataBinder.forField(trackingNumberField)
                .withValidator(new StringLengthValidator("Tracking number should not be empty", 1, 80))
                .bind(ResiData::getTrackingNumber, ResiData::setTrackingNumber);

        resiDataBinder.setBean(resiData);
    }

    private void resetResiBinder() {

        resiDataBinder = new Binder<>();
        setupResiDataValidator();
    }

    private void handlerAddResi(UI ui) {
        if (!resiDataBinder.validate().isOk()) {
            showErrorNotification("Please fix the form errors before submitting");
            return;
        }

        int trackingCount = resiGrid.getListDataView().getItemCount();
        if (trackingCount >= maxResiCount) {
            String message = String.format("You have reached the maximum number of packages: %s%%nConsider deleting some packages before adding more!", maxResiCount);
            showErrorNotification(message);
            return;
        }


        Long courierId = Optional.ofNullable(courierComboBox.getValue()).map(CourierInfo::getId).orElse(null);
        String trackingNumber = resiData.getTrackingNumber();

        if (courierId == null) {
            courierComboBox.setInvalid(true);
            courierComboBox.setErrorMessage("Please select a courier");
            return;
        }

        AddResiRequest payload = AddResiRequest.builder()
                .trackingNumber(trackingNumber)
                .userId(Long.valueOf(appUserInfo.getUserId().toString()))
                .courierId(courierId)
                .additionalValue1(resiData.getAdditionalValue())
                .build();

        // Disable form during submission
        setFormEnabled(false);
        addResiBtn.setText("Adding...");
        addResiBtn.setIcon(VaadinIcon.SPINNER.create());

        try {

            var apiResponse = resiService.addResi(payload)
                    .blockOptional(Duration.ofSeconds(40));
            apiResponse.ifPresent(res -> handleSuccessAddResi(res, ui));
        } catch (WebClientLoggingFilter.ApiClientException ex) {
            handleAddResiBadRequestError(ex, ui);
        }

    }

    private void handleSuccessAddResi(ApiResponse<String> apiResponse, UI ui) {
        showSuccessNotification(apiResponse.getMessage());
        resetForm();
        setFormEnabled(true);
        refreshResiList(ui);
    }

    private void handleAddResiBadRequestError(Throwable throwable, UI ui) {
        log.error("❌ Failed to add resi: {}", throwable.getMessage(), throwable);
        Optional.ofNullable(ui).ifPresent(it -> it.access(() -> {
            setFormEnabled(true);
            addResiBtn.setText(ADD_PACKAGE_TEXT);
            addResiBtn.setIcon(VaadinIcon.PLUS.create());

            if (throwable instanceof WebClientLoggingFilter.ApiClientException ex) {
                switch (HttpStatus.valueOf(ex.getHttpStatusCode())) {
                    case HttpStatus.BAD_REQUEST:
                        clearFieldErrors();
                        ex.getFieldErrors().forEach(this::showFieldError);
                        showErrorNotification(Optional.ofNullable(ex.getErrorMessage()).orElse("Please fix the form errors before submitting"));
                        break;
                    case HttpStatus.CONFLICT, NOT_FOUND:
                        showErrorNotification(ex.getErrorMessage());
                        break;
                    default:
                        showErrorNotification("Unexpected error occurred. Please try again.");
                }
            } else {
                showErrorNotification("Network error. Please check your connection and try again.");
            }
        }));
    }

    private void setFormEnabled(boolean enabled) {
        courierComboBox.setEnabled(enabled);
        trackingNumberField.setEnabled(enabled && courierComboBox.getValue() != null);
        additionalValue.setEnabled(enabled);
        addResiBtn.setEnabled(enabled);

        if (!enabled) {
            addResiBtn.setText("Adding...");
            addResiBtn.setIcon(VaadinIcon.SPINNER.create());
        } else {
            addResiBtn.setText(ADD_PACKAGE_TEXT);
            addResiBtn.setIcon(VaadinIcon.PLUS.create());
        }
    }

    private void resetForm() {
        courierComboBox.clear();
        trackingNumberField.clear();
        additionalValue.clear();
        trackingNumberField.setEnabled(false);
        additionalValue.setVisible(false);
        additionalFieldContainer.removeAll();
        clearFieldErrors();
    }

    private void showFieldError(ApiResponse.FieldError fieldError) {
        if (Objects.isNull(fieldError)) {
            return;
        }
        switch (fieldError.getName()) {
            case "trackingNumber":
                trackingNumberField.setInvalid(true);
                trackingNumberField.setErrorMessage(fieldError.getMessage());
                break;
            case "courierId":
                courierComboBox.setInvalid(true);
                courierComboBox.setErrorMessage(fieldError.getMessage());
                break;
            case "additionalValue1":
                additionalValue.setInvalid(true);
                additionalValue.setErrorMessage(fieldError.getMessage());
                break;
            default:
                log.warn("Unknown field error: {}", fieldError.getName());
                break;
        }
    }

    private void clearFieldErrors() {
        courierComboBox.setInvalid(false);
        courierComboBox.setErrorMessage(null);

        trackingNumberField.setInvalid(false);
        trackingNumberField.setErrorMessage(null);

        additionalValue.setInvalid(false);
        additionalValue.setErrorMessage(null);
    }

    private List<CourierInfo> getCouriersInfo(Pageable pageable, String filter) {
        ApiResponse<Page<CourierInfo>> data = courierService.getCouriers(filter, pageable).block();
        return Optional.ofNullable(data)
                .map(ApiResponse::getData)
                .map(Page::getContent)
                .orElse(new ArrayList<>());
    }

    private void handleAdditionalValueInfo(CourierInfo courierInfo) {
        // Clear previous additional fields
        additionalFieldContainer.removeAll();

        Optional.ofNullable(courierInfo)
                .ifPresent(it -> {
                    if (Objects.equals(it.getCode(), CourierCode.JNE)) {
                        additionalValue.setPlaceholder("e.g. 23972");
                        additionalValue.setHelperText("Last 5 digits of package receiver's phone number");
                        additionalValue.setRequiredIndicatorVisible(true);
                        additionalValue.setVisible(true);
                        additionalFieldContainer.add(additionalValue);

                        // Update binder for additional value
                        resiDataBinder.forField(additionalValue)
                                .withValidator(new StringLengthValidator("Last 5 digits of mobile phone is required", 5, 5))
                                .bind(ResiData::getAdditionalValue, ResiData::setAdditionalValue);
                    } else {
                        additionalValue.setVisible(false);
                        additionalValue.setRequiredIndicatorVisible(false);
                        resetResiBinder();
                    }
                    trackingNumberField.setEnabled(true);
                    trackingNumberField.setHelperText("Enter the tracking number provided by " + it.getName());
                });

        if (Objects.isNull(courierInfo)) {
            log.debug("Courier is null. Disable tracking number field");
            additionalValue.setVisible(false);
            trackingNumberField.setEnabled(false);
            trackingNumberField.setHelperText("Select a courier first to enable this field");
        }
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    private void refreshResiList(UI ui) {

        if (Objects.isNull(appUserInfo)) {
            log.warn("Current User is null. Cannot refresh resi list");
            return;
        }

        resiService.getResiByUserId(getCurrentUserId())
                .subscribe(res -> handleSuccessFetchResi(res, ui), err -> handleErrorFetchResi(err, ui));
    }

    private void handleSuccessFetchResi(ApiResponse<List<ResiInfo>> apiResponse, UI ui) {
        List<ResiInfo> resiInfoList = apiResponse.getData();
        Optional.ofNullable(ui).ifPresent(it -> it.access(() -> {
            resiGrid.setItems(resiInfoList);
            it.push();
        }));
    }

    private void handleErrorFetchResi(Throwable throwable, UI ui) {
        log.warn("Failed to fetch resi list: {}", throwable.getMessage());
        Optional.ofNullable(ui).ifPresent(it -> it.access(() -> {
            showErrorNotification("Failed to fetch resi list");
            it.push();
        }));
    }


    private Long getCurrentUserId() {
        return Optional.ofNullable(appUserInfo)
                .map(AppUserInfo::getUserId)
                .map(UserId::toString)
                .map(Long::valueOf)
                .orElse(null);
    }


    @Data
    @RegisterReflection
    public static class ResiData {
        private String trackingNumber;
        private String additionalValue;
    }
}