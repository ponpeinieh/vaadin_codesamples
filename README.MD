This is a sample project from Vaadin CRM Tutorial

- ContactFrom 
    - Instantiate a Binder and later use it to bind the form input fields to the bean
        - Binder<Contact> binder = new BeanValidationBinder<>(Contact.class);
        - In the contructor, call bindInstanceFields() to bind
            - binder.bindInstanceFields(this);
        - call setBean() or readBean() to read the bean attributes into form fields
            - setBean() will not buffer the update, but
            - readBean() needs writeBean() to write the form field update result back to the bean
            - setBean() and writeBean() will ensure form field validation is passed before updating the bean. Or call binder.isValid() to perform the validation before firing the save event.
        - Here I commented **binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));** , since adding that will require users to first leave the focus of the input field before enabling the save button (they may have the impression that the Save button is not clickable).
    - ContactFormEvent 
        - A common superclass for all the events fired by this component
        - It contains the contact bean that was edited or deleted.
        - The form component uses it to inform parent components of events.

```
package com.example.application.views.list;

import com.example.application.data.entity.Company;
import com.example.application.data.entity.Contact;
import com.example.application.data.entity.Status;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class ContactForm extends FormLayout {
    private Contact contact;
    TextField firstName = new TextField("First name");
    TextField lastName = new TextField("Last name");
    EmailField email = new EmailField("Email");
    ComboBox<Status> status = new ComboBox<>("Status");
    ComboBox<Company> company = new ComboBox<>("Company");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Cancel");

    //Instantiate a Binder and use it to bind the input fields
    Binder<Contact> binder = new BeanValidationBinder<>(Contact.class);

    public void setContact(Contact contact) {
        this.contact = contact;
        // bind the values from the contact to the UI fields.
        binder.setBean(contact);
        //binder.readBean(contact);
    }

    public ContactForm(List<Company> companies, List<Status> statuses) {
        addClassName("contact-form");
        // Match fields in Contact and ContactForm based on their names.
        binder.bindInstanceFields(this);
        company.setItems(companies);
        company.setItemLabelGenerator(Company::getName);
        status.setItems(statuses);
        status.setItemLabelGenerator(Status::getName);

        add(firstName,
                lastName,
                email,
                company,
                status,
                createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        //add save, delete, and close event listeners
        save.addClickListener(event -> validateAndSave());
        // fires a delete event and passes the active contact.
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, contact)));
        // fires a close event.
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        //Validates the form every time it changes.
        //If it is invalid, it disables the save button to avoid invalid submissions.
        //binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
            if(binder.isValid()){
                fireEvent(new SaveEvent(this, contact));
            }
    }
//    private void validateAndSave() {
//        try {
//            //Write the form contents back to the original contact,
//            //if the validation is passed
//            binder.writeBean(contact);
//            //Fire a save event, so the parent component can handle the action.
//            fireEvent(new SaveEvent(this, contact));
//        } catch (ValidationException e) {
//            e.printStackTrace();
//        }
//    }
    // Events
    public static abstract class ContactFormEvent extends ComponentEvent<ContactForm> {
        private Contact contact;

        protected ContactFormEvent(ContactForm source, Contact contact) {
            //The second constructor parameter of ComponentEvent determines whether the event is triggered by a DOM event in the browser
            //or through the component’s server-side API.
            super(source, false);
            this.contact = contact;
        }

        public Contact getContact() {
            return contact;
        }
    }

    public static class SaveEvent extends ContactFormEvent {
        SaveEvent(ContactForm source, Contact contact) {
            super(source, contact);
        }
    }

    public static class DeleteEvent extends ContactFormEvent {
        DeleteEvent(ContactForm source, Contact contact) {
            super(source, contact);
        }

    }

    public static class CloseEvent extends ContactFormEvent {
        CloseEvent(ContactForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        //uses Vaadin’s event bus to register the custom event types.
        return getEventBus().addListener(eventType, listener);
    }
}
```

- ListView
    - Autowiring:
        - Components annotated with @Route is autowired
    - Note that **CrmService is autowired using constructor injection, instead of field injection**, because the constructor needs the crmService bean to do its work.
    - Add the listener for ContactForm.SaveEvent
        - form.addListener(ContactForm.SaveEvent.class, this::saveContact);

        ```
        private void saveContact(ContactForm.SaveEvent event) {
            service.saveContact(event.getContact());
            updateList();
            closeEditor();
            Notification notification = Notification
                    .show("Contact saved!", 5000, Notification.Position.MIDDLE);
        }
        ```

    - Setup grid's ValueChangeListener
        - grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));

        ```
        private void editContact(Contact contact) {
            if(contact==null){
                closeEditor();
            } else{
                form.setContact(contact);
                form.setVisible(true);
                addClassName("editing");
            }
        }
        ```

```
package com.example.application.views.list;

import com.example.application.data.entity.Contact;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.util.Collections;

@PermitAll
@Route(value = "",layout= MainLayout.class)
@PageTitle("Contacts | Vaadin CRM")
public class ListView extends VerticalLayout {
    Grid<Contact> grid = new Grid<>(Contact.class);
    TextField filterText = new TextField();
    ContactForm form; //delay its contruction, because it needs CrmService
    CrmService service;

    public ListView(CrmService service) { //use contructor injection for CrmService
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private void closeEditor() {
        //sets the form contact to null, clearing out old values;
        form.setContact(null);
        //hides the form;
        form.setVisible(false);
        //removes the "editing" CSS class from the view.
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(service.findAllContacts(filterText.getValue()));
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new ContactForm(service.findAllCompanies(), service.findAllStatuses());
        form.setWidth("25em");
        form.addListener(ContactForm.SaveEvent.class, this::saveContact);
        form.addListener(ContactForm.DeleteEvent.class, this::deleteContact);
        form.addListener(ContactForm.CloseEvent.class, e -> closeEditor());
    }

    private  void deleteContact(ContactForm.DeleteEvent event) {
        service.deleteContact(event.getContact());
        updateList();
        closeEditor();
        Notification notification = Notification
                .show("Contact deleted!", 5000, Notification.Position.MIDDLE);
    }

    private void saveContact(ContactForm.SaveEvent event) {
        service.saveContact(event.getContact());
        updateList();
        closeEditor();
        Notification notification = Notification
                .show("Contact saved!", 5000, Notification.Position.MIDDLE);
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "email");
        grid.addColumn(contact -> contact.getStatus().getName()).setHeader("Status");
        grid.addColumn(contact -> contact.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));
    }

    private void editContact(Contact contact) {
        if(contact==null){
            closeEditor();
        } else{
            form.setContact(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        Button addContactButton = new Button("Add contact");
        addContactButton.addClickListener(click->addContact());
        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }
}
```


- MainLayout
    - If you omit the @Route annotation parameter, the route target is derived from the class name. The derived name will be in lower case and the trailing View will be removed. Also, MainView or Main names will be mapped to root (value will be "").
    - Creating Route Aliases
        - You can also use @RouteAlias to create aliases for routes. A primary route created by @Route is required. It’s possible to create multiple aliases for a route.

```
package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.views.list.ListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;

import java.util.Optional;
public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private Tabs menu;

    private H1 viewTitle;
    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        viewTitle = new H1("Vaadin CRM");
        viewTitle.addClassNames("text-l", "m-m");
        Button logout = new Button("Log out", e -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                viewTitle,
                logout
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

//    private void createDrawer() {
//        RouterLink listLink = new RouterLink("List", ListView.class);
//        listLink.setHighlightCondition(HighlightConditions.sameLocation());
//
//        addToDrawer(new VerticalLayout(
//                listLink,
//                new RouterLink("Dashboard", DashboardView.class)
//        ));
//    }

    private void createDrawer() {
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
    }
    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames("drawer");
        // Configure styling for the drawer
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Have a drawer header with an application logo
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        Image logo = new Image("images/bet.png", "Niu Niu Logo");
        logoLayout.add(logo);
        logoLayout.add(new H1("My Project"));

        // Display the logo and the menu in the drawer
        layout.add(logoLayout, menu);
        return layout;
    }
    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[] { createTab("List", ListView.class),
                createTab("Dashboard", DashboardView.class)};
    }

    private static Tab createTab(String text,
                                 Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        // Select the tab corresponding to currently shown view
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);

        // Set the view title in the header
        viewTitle.setText(getCurrentPageTitle());
    }
    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class)
                        .equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }
    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(PageTitle.class).value();
    }
}

```