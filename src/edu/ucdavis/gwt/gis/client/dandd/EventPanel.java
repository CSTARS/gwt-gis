package edu.ucdavis.gwt.gis.client.dandd;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * A panel that can register events but does not have focus set to it.  Setting focus
 * can give odd displays in some browsers such as selecting sections of text or highlighting it's
 * outline.
 * 
 * @author jrmerz
 */
public class EventPanel extends FocusPanel {
	
	private boolean disabled = false;
	
	/**
	 * Create a new event panel
	 */
	public EventPanel() {
		super();
		addFocusHandler(
			new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					EventPanel.this.setFocus(false);
				}
			}
		);
		
		addAttachHandler(new Handler(){
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if( event.isAttached() ) removeTabs();
			}
		});
		setTabIndex(-1);
		removeTabs();
		setNoSelect();
	}
	
	/**
	 * Set the user-select css property to none;
	 */
	private void setNoSelect() {
		try {
			getElement().getStyle().setProperty("userSelect", "none");
		} catch (Exception e) {}
	}
	
	/**
	 * remove table index;
	 */
	private void removeTabs() {
		Element ele = getElement();
		ele.removeAttribute("tabindex");
		for( int i = 0; i < ele.getChildCount(); i++ ) {
			Element.as(ele.getChild(i)).removeAttribute("tabindex");
		}
	}

	public boolean disabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
