package edu.ucdavis.gwt.gis.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class LayersHelpMenu extends BootstrapModalLayout {

    private static LayersHelpMenuUiBinder uiBinder = GWT.create(LayersHelpMenuUiBinder.class);
    interface LayersHelpMenuUiBinder extends UiBinder<Widget, LayersHelpMenu> {}

    public static LayersHelpMenu INSTANCE = new LayersHelpMenu();
    
    private Widget body;
    private HelpMenuFooter footer = new HelpMenuFooter();
    
    protected LayersHelpMenu() {
        body = uiBinder.createAndBindUi(this);
    }

    @Override
    public String getTitle() {
        return "<i class='fa fa-question-circle'></i> Layers Menu Help";
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
