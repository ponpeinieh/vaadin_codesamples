Various notes about VaadinService, Vaadin Session, UI, Cookies etc

- Cookies
```
private static void invalidateAllCookies() {
    Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

    // Iterate to invalidate
    for (Cookie cookie : cookies) {
        if (cookie.getName().startsWith("ClouTree.")) {
            cookie.setValue(null);// ww w  .j  a va2s .  c o  m
            cookie.setMaxAge(0);
        }
    }
}
```

- VaadinRequest 
    - Can get Cookies, Wrapped Session, etc.
    - Get the current request object : 
        - VaadinRequest.getCurrent()
        - VaadinService.getCurrentRequest()

- VaadinSession
    - VaadinService.getCurrentRequest().getWrappedSession()

    ```
    public static final String CURRENT_USER_SESSION_ATTRIBUTE_KEY = CurrentUser.class
            .getCanonicalName();
            
     public static void set(String currentUser) {
        if (currentUser == null) {
            VaadinService.getCurrentRequest().getWrappedSession().removeAttribute(
                    CURRENT_USER_SESSION_ATTRIBUTE_KEY);
        } else {
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                    CURRENT_USER_SESSION_ATTRIBUTE_KEY, currentUser);
        }
    }
    ```

- VaadinService
    - VaadinService.getCurrent()