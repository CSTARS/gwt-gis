package edu.ucdavis.gwt.gis.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.config.HelpTopicConfig;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class MainHelpMenu extends BootstrapModalLayout {

    private static MainHelpMenuUiBinder uiBinder = GWT.create(MainHelpMenuUiBinder.class);
    interface MainHelpMenuUiBinder extends UiBinder<Widget, MainHelpMenu> {}

    public static MainHelpMenu INSTANCE = new MainHelpMenu();
    
    /*@UiField Anchor layersMenuBtn;
    @UiField Anchor toolbarMenuBtn;
    @UiField Anchor drawMenuBtn;*/
    @UiField FlowPanel root;
    
    private JsArray<HelpTopicConfig> topics;
    private Widget body;
    private MainMenuFooter footer = new MainMenuFooter();
    
    protected MainHelpMenu() {
        body = uiBinder.createAndBindUi(this);
        
        topics = AppManager.INSTANCE.getConfig().getHelpTopics();
        for( int i = 0; i < topics.length(); i++ ) {
            addTopic(topics.get(i));
        }
        
        /*layersMenuBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                LayersHelpMenu.INSTANCE.show();
            }
        });
        toolbarMenuBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ToolbarHelpMenu.INSTANCE.show();
            }
        });
        drawMenuBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                DrawToolHelpMenu.INSTANCE.show();
            }
        });*/
    }
    
    private void addTopic(HelpTopicConfig topic) {
        FlowPanel fp = new FlowPanel();
        
        Anchor a = new Anchor(topic.getTitle(), topic.getUrl(), "_blank");
        HTML desc = new HTML(topic.getDescription());
        desc.getElement().getStyle().setColor("#888");
        desc.getElement().getStyle().setProperty("padding", "0 0 10px 20px");
        
        fp.add(a);
        fp.add(desc);
        root.add(fp);
    }

    public boolean hasTopics() {
        if( this.topics.length() > 0 ) return true;
        return false;
    }
    
    @Override
    public String getTitle() {
        return "<i class='icon-question-sign'></i> Help Topics";
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
