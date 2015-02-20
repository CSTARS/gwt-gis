package edu.ucdavis.gwt.gis.client.state.overlays;

import com.google.gwt.core.client.JavaScriptObject;

public class BasemapOverlay extends JavaScriptObject {

	protected BasemapOverlay() {}
	
	public final native String getLabel() /*-{
		if( this.label ) return this.label;
		return "";
	}-*/;
	
	public final native String getId() /*-{
		if( this.id ) return this.id;
		return "";
	}-*/;
	
	public final native String getIconUrl() /*-{
		if( this.iconUrl ) return this.iconUrl;
		return "";
	}-*/;
	
}
