package edu.ucdavis.gwt.gis.client.toolbar.menu;

import java.util.LinkedList;

import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;

public abstract class ToolbarMenuItem extends Composite {
	
	//private HTML panel = null;
	private LinkedList<ToolbarItem> items = new LinkedList<ToolbarItem>();
	private FlowPanel itemPanel = new FlowPanel();
	private ToolbarPopupMenu popup = null;
	
	public ToolbarMenuItem() {
		setTitleName(getText());
	}
	
	public void init(ToolbarPopupMenu popup) {
		this.popup = popup;
		
		/*String html = "";
		Image icon = getIcon();
		if( icon != null ) {
			icon.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
			html += icon.toString();
		}
		
		if( getText() != null ) {
			if( getText().length() > 0 && icon != null ) html += "&nbsp;";
		}
		
		html += getText();
		
		panel = new HTML(html);
		panel.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( !popup.isShowing() ) popup.showRelativeTo(panel);
			}
		});
		panel.setStyleName("toolbarButton"); */
		initWidget(itemPanel);
		//popup.addAutoHidePartner(panel.getElement());
	}
	
	// can be null
	public abstract Image getIcon();
	
	public abstract String getText();
	
	
	private void setTitleName(String title) {
		HTML t = new HTML(title);
		t.setStyleName("toolbarPopupMenu-title");
		itemPanel.add(t);
	}
	
	public void addItem(ToolbarItem item) {
		item.init();
		itemPanel.add(item);
		items.add(item);
	}
	
	public void onAdd(Toolbar toolbar) {
		for( ToolbarItem item: items ) item.onAdd(toolbar);
	}
	
	
}
