- Navigation Lifecycle
    - Events that are fired when a user navigates from one state or view to another.
        - Listener : fired to listeners added to the UI instance
        - Observer:  **fired to attached components** that implement related observer interfaces.
    - `BeforeLeaveEvent`
        - The first event fired during navigation.
        - The event allows the navigation to be **postponed, canceled, or changed to a different destination.**
        - This event is delivered to any component instance implementing `BeforeLeaveObserver` that’s attached to the UI before the navigation starts.
        - It’s also possible to register a listener for this event using the `addBeforeLeaveListener(BeforeLeaveListener)` method in the UI class.
        - A typical use case for this event is to ask the user **whether they want to save any unsaved changes before navigating** to another part of the application.
        - **Postponing** a Navigation Transition
            - BeforeLeaveEvent includes the `postpone()` method, which can be used to postpone the current navigational transition until a specific condition is met.
            - Example: The client requests the user’s confirmation before leaving the page
                - event.postpone()
                - action.proceed()
                - Postponing interrupts the process of notifying observers and listeners. When the transition resumes, the remaining observers (those that come after the observer that initiated the postpone) are called.
                - Only one navigation event may be postponed at a time. 
                    - Starting a new navigation transition while a previous one is in a postponed state makes the postponed state obsolete, and executing ContinueNavigationAction has no effect.

            ```
            public class SignupForm extends Div implements BeforeLeaveObserver {
                @Override
                public void beforeLeave(BeforeLeaveEvent event) {
                    if (hasChanges()) {
                        ContinueNavigationAction action = event.postpone();
                        ConfirmDialog confirmDialog = new ConfirmDialog();
                            confirmDialog.setText("Your form has changes! Are you sure you want to leave?");
                            confirmDialog.setCancelable(true);
                            confirmDialog.addConfirmListener(__ -> action.proceed());
                            confirmDialog.open();
                    }
                }

                private boolean hasChanges() {
                    // no-op implementation
                    return true;
                }
            }
            ```


    - `BeforeEnterEvent`
        - The second event fired during navigation.
        - The event allows you to change the navigation to **go to a destination that’s different from the original.**
        - This event is typically used to react to special situations, for example if there is no data to show, or if the user doesn’t have appropriate permissions. 
        - This event is delivered to any component instance implementing `BeforeEnterObserver` that’s attached to the UI, beginning with the UI and moving through the child components.  
        - The event is fired:
            - only after a Postpone method (called during a BeforeLeaveEvent) has been continued;
            - before detaching and attaching components to make the UI match the location being navigated to.
            - It’s also possible to register a standalone listener for this event using the `addBeforeEnterListener (BeforeEnterListener)` method in the UI class.    
    - Rerouting
        - Both BeforeLeaveEvent and BeforeEnterEvent can be used to reroute dynamically.
        - Rerouting is typically used when there is a need to show completely different information in a particular state.
        - When the reroute() method is called:
            - the event isn’t fired to any further listeners or observers;
            - the method triggers a new navigation phase, based on the new navigation target, and events are fired based on this instead
            - for BeforeEnterEvent: child components of the rerouting component aren’t instantiated.
            - **rerouteTo() keeps the original URL** in the browser’s address bar and doesn’t change it to a new URL based on the new target.
            - Example: Rerouting when entering a BlogList with no results.

            ``` 
            @Route("no-items")
            public class NoItemsView extends Div {
                public NoItemsView() {
                    setText("No items found.");
                }
            }

            @Route("blog")
            public class BlogList extends Div implements BeforeEnterObserver {
                @Override
                public void beforeEnter(BeforeEnterEvent event) {
                    // implementation omitted
                    Object record = getItem();

                    if (record == null) {
                        event.rerouteTo(NoItemsView.class);
                    }
                }

                private Object getItem() {
                    // no-op implementation
                    return null;
                }
            }
            ```
    - Forwarding
        - The forwardTo() method reroutes navigation and **updates the browser URL.**
        - Forwarding can be used during BeforeEnter and BeforeLeave lifecycle states to dynamically redirect to a different URL.
        - When the forwardTo() method is called:
            - the event isn’t fired to any further listeners or observers;
            - the method triggers a new navigation phase, based on the new navigation target, and fires new lifecycle events for the new forward navigation target
            - for BeforeEnterEvent: child components of the forwarding component aren’t instantiated.
            - forwardTo() **changes the URL in the browser’s address bar** to the URL of the new target. 
                - The URL of the original target isn’t kept in the browser history.
            - Example: Forwarding when viewing BlogList without the required permissions.

            ```
            @Route("no-permission")
            public class NoPermission extends Div {
                public NoPermission() {
                    setText("No permission.");
                }
            }

            @Route("blog-post")
            public class BlogPost extends Div implements BeforeEnterObserver {
                @Override
                public void beforeEnter(BeforeEnterEvent event) {
                    if (!hasPermission()) {
                        event.forwardTo(NoPermission.class);
                    }
                }

                private boolean hasPermission() {
                    // no-op implementation
                    return false;
                }
            }
            ```
    - `AfterNavigationEvent `    
        - The third and last event fired during navigation.
        - This event is typically used to **update various parts of the UI after the actual navigation is complete**. 
        - Examples include **adjusting the content of a breadcrumb component** and visually **marking the active menu item as active.**
        - AfterNavigationEvent is fired:
            - after BeforeEnterEvent, and
            - after updating which components are attached to the UI.
            - At this point, the current navigation state is actually shown to the user, and further reroutes and similar changes are no longer possible.
            - The event is delivered to any component instance implementing `AfterNavigationObserver` that’s attached after completing the navigation.
            - It’s also possible to register a listener for this event using the `addAfterNavigationListener (AfterNavigationListener)` method in the UI class.
            - Example: Marking the active navigation element as active.
            
            ```
            public class SideMenu extends Div implements AfterNavigationObserver {
                Anchor blog = new Anchor("blog", "Blog");

                @Override
                public void afterNavigation(AfterNavigationEvent event) {
                    boolean active = event.getLocation()
                            .getFirstSegment()
                            .equals(blog.getHref());
                    blog.getElement()
                            .getClassList()
                            .set("active", active);
                }
            }
            ```
- Router Layouts and Nested Router Targets
    - `RouterLayout` Interface
        - All parent layouts of a navigation target component must implement the `RouterLayout` interface.
        - You can define a parent layout using the optional element `layout`(eg. MainLayout.class) from the `@Route` annotation.
        - Example: Render CompanyComponent inside MainLayout:

        ```
        @Tag("div")
        @Route(value = "company", layout = MainLayout.class)
        public class CompanyComponent extends Component {
        }
        ```

        - Default render location when using @Route("path")
            - When using the @Route("path") annotation to define a route, the component by default renders in the `<body>` tag on the page. This is because the element returned by HasElement.getElement() is attached to the <body> tag.
    - Multiple Router Target Components
        - Where multiple router target components use the same parent layout, the parent layout instances remain the same when the user navigates between the child components.
    - Multiple Parent Layouts
        - Use the `@ParentLayout` annotation to define a parent layout for components in the routing hierarchy.
        - You can create a parent layout for a parent layout, where necessary.
        - Example: MainLayout used for everything and MenuBar reused for views:

        ```
        public class MainLayout extends Div implements RouterLayout {
        }

        @ParentLayout(MainLayout.class)
        public class MenuBar extends Div implements RouterLayout {
            public MenuBar() {
                addMenuElement(TutorialView.class, "Tutorial");
                addMenuElement(IconsView.class, "Icons");
            }
            private void addMenuElement(
                    Class<? extends Component> navigationTarget,
                    String name) {
                // implementation omitted
            }
        }

        @Route(value = "tutorial", layout = MenuBar.class)
        public class TutorialView extends Div {
        }

        @Route(value = "icons", layout = MenuBar.class)
        public class IconsView extends Div {
        }
        ```

    - ParentLayout Route Control
        - Annotating a parent layout with `@RoutePrefix("prefix_to_add")` adds a **prefix to its children’s route**.
        - Example: PathComponent receives the some prefix from its parent, resulting in some/path as its final route.

        ```
        @Route(value = "path", layout = SomeParent.class)
        public class PathComponent extends Div {
            // Implementation omitted
        }

        @RoutePrefix("some")
        public class SomeParent extends Div implements RouterLayout {
            // Implementation omitted
        }
        ```
        - Absolute Routes
            - A child component can **bypass the parent’s route prefix** by adding `absolute = true` to its own @Route or @RoutePrefix annotations.
            - Example: Building a MyContent class to add "something" to multiple places in the SomeParent layout, without adding the route prefix to the navigation path:

            ```
            @Route(value = "content", layout = SomeParent.class, absolute = true)
            public class MyContent extends Div {
                // Implementation omitted
            }
            ```
            - Example: Defining absolute = true in the middle of the chain.
                - **The bound route is framework/tutorial, even though the full chain is some/framework/tutorial.**
                - If a parent layout defines a @RoutePrefix, **the "default" child could have its route defined as @Route("") and be mapped to the parent layout route.** For example, Tutorials with route "" would be mapped as framework/.

            ```
            @RoutePrefix(value = "framework", absolute = true)
            @ParentLayout(SomeParent.class)
            public class FrameworkSite extends Div implements RouterLayout {
                // Implementation omitted
            }

            @Route(value = "tutorial", layout = FrameworkSite.class)
            public class Tutorials extends Div {
                // Implementation omitted
            }
            ```



- Retrieving Routes - using `RouteConfiguration`
    - The class `RouteConfiguration` exposes methods to get the generated route for **registered navigation targets.**
    - Standard Navigation Targets
        - `RouteConfiguration.forSessionScope().getUrl(Class target)`
        - Example: The generated route is resolved to path.

        ```
        @Route("path")
        public class PathComponent extends Div {
            public PathComponent() {
                setText("Hello @Route!");
            }
        }

        public class Menu extends Div {
            public Menu() {
                String route = RouteConfiguration.forSessionScope().getUrl(PathComponent.class);
                Anchor link = new Anchor(route, "Path");
                add(link);
            }
        }
        ``` 
    - Navigation Target with `Route Parameters`
        - For navigation targets with required `route parameters`, passing the parameter to the resolver returns a string containing the parameter.
        - `RouteConfiguration.forSessionScope().getUrl(Class target, T parameter)`

        ```
        @Route(value = "greet")
        public class GreetingComponent extends Div implements HasUrlParameter<String> {

            @Override
            public void setParameter(BeforeEvent event, String parameter) {
                setText(String.format("Hello, %s!", parameter));
            }
        }

        public class ParameterMenu extends Div {
            public ParameterMenu() {
                String route = RouteConfiguration.forSessionScope().getUrl(GreetingComponent.class, "anonymous");
                Anchor link = new Anchor(route, "Greeting");
                add(link);
            }
        }
        ```
    - Navigation Target with `Route Templates`
        - To access a route that contains template parameters, the values of the parameters are required to generate the route.
        - The route is generated by replacing the parameter placeholders with the actual values.
        - The provided parameter values have to match the actual regular expression of the parameter template, otherwise the route generation fails.

        ```
        @Route(value = "item/:id([0-9]*)/edit")
        public static class ItemEdit extends Component {
        }

        public class MenuView extends Div {

            private void addItemEditLink() {
                String url = routeConfiguration.getUrl(ItemEdit.class,
                        new RouteParameters("id", "123"));

                // The generated url is `item/123/edit`
                Anchor link = new Anchor(url, "Button Api");
                add(link);
            }
        }
        ```

        - Example: If the navigation target is registered with more than one route, ie. alias routes, the parameter values are matched against all route templates starting with the main route, until one succeeds.

        ```
        @Route(value = ":path*")
        @RouteAlias(value = ":tab(api)/:path*")
        @RouteAlias(value = ":tab(overview|samples|links|reviews|discussions)")
        @RoutePrefix("component/:identifier")
        public static class ComponentView extends Component {
        }

        public class MenuView extends Div {

            private void addButtonApiLink() {
                String url = routeConfiguration.getUrl(ComponentView.class, new RouteParameters(
                                new RouteParam("identifier", "button"),
                                new RouteParam("tab", "api"),
                                new RouteParam("path", "com/vaadin/flow/button")));

                // The generated url is `component/button/api/com/vaadin/flow/button`
                Anchor link = new Anchor(url, "Button Api");
                add(link);
            }
        }
        ```



- Router Exception Handling
    - Router provides special support for navigation target exceptions. When an unhandled exception is thrown during navigation, an error view is shown.
    - **Exception targets work in the same way as regular navigation targets, except that they don’t typically have a specific @Route, because they are shown for arbitrary URLs.**
    - Error Resolving
        - Errors in navigation are resolved to a target that’s **based on the exception type** thrown during navigation.
        - At startup, all classes implementing the `HasErrorParameter<T extends Exception>` interface are collected for use as exception targets during navigation. 
        - An example of such a class is `RouteNotFoundError`, which is included by default in the framework and is used to resolve errors related to the router’s `NotFoundException`
        - Example: `RouteNotFoundError` defines the default target for the `NotFoundException` that’s shown when there is no target for the given URL.

        ```
        @Tag(Tag.DIV)
        public class RouteNotFoundError extends Component implements HasErrorParameter<NotFoundException> {

            @Override
            public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
                getElement().setText("Could not navigate to '"
                            + event.getLocation().getPath()
                            + "'");
                return HttpServletResponse.SC_NOT_FOUND;
            }
        }
        ```
        - This returns a 404 HTTP response and displays the text specified in the parameter to setText().
        - Exceptions are matched in the order below:
            - By exception cause.
            - By exception super type.
        - **The 404 `RouteNotFoundError` (for `NotFoundException`), and 500 `InternalServerError` (for `java.lang.Exception`) are implemented by default.**
    - Custom Exception Handlers
        - You can override the default exception handlers by extending them.
        - Example: Custom "route not found" handler that uses a custom application layout

        ```
        @ParentLayout(MainLayout.class)
        public class CustomNotFoundTarget extends RouteNotFoundError {

            @Override
            public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
                getElement().setText("My custom not found class!");
                return HttpServletResponse.SC_NOT_FOUND;
            }
        }
        ```
        - Note:
            - Only extending instances are allowed.
            - Exception targets may define ParentLayouts. BeforeNavigationEvent and AfterNavigationEvent are still sent, as in normal navigation.
            - One exception may only have one exception handler.
    - Advanced Exception Handling Example
        - The following example assumes an application Dashboard that collects and shows widgets to users. Only authenticated users are allowed to see protected widgets.
        - If the collection instantiates a ProtectedWidget in error, the widget itself checks authentication on creation and throw an `AccessDeniedException.`
        - The unhandled exception propagates during navigation and is handled by the `AccessDeniedExceptionHandler` that keeps the MainLayout with its menu bar, but displays information that an exception has occurred.

        ```
        @Route(value = "dashboard", layout = MainLayout.class)
        @Tag(Tag.DIV)
        public class Dashboard extends Component {
            public Dashboard() {
                init();
            }

            private void init() {
                getWidgets().forEach(this::addWidget);
            }

            public void addWidget(Widget widget) {
                // Implementation omitted
            }

            private Stream<Widget> getWidgets() {
                // Implementation omitted, gets faulty state widget
                return Stream.of(new ProtectedWidget());
            }
        }

        public class ProtectedWidget extends Widget {
            public ProtectedWidget() {
                if (!AccessHandler.getInstance().isAuthenticated()) {
                    throw new AccessDeniedException("Unauthorized widget access");
                }
                // Implementation omitted
            }
        }

        @Tag(Tag.DIV)
        public abstract class Widget extends Component {
            public boolean isProtected() {
                // Implementation omitted
                return true;
            }
        }

        @Tag(Tag.DIV)
        @ParentLayout(MainLayout.class)
        public class AccessDeniedExceptionHandler extends Component
            implements HasErrorParameter<AccessDeniedException>
        {

            @Override
            public int setErrorParameter(BeforeEnterEvent event,
                    ErrorParameter<AccessDeniedException>
                            parameter) {
                getElement().setText(
                    "Tried to navigate to a view without "
                    + "correct access rights");
                return HttpServletResponse.SC_FORBIDDEN;
            }
        }
        ```

    - Rerouting to an Error View
        - It’s possible to reroute from the BeforeEnterEvent and BeforeLeaveEvent to an error view registered for an exception.
        - You can use one of the `rerouteToError()` method overloads. 
            - All you need to add is **the exception class to target, and a custom error message**, where necessary.
            - `event.rerouteToError(AccessDeniedException.class);`

            ```
            public class AuthenticationHandler implements BeforeEnterObserver {
                @Override
                public void beforeEnter(BeforeEnterEvent event) {
                    Class<?> target = event.getNavigationTarget();
                    if (!currentUserMayEnter(target)) {
                        event.rerouteToError(AccessDeniedException.class);
                    }
                }

                private boolean currentUserMayEnter(Class<?> target) {
                    // implementation omitted
                    return false;
                }
            }
            ```
        - If the rerouting method catches an exception, you can use the `rerouteToError(Exception, String)` method to set a custom message.
        - Example: Blog sample error view with a custom message

        ```
        @Tag(Tag.DIV)
        public class BlogPost extends Component implements HasUrlParameter<Long> {

            @Override
            public void setParameter(BeforeEvent event,Long parameter) {
                removeAll();

                Optional<BlogRecord> record = getRecord(parameter);

                if (!record.isPresent()) {
                    event.rerouteToError(IllegalArgumentException.class,
                        getTranslation("blog.post.not.found",event.getLocation().getPath())); //message text
                } else {
                    displayRecord(record.get());
                }
            }

            private void removeAll() {
                // NO-OP
            }

            private void displayRecord(BlogRecord record) {
                // NO-OP
            }   

            public Optional<BlogRecord> getRecord(Long id) {
                // Implementation omitted
                return Optional.empty();
            }
        }

        @Tag(Tag.DIV)
        public class FaultyBlogPostHandler extends Component 
                implements HasErrorParameter<IllegalArgumentException>{

            @Override
            public int setErrorParameter(BeforeEnterEvent event,
                    ErrorParameter<IllegalArgumentException>
                            parameter) {
                Label message = new Label(parameter.getCustomMessage()); //get the message text
                getElement().appendChild(message.getElement());

                return HttpServletResponse.SC_NOT_FOUND;
            }
        }
        ```
- Getting Registered Routes
    - To retrieve all registered Routes, use:
        - The `RouteData` object contains all the relevant information about the defined route, such as the route template, parameters, and parent layouts.

        ```
        List<RouteData> routes = RouteConfiguration.forSessionScope().getAvailableRoutes();
        ```
        
    - Getting Registered Routes by Parent Layout
        - To retrieve all the routes defined by parent layout, use:

        ```
        List<RouteData> routes = RouteConfiguration.forSessionScope().getAvailableRoutes();
        List<RouteData> myRoutes = routes.stream()
                .filter(routeData -> MyParentLayout.class.equals((routeData.getParentLayout())))
                .collect(Collectors.toList());
        ```
- Updating the Page Title during Navigation
    - You can update the page title in two ways during navigation:
        - Use the `@PageTitle` annotation.
        - Implement `HasDynamicTitle`
        - These approaches are mutually exclusive; using both in the same class results in a runtime exception at startup.
    - Using the `@PageTitle` Annotation
        - The @PageTitle annotation is read only from the actual navigation target; super classes and parent views aren’t considered.

        ```
        @PageTitle("home")
        class HomeView extends Div {

            public HomeView() {
                setText("This is the home view");
            }
        }
        ```

    - Setting the Page Title Dynamically
        - You can also update the page title at runtime by implementing the `HasDynamicTitle` interface.
            - Override `public String getPageTitle()`
        - Example: Implementing HasDynamicTitle to update the page title.

        ```
        @Route(value = "blog")
        class BlogPost extends Component implements HasDynamicTitle, HasUrlParameter<Long> {
            private String title = "";

            @Override
            public String getPageTitle() {
                return title;
            }

            @Override
            public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
                if (parameter != null) {
                    title = "Blog Post #" + parameter;
                } else {
                    title = "Blog Home";
                }
            }
        }
        ```



- Registering Routes Dynamically
    - In addition to registering routes and route templates using the @Route annotation, you can add and remove routes dynamically at runtime. 
    - This is useful, for example, when a route should be added or removed based on changed business data or application configuration at startup.
    - The `RouteConfiguration` class can be used to limit route access to:
        - all users using the **application scope**, or
        - only certain active users using the **session scope**.
        - You can access the scope using the static methods `forSessionScope()` and `forApplicationScope()` from the `RouteConfiguration` class.
        - All components annotated with @Route are added to the application scope at startup, unless the `registerAtStartup` element has been set to false.   
    - Configuring User-Specific Routes
        - You can add and remove routes for certain users, for example, based on their access rights.
            - use `RouteConfiguration.forSessionScope().setRoute()`
        - Example: Adding admin and home views for users with an active session.

        ```
        RouteConfiguration.forSessionScope().setRoute(
                        "admin", //path
                        AdminView.class //navigation target
                );

        // parent layouts can be given as a vargargs parameter
        RouteConfiguration.forSessionScope().setRoute(
                        "home", //path
                        HomeView.class, //navigation target
                        MainLayout.class //one or more parents
                );
        ```
        
        - **Any registered @Route in the application scope can be overridden for a specific user in the session scope.**
        - The routes in the session scope are accessible for the current user only for as long as the session is valid.
        - **When the session is invalidated by the user logging out, the session-scoped routes are no longer available. It’s unnecessary to manually remove these routes.**
    - Removing Routes
        - When removing routes, you need to define precisely which route to remove.
        - Examples: Removing a navigation target (AdminView.class) with all possible route aliases and route templates registered to it.

        ```
        RouteConfiguration configuration = RouteConfiguration.forSessionScope();
        configuration.removeRoute(AdminView.class);         // No view AdminView will be available
        ```

        - Removing a path ("admin"), which only removes the target mapped to it. 

        ```
        configuration.removeRoute("admin");         // No path "admin" will be available
        ```

        - Removing a route in the session scope that had previously overridden a route in the application scope makes the application-scoped route accessible once again.
        - **When dynamically registering a route, any annotations on classes are ignored, unless the method used contains `Annotated`**; for example, `setAnnotatedRoute()`. 


    - Adding Routes on Application Startup
        - You can register routes during application startup using the `VaadinServiceInitListener`. 

        ```
        public class ApplicationServiceInitListener implements VaadinServiceInitListener {

            @Override
            public void serviceInit(ServiceInitEvent event) {
                // add view only during development time
                if (!event.getSource()
                        .getDeploymentConfiguration()
                        .isProductionMode()) {
                    RouteConfiguration configuration =RouteConfiguration.forApplicationScope();
                    configuration.setRoute(
                        "crud", //path
                        DBCrudView.class //navigation target
                    );
                }
            }
        }
        ```
    - Getting Registered Routes and Listening for Changes
        - When routes are registered dynamically, you may need to update UI components, such as navigation menus, based on the added or removed routes.
        - You can retrieve the registered route templates using the `getAvailableRoutes()` method from the RouteConfiguration. To be notified of route changes, you can register a listener using the `addRoutesChangeListener()` method.
        - You should use the session registry to monitor changes, because it contains all the routes that are available for the current user.
        - Example: Getting available routes and registering a routes change listener.

        ```
        RouteConfiguration configuration = RouteConfiguration.forSessionScope();
        // add all currently available views
        configuration.getAvailableRoutes().forEach(menu::addMenuItem);

        // add and remove menu items when routes are added and removed
        configuration.addRoutesChangeListener(event -> {
            // ignoring any route alias changes
            event.getAddedRoutes().stream()
                    .filter(route -> route instanceof RouteData)
                    .forEach(menu::addMenuItem);
            event.getRemovedRoutes().stream()
                    .filter(route -> route instanceof RouteData)
                    .forEach(menu::removeMenuItem);
        });
        ```
    - Dynamic Registration of @Route Annotated Classes
        - If you want to map all routes in the same way using the @Route annotation, you can **configure the routes statically, but postpone registration until runtime.**
        - To skip static registration to the application-scoped registry on start-up, add the `registerAtStartup = false` parameter to the @Route annotation. This also makes it easier to use existing parent chains and paths that are modified from the parent.
        - Register the view during runtime using `RouteConfiguration.forSessionScope().setAnnotatedRoute(ReportView.class);`
        - Example: Using the registerAtStartup parameter to postpone route registration.

        ```
        @Route(value = "quarterly-report", layout = MainLayout.class, registerAtStartup = false)
        @RouteAlias(value = "qr", layout = MainLayout.class)
        public class ReportView extends VerticalLayout implements HasUrlParameter<String> {
            // implementation omitted
        }

        // register the above view during runtime
        if (getCurrentUser().hasAccessToReporting()) {
            RouteConfiguration.forSessionScope()
                    .setAnnotatedRoute(ReportView.class);
        }
        ```

    - Example: Adding a New View on User Log-in
        - Example: Two types of users exist: admin users and normal users. After log-in, a different view is shown depending on the user’s access rights.
        - The LoginPage class, which defines a statically registered route, "". This route is mapped to the log-in used for user authentication.

        ```
        @Route("")
        public class LoginPage extends Div {

            private TextField login;
            private PasswordField password;

            public LoginPage() {
                login = new TextField("Login");
                password = new PasswordField("Password");

                Button submit = new Button("Submit", this::handleLogin);

                add(login, password, submit);
            }

            private void handleLogin(ClickEvent<Button> buttonClickEvent) {
            }
        }
        ```

        - The MainLayout class, which contains a menu.

        ```
        public class MainLayout extends Div implements RouterLayout {
            public MainLayout() {
                // Implementation omitted, but could contain
                // a menu.
            }
        }
        ```

        - The InfoView class, which defines the "info" route. This route isn’t statically registered, because it has the registerAtStartup = false parameter.

        ```
        @Route(value = "info", layout = MainLayout.class, registerAtStartup = false)
        public class InfoView extends Div {
            public InfoView() {
                add(new Span("This page contains info about "
                        + "the application"));
            }
        }

        ```
        - After log-in, add a new route is added, depending on the access rights of the user. Two available targets are possible:

            - AdminView class.

            ```
            public class AdminView extends Div {
            }
            ```

            - UserView class.

            ```
            public class UserView extends Div {
            }
            ```

        - The LoginPage class handles adding only to the user session as follows:

        ```
        private void handleLogin(ClickEvent<Button> buttonClickEvent) {
            // Validation of credentials is skipped

            RouteConfiguration configuration = RouteConfiguration.forSessionScope();

            if ("admin".equals(login.getValue())) {
                configuration.setRoute("", AdminView.class, MainLayout.class);
            } else if ("user".equals(login.getValue())) {
                configuration.setRoute("", UserView.class, MainLayout.class);
            }

            configuration.setAnnotatedRoute(InfoView.class);

            UI.getCurrent().getPage().reload();
        }
        ```


        - A new target for the path "" is added to the session-scoped route registry. The new target overrides the application-scoped path "" for the user.

        - The InfoView class is added using the layout setup, configured using the @Route annotation. It’s registered to the path "info" with the same MainLayout as the parent layout. Other users on other sessions still get a log-in for the "" path and can’t access "info". 


