package edu.ucdavis.gwt.gis.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class DrawToolHelpMenu extends BootstrapModalLayout {

    private static DrawToolHelpMenuUiBinder uiBinder = GWT.create(DrawToolHelpMenuUiBinder.class);
    interface DrawToolHelpMenuUiBinder extends UiBinder<Widget, DrawToolHelpMenu> {}

    public static DrawToolHelpMenu INSTANCE = new DrawToolHelpMenu();
    
    private Widget body;
    private HelpMenuFooter footer = new HelpMenuFooter();
    
    protected DrawToolHelpMenu() {
        body = uiBinder.createAndBindUi(this);
    }

    @Override
    public String getTitle() {
        return "<i class='icon-question-sign'></i> Draw Tool Help";
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
