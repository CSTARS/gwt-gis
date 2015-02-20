package edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features;

import com.google.gwt.core.client.JavaScriptObject;

public class Feature extends JavaScriptObject {
	
	protected Feature() {}
	
	public final native JavaScriptObject getAttributes() /*-{
		if( this.attributes ) return this.attributes;
		return {};
	}-*/;
	
	public final native boolean hasSymbol() /*-{
		if( this.symbol ) return true;
		return false;
	}-*/;
	
	public final native JavaScriptObject getSymbolJson() /*-{
		if( this.symbol ) return this.symbol;
		return {};
	}-*/;

}
