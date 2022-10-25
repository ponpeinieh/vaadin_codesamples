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
    // ContactFormEvent is a common superclass for all the events.
    // It contains the contact that was edited or deleted.
    // The form component uses it to inform parent components of events.
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