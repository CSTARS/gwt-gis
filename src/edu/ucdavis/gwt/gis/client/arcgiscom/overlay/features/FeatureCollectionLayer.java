package edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features;

import com.google.gwt.core.client.JavaScriptObject;

public class FeatureCollectionLayer extends JavaScriptObject {

	protected FeatureCollectionLayer() {}
	
	public final native JavaScriptObject getLayerDefinition() /*-{
		if( this.layerDefinition ) return this.layerDefinition;
		return {};
	}-*/;
	
	public final native JavaScriptObject getFeatureSet() /*-{
		if( this.featureSet ) return this.featureSet;
		return {};
	}-*/;
	
	//public final native FeatureSet getFeatureSet() /*-{
	//	if( this.featureSet ) return this.featureSet;
	//	return {};
	//}-*/;
	
}
