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
import android.os.Handler;
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
 
public class EMInduction extends Activity {
 
	Button button,fit_bt;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	TextView timebase_label,msg,fit_msg;
	SeekBar timebase;
	String filename = new String();
	boolean running=false;
    private File dataDirectory;
    private Builder reconnect_message;
    DecimalFormat df1 = new DecimalFormat("#.###");
    int count=0;
    private Handler mHandler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_em_induction_experiment);
		
		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/EMINDUCTION/");
		//Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		ej=expeyesCommon.getInstance();
		setTitle(ej.title+"Electromagnetic Induction");
		reconnect_message = ej.makeReconnectDialog(this);
     	msg = (TextView)findViewById(R.id.msg);
     	fit_msg = (TextView)findViewById(R.id.RC_msg);
		LinearLayout plot=(LinearLayout)findViewById(R.id.plot);
		ejplot = new ejPlot(this, plot);
		timebase_label = (TextView) findViewById(R.id.timebase_label);
		timebase = (SeekBar) findViewById(R.id.timebase);
		fit_bt = (Button) findViewById(R.id.fit_bt);
		ejplot.setTimebase(20);
		mHandler = new Handler();
		fit_msg.setText("Start..");
	    timebase_label.setText("20mS/div");
		
	}
	
		
	@Override
	public void onDestroy(){
		super.onDestroy();
		running=false;
		//Toast.makeText(getBaseContext(),"ENDED CAPACITOR DISCHARGE EXPERIMENT",Toast.LENGTH_SHORT).show();
	}
 
	public void start(View v){
		count=0;
		fit_msg.setText("Running..");
		if(running){
			return;
		}
		running=true;
		cro.run();
		//if(!ej.ej.connected)reconnect_message.show();	
	}
	
	
	
	
	
	
	
	
	Runnable cro = new Runnable() {  
	    @Override 
	    public void run() {
	    	if(!running)return;

	    	ej.ej.capture_hr(1,ejplot.Par.NS,ejplot.Par.TG);
			if(ej.ej.commandStatus!=ej.ej.SUCCESS){
				//Toast.makeText(getBaseContext(),"FAILED. Check connections.",Toast.LENGTH_SHORT).show();
				if(!ej.ej.connected){reconnect_message.show();
									return;
								}
				}


			
			ejplot.clearPlots();
			ejplot.line(ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.length,0);
			ejplot.updatePlots();
			fit_msg.setText("Running.."+count++);
			
			for(int n=0;n<ej.ej.ejdata.length-1;n++){
				if(ej.ej.ejdata.ch1[n]>1.0){
					fit_msg.setText("Found data:"+n+", crossing value:"+ej.ej.ejdata.ch1[n]);
					double min=ej.ej.ejdata.ch1[0],max=ej.ej.ejdata.ch1[0],t1=0,t2=0;
					
					for(int j=0;j<ej.ej.ejdata.length-1;j++){
						if(ej.ej.ejdata.ch1[j]>max){
							max=ej.ej.ejdata.ch1[j];
							t1=ej.ej.ejdata.t1[j];
						}if(ej.ej.ejdata.ch1[j]<min){
							min=ej.ej.ejdata.ch1[j];
							t2=ej.ej.ejdata.t1[j];
						}
					}
					if(t1<t2)fit_msg.setText("Found data> Peak1:"+ej.df3.format(max)+",Peak2:"+ej.df3.format(min)+",Time difference:"+ej.df3.format(t2-t1)+"mS");
					else fit_msg.setText("Found data> Peak1:"+ej.df3.format(min)+"Peak2:"+ej.df3.format(max)+"Time difference:"+ej.df3.format(t1-t2));
					running=false;return;
					
					}
			}			
	      mHandler.postDelayed(cro, 10);
	    }
	};

	
	
	
	
	
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
