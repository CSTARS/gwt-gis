package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class Error extends JavaScriptObject {
	
	protected Error() {}
	
	public final native int getCode() /*-{
		if( this.code ) return this.code;
		return -1;
	}-*/;

	public final native String getMessage() /*-{
		if( this.message ) return this.message;
		return "";
	}-*/;
	
	public final native JsArrayString getDetails() /*-{
		if( this.details ) return this.details;
		return [];
	}-*/;
	
}
