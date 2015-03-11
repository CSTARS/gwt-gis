package edu.ucdavis.gwt.gis.client.toolbar.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.addlayer.ServiceAdder;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class AddLayerButton extends ToolbarItem {
	
	private ServiceAdder serviceAdder;
	
	public AddLayerButton(){
		serviceAdder = new ServiceAdder(AppManager.INSTANCE.getClient());
	}

	@Override
	public String getIcon() {
		return "<i class='fa fa-plus'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {
		serviceAdder.setToolbar(toolbar);
	}

	public void onClick() {
		serviceAdder.run(); 
	}

	@Override
	public String getText() {
		return "Add Layer";
	}
	
	public ServiceAdder getServiceAdder() {
		return serviceAdder;
	}

}
