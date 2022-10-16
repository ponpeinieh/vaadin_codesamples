- Common features available in all components:
    - You can use the basic features listed below with all components that extend com.vaadin.flow.component.Component.
    - Id 
    - Element:
        - Every component is associated with a root Element.
        - You can use the Element to access low-level functionality using the `component.getElement()` method.
    - Visibility
        - You can set a component to invisible using the `component.setVisible(false)` method.
        - Invisible components:
            - are no longer displayed in the UI;
            - **don’t receive updates from the client side**
            - **transmission of server-side updates resumes when the component is made visible again.**

            ```
            Span label = new Span("My label");
            label.setVisible(false);
            // this isn't transmitted to the client side
            label.setText("Changed my label");

            Button makeVisible = new Button("Make visible", evt -> {
                // makes the label visible - only now the "Changed my label" text is transmitted
                label.setVisible(true);
            });
            ```
    - Enabled State
        - You can **disable user interaction with a component** using the `component.setEnabled(false)` method on the server.
        - `Explicitly disabled`: A component is explicitly disabled when `setEnabled(false)` is called directly on it. 
            - The user cannot interact with the component, and communication from the client to the server is blocked.
        - `Implicitly disabled`: A component is implicitly disabled when it’s a child of an explicitly disabled container. 
            - The component behaves exactly like an explicitly disabled component, except that it’s automatically enabled again as soon as it **detaches from the disabled container**. 

- Components for **Standard HTML Elements**
    - Flow comes with a set of components for standard HTML elements.
    - The module flow-html-components contains things like a, div, h1,img,input,label,li,button,ol,p,span,ul etc
    - Standard HTML components have the `component API` that allows you to set most typical properties and attributes.
    - You can also use the `Element API` to set any property or attribute, if the `component API` doesn’t have an appropriate method.
    - Components that can contain other components implement the `HtmlContainer` interface to create a hierarchical structure. 
    - The `Element API` allows you to create any standard HTML element using the Element constructor. 
        - The `ElementFactory` class contains factory methods for many standard HTML elements.

- Creating Components
    - Creating components by **combining or extending existing ones**, or by **constructing a new one using the Element API.**
    
    - Creating a component **using a single HTML element.**
        - Example: Creating a TextField component based on an <input> element.

        ```
        @Tag("input")
        public class TextField extends Component {

            public TextField(String value) {
                getElement().setProperty("value",value);
            }
        }
        ```
        - The root element is created automatically (by the Component class) based on the `@Tag` annotation.
            - You can use predefined constants in the @Tag annotation. 
            - For example, the `@Tag("input")` annotation is equivalent to `@Tag(Tag.INPUT)`. 
            - Most tag names have a constant, but not all.
        - The root element is accessed using the `getElement()` method.
        - The root element is used to set the initial value of the field.
        - **Adding an API to make the component easier to use, you can add an API to get and set the value.**
            - Example: Adding an API using the `@Synchronize` annotation.
                - **Adding the @Synchronize annotation to the getter ensures that the browser sends property changes to the server.**
                - The annotation defines the **name of the DOM event** that triggers synchronization, in this case a **`change` event**.
                - Changes to the input element cause the updated `value` property (deduced from the getter name) to be sent to the server.

            ```
            @Synchronize("change")
            public String getValue() {
                return getElement().getProperty("value");
            }
            public void setValue(String value) {
                getElement().setProperty("value", value);
            }
            ```

    - Creating a component **using multiple HTML elements.**
        - We create a TextField component that supports a label.
        - Example: DOM structure of the component
            - Example: TextField component with <input> and <label> elements

            ```
            <div>
                <label></label>
                <input>
            </div>
            ```

            - The DOM structure is created by marking the root element as a <div> in the @Tag annotation.
            - The label and input elements are appended to the root element.
            - Synchronization of the `value` property is set up using the input element through `addPropertyChangeListener()`

            ```
            @Tag("div")
            public class TextField extends Component {

                Element labelElement = new Element("label");
                Element inputElement = new Element("input");

                public TextField() {
                    inputElement.addPropertyChangeListener("value", "change", e -> {});
                    getElement().appendChild(labelElement, inputElement);
                }

            }
            ```

        - Adding an API - to make the component easier to use, you can add an API to set the input value and label text.
        - Example: Adding an API to get and set values for the input and label elements.

        ```
        public String getLabel() {
            return labelElement.getText();
        }

        public String getValue() {
            return inputElement.getProperty("value");
        }

        public void setLabel(String label) {
            labelElement.setText(label);
        }

        public void setValue(String value) {
            inputElement.setProperty("value", value);
        }
        ```

    - Creating a component **using(combining) Existing Components** together
        - Create a `Composite component` using existing components.
        - We create a TextField component by combining existing Div, Label and Input HTML components into this hierarchy
        - **Creating the component based on a `Composite` is the best practice in these circumstances**. 
            - It’s possible to create a new component by extending the Div HTML component, but this isn’t advisable, because **it unnecessarily exposes Div API methods, such as add(Component), to the user**.
            - Keeping overhead to a minimum: Using a Component (instead of an Element) or a Composite doesn’t result in extra overhead.
        - Example: Creating a TextField component by extending `Composite<Div>`.
            - The Composite automatically creates the root component. We specify this by using generics (Composite<Div>).
            - We can access the root component through the `getContent()` method.
            - In the constructor, we only need to create the child components and add them to the root Div.
            - We set the value by using setValue() method in the Input component. 

        ```
        public class TextField extends Composite<Div> {

            private Label label;
            private Input input;

            public TextField(String labelText, String value) {
                label = new Label();
                label.setText(labelText);
                input = new Input();
                input.setValue(value);

                getContent().add(label, input);
            }
        }
        ```

        - Adding an API to get and set the value and label text. We do this by delegating to the Input and Label components.
            - Example: Adding an API to get and set the value and label.

            ```
            public String getValue() {
                return input.getValue();
            }
            public void setValue(String value) {
                input.setValue(value);
            }

            public String getLabel() {
                return label.getText();
            }
            public void setLabel(String labelText) {
                label.setText(labelText);
            }
            ```




    - Create a new component by **extending any existing component.**
        - For most components, there is a client-side component and a corresponding server-side component
        - Client-side component: Contains the HTML, CSS, and JavaScript, and defines a set of properties that determine the component’s behavior on the client side.
        - Server-side component: Contains Java code that allows client-side properties to be changed, and manages the component’s behavior on the server side.
        - **You can extend a component on either the server side or the client side.** These are alternative approaches that are mutually exclusive.
        - Example: the two different approaches to achieving the same changes to the prebuilt text field component.   
            - `com.vaadin.flow.component.textfield.TextField`
        - Extending a Component Using the Server-side Approach:
            - Extending a server-side component is useful when you want to add new functionality (as opposed to visual aspects) to an existing component. 
            - You might use this approach, for example, when **automatically processing data, when adding default validators, or when combining multiple simple components into a field that manages complex data.**
            - Consider using a Web Component : If your component contains a lot of logic that could conveniently be done on the client side, consider implementing it as a Web Component and creating a wrapper for it. This approach may offer a better user experience and result in less load on the server.

            ```
            @CssImport("./styles/numeric-field-styles.css")
            public class NumericField extends TextField {

                private Button substractBtn;
                private Button addBtn;

                private static final int DEFAULT_VALUE = 0;
                private static final int DEFAULT_INCREMENT = 1;

                private int numericValue;
                private int incrementValue;
                private int decrementValue;

                public NumericField() {
                    this(DEFAULT_VALUE, DEFAULT_INCREMENT,
                            -DEFAULT_INCREMENT);
                }

                public NumericField(int value, int incrementValue,
                                    int decrementValue) {
                    setNumericValue(value);
                    this.incrementValue = incrementValue;
                    this.decrementValue = decrementValue;

                    setPattern("-?[0-9]*");
                    setPreventInvalidInput(true);

                    // Adds a listener for change events fired by the webcomponent.
                    addChangeListener(event -> {
                        String text = event.getSource().getValue();
                        if (StringUtils.isNumeric(text)) {
                            setNumericValue(Integer.parseInt(text));
                        } else {
                            setNumericValue(DEFAULT_VALUE);
                        }
                    });

                    substractBtn = new Button("-", event -> {
                        setNumericValue(numericValue +
                                decrementValue);
                    });

                    addBtn = new Button("+", event -> {
                        setNumericValue(numericValue +
                                incrementValue);
                    });

                    getElement().setAttribute("theme", "numeric");
                    styleBtns();

                    addToPrefix(substractBtn);
                    addToSuffix(addBtn);
                }

                private void styleBtns() {
                    // Note: The same as addThemeVariants
                    substractBtn.getElement()
                            .setAttribute("theme", "icon");
                    addBtn.getElement()
                            .setAttribute("theme", "icon");
                }

                public void setNumericValue(int value) {
                    numericValue = value;
                    setValue(value + "");
                }

                // getters and setters
            }
            ```

            - We import additional styles for the component using the `@CssImport annotation`. These styles apply only to our NumericField component, and not to all TextField components.
                - Example: Creating numeric-field-styles.css to customize the appearance of the `vaadin-text-field` component.
                    - <vaadin-text-field> is a Web Component for text field control in forms.
                    - Prefixes and suffixes:
                        - These are child elements of a <vaadin-text-field> that are displayed inline with the input, before or after. 
                        - In order for an element to be considered as a prefix, it must have the slot attribute set to prefix (and similarly for suffix).

                            ```
                            <vaadin-text-field label="Email address"> <div slot="prefix">Sent to:</div> <div slot="suffix">@vaadin.com</div> </vaadin-text-area>
                            ```
                - styles/numeric-field-styles.css

                    ```
                    :host([theme~="numeric"]) [part="input-field"] {
                        background-color: var(--lumo-base-color);
                        border: 1px solid var(--lumo-contrast-30pct);
                        box-sizing: border-box;
                    }

                    :host([theme~="numeric"]) [part="value"]{
                        text-align: center;
                    }
                    ```

