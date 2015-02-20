package edu.ucdavis.gwt.gis.client.extras;

import com.google.gwt.user.client.ui.ListBox;

public class SimpleOpacitySelector extends ListBox {
	
	private static final int[] VALUES = new int[] {100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0};
	
	public SimpleOpacitySelector() {
		super();
		init();
		setValue(100);
	};
	
	public SimpleOpacitySelector(double value){
		super();
		init();
		setValue(value);
	}
	
	private void init() {
		setWidth("80px");
		for( int i = 0; i < VALUES.length; i++ ) {
			addItem(VALUES[i]+"%", VALUES[i]+"");
		}
	}
	
	public void setValue(double value) {
		if( value > 1 ) value = value / 100;
			
		value = Math.round(value) * 100;
		
		for( int i = 0; i < VALUES[i]; i++ ) {
			if( VALUES[i] == value ) {
				setSelectedIndex(i);
				return;
			}
		}
		setSelectedIndex(0);
	}
	
	public int getValue() {
		return VALUES[getSelectedIndex()];
	}

}
