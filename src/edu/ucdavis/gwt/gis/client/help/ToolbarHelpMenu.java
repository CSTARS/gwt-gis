package edu.ucdavis.gwt.gis.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class ToolbarHelpMenu extends BootstrapModalLayout {

    private static ToolbarHelpMenuUiBinder uiBinder = GWT.create(ToolbarHelpMenuUiBinder.class);
    interface ToolbarHelpMenuUiBinder extends UiBinder<Widget, ToolbarHelpMenu> {}

    public static ToolbarHelpMenu INSTANCE = new ToolbarHelpMenu();
    
    private Widget body;
    private HelpMenuFooter footer = new HelpMenuFooter();
    
    protected ToolbarHelpMenu() {
        body = uiBinder.createAndBindUi(this);
    }

    @Override
    public String getTitle() {
        return "<i class='icon-question-sign'></i> Top Toolbar Help";
    }

    @Override
    public Widget getBody() {
        return body;
    }

    @Override
    public Widget getFooter() {
        return footer;
    }

}
