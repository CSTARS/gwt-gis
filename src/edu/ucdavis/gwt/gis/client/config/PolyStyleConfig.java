package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

public class PolyStyleConfig extends JavaScriptObject {
    
    protected PolyStyleConfig() {}
    
    public final native JavaScriptObject getFillColor() /*-{
        if( this.fill ) return this.fill;
        return {r:255,g:0,b:0,a:.75};
    }-*/;
    
    public final native JavaScriptObject getOutlineColor() /*-{
        if( this.outline ) return this.outline;
        return {r:255,g:0,b:0,a:1};
    }-*/;
    
}
