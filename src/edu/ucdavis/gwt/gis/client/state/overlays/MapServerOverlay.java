package edu.ucdavis.gwt.gis.client.state.overlays;


public class MapServerOverlay extends DataLayerOverlay {
	
	protected MapServerOverlay() {}
	
	public final native boolean isTiled() /*-{
		if( this.isTiled ) return this.isTiled;
		return false;
	}-*/;
	
	public final native boolean hasTiledOption() /*-{
		if( this.hasTiledOption ) return this.hasTileOption;
		return false;
	}-*/;
	
	public final native boolean legendIsGradient() /*-{
		if( this.legendIsGradient ) return this.legendIsGradient;
		return false;
	}-*/;

}
