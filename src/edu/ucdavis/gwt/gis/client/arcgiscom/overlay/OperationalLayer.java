package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;

import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features.FeatureCollection;

public class OperationalLayer extends JavaScriptObject {

	protected OperationalLayer() {}
	
	public final native String getTitle() /*-{
		if( this.title ) return this.title;
		return "";
	}-*/;
	
	public final native String getId() /*-{
		if( this.id ) return this.id;
		return "";
	}-*/;
	
	public final native double getOpacity() /*-{
		if( this.opacity ) return this.opacity;
		return 1;
	}-*/;
	
	public final native boolean isVisible() /*-{
		if( this.visible ) return true;
		return false;
	}-*/;
	
	public final native String getUrl() /*-{
		if( this.url ) return this.url;
		return "";
	}-*/;
	
	public final native FeatureCollection getFeatureCollection() /*-{
		if( this.featureCollection ) return this.featureCollection;
		return {};
	}-*/;
	
	public final native boolean hasFeatureCollection() /*-{
		if( this.featureCollection ) return true;
		return false;
	}-*/;

}
