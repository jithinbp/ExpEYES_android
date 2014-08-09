package com.expeyesexperiments;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import explib.devhandler;
import explib.ejlib;

public class expeyesCommon{
	   private static expeyesCommon instance = null;
	   public long timestamp;
	   public String title="ExpEYES : ";
	    private devhandler mcp2200;
	    public ejlib ej;
	    public boolean connected=false;
	    public DecimalFormat df1 = new DecimalFormat("#.#");
	    public DecimalFormat df2 = new DecimalFormat("#.##");
	    public DecimalFormat df3 = new DecimalFormat("#.###");
	    public DecimalFormat df6 = new DecimalFormat("#.######");
	    
	    protected expeyesCommon() {
	      // Exists only to defeat instantiation.
		   timestamp = System.currentTimeMillis();
	   }
	    public boolean open_device(devhandler dev){
	    	mcp2200 = dev;
	    	ej = new ejlib(mcp2200);
	    	
         	if(!ej.open()){
         		Log.e("ERROR",ej.message);  
         		return false;}
         	Log.e("VERSION",ej.version+"");
         	ej.disable_actions();
 	    	ej.set_trigval(1, 0.0);
 	    	connected=true;
 	    	return true;
	    }
	    
	    public static expeyesCommon getInstance() {
	      if(instance == null) {
	         instance = new expeyesCommon();
	      }
	      return instance;
	   }
	    

	    public Builder makeReconnectDialog(Context context){
	        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
	        builder1.setMessage("Device disconnected. Return to main menu and reconnect.");
            builder1.setCancelable(true);
            builder1.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
	        return builder1;
		    }
	    
		   public int toInt(String txt) {
				if (txt == null || txt.isEmpty()) {
					return 0;
				} else {
					return Integer.parseInt(txt);
				}
			}
			
		   public double toDouble(String txt) {
				if (txt == null || txt.isEmpty()) {
					return 0;
				} else {
					return Double.parseDouble(txt);
				}
			}
	    
	    
	    
}

