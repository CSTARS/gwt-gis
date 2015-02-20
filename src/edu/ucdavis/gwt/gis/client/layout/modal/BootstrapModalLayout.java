package edu.ucdavis.gwt.gis.client.layout.modal;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class BootstrapModalLayout {
	
	public abstract String getTitle();
	
	public abstract Widget getBody();
	
	public abstract Widget getFooter();
	
	protected void onShow() {}
	
	protected void onHide() {};
	
	public boolean isVisible() {
		return BootstrapModal.INSTANCE.isVisible();
	}
	
	public void show() {
		BootstrapModal.INSTANCE.show(this);
		onShow();
	}
	
	public void hide() {
		BootstrapModal.INSTANCE.hide();
		onHide();
	}
	
	// this can be overridden to set a widget as a title
	protected void setTitle(String title, SimplePanel panel) {
		panel.clear();
		panel.add(new HTML("<h3>"+title+"</h3>"));
	}
	
}
