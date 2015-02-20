package edu.ucdavis.gwt.gis.client.canvas.markers;

import com.google.gwt.canvas.dom.client.Context2d;

import edu.ucdavis.gwt.gis.client.canvas.CanvasGeometry;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;

/**
 * Replicating the SimpleMarker square style
 * 
 * @author jrmerz
 */
public class CanvasSquareMarker extends CanvasGeometry {

	private double x = 0;
	private double y = 0;
	private double size = 0;
	private String lineStyle = null;
	private String fillStyle = null;
	
	/**
	 * Create a new square marker
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param size - width and height
	 * @param lineStyle - css line style
	 * @param fillStyle - css fill style
	 */
	public CanvasSquareMarker(double x, double y, double size, String lineStyle, String fillStyle) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.lineStyle = lineStyle;
		this.fillStyle = fillStyle;
	}
	
	/**
	 * Redraw marker on map
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		Context2d context = canvasMap.getCanvas().getContext2d();
		
		context.beginPath();
		context.setLineWidth(getLineWidth());
		if( fillStyle != null ) context.setFillStyle(fillStyle);
		if( lineStyle != null ) context.setStrokeStyle(lineStyle);
		
		if( fillStyle != null ) context.fillRect(x, y, size, size);
		if( lineStyle != null ) context.strokeRect(x, y, size, size);
		context.closePath();
		
		return true;
	}
	
}
