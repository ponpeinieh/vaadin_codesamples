- Test various events occuring sequence:
    1. First the view constructor is called
    2. If implementing BeforeEnterObserver, the beforeEnter() is called.
    3. If triggering any events, event handler may be run now
    4. onAttach() is called. 
- Fire events:
    - Event source using **current UI** object, since the MainLayout doesn't have a reference to the view object
        - The UI object is shared between these views
        - But be careful when browser reload problem -  which cause the UI object to be regenerated.

        ```
        ComponentUtil.fireEvent(UI.getCurrent(), new HistoryRouteEvent(UI.getCurrent(),
            Map.of("betAmount", betAmount, "tableNo", tableNo)));
        ```
- Register for an event:
    - this registration step needs to be put inside onAttach(), so if the user does a browser reload, it will re-register against the new UI object.

    ```
    private void registerListeners(){
        registration = ComponentUtil.addListener(UI.getCurrent(),HistoryRouteEvent.class, this::updateHistoryRoutes);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //move the registerListeners() call here, so during browser reload, the onAttach() will be called again,
        // so it can register the listener against the newly created UI instance.
        registerListeners();
    }
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
    }
    ```
- Event handler:
    - get the event data attached to the event object
    - update corresponding state of the listener view, eg. `historyRouterLinks.put(label, rl);`
    - update components using remove/add to trigger onDetach(), onAttach()

```
    private void updateHistoryRoutes(HistoryRouteEvent event) {
        BetAmount betAmount = (BetAmount) event.getEventData().get("betAmount");
        TableNo tableNo = (TableNo) event.getEventData().get("tableNo");
        String label = String.format("注金%4s元, %s", betAmount.getName(),tableNo.getName() );
        RouterLink rl = new RouterLink(label, BetView.class,
                new RouteParameters(new RouteParam("betAmount",  betAmount.getName()),
                        new RouteParam("tableNo", Integer.toString(tableNo.getNo()))));
        historyRouterLinks.put(label, rl);
        printout(historyRouterLinks);
        drawerContent.remove(historyBetRoutesComponent);
        drawerContent.add(historyBetRoutesComponent);
    }

```

```
public class HistoryBetRoutesComponent extends VerticalLayout {

    private final Accordion accordion = new Accordion();
    private AccordionPanel historyPanel;
    private MyLinkedHashMap<String, RouterLink> historyRouterLinks;

    public HistoryBetRoutesComponent(MyLinkedHashMap<String, RouterLink> historyRouterLinks) {
        LoggerFactory.getLogger(getClass()).info("In HistoryBetRoutesComponent ctor called");
        this.historyRouterLinks = historyRouterLinks;
    }

    protected void onAttach(AttachEvent attachEvent) {
        LoggerFactory.getLogger(getClass()).info("In HistoryBetRoutesComponent: onAttach() called");
        historyPanel = accordion.add("歷史下注區", createContent());
        historyPanel.addThemeVariants(DetailsVariant.REVERSE);
        add(accordion);
    }

    protected void onDetach(DetachEvent detachEvent) {
        LoggerFactory.getLogger(getClass()).info("In HistoryBetRoutesComponent: onDetach() called");
        accordion.remove(historyPanel);
    }

    private VerticalLayout createContent() {
        VerticalLayout content = new VerticalLayout();
        historyRouterLinks.keySet().stream().forEach(label -> {
            content.add(createStyledAnchor(historyRouterLinks.get(label).getHref(), label));
        });
        return content;
    }

    private Anchor createStyledAnchor(String href, String text) {
        Anchor anchor = new Anchor(href, text);
        anchor.getStyle().set("color", "var(--lumo-primary-text-color)");
        anchor.getStyle().set("text-decoration", "none");

        return anchor;
    }

}
```