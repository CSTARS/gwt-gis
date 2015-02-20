package edu.ucdavis.gwt.gis.client.extras;

import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class InfoPopup extends BootstrapModalLayout {
	
	private HTML description = new HTML();
	private Anchor link = new Anchor();
	private FlowPanel panel = new FlowPanel();
	private MainMenuFooter footer = new MainMenuFooter();
	
	public InfoPopup() {
		
		link.setText("Home Page");
		link.setTarget("_blank");
		
		panel.getElement().getStyle().setTextAlign(TextAlign.CENTER);

		panel.add(description);
		
		SimplePanel div = new SimplePanel();
		div.add(link);
		panel.add(div);
		
	}
	
	public void alignDescriptionLeft() {
		panel.getElement().getStyle().setTextAlign(TextAlign.LEFT);
	}
	
	public void setDescription(String des){
		description.setHTML(des);
	}
	
	public void setLinkUrl(String url){
		link.setHref(url);
		if( url.length() == 0 ) link.setVisible(false);
	}

	@Override
	public String getTitle() {
		return "About";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}
	
}
