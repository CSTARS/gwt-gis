package edu.ucdavis.gwt.gis.client.export;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.ESRI;
import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.callback.PrintTaskCallback;
import edu.ucdavis.cstars.client.restful.GPServerInfo;
import edu.ucdavis.cstars.client.restful.GPServerParameterInfo;
import edu.ucdavis.cstars.client.tasks.PrintParameters;
import edu.ucdavis.cstars.client.tasks.PrintTask;
import edu.ucdavis.cstars.client.tasks.PrintTemplate;
import edu.ucdavis.cstars.client.tasks.PrintTask.PrintResult;
import edu.ucdavis.cstars.client.tasks.PrintTemplate.Format;
import edu.ucdavis.cstars.client.tasks.PrintTemplate.Layout;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.config.PrintConfig;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModal;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class PrintMapPopup extends BootstrapModalLayout {

	private static PrintMapPopupUiBinder uiBinder = GWT.create(PrintMapPopupUiBinder.class);
	interface PrintMapPopupUiBinder extends UiBinder<Widget, PrintMapPopup> {}

	public static final String DEFAULT_SERVER = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Utilities/PrintingTools/GPServer/Export%20Web%20Map%20Task";
	
	private PrintTask printTask = null;

	@UiField HTML message;
	@UiField ListBox format;
	@UiField ListBox layout;
	@UiField FlowPanel extraOptions;
	@UiField TextBox title;
	@UiField CheckBox showLegend;
	@UiField ScrollPanel legendSelectSp;
	@UiField VerticalPanel legends;
	@UiField TextBox height;
	@UiField TextBox width;
	@UiField HTML legendMessage;
	
	@UiField Element titleControl;
	@UiField Element layoutControl;
	@UiField Element formatControl;
	@UiField Element legendControl;

	private Widget panel;
	
	Anchor printButton = new Anchor("Export Map");
	private Widget footer = createFooter();
	
	private String server;
	private String host;
	
	private PrintConfig config = null;	
	
	public PrintMapPopup() {
		panel = uiBinder.createAndBindUi(this);
		
		config = AppManager.INSTANCE.getConfig().getPrintConfig();
		
		server = DEFAULT_SERVER;
		if( config.getServer().length() > 0 ) {
		    server = config.getServer();
		}
		
		host = getHost(server);
		
		ESRI.addCorsEnabledServers(host);
		printTask = PrintTask.create(server);
		
		printButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		
		if( !config.allowTitleSelection() ) titleControl.getStyle().setDisplay(Display.NONE);
		if( !config.allowLegendSelection() ) legendControl.getStyle().setDisplay(Display.NONE);
		
		// load layout types and export types
		RequestManager.INSTANCE.makeRequest(
                new Request() {
                    @Override
                    public void onError() {
                        // BAD
                        Debugger.INSTANCE.log("Error: loading print layouts: "+server+"?f=pjson");
                    }
                    @Override
                    public void onSuccess(JavaScriptObject json) {
                        GPServerInfo info = (GPServerInfo) json;
                        setTemplateTypes(info);
                        setFormatTypes(info);
                    }               
                }, server+"?f=pjson"
        );
	}
	
	private Widget createFooter() {
	    FlowPanel panel = new FlowPanel();
	    
	    Anchor closeBtn = new Anchor("Close");
	    Anchor menuBtn = new Anchor("Menu");

	    printButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                Debugger.INSTANCE.log("Print button clicked");
                print();
            }
	    });
	    
        closeBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                BootstrapModal.INSTANCE.hide();
            }
        });
        
        menuBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                AppManager.INSTANCE.getClient().getToolbar().showMenu();
            }
        });
        
        closeBtn.addStyleName("btn");
        
        menuBtn.addStyleName("btn");
        menuBtn.addStyleName("btn-primary");
        
        printButton.addStyleName("btn");
        printButton.addStyleName("btn-primary");
        printButton.addStyleName("pull-left");
        
        panel.add(printButton);
        panel.add(menuBtn);
        panel.add(closeBtn);
        
        return panel;
	}
	
	private GPServerParameterInfo getParameter(String param, GPServerInfo info) {
        for( int i = 0; i < info.getParameters().length(); i++ ) {
            if( info.getParameters().get(i).getName().equals(param) ) {
                return info.getParameters().get(i);
            }
        }
        return null;
	}
	
	private void setFormatTypes(GPServerInfo info){
	    GPServerParameterInfo param = getParameter("Format", info);
        if( param == null ) return;
        
        String defaultValue = param.getDefaultValue();
        if( config.getDefaultFormat().length() > 0 ) defaultValue = config.getDefaultFormat();
        
        for( int i = 0; i < param.getChoiceList().length(); i++ ) {
            String value = param.getChoiceList().get(i);
            format.addItem(value);
            if( defaultValue.equals(value) ) format.setSelectedIndex(i);
        }
        
        showLegend.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                updateLegendSelect();
            }
        });
        
        if( !config.allowFormatSelection() ) formatControl.getStyle().setDisplay(Display.NONE);
	}
	
	private void setTemplateTypes(GPServerInfo info) {
	    GPServerParameterInfo param = getParameter("Layout_Template", info);
	    if( param == null ) return;
	    
	    String defaultValue = param.getDefaultValue();
        if( config.getDefaultTemplate().length() > 0 ) defaultValue = config.getDefaultTemplate();
	            
        for( int i = 0; i < param.getChoiceList().length(); i++ ) {
            String value = param.getChoiceList().get(i);
          
            if( value.equals("MAP_ONLY") ) layout.addItem(value.replaceAll("_", " "), value);
            else layout.addItem(value.replaceAll("_", " "), value);
            
            if( defaultValue.equals(value) ) layout.setSelectedIndex(i);
        }
        layout.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                if( getLayout().equals("MAP_ONLY") ) {
                    extraOptions.setVisible(false);
                } else {
                    extraOptions.setVisible(true);
                }
            }
        });
        
        if( !config.allowTemplateSelection() ) layoutControl.getStyle().setDisplay(Display.NONE);
	}
	
	private String getHost(String server) {
	    return server.replaceAll("^.*:\\/\\/", "").split("/")[0];
	}
	
	public void initAndShow() {
		legends.clear();
		message.setHTML("");
		title.setValue(config.getDefaultTitle());
		
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer dl: layers ) {
			if( dl.getType() == DataLayerType.MapServer) {
				CheckBox cb = new CheckBox(dl.getLabel());
				cb.getElement().setAttribute("value", dl.getId());
				legends.add(cb);
			}
		}
		if( legends.getWidgetCount() == 0 ) legends.add(new HTML("No legends to include"));
		
		width.setValue(AppManager.INSTANCE.getMap().getWidth()+"");
		height.setValue(AppManager.INSTANCE.getMap().getHeight()+"");
		
		updateLegendSelect();
		
		show();
	}
	
	public void print() {
	    Debugger.INSTANCE.log("Disabling btn");
		printButton.addStyleName("disabled");
		printButton.setHTML("<i class='fa fa-spinner fa-spin'></i> Exporting...");
	    
		Debugger.INSTANCE.log("Creating print params");
		PrintParameters params = PrintParameters.create();
		params.setMap(AppManager.INSTANCE.getMap());
		
		Debugger.INSTANCE.log("Creating print template");
		PrintTemplate template = PrintTemplate.create();
		template.setFormat(getFormat());
		
		PrintTemplate.LayoutOptions layoutOptions = PrintTemplate.LayoutOptions.create();
		layoutOptions.setTitleText(title.getText());
		layoutOptions.setScalebarUnit("Meters");
		layoutOptions.setLegendLayers(getLegends());
		layoutOptions.setCopyrightText("");
		layoutOptions.setAuthorText("");
		template.setLayoutOptions(layoutOptions);
		
		template.setLayout(getLayout());
		template.setLabel(title.getText());
		
		template.setExportOptions(PrintTemplate.ExportOptions.create(getInputWidth(), getInputHeight(), 96));
		params.setPrintTemplate(template);
		
		Debugger.INSTANCE.log("Running print task: server="+server+"   cors host="+host);
		Debugger.INSTANCE.log(params);
		printTask.execute(params, 
			new PrintTaskCallback(){
				@Override
				public void onComplete(PrintResult result) {
				    Debugger.INSTANCE.log("Print task success, opening window");
					Window.open(result.getUrl(), "_blank", "");
					 Debugger.INSTANCE.log("Print task reseting UI");
					resetButton();
					message.setHTML("");
				}
				@Override
				public void onError(Error error) {
				    Debugger.INSTANCE.log("Print task error: ");
				    Debugger.INSTANCE.log(error);
				    resetButton();
					message.setHTML("<div class='alert alert-danger'>Error: "+error.getMessage()+"</div>");
				}
		});
	}
	
	private void resetButton() {
	    printButton.removeStyleName("disabled");
        printButton.setText("Export Map");
	}
	
	private String[] getLegends() {
		LinkedList<String> ids = new LinkedList<String>();
		for( int i = 0; i < legends.getWidgetCount(); i++ ) {
			if( legends.getWidget(i) instanceof CheckBox ) {
				CheckBox cb = (CheckBox) legends.getWidget(i);
				if( cb.getValue() ) {
					ids.add(cb.getElement().getAttribute("value"));
				}
			}
		}
		String[] list = new String[ids.size()];
		for( int i = 0; i < ids.size(); i++ ) {
			list[i] = ids.get(i);
		}
		return list;
	}
	
	private String getFormat(){
		return format.getValue(format.getSelectedIndex());
	}
	
	private String getLayout(){
		return layout.getValue(layout.getSelectedIndex());
	}
	
	private int getInputWidth() {
		try {
			return Integer.parseInt(width.getText());
		} catch (Exception e) {}
		return 400;
	}
	
	private int getInputHeight() {
		try {
			return Integer.parseInt(height.getText());
		} catch (Exception e) {}
		return 400;
	}

	private void updateLegendSelect() {
		if( showLegend.getValue() ) {
			legendMessage.setVisible(true);
			extraOptions.setVisible(true);
			legendSelectSp.getElement().getStyle().setOpacity(1);
	         if( legends.getOffsetHeight() > 100 ) {
                legendSelectSp.setHeight("100px");
            } else {
                legendSelectSp.setHeight(legends.getOffsetHeight()+"px");
            }
		} else {
			legendSelectSp.getElement().getStyle().setOpacity(0);
			legendSelectSp.setHeight("0px");
			legendMessage.setVisible(false);
			extraOptions.setVisible(false);
		}
	}

	@Override
	public String getTitle() {
		return "Export Map";
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
