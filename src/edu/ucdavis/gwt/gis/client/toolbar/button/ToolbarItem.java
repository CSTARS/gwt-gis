package edu.ucdavis.gwt.gis.client.toolbar.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public abstract class ToolbarItem extends Anchor {
		
	public void init() {
		String txt = "";
		if( getIcon() != null ) txt += getIcon();
		if( !getText().isEmpty() ) txt += "&nbsp;&nbsp;"+getText();
		
		setHTML(txt);
		setStyleName("menu-link");
		
		addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				ToolbarItem.this.onClick();
			}
		});
	}
	
	public abstract String getIcon();
	
	public abstract String getText();
	
	public abstract void onClick();
	
	public abstract void onAdd(Toolbar toolbar);
	
	
}
