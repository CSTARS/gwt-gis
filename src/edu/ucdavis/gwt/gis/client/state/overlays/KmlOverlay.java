package edu.ucdavis.gwt.gis.client.state.overlays;


public class KmlOverlay extends DataLayerOverlay {
	
	protected KmlOverlay() {}
	
	public final native int getNumLayerInfos() /*-{
		if( this.layerInfos ) return this.layerInfos.length;
		return 0;
	}-*/;
	
	public final native String getSymbolKey(int index) /*-{
		if( this.layerInfos ) {
			return this.layerInfos[index].symbol;
		}
		return "";
	}-*/;
	
	public final native String getSymbolName(int index) /*-{
		if( this.layerInfos ) {
			return this.layerInfos[index].name;
		}
		return "";
	}-*/;

}
