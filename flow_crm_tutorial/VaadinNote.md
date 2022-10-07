Vaadin used with Spring Boot:

- Routing in Spring Boot and WAR Applications
    - There is a difference between running an application as a Spring Boot application and as a WAR application deployed to a web server.
        - In WAR applications, all @Route annotations are discovered automatically, due to the Servlet 3.0 specification. 
        - With Spring Boot applications, this is, by design, not the case. 
        - **The Vaadin Spring add-on implements scanning for router classes in Spring Boot applications.** This is also true for other Vaadin types that need to be discovered and registered at start-up. 
            - However, **scanning only occurs inside the Spring Boot application class package, that is, the package in which the @SpringBootApplication class resides.**

- Using Scopes with Vaadin and Spring
    - You need to mark Vaadin-related bean classes with the @Component annotation to allow them to be picked up as managed beans. 
    - Because there is also a Component class in Vaadin, you can use the **@SpringComponent annotation from the Vaadin Spring add-on** to avoid having to use fully qualified names.
    - If no scope is specified in addition to the @SpringComponent / @Component annotation, the component is in singleton scope.
    - In addition to standard Spring scopes, the Vaadin Spring add-on introduces three additional scopes:
        - `VaadinSessionScope` to target beans to a user’s session
        - `UIScope` to target beans to a browser window or tab opened by the user
        - `RouteScope` to target beans to certain routing components pointed to by the `RouteScopeOwner `qualifier