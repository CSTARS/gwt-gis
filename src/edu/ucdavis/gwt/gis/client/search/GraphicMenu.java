package edu.ucdavis.gwt.gis.client.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class GraphicMenu extends BootstrapModalLayout {

    private static GraphicMenuUiBinder uiBinder = GWT.create(GraphicMenuUiBinder.class);
    interface GraphicMenuUiBinder extends UiBinder<Widget, GraphicMenu> {}

    private Widget panel = null;
    
    @UiField HTML label;
    @UiField Element cp;
    @UiField Anchor deleteBtn;
    
    private Anchor closeBtn = new Anchor("Close");
    
    public GraphicMenu() {
        panel = uiBinder.createAndBindUi(this);
        
        deleteBtn.setHTML("<i class='icon-remove'></i> Remove");
        closeBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
    }
    
    public void show(String name, Graphic g) {
        label.setHTML(name);
        super.show();
    }

    @Override
    public String getTitle() {
        return "Graphic Settings";
    }

    @Override
    public Widget getBody() {
        return panel;
    }

    @Override
    public Widget getFooter() {
        // TODO Auto-generated method stub
        return closeBtn;
    }

}
