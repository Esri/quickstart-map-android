package com.esri.quickstart;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.geocode.Locator;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.geocode.LocatorReverseGeocodeResult;

/**
 * A helper library for use with ArcGIS <code>MapView</code> and Android OS. Take advantage of this library
 * to accelerate your application development cycles. It includes many helper methods for dealing with maps
 * and basic graphics. It also has a built-in event bus to help you loosely couple the various components
 * in your Activities.
 * 
 * @author Andy Gup
 * @version 1.2
 */
public class EsriQuickStart extends MapView {
	
	private MapView _mapView;
	private Activity _activity;	
	
	private boolean _isMapLoaded = false;
	
	private ArcGISTiledMapServiceLayer _streets = null;
	private ArcGISTiledMapServiceLayer _topo = null;
	private ArcGISTiledMapServiceLayer _satellite = null;	
	
	private GraphicsLayer _pointsGraphicsLayer = null;
	private GraphicsLayer _drawGraphicsLayer = null;
	
	private MyTouchListener _myTouchListener;	
	
	private LocationService _locationService = null;
	private LocationManager _locationManager = null;
	private Location _lastKnownLocation = null;
	
	private EsriQuickStartEvent QSEvent;
	
	private Double _center_lat; 
	private Double _center_lon;
	private Double _center_scale;
	
	private static double _DEFAULT_SCALE = 1155581;
	private static double _DEFAULT_LAT = 37.719098; 
	private static double _DEFAULT_LON = -122.421916;
	private static double _DEFAULT_GPS_ACCURACY_RADIUS_METERS = 1000;
	private final static double SEARCH_RADIUS = 4;		
	
	/**
	 * A helper library for use with ArcGIS <code>MapView</code> and Android OS. Take advantage of this library
	 * to accelerate your application development cycles. It includes many helper methods for dealing with maps
	 * and basic graphics. It also has a built-in event bus to help you loosely couple the various components
	 * in your Activities.
	 * @author Andy Gup
	 * @version 1.2
	 * @param activity A reference to the primary Activity where the map will be displayed
	 * @param mapId id the mapView in your primary layout xml file, e.g. (R.id.map);
	 */
	public EsriQuickStart(Activity activity,int mapId) {
		super(activity); 
		_activity = activity; 
		_mapView = (MapView) _activity.findViewById(mapId);
		QSEvent = new EsriQuickStartEvent(_activity);
		setMapListeners();
		
		_streets = new ArcGISTiledMapServiceLayer(MapType.STREETS.getURL());
		_streets.setVisible(false);
		_topo = new ArcGISTiledMapServiceLayer(MapType.TOPO.getURL());
		_topo.setVisible(false);
		_satellite = new ArcGISTiledMapServiceLayer(MapType.SATELLITE.getURL());
		_satellite.setVisible(false);
		
		_pointsGraphicsLayer = new GraphicsLayer();
		_drawGraphicsLayer = new GraphicsLayer();
	}
	
	/**
	 * <b>REQUIRED!</b> Specifies a map from the choices listed in the Map enum. <br><br>
	 * <b>IMPORTANT:</b> To swap layers use the <code>setBaseMapVisibility()</code> method.
	 * @param maptype the type of map as defined by the public enum Map
	 * @param latitude center the map at this latitude. If null then library uses <code>_DEFAULT_LAT</code>.
	 * @param longitude center the map at this longitude. If null then library uses <code>_DEFAULT_LON</code>.
	 * @param scale sets the scale when the map first loads. If null then library uses <code>_DEFAULT_SCALE</code>.
	 * @param visible if you add multiple layers then it's a best practice to have one layer set to visible at a time.
	 * @return ArcGISTiledMapServiceLayer
	 */
	public ArcGISTiledMapServiceLayer addLayer(MapType maptype,Double latitude, Double longitude, Double scale, Boolean visible){

		_center_lat = latitude;
		_center_lon = longitude;
		_center_scale = scale;

		if(maptype == null){
			Log.d("EsriQuickStart","Warning: addLayer() maptype = null. The map will not display.");
		}	
		
		ArcGISTiledMapServiceLayer mapLayer = setMapLayer(maptype, visible);
		createDefaultLayers();
		
		return mapLayer;
	}	
	
	private void createDefaultLayers(){
		int pointsGraphicsLayerNumGraphics = _pointsGraphicsLayer.getNumberOfGraphics();
		int drawGraphicsLayerNumGraphics = _drawGraphicsLayer.getNumberOfGraphics();
		
		if(pointsGraphicsLayerNumGraphics == 0){
			_pointsGraphicsLayer = new GraphicsLayer();
			_mapView.addLayer(_pointsGraphicsLayer);
		}
		else{
			_mapView.removeLayer(_pointsGraphicsLayer);
			_pointsGraphicsLayer = new GraphicsLayer();
			_mapView.addLayer(_pointsGraphicsLayer);
		}
			
		
		if(drawGraphicsLayerNumGraphics == 0){
			_drawGraphicsLayer = new GraphicsLayer();
			_mapView.addLayer(_drawGraphicsLayer);
		}
		else{
			_mapView.removeLayer(_drawGraphicsLayer);
			_pointsGraphicsLayer = new GraphicsLayer();
			_mapView.addLayer(_drawGraphicsLayer);
		}
		
	}
	
	private void setMapListeners(){
		
		_mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
	         private static final long serialVersionUID = 1L;
	         Point latlon;
	         
			@Override
			public void onStatusChanged(Object source, STATUS status) {
		         if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == _mapView) {
		            Log.d("test","backend");
		        	 if(_isMapLoaded == false){
			        	 _isMapLoaded = true;
											 			
						if(_center_lat == null || _center_lon == null){
							latlon = new Point(_DEFAULT_LON,_DEFAULT_LAT);
						}
						else{
							latlon = new Point(_center_lon,_center_lat);
						}
						Point point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());		
						_mapView.centerAt(point,false);
						
						if(_center_scale == null){
							_mapView.setScale(_DEFAULT_SCALE);
						}
						else{
							_mapView.setScale(_center_scale);
						}							        		 
		        	 }
		             
		        	Log.d("EsriQuickStartLib", "setOnStatusChangedListener() STATUS: " + status);	
		        	QSEvent.dispatchStatusEvent(new EsriQuickStartEvent(MapViewEventType.STATUS_INITIALIZED), status.toString(), source);		        	 
		         }
		         if (OnStatusChangedListener.STATUS.LAYER_LOADING_FAILED == status && source == _mapView){
		        	 Log.d("EsriQuickStartLib", "setMapListeners() STATUS: " + status + ", " + status.toString());
		        	 displayToast("There was a map problem with loading a layer: " + status.toString());
		        	 QSEvent.dispatchStatusErrorEvent(new EsriQuickStartEvent(MapViewEventType.STATUS_LAYER_LOADING_FAILED), status.toString());
		         }
			}	   
	    });	
	}
	
	/**
	 * Creates a new map layer via the <code>MapType</code>. If layer exists
	 * this method writes error to <code>Log</code> and will not create a duplicate
	 * layer.
	 * @param maptype
	 * @param visible whether or not you want the layer to be visible
	 * @return ArcGISTiledMapServiceLayer
	 */
	private ArcGISTiledMapServiceLayer setMapLayer(MapType maptype, Boolean visible){

		ArcGISTiledMapServiceLayer tempLayer = null;
		
		switch(maptype){
		case STREETS:
			if(layerExists(MapType.STREETS) == false){
				_streets.setOnStatusChangedListener(new OnStatusChangedListener() {

					private static final long serialVersionUID = -4632603154399767754L;

					@Override
					public void onStatusChanged(Object source, STATUS status) {
						handleStatusEvent(status, MapType.STREETS);
					}
				});
				
				_streets.setVisible(visible);
				_mapView.addLayer(_streets);	
				tempLayer = _streets;
			}
			else{
				Log.d("EsriQuickStartLib","Error: setMapLayer() - STREETS already exists.");
			}
			
			break;
		case TOPO:
			if(layerExists(MapType.TOPO) == false){
				_topo.setOnStatusChangedListener(new OnStatusChangedListener() {
					
					private static final long serialVersionUID = 1494551602695195478L;

					@Override
					public void onStatusChanged(Object source, STATUS status) {
						handleStatusEvent(status, MapType.TOPO);					}
				});
				
				_topo.setVisible(visible);
				_mapView.addLayer(_topo);	
				tempLayer = _topo;
			}
			else{
				Log.d("EsriQuickStartLib","Error: setMapLayer() - TOPO already exists.");
			}
			break;
		case SATELLITE:	
			if(layerExists(MapType.SATELLITE) == false){
				_satellite.setOnStatusChangedListener(new OnStatusChangedListener() {
					
					private static final long serialVersionUID = 2127005438050776513L;

					@Override
					public void onStatusChanged(Object source, STATUS status) {
						handleStatusEvent(status, MapType.SATELLITE);	
					}
				});
				
				_satellite.setVisible(visible);
				_mapView.addLayer(_satellite);
				tempLayer = _satellite;
			}
			else{
				Log.d("EsriQuickStartLib","Error: setMapLayer() - SATELLITE already exists.");
			}
			break;
		}	
				
		return tempLayer;
	}
	
	/**
	 * Internal method handling MapView and Layer status change events.
	 * @param status
	 * @param mapType
	 */
	private void handleStatusEvent(OnStatusChangedListener.STATUS status, MapType mapType){
		if (OnStatusChangedListener.STATUS.INITIALIZED == status){
			Log.d("EsriQuickStartLib", "setMapLayer() MapType: " + mapType.name()+ ", " + status.toString());			
		}						
		if (OnStatusChangedListener.STATUS.INITIALIZATION_FAILED == status){
			Log.d("EsriQuickStartLib", "setMapLayer() STATUS: " + status + ", " + status.toString());
			displayToast("There was a map problem with loading a layer: " + status.toString());
			// TODO
		}
	}

	
	/**
	 * Allows you to toggle map visibility between <code>MapType.STREETS</code>,<code>MapType.TOPO</code>,
	 * and <code>MapType.SATELLITE</code>. Safe to use even if map layer is null.
	 * @param maptype The Map enum specifying which map will be visible.
	 */
	public void setBaseMapVisibility(MapType maptype){
		boolean streets = layerExists(MapType.STREETS);
		boolean topo = layerExists(MapType.TOPO);
		boolean satellite = layerExists(MapType.SATELLITE);
		
		switch(maptype){
		case STREETS:			
			if(streets){
				_streets.setVisible(true);
			}
			if(topo){
				_topo.setVisible(false);
			}
			if(satellite){
				_satellite.setVisible(false);
			}
			break;
		case TOPO:
			if(streets){
				_streets.setVisible(false);
			}
			if(topo){
				_topo.setVisible(true);
			}
			if(satellite){
				_satellite.setVisible(false);
			}			
			break;
		case SATELLITE:		
			if(streets){
				_streets.setVisible(false);
			}
			if(topo){
				_topo.setVisible(false);
			}
			if(satellite){
				_satellite.setVisible(true);
			}			
			break;
			//TODO always leave graphicslayer on top
		}	
	}

	/**
	 * Returns whether or not a MapType layer has been added or not via <code>addLayer()</code>
	 * @param maptype
	 * @return Boolean confirms the layer exists
	 */
	public Boolean layerExists(MapType maptype){
		Boolean exists = false;
		String layerName = null;
		Layer[] layerArr = _mapView.getLayers();
		
		for(Layer layer : layerArr)
		{
			if(layer instanceof ArcGISTiledMapServiceLayer){
				ArcGISTiledMapServiceLayer tempMap = (ArcGISTiledMapServiceLayer) layer;
				ArcGISLayerInfo[] info = tempMap.getLayers();
				
				if(info != null){
					layerName = info[0].getName().toLowerCase();
					if(layerName.contains("world imagery") && maptype == MapType.SATELLITE){
						exists = true;
						break;
					}
					if(layerName.contains("topographic info") && maptype == MapType.TOPO){
						exists = true;
						break;
					}		
					if(layerName.contains("world street map") && maptype == MapType.STREETS){
						exists = true;
						break;
					}				
				}
			}
		}
		
		return exists;
	}	
	
	/**
	 * Easy way to check if a GraphicsLayer exists
	 * @param graphicsLayer
	 * @return boolean
	 */
	public Boolean layerExists(GraphicsLayer graphicsLayer){
		Boolean exists = false;
		String layerName = graphicsLayer.getName();
		Layer[] layerArr = _mapView.getLayers();
		
		for(Layer layer : layerArr)
		{
			if(layer instanceof GraphicsLayer){
				GraphicsLayer tempLayer = (GraphicsLayer) layer;
				if(tempLayer.getName() == layerName){
					exists = true;
				}

			}
		}
		
		return exists;
	}
	
	/**
	 * Returns the name of the current visible ArcGISTiledMapServiceLayer
	 * @return String The layer name
	 */
	public String getCurrentVisibleLayerName(){
		String layerName = null;
		Layer[] layerArr = _mapView.getLayers();
		
		for(Layer layer : layerArr)
		{
			Boolean visible = layer.isVisible();
			if(layer instanceof ArcGISTiledMapServiceLayer && visible == true){
				ArcGISTiledMapServiceLayer tempMap = (ArcGISTiledMapServiceLayer) layer;
				ArcGISLayerInfo[] info = tempMap.getLayers();
				if(info != null)layerName = info[0].getName().toLowerCase();
				break;
			}
		}
		
		return layerName;
	}
	
	/**
	 * Helper method that finds closest graphic based on a map click.  
	 * @param x Float value of x from map click
	 * @param y Float value of y from map click
	 * @param graphicsLayer a reference to any <code>GraphicsLayer</code> being used by your app.
	 * @param tolerance the tolerance in pixels. Recommended value is 25 or greater.
	 * @return Graphic returns null if no <code>Graphic</code> is found
	 */
	public Graphic findClosestGraphic(float x, float y,GraphicsLayer graphicsLayer,int tolerance){
	    Graphic graphic = null;
		int[] graphicIDs = graphicsLayer.getGraphicIDs(x, y, tolerance);
		if (graphicIDs != null && graphicIDs.length > 0) {
			graphic = graphicsLayer.getGraphic(graphicIDs[0]);
		}
		
		return graphic;
	}	
	
	/**
	 * Helper method that uses latitude/longitude points to programmatically 
	 * draw a <code>SimpleMarkerSymbol</code> and adds the <code>Graphic</code> to map.
	 * @param latitude
	 * @param longitude
	 * @param attributes
	 * @param style You defined the style via the Enum <code>SimpleMarkerSymbol.STYLE</code>
	 */
	public void addGraphicLatLon(double latitude, double longitude, Map<String, Object> attributes, SimpleMarkerSymbol.STYLE style){
		
		Point latlon = new Point(longitude,latitude);		
		
		//Convert latlon Point to mercator map point.
		Point point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());		
		
		//Set market's color, size and style. You can customize these as you see fit
		SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.BLUE,25, style);			
		Graphic graphic = new Graphic(point, symbol,attributes,null);
		_pointsGraphicsLayer.addGraphic(graphic);
	}
	
	/**
	 * Helper method uses latitude/longitude to programmatrically draw a simple, non-annotated line and add it to the map.
	 * 
	 * @param lineArray a <code>String</code> Array containing "lat,lon" pairs.
	 * @param style use SimpleLineSymbol.STYLE
	 * @param color the color of the line. e.g. Color.BLUE
	 */
	public void addGraphicLine(String[] lineArray, SimpleLineSymbol.STYLE style, int color){
		
		SimpleLineSymbol symbol = new SimpleLineSymbol(color,5,style);

		Float lat;
		Float lon;
		Point latlon;
		Point point;
		Boolean startPoint = false;
		Graphic graphic;
		MultiPath poly = new Polyline();	
		
		for(String s : lineArray){
			lat = Float.parseFloat(s.split(",")[0]);
			lon = Float.parseFloat(s.split(",")[1]);
			latlon = new Point(lon,lat);
			
			//Convert latlon Point to mercator map point.
			point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());
			
			if(startPoint == false){

				poly.startPath((float)point.getX(),(float)point.getY());
				startPoint = true;

				//Create a Graphic and add polyline geometry
				graphic = new Graphic(latlon,symbol);

				 //Add the updated graphic to graphics layer
				_drawGraphicsLayer.addGraphic(graphic);				
			}
			else{
				poly.lineTo((float)point.getX(),(float)point.getY());
				graphic = new Graphic(poly,symbol);
				_drawGraphicsLayer.addGraphic(graphic);
			}
		}
	}
	
	/**
	 * Helper method that uses latitude/longitude to center the map
	 * @param latitude
	 * @param longitude
	 * @param animated
	 */
	public void centerAt(double latitude, double longitude, boolean animated){
		Point latlon = new Point(longitude,latitude);		
		
		//Convert latlon Point to mercator map point.
		Point point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());
		_mapView.centerAt(point, animated);
	}
	
	/**
	 * Helper method that uses latitude/longitude to center the map
	 * @param latitude
	 * @param longitude
	 * @param scale Get info on scale at the map services REST endpoing. See MapType.getURL().
	 * @param animated
	 */
	public void centerAt(double latitude, double longitude, Double scale, boolean animated){
		Point latlon = new Point(longitude,latitude);		
		
		//Convert latlon Point to mercator map point.
		Point point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());
		_mapView.centerAt(point, animated);

		if(scale == null){
			_mapView.setScale(_DEFAULT_SCALE);
		}
		else{
			_mapView.setScale(scale);
		}		
		
	}
	
	/**
	 * Helper method that displays a TOAST message. Default is TOAST.LENGTH_LONG.
	 * @param message The message you wish to be displayed in TOAST.
	 * @param toastLength (Optional) valid options are Toast.LENGTH_LONG or Toast.LENGTH_SHORT.
	 */
	public void displayToast(String message,int... toastLength) {
		int length = Toast.LENGTH_LONG;
		if(toastLength.length != 0){
			length = toastLength[0];
		}
		Toast toast = Toast.makeText(getContext(),
				message,
				length);
		toast.show();
	}	
	
	/**
	 * <b>REQUIRED:</b> Sets the DrawType for the draw functionality. Without
	 * this you will get a <code>nullPointerException.</code><br><br>
	 * <b>NOTE:</b> For more complex editing, such as editing attributes or points
	 * within a polygon see the Editing samples in the SDK
	 * @param drawType
	 */
	public void setDrawType(DrawType drawType){
		setDrawTouchListener();
		_myTouchListener.setDrawType(drawType);
	}
	
	////////////////////////////
	////	
	//// GPS WRAPPER
	////
	////////////////////////////
	
	/**
	 * Performs safety checks before starting the LocationService. Should be limited to internal methods within
	 * the EsriQuickStartLib. <br>
	 * It is almost always better to use delayedStartLocation() to guarantee that map is loaded first and LocationService
	 * has fully initialized and all its listeners have been enabled.
	 * @throws LOCATION_EXCEPTION if unable to shutdown the LocationService
	 * @see delayedStartLocationService for thread safe starting after an Activity change
	 */
	public void startLocationService(){
		try{
			if(_locationService.isStarted() == false){
				_locationService.start();
				Location loc = new Location("null"); 
				QSEvent.dispatchLocationChangedEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_INITIALIZED),loc);	
			}
		}
		catch(Exception exc){
			Log.d("EsriQuickStartLib","EsriQuickStartLib startLocation() unable to start. " + exc.toString());
		}
	}
	
	/**
	 * Internal method for killing of all location listeners and managers
	 */
	private void shutdownAllLocation(){
		_locationManager = null;
		_locationService = null;
	}
	
	/**
	 * Stops LocationService. Checks if it is running first. Attempts 5 times to stop the service.
	 * @param silent OPTIONAL will prevent this method from dispatching shutdown events. Accepts <code>null</code> values.
	 * @throws MapViewEventType.LOCATION_SHUTDOWN 
	 */
	public void stopLocationService(final Boolean... silent){

		if(_locationService != null){
			try{
				if(_locationService.isStarted() == true){
					_locationService.stop();
				}

				if(_locationService.isStarted() == false){
					//Be sure to kill off all location listeners and managers
					shutdownAllLocation();										
					Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation() LocationService has stopped.");
					if(silent == null || silent.length == 0){
						QSEvent.dispatchLocationShutdownEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_SHUTDOWN), "LocationService stopped via stopLocation() request");
					}
				}
				else{
	
					Runnable task = new Runnable() {
						final Handler handler = new Handler();
						
						@Override
						public void run() {

							handler.postDelayed(new Runnable() {
								int counter = 0;
								
								@Override
								public void run() {
									counter++;
									try{
										_locationService.stop();
										boolean test = _locationService.isStarted();
										Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation(): attempting to run stopLocation(). Attempt #" + counter);
										
										if(test == false){
											
											//Be sure to kill off all location listeners and managers
											shutdownAllLocation();
											
											Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation() LocationService has stopped.");
											if(silent == null || silent.length == 0){
												QSEvent.dispatchLocationShutdownEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_SHUTDOWN), "LocationService stopped via stopLocation() request");
											}	
										}
										else{
											if(counter < 5){
												handler.postDelayed(this, 5000);
											}
											else{
												Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation(): Unable to stop location after 5 attempts.");
												QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_EXCEPTION),"Unable to stop location");
											}
										}
									}
									catch(Exception exc){
										Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation() exception: " + exc.toString());
										QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_EXCEPTION),exc.toString());
									}
									
								}
							}, 2000);					
						}	
					};
					
					//task.run();
					Thread thread = new Thread(task);  
					thread.start();
				}					
			}
			catch(Exception exc){
				Log.d("EsriQuickStartLib","EsriQuickStartLib stopLocation() unable to stop. " + exc.toString());
				QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_EXCEPTION),exc.toString());
			}
			
		}
	}
	
	/**
	 * Last known location taken from cache.
	 * @return Location this will be <code>null</code> if location not known.
	 */
	public Location getLastKnowLocation(){
		return _lastKnownLocation;
	}
	
	/**
	 * Uses a Handler to delay the start of the location service until the base map has fully initialized. This is the
	 * preferred method when accessing this controller publicly. By default it will try 5 times to start().
	 * If you don't do this, then you'll get <code>nullPointerException</code> when the location service attempts
	 * to plot a graphic on a non-existent map.
	 * @param gpsSnapshotMode OPTIONAL set to true if you want the GPS to settle down first before dispatching location event
	 * and then auomatically shutoff. Listen for event via <code>MapViewEvent.LOCATION_UPDATE</code> 
	 * The shutdown threshold is determined by <code>_DEFAULT_GPS_ACCURACY_RADIUS_METERS</code>
	 * @throws LOCATION_INITIALIZED when LocationService is initialized but before start() is called.
	 * @throws LOCATION_DELAYEDSTART_FAILURE if unable to start LocationService.
	 */	
	public void delayedStartLocationService(final boolean...gpsSnapshotMode){	
		
		Runnable task = new Runnable() {
			final Handler handler = new Handler();	
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{

					handler.postDelayed(new Runnable() {
						int counter = 0;
						
						@Override
						public void run() {
							counter++;
							try{ 
								boolean test = isMapLoaded();
								Log.d("EsriQuickStartLib","EsriQuickStartLib delayedStartLocation(): Testing if layer is loaded. Attempt #" + counter);
								if(test == true){
									
									Log.d("EsriQuickStartLib","EsriQuickStartLib delayedStartLocation(): map is loaded.");
									
									//LocationService is null, if so we need to reinitialize all the listeners and attributes
									if(_locationService == null && 
											(gpsSnapshotMode.length == 0 || gpsSnapshotMode == null || gpsSnapshotMode[0] == false)){ 										
										//Snapshot mode false
										setLocationListener(true, false,true);
									}
									else if(gpsSnapshotMode.length == 0 || gpsSnapshotMode[0] == false){
										setLocationListener(true, false,true);
									}
									else{
										//Snapshot mode true
										setLocationListener(true, true,true);
									}									
									//startLocationService();
								}
								else{
									if(counter < 5){
										handler.postDelayed(this, 5000);
									}
									else{
										Log.d("EsriQuickStartLib","EsriQuickStartLib delayedStartLocation(): Unable to reinitialize location.");
										QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_DELAYEDSTART_EXCEPTION),"Attempting to start location service failed after 5 attempts.");
									}
								}
							}
							catch(Exception exc){
								Log.d("EsriQuickStartLib","EsriQuickStartLib delayedStartLocation() exception: " + exc.toString());
								QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_DELAYEDSTART_EXCEPTION),exc.toString());
							}
							
						}
					}, 250);					
				}
				catch(Exception exc){
					Log.d("EsriQuickStartLib","EsriQuickStartLib delayedStartLocation() exception: " + exc.toString());
					QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_DELAYEDSTART_EXCEPTION),exc.toString());
				}
			}
		};
		
		//task.run();
		Thread thread = new Thread(task);
		thread.start();
	}
	
	/**
	 * Starts the location service. A reference to the location service is available through
	 * the <code>getLocationService()</code> method. <br>
	 * <b>IMPORTANT!</b> Make sure the map is initialized before setting the listeners. 
	 * @param autoCenter if set to true this will force the map to center on each location update.
	 * @param gpsSnapshotMode OPTIONAL set to true if you want the GPS to settle down first and then shutoff. 
	 * The shutdown threshold is determined by <code>_DEFAULT_GPS_ACCURACY_RADIUS_METERS</code>
	 * @param autoStart (Optional) starts the location service immediately.
	 * @throws LOCATION_ERROR if an error was encountered while trying to start() the service,
	 * or after multiple null values have been thrown by the device.
	 * @see getLocationService provides a reference to the location service.
	 * @see delayedStartLocationService
	 */
	private void setLocationListener(boolean autoCenter,final boolean gpsSnapshotMode, boolean... autoStart){
		boolean locationEnabled = isLocationEnabled();
		Location lastKnownLocation = null;

		_locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
		// Or use LocationManager.GPS_PROVIDER
		final Boolean networkProviderEnabled = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		final Boolean gpsProviderEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(networkProviderEnabled == true){
			lastKnownLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		else if(gpsProviderEnabled == true){
			lastKnownLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		else{
			Log.d("EsriQuickStartLib", "IMPORTANT: Neither NETWORK_PROVIDER nor GPS_PROVIDER are available.");
		}

		//If possible use the last known location when map first launches
		if(locationEnabled == false && lastKnownLocation != null){			
			zoomToLocation(lastKnownLocation);	
			showGPSDisabledAlertToUser(); 			
		}
		else if (lastKnownLocation != null) {
			zoomToLocation(lastKnownLocation);
		}

		final boolean _autoCenter = autoCenter; 

		_locationService = _mapView.getLocationService(); 
		_locationService.setAutoPan(_autoCenter);	
		_locationService.setLocationListener(new LocationListener() {
			int counter = 0;
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				Log.d("EsriQuickStartLib","EsriQuickStartLib - location provider status changed!");
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				Log.d("EsriQuickStartLib","EsriQuickStartLib - location provider has been enabled!");	
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				_locationService.stop();
			}
			
			@Override
			public void onLocationChanged(Location location) {				
				
				if (location != null) {
					
					Log.d("EsriQuickStartLib","EsriQuickStartLib - onLocationChanged: " + 
							Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));	
					
					if(location.hasAccuracy()){
						
						QSEvent.dispatchLocationChangedEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_UPDATE),location);
						
						if(location.getAccuracy() < _DEFAULT_GPS_ACCURACY_RADIUS_METERS && gpsSnapshotMode == true){
							zoomToLocation(location);								
							Log.d("EsriQuickStartLib","onLocationChanged() snapshot mode = true. Criteria has been met. " +
									"Stopping location listener. ");
							stopLocationService();
							
							//Assign some basic attributes to the point
							DecimalFormat df = new DecimalFormat("0.000");
							Map<String, Object> attributes = new HashMap<String, Object>();
							attributes.put("Lat", df.format(location.getLatitude()));
							attributes.put("Lon", df.format(location.getLongitude()));
							
							addGraphicLatLon(location.getLatitude(),location.getLongitude(), attributes, SimpleMarkerSymbol.STYLE.DIAMOND);
							QSEvent.dispatchLocationShutdownEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_SHUTDOWN), "LocationService stopped via stopLocation() request");
						}
						else{
							zoomToLocation(location);			
						}
					}
					counter = 0;
				}
				else{
					counter++;
					Log.d("EsriQuickStartLib","setLocationListener() location is null. There might be a problem. ");
					QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_EXCEPTION),"Error counter = " + counter);
					
					if(counter > 3){
						Log.d("EsriQuickStartLib","setLocationListener() location is null. There might be a problem. Shutting down location service.");
						QSEvent.dispatchLocationExceptionEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_EXCEPTION),"Too many null location values. Stopping location service.");						
						_locationService.stop();
						counter = 0;
						//TO-DO consider adding an event when location service is stopped.
					}
				}
			}
		});	
		
		if(autoStart != null && autoStart[0] == true && locationEnabled == true){
			_locationService.start();
			Location loc = new Location("null"); 
			QSEvent.dispatchLocationChangedEvent(new EsriQuickStartEvent(MapViewEventType.LOCATION_INITIALIZED),loc);	
		}		
				
	}
	
	/**
	 * Zooms the map given a Location.<br>
	 * Uses final static double SEARCH_RADIUS set to Envelope.
	 * @param location android.location.Location
	 */
	protected void zoomToLocation(Location location){
		double locy = location.getLatitude();
		double locx = location.getLongitude(); 
		Point wgspoint = new Point(locx, locy);
		Point mapPoint = (Point) GeometryEngine
				.project(wgspoint,
						SpatialReference.create(4326),
						_mapView.getSpatialReference()); 
		
		//found a multi-threading bug that the spatialReference may not be immediately available.
		try{ 
			SpatialReference sr = _mapView.getSpatialReference(); 
			Unit mapUnit = sr.getUnit();  
			double zoomWidth = Unit.convertUnits(
					SEARCH_RADIUS,
					Unit.create(LinearUnit.Code.MILE_US),
					mapUnit); 
			Envelope zoomExtent = new Envelope(mapPoint,
					zoomWidth, zoomWidth); 
			_mapView.setExtent(zoomExtent);
		}
		catch(Exception exc){
			Log.d("EsriQuickStartLib","EsriQuickStartLib zoomToLocation() + " + exc.toString());
		}
	}
	
	/**
	 * Provide the user a dialog box when the GPS is disabled.
	 */
	private void showGPSDisabledAlertToUser(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
		alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
			.setCancelable(false)
			.setPositiveButton("Goto Settings Page To Enable GPS",
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int id){ 
							Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							getContext().startActivity(callGPSSettingIntent);
						}
					}
			);
		
			alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.cancel();
				}
		});
		
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
	
	
	/**
	 * Determines whether or not location via <code>GPS_PROVIDER</code> or <code>NETWORK_PROVIDER</code> has been enabled on the device
	 * @return boolean
	 */
	public boolean isLocationEnabled(){
		boolean enabled = false;
		LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
				locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			enabled = true;
		}
		
		return enabled;
						
	}	
	
	/**
	 * Clear only the points graphic layer.
	 */
	public void clearPointsGraphicLayer(){
		_pointsGraphicsLayer.removeAll();
	}
	
	/**
	 * Clear only the draw graphic layer
	 */
	public void clearDrawGraphicLayer(){
		_drawGraphicsLayer.removeAll();
	}
	
	/**
	 * Clear graphics from all layers. Full reset.
	 */
	public void clearAllGraphics(){
		_drawGraphicsLayer.removeAll();	
		_pointsGraphicsLayer.removeAll();		
	}
	
	/**
	 * Overloaded pause method used to ensure the MapView used internally is paused correctly.
	 */
	public void pause(){
		_mapView.pause();
	}
	
	/**
	 * Overloaded unpause method used to ensure the MapView used internally is unpaused correctly.
	 */
	public void unpause(){
		_mapView.unpause();
	}
	
	////////////////////////////
	////	
	//// VIEW LISTENERS
	////
	////////////////////////////		
	
	/**
	 * Restores the MapOnTouchListener to its default 
	 */
	public void setDefaultTouchListener(){
		MapOnTouchListener ml = new MapOnTouchListener(getContext(), _mapView);
		_mapView.setOnTouchListener(ml);
	}
	
	/**
	 * Set the MyTouchListener which overrides various user touch events.<br><br>
	 * REQUIRED to use with <code>setDrawType()</code>
	 * @see setDrawType
	 */
	public void setDrawTouchListener(){
		_myTouchListener = new MyTouchListener(getContext(), _mapView);
		_mapView.setOnTouchListener(_myTouchListener);
	}	
	
	////////////////////////////
	////	
	//// PUBLIC PROPERTIES
	////
	////////////////////////////		
	
	
	/**
	 * Helper method that returns a reference to the INTERNAL MapView instance
	 * @return MapView
	 */
	public MapView getMapView(){
		return _mapView;
	}
	
	/**
	 * Helper method that returns whether or not the location service has been started.
	 * @return boolean
	 */
	public boolean isLocationStarted(){
		boolean test = false;
		
		if(_locationService != null){
			if(_locationService.isStarted())test = true;
		}
		return test;
	}
	
	/**
	 * Helper method that returns a reference to the INTERNAL Streets layer. 
	 * @return the layer or null
	 */
	public ArcGISTiledMapServiceLayer getStreetsLayer(){
		return _streets;
	}

	/**
	 * Helper method that returns a reference to the INTERNAL Topo layer.
	 * @return the layer or null
	 */
	public ArcGISTiledMapServiceLayer getTopoLayer(){
		return _topo;
	}
	
	/**
	 * Helper method that returns a reference to the INTERNAL Satellite layer.
	 * @return the layer or null
	 */
	public ArcGISTiledMapServiceLayer getSatelliteLayer(){
		return _satellite;
	}
	
	/**
	 * Helper method that returns a reference to the EsriQuickStartLib points graphics layer
	 * @return GraphicsLayer
	 */
	public GraphicsLayer getPointsLayer(){
		return _pointsGraphicsLayer;
	}
	
	/**
	 * Helper method that returns a reference to the EsriQuickStartLib drawing graphics layer
	 * @return GraphicsLayer
	 */
	public GraphicsLayer getDrawLayer(){
		return _drawGraphicsLayer;
	}
	
	
	/**
	 * Public property for detecting if a base map has been loaded.
	 * @return boolean
	 */
	public boolean isMapLoaded(){
		return _isMapLoaded; 
	}	
	
	/**
	 * This enum specifies default maps that are available through this library.
	 */
	public enum MapType{
		/**
		 * Street map world
		 */
		STREETS("http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"),
		/**
		 * Topographic map world
		 */
		TOPO("http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer"),
		/**
		 * Satellite imagery map world
		 */
		SATELLITE("http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
		
		private String url;
		private MapType(String url){
			this.url = url;
		}
		
		/**
		 * Allows you to retrieve the URL for each MapType enum
		 * @return URL String
		 */
		public String getURL(){
			return url;
		}	
	}
	
	/**
	 * This enum specifies default drawing types that are available through this library. 
	 */
	public enum DrawType{
		POINT, POLYLINE, POLYGON
	}	
	
	////////////////////////////
	////	
	//// ADDRESS LOCATOR AND REVERSE GEOCODER
	////
	////////////////////////////
	
	/**
	 * This enum provides several different geocoder options.
	 */
	public enum Geocoder{
		/**
		 * North America specific geocoder. URL available via <code>getURL()</code>.
		 */
		TA_ADDRESS_NA("http://tasks.arcgisonline.com/ArcGIS/rest/services/Locators/TA_Address_NA_10/GeocodeServer"),
		/**
		 * World geocoder URL available via <code>getURL()</code>.
		 */
		ESRI_PLACES_WORLD("http://tasks.arcgisonline.com/ArcGIS/rest/services/Locators/ESRI_Places_World/GeocodeServer");

		private String url;
		private Geocoder(String url){
			this.url = url;
		}
		
		/**
		 * Allows you to retrieve the URL for each geocoder enum
		 * @return URL String
		 */
		public String getURL(){
			return url;
		}
	}
	
	/**
	 * Helper method that finds an address by x and y. this is usually derived by a user clicking on the map
	 * and no conversion is needed. <br><br>
	 * For example: public void onSingleTap(final float x, final float y) {...}<br><br>
	 * Uses <code>ReverseGeocoder</code> which runs asynchronously. 
	 * Listen for events via <code>MapViewEvent.REVERSEGEOCODE_ADDRESS_COMPLETE</code> internally and
	 * <code>onAddressResultEvent</code> externally.
	 * @param x a screen point
	 * @param y a screen point
	 * @see dispatchGeocodeEvent 
	 * @see onAddressResultEvent
	 */
	@SuppressWarnings("unchecked")
	public void findAddressByXY(float x, float y){
		HashMap<String, Double> addressFields = new HashMap<String, Double>();
		addressFields.put("X", (double)x);
		addressFields.put("Y", (double)y);	
		addressFields.put("useLatLon", (double)0.0); //false

		new ReverseGeocoder(Geocoder.TA_ADDRESS_NA).execute(addressFields);
	}
	
	/**
	 * Helper method that finds an address by latitude and longitude. We use double to accommodate high-precision
	 * lat/lons.<br><br>
	 * Uses <code>ReverseGeocoder</code> which runs asynchronously. 
	 * Listen for events via <code>MapViewEvent.REVERSEGEOCODE_ADDRESS_COMPLETE</code> internally and
	 * <code>onAddressResultEvent</code> externally.	 * 
	 * @param latitude
	 * @param longitude
	 * @see dispatchGeocodeEvent 
	 * @see onAddressResultEvent	 * 
	 */
	@SuppressWarnings("unchecked")
	public void findAddressByLatLon(double latitude, double longitude){
		HashMap<String, Double> addressFields = new HashMap<String, Double>();
		addressFields.put("X", latitude);
		addressFields.put("Y", longitude);	
		addressFields.put("useLatLon", 1.0); //true

		new ReverseGeocoder(Geocoder.TA_ADDRESS_NA).execute(addressFields);
	}
	
	/*
	 * Executes a reverse geocode task asynchronously. Converts x/y to address. Extends AsyncTask.<br>
	 * Listen for results via <code>MapViewEvent.ADDRESS_COMPLETE</code> and
	 * exceptions via <code>MapViewEvent.ADDRESS_EXCEPTION</code>
	 */
	public class ReverseGeocoder
			extends
			AsyncTask<java.util.Map<java.lang.String, Double>, Void, LocatorReverseGeocodeResult> {

		private Locator _addressLocator;

		/**
		 * Set the type of geocoder that you wish to use.
		 * @param geocoder Geocoder enum
		 */
		public ReverseGeocoder(Geocoder geocoder){			
			_addressLocator =new Locator(geocoder.getURL());
		}
		
		@Override
		protected void onPostExecute(LocatorReverseGeocodeResult result) {			
			QSEvent.dispatchGeocodeEvent(new EsriQuickStartEvent(MapViewEventType.REVERSEGEOCODE_ADDRESS_COMPLETE),result,null);	
			_addressLocator = null;
		}

		protected LocatorReverseGeocodeResult doInBackground(Map<String, Double>... params) {

			SpatialReference sr;
			final Point point;
			LocatorReverseGeocodeResult result = null;
			if(params[0].get("useLatLon") == 1.0){
				sr = SpatialReference.create(4326);
				Point latlon = new Point(params[0].get("Y"),params[0].get("X"));	
				point = (Point)GeometryEngine.project(latlon,SpatialReference.create(4326), _mapView.getSpatialReference());
			}
			else{
				sr = _mapView.getSpatialReference();
				point = _mapView.toMapPoint(params[0].get("X").floatValue(),params[0].get("Y").floatValue());	
			} 
			
			try {

				/*
				 * API v1.1 method signature changed to take SpatialReference
				 * parameter instead of int wkid.
				 */
				result = _addressLocator.reverseGeocode(point, 1000.00, sr, sr);
			} catch (Exception e) { 
				QSEvent.dispatchGeocodeEvent(new EsriQuickStartEvent(MapViewEventType.REVERSEGEOCODE_ADDRESS_EXCEPTION),(LocatorReverseGeocodeResult)null,e.toString());
			}
			return result; 
		}

	}	
	
	/**
	 * Helper method that takes an address as input and then runs it through a Locator service. <br><br>
	 * Uses <code>AddressLocator</code> which runs asynchronously. 
	 * Listen for results via MapViewEvent.ADDRESS_COMPLETE and for exceptions via MapViewEvent.ADDRESS_EXCEPTION
	 * 
	 * @param address Can be comma delimited or single line
	 */
	@SuppressWarnings("unchecked")
	public void findAddress(String address) {

		try {
			
			HashMap<String, String> addressFields = new HashMap<String, String>();
			// get the user entered address and create arcgis
			// locator address fields
			//String address = edtText.getText().toString();

			StringTokenizer st = new StringTokenizer(address, ",");

			if (st.countTokens() == 4) {

				addressFields.put("Address", st.nextToken());
				addressFields.put("City", st.nextToken());
				addressFields.put("State", st.nextToken());
				addressFields.put("Zip", st.nextToken());

			} else if (st.countTokens() == 3) {

				addressFields.put("Address", st.nextToken());
				addressFields.put("City", st.nextToken());
				addressFields.put("State", st.nextToken());

			} else {
				addressFields.put("SingleLine", address);
			}

			if (address != null) {

				if(!address.trim().equalsIgnoreCase("")){
					new AddressLocator(Geocoder.ESRI_PLACES_WORLD).execute(addressFields);
				}
				else{
					//displayToast("Please enter address in correct format.");
					QSEvent.dispatchAddressEvent(new EsriQuickStartEvent(MapViewEventType.ADDRESS_EXCEPTION),(java.util.List<LocatorGeocodeResult>)null,
							"Please enter address in correct format.");					
				}

			}
			else{
				//displayToast("Please enter address in correct format.");
				QSEvent.dispatchAddressEvent(new EsriQuickStartEvent(MapViewEventType.ADDRESS_EXCEPTION),(java.util.List<LocatorGeocodeResult>)null,
						"Please enter address in correct format.");
			}

		} catch (Exception e) {
			QSEvent.dispatchAddressEvent(new EsriQuickStartEvent(MapViewEventType.ADDRESS_EXCEPTION),(java.util.List<LocatorGeocodeResult>)null,e.toString());
		}
	}	
	
	/*
	 * Executes geocode task asynchronously. Extends AsyncTask.
	 * Listen for results via <code>MapViewEvent.ADDRESS_COMPLETE</code> and
	 * exceptions via <code>MapViewEvent.ADDRESS_EXCEPTION</code>
	 */
	public class AddressLocator
			extends
			AsyncTask<java.util.Map<java.lang.String, java.lang.String>, Void, java.util.List<LocatorGeocodeResult>> {
		
		private Locator _addressLocator;
		
		public AddressLocator(Geocoder geocoder){
			_addressLocator =new Locator(geocoder.getURL());
		}
		
		@Override
		protected void onPostExecute(java.util.List<LocatorGeocodeResult> result) {			
			QSEvent.dispatchAddressEvent(new EsriQuickStartEvent(MapViewEventType.ADDRESS_COMPLETE),result,null);								
			_addressLocator = null;
		}

		@Override
		protected List<LocatorGeocodeResult> doInBackground(
				Map<String, String>... params) {

			SpatialReference sr = _mapView.getSpatialReference();
			List<LocatorGeocodeResult> results = null;
			
			//_addressLocator =new Locator(LOCATOR_ENDPOINT_NA);
			_addressLocator =new Locator(Geocoder.ESRI_PLACES_WORLD.getURL());
			
			try {

				/*
				 * API v1.1 method signature changed to take SpatialReference
				 * parameter instead of int wkid.
				 */
				// perform geocode operation
				results = _addressLocator.geocode(params[0], null, sr);
				
			} catch (Exception e) {
				QSEvent.dispatchAddressEvent(new EsriQuickStartEvent(MapViewEventType.ADDRESS_EXCEPTION),(java.util.List<LocatorGeocodeResult>)null,e.toString());	
			}
			return results; 
		}
	}		
	
	/**
	 * Adds the eventListenerList for EsriQuickStart
	 * @param listener
	 */
	public void addEventListener(EsriQuickStartEventListener listener){
		QSEvent.eventListenerList.add(EsriQuickStartEventListener.class, listener);
	}
	
	/**
	 * Removes the eventListenerList for EsriQuickStart
	 * @param listener
	 */
	public void removeEventListener(EsriQuickStartEventListener listener){
		QSEvent.eventListenerList.remove(EsriQuickStartEventListener.class, listener);
	}		
	
	////////////////////////////
	////	
	//// CUSTOM TOUCH LISTENER CLASS
	////
	////////////////////////////
	
	/*
	 * Override MapView's touch listener in order to implement drawing capabilities.<br><br>
	 * Use <code>setMyTouchListener()</code> to start or override events,
	 * and <code>setDrawType()</code>.
	 * @see setMyTouchListener
	 * @see setDrawType 
	 */
	public class MyTouchListener extends MapOnTouchListener {
		// ArrayList<Point> polylinePoints = new ArrayList<Point>();

		private MultiPath poly;
		//String type = "";
		private DrawType drawType = null;
		private Point startPoint = null;
		private Button _clearButton;
		private SimpleFillSymbol _simpleFillSymbol = null;

		/**
	 	 * Override MapView's touch listener.<br><br>
	 	 * Use <code>setMyTouchListener()</code> to start or override events,
	 	 * and <code>setDrawType()</code>.
		 * @param context
		 * @param view
		 * @see setMyTouchListener
		 * @see setDrawType
		 */
		public MyTouchListener(Context context, MapView view) {
			super(context, view);
			_simpleFillSymbol = new SimpleFillSymbol(Color.RED);
			_simpleFillSymbol.setAlpha(100);
		}

		/**
		 * Sets the type of object that you want to draw.
		 * @param drawType
		 */
		public void setDrawType(DrawType drawType){
			this.drawType = drawType;
		}

		/**
		 * Gets the current drawing type for the drawing tool.
		 * @return
		 */
		public DrawType getDrawType() {
			return this.drawType;
		}
		
		
		/**
		 * Let's you pass in a reference to a button that you want to use
		 * to clear the graphics layer used for drawing. <code>MyTouchListener</code>
		 * will toggle it on via <code>setEnabled(true)</code> when the user draws something.
		 * <br><br>
		 * <b>IMPORTANT:</b> If you don't do this you'll get a nullPointerException.
		 * @param button
		 * @see clearDrawGraphicsLayer
		 */
		public void setDrawClearButton(Button button){
			_clearButton = button;
		}
		
		/**
		 * Clears all drawing graphics.
		 */
		public void clearDrawGraphicsLayer(){
			_drawGraphicsLayer.removeAll();
		}
		
		/**
		 * Clears ALL graphics.
		 * Added at v1.2.
		 */
		public void clearAllGraphics(){
			_drawGraphicsLayer.removeAll();
			_pointsGraphicsLayer.removeAll();
			
		}

		/*
		 * Invoked when user single taps on the map view. This event handler
		 * draws a point at user-tapped location, only after "Draw Point" is
		 * selected from Spinner. 
		 * 
		 * @see
		 * com.esri.android.map.MapOnTouchListener#onSingleTap(android.view.
		 * MotionEvent)
		 */
		public boolean onSingleTap(MotionEvent e) {
			if (drawType.equals(DrawType.POINT)) {
				_drawGraphicsLayer.removeAll();
				Graphic graphic = new Graphic(_mapView.toMapPoint(new Point(e.getX(), e
						.getY())),new SimpleMarkerSymbol(Color.RED,25,STYLE.DIAMOND));
				//graphic.setGeometry();
				_drawGraphicsLayer.addGraphic(graphic);
				
				setDefaultTouchListener();
				return true;
			}
			return false;

		}
		

		/*
		 * Invoked when user drags finger across screen. Polygon or Polyline is
		 * drawn only when right selected is made from Spinner
		 * 
		 * @see com.esri.android.map.MapOnTouchListener#onDragPointerMove(android.view.MotionEvent, 
		 * android.view.MotionEvent)
		 */
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			
			if (drawType.equals(DrawType.POLYLINE)){
				MultiPath polyline = new Polyline();
				Point mapPt = _mapView.toMapPoint(to.getX(), to.getY());
				startPoint = _mapView.toMapPoint(from.getX(), from.getY());
				polyline.startPath((float) startPoint.getX(),
						(float) startPoint.getY());

				polyline.lineTo((float) mapPt.getX(), (float) mapPt.getY());
				
				_drawGraphicsLayer.addGraphic(new Graphic(polyline,new SimpleLineSymbol(Color.BLUE,5)));
				
				return true;
			}
			
			if (drawType.equals(DrawType.POLYGON)) {
				
				Point mapPt = _mapView.toMapPoint(to.getX(), to.getY());

				if (startPoint == null) {
					_drawGraphicsLayer.removeAll();
					poly = new Polygon();
					startPoint = _mapView.toMapPoint(from.getX(), from.getY());
					poly.startPath((float) startPoint.getX(),
							(float) startPoint.getY());
				}

				poly.lineTo((float) mapPt.getX(), (float) mapPt.getY());
				
				_drawGraphicsLayer.addGraphic(new Graphic(poly,_simpleFillSymbol));
				
				return true;
			}
			return super.onDragPointerMove(from, to);
		}

		@Override
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			if (drawType.equals(DrawType.POLYLINE) || drawType.equals(DrawType.POLYGON)) {

				/*
				 * When user releases finger, add the last point to polyline.
				 */
				if (drawType.equals(DrawType.POLYGON)) {
					poly.lineTo((float) startPoint.getX(),
							(float) startPoint.getY());
					_drawGraphicsLayer.removeAll();
					_drawGraphicsLayer.addGraphic(new Graphic(poly,_simpleFillSymbol));
					
				}

				startPoint = null;
				setDefaultTouchListener();
				return true;
			}
			return super.onDragPointerUp(from, to);
		}
	}	
	
}
