package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class AddShapefileButton extends ToolbarItem {
	
	private ShapefileUploader uploader;
	
	public AddShapefileButton(){
		uploader = new ShapefileUploader();
	}

	@Override
	public String getIcon() {
		return "<i class='icon-upload-alt'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		uploader.show();
	}

	@Override
	public String getText() {
		return "Upload Shapefile";
	}
	
}
