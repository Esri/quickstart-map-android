package com.esri.template;

import android.app.Activity;
import android.os.Bundle;

import com.esri.quickstart.EsriQuickStart;
import com.esri.quickstart.EsriQuickStart.MapType;


public class EsriQuickStartTemplateActivity extends Activity {
	
	EsriQuickStart _esriQuickStartLib = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		_esriQuickStartLib = new EsriQuickStart(this,R.id.map);
		_esriQuickStartLib.addLayer(MapType.STREETS, null, null, null,true);
    }

	@Override 
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		_esriQuickStartLib.pause();
		super.onPause();
	}
	
	@Override 	protected void onResume() {
		super.onResume(); 
		_esriQuickStartLib.unpause();
	}

}