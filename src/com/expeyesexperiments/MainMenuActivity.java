package com.expeyesexperiments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import explib.devhandler;




public class MainMenuActivity extends Activity {
 
    private UsbManager mUsbManager;
    private devhandler mcp2200;
    
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
	Button capacitor_button,inductor_button,RLC_button,TandM_button,diodeIV_button,scope_button,logger_button,custom_button;
	Button RLCPhase_button,IC555_button;
	expeyesCommon ej;
	IntentFilter filter;
	public Builder about_dialog;
	PackageManager packageManager;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    //MenuItem refresh = menu.getItem(R.id.menu_refresh);
	    //refresh.setEnabled(true);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final boolean remote_available;
		final String appPackageName = "com.expeyesserver"; // getPackageName() from Context or Activity object
	    Builder dialog;
		switch(item.getItemId())	    

	    {
	    case R.id.menu_reconnect:
	    	askForPermission();
	    	break;
	    case R.id.credits:
	    	//display_about_dialog();
	    	about_dialog.show();
	    	break;
	    case R.id.remote:
	    	//display_about_dialog();
	        dialog = new AlertDialog.Builder(this);
	        
	        Intent intent = packageManager.getLaunchIntentForPackage(appPackageName);
	        if (intent != null)
	        {
	            /* we found the activity now start the activity */
	            remote_available=true;
	            dialog.setMessage("Feature moved to a separate application. Launch it?");
	        }
	        else
	        {
	        	remote_available=false;
	            dialog.setMessage("Feature moved to a separate application. Download Expeyes remote server from the Play store?");
	            
	        }
	        
	        
	        
	        
	        dialog.setTitle("Feature moved");
	        dialog.setCancelable(true);
	        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) { 
	    			if(!remote_available){
	    			try {
	    			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
	    			} catch (android.content.ActivityNotFoundException anfe) {
	    			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
	    			}}
	    			else{
	    				Intent intent = packageManager.getLaunchIntentForPackage(appPackageName);
	    				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	            startActivity(intent);
	    			}
	    			
	            }
	         })
	        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) { 
	                // do nothing
	            }
	         });
			dialog.show();
			
			
			

			
		    break;
	    }
	    return true;
	}
			
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		setTitle("ExpEYES Experiments ( Connect... )");
		
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		packageManager = getPackageManager();
        
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mcp2200 = new devhandler(mUsbManager);
        
        if(mcp2200.device_found )mUsbManager.requestPermission(mcp2200.mDevice, mPermissionIntent);
        else Toast.makeText(getBaseContext(),"No device connected. check connections.",Toast.LENGTH_SHORT).show();
	
        
        about_dialog = new AlertDialog.Builder(this);
        about_dialog.setMessage("e-mail:jithinbp@gmail.com.\nhttps://github.com/jithinbp\nWould like to thank Dr. Ambar Chatterjee\nfor helping out with curve fitting routines");
        about_dialog.setTitle("Developed by Jithin B.P");
        about_dialog.setCancelable(true);
        ej=expeyesCommon.getInstance();
        
        
        
	}
	@Override
	public void onResume(){
		super.onResume();
		addListenersOnButtons();
	}
	@Override
	public void onDestroy(){
	       super.onDestroy();
	       unregisterReceiver(mUsbReceiver);
	}

	
	public void addListenersOnButtons() {
 		final Context context = this;
 		capacitor_button = (Button) findViewById(R.id.RC);
 		capacitor_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 				Intent intent = new Intent(context, RCCircuits.class);
                startActivity(intent);   
                
 			}
 		});

		inductor_button = (Button) findViewById(R.id.RL);
 		inductor_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, RLCircuits.class);
                startActivity(intent);   
 			}
 		});

		RLC_button = (Button) findViewById(R.id.RLC);
 		RLC_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, RLCCircuits.class);
                startActivity(intent);   
 			}
 		});
		RLCPhase_button = (Button) findViewById(R.id.RLCPhase);
 		RLCPhase_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, PhaseShift.class);
                startActivity(intent);   
 			}
 		});
		TandM_button = (Button) findViewById(R.id.TandM);
 		TandM_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, TestAndMeasurement.class);
                startActivity(intent);   
 			}
 		});
 		 		
		diodeIV_button = (Button) findViewById(R.id.DIODE);
 		diodeIV_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, DiodeIV.class);
                startActivity(intent);   
 			}
 		});
 		
		scope_button = (Button) findViewById(R.id.SCOPE);
 		scope_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, Oscilloscope.class);
                startActivity(intent);   
 			}
 		}); 		
		
		logger_button = (Button) findViewById(R.id.LOGGER);
 		logger_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, DataLogger.class);
                startActivity(intent);   
 			}
 		}); 	

		custom_button = (Button) findViewById(R.id.CUSTOM);
 		custom_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, Custom.class);
                startActivity(intent);   
 			}
 		}); 	
		IC555_button = (Button) findViewById(R.id.IC555);
 		IC555_button.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				if(!ej.connected){Toast.makeText(getBaseContext(),"No device connected. Open menu and reconnect.",Toast.LENGTH_SHORT).show();return;}
 			    Intent intent = new Intent(context, IC555.class);
                startActivity(intent);   
 			}
 		}); 		
	
	}	
 		
 	
	
	
	public void askForPermission(){
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
	
        mcp2200 = new devhandler(mUsbManager);
        
        if(mcp2200.device_found )mUsbManager.requestPermission(mcp2200.mDevice, mPermissionIntent);
        else{
	    	
        	Toast.makeText(getBaseContext(),"No device connected. check connections.",Toast.LENGTH_SHORT).show();
        }
		
	}	

	
	
	
 		
 		/*---------------------REQUEST USB PERMISSION WITHIN THE APPLICATION--------------------------*/
 		
 		private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

 		    public void onReceive(Context context, Intent intent) { //called when permission request reply received
 		        String action = intent.getAction();
 		        if (ACTION_USB_PERMISSION.equals(action)) {
 		            synchronized (this) {
 		            	if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ) { //permission granted
 		                    if(mcp2200.mDevice != null){
 		                      //call method to set up device communication singleton class
 		                    	
 		                    	
 		                    	if(ej.open_device(mcp2200)){
 		                    			//buttons will be inactive till the device is ready
 		                    			setTitle(ej.title+ej.ej.version);
 		                    			Toast.makeText(getBaseContext(),"Device found!!",Toast.LENGTH_SHORT).show();
 		                    	}
 		                    	else{
 		                    		Toast.makeText(getBaseContext(),"Something went wrong!!  Reconnect device",Toast.LENGTH_SHORT).show();
 		                    	}
 		                   }
 		                    else{
 		                    	Toast.makeText(getBaseContext(),"No device connected. check connections.",Toast.LENGTH_LONG).show();
 		                    }
 		                } 
 		                else {																		//permission denied
 	            	    	Toast.makeText(getBaseContext(),"Please grant permissions to access the device",Toast.LENGTH_LONG).show();
 		                   //Log.d("UH-OH", "permission denied for device " + mcp2200.mDevice);
 		                }
 		            }
 		        }
 		    }
 		};
 		
 		/*-----------------------------------------------*/

 		
 		
 		
	
	}
 
	
	
	
	
	
