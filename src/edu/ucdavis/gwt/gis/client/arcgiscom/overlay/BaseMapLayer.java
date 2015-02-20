package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;

public class BaseMapLayer extends JavaScriptObject {

	protected BaseMapLayer() {}
	
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
	
	public final native boolean isReference() /*-{
		if( this.isReference ) return true;
		return false;
	}-*/;
	
}
