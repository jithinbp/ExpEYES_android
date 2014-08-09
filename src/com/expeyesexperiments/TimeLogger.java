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
 
public class TimeLogger extends Activity {
 
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
    EditText command_entry;
    Builder reconnect_message;
    ArrayAdapter<String> cmdlist;
    private String cmd=new String("");
    double sum=0;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_logger);
		Toast.makeText(getBaseContext(),"Log time intervals",Toast.LENGTH_SHORT).show();

		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/Time_logger/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
     	setTitle(ej.title+"Time Interval Logger");
     	command_entry=(EditText)findViewById(R.id.command_entry);
     	experiment_selector = (Button) findViewById(R.id.experiment_selector);
     	
		String[] available_time_measurement_functions = getResources().getStringArray(R.array.time_exp_list);
		cmdlist = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,  available_time_measurement_functions );

		experiment_selector.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				open_experiment_list();
 			}
 		});
		
     	
     	
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
 
	
	
    public void open_experiment_list(){
    	new AlertDialog.Builder(this)
    	  .setTitle("Select an experiment")
    	  .setAdapter(cmdlist, new DialogInterface.OnClickListener() {

    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	      // TODO: user specific action
    	    	Log.e("CLICKED",cmdlist.getItem(which)+"" );
    	    	String[] args = cmdlist.getItem(which).split(">");
    	    	command_entry.setText(args[1]);
    	      dialog.dismiss();
    	    }
    	  }).create().show();
    }
    
	
	public void show_data(View v){
    	String message = new String();
    	for(int a=0;a<length;a++){message+=VAL[a]+" \n";}
    	if(length>1)message+="\nAverage time : "+ej.df6.format(sum/length)+"\n";
		new AlertDialog.Builder(this)
  	  .setTitle("Datapoints (Seconds)")
  	  .setMessage(message).create().show();
	}
	
	
	public void start(View v){
		start_time=System.currentTimeMillis();
		cmd=command_entry.getText().toString();
		if(cmd.length()==0){
			running=false;
			Toast.makeText(getBaseContext(),"Please select a timer function",Toast.LENGTH_SHORT).show();
			return;
		}
		max_x=30; 
		ejplot.setWorld(0, max_x, min, max);
		length=0;
		sum=0;
		max=0;
		ejplot.clearPlots();
		Toast.makeText(getBaseContext(),"Started Logging :"+cmd,Toast.LENGTH_SHORT).show();
		if(!running){
			running=true;
			t.run();
			}
		}
	public void stop(View v){
		running=false;
	}

	
	Runnable plotter = new Runnable() {  
	    @Override 
	    public void run() {
	    	ejplot.clearPlots();
			if(length>1){
	    		ejplot.line(POINT,VAL,length,0);
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
	    	//ej.ej.multi_r2rtime(6, 0);
	    	ej.ej.executeString(cmd);
	        //Executors.newSingleThreadExecutor().submit(new Runnable(){ public void run(){ej.ej.executeString(cmd);}});
	    	
	    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
		    	if(ej.ej.commandStatus == ej.ej.TIMEOUT)Toast.makeText(getBaseContext(),"Timeout error. Logging stopped.",Toast.LENGTH_SHORT).show();
		    	else if(ej.ej.commandStatus == ej.ej.INVARG)Toast.makeText(getBaseContext(),"Invalid argument error. Logging stopped.",Toast.LENGTH_SHORT).show();
		    	else Toast.makeText(getBaseContext(),"error. Logging stopped. Code="+ej.ej.commandStatus,Toast.LENGTH_SHORT).show();
		    	running=false;
		    	return;
				}
	        //if(length==200)Log.e("TIME ELAPSED:",(System.currentTimeMillis()-start_time)+"");
	    	POINT[length]=length;
	    	VAL[length++] = (float) (ej.ej.ejdata.ddata*1e-6);//conversion to seconds
	    	sum+=(float) (ej.ej.ejdata.ddata*1e-6);
	    	//scaling
	    	if(ej.ej.ejdata.ddata>max){
	    		max=(float) (ej.ej.ejdata.ddata*1e-6*1.5); //convert to seconds. scale by 1.5
	    		ejplot.setWorld(0, max_x, min, max);
	    	}
	    	if(length>max_x){
	    		max_x=length+30; //add another 30 points of X scale
	    		ejplot.setWorld(0, max_x, min, max);
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
