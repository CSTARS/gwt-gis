package edu.ucdavis.gwt.gis.client.canvas;

import java.util.LinkedList;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JsArrayNumber;

import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.geometry.Ring;

/**
 * A polygon geometry for the CanvasMap
 * 
 * @author jrmerz
 */
public class CanvasPolygon extends CanvasGeometry {


	private LinkedList<LinkedList<double[]>> polygons = new LinkedList<LinkedList<double[]>>();
	private String lineStyle = null;
	private String fillStyle = null;
	
	
	/**
	 * Create a new CanvasPolygon from a gwt-esri polygon.
	 * 
	 * @param polygon - gwt-esri polygon to replicate.
	 * @param lineStyle - css styling for the polygons outline
	 * @param fillStyle - css styling for the polygons fill
	 */
	public CanvasPolygon(Polygon polygon, String lineStyle, String fillStyle) {
		this.lineStyle = lineStyle;
		this.fillStyle = fillStyle;
		
		for( int i = 0; i < polygon.getRings().getNumRings(); i++ ) {
			Ring rg = polygon.getRings().getRing(i);
			LinkedList<double[]> ring = new LinkedList<double[]>();
			for( int j = 0; j < rg.getNumPoints(); j++ ) {
				JsArrayNumber p = rg.getPoint(j);
				ring.add(new double[]{p.get(0), p.get(1)});
			}
			polygons.add(ring);
		}	
	}
	
	/**
	 * Set the polygons style
	 * 
	 * @param lineStyle - css styling for the polygons outline
	 * @param fillStyle - css styling for the polygons fill
	 */
	public CanvasPolygon(String lineStyle, String fillStyle) {
		this.lineStyle = lineStyle;
		this.fillStyle = fillStyle;
	}
	
	/**
	 * Add a point to the polygon.
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 */
	public void addPoint(double x, double y) {
		if( polygons.size() == 0) polygons.add(new LinkedList<double[]>());
		polygons.get(polygons.size()-1).add(new double[] {x, y});
	}

	/**
	 * Redraw the polygon on the map
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		Context2d context = canvasMap.getCanvas().getContext2d();
		
		context.setLineWidth(getLineWidth());
		if( fillStyle != null ) context.setFillStyle(fillStyle);
		if( lineStyle != null ) context.setStrokeStyle(lineStyle);
		
		for( int i = 0; i < polygons.size(); i++ ) {
			drawPolygon(polygons.get(i), context);
		}
		
		return true;
	}

	/**
	 * draw the polygons line rings
	 * 
	 * @param polygon - points to draw
	 * @param context - canvas 2d context
	 */
	private void drawPolygon(LinkedList<double[]> polygon, Context2d context) {
		double[] point = null;
		if( polygon.size() > 0 ) {
			context.beginPath();
			point = polygon.get(0);
			context.moveTo(point[0], point[1]);
		}
		
		for( int i = 1; i < polygon.size(); i++ ) {
			point = polygon.get(i);
			context.lineTo(point[0], point[1]);
		}
		
		if( polygon.size() > 0 ) {
			point = polygon.get(0);
			context.lineTo(point[0], point[1]);
		}
		
		if( fillStyle != null ) context.fill();
		if( lineStyle != null ) context.stroke();
		
		context.closePath();
	}
	
}
