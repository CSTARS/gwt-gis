package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class ToolbarPopupMenu extends BootstrapModalLayout {
	
	private SimplePanel outerPanel = new SimplePanel();
	
	private FlowPanel panel = new FlowPanel();
	private FlowPanel panelLeft = new FlowPanel();
	private FlowPanel panelRight = new FlowPanel();
	
	private Anchor closeBtn = new Anchor("Close");
	
	int count = 0;
	
	public ToolbarPopupMenu(String title) {
		
		outerPanel.add(panel);
		
		panel.addStyleName("row-fluid");
		panelLeft.addStyleName("span6");
		panelRight.addStyleName("span6");
		
		panel.add(panelLeft);
		panel.add(panelRight);
		
		closeBtn.setStyleName("btn");
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}

	
	public void addMenu(ToolbarMenuItem menu) {
		if( count % 2 == 0 ) panelLeft.add(menu);
		else panelRight.add(menu);
		count++;
	}


	@Override
	public String getTitle() {
		return "<i class='icon-reorder'></i> Main Menu";
	}


	@Override
	public Widget getBody() {
		return outerPanel;
	}


	@Override
	public Widget getFooter() {
		return closeBtn;
	}

}
