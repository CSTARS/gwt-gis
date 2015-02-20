package edu.ucdavis.gwt.gis.client.auth;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class TokenPopupPanel extends BootstrapModalLayout {
	
	private static TokenPopupPanelUiBinder uiBinder = GWT.create(TokenPopupPanelUiBinder.class);
	interface TokenPopupPanelUiBinder extends UiBinder<Widget, TokenPopupPanel> {}
	
	public static TokenPopupPanel INSTANCE = new TokenPopupPanel();

	@UiField VerticalPanel tokenPanel;
	@UiField TextBox domain;
	@UiField TextBox token;
	@UiField TextBox username;
	@UiField Anchor addButton;
	@UiField HTML messagePanel;
	
	private Widget panel;
	private Anchor closeBtn = new Anchor("Close");
	
	private  TokenPopupPanel() {
		panel = uiBinder.createAndBindUi(this);
		
		addButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addToken();
			}
		});
		
		closeBtn.setStyleName("btn");
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

	}
	
	public void show() {
		showTokens();
		super.show();
	}
	
	private void showTokens() {
		messagePanel.setText("");
		tokenPanel.clear();
		
		LinkedList<DomainToken> tokens = AppManager.INSTANCE.getDomainAccess().getTokens();
		for( DomainToken token: tokens ) {
			tokenPanel.add(new TokenPanel(token));
		}
		
		if( tokens.size() == 0 ) tokenPanel.add(new HTML("No tokens set."));

	}
	
	private void addToken() {
		String d = domain.getText();
		String t = token.getText();
		String u = username.getText();
		
		if( d.isEmpty() || t.isEmpty() ) {
			messagePanel.setText("Please provide a domain and a token.");
			return;
		}
		
		messagePanel.setText("");
		
		DomainToken dt = new DomainToken(d, t, u);
		AppManager.INSTANCE.getDomainAccess().setDomainToken(dt);
		showTokens();
		
		domain.setText("");
		token.setText("");
		username.setText("");
	}
	
	private class TokenPanel extends Composite {
		private SimplePanel panel = new SimplePanel();
		private DomainToken token = null;
		
		public TokenPanel(DomainToken t) {
			panel.getElement().getStyle().setProperty("padding", "5px 0");
			initWidget(panel);
			token = t;
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.setWidth("100%");
			
			VerticalPanel vp = new VerticalPanel();
			
			String title = "<b>"+token.getDomain()+"</b>";
			if( !token.getUsername().isEmpty() ) title += " - "+token.getUsername();
			
			vp.add(new HTML(title));
			vp.add(new HTML("<span style='font-style:italic;font-size:11px'>"+token.getToken()+"</span>"));

			hp.add(vp);
			hp.setCellWidth(vp, "85%");
			
			Image remove = new Image(GadgetResources.INSTANCE.remove());
			remove.setStyleName("AnimatedRemoveIcon");
			remove.setTitle("Delete Token");
			remove.getElement().getStyle().setCursor(Cursor.POINTER);
			hp.add(remove);
			hp.setCellHorizontalAlignment(remove, HorizontalPanel.ALIGN_RIGHT);
			hp.setCellVerticalAlignment(remove, HorizontalPanel.ALIGN_MIDDLE);
			remove.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					if( Window.confirm("Are you sure you want to delete the token for: "+token.getDomain()) ) {
						AppManager.INSTANCE.getDomainAccess().removeDomainToken(token);
						tokenPanel.remove(TokenPanel.this);
					}
				}
			});
			
			panel.add(hp);
		}
		
		
	}

	@Override
	public String getTitle() {
		return "Token Manager";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return closeBtn;
	}

}
