- Using Events with Components - How to handle events in your components.
    - Your component class can provide **an event-handling API that uses the `event bus` provided by the `Component` base class.**
    - The event bus supports:
        - Event classes that extend `ComponentEvent`, and 
        - Listeners of the type `ComponentEventListener<EventType>`.
    - ComponentEvent 
        - The base type is parameterized with the type of the 'source' component firing the event. 
            - This means that the getSource() method automatically returns the correct component type.
        - The second constructor parameter determines **whether the event is triggered by a DOM event in the browser or through the component’s server-side API.**
        - Example: Creating an event by extending ComponentEvent.

            ```
            public class ChangeEvent extends ComponentEvent<TextField> {
                public ChangeEvent(TextField source, boolean fromClient) {
                    super(source, fromClient);
                }
            }
            ```

    - Event listeners
        - The generic `ComponentEventListener<EventType>` is used, and it isn’t necessary to create a separate interface for your event type.
        - The method for adding a listener returns **a handle that can be used to remove the listener** (ie. the `Registration` object)
        - Hence, it isn’t necessary to implement a separate method to remove an event listener.
        - Example: Using the addChangeListener() method to add an event listener.
            - `Component.addListener​(Class<T> eventType, ComponentEventListener<T> listener)` is actually doing the work.

            ```
            @Tag("input")
            public class TextField extends Component {
                public Registration addChangeListener(ComponentEventListener<ChangeEvent> listener) {
                    return addListener(ChangeEvent.class, listener);
                }

                // Other component methods omitted
            }
            ```

            - Example: Adding and removing event listeners.

            ```
            TextField textField = new TextField();
            Registration registration = textField.addChangeListener(e ->System.out.println("Event fired"));
            // In some other part of the code
            registration.remove();
            ```
    - Firing Events from the Server
        - You can fire an event on the server by creating the event instance and passing it to the `fireEvent()` method. 
        - **Use false as the second constructor parameter to indicate that the event doesn’t come from the client.**
        - Example: Using the `fireEvent()` method set to false to fire an event from the server.

        ```
        @Tag("input")
        public class TextField extends Component {

            public void setValue(String value) {
                getElement().setAttribute("value", value);
                fireEvent(new ChangeEvent(this, false)); //false means fire the event from the server
            }

            // Other component methods omitted
        }
        ```
    - Firing Events from the Client
        - You can **connect a component event to a DOM event** that’s fired by the element in the browser.
        - To do this, use the **`@DomEvent` annotation in your event class to specify the name of the DOM event to listen to.** 
        - **Vaadin Flow automatically adds a DOM event listener to the element when a component event listener is present.**
        - Example: Using the `@DomEvent` annotation to connect a TextField component to a DOM event.

        ```
        @DomEvent("change")
        public class ChangeEvent extends ComponentEvent<TextField> {
            public ChangeEvent(TextField source,boolean fromClient) {
                super(source, fromClient);
            }
        }
        ```

        - Adding Event Data
            - An event can include additional information, for example the mouse button used in a click event.
            - The @DomEvent annotation supports additional constructor parameters. You can **use the `@EventData` annotation to specify which data to send from the browser.**
            - Example: Using the `@EventData` annotation to define additional click-event data.
                - The @EventData definition runs as JavaScript in the browser.
                - The DOM event is available as `event` and the element to which the listener was added is available as `element`.
                - See `DomListenerRegistration.addEventData` in the Javadoc for more about how event data is collected and sent to the server.

            ```
            @DomEvent("click")
            public class ClickEvent extends ComponentEvent<NativeButton> {
                private final int button;

                public ClickEvent(NativeButton source, boolean fromClient, @EventData("event.button") int button) {
                    super(source, fromClient);
                    this.button = button;
                }

                public int getButton() {
                    return button;
                }
            }
            ```
        - Filtering Events
            - Instead of sending all DOM events to the server, you can filter events by defining a filter in the @DomEvent annotation. The filter is typically based on things related to the event.
            - Example: Defining a filter in the @DomEvent annotation.
                - The filter definition runs as JavaScript in the browser.
                - The DOM event is available as `event` and the element to which the listener was added is available as `element`.
                - See `DomListenerRegistration.setFilter` in the Javadoc for more about how the filter is used.

            ```
            @DomEvent(value = "keypress", filter = "event.key == 'Enter'")
            public class EnterPressEvent extends ComponentEvent<TextField> {
                public EnterPressEvent(TextField source,boolean fromClient) {
                    super(source, fromClient);
                }
            }
            ```
        - Limiting Event Frequency
            - Certain kinds of events are fired very frequently when the user interacts with the component. For example, text input events are fired while the user types.
            - You can configure the rate at which events are sent to the server by defining different `debounce` settings in the `@DomEvent` annotation. Debouncing always requires a `timeout` (in milliseconds) and a `burst phase`, which determines when events are sent to the server. You have three burst phase options:
                - LEADING phase: An event is sent **at the beginning of a burst**, but subsequent events are only sent after one timeout period has passed without any new events. This is useful for things like **button clicks, to prevent accidental double submissions.**
                - INTERMEDIATE phase: **Periodical events are sent while a burst is ongoing**. Subsequent events are delayed until one timeout period since the last event passed. This is useful for things like **text input, if you want to react continuously while the user types.**
                - TRAILING phase: This phase is **triggered at the end of a burst** after the timeout period has passed without any further events. This is useful for things like **text input if you want to react only when the user stops typing.**

                ```
                @DomEvent(value = "input",
                        debounce = @DebounceSettings(
                            timeout = 250,
                            phases = DebouncePhase.TRAILING))
                public class InputEvent extends ComponentEvent<TextField> {
                    private String value;

                    public InputEvent(TextField source, boolean fromClient,
                            @EventData("element.value") String value) {
                        super(source, fromClient);
                        this.value = value;
                    }

                    public String getValue() {
                        return value;
                    }
                }
                ```

