package edu.ucdavis.gwt.gis.client.draw;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.ucdavis.cstars.client.dojo.Color;

// TODO: make this work
public class ColorTable extends Composite {
	
	private HorizontalPanel panel = new HorizontalPanel();
	private Canvas canvas = Canvas.createIfSupported();
	
	private Color baseColor = Color.create(0,255,0,1);
	private Color currentColor = Color.create(0,0,0,1);
	private Color clearColor = Color.create(0,0,0,0);
	
	private int width = 200;
	private int height = 200;
	
	private int currentX = -1;
	private int currentY = -1;
	
	private boolean mouseDown = false;
	
	public interface UpdateHandler {
		public void onColorChange(Color color);
	}
	
	private UpdateHandler handler = null;
	
	public ColorTable() {
		init();
	}
	
	public ColorTable(int width, int height) {
		this.width = width;
		this.height = height;
		init();
	}
	
	private void init() {
		panel.add(canvas);
	
		canvas.setSize(width+"px", height+"px");
	
		addHandlers();
		
		draw();

		initWidget(panel);
	}
	
	public void setColor(Color color){
		currentColor.setColor(color.getRed(), color.getGreen(), color.getBlue(), 1);
		for( int i = 0; i < width; i++ ){
			for( int j = 0; j < height; i++ ){
				ImageData rgb = canvas.getContext2d().getImageData(i, j, 1, 1);
				if( rgb.getRedAt(i, j) == color.getRed() && rgb.getGreenAt(i, j) == color.getGreen() && rgb.getBlueAt(i, j) == color.getBlue() ){
					drawCrossHairs(i, j);
					break;
				}
			}
		}
	}
	
	private void draw(){
		Context2d context = canvas.getContext2d();
		context.setFillStyle(Color.create(255,255,255,1).toString());
		context.fillRect(0, 0, width, height);
		
		
		CanvasGradient lingrad = context.createLinearGradient(0,0,0,height);
		lingrad.addColorStop(0, baseColor.toString());
		lingrad.addColorStop(1, clearColor.toString());  
		context.setFillStyle(lingrad);
		context.fillRect(0, 0, width, height);
		
		lingrad = context.createLinearGradient(0,0,width,0);
		lingrad.addColorStop(0, "black");
		lingrad.addColorStop(1, clearColor.toString());  
		context.setFillStyle(lingrad);
		context.fillRect(0, 0, width, height);	
	}
	
	public void setBaseColor(Color color){
		baseColor = color;
		if( currentX == -1) {
			canvas.getContext2d().clearRect(0, 0, width, height);
			draw();
		} else {
			drawCrossHairs(currentX, currentY);
		}
		if (handler != null ) handler.onColorChange(getColor());
	}
	
	private void drawCrossHairs(int x, int y){
		currentX = x;
		currentY = y;
		
		Context2d context = canvas.getContext2d();
		context.clearRect(0, 0, width, height);
		
		draw();
		
		// get current color before we draw over it :)
		ImageData color = context.getImageData(currentX, currentY, 1, 1);
		currentColor = Color.create(color.getRedAt(0, 0), color.getGreenAt(0, 0), color.getBlueAt(0, 0), 1);

		CanvasGradient lingrad = context.createLinearGradient(0,y,width,y);
		lingrad.addColorStop(0, Color.create(255,255,255,1).toString());  
		lingrad.addColorStop(1, "black");
		context.setStrokeStyle(lingrad);
		context.beginPath();
		context.moveTo(0, y);
		context.lineTo(width, y);
		context.stroke();
		
		context.setStrokeStyle(Color.create(255-x,255-x,255-x,1).toString());
		context.beginPath();
		context.moveTo(x, 0);
		context.lineTo(x, height);
		context.stroke();
	}
	
	private Color getColor(){
		return currentColor;
	}
	
	private void addHandlers(){
		canvas.addMouseDownHandler(new MouseDownHandler(){
			@Override
			public void onMouseDown(MouseDownEvent event) {
				mouseDown = true;
				drawCrossHairs(event.getX(), event.getY());
				if( handler != null ) handler.onColorChange(getColor());
			}
		});
		canvas.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				mouseDown = false;
			}
		});
		canvas.addMouseMoveHandler(new MouseMoveHandler(){
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if( mouseDown ) {
					drawCrossHairs(event.getX(), event.getY());
					if( handler != null ) handler.onColorChange(getColor());
				}
			}
		});
		canvas.addMouseUpHandler(new MouseUpHandler(){
			@Override
			public void onMouseUp(MouseUpEvent event) {
				mouseDown = false;
			}
		});
	}

	public void setUpdateHandler(UpdateHandler handler){
		this.handler = handler;
	}
}
