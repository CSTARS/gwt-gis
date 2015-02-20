package edu.ucdavis.gwt.gis.client.canvas;

/**
 * Parent class for all canvas map geometries
 * 
 * @author jrmerz
 */
public abstract class CanvasGeometry {
	
	// default line width
	private double lineWidth = 1;

	/**
	 * What to do when ask to draw self.  Returns true if successful.  Otherwise false.
	 * Mostly images will return false when loading and let the map know it will need to
	 * redraw again when they are loaded
	 * 
	 * @param canvasMap - canvas
	 * @return boolean
	 */
	public abstract boolean redraw(CanvasMap canvasMap);
	
	/**
	 * Set the geometries line width
	 * 
	 * @param width
	 */
	public void setLineWidth(double width) {
		lineWidth = width;
	}
	
	/**
	 * Get the geometries line width
	 * 
	 * @return
	 */
	public double getLineWidth() {
		return lineWidth;
	}
	
}
