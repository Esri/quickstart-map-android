package com.esri.quickstart.controller;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.geocode.LocatorReverseGeocodeResult;
import com.esri.quickstart.EsriQuickStart;
import com.esri.quickstart.EsriQuickStart.DrawType;
import com.esri.quickstart.EsriQuickStart.MapType;
import com.esri.quickstart.EsriQuickStartEvent;
import com.esri.quickstart.EsriQuickStartEventListener;
import com.esri.quickstart.R;

public class EsriQuickStartActivityController {
	
	private Activity _activity;
	private EsriQuickStart _quickStartLib;
	private Spinner _spinner;
	EditText _editText;	
	private GraphicsLayer _pointsGraphicsLayer;
	private GraphicsLayer _drawGraphicsLayer;
	private MapView _mapView;
	
	ProgressDialog _dialog = null;	
	ProgressBar _progressBar;
	private Callout _callout;
	
	Button goDirectionsButton;
	ImageView restartGPSButton;
	ImageView goGeocodeButton;
	ImageView drawButton;
	ImageView clearButton;
	ImageView shareButton;
	
	static final String _DEFAULT_EMAIL_SUBJECT = "ArcGIS for Android Sample App";
	static final String _DEFAULT_EMAIL_TEXT = "Get the code at: http://arcgis.com";
	static final int CLOSE_LOADING_WINDOW = 0;
	static final int CANCEL_LOADING_WINDOW = 1;
	static final int OPEN_ADDRESS_WINDOW = 3;
	static final int CLOSE_ADDRESS_WINDOW = 4;
	static final int OPEN_DRAW_WINDOW = 5;	
	Timer _cancelLocate = new Timer();	
	final String[] _geometryTypes = new String[] { "Point", "Polyline","Polygon" };
	int _selectedGeometryIndex = -1;
	
	public EsriQuickStartActivityController(EsriQuickStart quickStart,Activity activity){
		_activity = activity; 	
		_quickStartLib = quickStart;
		_pointsGraphicsLayer = _quickStartLib.getPointsLayer();
		_drawGraphicsLayer = _quickStartLib.getDrawLayer();	
		_mapView = _quickStartLib.getMapView();
		setUI();
		setEventListeners();
		setButtons();
	}
	
	private void setUI(){
		_editText = (EditText) _activity.findViewById(R.id.addressText);		
		_progressBar = (ProgressBar) _activity.findViewById(R.id.progress_bar);
		_progressBar.setVisibility(View.GONE);
	}
	
	/**
	 * Sets conditions for all the user interface buttons
	 */
	public void setButtons(){
		clearButton = (ImageView) _activity.findViewById(R.id.clearbutton);
		clearButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_pointsGraphicsLayer.removeAll();
				_drawGraphicsLayer.removeAll();
				_quickStartLib.displayToast("Deleting all map graphics.");
			}
		});
		
		goGeocodeButton = (ImageView) _activity.findViewById(R.id.executeGeocode);
		goGeocodeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				_progressBar.setVisibility(View.VISIBLE);
				_pointsGraphicsLayer.removeAll();
				_mapView.getCallout().hide();
				String address = _editText.getText().toString();
				_quickStartLib.stopLocationService();
				_quickStartLib.findAddress(address);
			}
		});
		
		//NOTE: For more complex editing, such as editing attributes or points
		//within a polygon see the Editing samples in the SDK
		drawButton = (ImageView) _activity.findViewById(R.id.drawSpinner);
		drawButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO replace with DialogFragment
				_activity.showDialog(OPEN_DRAW_WINDOW); 
			}
		});
		
//		goDirectionsButton = (Button) _activity.findViewById(R.id.goDirections);
//		goDirectionsButton.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				_activity.showDialog(OPEN_ADDRESS_WINDOW); //TODO replace with DialogFragment
//			}
//		});
		
		shareButton = (ImageView) _activity.findViewById(R.id.share);
		shareButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shareMessage(_DEFAULT_EMAIL_SUBJECT,_DEFAULT_EMAIL_TEXT);
			}
		});
		
		//restartGPSButton = (Button) _activity.findViewById(R.id.restartGPS);
		restartGPSButton = (ImageView) _activity.findViewById(R.id.restartGPS);
		restartGPSButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(_quickStartLib.isLocationStarted() == false){
					_quickStartLib.delayedStartLocationService(false);	
				}
				else{
					_quickStartLib.stopLocationService();
				}
			}
		});
	}	
	
	/**
	 * Sets up a map spinner. <br><br>
	 * <b>REQUIRES: </b> <code>EsriQuickStartLib</code>.
	 */
	public void setMapSpinner(){
		_spinner = (Spinner)_activity.findViewById(R.id.mapSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(_activity, R.array.maps, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinner.setAdapter(adapter);
		_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				switch(arg2){
				case 0:
					_quickStartLib.setBaseMapVisibility(MapType.STREETS);
					break;
				case 1:
					_quickStartLib.setBaseMapVisibility(MapType.SATELLITE);
					break;
				case 2:
					_quickStartLib.setBaseMapVisibility(MapType.TOPO);
					break;
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});			
	}
	
	public void addressResultsHandler(java.util.List<LocatorGeocodeResult> result){
		if (result.size() == 0) {
			Toast toast = Toast.makeText(_activity,
					"No result found.", Toast.LENGTH_SHORT);
			toast.show();

		} else {
			_dialog = ProgressDialog.show(_activity, "",
					"Loading. Please wait...", true, true);
			_cancelLocate = new Timer();
			_cancelLocate.schedule(new TimerTask() {

				@Override
				public void run() {
					uiHandler.sendEmptyMessage(CANCEL_LOADING_WINDOW);
				}

			}, 60000);

			// Create graphic to add locator result to map

			Geometry geomS = result.get(0).getLocation();
			SimpleMarkerSymbol smsS = new SimpleMarkerSymbol(Color.RED, 20,
					SimpleMarkerSymbol.STYLE.DIAMOND);
			Graphic graphicS = new Graphic(geomS, smsS);

			Geometry geomT = result.get(0).getLocation();
			TextSymbol tsT = new TextSymbol(20, result.get(0).getAddress(),
					Color.BLACK);
			tsT.setOffsetX(0);
			tsT.setOffsetY(30);
			Graphic graphicT = new Graphic(geomT, tsT);

			/*
			 * add the updated graphic to graphics layer and display the
			 * result on the map
			 */
			_pointsGraphicsLayer.addGraphic(graphicS);
			_pointsGraphicsLayer.addGraphic(graphicT);

			// zoom to the locator result
			_mapView.zoomTo(result.get(0).getLocation(), 2);

			uiHandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);

		}
	}	
	
	/**
	 * Encapsulates initializing various map listeners
	 */
	public void setMapListeners(){

		_mapView.setOnPanListener(new OnPanListener() {
			
			private static final long serialVersionUID = 3350423096166771396L;

			@Override
			public void prePointerUp(float fromx, float fromy, float tox, float toy) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void prePointerMove(float fromx, float fromy, float tox, float toy) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postPointerUp(float fromx, float fromy, float tox, float toy) {
				if(_quickStartLib.isLocationStarted() == true){
					_quickStartLib.stopLocationService();
					_quickStartLib.displayToast("GPS disabled when you pan. Use button to restart.");
					
				}
			}
			
			@Override
			public void postPointerMove(float fromx, float fromy, float tox, float toy) {
				// TODO
			}
		});
		
		// perform reverse geocode on single tap.		
		_mapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;					
			
			public void onSingleTap(final float x, final float y) {

				if(_callout != null){
					_callout.hide(); 
				}
				else
				{
					_callout = _mapView.getCallout();
					_callout.setStyle(R.xml.calloutstyle);
				}

				Graphic graphic = _quickStartLib.findClosestGraphic(x, y, _quickStartLib.getPointsLayer(),25);
				if(graphic == null){
				//	graphicsLayer.removeAll();
					_quickStartLib.findAddressByXY(x, y);
				}
				else{
					String message = new String();
					Map<String,Object> atts = graphic.getAttributes();
					for(Map.Entry<String,Object> entry : atts.entrySet()){
						String key = entry.getKey();
						Object value = entry.getValue();
						message = message + key + ": " + value +"\n";
					}
					Point location = (Point) graphic.getGeometry();

					_callout.setOffset(0, -15);
					_callout.show(location, message(message));
				}

			}
		});
	}	
	
	/**
	 * Trigger a send email popup
	 * @param bodyText
	 */
	private void shareMessage(String subject, String bodyText){
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		shareIntent.setType("plain/text"); 
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, bodyText);
		
		_activity.startActivity(shareIntent);   
	}	
	
	/**
	 * These are the eventlisteners associated with EsriQuickStart
	 */
	protected void setEventListeners(){
		
		_quickStartLib.addEventListener(new EsriQuickStartEventListener() {
						
			@Override
			public void onStatusEvent(EsriQuickStartEvent event, String status,
					Object source) {
				Log.d("test2","Satellite Layer exists: " + _quickStartLib.layerExists(MapType.SATELLITE));
				Log.d("test2","Topo Layer exists: " + _quickStartLib.layerExists(MapType.TOPO));
				
	        	String layerName = _quickStartLib.getCurrentVisibleLayerName();

				if(layerName != null){
					if(layerName.contains("world street map")){
						_spinner.setSelection(0);
					}						
					if(layerName.contains("world imagery")){
						_spinner.setSelection(1);
					}
					if(layerName.contains("topographic info")){
						_spinner.setSelection(2);
					}							
				}	 
			
			}
			
			@Override
			public void onStatusErrorEvent(EsriQuickStartEvent event, String status) {
				Log.d("QuickStartController","Layer loading Error: " + status.toString());
			}
			
			@Override
			public void onLocationShutdownEvent(EsriQuickStartEvent event, String reason) {
				//restartGPSButton.setVisibility(View.VISIBLE);
				restartGPSButton.setImageResource(R.drawable.location32);
				_quickStartLib.displayToast("GPS has been shut off: " + reason);
			}
			
			@Override
			public void onLocationExceptionEvent(EsriQuickStartEvent event,
					String message) {
				_quickStartLib.displayToast("There was a GPS problem: " + message);
				//restartGPSButton.setVisibility(View.VISIBLE);
				restartGPSButton.setImageResource(R.drawable.location32);
			}
			
			@Override
			public void onLocationChangedEvent(EsriQuickStartEvent event,
					Location location) {
				String eventName = event.getSource().toString();
				Log.d("test", "EsriQuickStart: " + eventName);
				if(eventName.contains("INITIALIZED")){
					//restartGPSButton.setVisibility(View.INVISIBLE);
					restartGPSButton.setImageResource(R.drawable.location32yellow);
				}
			}
			
			@Override
			public void onAddressResultEvent(EsriQuickStartEvent event,
					LocatorReverseGeocodeResult result, String exception) {
				if(result != null){
					displayMapClickResults(result);	
				}
				else if(exception != null)
				{
					_quickStartLib.displayToast("There was an address problem: " + exception);
				}
				
			}
			
			@Override
			public void onAddressResultEvent(EsriQuickStartEvent event,
					List<LocatorGeocodeResult> result, String exception) {
				_progressBar.setVisibility(View.GONE);
				
				if(result != null){	
					addressResultsHandler(result);
				}
				else if(exception != null)
				{
					_quickStartLib.displayToast("There was an address problem: " + exception);
				}

			}

		});
		
	}
	
	/*
	 * Returns an AlertDialog that displays drawing options
	 * service
	 */
	protected Dialog setDrawDialogBox() {
		return new AlertDialog.Builder(_activity)
				.setTitle("Select Geometry")
				.setItems(_geometryTypes, 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							_drawGraphicsLayer.removeAll();
	
							// ignore first element
							Toast toast = Toast.makeText(_activity.getApplicationContext(),
									"", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.BOTTOM, 0, 0);
	
							// Get item selected by user.
							
							String geomType = _geometryTypes[which];
							_selectedGeometryIndex = which;
	
							// process user selection
							if (geomType.equalsIgnoreCase("Polygon")) {
								_quickStartLib.setDrawType(DrawType.POLYGON);
								toast.setText("Drag finger across screen to draw a Polygon. \nRelease finger to stop drawing.");
							} else if (geomType.equalsIgnoreCase("Polyline")) {
								_quickStartLib.setDrawType(DrawType.POLYLINE);
								toast.setText("Drag finger across screen to draw a Polyline. \nRelease finger to stop drawing.");
							} else if (geomType.equalsIgnoreCase("Point")) {
								_quickStartLib.setDrawType(DrawType.POINT);
								toast.setText("Tap on screen once to draw a Point.");
							}
	
//							final Button clearButton = (Button) _activity.findViewById(R.id.clearbutton);
//							if(clearButton.isEnabled() == false){
//								clearButton.setEnabled(true);	
//							}
							toast.show();
						}
				}).create();
	}	
	
	private void displayMapClickResults(LocatorReverseGeocodeResult result){
		// retrieve the user clicked location
		final Point loc = result.getLocation();
		
		try {
			// checks if State and Zip is present in the result
			if (result.getAddressFields().get("State").length() != 2
					&& result.getAddressFields().get("Zip").length() != 5) {

				_mapView.getCallout()
						.show(loc, message("No Address Found."));

			} else {

				// display the result in map callout
				String msg = "Address:"
						+ result.getAddressFields().get("Address")
						+ "\n" + "City:"
						+ result.getAddressFields().get("City") + "\n"
						+ " State:"
						+ result.getAddressFields().get("State") + "\n"
						+ " Zip:"
						+ result.getAddressFields().get("Zip");
				_mapView.getCallout().show(loc, message(msg));

			}
		} catch (Exception e) {

			e.printStackTrace();
			// map.getCallout().setAnchor(Callout.ANCHOR_POSITION_FLOATING);
			_mapView.getCallout().show(loc, message("No Address Found."));

		}		
	}	
	
	/**
	 * Create an onCreateDialog() method in the main Activity
	 * to listen for this event.
	 * @param id
	 * @return Dialog
	 */
	@SuppressWarnings("unused")
	public Dialog onCreateDialogHandler(int id){
		Dialog dialog = null;
		switch(id){
			case OPEN_ADDRESS_WINDOW:
				Context mContext = _activity;		
				dialog = new Dialog(mContext);
				dialog.setContentView(R.layout.address_dialog);
				dialog.setTitle(" ");
				dialog.setCancelable(true);
				Button button = (Button)dialog.findViewById(R.id.closeButton);
				final Dialog _tempDialog = dialog;
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						_tempDialog.dismiss();
					}
				});
	
				break;
			case CLOSE_ADDRESS_WINDOW:
				if(dialog != null){
					dialog.dismiss();
				}
				break;
				default: 
					dialog =null;
			case OPEN_DRAW_WINDOW:				
				dialog = setDrawDialogBox();
				dialog.show();
				if(_quickStartLib != null){
					_quickStartLib.setDrawTouchListener();
					//Button clearButton = (Button) findViewById(R.id.clearbutton);
					//_esriQuickStartLib.setClearButton(clearButton);
				}
				break;		
		}
		return dialog;
	}	
	
	/*
	 * Customize the map Callout text
	 */
	private TextView message(String text) {

		final TextView msg = new TextView(_quickStartLib.getContext());
		msg.setText(text);
		msg.setTextSize(12);
		msg.setTextColor(Color.BLACK);
		return msg;

	}	
	
	/**
	 * Customizes functions of the  dialog box
	 */
	private Handler uiHandler = new Handler(new Callback() {

		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CLOSE_LOADING_WINDOW:
				if (_dialog != null) {
					_dialog.dismiss();
				}
				_cancelLocate.cancel();
				break;
			case CANCEL_LOADING_WINDOW:
				if (_dialog != null) {
					_dialog.dismiss();
				}
				Toast toast = Toast.makeText(_activity,
						"Locate canceled", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			return false;
		}

	});	
}
