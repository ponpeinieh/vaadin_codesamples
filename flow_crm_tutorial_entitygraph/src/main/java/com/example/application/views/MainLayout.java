package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.views.list.ListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;

import java.util.Optional;
public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private Tabs menu;

    private H1 viewTitle;
    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        viewTitle = new H1("Vaadin CRM");
        viewTitle.addClassNames("text-l", "m-m");
        Button logout = new Button("Log out", e -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                viewTitle,
                logout
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

//    private void createDrawer() {
//        RouterLink listLink = new RouterLink("List", ListView.class);
//        listLink.setHighlightCondition(HighlightConditions.sameLocation());
//
//        addToDrawer(new VerticalLayout(
//                listLink,
//                new RouterLink("Dashboard", DashboardView.class)
//        ));
//    }

    private void createDrawer() {
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
    }
    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames("drawer");
        // Configure styling for the drawer
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Have a drawer header with an application logo
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        Image logo = new Image("images/bet.png", "Niu Niu Logo");
        logoLayout.add(logo);
        logoLayout.add(new H1("My Project"));

        // Display the logo and the menu in the drawer
        layout.add(logoLayout, menu);
        return layout;
    }
    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[] { createTab("List", ListView.class),
                createTab("Dashboard", DashboardView.class)};
    }

    private static Tab createTab(String text,
                                 Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        // Select the tab corresponding to currently shown view
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);

        // Set the view title in the header
        viewTitle.setText(getCurrentPageTitle());
    }
    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class)
                        .equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }
    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(PageTitle.class).value();
    }
}
