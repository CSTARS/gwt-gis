package edu.ucdavis.gwt.gis.client.layers;

public interface DataLayerHandler {
	
	public void onDataLayerAdd(DataLayer dl);
	
	public void onDataLayerRemove(DataLayer dl);
	
	public void onDataLayerUpdate(DataLayer dl);

}
