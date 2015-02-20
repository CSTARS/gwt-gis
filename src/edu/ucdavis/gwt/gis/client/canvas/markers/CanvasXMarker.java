package edu.ucdavis.gwt.gis.client.canvas.markers;

import com.google.gwt.canvas.dom.client.Context2d;

import edu.ucdavis.gwt.gis.client.canvas.CanvasGeometry;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;

/**
 * Replicating the SimpleMarker X style
 * 
 * @author jrmerz
 */
public class CanvasXMarker extends CanvasGeometry {

	private double x = 0;
	private double y = 0;
	private double r = 0;
	private String lineStyle = null;
	
	/**
	 * Create a new X marker
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param size - width & height
	 * @param lineStyle - css line style
	 */
	public CanvasXMarker(double x, double y, double size, String lineStyle) {
		this.x = x;
		this.y = y;
		r = (int) Math.floor( size / 2 );
		this.lineStyle = lineStyle;
	}
	
	/**
	 * Redraw the marker
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		Context2d context = canvasMap.getCanvas().getContext2d();
		
		context.setLineWidth(getLineWidth());
		if( lineStyle == null ) return true;
		context.setStrokeStyle(lineStyle);
		
		context.beginPath();
		context.moveTo(x-r, y+r);
		context.lineTo(x+r, y-r);
		context.stroke();
			
		context.moveTo(x+r, y+r);
		context.lineTo(x-r, y-r);
		context.stroke();	
		context.closePath();
		
		return true;
	}
	
}
