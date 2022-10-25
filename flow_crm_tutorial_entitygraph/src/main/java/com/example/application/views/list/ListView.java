package com.example.application.views.list;

import com.example.application.data.entity.Contact;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.util.Collections;

//@PermitAll
@RolesAllowed("ADMIN")
@Route(value = "list",layout= MainLayout.class)
@RouteAlias(value = "",layout= MainLayout.class)
@PageTitle("Contacts | Vaadin CRM")
public class ListView extends VerticalLayout {
    Grid<Contact> grid = new Grid<>(Contact.class);
    TextField filterText = new TextField("Filter:");
    ContactForm form;
    CrmService service;



    public ListView(CrmService service) {
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
        //hides the form;
        //removes the "editing" CSS class from the view.
        form.setContact(null);
        form.setVisible(false);
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
        filterText.setPrefixComponent(new Icon("vaadin", "stopwatch"));
        filterText.setSuffixComponent(new Icon("vaadin", "rocket"));
        HorizontalLayout helper = new HorizontalLayout();
        helper.add(new Icon("vaadin", "timer"));
        helper.add(new Label("Filter by last name or first name"));
        filterText.setHelperComponent(helper);
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        Button addContactButton = new Button("Add contact",this::addContact);
        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void addContact(ClickEvent<Button> event) {
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }
}