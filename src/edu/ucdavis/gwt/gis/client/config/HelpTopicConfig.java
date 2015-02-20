package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;


public class HelpTopicConfig extends JavaScriptObject {

       protected HelpTopicConfig() {}
       
       public final native String getTitle() /*-{
           if( this.title ) return this.title;
           return "";
       }-*/;
       
       public final native String getDescription() /*-{
           if( this.description ) return this.description;
           return "";
       }-*/;
       
       public final native String getUrl() /*-{
           if( this.url ) return this.url;
           return "";
       }-*/;
       
       // flag help for the layer menu so you can directly link
       public final native boolean isLayerMenuHelp() /*-{
           if( this.isLayerMenuHelp ) return this.isLayerMenuHelp;
           return false;
       }-*/;
    
}
