package edu.ucdavis.gwt.gis.client.state.overlays;

import com.google.gwt.core.client.JavaScriptObject;

public class DataLayerOverlay extends JavaScriptObject {
	
	protected DataLayerOverlay() {}
	
	public final native String getType() /*-{
		if( this.type ) return this.type;
		return "";
	}-*/;

	public final native String getLabel() /*-{
		if( this.label ) return this.label;
		return "";
	}-*/;
	
	public final native String getUrl() /*-{
		if( this.url ) return this.url;
		return "";
	}-*/;
	
	public final native String getLegendUrl() /*-{
		if( this.legendUrl ) return this.legendUrl;
		return "";
	}-*/;
	
	public final native boolean isVisible() /*-{
		if( this.visible ) return this.visible;
		return false;
	}-*/;
	
	public final native double getOpacity() /*-{
		if( this.opacity ) return this.opacity;
		return 1;
	}-*/;
	
	public final native boolean showLegendOnLoad() /*-{
		if( this.showLegendOnLoad ) return this.showLegendOnLoad;
		return this.showLegendOnLoad;
	}-*/;

}
