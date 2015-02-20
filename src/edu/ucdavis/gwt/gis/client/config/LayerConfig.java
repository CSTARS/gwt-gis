package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DataLayer config object
 * 
 * @author jrmerz
 */
public class LayerConfig extends JavaScriptObject {

	protected LayerConfig() {}
	
	/**
	 * Get the nice label for this layer
	 * 
	 * @return String
	 */
	public final native String getLabel() /*-{
		if( this.label) return this.label;
		return "";
	}-*/;
	
	/**
	 * Get the url of the layer
	 * 
	 * @return String
	 */
	public final native String getUrl() /*-{
		if( this.url ) return this.url;
		return "";
	}-*/;
	
	/**
	 * Get the url of the restful legend service for this layer
	 * 
	 * @return String
	 */
	public final native String getLegendUrl() /*-{
		if( this.legendUrl ) return this.legendUrl;
		return "";
	}-*/;
	
	/**
	 * Should the legend be rendered as a gradient?
	 * 
	 * @return boolean
	 */
	public final native boolean legendIsGradient() /*-{
		if( this.legendIsGradient ) return true;
		return false;
	}-*/;
	
	public final native boolean showLegend() /*-{
	    if( this.showLegend ) return this.showLegend;
	    return false;
	}-*/;
	
	//public final native boolean useLegendProxy() /*-{
	//	if( this.proxyLegend ) return this.proxyLegend;
	//	return false;
	//}-*/;
	
	/**
	 * Should the layer be visible by default?
	 * 
	 * @return boolean
	 */
	public final native boolean isVisible() /*-{
		if( this.visible != null ) return this.visible;
		return false;
	}-*/;
	
	/**
	 * Get the default opacity for this layer
	 * 
	 * @return double
	 */
	public final native double getOpacity() /*-{
		if( this.transparency ) return this.transparency;
		return 100;
	}-*/;
	
}
