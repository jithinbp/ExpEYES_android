package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import explib.ejPlot;
 
public class TransistorCE extends Activity {
 
	Button button;
	expeyesCommon ej;
	EditText V_BASE;
	TextView V_SET;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private double VSET=0.0,Vtemp=0,Itemp=0;
    private Handler mHandler;
    private boolean running=false;
    private float[][] V=new float[4][1800],I=new float[4][1800];
    private int current_trace=-1;
    private int[] length={0,0,0,0};
    private double[] B_SETS={0.0,0.0,0.0,0.0};
    Builder reconnect_message;
    
    
    
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transistor_experiment);
		Toast.makeText(getBaseContext(),"Transistor Collector-Emitter characteristics",Toast.LENGTH_SHORT).show();

		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/transistor/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		V_BASE = (EditText) findViewById(R.id.V_BASE);
		V_SET = (TextView) findViewById(R.id.V_SET);
		ej=expeyesCommon.getInstance();
		ej.ej.set_sqr1_dc(3.0); //Charging filter capacitor
		
		setTitle(ej.title+"Transistor CE");
		
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
		if(running){
			Toast.makeText(getBaseContext(),"Stopping current acquisition...  wait and retry",Toast.LENGTH_SHORT).show();	
			running=false;
			return;
		}
		VSET=0;
		current_trace++;
		if(current_trace>3)current_trace=0;
		Log.e("START",current_trace+":"+length[current_trace]);
		B_SETS[current_trace]=ej.toDouble(V_BASE.getText().toString());
		ej.ej.set_sqr1_dc(B_SETS[current_trace]);
		SystemClock.sleep(300);
		length[current_trace]=0;
		ejplot.clearPlots();
		V_SET.setTextColor(ejplot.paints[current_trace].getColor());
		Log.e("THREADS",""+java.lang.Thread.activeCount());
		running=true;
		t.run();
	} 
	
	public void clear_traces(View v){
		for(int a=0;a<4;a++){length[a]=0;ejplot.line(V[a],I[a],length[a],a);}
		ejplot.updatePlots();
	}

	
	Runnable plotter = new Runnable() {  
	    @Override 
	    public void run() {
	    	if(!running)return;
	    	V_SET.setText("V_SET = "+ej.df3.format(VSET)+"V"+"\nI="+ ej.df3.format(Itemp)+" mA");
	    	ejplot.clearPlots();
			for(int a=0;a<4;a++){
	    	if(length[a]>1){
	    		ejplot.line(V[a],I[a],length[a],a);
				}
			}
			ejplot.updatePlots();
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
		    	V[current_trace][length[current_trace]]=(float) ej.ej.ejdata.ddata;
		    	I[current_trace][length[current_trace]++]=(float) Itemp;
		    	VSET+=0.05;
		    	if(VSET>4.95){running=false;return;}
		    	//Log.e("LENGTH",length[current_trace]+"");
		    	
		    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
		    	Toast.makeText(getBaseContext(),"Read error. Thread killed.",Toast.LENGTH_SHORT).show();
		    	return;
				}
	
		    	mHandler.postDelayed(plotter, 1);
		    	
	    	
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
        for(int a=0;a<4;a++)appendToFile(myOutWriter,V[a],I[a],length[a]);
    	
		myOutWriter.close();
        fOut.close();
        
        Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
	
	
}


}
