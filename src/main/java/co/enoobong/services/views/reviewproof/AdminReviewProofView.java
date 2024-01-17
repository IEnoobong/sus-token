package co.enoobong.services.views.reviewproof;

import co.enoobong.services.data.ActionStatus;
import co.enoobong.services.data.entities.SusActionEntity;
import co.enoobong.services.services.SusActionAdminService;
import co.enoobong.services.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Admin Review Proof")
@Route(value = "admin/requests/:actionId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class AdminReviewProofView extends Div implements BeforeEnterObserver {

  private static final String ACTION_ROUTE_EDIT_TEMPLATE = "admin/requests/%s/edit";

  private final Grid<SusActionEntity> grid = new Grid<>(SusActionEntity.class, false);

  private final Button cancel = new Button("Cancel");
  private final Button save = new Button("Save");
  private final BeanValidationBinder<SusActionEntity> binder;
  private final Filters filters;
  private final transient SusActionAdminService adminService;
  private TextField walletAddress;
  private TextField description;
  @PropertyId("status")
  private ComboBox<ActionStatus> statusComboBox;
  private TextField statusNote;
  private Image actionProof;
  private transient SusActionEntity selectedItem;

  public AdminReviewProofView(SusActionAdminService adminService) {
    this.adminService = adminService;
    addClassNames("master-detail-view");

    filters = new Filters(this::refreshGrid);

    // Create UI
    SplitLayout splitLayout = new SplitLayout();

    createGridLayout(splitLayout);
    createEditorLayout(splitLayout);

    VerticalLayout layout = new VerticalLayout(filters, splitLayout);
    layout.setSizeFull();
    layout.setPadding(false);
    layout.setSpacing(false);

    add(layout);

    // Configure Grid
    grid.addColumn("walletAddress").setAutoWidth(true);
    grid.addColumn("description").setAutoWidth(true);
    grid.addColumn("status").setAutoWidth(true);

    grid.setItems(query -> adminService.getSusActions(filters,
            PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query)))
        .stream());
    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);

    // when a row is selected or deselected, populate form
    grid.asSingleSelect().addValueChangeListener(event -> {
      if (event.getValue() != null) {
        UI.getCurrent()
            .navigate(String.format(ACTION_ROUTE_EDIT_TEMPLATE, event.getValue().getId()));
      } else {
        clearForm();
        UI.getCurrent().navigate(AdminReviewProofView.class);
      }
    });

    // Configure Form
    binder = new BeanValidationBinder<>(SusActionEntity.class);

    // Bind fields. This is where you'd define e.g. validation rules

    binder.bindInstanceFields(this);

    cancel.addClickListener(e -> {
      clearForm();
      refreshGrid();
    });

    save.addClickListener(e -> {
      try {
        if (this.selectedItem == null) {
          this.selectedItem = new SusActionEntity();
        }
        binder.writeBean(this.selectedItem);
        adminService.validateSusAction(this.selectedItem);
        clearForm();
        refreshGrid();

        var notification = new Notification("Proof updated", 3000,
            Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
        UI.getCurrent().navigate(AdminReviewProofView.class);
      } catch (ObjectOptimisticLockingFailureException exception) {
        Notification n = Notification.show(
            "Error updating the data. Somebody else has updated the record while you were making changes.");
        n.setPosition(Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
      } catch (ValidationException validationException) {
        Notification.show("Failed to update the data. Check again that all values are valid");
      }
    });
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Optional<UUID> actionId = event.getRouteParameters().get("actionId")
        .map(UUID::fromString);
    if (actionId.isPresent()) {
      Optional<SusActionEntity> susActionFromBackend = adminService.getSusAction(actionId.get());
      if (susActionFromBackend.isPresent()) {
        populateForm(susActionFromBackend.get());
      } else {
        Notification.show(
            String.format("The requested action was not found, ID = %s",
                actionId.get()), 3000,
            Notification.Position.BOTTOM_START);
        // when a row is selected but the data is no longer available,
        // refresh grid
        refreshGrid();
        event.forwardTo(AdminReviewProofView.class);
      }
    }
  }

  private void createEditorLayout(SplitLayout splitLayout) {
    Div editorLayoutDiv = new Div();
    editorLayoutDiv.setClassName("editor-layout");

    Div editorDiv = new Div();
    editorDiv.setClassName("editor");
    editorLayoutDiv.add(editorDiv);

    FormLayout formLayout = new FormLayout();
    walletAddress = new TextField("Wallet Address");
    walletAddress.setReadOnly(true);

    description = new TextField("Description");
    description.setReadOnly(true);

    statusComboBox = new ComboBox<>("Status", ActionStatus.values());

    statusNote = new TextField("Status Note");

    actionProof = new Image();
    actionProof.setWidth("100%");

    formLayout.add(walletAddress, description, statusComboBox, statusNote, actionProof);

    editorDiv.add(formLayout);
    createButtonLayout(editorLayoutDiv);

    splitLayout.addToSecondary(editorLayoutDiv);
  }

  private void createButtonLayout(Div editorLayoutDiv) {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setClassName("button-layout");
    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    buttonLayout.add(save, cancel);
    editorLayoutDiv.add(buttonLayout);
  }

  private void createGridLayout(SplitLayout splitLayout) {
    Div wrapper = new Div();
    wrapper.setClassName("grid-wrapper");
    splitLayout.addToPrimary(wrapper);
    wrapper.add(grid);
  }

  private void refreshGrid() {
    grid.select(null);
    grid.getDataProvider().refreshAll();
  }

  private void clearForm() {
    populateForm(null);
  }

  private void populateForm(SusActionEntity value) {
    this.selectedItem = value;
    binder.readBean(this.selectedItem);

    if (selectedItem != null) {
      var streamResource = new StreamResource("proof-stream", () -> {
        try {
          return new FileInputStream(selectedItem.getProofData());
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      });
      actionProof.setSrc(streamResource);
      actionProof.setAlt("action-image");
      actionProof.setVisible(true);
    } else {
      actionProof.setVisible(false);
    }
  }

  public static class Filters extends Div implements Specification<SusActionEntity> {

    private final TextField walletAddress = new TextField("Wallet Address");
    private final DatePicker startDate = new DatePicker("Create Date");
    private final DatePicker endDate = new DatePicker();
    private final MultiSelectComboBox<ActionStatus> status = new MultiSelectComboBox<>("Status");

    public Filters(Runnable onSearch) {

      setWidthFull();
      addClassName("filter-layout");
      addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
          LumoUtility.BoxSizing.BORDER);
      walletAddress.setPlaceholder("Wallet Address");

      status.setItems(ActionStatus.values());

      // Action buttons
      Button resetBtn = new Button("Reset");
      resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      resetBtn.addClickListener(e -> {
        walletAddress.clear();
        startDate.clear();
        endDate.clear();
        status.clear();
        onSearch.run();
      });
      Button searchBtn = new Button("Search");
      searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      searchBtn.addClickListener(e -> onSearch.run());

      Div actions = new Div(resetBtn, searchBtn);
      actions.addClassName(LumoUtility.Gap.SMALL);
      actions.addClassName("actions");

      add(walletAddress, status, createDateRangeFilter(), actions);
    }

    private Component createDateRangeFilter() {
      startDate.setPlaceholder("From");

      endDate.setPlaceholder("To");

      // For screen readers
      startDate.setAriaLabel("From date");
      endDate.setAriaLabel("To date");

      FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
      dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
      dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

      return dateRangeComponent;
    }

    @Override
    public Predicate toPredicate(Root<SusActionEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>();

      if (!walletAddress.isEmpty()) {
        String walletAddressValue = walletAddress.getValue();
        predicates.add(criteriaBuilder.equal(root.get("walletAddress"), walletAddressValue));
      }
      if (startDate.getValue() != null) {
        String databaseColumn = "createdAt";
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
            criteriaBuilder.literal(startDate.getValue())));
      }
      if (endDate.getValue() != null) {
        var databaseColumn = "createdAt";
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                root.get(databaseColumn)));
      }
      if (!status.isEmpty()) {
        var databaseColumn = "status";
        List<Predicate> statusPredicates = new ArrayList<>();
        for (ActionStatus occupation : status.getValue()) {
          statusPredicates
              .add(criteriaBuilder.equal(criteriaBuilder.literal(occupation),
                  root.get(databaseColumn)));
        }
        predicates.add(criteriaBuilder.or(statusPredicates.toArray(Predicate[]::new)));
      }
      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
  }
}
