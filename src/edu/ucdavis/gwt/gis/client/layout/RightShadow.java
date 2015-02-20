package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.user.client.ui.HTML;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.control.Position;
import edu.ucdavis.cstars.client.event.MapResizeHandler;
import edu.ucdavis.cstars.client.geometry.Extent;

public class RightShadow extends Control {

	private int offset = 33; 
	
	public RightShadow() {
		initWidget(new HTML("&nbsp;"));
		setStyleName("RightShadow");
		setPosition(offset, 0, Position.TOP_RIGHT);
	}
	
	@Override
	public void init(MapWidget mapWidget) {
		mapWidget.addResizeHandler(new MapResizeHandler() {
			@Override
			public void onMapResize(Extent extent, int width, int height) {
				setHeight((height-offset)+"px");
			}			
		});
		setHeight((mapWidget.getHeight()-offset)+"px");
	}

}
