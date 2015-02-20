package edu.ucdavis.gwt.gis.client.dandd;

import java.util.LinkedList;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.LayerPanel;

/**
 * This is the main container for the right side layers main.  It allows events to be
 * fired when containers are dragged and dropped inside the container
 * 
 * @author jrmerz
 */
public class DragAndDropContainer extends Composite {

	private AnchorPanel selected = null;
	private Moveable selectedM = null;
	private SimplePanel selectedMContainer = new SimplePanel();
	
	private FlowPanel panel = new FlowPanel();
	private EventPanel eventPanel = new EventPanel();
	
	// handler to fire when order switches
	public interface OrderChangeHandler {
		public void onOrderChange();
	}
	private OrderChangeHandler changeHandler = null;
	
	/**
	 * Create a new DragAndDropContainer
	 */
	public DragAndDropContainer() {
		eventPanel.add(panel);
		initWidget(eventPanel);
		
		eventPanel.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().mouseUp();
					selectedMContainer.removeFromParent();
				}
			}
		});
		eventPanel.addTouchCancelHandler(new TouchCancelHandler(){
			@Override
			public void onTouchCancel(TouchCancelEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().mouseUp();
					selectedMContainer.removeFromParent();
				}
			}
		});
		
		eventPanel.addMouseUpHandler(new MouseUpHandler(){
			@Override
			public void onMouseUp(MouseUpEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().mouseUp();
					selectedMContainer.removeFromParent();
				}
			}
		});
		eventPanel.addTouchEndHandler(new TouchEndHandler(){
			@Override
			public void onTouchEnd(TouchEndEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().mouseUp();
					selectedMContainer.removeFromParent();
				}
			}
		});
		
		eventPanel.addMouseMoveHandler(new MouseMoveHandler(){
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().move(event.getY());
					onMove();
				}
			}
		});
		eventPanel.addTouchMoveHandler(new TouchMoveHandler(){
			@Override
			public void onTouchMove(TouchMoveEvent event) {
				if( selected != null ) {
					selected.getMoveablePanel().move(event.getTouches().get(0).getClientY());
					onMove();
				}
			}
		});
		
		selectedMContainer.getElement().getStyle().setPosition(Position.RELATIVE);
		selectedMContainer.setHeight("0px");
	}
	
	/**
	 * Get a list of all the widgets currently attached to the container
	 * 
	 * @return LinkedList<Widget>
	 */
	public LinkedList<Widget> getWidgets() {
		LinkedList<Widget> list = new LinkedList<Widget>();
		for( int i = 0; i < panel.getWidgetCount(); i++ ) {
			Widget w = panel.getWidget(i);
			if( w instanceof AnchorPanel ) {
				list.add(((AnchorPanel) w).getMoveablePanel().getWidget());
			}
		}
		return list;
	}
	
	/**
	 * Add a new widget to the container at a certain index
	 * 
	 * @param widget - widget to be added
	 * @param index - insert index
	 */
	public void insert(LayerPanel widget, int index) {
		panel.insert(new AnchorPanel(new Moveable(widget, this)), index);
	}
	
	/**
	 * Add a widget to the container
	 * 
	 * @param widget - widget to be added
	 */
	public void add(LayerPanel widget) {
		panel.add(new AnchorPanel(new Moveable(widget, this)));
	}
	
	public void add(EventPanel widget) {
        panel.add(new AnchorPanel(new Moveable(widget, this)));
    }
	
	/**
	 * Get the index of the specified widget
	 * 
	 * @param widget - widget that is attached to the container
	 * @return int
	 */
	public int getIndex(Widget widget) {
		for( int i = 0; i < panel.getWidgetCount(); i++ ) {
			AnchorPanel m = (AnchorPanel) panel.getWidget(i);
			if( m.getMoveablePanel().getWidget() == widget ) {
				return i;
			}
		}
		return 0;
	}
	
	/**
	 * remove a widget from the dragAnDrop panel
	 * 
	 * @param widget - widget to be removed
	 */
	public void remove(Widget widget) {
		for( int i = 0; i < panel.getWidgetCount(); i++ ) {
			AnchorPanel m = (AnchorPanel) panel.getWidget(i);
			if( m.getMoveablePanel().getWidget() == widget ) {
				panel.remove(i);
				break;
			}
		}
	}
	
	/**
	 * Fired when the widget list is reorderer
	 */
	public void onMove() {
		if( selected == null ) return;
		
		int y = selected.getMoveablePanel().getY();
		int index = panel.getWidgetIndex(selected);
		int baseY = selected.getAbsoluteTop();
		
		if( baseY == y) {
			return;
		} else if( baseY < y ) {
			y += selected.getMoveablePanel().getOffsetHeight();
			for( int i = index+1; i < panel.getWidgetCount(); i++ ) {
				if( y > (panel.getWidget(i).getAbsoluteTop()+(panel.getWidget(i).getOffsetHeight()/2)) ) {
					rotate(i, true);
					break;
				}
			}
		} else {
			for( int i = 1; i < index; i++ ) {
				if( y < (panel.getWidget(i).getAbsoluteTop()+(panel.getWidget(i).getOffsetHeight()/2)) ) {
					rotate(i, false);
					break;
				}
			}
		}
	}
	
	/**
	 * Rotate a specific index.  
	 * 
	 * @param index - index to rotate
	 * @param isGoingDown - are you rotating w/ the widget above or below the given index?
	 */
	private void rotate(int index, boolean isGoingDown) {
		AnchorPanel anchor = (AnchorPanel) panel.getWidget(index);

		selected.setHeight(anchor.getMoveablePanel().getOffsetHeight()+"px");
		anchor.setHeight(selectedM.getOffsetHeight()+"px");
		
		Moveable a = anchor.releaseMoveablePanel();
		selected.setMoveablePanel(a);
		anchor.setNoAddMoveablePanel(selectedM);
		
		selected.setStyleName("");
		anchor.setStyleName("MoveToPanel");
		
		selected.setHeight("auto");
		selected = anchor;
		
		if( changeHandler != null ) changeHandler.onOrderChange();
	}
	
	/**
	 * Set the handler to fire on change events
	 * 
	 * @param handler - event handler to fire
	 */
	public void setOrderChangeHandler(OrderChangeHandler handler) {
		changeHandler = handler;
	}
	
	/**
	 * Set a specific index to a movable state
	 * 
	 * @param m - movable widget wrapper
	 */
	public void setSelected(Moveable m) {
		if( selected != null ) selected.setHeight("auto");
		
		if( m == null ) {
			if( selected != null && selectedM != null ) {
				selected.setMoveablePanel(selectedM);
				selected.setStyleName("");
			}
			selected = null;
			selectedM = null;
			return;
		}
		
		for( int i = 0; i < panel.getWidgetCount(); i++ ) {
			if( panel.getWidget(i).getClass() != AnchorPanel.class ) continue;
			
			if( ((AnchorPanel) panel.getWidget(i)).getMoveablePanel() == m ) {
				selected = (AnchorPanel) panel.getWidget(i);
				selected.setHeight(selected.getMoveablePanel().getOffsetHeight()+"px");
				selected.setStyleName("MoveToPanel");
				selectedM = selected.releaseMoveablePanel();
				selectedMContainer.clear();
				selectedMContainer.add(selectedM);
				panel.insert(selectedMContainer, 0);
				return;
			}
		}
		selected = null;
		selectedM = null;
	}
	
	private class AnchorPanel extends SimplePanel {
		private Moveable moveable = null;
		public AnchorPanel(Moveable m) {
			super();
			moveable = m;
			add(m);
		}
		public Moveable getMoveablePanel() {
			return moveable;
		}
		public Moveable releaseMoveablePanel() {
			moveable.removeFromParent();
			return moveable;
		}
		public void setMoveablePanel(Moveable m){
			add(m);
			moveable = m;
		}
		public void setNoAddMoveablePanel(Moveable m){
			moveable = m;
		}
	}
	
}
