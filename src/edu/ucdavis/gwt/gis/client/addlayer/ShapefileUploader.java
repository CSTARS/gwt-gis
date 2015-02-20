package edu.ucdavis.gwt.gis.client.addlayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.FeatureLayer.FeatureCollectionObject;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class ShapefileUploader extends BootstrapModalLayout{

	private static ShapefileUploaderUiBinder uiBinder = GWT.create(ShapefileUploaderUiBinder.class);
	interface ShapefileUploaderUiBinder extends UiBinder<Widget, ShapefileUploader> {}

	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	private int postCount = 0;
	private boolean uploading = false;
	
	@UiField FormPanel form;
	@UiField FileUpload file;
	@UiField Button addBtn;
	@UiField HTML message;
	@UiField Element helpLink;
	
	public ShapefileUploader() {
		panel = uiBinder.createAndBindUi(this);
		file.setName("file");
		helpLink.setAttribute("href", "http://resources.arcgis.com/en/help/arcgisonline/index.html#//010q000000m2000000");
		
		String[] domain = AppManager.INSTANCE.getConfig().getProxy().split(":\\/\\/");
		initEventListener(domain[0]+"://"+domain[1].replaceAll("\\/.*",""),this);
		
		
		addBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( uploading ) return;
				uploading = true;
				
				message.removeStyleName("alert-success");
				message.removeStyleName("alert-error");
				message.getElement().getStyle().setDisplay(Display.BLOCK);
				message.setHTML("Uploading shapefile...");
				
				upload(
						AppManager.INSTANCE.getConfig().getProxy().replaceAll("proxy$", "loadShapefile"),
						form.getElement(),
						file.getElement()
				);
			}
		});
	}
	
	@Override
	public void show() {
		message.getElement().getStyle().setDisplay(Display.NONE);
		uploading = false;
		super.show();
	}
	
	private void onUploadSuccess() {
		message.addStyleName("alert-success");
		message.setHTML("Success!");
		uploading = false;
		
		new Timer() {
			@Override
			public void run() {
				hide();
			}
		}.schedule(2000);
	}
	
	private void onUploadFail(String msg) {
		message.addStyleName("alert-error");
		message.setHTML("Error :(<br />"+msg);
		uploading = false;
	}
	
	private native void initEventListener(String allowedUrl, ShapefileUploader sh) /*-{
		
		function receiveMessage(event) {
			
			if ( allowedUrl != event.origin ) {
				sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::onUploadFail(Ljava/lang/String;)("Domain Error");
				console.log("Url "+allowedUrl+" and origin "+event.origin+" are not the same");
				return;
			}
			
			if( event.data.error ) {
				if( typeof event.data.error == "object" ) event.data.error = JSON.stringify( event.data.error );
				sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::onUploadFail(Ljava/lang/String;)(event.data.error);
				return;
			}
			
			if( event.data.payload.error ) {
				sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::onUploadFail(Ljava/lang/String;)(JSON.stringify(event.data.payload.error));
				return;
			}
			
			try {
				var lname = event.data.payload.layerName;
				if( event.data && event.data.msg == "createShapeFile" ) {
					for( var i = 0; i < event.data.payload.featureCollection.layers.length; i++ ) {
						sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::addFCLayer(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(lname+"-"+i,event.data.payload.featureCollection.layers[i]);
					}
				}
			} catch(e) {
				sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::onUploadFail(Ljava/lang/String;)("Parse Error");
				return;
			}

			sh.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::onUploadSuccess()();
		}
		
		if ($wnd.addEventListener){
  			$wnd.addEventListener('message', receiveMessage, false); 
		} else if (window.attachEvent){
			$wnd.attachEvent('onmessage', receiveMessage);
		}
	}-*/;
	
	private  void addFCLayer(String name, JavaScriptObject json) {
		FeatureLayer layer = FeatureLayer.create((FeatureCollectionObject) json);
		AppManager.INSTANCE.getClient().addLayer(new FeatureCollectionDataLayer(name, layer));
	}
	
	private native void upload(String url, Element form, Element file) /*-{
			var pCount = this.@edu.ucdavis.gwt.gis.client.addlayer.ShapefileUploader::postCount;
		
			// remove old frame if exsits
			$wnd.$("#postiframe-"+pCount).remove();
			pCount++;
			
			var iframe = $wnd.$('<iframe name="postiframe-'+pCount+'" id="postiframe-'+pCount+'" style="display: none" />');
            $wnd.$("body").append(iframe);
            
            form = $wnd.$(form);
            form.attr("action", url);
            form.attr("method", "post");
            form.attr("enctype", "multipart/form-data");
            form.attr("encoding", "multipart/form-data");
            form.attr("target", "postiframe-"+pCount);
            form.submit();

	}-*/;


	@Override
	public String getTitle() {
		return "Add Shapefile";
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
