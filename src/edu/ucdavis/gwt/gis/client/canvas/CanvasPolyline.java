package edu.ucdavis.gwt.gis.client.canvas;

import java.util.LinkedList;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JsArrayNumber;

import edu.ucdavis.cstars.client.geometry.Polyline;
import edu.ucdavis.cstars.client.geometry.Ring;

/**
 * A line geometry for the CanvasMap
 * 
 * @author jrmerz
 */
public class CanvasPolyline extends CanvasGeometry {

	private LinkedList<LinkedList<double[]>> lines = new LinkedList<LinkedList<double[]>>();
	private String lineStyle = null;
	
	/**
	 * Create a new polyline from a gwt-esri polyline
	 * 
	 * @param polyline - gwt-esri line to replicate
	 * @param lineStyle - css style for line
	 */
	public CanvasPolyline(Polyline polyline, String lineStyle) {
		this.lineStyle = lineStyle;
		
		for( int i = 0; i < polyline.getPaths().getNumRings(); i++ ) {
			Ring rg = polyline.getPaths().getRing(i);
			LinkedList<double[]> ring = new LinkedList<double[]>();
			for( int j = 0; j < rg.getNumPoints(); j++ ) {
				JsArrayNumber p = rg.getPoint(j);
				ring.add(new double[]{p.get(0), p.get(1)});
			}
			lines.add(ring);
		}
	}
	
	/**
	 * Set the lines style
	 * 
	 * @param lineStyle - css style for the line
	 */
	public CanvasPolyline(String lineStyle) {
		this.lineStyle = lineStyle;
	}
	
	/**
	 * add a point to the line
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 */
	public void addPoint(double x, double y) {
		if( lines.size() == 0) lines.add(new LinkedList<double[]>());
		lines.get(lines.size()-1).add(new double[] {x, y});
	}

	/**
	 * Redraw the geometry on the map
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		Context2d context = canvasMap.getCanvas().getContext2d();
		
		context.setLineWidth(getLineWidth());
		if( lineStyle != null ) context.setStrokeStyle(lineStyle);
		
		for( int i = 0; i < lines.size(); i++ ) {
			drawLine(lines.get(i), context);
		}
		
		return true;
	}
	
	/**
	 * draw the points on the canvas
	 * 
	 * @param line - lines points
	 * @param context - canvas 2d context
	 */
	private void drawLine(LinkedList<double[]> line, Context2d context) {
		double[] point = null;
		if( line.size() > 0 ) {
			context.beginPath();
			point = line.get(0);
			context.moveTo(point[0], point[1]);
		}
		
		for( int i = 1; i < line.size(); i++ ) {
			point = line.get(i);
			context.lineTo(point[0], point[1]);
		}
		
		if( lineStyle != null ) context.stroke();
		
		context.closePath();
	}
	
	
}
