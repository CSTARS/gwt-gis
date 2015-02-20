package edu.ucdavis.gwt.gis.client.identify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModal;

public class IdentifyResultFooter extends Composite {

	private static StatusPanelUiBinder uiBinder = GWT.create(StatusPanelUiBinder.class);
	interface StatusPanelUiBinder extends UiBinder<Widget, IdentifyResultFooter> {}

	@UiField Anchor menu;
	@UiField Anchor close;
	@UiField Anchor settings;

	public IdentifyResultFooter() {
		initWidget(uiBinder.createAndBindUi(this));
		
		menu.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				AppManager.INSTANCE.getClient().getToolbar().showMenu();
			}
		});
		
		close.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				BootstrapModal.INSTANCE.hide();
			}
		});
		
		settings.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				IdentifyTool.INSTANCE.show();
			}
		});
	
	}

}
