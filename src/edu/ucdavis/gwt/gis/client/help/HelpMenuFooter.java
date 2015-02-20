package edu.ucdavis.gwt.gis.client.help;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModal;

public class HelpMenuFooter extends FlowPanel {
	
	private Anchor closeBtn = new Anchor("Close");
	private Anchor menuBtn = new Anchor("Help Topics");
	
	public HelpMenuFooter() {
		
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				BootstrapModal.INSTANCE.hide();
			}
		});
		
		menuBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				MainHelpMenu.INSTANCE.show();
			}
		});
		
		closeBtn.addStyleName("btn");
		
		menuBtn.addStyleName("btn");
		menuBtn.addStyleName("btn-primary");
		
		add(menuBtn);
		add(closeBtn);
	}

}
