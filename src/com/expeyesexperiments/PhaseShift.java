package com.expeyesexperiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import explib.ejMath;
import explib.ejPlot;
 
public class PhaseShift extends Activity {
 
	Button button,fit_bt;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	TextView timebase_label,msg,fit_msg,V_t,V_r,V_lc,V_theta;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private double[] msPerDiv = {0.100, 0.200, 0.500, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 50.0};	
    private Builder reconnect_message;
    DecimalFormat df1 = new DecimalFormat("#.###");
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phase_shift);
		Toast.makeText(getBaseContext(),"TRANSIENT RESPONSE MEASUREMENTS",Toast.LENGTH_SHORT).show();

		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/PhaseShift/");
		//Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
     	setTitle(ej.title+"RLC phase shift measurements");
     	reconnect_message = ej.makeReconnectDialog(this);
     	
     	msg = (TextView)findViewById(R.id.msg);
     	V_t = (TextView)findViewById(R.id.frq);
     	V_r = (TextView)findViewById(R.id.dcycle);
     	V_lc = (TextView)findViewById(R.id.V_lc);
     	V_theta = (TextView)findViewById(R.id.V_theta);
     	
     	fit_msg = (TextView)findViewById(R.id.RLC_msg);
		LinearLayout plot=(LinearLayout)findViewById(R.id.plot);
		ejplot = new ejPlot(this, plot);
		timebase_label = (TextView) findViewById(R.id.timebase_label);
		timebase = (SeekBar) findViewById(R.id.timebase);
		fit_bt = (Button) findViewById(R.id.fit_bt);
		setScale();
		
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

     	
	}
	
	
	@Override public void onResume(){
		super.onResume();
		fit_bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				show_data(true);
			}
	    	
	    });

	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		Toast.makeText(getBaseContext(),"RETURNING TO MAIN MENU",Toast.LENGTH_SHORT).show();
	}
 

	public void measure(View v){
		setScale();
		ej.ej.capture_hr(1,2,ejplot.Par.NS,ejplot.Par.TG);
		if(ej.ej.commandStatus!=ej.ej.SUCCESS){
			//Toast.makeText(getBaseContext(),"FAILED. Check connections.",Toast.LENGTH_SHORT).show();
			if(!ej.ej.connected)reconnect_message.show();
			return;
			}
		for(int i=0;i<ej.ej.ejdata.length;i++){
			ej.ej.ejdata.ch3[i]=ej.ej.ejdata.ch1[i]-ej.ej.ejdata.ch2[i];
			ej.ej.ejdata.t3[i]=ej.ej.ejdata.t1[i];
		}
		show_data(false);
		//if(!ej.ej.connected)reconnect_message.show();	
	}


	public void show_data(boolean showfit){
		
		int MAX_PAR=25;
		double[][] P=new double[4][MAX_PAR],Err=new double[4][MAX_PAR];
		boolean Ok1,Ok2,Ok3;
		Ok1=ejMath.DoFitSine(ej.ej.ejdata.length,4,ej.ej.ejdata.t1,ej.ej.ejdata.ch1,P[0],Err[0]);
		Ok2=ejMath.DoFitSine(ej.ej.ejdata.length,4,ej.ej.ejdata.t2,ej.ej.ejdata.ch2,P[1],Err[1]);
		Ok3=ejMath.DoFitSine(ej.ej.ejdata.length,4,ej.ej.ejdata.t3,ej.ej.ejdata.ch3,P[2],Err[2]);
		fit_msg.setText("");
		//P[1]=P[1]/(2*3.1415); //change frequency from angular to normal
		//P[2]=P[2]*180/3.1415; //change phase to degrees
		Log.e("FITTING L",Ok1+":"+P[0][0]+","+P[0][1]+","+P[0][2]+","+P[0][3]);
		Log.e("FITTING R",Ok2+":"+P[1][0]+","+P[1][1]+","+P[1][2]+","+P[1][3]);
		Log.e("FITTING LC",Ok3+":"+P[2][0]+","+P[2][1]+","+P[2][2]+","+P[2][3]);
		if(Ok1)V_t.setText("V_t ="+df1.format(P[0][0]));else V_t.setText("V_t = Err");
		if(Ok2)V_r.setText("V_r ="+df1.format(P[1][0]));else V_r.setText("V_r = Err");
		if(Ok3)V_lc.setText("V_lc ="+df1.format(P[2][0]));else V_lc.setText("V_lc = Err");
		if(Ok1 && Ok2)V_theta.setText("theta ="+df1.format(  (P[1][2]-P[0][2])*180/3.1415  ));else V_theta.setText("theta = Err");
		
		ejplot.clearPlots();
		if(Ok1 && showfit){ //replace original plot data with fitted data;
			for(int n=0;n<ej.ej.ejdata.length;n++){
				ej.ej.ejdata.ch1[n]=(float) ejMath.Func_Dsine(P[0], ej.ej.ejdata.t1[n]);
				ej.ej.ejdata.ch2[n]=(float) ejMath.Func_Dsine(P[1], ej.ej.ejdata.t2[n]);
				ej.ej.ejdata.ch3[n]=(float) ejMath.Func_Dsine(P[2], ej.ej.ejdata.t3[n]);
				}
			fit_msg.setText("DISPLAYING FITTED DATA");
		}
		ejplot.line(ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length,0);
		ejplot.line(ej.ej.ejdata.t2,ej.ej.ejdata.ch2,ej.ej.ejdata.length,1);
		ejplot.line(ej.ej.ejdata.t3,ej.ej.ejdata.ch3,ej.ej.ejdata.length,2);
		
		ejplot.updatePlots();
		
	}

	// Changing the Time Base
		public void setScale() {
			int selectedTimebase = timebase.getProgress();    				// Slider returns a number from 0 to 9
			ejplot.setTimebase(msPerDiv[selectedTimebase]);
			timebase_label.setText(msPerDiv[selectedTimebase]+"mS/div");	
			 
		}
		
	
	
private void appendToFile(OutputStreamWriter writer,float[] x,float[] y,int length) throws IOException{
	for(int i=0;i<length;i++){writer.append(x[i]+" "+y[i]+"\n");}
	writer.append("\n");
	
}

public void dumpToFile(View v){
	SimpleDateFormat s = new SimpleDateFormat("dd-MM_hh-mm-ss");
	String format = s.format(new Date());
	//Log.e("FILENAME",format+"");
	filename = format+".txt";
	//Log.e("SAVING",""+filename);
	msg.setText("Saved to:"+filename);
	try {
    	File outputFile = new File(dataDirectory, filename);
  		outputFile.createNewFile();
		FileOutputStream fOut = new FileOutputStream(outputFile);
		OutputStreamWriter myOutWriter =  new OutputStreamWriter(fOut);
        appendToFile(myOutWriter,ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length);
    	
		myOutWriter.close();
        fOut.close();
        
        Toast.makeText(getBaseContext(), "Done writing SD " + filename + "",Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
	
	
}


}
