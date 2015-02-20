package edu.ucdavis.gwt.gis.client.mapcontroller;

public class MapControllerHost {

	private MapController activeControl = null;
	
	public void setActive(MapController control) {
		if( activeControl == control ) return;
		if( activeControl != null ) activeControl.deactivate();
		activeControl = control;
	}
	
}
