package edu.ucdavis.gwt.gis.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

/**
 * Test what capabilities the browser has and alert if important functions are missing
 *
 * TODO: check for postMessage support
 * TODO: Switch to using modernizer
 * 
 * @author jrmerz
 */
public class CompatibilityTester {
	
	private static boolean localStorageSupported = Storage.isLocalStorageSupported();
	private static boolean canvasSupported = Canvas.isSupported();
	private static boolean geolocationSupported = Canvas.isSupported();
	private static boolean corsSupported = isCorsSupported();
	
	private static String firefoxUrl = "http://www.mozilla.org/en-US/firefox/new/";
	private static String chromeUrl = "http://www.google.com/chrome";
	private static String safariUrl = "http://www.apple.com/safari/";
	private static boolean showDetails = true;
	
	/**
	 * Test and show and lacking HTML5 functionality
	 */
	public static void test() {
	    Debugger.INSTANCE.log("CompatibilityTester: Running tests...");
		if( !localStorageSupported || !canvasSupported || !corsSupported ) {
		    Debugger.INSTANCE.log("CompatibilityTester: Tests failed");
			TesterPopup popup = new TesterPopup();
			popup.show();
		} else {
		    Debugger.INSTANCE.log("CompatibilityTester: All tests passed");
		}
	}
	
	public static void showDetails(boolean show) {
		showDetails = show;
	}
	
	
	/**
	 * Popup window for CompatibilityTester
	 * 
	 * @author jrmerz
	 */
	private static class TesterPopup extends AbsolutePanel {
		private FlowPanel panel = new FlowPanel();
		
		public TesterPopup() {
		    Debugger.INSTANCE.log("CompatibilityTester: Creating alert popup");
		    
			add(panel);
			panel.setWidth("800px");
			
			panel.setStyleName("GwtGisPopup");
			setStyleName("GwtGisPopup-glasspanel-solid");
			
			getElement().getStyle().setZIndex(8010);
			
			panel.getElement().getStyle().setColor("#666666");
			panel.add(new HTML("<div style='color:red;text-align:center;font-size:24px; padding-bottom:10px'>- Please Read -</div>"));
			
			if( showDetails ) {
				panel.add(new HTML("<div style='color:#333333;text-align:center;font-size:20px; padding-bottom:10px'>Browser Support</div>"));
				panel.add(new HTML("<div>Your browser does not have support for the following <a href='http://html5test.com/index.html' target='_blank'>HTML 5</a> feature(s).</div>"));
				
				String list = "<ul>";
				if( !canvasSupported ) list += "<li><a href='http://www.w3schools.com/html5/tag_canvas.asp' target='_blank'>Canvas</a> Support</li>";
				if( !localStorageSupported ) list += "<li><a href='http://www.w3schools.com/html5/html5_webstorage.asp' target='_blank'>Local Storage</a> Support</li>";
				if( !geolocationSupported ) list += "<li><a href='http://www.w3schools.com/html5/html5_geolocation.asp' target='_blank'>Geolocation</a> Support</li>";
				if( !corsSupported ) list += "<li><a href='http://enable-cors.org/' target='_blank'>CORS</a> Support</li>";
				list += "</ul>";
				panel.add(new HTML(list));
			} else {
				panel.add(new HTML("<div style='color:#333333;text-align:center;font-size:18px; padding-bottom:10px'>Your browser is not fully supported.</div>"));
			}
			
			panel.add(new HTML("<div style='padding-top:15px;'>Upgrading to the latest version of one of the following browsers is recommended to run this app." +
					"  If you are using Internet Explorer, you can install <a href='https://developers.google.com/chrome/chrome-frame/'>Chrome Frame</a> to run this app." +
					"  This site is fully supported in Internet Explorer 9 as well.</div>"));
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.getElement().getStyle().setPadding(15, Unit.PX);
			hp.setWidth("100%");
			hp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			
			Image firefox = new Image(GadgetResources.INSTANCE.firefox());
			firefox.getElement().getStyle().setCursor(Cursor.POINTER);
			firefox.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					Window.Location.assign(firefoxUrl);
				}
			});
			hp.add(firefox);
			
			Image chrome = new Image(GadgetResources.INSTANCE.chrome());
			chrome.getElement().getStyle().setCursor(Cursor.POINTER);
			chrome.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					Window.Location.assign(chromeUrl);
				}
			});
			hp.add(chrome);
			
			Image safari = new Image(GadgetResources.INSTANCE.safari());
			safari.getElement().getStyle().setCursor(Cursor.POINTER);
			safari.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					Window.Location.assign(safariUrl);
				}
			});
			hp.add(safari);
			
			panel.add(hp);
			
			HTML close = new HTML("<div style='color:#2278da;cursor:pointer; text-align:center; padding-bottom:15px;font-size:36px;'>Proceed to Map</div>");
			close.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					RootPanel.get().remove(TesterPopup.this);
				}
			});
			panel.add(close);
			
			Debugger.INSTANCE.log("CompatibilityTester: Done creating popup");
		}
		
		public void show() {
		    Debugger.INSTANCE.log("CompatibilityTester: Showing popup...");
			GisClient client = AppManager.INSTANCE.getClient();
			
			int w = Window.getClientWidth();
			int h = client.getHeight();
			
			setSize(w+"px", h+"px");
			getElement().getStyle().setTop(client.getTop(), Unit.PX);
			getElement().getStyle().setLeft(0, Unit.PX);
			setWidgetPosition(panel, 
					(int) Math.floor((double) w / (double) 2) - 402,
					(int) Math.floor((double) h / (double) 2) - 200);
			
			if( !isAttached() ) RootPanel.get().add(this);
			getElement().getStyle().setPosition(Position.ABSOLUTE);
			Debugger.INSTANCE.log("CompatibilityTester: Popup attached");
		}	
	}
	
	public static native boolean isCorsSupported() /*-{
		if ('withCredentials' in new XMLHttpRequest()) {
		    //document.write("CORS supported (XHR)");
		    return true;
		} else if(typeof XDomainRequest !== "undefined"){
			//document.write("CORS supported (XDR)");
			return true;
		}
		//document.write("No CORS Support!");
		return false;
	}-*/;
	

}
