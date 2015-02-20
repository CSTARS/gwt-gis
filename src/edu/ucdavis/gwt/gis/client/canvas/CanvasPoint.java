package edu.ucdavis.gwt.gis.client.canvas;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Use this class to represent points on a canvas map.  Points are represented by
 * image icons and draw when the image is loaded.
 * 
 * @author jrmerz
 */
public class CanvasPoint extends CanvasGeometry {

	private Image icon = null;
	
	private boolean waitingToDraw = false;
	private boolean loaded = false;
	private CanvasMap canvasMap = null;
	private double x = 0;
	private double y = 0;
	private double h = -1;
	private double w = -1;
	
	/**
	 * Create a new point at the x, y coordinate with the given image url.
	 * 
	 * @param x
	 * @param y
	 * @param url
	 */
	public CanvasPoint(double x, double y, String url) {
		this.x = x;
		this.y = y;
		icon = new Image(url);
		icon.setVisible(false);
		icon.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				loaded = true;
				if( waitingToDraw ) canvasMap.redraw();
				RootPanel.get().remove(icon);
			}
		});
		RootPanel.get().add(icon);
	}
	
	/**
	 * Create a new point at the x, y coordinate with the given image url.  W and h
	 * represent the height and width the icon should be when drawn.
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param w - icon width
	 * @param h - icon height
	 * @param url - icon url
	 */
	public CanvasPoint(double x, double y, double w, double h, String url) {
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = w;
		icon = new Image(url);
		icon.setVisible(false);
		icon.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				loaded = true;
				if( waitingToDraw ) canvasMap.redraw();
				RootPanel.get().remove(icon);
			}
		});
		RootPanel.get().add(icon);
	}
	
	/**
	 * set the icon size when drawn on the canvas
	 * 
	 * @param width
	 * @param height
	 */
	public void setImageSize(int width, int height) {
		w = width;
		h = height;
	}
	
	/**
	 * Redraw the icon on the map if img is loaded.  Otherwise set to waiting state.
	 */
	@Override
	public boolean redraw(CanvasMap canvasMap) {
		if( loaded ) {
			if( w != -1 && h != -1 ) {
				canvasMap.getCanvas().getContext2d().drawImage(ImageElement.as(icon.getElement()), x, y, w, h);
			} else {
				canvasMap.getCanvas().getContext2d().drawImage(ImageElement.as(icon.getElement()), x, y);
			}
			return true;
		}
		
		waitingToDraw = true;
		this.canvasMap = canvasMap;
		return false;
	}
	
}
