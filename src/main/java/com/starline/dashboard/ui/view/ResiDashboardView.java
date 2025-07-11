package com.starline.dashboard.ui.view;

import com.starline.base.api.config.WebClientLoggingFilter;
import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.resi.CourierService;
import com.starline.base.api.resi.ResiService;
import com.starline.base.api.resi.dto.AddResiRequest;
import com.starline.base.api.resi.dto.CourierInfo;
import com.starline.base.api.resi.dto.ResiInfo;
import com.starline.base.ui.component.AppVerticalLayout;
import com.starline.base.ui.constant.StyleSheet;
import com.starline.base.ui.view.MainLayout;
import com.starline.security.AppUserInfo;
import com.starline.security.CurrentUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@PageTitle("Dashboard")
@Menu(title = "Dashboard", icon = "vaadin:cube", order = 1)
@Route(value = "resi-dashboard", layout = MainLayout.class)
@PermitAll
@Slf4j
public class ResiDashboardView extends AppVerticalLayout {


    private final transient AppUserInfo appUserInfo;

    private final transient ResiService resiService;

    private final transient CourierService courierService;

    private final transient TextField trackingNumberField = new TextField("Tracking Number");

    private final transient TextField additionalValue = new TextField("Additional Value");

    private final transient ComboBox<CourierInfo> courierComboBox = new ComboBox<>("Courier");

    private final transient Button addResiBtn = new Button("Add Resi");

    private final Binder<ResiData> resiDataBinder = new Binder<>();

    private final List<ResiInfo> resiDataList = new ArrayList<>();

    private final Grid<ResiInfo> resiGrid = new Grid<>();

    private final transient ResiData resiData = new ResiData();

    private final VerticalLayout resiLeftVerticalLayout = new VerticalLayout();

    public ResiDashboardView(CurrentUser currentUser, ResiService resiService, CourierService courierService) {
        super("Dashboard");
        this.appUserInfo = currentUser.require();
        this.resiService = resiService;
        this.courierService = courierService;



        // Set Courier
        courierComboBox.setAllowCustomValue(false);
        courierComboBox.setItemsPageable(this::getCouriersInfo);
        courierComboBox.setItemLabelGenerator(CourierInfo::getName);
        courierComboBox.addValueChangeListener(it -> handleAdditionalValueInfo(it.getValue()));
        courierComboBox.addCustomValueSetListener(it -> trackingNumberField.setEnabled(false));

        // Set Tracking Number
        trackingNumberField.setPlaceholder("input tracking number");
        trackingNumberField.setEnabled(false);
        trackingNumberField.setRequiredIndicatorVisible(true);

        // Hide Additional Value
        additionalValue.setVisible(false);


        // Add Resi Btn
        setupAddResiBtn();

        // Setup Binder
        setupResiDataValidator();

        // Setup form
        setupFormAddResi();

        // Setup Grid
        setupGrid();
    }

    @Getter
    static class CourierCode {

        private CourierCode() {
        }

        public static final String JNE = "JNE";
    }

    private void setupAddResiBtn() {
        addResiBtn.addClickListener(it -> handlerAddResi());
        addResiBtn.getStyle().set(StyleSheet.CURSOR, StyleSheet.CURSOR_POINTER);
    }

    private void setupGrid() {

        resiGrid.addColumn(ResiInfo::getTrackingNumber).setHeader("Tracking Number").setAutoWidth(true).setSortable(false);
        resiGrid.addColumn(ResiInfo::getCourierName).setHeader("Courier").setAutoWidth(true).setSortable(true);
        resiGrid.addColumn(ResiInfo::getLastCheckpointUpdate).setHeader("Last Update").setAutoWidth(true).setSortable(true);

        add(resiGrid);
    }



    private void setupFormAddResi() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(courierComboBox);

        resiLeftVerticalLayout.add(trackingNumberField);
        layout.add(resiLeftVerticalLayout);

        layout.add(addResiBtn);
        add(layout);
    }

    private void handlerAddResi() {
        Long courierId = Optional.ofNullable(courierComboBox.getValue()).map(CourierInfo::getId).orElse(null);
        String trackingNumber = resiData.getTrackingNumber();


        AddResiRequest payload = AddResiRequest.builder()
                .trackingNumber(trackingNumber)
                .userId(Long.valueOf(appUserInfo.getUserId().toString()))
                .courierId(courierId)
                .additionalValue1(resiData.getAdditionalValue())
                .build();
        resiService.addResi(payload)
                .subscribe(this::handleSuccessAddResi, this::handleAddResiBadRequestError);
        Notification.show("Resi Sent! You will receive an update if the process is success", 3000, Notification.Position.TOP_CENTER);
    }

    private void handleSuccessAddResi(ApiResponse<String> apiResponse) {
        getUI().ifPresent(ui -> ui.access(() -> Notification.show(apiResponse.getMessage(), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)));
    }

    private void handleAddResiBadRequestError(Throwable throwable) {
        WebClientLoggingFilter.ApiClientException ex = (WebClientLoggingFilter.ApiClientException) throwable;

        if (HttpStatus.BAD_REQUEST.value() != ex.getHttpStatusCode()) {
            throw ex;
        }

        getUI().ifPresent(ui -> ui.access(() -> {
            clearFieldErrors();
            ex.getFieldErrors().forEach(this::showFieldError);
        }));

        throw ex;
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


    private void setupResiDataValidator() {
        resiDataBinder.forField(trackingNumberField)
                .withValidator(new StringLengthValidator("Tracking number should not be empty", 1, 80))
                .bind(ResiData::getTrackingNumber, ResiData::setTrackingNumber);
    }

    private List<CourierInfo> getCouriersInfo(Pageable pageable, String filter) {
        ApiResponse<Page<CourierInfo>> data = courierService.getCouriers(filter, pageable).block();
        return Optional.ofNullable(data)
                .map(ApiResponse::getData)
                .map(Page::getContent)
                .orElse(new ArrayList<>());
    }

    private void handleAdditionalValueInfo(CourierInfo courierInfo) {
        Optional.ofNullable(courierInfo)
                .ifPresent(it -> {
                    if (Objects.equals(it.getCode(), CourierCode.JNE)) {
                        additionalValue.setPlaceholder("e.g 23972");
                        additionalValue.setHelperText("*Last 5 digits of package's receiver's phone number");
                        additionalValue.setRequiredIndicatorVisible(true);
                        additionalValue.setVisible(true);
                        resiLeftVerticalLayout.add(additionalValue);
                        resiDataBinder.forField(additionalValue)
                                .withValidator(new StringLengthValidator("Last 5 digits of mobile phone is required", 5, 5))
                                .bind(ResiData::getAdditionalValue, ResiData::setAdditionalValue);
                    } else {
                        resiLeftVerticalLayout.remove(additionalValue);
                        additionalValue.setVisible(false);
                    }
                    trackingNumberField.setEnabled(true);
                });
        if (Objects.isNull(courierInfo)) {
            log.info("Courier is null. Disable tracking number field");
            additionalValue.setVisible(false);
            trackingNumberField.setEnabled(false);
        }

    }

    @Data
    public static class ResiData {
        private String trackingNumber;
        private String additionalValue;
    }
}
