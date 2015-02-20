package edu.ucdavis.gwt.gis.client.canvas.markers;

import com.google.gwt.canvas.dom.client.Context2d;

import edu.ucdavis.gwt.gis.client.canvas.CanvasGeometry;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;

/**
 * Replicating the SimpleMarker Circle style
 * 
 * @author jrmerz
 */
public class CanvasCircleMarker extends CanvasGeometry {

	private double x = 0;
	private double y = 0;
	private double r = 0;
	private String lineStyle = null;
	private String fillStyle = null;
	
	/**
	 * Create a new circle marker
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param size - size (diameter)
	 * @param lineStyle - css line style
	 * @param fillStyle - css fill style
	 */
	public CanvasCircleMarker(double x, double y, double size, String lineStyle, String fillStyle) {
		this.x = x;
		this.y = y;
		r = (int) Math.floor( size / 2 );
		this.lineStyle = lineStyle;
		this.fillStyle = fillStyle;
	}
	
	/**
	 * Redraw circle marker
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		Context2d context = canvasMap.getCanvas().getContext2d();
		
		context.setLineWidth(getLineWidth());
		if( fillStyle != null ) context.setFillStyle(fillStyle);
		if( lineStyle != null ) context.setStrokeStyle(lineStyle);
		
		context.beginPath();
		context.arc(x, y, r, 0, 2*Math.PI);
		
		if( fillStyle != null ) context.fill();
		if( lineStyle != null ) context.stroke();
		
		context.closePath();
		
		return true;
	}
	
}
