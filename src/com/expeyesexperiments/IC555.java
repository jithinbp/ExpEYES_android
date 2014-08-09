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
 
public class IC555 extends Activity {
 
	Button button;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	TextView timebase_label,msg,frq,dcycle;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private double[] msPerDiv = {0.100, 0.200, 0.500, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 50.0};	
    private Builder reconnect_message;
    DecimalFormat df1 = new DecimalFormat("#.###");
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic_555);
		
		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/IC555/");
		//Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		
		ej=expeyesCommon.getInstance();
     	ej.ej.set_state(10,1);
		setTitle(ej.title+"IC 555 measurements");
     	reconnect_message = ej.makeReconnectDialog(this);
     	
     	msg = (TextView)findViewById(R.id.msg);
     	frq = (TextView)findViewById(R.id.frq);
     	dcycle = (TextView)findViewById(R.id.dcycle);
     	
     	LinearLayout plot=(LinearLayout)findViewById(R.id.plot);
		ejplot = new ejPlot(this, plot);
		timebase_label = (TextView) findViewById(R.id.timebase_label);
		timebase = (SeekBar) findViewById(R.id.timebase);
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
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
	}
 

	public void measure(View v){
		ej.ej.set_state(10,1);
		double r2f=0,mr2r=1;
		boolean a=false,b=false;
		setScale();
		ej.ej.capture_hr(3,ejplot.Par.NS,ejplot.Par.TG);
		ej.ej.get_frequency(3);
		if(ej.ej.commandStatus==ej.ej.SUCCESS)frq.setText("Frequency = "+df1.format(ej.ej.ejdata.ddata)+"Hz");
		else frq.setText("Frequency = Err");
		
		ej.ej.r2ftime(3, 3);
		if(ej.ej.commandStatus==ej.ej.SUCCESS){a=true;r2f=ej.ej.ejdata.ddata;}
		ej.ej.multi_r2rtime(3, 0);
		if(ej.ej.commandStatus==ej.ej.SUCCESS){b=true;mr2r=ej.ej.ejdata.ddata;}
		
		if(ej.ej.commandStatus!=ej.ej.SUCCESS){
			//Toast.makeText(getBaseContext(),"FAILED. Check connections.",Toast.LENGTH_SHORT).show();
			if(!ej.ej.connected)reconnect_message.show();
			return;
			}

		if(a && b)
			dcycle.setText("Duty cycle = "+df1.format(r2f*100/mr2r)+"%");
		else
			dcycle.setText("Duty Cycle = Err");
		
		ejplot.clearPlots();
		ejplot.line(ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length,0);
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
