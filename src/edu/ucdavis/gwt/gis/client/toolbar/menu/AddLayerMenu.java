package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.addlayer.ServiceAdder;
import edu.ucdavis.gwt.gis.client.toolbar.button.AddLayerButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.AddShapefileButton;

public class AddLayerMenu extends ToolbarMenuItem {
	
	private AddLayerButton button = new AddLayerButton();
	private AddShapefileButton shapeButton = null;
	
	public AddLayerMenu() {
		addItem(button);
		
		if( AppManager.INSTANCE.getConfig().getProxy().length() > 0 && AppManager.INSTANCE.getConfig().enableShapefileUpload() ) {
		    shapeButton = new AddShapefileButton();
		    addItem(shapeButton);
		}
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Add";
	}
	
	public ServiceAdder getServiceAdder() {
		return button.getServiceAdder();
	}
	

}
