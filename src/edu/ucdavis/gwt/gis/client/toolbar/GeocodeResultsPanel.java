package edu.ucdavis.gwt.gis.client.toolbar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class GeocodeResultsPanel extends BootstrapModalLayout {

	private SimplePanel panel = new SimplePanel();
	private VerticalPanel results = new VerticalPanel();
	
	private Widget searchPanel = null;
	
	private Anchor closeBtn = new Anchor("Close");
	
	public GeocodeResultsPanel(Widget searchPanel) {
		this.searchPanel = searchPanel;
		results.setWidth("100%");
		panel.add(results);
		
		closeBtn.setStyleName("btn");
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
	 
	public void add(Widget w) {
		results.add(w);
	}
	
	public void clear() {
		results.clear();
	}
	
	public int getResultCount() {
		return results.getWidgetCount();
	}
	
	public void addResult(Widget result) {
		results.add(result);
	}

	@Override
	public String getTitle() {
		return "Location Search Results";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return closeBtn;
	}
	
}
