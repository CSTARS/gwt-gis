package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

import edu.ucdavis.gwt.gis.client.layers.DataLayer;

public abstract class LayerMenuItem extends Composite {
	
	protected Anchor a = new Anchor();
	private DataLayer dataLayer = null;
	
	public void init(DataLayer dl) {
		dataLayer = dl;
		
		String txt = "";
		if( getIcon() != null ) txt += getIcon();
		if( !getText().isEmpty() ) txt += getText();
		
		a.setStyleName("menu-link");
		a.setHTML(txt);
		

		a.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				LayerMenuItem.this.onClick(dataLayer);
			}
		});
		initWidget(a);
	}
	
	public abstract String getIcon();
	
	public abstract String getText();
	
	public abstract void onClick(DataLayer dataLayer);
	
	public void addClickHandler(ClickHandler clickHandler) {
		a.addClickHandler(clickHandler);
	}
	
}
