package edu.ucdavis.gwt.gis.client.canvas;

import java.util.LinkedList;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.Window;

/**
 * Wrapper to canvas the allows it to act more like a map accepting geometries
 * and drawing as needed.
 * 
 * @author jrmerz
 */
public class CanvasMap {

	private Canvas canvas = null;
	
	private LinkedList<CanvasGeometry> geometries = new LinkedList<CanvasGeometry>();
	
	private int height = 0;
	private int width = 0;
	
	// event handler for when canvas is done drawing
	public interface CanvasMapLoadHandler {
		public void onLoad(CanvasMap map);
	}
	private CanvasMapLoadHandler handler = null;
	
	/**
	 * Create a new CanvasMap with given height and width
	 * 
	 * @param width
	 * @param height
	 */
	public CanvasMap(int width, int height) {
		if( !Canvas.isSupported() ) {
			Window.alert("Please use a modern browser to preform this operation");
			return;
		}
		
		canvas = Canvas.createIfSupported();
		canvas.setSize(width+"px", height+"px");
		canvas.setCoordinateSpaceHeight(height);
		canvas.setCoordinateSpaceWidth(width);
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Get the canvas widget associated with this CanvasMap
	 * 
	 * @return Canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Add a geometry to the map
	 * 
	 * @param geometry - geometry to add
	 */
	public void addGeometry(CanvasGeometry geometry) {
		geometries.add(geometry);
	}
	
	/**
	 * get a base64 encoded png file
	 * 
	 * @return String
	 */
	public String getImageData() {
		return canvas.toDataUrl("image/png");
	}
	
	/**
	 * Redraw the entire map.  Fire anytime there is an update to the maps geometries.
	 */
	public void redraw() {
		// make white
		canvas.getContext2d().setFillStyle("#ffffff");
		canvas.getContext2d().fillRect(0, 0, width, height);
		
		boolean success = true;
		for( CanvasGeometry geo: geometries ) {
			if( !geo.redraw(this) ) success = false; 
		}
		
		if( success && handler != null ) {
			handler.onLoad(this);
			handler = null;
		}
	}
	
	/**
	 * Set a load handler to fire each time the CanvasMap successfully redraws.  For the most
	 * part, an unsuccessful redraw happens when redraw is fired w/ an image as a geometry and 
	 * that image is not loaded in the browser.  NOTE:  the canvas map will automatically 
	 * redraw when the image IS loaded, firing the load handler.
	 * 
	 * @param handler
	 */
	public void setLoadHandler(CanvasMapLoadHandler handler){
		this.handler = handler;
	}
	
}
