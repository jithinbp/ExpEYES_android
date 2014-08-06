/* ExpEYES Scope.
   Oscilloscope for ExpEYES (http://expeyes.in) under Android
   Copyright (C) 2014 Jithin B.P. , IISER Mohali (jithinbp@gmail.com)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.
*/

package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import explib.ejMath;
import explib.ejPlot;

public class Oscilloscope
extends Activity {
	ejPlot ejplot;
	private TextView freq;
	private int[] channel_status = {0,0,0,0,0,0,0},selected_channel_list={0,0,0,0};
	private int selected_channels = 0;
	private int TG = 10;			// 10 microseconds
	private int NS = 300;			// for capture calls
	int activeChans=0;					// Number of active channels, maximum is 4
	Map<String, Integer>channelmap = new HashMap<String, Integer>();
	CompoundButton[] channel_buttons = new CompoundButton[7];
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	/*--------------------------------------flags-------------------------*/
	private boolean save_last_capture = false,disableLoop=false,timebaseChanged=true,running=true;
	
	private Handler mHandler;
      
    private expeyesCommon ej;
    private TextView timebase_label;
    private SeekBar timebase;
    private File dataDirectory;
    private String filename;
    private Builder reconnect_message;
    DecimalFormat df1 = new DecimalFormat("#.#");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.oscilloscope);
		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/SCOPE");
		//Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();
		
		ej=expeyesCommon.getInstance();
		setTitle(ej.title+"Oscilloscope");
		reconnect_message = ej.makeReconnectDialog(this);
		
		
		ej.ej.disable_actions();
    	ej.ej.set_trigval(1, 0.0);
    	
		//----------------Plotting section-------------------
    	LinearLayout ll=(LinearLayout)findViewById(R.id.scope_plot);
		ejplot = new ejPlot(this, ll);
		timebase_label = (TextView) findViewById(R.id.timebase_label);
		timebase = (SeekBar) findViewById(R.id.timebase_slider);
		ejplot.setWorld(0.0,1, -5, 5); 
    	channelmap.put("A1", 1);
        channelmap.put("A2", 2);
        channelmap.put("IN1", 3);
        channelmap.put("IN2", 4);
        channelmap.put("SEN", 5);
        channelmap.put("SQ1", 6);
        channelmap.put("SQ2", 7);
        
        freq=(TextView) findViewById(R.id.FREQ);
        
        timebase.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				
				timebase_label.setText(msPerDiv[progress]+"mS/div");
			}
		});
        
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
		
    	
    	mHandler = new Handler();
		cro.run();
		
	} 
	
	
	// Changing the Time Base
	private double[] msPerDiv = {0.100, 0.200, 0.500, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 50.0};	

	public void setTimebase() {
		if(activeChans==0)return;
		int selectedTimebase = timebase.getProgress();     // Slider returns a number from 0 to 9
		double totalTime = msPerDiv[selectedTimebase] * 10 * 1000;   // in microseconds
		NS = 300; // start with 300
		TG = (int) (totalTime / NS);
		if ( (TG/activeChans) <= 4) {
			TG = 4 * activeChans;
			NS = (int)(totalTime /TG);
			}
		while (NS*TG > (int)totalTime)--NS;
		while (NS*TG < (int)totalTime)++NS;

		if (TG > 1000) TG = 1000;
		if(selectedTimebase >= 8)
		NS = 400;
		if( (selectedTimebase == 9) && (activeChans != 4 )) // with 4 channels we can't take 500 samples
		NS = 500;
		//Log.e("setTimebase","ns= "+NS+ " tg ="+ TG + "ms/div "+msPerDiv[selectedTimebase]+ " and "+ 0.001*NS*TG);
		    ejplot.setWorld(0.0,totalTime/1000.0, -5, 5);
		   
		}


	
	
	/*implement CRO toggle switches*/
	public void load_channel(CompoundButton buttonView, boolean isChecked){
		if(isChecked){
			if(selected_channels<4){
			channel_status[channelmap.get(buttonView.getText())-1]=1;
			selected_channels++;
			//Log.e("CHANGES","Added:"+buttonView.getText());
			}
			else{
				Toast.makeText(getBaseContext(),"A maximum of four channels can be selected.",Toast.LENGTH_SHORT).show();
				buttonView.setChecked(false);
			}
		}
		else{
			//Log.e("CHANGES","Dropped:"+buttonView.getText());
			channel_status[channelmap.get(buttonView.getText())-1]=0;
			selected_channels--;
		}
		
		return;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		running=false;
		Toast.makeText(getBaseContext(),"RETURNING TO MAIN MENU",Toast.LENGTH_SHORT).show();
	}
 	
	public void dumpToFile(View v){
		SimpleDateFormat s = new SimpleDateFormat("dd-MM_hh-mm-ss");
		String format = s.format(new Date());
		filename = "traces"+format+".txt";
		save_last_capture=true;
		
		
	}
	
	
	private void appendToFile(OutputStreamWriter writer,float[] x,float[] y,int length) throws IOException{
		for(int i=0;i<length;i++){writer.append(x[i]+" "+y[i]+"\n");}
		writer.append("\n");
		
	}
	
	public void email_data_to_me(){
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType(HTTP.PLAIN_TEXT_TYPE);
		//emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"jithinbp@gmail.com"}); // recipients
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Oscilloscope data");
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Find attached oscilloscope plots");
		File myFile = new File(dataDirectory,filename);
		Uri uri = Uri.fromFile(myFile);
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(emailIntent);

	}
	Runnable cro = new Runnable() {  
	    @Override 
	    public void run() {
	    	if(!running)return;
	    	  if ( save_last_capture==true){
	    		  	  save_last_capture=false;
			          try {
			        	File outputFile = new File(dataDirectory, filename);
			      		outputFile.createNewFile();
						FileOutputStream fOut = new FileOutputStream(outputFile);
						OutputStreamWriter myOutWriter =  new OutputStreamWriter(fOut);
			            if(activeChans>0)appendToFile(myOutWriter,ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length);
			            if(activeChans>1)appendToFile(myOutWriter,ej.ej.ejdata.t2,ej.ej.ejdata.ch2,ej.ej.ejdata.length);
			            if(activeChans>2)appendToFile(myOutWriter,ej.ej.ejdata.t3,ej.ej.ejdata.ch3,ej.ej.ejdata.length);
			            if(activeChans>3)appendToFile(myOutWriter,ej.ej.ejdata.t4,ej.ej.ejdata.ch4,ej.ej.ejdata.length);
			            
						
						myOutWriter.close();
			            fOut.close();
			            email_data_to_me();
			            Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
			        } catch (Exception e) {
			            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			        }
			   }
	    	
	    	
	    	activeChans = 0;				// calculate the number of active channels
		    for(int i=0;i<7;i++){
		    	channel_buttons[i].setBackgroundColor(Color.TRANSPARENT);
		    	if(channel_status[i]!=0){
		    		channel_buttons[i].setBackgroundColor(channel_colors[activeChans]);
	    			selected_channel_list[activeChans++]=i+1;
	    			
	    		} 
	    	}
		    if(activeChans==0 || disableLoop || ej.ej.connected==false)
		    	{
		    	mHandler.postDelayed(cro, 1);
		    	//Log.e("ERROR","errors"+NS+":"+TG);
		    	return;
		    	}
		    setTimebase();
		    
	    	if(activeChans==1)ej.ej.capture(selected_channel_list[0],NS,TG);
	    	if(activeChans==2)ej.ej.capture(selected_channel_list[0],selected_channel_list[1],NS,TG);
	    	if(activeChans==3)ej.ej.capture(selected_channel_list[0],selected_channel_list[1],selected_channel_list[2],NS,TG);
	    	if(activeChans==4)ej.ej.capture(selected_channel_list[0],selected_channel_list[1],selected_channel_list[2],selected_channel_list[3],NS,TG);

			
	    	if(ej.ej.connected==false)
	    		{
	    		Toast.makeText(getBaseContext(),"Device disconnected.  Check connections.",Toast.LENGTH_SHORT).show();
	    		reconnect_message.show();
	    		return;
	    		}
	    		
	    		
	    	if(ej.ej.commandStatus != ej.ej.SUCCESS){		// error return
	    	mHandler.postDelayed(cro, 1);
	    	return;
				 
			}
			
	    	ejplot.clearPlots();
			if(activeChans>0)ejplot.line(ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length,0);
			if(activeChans>1)ejplot.line(ej.ej.ejdata.t2,ej.ej.ejdata.ch2,ej.ej.ejdata.length,1);
			if(activeChans>2)ejplot.line(ej.ej.ejdata.t3,ej.ej.ejdata.ch3,ej.ej.ejdata.length,2);
			if(activeChans>3)ejplot.line(ej.ej.ejdata.t4,ej.ej.ejdata.ch4,ej.ej.ejdata.length,3);
			ejplot.updatePlots();
			
			if(ejplot.touched){
				String fitvals=new String();
				//double f= ej.ej.calc_frequency(1);
				int MAX_PAR=25;
				double[] P=new double[MAX_PAR],Err=new double[MAX_PAR];
				boolean Ok;
				int NPts=ej.ej.ejdata.length;
				if(activeChans>0){
					Ok=ejMath.DoFitSine(NPts,4,ej.ej.ejdata.t1,ej.ej.ejdata.ch1,P,Err);        // call the fit function
					P[1]=1000*P[1]/(2*3.1415); //change frequency to hertz
					P[2]=P[2]*180/3.1415; //change phase to degrees
					if(Ok) fitvals+="CH1.freq ="+df1.format(P[1])+"\n";
					else fitvals+="Ch1.freq = Err\n";
					}
				if(activeChans>1){
					Ok=ejMath.DoFitSine(NPts,4,ej.ej.ejdata.t2,ej.ej.ejdata.ch2,P,Err);        // call the fit function
					P[1]=1000*P[1]/(2*3.1415); //change frequency to hertz
					P[2]=P[2]*180/3.1415; //change phase to degrees
					if(Ok) fitvals+="CH2.freq ="+df1.format(P[1])+"\n";
					else fitvals+="Ch2.freq = Err\n";
					}
				if(activeChans>2){
					Ok=ejMath.DoFitSine(NPts,4,ej.ej.ejdata.t3,ej.ej.ejdata.ch3,P,Err);        // call the fit function
					P[1]=1000*P[1]/(2*3.1415); //change frequency to hertz
					P[2]=P[2]*180/3.1415; //change phase to degrees
					if(Ok) fitvals+="CH3.freq ="+df1.format(P[1])+"\n";
					else fitvals+="Ch3.freq = Err\n";
					}
				if(activeChans>3){
					Ok=ejMath.DoFitSine(NPts,4,ej.ej.ejdata.t4,ej.ej.ejdata.ch4,P,Err);        // call the fit function
					P[1]=1000*P[1]/(2*3.1415); //change frequency to hertz
					P[2]=P[2]*180/3.1415; //change phase to degrees
					if(Ok) fitvals+="CH4.freq ="+df1.format(P[1])+"\n";
					else fitvals+="Ch4.freq = Err\n";
					}
				freq.setText(fitvals);
				
				
				ejplot.touched=false;
				}
	      mHandler.postDelayed(cro, 10);
	    }
	};



}
