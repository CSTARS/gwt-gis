package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;

public class RootLayout extends Composite {
	
	private static RootLayoutUiBinder uiBinder = GWT.create(RootLayoutUiBinder.class);
	interface RootLayoutUiBinder extends UiBinder<Widget, RootLayout> {}

	@UiField SimplePanel mapPanel;
	@UiField SimplePanel layerPanel;
	
	public RootLayout() {
		initWidget(uiBinder.createAndBindUi(this));
		mapPanel.getElement().setId("gwt-gis-map");
		mapPanel.setSize("600px", "600px");
	}
	
	/*public void setMap(MapWidget map) {
		mapPanel.add(map);
	}*/
	
	public SimplePanel getMapPanel() {
		return mapPanel;
	}
	
	public void setLayersPanel(LayersPanel layersPanel) {
		layerPanel.add(layersPanel);
		
		if( GisClient.isIE7() || GisClient.isIE8() || GisClient.isIE9() ) {
			layerPanel.addStyleName("IE");
		}
	}
	
	public boolean isPhoneMode() {
		return _isPhoneMode();
	}
	
	private native boolean _isPhoneMode() /*-{
		if( $wnd.$(".nav-expand-btn").css("display") == "block" ) return true;
		return false;
	}-*/;
	
	public void toggleNav(Anchor btn) {
		if( GisClient.isIE7() || GisClient.isIE8() || GisClient.isIE9() ) {
			// crappy IE
			if( mapPanel.getParent().getStyleName().contains("openIE") ) {
				mapPanel.getParent().removeStyleName("openIE");
				layerPanel.removeStyleName("openIE");
				layerPanel.addStyleName("IE");
				btn.setHTML("<i class='fa fa-angle-double-left'></i>");
				
			} else {
				mapPanel.getParent().addStyleName("openIE");
				layerPanel.addStyleName("openIE");
				layerPanel.removeStyleName("IE");
				btn.setHTML("<i class='fa fa-angle-double-right'></i>");
				
			}
			
		} else {
			// not crappy IE
			
			if( mapPanel.getParent().getStyleName().contains("open") ) {
				mapPanel.getParent().removeStyleName("open");
				layerPanel.removeStyleName("open");
				btn.setHTML("<i class='fa fa-angle-double-left'></i>");
			} else {
				mapPanel.getParent().addStyleName("open");
				layerPanel.addStyleName("open");
				btn.setHTML("<i class='fa fa-angle-double-right'></i>");
			}
		}
		
	}
	
}
