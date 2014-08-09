package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import explib.ejPlot;
import explib.headers;
 
public class RodPendulum extends Activity {
 
	Button button,experiment_selector;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private double Itemp=0,start_time=0;
    private Handler mHandler;
    private boolean running=false;
    private float[] POINT=new float[5000],VAL=new float[5000];
    private int length=0,max_x=30;
    private float min=0,max=(float) 0.1;
    double sum=0;
    EditText samples_entry,length_rod;
    Builder reconnect_message;
    ArrayAdapter<String> cmdlist;
    
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rod_pendulum);
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/Rod_pendulum/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
		ej.ej.set_sqr1(0);
     	setTitle(ej.title+"Rod Pendulum Time Interval Logger");
     	samples_entry=(EditText)findViewById(R.id.samples_entry);
     	length_rod=(EditText)findViewById(R.id.length_rod);
     	experiment_selector = (Button) findViewById(R.id.experiment_selector);
     	
		String[] available_time_measurement_functions = getResources().getStringArray(R.array.time_exp_list);
		cmdlist = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,  available_time_measurement_functions );


     	
     	LinearLayout plot=(LinearLayout)findViewById(R.id.plot);
     	ejplot = new ejPlot(this, plot);
	    ejplot.xlabel="points";
	    ejplot.ylabel="Seconds";
	    ejplot.setWorld(0, max_x, min,max);
        
	    reconnect_message = ej.makeReconnectDialog(this);
	    mHandler = new Handler();
     	t.start(); //fetches values in the background

		     	
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		running=false;
		t.interrupt();//
		Toast.makeText(getBaseContext(),"RETURNING TO MAIN MENU",Toast.LENGTH_SHORT).show();
	}
 
	
	public void show_data(View v){
    	String message = new String();
    	
    	for(int a=0;a<length;a++){message+=VAL[a]+" \n";}
		
    	double rodlen = ej.toDouble(length_rod.getText().toString()); 
    	if(length<1 || rodlen==0)message+="Insufficient data / Rod length not provided.\n";
    	else{
    		double T=sum/length;
    		message+="\nAverage time : "+ej.df6.format(T)+"\n";
    		message+="Value of g : "+4*3.1415*3.1415*2*rodlen/(3*T*T) + " cm/S^2\n";
    	}
    	
    	new AlertDialog.Builder(this)
  	  .setTitle("Datapoints (Seconds)")
  	  .setMessage(message).create().show();
	}
	
	
	
	public void start(View v){
		start_time=System.currentTimeMillis();
		max_x=ej.toInt(samples_entry.getText().toString());
		if(max_x==0){max_x=3;samples_entry.setText("3");}
		ejplot.setWorld(0, max_x*1e-6*1.5, min, max);
		length=0;
		sum=0;
		max=0;
		ejplot.clearPlots();
		if(!running){
			running=true;
			mHandler.postDelayed(plotter, 1);
			//t.run();
			}
		}
	

	
	Runnable plotter = new Runnable() {  
	    @Override 
	    public void run() {
	    	ejplot.clearPlots();
			if(length>0){
	    		ejplot.spikes(POINT,VAL,length,0);
				ejplot.updatePlots();
				//Log.e("TIME :",ej.ej.ejdata.ddata+"");
		    	
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
	    	ej.ej.multi_r2rtime(0, 1);
	    	//ej.ej.executeString(cmd);
	    	
	    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
		    	if(ej.ej.commandStatus == ej.ej.TIMEOUT)Toast.makeText(getBaseContext(),"Timeout error. Logging stopped.",Toast.LENGTH_SHORT).show();
		    	else if(ej.ej.commandStatus == ej.ej.INVARG)Toast.makeText(getBaseContext(),"Invalid argument error. Logging stopped.",Toast.LENGTH_SHORT).show();
		    	else Toast.makeText(getBaseContext(),"error. Logging stopped. Code="+ej.ej.commandStatus,Toast.LENGTH_SHORT).show();
		    	running=false;
		    	return;
				}
	    	
	        POINT[length]=length+1;
	    	VAL[length++] = (float) (ej.ej.ejdata.ddata*1e-6);//conversion to seconds
	    	sum+=(float) (ej.ej.ejdata.ddata*1e-6);
	    	//scaling
	    	if(ej.ej.ejdata.ddata>max){
	    		max=(float) (ej.ej.ejdata.ddata); 
	    		ejplot.setWorld(0, max_x*1.2, min, max*1e-6*1.5);//convert to seconds. scale by 1.5
	    	}
	    	if(length>max_x){
	    		//max_x=length+5; //add another 5 points of X scale
	    		//ejplot.setWorld(0, max_x, min, max);
	    		Toast.makeText(getBaseContext(),"Logging finished."+max_x+" datapoints acquired.",Toast.LENGTH_SHORT).show();
	    		running=false;
	    		return;
	    	}
			
	    	
			if(length>4999){running=false;return;}
			
	    
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
        appendToFile(myOutWriter,POINT,VAL,length);
    	
		myOutWriter.close();
        fOut.close();
        
        Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
	
	
}


}
