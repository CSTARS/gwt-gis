package edu.ucdavis.gwt.gis.client.canvas;

/**
 * Geometry for and text based on an x, y coordinate
 * 
 * @author jrmerz
 */
public class CanvasText extends CanvasGeometry {

	private String text = "";
	private String font = "";
	private String style = "";
	private double x = 0;
	private double y = 0;
	
	/**
	 * Create a new text geometry
	 * 
	 * @param text - text to display
	 * @param font - font to be used
	 * @param style - font style to be used
	 * @param x - x coordinate
	 * @param y - y coordinate
	 */
	public CanvasText(String text, String font, String style, double x, double y) {
		this.text = text;
		this.font = font;
		this.x = x;
		this.y = y;
		this.style = style;
	}
	

	/**
	 * Redraw the text on the map
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		if( style != null ) canvasMap.getCanvas().getContext2d().setFillStyle(style);
		if( font != null ) canvasMap.getCanvas().getContext2d().setFont(font);
		canvasMap.getCanvas().getContext2d().fillText(text, x, y);
		return true;
	}

	
	
}
