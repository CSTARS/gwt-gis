package edu.ucdavis.gwt.gis.client;

import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This is a runtime 'debugger'.  Basically it captures debugging information when the app is 
 * live and running.  This information can be used to help fix bugs only showing in when the
 * app is up and running on a website.
 * 
 * @author jrmerz
 */
public class Debugger {
	
	public static Debugger INSTANCE = new Debugger();
	private LinkedList<GwtGisError> errors = new LinkedList<GwtGisError>();
	//private DebuggerPanel panel = new DebuggerPanel();
	
	private boolean logging = false;
	
	public void setLogging(boolean logging) {
	    this.logging = logging;
	}
	
	public void log(String msg) {
	    if( logging ) _log(msg);
	}
	
	private final native void _log(String msg) /*-{
	    // USE FOR IE 7/8
	    //$("body").append("<div>"+msg+"</div>");
	    console.log("GWT-GIS LOG: "+msg);
	}-*/;
	
	public void log(JavaScriptObject msg) {
        if( logging ) _log(msg);
    }
	
	private final native void _log(JavaScriptObject msg) /*-{
	    console.log("GWT-GIS OBJECT--");
        console.log(msg);
        console.log("--GWT-GIS OBJECT");
    }-*/;
	
	/**
	 * Show the debugger
	 */
	public void show() {
		//panel.show();
		//panel.update();
	}
	
	/**
	 * Catch an exception
	 * 
	 * @param e
	 */
	public void catchException(Exception e) {
		catchException(e.getMessage(), e.getLocalizedMessage(), "", "");
	}
	
	/**
	 * Catch a throwable
	 * 
	 * @param e
	 */
	public void catchException(Throwable e) {
		catchException(e.getMessage(), e.getLocalizedMessage(), "", "");
	}
	
	/**
	 * Catch an exception with custom message
	 * 
	 * @param e
	 * @param className
	 * @param customMessage
	 */
	public void catchException(Exception e, String className, String customMessage) {
		catchException(e.getMessage(), e.getLocalizedMessage(), className, customMessage);
	}
	
	/**
	 * Catch a throwable with custom message
	 * 
	 * @param e
	 * @param className
	 * @param customMessage
	 */
	public void catchException(Throwable e, String className, String customMessage) {
		catchException(e.getMessage(), e.getLocalizedMessage(), className, customMessage);
	}
	
	/**
	 * Catch a custom exception.  This can be used for general debug i/o as well.
	 * 
	 * @param message
	 * @param detailedMessage
	 * @param className
	 * @param customMessage
	 */
	public void catchException(String message, String detailedMessage, String className, String customMessage) {
		errors.add(new GwtGisError(message, detailedMessage, className, customMessage));
		
		//if( panel.isShowing() ) {
		//	panel.update();
		//}
	}
	
	/**
	 * Popup panel for the debugger
	 * 
	 * @author jrmerz
	 */
	private class DebuggerPanel extends PopupPanel {
		
		private FocusPanel dragPanel = new FocusPanel();
		private FlowPanel panel = new FlowPanel();
		private TextBox filterBox = new TextBox();
		
		private int width = 400;
		private int height = 400;
		
		private boolean mouseDown = false;
		private int startX = 0;
		private int startY = 0;
		
		public DebuggerPanel() {
			VerticalPanel vp = new VerticalPanel();
			vp.setSpacing(5);
			
			dragPanel.setSize(width+"px", "30px");
			dragPanel.add(new HTML("<div style='color:#888888;text-align:center'>GwtGis Debugger</div>"));
			initDragHandlers();
			vp.add(dragPanel);
			
			filterBox.addKeyUpHandler(new KeyUpHandler(){
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
						update();
					}
				}
			});
			vp.add(filterBox);
			
			vp.add(new HTML("<div style='color:#888888;text-align:center;font-size:11px'>[Class] | [Message] | [Detailed Message] | [Custom Message]</div>"));
			
			ScrollPanel sp = new ScrollPanel();
			sp.setSize(width+"px", height+"px");
			sp.add(panel);
			vp.add(sp);
			
			HTML close = new HTML("<span style='color:blue; cursor:pointer'>Close</span>");
			close.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			vp.add(close);
			
			setWidget(vp);
			setStyleName("");
			getElement().getStyle().setBackgroundColor("#ffffff");
			getElement().getStyle().setProperty("border", "1px solid red");
			getElement().getStyle().setZIndex(6000);
		}
		
		/**
		 * update the debugger panel with the latest information
		 */
		public void update() {
			panel.clear();
			
			if( errors.size() == 0 ) {
				panel.add(new HTML("No errors have been caught!"));
				return;
			}
			
			String filter = filterBox.getText().toLowerCase();
			for( GwtGisError error: errors ) {
				if( isFilterMatch(filter, error) ) {
					addError(error);
				}
			}
		}
		
		/**
		 * Does the error match a given filter
		 * 
		 * @param filter
		 * @param error
		 * @return
		 */
		private boolean isFilterMatch(String filter, GwtGisError error) {
			if( filter.length() == 0 ) return true;
			
			if( error.getClassName().matches(".*"+filter+".*") ) return true;
			if( error.getMessage().matches(".*"+filter+".*") ) return true;
			if( error.getDetailedMessage().matches(".*"+filter+".*") ) return true;
			if( error.getCustomMessage().matches(".*"+filter+".*") ) return true;
			
			return false;
		}
		
		/**
		 * Add a new error the error stack
		 * 
		 * @param error
		 */
		private void addError(GwtGisError error) {
			panel.add(new HTML("<div style='font-size:11px;padding:10px 0; border-bottom:1px solid red'>" +
					error.getClassName() + " <b>|</b> " +
					error.getMessage() + " <b>|</b> " +
					error.getDetailedMessage() + " <b>|</b> " +
					error.getCustomMessage() +"</div>"
			));
		}
		
		/**
		 * setup the event handlers for dragging the popup around the window
		 */
		public void initDragHandlers() {
			dragPanel.getElement().getStyle().setProperty("border", "1px solid #cccccc");
			dragPanel.getElement().getStyle().setCursor(Cursor.POINTER);
			dragPanel.addMouseDownHandler(new MouseDownHandler(){
				@Override
				public void onMouseDown(MouseDownEvent event) {
					mouseDown = true;
					startX = event.getX();
					startY = event.getY();
				}
			});
			dragPanel.addMouseUpHandler(new MouseUpHandler(){
				@Override
				public void onMouseUp(MouseUpEvent event) {
					mouseDown = false;
				}
			});
			dragPanel.addMouseOutHandler(new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					mouseDown = false;
				}
			});
			dragPanel.addMouseMoveHandler(new MouseMoveHandler(){
				@Override
				public void onMouseMove(MouseMoveEvent event) {
					if( mouseDown ) {
						int x = event.getClientX() + (startX - event.getClientX());
						int y = event.getClientY() + (startY - event.getClientY());
						
						DebuggerPanel dp = DebuggerPanel.this;
						dp.setPopupPosition(
								event.getClientX()-x-6, 
								event.getClientY()-y-6
						);
					}
				}
			});
		}
		
		
	}
	
	/**
	 * A common struct for all errors
	 * 
	 * @author jrmerz
	 */
	private class GwtGisError {
		private String message = "";
		private String detailedMessage = "";
		private String className = "";
		private String customMessage = "";
		
		/**
		 * Create the new GwtGisError using common strings for messages.
		 * 
		 * @param message
		 * @param detailedMessage
		 * @param className
		 * @param customMessage
		 */
		public GwtGisError(String message, String detailedMessage, String className, String customMessage) {
			if( message == null ) message = "";
			if( detailedMessage == null ) detailedMessage = "";
			if( className == null ) className = "";
			if( customMessage == null ) customMessage = "";
			
			this.message = message.toLowerCase();
			this.detailedMessage = detailedMessage.toLowerCase();
			this.className = className.toLowerCase();
			this.customMessage = customMessage.toLowerCase();
		}
		
		/**
		 * Get the general message
		 * 
		 * @return String
		 */
		public String getMessage() {
			return message;
		}
		
		/**
		 * Get the detailed message
		 * 
		 * @return String
		 */
		public String getDetailedMessage() {
			return detailedMessage;
		}
		
		/**
		 * Get the class which the message was generated in
		 * 
		 * @return String
		 */
		public String getClassName() {
			return className;
		}
		
		/**
		 * get any custom developer message passed along
		 * 
		 * @return String
		 */
		public String getCustomMessage() {
			return customMessage;
		}
		
	}
	
}
