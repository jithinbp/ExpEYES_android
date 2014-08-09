package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import explib.ejPlot;
 
public class DataLogger extends Activity {
 
	Button button;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE,Color.CYAN};
	private boolean[] channel_status={false,false,false,false,false};
	ejPlot ejplot;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private Handler mHandler;
    private boolean running=false;
    private float[][] T=new float[5][10000],Y=new float[5][10000];
    private int[] length={0,0,0,0,0};
    private int interval=0,duration=0;
    private long start_time=0;
    Builder reconnect_message;
    private EditText INTERVAL,DURATION;
    
    Map<String, Integer>channelmap = new HashMap<String, Integer>();
	CompoundButton[] channel_buttons = new CompoundButton[5];
    
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.data_logger);
		
		
		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/logger/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
     	setTitle(ej.title+"Data Logger");
     	reconnect_message = ej.makeReconnectDialog(this);
     	
     	LinearLayout plot=(LinearLayout)findViewById(R.id.logger_plot);
     	ejplot = new ejPlot(this, plot);
	    ejplot.xlabel="Time(S)";
	    ejplot.ylabel="Volts(V)";
	    ejplot.setWorld(0, 5, 0, 5);
        
	    DURATION = (EditText) findViewById(R.id.duration);
	    INTERVAL = (EditText) findViewById(R.id.interval);
	    
	    reconnect_message = ej.makeReconnectDialog(this);
	    mHandler = new Handler();
	    
	    
	    channelmap.put("A1", 1);
        channelmap.put("A2", 2);
        channelmap.put("IN1", 3);
        channelmap.put("IN2", 4);
        channelmap.put("SEN", 5);
        
        /*------FIND ALL SWITCHES AND ASSOCIATE A COMMON FUNCTION(load_channel) WITH THEM----------_*/
        ViewGroup group = (ViewGroup)findViewById(R.id.switches);
    	CompoundButton v;
    	int n=0;
    	for(int i = 0; i < group.getChildCount(); i++) {
    	    v = (CompoundButton) group.getChildAt(i);
    	    if(v instanceof CompoundButton){
    	    	v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    	    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	    	    	load_channel(buttonView, isChecked);
    	    	    }
    	    	});
    	    	channel_buttons[n++]=v;

    	    }
    	}
		     	
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		running=false;
		
		//Toast.makeText(getBaseContext(),"RETURNING TO MAIN MENU",Toast.LENGTH_SHORT).show();
	}
 
	public void load_channel(CompoundButton buttonView, boolean isChecked){
		if(isChecked){
			channel_status[channelmap.get(buttonView.getText())-1]=true;
			Log.e("ENABLED","CH1");
		}
		else{
			//Log.e("CHANGES","Dropped:"+buttonView.getText());
			channel_status[channelmap.get(buttonView.getText())-1]=false;
			length[channelmap.get(buttonView.getText())-1]=0;
			Log.e("DISABLED","CH1");
		}
		

	}
	
	
	public double toDouble(EditText txt) {
		String val = txt.getText().toString();

		if (val == null || val.isEmpty()) {
			return 0.0;
		} else {
			return Double.parseDouble(val);
		}
	}
	
	public void start(View v){
		for(int i=0;i<5;i++)length[i]=0;
		start_time=System.nanoTime();
		duration=(int) toDouble(DURATION);
		interval=(int) toDouble(INTERVAL);
		if(duration<1 || interval<10){
			Toast.makeText(getBaseContext(),"Check parameters. Interval > 10mS, and Duration>1 S ",Toast.LENGTH_SHORT).show();
			return;
			}
		ejplot.setWorld(0, duration, -5, 5);
		ejplot.clearPlots();
		if(!running){
			running=true;
			Log.e("STARTING",length+"");
			Toast.makeText(getBaseContext(),"Started Log , Duration:"+duration+" S, interval:"+interval+"mS",Toast.LENGTH_SHORT).show();
			mHandler.postDelayed(plotter, interval);
			}
		}
	
	public void stop(View v){
		running=false;
	}

	
	Runnable plotter = new Runnable() {  
	    @Override 
	    public void run() {
	    	for(int i=0;i<5;i++){
	    	    
	    	    if(channel_status[i]){
	    	    	if(length[i]>9999){
		    	    	running=false;
		    	    	Toast.makeText(getBaseContext(),"Logger stopped. Array size exceeded",Toast.LENGTH_SHORT).show();
		    	    	return;
		    	    }

	    	    	
	    	    	ejplot.clearPlot(i);
	    			//T[i][length]=0;
		    		//Y[i][length]=0;
		    		ej.ej.get_voltage_time(i+1);
	    			T[i][length[i]]=(float) ((ej.ej.ejdata.timestamp-start_time)/1e9);
		    	    Y[i][length[i]]=(float) ej.ej.ejdata.ddata;
		    	    ejplot.line(T[i],Y[i],length[i],i); 
		    	    
		    	    
		    	    if(T[i][length[i]]>duration){
		    	    	running=false;
		    	    	Toast.makeText(getBaseContext(),"Logging complete",Toast.LENGTH_SHORT).show();
		    	    	return;
		    	    }

		    	    
			    	length[i]++;
	    		  }
	    		}
	    	
	    	if(!ej.ej.connected)reconnect_message.show();
	    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
		    	Toast.makeText(getBaseContext(),"Read error. Thread killed.",Toast.LENGTH_SHORT).show();
		    	running=false;
		    	return;
				}
		    
				ejplot.updatePlots();
			    
	    		
			
			
	    	
    	    
	    	if(running)mHandler.postDelayed(plotter, interval);
	    	
	      
	    }
	};

	
	
	
	
	
	
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
		for(int i=0;i<5;i++){
			if(channel_status[i])appendToFile(myOutWriter,T[i],Y[i],length[i]);
        }
		myOutWriter.close();
        fOut.close();
        
        Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
	
	
}


}
