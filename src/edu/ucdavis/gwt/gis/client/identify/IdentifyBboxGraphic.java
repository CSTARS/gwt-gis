package edu.ucdavis.gwt.gis.client.identify;

import com.google.gwt.core.client.JavaScriptObject;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.event.MouseEvent;
import edu.ucdavis.cstars.client.event.MouseMoveHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.AppManager;

public class IdentifyBboxGraphic {

		private Graphic graphic = null;
		private Symbol symbol = null;
		private Point firstPoint = null;
		private Polygon poly = null;
		private boolean active = false;

		public IdentifyBboxGraphic() {
			symbol = SimpleFillSymbol.create(
					SimpleFillSymbol.StyleType.STYLE_SOLID, 
					SimpleLineSymbol.create(
							SimpleLineSymbol.StyleType.STYLE_SOLID,
							Color.create(255, 0, 0, .9),
							2), 
					Color.create(0, 0, 0, .4));

			AppManager.INSTANCE.getMap().addMouseMoveHandler(new MouseMoveHandler() {
				@Override
				public void onMouseMove(MouseEvent event) {
					if( active ) update(event.getMapPoint());
				}
			});
		}

		public void start(Point p) {
			firstPoint = p;
			active = true;
			update(p);
		}

		private void update(Point p) {
			JavaScriptObject pJso = null;
			if( p.getX() < firstPoint.getX() ) {
				if( p.getY() < firstPoint.getY() ) {
					pJso = createPolyJso(p.getX(), p.getY(), firstPoint.getX(), firstPoint.getY(), p.getSpatialReference());
				} else {
					pJso = createPolyJso(p.getX(), firstPoint.getY(), firstPoint.getX(), p.getY(), p.getSpatialReference());
				}
			} else {
				if( p.getY() < firstPoint.getY() ) {
					pJso = createPolyJso(firstPoint.getX(), p.getY(), p.getX(), firstPoint.getY(), p.getSpatialReference());
				} else {
					pJso = createPolyJso(firstPoint.getX(), firstPoint.getY(), p.getX(), p.getY(), p.getSpatialReference());
				}
			}
			poly = Polygon.create(pJso);
			if( graphic != null ) AppManager.INSTANCE.getMap().getGraphics().remove(graphic);
			graphic = Graphic.create(poly, symbol);
			AppManager.INSTANCE.getMap().getGraphics().add(graphic);
		}

		public boolean isActive() {
			return active;
		}

		public Extent end(Point p) {
			// TODO
			//pointAnimation.show(((Polygon) graphic.getGeometry()).getExtent().getCenter());
			active = false;
			AppManager.INSTANCE.getMap().getGraphics().remove(graphic);
			graphic = null;

			Extent ext = null;
			if( p.getX() < firstPoint.getX() ) {
				if( p.getY() < firstPoint.getY() ) {
					ext = Extent.create(p.getX(), p.getY(), firstPoint.getX(), firstPoint.getY(), p.getSpatialReference());
				} else {
					ext = Extent.create(p.getX(), firstPoint.getY(), firstPoint.getX(), p.getY(), p.getSpatialReference());
				}
			} else {
				if( p.getY() < firstPoint.getY() ) {
					ext = Extent.create(firstPoint.getX(), p.getY(), p.getX(), firstPoint.getY(), p.getSpatialReference());
				} else {
					ext = Extent.create(firstPoint.getX(), firstPoint.getY(), p.getX(), p.getY(), p.getSpatialReference());
				}
			}

			return ext;
		}

		private native JavaScriptObject createPolyJso(double xmin, double ymin, double xmax, double ymax, JavaScriptObject sr) /*-{
			return {
				"rings":[
					[	[xmin, ymax],
						[xmax, ymax],
						[xmax, ymin],
						[xmin, ymin],
						[xmin, ymax],
					]
				],"spatialReference":sr
			};
		}-*/;

}
