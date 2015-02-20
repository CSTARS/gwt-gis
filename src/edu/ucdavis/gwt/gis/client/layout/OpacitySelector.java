package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class OpacitySelector extends PopupPanel {
	
	private double[] opacities = new double[] { 1, .9, .8, .7, .6, .5, .4, .3, .2, .1, 0 };
	private double currentOpacity = .5;

	private int height = 220;
	
	public interface SelectHandler {
		public void onSelect(double opacity);
	}
	private SelectHandler handler = null;
	
	private AbsolutePanel panel = new AbsolutePanel();
	
	public OpacitySelector() {
		setStyleName("OpacitySelector");
		setAutoHideEnabled(true);
		
		
		setWidget(panel);
		
		if( Window.Navigator.getUserAgent().contains("MSIE") ){
			getElement().getStyle().setProperty("border", "1px solid #cccccc");
		}
	}
	
	public void show(Widget w, double opacity){
		panel.clear();
		currentOpacity = opacity;
		
		for( int i = 0; i < opacities.length; i++ ){
			
			AbsolutePanel ap = new AbsolutePanel();
			ap.setStyleName("OpacitySelector-absolutepanel");
			HTML amount = new HTML(String.valueOf((int) (opacities[i]*100))+"%");
			amount.getElement().getStyle().setFontSize(11, Unit.PX);
			amount.getElement().getStyle().setProperty("textAlign", "center");
			ap.add(amount);
			
			SimplePanel colorPanel = new SimplePanel();
			colorPanel.setStyleName("OpacitySelector-colorpanel");
			colorPanel.getElement().getStyle().setOpacity(opacities[i]);
			ap.add(colorPanel, 0, 0);
			
			if( currentOpacity == opacities[i] ) {
				SimplePanel current = new SimplePanel();
				current.setStyleName("OpacitySelector-current");
				ap.add(current, 0, 0);
			}
			
			FocusPanel selectPanel = new FocusPanel();
			selectPanel.getElement().setId("OpacitySelector-selectpanel-"+i);
			selectPanel.setStyleName("OpacitySelector-selectpanel");
			selectPanel.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					String id = ((FocusPanel) event.getSource()).getElement().getId().replaceAll("OpacitySelector-selectpanel-", "");
					try {
						double o = opacities[Integer.parseInt(id)];
						currentOpacity = o;
						if( handler != null ) handler.onSelect(o);
					} catch (Exception e) {}
					hide();
				}
			});
			/*selectPanel.addMouseOverHandler(new MouseOverHandler(){
				public void onMouseOver(MouseOverEvent event) {
					((FocusPanel) event.getSource()).addStyleDependentName("mouseover");
				}
			});
			selectPanel.addMouseOutHandler(new MouseOutHandler(){
				public void onMouseOut(MouseOutEvent event) {
					((FocusPanel) event.getSource()).removeStyleDependentName("mouseover");
				}
			});*/
			
			selectPanel.add(ap);
			selectPanel.setTitle(((opacities.length-i-1)*10)+"% Opacity");
			panel.add(selectPanel, 0, i*20);
		}
		panel.setSize(24+"px", (opacities.length*20)+"px");
		panel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		show();
		
		// remove any clipping
		if( Window.getClientHeight() < (w.getAbsoluteTop() + height) ){
			getElement().getStyle().setTop(w.getAbsoluteTop()-height+10, Unit.PX);
			getElement().getStyle().setLeft(w.getAbsoluteLeft(), Unit.PX);
		} else {
			getElement().getStyle().setTop(w.getAbsoluteTop(), Unit.PX);
			getElement().getStyle().setLeft(w.getAbsoluteLeft(), Unit.PX);
		}
	}

	
	public void setOpacity(double o){
		// make sure its order of 10
		currentOpacity = Math.round(o*10) / 10.0;
	}
	
	public void setSelectHandler(SelectHandler handler){
		this.handler = handler;
	}
	
	public double getOpacity() {
		return currentOpacity;
	}

}
