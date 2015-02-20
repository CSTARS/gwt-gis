package edu.ucdavis.gwt.gis.client.layout.modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BootstrapModal extends Composite {	
	
	private static BootstrapModalUiBinder uiBinder = GWT.create(BootstrapModalUiBinder.class);
	interface BootstrapModalUiBinder extends UiBinder<Widget, BootstrapModal> {}
	
	public static final BootstrapModal INSTANCE = new BootstrapModal();
	
	@UiField SimplePanel title;
	@UiField FlowPanel body;
	@UiField FlowPanel footer;
	@UiField Anchor close;
	
	@UiField Element bodyPanel;
	@UiField Element footerPanel;
	@UiField Element headerPanel;
	
	private boolean visible = false;
	private boolean cancelTransition = false;

	private BootstrapModal() {
		initWidget(uiBinder.createAndBindUi(this));
		getElement().setId("gwt-gis-modal");
		RootPanel.get().add(this);
		_initJs();
		
		close.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		Window.addResizeHandler(new ResizeHandler(){
			@Override
			public void onResize(ResizeEvent event) {
				if( visible ) resize(true);
			}
		});
	}
	
	private native void _initJs() /*-{
		$wnd.$('#gwt-gis-modal').modal({
			backdrop : true,
			show     : false
		});
	}-*/;
	
	private native boolean _isVisible() /*-{
		if( $wnd.$('#gwt-gis-modal').css('display') == 'block' ) return true;
		return false;
	}-*/;

	private native void _show() /*-{
		$wnd.$('#gwt-gis-modal').modal('show');
	}-*/;
	
	private native void _hide() /*-{
		$wnd.$('#gwt-gis-modal').modal('hide');
	}-*/;
	
	private void transition(final BootstrapModalLayout layout) {
		cancelTransition = false;
		addStyleName("transition");
		
		new Timer() {
			@Override
			public void run() {
				if( !cancelTransition ) show(layout, true);
			}
		}.schedule(250);
		
		new Timer() {
			@Override
			public void run() {
				if( !cancelTransition ) removeStyleName("transition");
			}
		}.schedule(600);
	};
	
	public boolean isVisible() {
		return _isVisible();
	}
	
	public void show(BootstrapModalLayout layout) {
		show(layout, false);
	}
	
	public void show(BootstrapModalLayout layout, boolean noTransition) {
		if( visible && !noTransition ) {
			transition(layout);
			return;
		}
		
		layout.setTitle(layout.getTitle(), title);
		
		body.clear();
		body.add(layout.getBody());
		
		footer.clear();
		footer.add(layout.getFooter());
		
		_show();
		visible = true;
		
		new Timer() {
			@Override
			public void run() {
				// HACK!!! make sure opacity is 1
				getElement().getStyle().setOpacity(1);
				
				resize(true);
			}
		}.schedule(100);
	}
	
	public void hide() {
		// make sure the transition is cleared
		cancelTransition = true;
		removeStyleName("transition");
		
		getElement().getStyle().setProperty("opacity", "inherit");
		
		_hide();
		visible = false;
	}
	
	// make sure the max-height of modal doesn't let it flow off screen
	private void resize(boolean doubleCheck) {
		
		int maxHeight = 400;
		
		// 30 is for the body padding, 10 is for a little extra
		int cHeight = headerPanel.getClientHeight() + maxHeight + footerPanel.getClientHeight() + 40;
		
		if( (cHeight + getAbsoluteTop()) > Window.getClientHeight() ) {
			maxHeight -= (cHeight + getAbsoluteTop()) - Window.getClientHeight();
			if( maxHeight < 50 ) maxHeight = 50;
		}
		
		bodyPanel.getStyle().setProperty("maxHeight", maxHeight+"px");
		
		// after animation, doubleCheck
		if( doubleCheck ) {
			new Timer() {
				@Override
				public void run() {
					resize(false);
				}
			}.schedule(500);
		}
	}
	
}
