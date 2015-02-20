package edu.ucdavis.gwt.gis.client.state.overlays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GraphicsLayerOverlay extends JavaScriptObject {
	
	protected GraphicsLayerOverlay() {}
	
	public final native boolean hasRenderer() /*-{
		if( this.renderer != null ) return true;
		return false;
	}-*/;
	
	public final native JavaScriptObject getRenderer() /*-{
		if( this.renderer ) return this.renderer;
		return {};
	}-*/;
	
	public final native JsArray<JavaScriptObject> getGraphics() /*-{
		if( this.graphics ) return this.graphics;
		return [];
	}-*/;

}
