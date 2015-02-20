package edu.ucdavis.gwt.gis.client.layout.modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

import edu.ucdavis.gwt.gis.client.AppManager;

/**
 * Main menu footer has a 'Close' button and an 'Menu'
 * btn to return to the main menu
 * 
 * @author jrmerz
 *
 */
public class MainMenuFooter extends FlowPanel {
	
	private Anchor closeBtn = new Anchor("Close");
	private Anchor menuBtn = new Anchor("Menu");
	
	public MainMenuFooter() {
		
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				BootstrapModal.INSTANCE.hide();
			}
		});
		
		menuBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				AppManager.INSTANCE.getClient().getToolbar().showMenu();
			}
		});
		
		closeBtn.addStyleName("btn");
		
		menuBtn.addStyleName("btn");
		menuBtn.addStyleName("btn-primary");
		
		add(menuBtn);
		add(closeBtn);
	}

}
