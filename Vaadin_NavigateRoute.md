- Navigating Between Routes
    - Switching between routes/views can be initiated in two ways: 
        - programmatically using UI.navigate() methods or 
        - using links. 
    - The navigate() method is more agile for a Java programmer, as the view change can be issued from anywhere in your code and you can interact with the target view. 
    - Using links, on the other hand, is the native web way of moving from one view to another, allowing you for example to share the direct link to a specific view with your colleague, and they work even if the session has expired.

- Server-Side Navigation:

    - Trigger navigation from the server-side code using various `UI.navigate()` methods.
        - You should primarily use `UI.navigate(Class<? extends Component> navigationTarget)` or `navigate(Class<? extends C> navigationTarget, RouteParameters parameters)` methods, where you pass the Class of the target view as a parameter. 
        - Compared to String versions, this avoids having to generate the route string manually.
        - In the browser, the **navigate() method triggers a location update and the addition of a new history state entry, but doesn’t issue a full page reload**.

        ```
        Button button = new Button("Navigate to company");
        button.addClickListener(e ->
            button.getUI().ifPresent(ui ->
                ui.navigate("company"))
        );
        ```

        - Class UI : 
            - The topmost component in any component hierarchy. 
            - There is one UI for every Vaadin instance in a browser window. 
                - A UI may either represent an entire browser window (or tab) or some part of a html page where a Vaadin application is embedded.
            - **The UI is the server side entry point for various client side features that are not represented as components added to a layout**, e.g notifications, sub windows, and executing javascript in the browser.

    - Interacting directly with the Target View
        - The navigate() method where you give the **target view class as a parameter returns the actual target view as Optional.** This allows you to **further configure the new view with its Java API.**
            - The return value may be empty if the target view isn’t immediately available for some reason. This may happen, for example, because of security constraints or if the navigation was canceled or postponed in a navigation lifecycle events.
            - Example: Creating an edit button that navigates to a separate view and **assigns the DTO to the target view with its Java API**:

            ```
            new Button("Edit " + user.getName(), event -> {
                ui.navigate(UserEditor.class)
                        .ifPresent(editor -> editor.editUser(user)); //call the view class' editUser() and pass in the user 
            })
            ```

        - With this code, Vaadin **doesn’t have the data to maintain a deep link to edit the same entity.**
        - For the active user of the application, a view to edit the selected entity is opened, but they cannot share a direct link to edit the same entity with their colleague. 
        - If deep linking is required, the target view must maintain enough details about the UI state in the URL, or the developer must use `route parameters` to pass the data, instead of the `direct Java API`.

        - Below is the code to provide a deep link using `History` object: 

        ```
        @Route
        public class UserEditor extends VerticalLayout implements HasUrlParameter<Integer> {

            /**
            * This method can be called directly by other views.
            *
            * @param user the User instance to edit
            */
            public void editUser(User user) {
                // do the actual UI changes
                createFormForUser(user);
                // maintain a complete url in the browser
                updateQueryParameters(user);
            }

            private void updateQueryParameters(User o) {
                String deepLinkingUrl = RouteConfiguration.forSessionScope()
                        .getUrl(getClass(), o.getId());
                // Assign the full deep linking URL directly using
                // History object: changes the URL in the browser,
                // but doesn't reload the page.
                getUI().get().getPage().getHistory()
                        .replaceState(null, deepLinkingUrl);
            }

            @Override // HasUrlParameter interface
            public void setParameter(BeforeEvent event,
                                    @OptionalParameter Integer id) {
                if(id != null) {
                    // This method is called if user arrives via "deep link"
                    // directly to this form. In a real world app, one would
                    // likely fetch an entity/DTO via service based on its id
                    createFormForUser(new User("User " + id, id));
                }
            }

            private void createFormForUser(User user) {
                // just show the user ID as this is really not a form example
                add(new Paragraph("User: " + user.getId()) );
            }

        }
        ```

    - Passing Data Using `Route Parameters`
        - If your target view implements `HasRouteParameters`, you can submit trivial data to the target view as `route parameters`. 
        - Compared to directly interacting with the target views Java API, this method automatically maintains the URL for deep linking. 
        - However, you cannot pass more complex objects as route parameters as such, and you always need to **consider data safety parameters that end up in URLs.**
        - Example: Navigation to the user/123 route target (where "123" is the parameter) upon clicking a button:

        ```
        Button editButton = new Button("Edit user details");
        editButton.addClickListener(e ->
                editButton.getUI().ifPresent(ui -> ui.navigate(
                        UserProfileEdit.class, "123"))
        );
        ```
        - Defining `Route Parameters`
            - A route target (view) that accepts route parameters should:
                - implement the `HasUrlParameter<T>` interface, and
                - define the parameter type using generics.
                    - HasUrlParameter<T> only supports a type argument of Long, Integer, String, and Boolean types.
                - `HasUrlParameter` defines the `setParameter()` method that’s called by the Router, based on values extracted from the URL. **This method will always be invoked before a navigation target is activated (before the BeforeEnter event).**
                - Example: Defining a navigation target that takes a string parameter and produces a greeting string from it, which the target then sets as its own text content on navigation:

                ```
                @Route(value = "greet")
                public class GreetingComponent extends Div implements HasUrlParameter<String> {

                    @Override
                    public void setParameter(BeforeEvent event, String parameter) {
                        setText(String.format("Hello, %s!", parameter));
                    }
                }
                ```
        - Optional Route Parameters
            - Route parameters can be annotated as optional using `@OptionalParameter`.
            - Example: Defining the route to match both greet and greet/<anything>:

            ```
            @Route("greet")
            public class OptionalGreeting extends Div implements HasUrlParameter<String> {

                @Override
                public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
                    if (parameter == null) {
                        setText("Welcome anonymous.");
                    } else {
                        setText(String.format("Welcome %s.", parameter));
                    }
                }
            }
            ```
        - Wildcard Route Parameters
            - When more parameters are needed, the route parameter can also be annotated with `@WildcardParameter`.
            - Example: Defining the route to match greet and anything after it, for instance greet/one/five/three:
                - But you need to split the parameter yourself
            ```
            @Route("greet")
            public class WildcardGreeting extends Div implements HasUrlParameter<String> {

                @Override
                public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
                    if (parameter.isEmpty()) {
                        setText("Welcome anonymous.");
                    } else {
                        setText(String.format("Handling parameter %s.", parameter)); // one/five/three
                    }
                }
            }
            ```
    - Passing Data Using `Query Parameters`
        - Pass parameters to a given route in the form of name-value pairs
            - `navigate​(Class<? extends C> navigationTarget, T parameter, QueryParameters queryParameters)`
            - `navigate​(Class<? extends T> navigationTarget, QueryParameters queryParameters)`
        - We can retrieve any query parameters contained in a URL; for example, ?name1=value1&name2=value2.
        - Use the `getQueryParameters()` method of a `Location` instance to access query parameters. 
        - You can retrieve the Location class through 
            - the `BeforeEnterEvent` parameter of the `BeforeEnterObserver::beforeEnter()` method or
            - the `BeforeEvent` parameter of the `HasUrlParameter::setParameter()` method (see below example)

            ```
            @Override
            public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

                Location location = event.getLocation();
                QueryParameters queryParameters = location.getQueryParameters();

                Map<String, List<String>> parametersMap = queryParameters.getParameters(); //note all String type
            }
            ```
    - Passing Data Using `Route Template Parameters` 
        - Route templates provide powerful and highly customizable methods to include parameters into a route.
        - `navigate​(Class<T> navigationTarget, RouteParameters parameters)`
        - A route template is a sequence of segments /seg_1/seg_2/…​/seg_n, where each segment is either a `fixed segment` (a string that doesn’t start with :) or a `parameter segment` according to the syntax rule given in the next paragraph. At least one segment must be a parameter segment.
        - Route template parameters must use the following syntax: 
            - `:parameter_name[modifier][(regex)]`
                - parameter_name is the name of the parameter whose value to retrieve when a URL matching the template is resolved on the server. It must be prefixed by a colon (:).
                - modifier is optional and may be one of the following:
                    - ? denotes an optional parameter which might be missing from the URL being resolved;
                    - \* denotes a wildcard parameter which can be used only as the last segment in the template, resolving all segment values at the end of the URL.
                    - regex is also optional and defines the regex used to match the parameter value. The regex is compiled using java.util.regex.Pattern and shouldn’t contain the segment delimiter sign /. If regex is missing, the parameter accepts any value.
            - Example: A simple route where the parameter is defined as a middle segment.

            ```
            @Route("user/:userID/edit")
            public class UserProfileEdit extends Div implements BeforeEnterObserver {

                private String userID;

                @Override
                public void beforeEnter(BeforeEnterEvent event) {
                    userID = event.getRouteParameters().get("userID").get();
                }
            }
            ```

- Using the `RouterLink` Component
    - RouterLink is a special component based on the <a> tag to create links pointing to route targets in your application.
    - Navigation with RouterLink fetches the content of the new component without reloading the page. 
        - The page is updated in place without a full page reload, but the URL in the browser is updated.

        ```
        void buildMenu() {
            menu.add(new RouterLink("Home", HomeView.class));
        }

        @Route(value = "")
        public class HomeView extends Component {
        }
        ```

        - with `Route parameter`

        ```
        void buildMenu() {
            menu.add(new RouterLink("Greeting", GreetingComponent.class, "default"));
        }

        @Route(value = "greet")
        public class GreetingComponent extends Div implements HasUrlParameter<String> {

            @Override
            public void setParameter(BeforeEvent event, String parameter) {
                setText(String.format("Hello, %s!", parameter));
            }
        }
        ```

        - with `Route Template Parameters` 

        ```
        void buildMenu() {
            // user/123/edit
            menu.add(new RouterLink("Edit user details", UserProfileEdit.class, 
                new RouteParameters("userID", "123")));

            // user/edit
            menu.add(new RouterLink("Edit my details", UserProfileEdit.class));
        }

        @Route("user/:userID?/edit") //userID is optional
        public class UserProfileEdit extends Div implements BeforeEnterObserver {

            private String userID;

            @Override
            public void beforeEnter(BeforeEnterEvent event) {
                userID = event.getRouteParameters().get("userID").
                        orElse(CurrentUser.get().getUserID());
            }
        }
        ```

        - use new RouterLink(viewClass) to embed other elements into RouterLink without text, for example images or icons.

        ```
        void buildMenu() {
            Icon vaadinIcon = new Icon(VaadinIcon.HOME);
            RouterLink link = new RouterLink(HomeView.class);
            link.add(vaadinIcon);
            menu.add(link);
        }

        @Route(value = "")
        public class HomeView extends Component {
        }
        ```

- Using Standard Links

    - It’s also possible to navigate with standard <a href="company"> type links. 
    - You can do that via an Anchor component to which you would supply an href and text content:

    ```
    new Anchor("/hello", "Go to /hello route");
    ```
    - You can configure a standard link to open in a new tab by setting the anchor target attribute to `_blank`:


    ```
    Anchor anchor = new Anchor("/hello", "Go to /hello route");
    anchor.getElement().setAttribute("target", "_blank");
    ```
    - Vaadin router intercepts all instances of anchor navigation, and clicking on a standard link doesn’t cause a full page reload to happen by default. 
    - If you want **a full page reload** to happen, for example when navigating to a page that isn’t implemented using Vaadin, you can add `router-ignore` attribute; for example, `<a router-ignore href="company">Go to the company page</a>`. This can be done from the Java API as follows:

    ```
    Anchor anchor = new Anchor("/hello", "Go to /hello route");
    anchor.getElement().setAttribute("router-ignore", "");
    ```