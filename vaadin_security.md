 Vaadin Security

 - In a Spring Boot application, Vaadin Flow built-in security helpers enable a **view-based access control** mechanism with minimum Spring Security configurations. 
 - This view-based access control mechanism enables you to fully secure views in Vaadin Flow applications in a flexible way, based on different **access level annotations**. 
 - Specifically, the view-based access control mechanism uses the `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed`, and `@DenyAll` annotations on view classes to define the access control rules.

- To enable the mechanism in a Vaadin Flow Spring Boot application without any security, add the following to the project
    - A Login view.
    - Spring Security dependencies.
    - Log-out capability.
    - A security configuration class that extends `VaadinWebSecurity`.
    - One of the following annotations on each view class: **@AnonymousAllowed, @PermitAll, or @RolesAllowed.**

- Log-in View
    - Having a log-in view is a basic requirement of many authentication and authorization mechanisms, to be able to redirect anonymous users to that page before giving access to view any protected resources.
    - The log-in view should always be accessible by anonymous users.
    - Usage of Vaadin’s Login Form component

    ```
    @Route("login")
    @PageTitle("Login")
    @AnonymousAllowed
    public class LoginView extends VerticalLayout implements BeforeEnterObserver {

        private LoginForm login = new LoginForm();

        public LoginView() {
            addClassName("login-view");
            setSizeFull();

            setJustifyContentMode(JustifyContentMode.CENTER);
            setAlignItems(Alignment.CENTER);

            login.setAction("login");

            add(new H1("Test Application"), login);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
            if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
                login.setError(true);
            }
        }
    }
    ```
- Spring Security Dependencies
    - To enable Spring Security, add the following dependency
        - **Spring Boot Starter Security** 

    ```
    <dependencies>
        <!-- other dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- other dependencies -->
    </dependencies>
    ```
- Log-Out Capability
    - To handle log-out in web applications, one typically uses a log-out button. 
    - Here is a basic implementation of a log-out button shown on the header of the main layout:
        - `securityService.getAuthenticatedUser()    `
        - `securityService.logout()`

    ```
    public class MainLayout extends AppLayout {

        private SecurityService securityService;

        public MainLayout(@Autowired SecurityService securityService) {
            this.securityService = securityService;

            H1 logo = new H1("Vaadin CRM");
            logo.addClassName("logo");
            HorizontalLayout header;
            if (securityService.getAuthenticatedUser() != null) {
                Button logout = new Button("Logout", click ->
                        securityService.logout());
                header = new HorizontalLayout(logo, logout);
            } else {
                header = new HorizontalLayout(logo);
            }

            // Other page components omitted.

            addToNavbar(header);
        }
    }
    ```
    - SecurityService

    ```
    @Component
    public class SecurityService {

        private static final String LOGOUT_SUCCESS_URL = "/";

        public UserDetails getAuthenticatedUser() {
            SecurityContext context = SecurityContextHolder.getContext();
            Object principal = context.getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) context.getAuthentication().getPrincipal();
            }
            // Anonymous or no authentication.
            return null;
        }

        public void logout() {
            UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(), //get the session through Servlet request 
                    null,
                    null);
        }
    }
    ```

- Security Configuration Class
    - The next step is to have a Spring Security class that extends `VaadinWebSecurity`. 
        - `VaadinWebSecurity` is a helper class that configures the common Vaadin-related Spring security settings. 
        - By extending it, the **view-based access control mechanism is enabled automatically**, and no further configuration is needed to enable it.
    - There’s no convention for naming this class, so in this documentation it’s named SecurityConfiguration. 
    - Delegating the responsibility of general configurations of http security to the super class. 
    - It's configuring the followings: 
        - Vaadin's `CSRF protection` by ignoring framework's internal requests, default request cache,
            ignoring public views annotated with `@AnonymousAllowed`, restricting access to other views/endpoints, and enabling `ViewAccessChecker` authorization.
        - http.rememberMe().alwaysRemember(false);
        - **Configure your static resources with public access before calling `super.configure(HttpSecurity)` as it adds final anyRequest matcher**
        - This is important to **register your login view** to the view access checker mechanism
            -  This is how the view-based access control mechanism knows where to redirect users when they attempt to navigate to a protected view. The log-in view should always be accessible by anonymous users, so it should have the `@AnonymousAllowed` annotation.
    - The default implementation of the configure methods takes care of all the Vaadin-related configuration, for example ignoring static resources, or enabling CSRF checking, while ignoring unnecessary checking for Vaadin internal requests, etc.
    ```
    @EnableWebSecurity 
    @Configuration
    public class SecurityConfiguration extends VaadinWebSecurity { 

        @Override
        protected void configure(HttpSecurity http) throws Exception {
           
            http.authorizeRequests().antMatchers("/public/**")
                .permitAll();

            super.configure(http); 

            setLoginView(http, LoginView.class); 
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // Customize your WebSecurity configuration.
            super.configure(web);
        }

        /**
        * Demo UserDetailsManager which only provides two hardcoded
        * in memory users and their roles.
        * NOTE: This shouldn't be used in real world applications.
        */
        @Bean
        public UserDetailsManager userDetailsService() {
            UserDetails user =
                    User.withUsername("user")
                            .password("{noop}user")
                            .roles("USER")
                            .build();
            UserDetails admin =
                    User.withUsername("admin")
                            .password("{noop}admin")
                            .roles("ADMIN")
                            .build();
            return new InMemoryUserDetailsManager(user, admin);
        }
    }
    ```
- Component-based security configuration
    - `Spring Security 5.7.0` deprecates the `WebSecurityConfigurerAdapter` and encourages users to move towards a component-based security configuration.
    - VaadinWebSecurityConfigurerAdapter is still available for Vaadin 23.2 users, although it’s recommended to use component-based security configuration as in SecurityConfiguration example above.

- Annotating the View Classes
    - `@AnonymousAllowed`: permits anyone to navigate to the view without any authentication or authorization.
    - `@PermitAll`: **allows any authenticated user to navigate to the view.** 
    - `@RolesAllowed`: grants access to users having the roles specified in the annotation value.
    - `@DenyAll`: disallows everyone from navigating to the view.
        - This is the default, which means that, if a view isn’t annotated at all, the @DenyAll logic is applied.