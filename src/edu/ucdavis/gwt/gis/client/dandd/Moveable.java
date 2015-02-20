package edu.ucdavis.gwt.gis.client.dandd;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.LayerPanel;

/**
 * A movable container widgets are wrapped in while being dragged inside
 * the DragAndDropContainer
 * 
 * @author jrmerz
 */
public class Moveable extends Composite {
	
	// is it a click or a hold?
	// this delay should help us tell user intent
	// Touch is longer cause of scrolling
	private static final int TOUCH_DELAY = 50;
	private static final int MOUSE_DELAY = 50;
	
	private Widget widget = null;
	private EventPanel eventPanel = new EventPanel();
	private DragAndDropContainer ddContainer = null;
	private Widget contentPanel = null;
	
	//private boolean delayMouseDown = false;
	private int offsetY = 0;
	private int startY = 0;
	
	private Timer delayTimer = null;
	
	/**
	 * Create a new moveable panel
	 * 
	 * @param w - widget to be wrapped
	 * @param ddc - parent container
	 */
	public Moveable(EventPanel w, DragAndDropContainer ddc) {
	    contentPanel = w;
        eventPanel.add(w);
        eventPanel.setStyleName("Moveable");
        initWidget(eventPanel);
        setStyleName("Moveable");
        
        widget = w;
        ddContainer = ddc;
        
        init();
	}
	
	public Moveable(LayerPanel w, DragAndDropContainer ddc) {
		contentPanel = w;
		eventPanel = w.getDragIcon();
		
		SimplePanel sp = new SimplePanel();
		sp.add(w);
		initWidget(sp);
		setStyleName("Moveable");
		
		widget = w;
		ddContainer = ddc;
		
		init();
	}
	
	private void init() {
	       eventPanel.addMouseDownHandler(new MouseDownHandler(){
	            @Override
	            public void onMouseDown(MouseDownEvent event) {
	                //if( contentPanel.disabled() ) return;
	                if( delayTimer != null ) delayTimer.cancel();

	                offsetY = event.getY();
	                startY = event.getRelativeY(ddContainer.getElement());
	                delayTimer = new Timer() {
	                    @Override
	                    public void run() {
	                        mouseDown();
	                    }
	                };
	                delayTimer.schedule(MOUSE_DELAY);
	            }
	        });
	        
	        eventPanel.addTouchStartHandler(new TouchStartHandler(){
	            @Override
	            public void onTouchStart(TouchStartEvent event) {
	                //if( contentPanel.disabled() ) return;
	                
	                if( delayTimer != null ) delayTimer.cancel();
	                
	                offsetY = event.getTouches().get(0).getRelativeY(event.getRelativeElement());
	                startY = event.getTouches().get(0).getRelativeY(ddContainer.getElement());
	                delayTimer = new Timer() {
	                    @Override
	                    public void run() {
	                        mouseDown();
	                    }
	                };
	                delayTimer.schedule(TOUCH_DELAY);
	            }
	        });
	        
	        eventPanel.addTouchMoveHandler(new TouchMoveHandler(){
	            @Override
	            public void onTouchMove(TouchMoveEvent event) {
	                // if the hand moves prior to the 600ms, it's a scroll action, ignore the drag and drop
	                int oY = event.getTouches().get(0).getRelativeY(event.getRelativeElement());
	                int sY = event.getTouches().get(0).getRelativeY(ddContainer.getElement());
	                
	                if( Math.abs(oY - offsetY) > 10 || Math.abs(sY - startY) > 10 ) {
	                    if( delayTimer != null ) delayTimer.cancel();
	                }
	            }
	        });
	        
	        eventPanel.addMouseUpHandler(new MouseUpHandler(){
	            @Override
	            public void onMouseUp(MouseUpEvent event) {
	                //if( contentPanel.disabled() ) return;
	                
	                if( delayTimer != null ) delayTimer.cancel();
	            }
	        });
	        
	        eventPanel.addTouchEndHandler(new TouchEndHandler(){
	            @Override
	            public void onTouchEnd(TouchEndEvent event) {
	                //if( contentPanel.disabled() ) return;
	                
	                if( delayTimer != null ) delayTimer.cancel();
	            }
	        });
	        
	        getElement().getStyle().setOverflow(Overflow.VISIBLE);
	        getElement().getStyle().setPosition(Position.RELATIVE);
	}
	
	/**
	 * get the top of the moveable
	 * 
	 * @return int
	 */
	public int getY() {
		return getAbsoluteTop();
	}
	
	/**
	 * get the wrapped widget
	 */
	public Widget getWidget() {
		return widget;
	}
	
	/**
	 * fire mouse down events
	 */
	private void mouseDown() {
		delayTimer = null;
		
		setWidth(ddContainer.getOffsetWidth()+"px");
		getElement().getStyle().setTop(startY-offsetY, Unit.PX);
		
		ddContainer.setSelected(Moveable.this);
		addStyleDependentName("selected");
	}
	
	private native void debug(String msg) /*-{
		console.log(msg);
	}-*/;
	
	/**
	 * Fire mouse up events
	 */
	public void mouseUp() {
		delayTimer = null;

		setWidth("auto");
		removeStyleDependentName("selected");
		getElement().getStyle().setTop(0, Unit.PX);
		
		ddContainer.setSelected(null);
	}
	
	/**
	 * move the panel
	 * 
	 * @param pos - vertical position to move to
	 */
	public void move(int pos) {
		//int pos = y - getAbsoluteTop();
		getElement().getStyle().setTop(pos-offsetY, Unit.PX);
	}
	
}
