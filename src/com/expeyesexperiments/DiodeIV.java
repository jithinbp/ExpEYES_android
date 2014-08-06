package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import explib.ejPlot;
 
public class DiodeIV extends Activity {
 
	Button button;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private double VSET=0.0,Vtemp=0,Itemp=0;
    private Handler mHandler;
    private boolean running=false;
    private float[] V=new float[1800],I=new float[1800];
    private int length=0;

    Builder reconnect_message;
    
    
    
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diode_experiment);
		Toast.makeText(getBaseContext(),"Diode Current-Voltage characteristics",Toast.LENGTH_SHORT).show();

		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/diode/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
     	setTitle(ej.title+"Diode IV");
		
     	LinearLayout plot=(LinearLayout)findViewById(R.id.plot);
     	ejplot = new ejPlot(this, plot);
	    ejplot.xlabel="Volts";
	    ejplot.ylabel="mA";
	    ejplot.setWorld(0, 5, 0, 5);
        
	    reconnect_message = ej.makeReconnectDialog(this);
	    mHandler = new Handler();
     	t.start(); //fetches values in the background

		     	
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		running=false;
		t.interrupt();
		Toast.makeText(getBaseContext(),"RETURNING TO MAIN MENU",Toast.LENGTH_SHORT).show();
	}
 
	public void start(View v){
		VSET=0;
		length=0;
		ejplot.clearPlots();
		if(!running){
			running=true;
			t.run();
			}
		}
	

	
	Runnable plotter = new Runnable() {  
	    @Override 
	    public void run() {
	    	ejplot.clearPlots();
			if(length>1){
	    		ejplot.line(V,I,length,0);
				ejplot.updatePlots();
		    	}
			
	    	
	    	if(running)t.run();
	    	if(!ej.ej.connected)reconnect_message.show();
    	    
	      
	    }
	};

	
	
	
	//--------------------THIS THREAD FETCHES THE ACTUAL VALUES-----------------
	Thread t = new Thread(new Runnable() {  
	    @Override 
	    public void run() {
	    	if(!running)return;
	    	if(ej.ej.connected==false)
	    		{
	    		
	    		Toast.makeText(getBaseContext(),"Device disconnected.  Check connections.",Toast.LENGTH_SHORT).show();
	    		
	    		return;
	    		}

	    	ej.ej.set_voltage(VSET);	
	    	
	    	Vtemp = ej.ej.ejdata.ddata;
			ej.ej.get_voltage(3);
	    	Itemp = (Vtemp-ej.ej.ejdata.ddata)/1.0;
	    	V[length]=(float) ej.ej.ejdata.ddata;
	    	I[length++]=(float) Itemp;
	    	VSET+=0.05;
	    	if(VSET>4.95)return;
	    	Log.e("LENGTH",length+"");
	    	
	    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
	    	Toast.makeText(getBaseContext(),"Read error. Thread killed.",Toast.LENGTH_SHORT).show();
	    	return;
			}
	    	mHandler.postDelayed(plotter, 1);
	    	//multimeter.run();
	      
	    }
	});

	
	
	
	
	
	
	
	
private void appendToFile(OutputStreamWriter writer,float[] x,float[] y,int length) throws IOException{
	for(int i=0;i<length;i++){writer.append(x[i]+" "+y[i]+"\n");}
	writer.append("\n");
	
}

public void dumpToFile(View v){
	SimpleDateFormat s = new SimpleDateFormat("dd-MM_hh-mm-ss");
	String format = s.format(new Date());
	Log.e("FILENAME",format+"");
	filename = format+".txt";
	Log.e("SAVING",""+filename);
	try {
    	File outputFile = new File(dataDirectory, filename);
  		outputFile.createNewFile();
		FileOutputStream fOut = new FileOutputStream(outputFile);
		OutputStreamWriter myOutWriter =  new OutputStreamWriter(fOut);
        appendToFile(myOutWriter,V,I,length);
    	
		myOutWriter.close();
        fOut.close();
        
        Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
	
	
}


}
