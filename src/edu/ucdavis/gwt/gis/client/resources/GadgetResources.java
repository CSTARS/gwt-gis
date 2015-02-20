package edu.ucdavis.gwt.gis.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface GadgetResources extends ClientBundle {
	public static final GadgetResources INSTANCE = GWT.create(GadgetResources.class);

	@Source("images/point.png")
	public ImageResource point();
	
	@Source("images/line.png")
	public ImageResource line();
	
	@Source("images/polygon.png")
	public ImageResource polygon();
	
	@Source("images/querymap.png")
	public ImageResource querymap();
	
	@Source("images/querymap_inactive.png")
	public ImageResource querymapInactive();
	
	@Source("images/edit.png")
	public ImageResource edit();
	
	@Source("images/zoom_to.png")
	public ImageResource zoomTo();
	
	@Source("images/trash.png")
	public ImageResource trash();
	
	@Source("images/link.png")
	public ImageResource link();
	
	@Source("images/draw.png")
	public ImageResource draw();
	
	@Source("images/cursor.png")
	public ImageResource cursor();
	
	@Source("images/firefox.png")
	public ImageResource firefox();
	
	@Source("images/chrome.png")
	public ImageResource chrome();
	
	@Source("images/safari.png")
	public ImageResource safari();
	
	@Source("images/arrowDown.png")
	public ImageResource arrowDown();
	
	@Source("images/arrowRight.png")
	public ImageResource arrowRight();
	
	@Source("images/server.png")
	public ImageResource server();
	
	@Source("images/bullet_blue.png")
	public ImageResource bullet_blue();
	
	@Source("images/bullet_grey.png")
	public ImageResource bullet_grey();
	
	@Source("images/bullet_red.png")
	public ImageResource bullet_red();
	
	@Source("images/email.png")
	public ImageResource email();

	@Source("images/save.png")
	public ImageResource save();
	
	@Source("images/load.png")
	public ImageResource load();
	
	@Source("images/map.png")
	public ImageResource map();
	
	@Source("images/search.png")
	public ImageResource search();
	
	@Source("images/dir_arrow.png")
	public ImageResource dir_arrow();
	
	@Source("images/advancedsettings.png")
	public ImageResource advancedsettings();
	
	@Source("images/help.png")
	public ImageResource help();
	
	@Source("images/help_grey.png")
	public ImageResource help_grey();
	
	@Source("images/add.png")
	public ImageResource add();
	
	@Source("images/remove.png")
	public ImageResource remove();
	
	@Source("images/add_grey.png")
	public ImageResource add_grey();
	
	@Source("images/information.png")
	public ImageResource infoSmall();
	
	@Source("images/information_grey.png")
	public ImageResource infoSmall_grey();
	
	@Source("images/information_blue.png")
	public ImageResource infoSmall_blue();
	
	@Source("images/opacityIcon.png")
	public ImageResource opacityIcon();
	
	@Source("images/layers.png")
	public ImageResource layers();
	
	@Source("images/folder.png")
	public ImageResource folder();
	
	@Source("images/opacity.png")
	public ImageResource opacity();
	
	@Source("images/opacity_blue.png")
	public ImageResource opacity_blue();
	
	@Source("images/esriglobe.png")
	public ImageResource esriglobe();
	
	@Source("images/filters-bg.jpg")
	@ImageOptions(repeatStyle = RepeatStyle.Horizontal)
	public ImageResource filters_bg();
	
	@Source("images/icon_minimize_u.png")
	public ImageResource icon_minimize_u();
	
	@Source("images/icon_minimize.png")
	public ImageResource icon_minimize();
	
	@Source("images/icon_restore_u.png")
	public ImageResource icon_restore_u();
	
	@Source("images/icon_restore.png")
	public ImageResource icon_restore();

	@Source("images/icon_close_u.png")
	public ImageResource icon_close_u();
	
	@Source("images/icon_close.png")
	public ImageResource icon_close();
	
	@Source("images/metadata.png")
	public ImageResource metadata();
	
	@Source("images/printer.png")
	public ImageResource printer();
	
	@Source("images/printer_grey.png")
	public ImageResource printer_grey();
	
	@Source("images/exportMap.png")
	public ImageResource exportMap();
	
	@Source("images/exportMap_grey.png")
	public ImageResource exportMap_grey();
	
	@Source("images/exportLegend.png")
	public ImageResource exportLegend();
	
	@Source("images/exportLegend_grey.png")
	public ImageResource exportLegend_grey();
	
	@Source("images/ok.png")
	public ImageResource ok();
	
	@Source("images/error.png")
	public ImageResource error();
	
	@Source("images/loading.gif")
	public ImageResource loading();
	
	@Source("images/mapLoading.gif")
	public ImageResource mapLoading();
	
	@Source("images/drive_icon.png")
	public ImageResource driveIcon();
	
	@Source("css/resources.css")
	public GadgetCssResource css();
	
}
