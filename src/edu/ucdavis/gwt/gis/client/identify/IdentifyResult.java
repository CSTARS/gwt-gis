package edu.ucdavis.gwt.gis.client.identify;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.Graphic.Attributes;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class IdentifyResult extends BootstrapModalLayout {
	
	private static IntersectResultUiBinder uiBinder = GWT.create(IntersectResultUiBinder.class);
	interface IntersectResultUiBinder extends UiBinder<Widget, IdentifyResult> {}
	
	public final static IdentifyResult INSTANCE = new IdentifyResult(); 
	
	private final static String[] IMAGE_FORMATS = new String[] {"png","jpg","jpeg","bmp","gif", "tiff"}; 

	private Widget panel;
	private IdentifyResultFooter footer = new IdentifyResultFooter();
	
	// store the current table data, good for when data is exported
	private JavaScriptObject currentTableData;
	private Geometry currentIntersectGeo;
	
	@UiField FlowPanel selectedGeoAttrs;
	@UiField Element layerResultTable;
	@UiField Element layerResultHeader;
	
	public IdentifyResult() {
		panel = uiBinder.createAndBindUi(this);
	}

	private native Element addLayerSelectWidget(Element ele, String selected, JavaScriptObject options, IdentifyResult ir) /*-{
		var nav = $wnd.$('<ul class="nav nav-pills" style="font-size:16px;margin-bottom:10px;border-bottom:1px solid #ccc">'+
						'<li class="dropdown">'+
							'<a class="dropdown-toggle" data-toggle="dropdown" style="cursor:pointer" id="intersect-title-dd"  role="button" >'+selected+'<b class="caret"></b></a>'+
							'<ul class="dropdown-menu" role="menu" aria-labelledby="intersect-title-dd">'+
							'</ul>'+
						'</li>'+
					'</ul>');
		var menu = nav.find(".dropdown-menu");
		for( var i = 0; i < options.length; i++ ) {
			menu.append($wnd.$("<li role='presentation'><a role='menuitem' value='"+i+"'>"+options[i]+"</a></li>"));
		}
		menu.find('a').on('click', function() {
			nav.find('.dropdown-toggle').text($wnd.$(this).text());
			var index = parseInt($wnd.$(this).attr("value"));
			console.log("selecting index: "+index);
			ir.@edu.ucdavis.gwt.gis.client.identify.IdentifyResult::onTitleChange(I)(index);
		});
		
		$wnd.$(ele).html("").append(nav);
		return nav[0];
	}-*/;

	// fired when a new result layer is selected
	private void onTitleChange(int index) {
		loading(false);
		IdentifyTool.INSTANCE.getQuery(index);
	}
	
	public void loading(boolean clearAttrs) {
		if( clearAttrs ) {
			selectedGeoAttrs.clear();
			selectedGeoAttrs.add(new HTML("Finding geometry..."));
		}
		_clear(layerResultTable);
		layerResultTable.setInnerHTML("Loading...");
		if( !isVisible() ) show();
	}
	
	public void setLayerSelector(LinkedList<MapServerDataLayer> layers, String selected) {
		JsArrayString arr = JavaScriptObject.createArray().cast();
		for( int i = 0; i < layers.size(); i++ ) {
			_push(arr, layers.get(i).getLabel());
		}
		addLayerSelectWidget(layerResultHeader, selected, arr, this);
	}
	
	private native void _push(JavaScriptObject jso, String attr) /*-{
		jso.push(attr);
	}-*/;
	
	private native void _push(JavaScriptObject jso, JavaScriptObject attr) /*-{
		jso.push(attr);
	}-*/;
	
	private native void _put(JavaScriptObject jso, String attr, JavaScriptObject value) /*-{
		jso[attr] = value;
	}-*/;
	
	public void showIntersectingGeo(Graphic selectedGeo) {
		selectedGeoAttrs.clear();
		JsArrayString keys = selectedGeo.getAttributes().getKeys();
		for( int i = 0; i < keys.length(); i++) {
			selectedGeoAttrs.add(new HTML("<b>"+keys.get(i)+":</b> "+selectedGeo.getAttributes().getStringForced(keys.get(i))));
		}
		currentIntersectGeo = selectedGeo.getGeometry();
	}
	
	public void showIntersectingGeo(String html, Geometry geo) {
		selectedGeoAttrs.clear();
		selectedGeoAttrs.add(new HTML(html));
		currentIntersectGeo = geo;
	}
	
	public void showResult(HashMap<String, FeatureSet> results) {		
		_clear(layerResultTable);
		currentTableData = JavaScriptObject.createObject();
		
		if( results.size() == 0 ) {
			layerResultTable.setInnerHTML("No interesting geometries found.");
		} else {
			for( String url: results.keySet() ) {
				FeatureSet fs = results.get(url);
				
				addTable(fs.getDisplayFieldName(), fs.getFeatures(), url);
			}
		}
		
		if( !isVisible() ) show();
	}

	
	private void addTable(String title, JsArray<Graphic> features, String url) {
		// create export array
		JavaScriptObject arr = JavaScriptObject.createArray();
		
		// get attribute list
		LinkedList<String> attrs = new LinkedList<String>();
		for( int i = 0; i < features.length(); i++ ) {
			JsArrayString keys = features.get(i).getAttributes().getKeys();
			for( int j = 0; j < keys.length(); j++ ) {
				if( !attrs.contains(keys.get(j)) ) attrs.add(keys.get(j));
			}
		}
		
		String table = "<table class='table table-striped'><tr>";
		JavaScriptObject row = JavaScriptObject.createArray();
		for( int i = 0; i < attrs.size(); i++ ) {
			table += "<th>"+attrs.get(i)+"</th>";
			_push(row, attrs.get(i));
		}
		_push(arr, row);
		table += "</tr>";
		
		for( int i = 0; i < features.length(); i++ ) {
			table += "<tr>";
			Attributes aList = features.get(i).getAttributes();
			row = JavaScriptObject.createArray();
			for( int j = 0; j < attrs.size(); j++ ) {
				if( aList.hasKey(attrs.get(j)) ) {
					table += "<td>"+checkLink(aList.getStringForced(attrs.get(j)))+"</td>";
					_push(row, aList.getStringForced(attrs.get(j)));
				} else {
					table += "<td>&nbsp;</td>";
					_push(row, "");
				}
				
			}
			_push(arr, row);
			table += "</tr>";
		}
		
		_put(currentTableData, title, arr);
		_setTable(layerResultTable, title, table, url, this, AppManager.INSTANCE.getConfig().getProxy());
	}
	
	private String checkLink(String text) {
		if( text.startsWith("http://") || text.startsWith("https://") ) {
			String link = "";
			String[] parts = text.split("\\.");
			if( parts.length > 0 && allowedFormat( parts[parts.length-1] ) ) {  
				link += "<img src='"+text+"' /><br />";
			}
			link += "<a href='"+text+"' target='_blank' name='"+text+"' style='white-space:nowrap'><i class='icon-link'></i> Link</a>";
			return link;
		}
		return text;
	}
	
	private boolean allowedFormat(String format) {
		if( format == null ) return false;
		format = format.toLowerCase();
		
		for( int i = 0; i < IMAGE_FORMATS.length; i++) {
			if( IMAGE_FORMATS[i].equals(format) ) return true;
		}
		return false;
	}
	
	private native void _clear(Element ele) /*-{
		$wnd.$(ele).html("");
	}-*/;
	
	private native void _setTable(Element ele, String title, String table, String url, IdentifyResult ir, String exportUrl) /*-{
		var exportBtn = $wnd.$('<div style="height:45px">'+title+' <br /><i class="icon-download-alt"></i> <a class="d-kml">kml</a> - <a class="d-shape">shapefile</a> - <a class="d-csv">csv</a> </div>');
		exportBtn.find('.d-csv').on('click', function(){
			var tables = ir.@edu.ucdavis.gwt.gis.client.identify.IdentifyResult::currentTableData;
			if( tables[title] ) {
				
				var form = $wnd.$('<form name="input" style="display:none" target="_blank" action="'+exportUrl.replace(/proxy$/,"export")+'" method="post">' +
									'<input type="text" name="title">'+
									'<input type="text" name="data">'+
									'<input type="text" name="type">'+
								  '</form>');
				
				form.find("input[name=title]").val(title);
				form.find("input[name=type]").val("csv");
				form.find("input[name=data]").val(JSON.stringify(tables[title]));
				form.submit();
				
				// TODO: not attaching to DOM for now... do we need to for good old IE?
				//setTimeout(function(){
				//	form.remove();	
				//},10000);
			}
		});
		
		exportBtn.find('a').css('cursor','pointer');
		
		exportBtn.find('.d-kml').on('click', function() {
			var geo = ir.@edu.ucdavis.gwt.gis.client.identify.IdentifyResult::currentIntersectGeo;
			var geoType = @edu.ucdavis.cstars.client.geometry.Geometry::getJsonType(Ledu/ucdavis/cstars/client/geometry/Geometry;)(geo);
			
			var form = $wnd.$('<form name="input" style="display:none" target="_blank" action="'+exportUrl.replace(/proxy$/,"export")+'" method="post">' +
					'<input type="text" name="title">'+
					'<input type="text" name="url">'+
					'<input type="text" name="type">'+
					'<input type="text" name="geometry">'+
					'<input type="text" name="geometryType">'+
					'<input type="text" name="sr">'+
				'</form>');
			
			form.find("input[name=title]").val(title);
			form.find("input[name=url]").val(url+"/query");
			form.find("input[name=type]").val("KML");
			form.find("input[name=geometry]").val(JSON.stringify(geo.toJson()));
			form.find("input[name=geometryType]").val(geoType);
			form.find("input[name=sr]").val(geo.spatialReference.wkid);
			form.submit();
		});
		
		exportBtn.find('.d-shape').on('click', function() {
			var geo = ir.@edu.ucdavis.gwt.gis.client.identify.IdentifyResult::currentIntersectGeo;
			var geoType = @edu.ucdavis.cstars.client.geometry.Geometry::getJsonType(Ledu/ucdavis/cstars/client/geometry/Geometry;)(geo);
			
			var form = $wnd.$('<form name="input" style="display:none" target="_blank" action="'+exportUrl.replace(/proxy$/,"export")+'" method="post">' +
					'<input type="text" name="title">'+
					'<input type="text" name="url">'+
					'<input type="text" name="type">'+
					'<input type="text" name="geometry">'+
					'<input type="text" name="geometryType">'+
					'<input type="text" name="sr">'+
				'</form>');
			
			form.find("input[name=title]").val(title);
			form.find("input[name=url]").val(url+"/query");
			form.find("input[name=type]").val("ESRI Shapefile");
			form.find("input[name=geometry]").val(JSON.stringify(geo.toJson()));
			form.find("input[name=geometryType]").val(geoType);
			form.find("input[name=sr]").val(geo.spatialReference.wkid);
			form.submit();
		});
				
		$wnd.$(ele).append(exportBtn);
		$wnd.$(ele).append($wnd.$(table));
	}-*/;
	
	@Override
	public String getTitle() {
		return "Identify Result";
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
