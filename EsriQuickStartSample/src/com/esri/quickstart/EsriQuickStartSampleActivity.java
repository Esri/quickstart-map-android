package com.esri.quickstart;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

import com.esri.quickstart.EsriQuickStart;
import com.esri.quickstart.EsriQuickStart.MapType;
import com.esri.quickstart.controller.EsriQuickStartActivityController;

/**
 * 
 * @author Andy Gup
 * @version 1.1
 *
 */
public class EsriQuickStartSampleActivity extends Activity {
	
	ArcGISTiledMapServiceLayer tileLayer;
	private EsriQuickStart _esriQuickStartLib = null;
	protected EsriQuickStartActivityController _activityController;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		_esriQuickStartLib = new EsriQuickStart(this,R.id.map);
		_esriQuickStartLib.addLayer(MapType.STREETS, null, null, null,false);
		_esriQuickStartLib.addLayer(MapType.SATELLITE, null, null, null,false);
		_esriQuickStartLib.addLayer(MapType.TOPO, null, null, null, true);
		
        _activityController = new EsriQuickStartActivityController(_esriQuickStartLib, this);
		_activityController.setMapSpinner();		
		_activityController.setMapListeners();		
    }

	/**
	 * Handles showDialog() at the Activity level
	 */
    protected Dialog onCreateDialog(int id){
		Dialog dialog = null;
		dialog = _activityController.onCreateDialogHandler(id);
		return dialog;
	}
    
	@Override 
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		_esriQuickStartLib.pause();
		
		//Save battery and stop GPS
		if(_esriQuickStartLib != null)_esriQuickStartLib.stopLocationService();				
	}
	
	@Override 	
	protected void onResume() {
		super.onResume(); 
		_esriQuickStartLib.unpause();
		
		//Start GPS using multi-threaded best practices. This will help
		//minimize future problems related to handling GPS across multiple Activities
		if(_esriQuickStartLib != null)_esriQuickStartLib.delayedStartLocationService(false);				
	}

}