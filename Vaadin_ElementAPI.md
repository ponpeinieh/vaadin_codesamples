- Element API
    - You can control the HTML DOM in the browser from the server-side using the Element API.
    - package : `com.vaadin.flow.dom`
    - Get and set **DOM properties and attributes**.
        - The `Element API` contains methods to update and query parts of an element.
        - You can **use the Element API to change property and attribute values for server-side elements.**
        - **Transferring data to the server: 
            - By default, **values updated in the browser aren’t sent to the server.**
        - About Attributes:
            - **Attributes are used mainly for the initial configuration of elements.**
            - Attribute values are always stored as strings.
            - Example: Setting attributes for the nameField element.

                ```
                Element nameField = ElementFactory.createInput();
                nameField.setAttribute("id", "nameField");
                nameField.setAttribute("placeholder", "John Doe");
                nameField.setAttribute("autofocus", "");
                ```
                - is equivalent to:

                ```
                <input id="nameField" placeholder="John Doe" autofocus>
                ```
            - You can also retrieve and manipulate attributes after they have been set.
                 - Example: Retrieving and changing attributes in the nameField element.

                ```
                String placeholder = nameField.getAttribute("placeholder");

                // true
                nameField.hasAttribute("autofocus");

                nameField.removeAttribute("autofocus");

                // ["id", "placeholder"]
                nameField.getAttributeNames().toArray();
                ```
        - About Properties:
            - **Properties are used mainly to dynamically change the settings of an element after it has been initialized.**
                - Any JavaScript value can be used as a property value in the browser.
                - You can use different variations of the `setProperty()` method to set a property value as a String, boolean, double or JsonValue.
                    - Example: Setting a property value as a double.
                
                    ```
                    Element element = ElementFactory.createInput();
                    element.setProperty("value", "42.2");
                    ```

                - Similarly, you can use different variations of the `getProperty()` method to retrieve the value of a property as a String, boolean, double or JsonValue.
                    - If you retrieve the value of a property as a different type from that used to set it, **JavaScript type coercion rules** are used to convert the value. For example, a property set as a non-empty String results as true if fetched as a boolean.
                    - public boolean getProperty​(String name, boolean defaultValue) 
                        - Gets the value of the given property as a boolean, or the given default value if the underlying value is null.
                        - A value defined as some other type than boolean is converted according to JavaScript semantics:
                            - String values are true, except for the empty string.
                            - Numerical values are true, except for 0 and NaN.
                            - JSON object and JSON array values are always true.

                    - Example: Converting retrieved value types.

                    ```
                    boolean helloBoolean = element.getProperty("value", true);

                    // 42, string is parsed to a JS number and truncated to an int
                    int helloInt = element.getProperty("value", 0);
                    ```

        - Difference Between Using Attributes and Properties
            - It’s often possible to use either an attribute or property with the same name for the same effect, and both work fine.
            - In certain cases, only one or the other works, 
            - **The attribute is considered only when the element is initialized, and the property is effective after initialization.**
        - Using the `textContent` Property
            - Using the `setText()` method to set the `textContent` Property. 
            - **This removes all the children of the element and replaces them with a single text node with the given value.**
            - The ElementFactory interface provides helpers that you can use to create an element with a given text content.
                - Example: Using the `createSpan()` and `createDiv()` helper methods with the setText() method.

                ```
                // <div>Hello world</div>
                Element element = ElementFactory.createDiv("Hello world");

                // <div>Hello world<span></span></div>
                element.appendChild(ElementFactory.createSpan());

                // <div>Replacement text</div>
                element.setText("Replacement text");
                ```
            - Use `getText()` method to return the text in the element itself. Text in child elements is ignored.
            - Use `getTextRecursively()` method to return the text of the entire element tree, by recursively concatenating the text from all child elements.


    - Retrieving User Input from the client-side Using the Element API
        - Example: adds a text input field that allows the user to enter their name.

        ```
        Element textInput = ElementFactory.createInput();
        textInput.setAttribute("placeholder", "Enter your name");
        ```

        - Transfer the value to the server by **asking the client to update the server-side input element every time the value changes in the browser.**
            - Example: Using the `addPropertyChangeListener()` method with a NO-OP listener to update the value of the text input element.
            - Configures Flow to **synchronize the `value` property to the server side when a `change` event occurs.**

            ```
            textInput.addPropertyChangeListener("value", "change", e -> {}); // "value" is the property name, "change" is the DOM event name
            ```

        - Retrieve the synchronized properties using the `Element.getProperty()` API.

            - Example: Using the `textInput.getProperty("value")` method to retrieve the property `value`.
                - The value property of the TextInput element returns null if the property wasn’t previously set and the user hasn’t typed text into the field.

            ```
            button.addEventListener("click", e -> {
                String responseText = "Hello " + textInput.getProperty("value");
                Element response = ElementFactory.createDiv(responseText);
                getElement().appendChild(response);
            });
            ```
        - As an alternative, you can use the `addEventData()` method to transfer the value from the input to the server. (see below)

- addPropertyChangeListener() :
    - There are two addPropertyChangeListener() methods in Element API.
    - public Registration addPropertyChangeListener​(String name, PropertyChangeListener listener)
        - Adds a property change listener which is triggered **when the property's value is updated on the server side.**
        - Note that **properties changed on the server are updated on the client** but **changes made on the client side are not reflected back to the server unless configured using `addPropertyChangeListener(String, String, PropertyChangeListener)` or `DomListenerRegistration.synchronizeProperty(String)`.**
    - public DomListenerRegistration addPropertyChangeListener​(String propertyName, String domEventName, PropertyChangeListener listener)
        - Adds a property change listener and **configures the property to be synchronized to the server when a given DOM event is fired**. 

- Listening to User Events from browsers using the Element API  
    - The Element API provides the `addEventListener()` method, which you can use to **listen to any browser event.**
    - Example: Using the addEventListener() method to create a click event.

    ```
    Element helloButton = ElementFactory.createButton("Say hello");
    helloButton.addEventListener("click", e -> {
        Element response = ElementFactory.createDiv("Hello!");
        getElement().appendChild(response);
    });
    getElement().appendChild(helloButton);
    ```
    
- Accessing Data from Events
    - You can get more information about the element or user interaction by defining the required event data on the `DomListenerRegistration` returned by the `addEventListener()` method.
    - Example: Using the `addEventData()` method to define the required event data.
        - The requested event data values are sent as a `JSON object` from the client.
        - You can retrieve the event data (JSON object) using the `event.getEventData()` method in the listener.
        - Make sure that you use the correct getter (of JSON object) based on the data type (eg. getBoolean(), getNumber())
            - the same keys that were provided as parameters in the addEventData() method.

    ```
    helloButton.addEventListener("click", this::handleClick)
        .addEventData("event.shiftKey")
        .addEventData("element.offsetWidth");

    private void handleClick(DomEvent event) {
        JsonObject eventData = event.getEventData();
        boolean shiftKey = eventData
                .getBoolean("event.shiftKey");
        double width = eventData
                .getNumber("element.offsetWidth");

        String text = "Shift " + (shiftKey ? "down" : "up");
        text += " on button whose width is " + width + "px";

        Element response = ElementFactory.createDiv(text);
        getElement().appendChild(response);
    }
    ```
