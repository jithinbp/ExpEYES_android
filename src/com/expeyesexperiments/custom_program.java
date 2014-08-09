package com.expeyesexperiments;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import explib.ejPlot;
 
public class custom_program extends Activity {
 
	LinearLayout plt,widget_layout;
	TextView results_txt;
	ScrollView sv;
	ejPlot ejplot;
	
	long start_time=0;
	Handler mHandler;
	expeyesCommon ej;
	command_executor executor;
	
	boolean interpreter_running=false;
	interpreter_input inp;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program_running);
		
		setTitle("Custom code...");
		
		ej = expeyesCommon.getInstance();
		executor = new command_executor();
		
		inp = interpreter_input.getInstance();
		inp.generate_method_list();
		
		
		
		
		plt=(LinearLayout)findViewById(R.id.plotlayout);
     	ejplot = new ejPlot(this, plt);
     	ejplot.updatePlots();
     	
     	widget_layout=(LinearLayout)findViewById(R.id.widget_layout);
     	results_txt = (TextView)findViewById(R.id.results_txt);
     	sv = (ScrollView)findViewById(R.id.sv1);
     	
     	interpreter_running=true;
    	start_time = System.currentTimeMillis();
    	inp.init_worm();
    	inp.show_children();
    	mHandler = new Handler();
    	//mHandler.postDelayed(interpreter, 1);
    	mHandler.postDelayed(runner, 100);

	}
	
	
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	   	interpreter_running=false;
		
	}
	@Override
	public void onPause(){
		super.onPause();
		interpreter_running=false;
	}
 
	Runnable runner = new Runnable() {  
	    @Override 
	    public void run() {
	    	if(!interpreter_running)return;
	    	//Object fn_call = precompiled_cmd_iter.next();
	    	Object fn_call = inp.get_next();
	    	if(fn_call==null){
	    		interpreter_running=false;
	    		Log.e("DONE",System.currentTimeMillis()-start_time +"");
	    		start_time = System.currentTimeMillis();
	    		Toast.makeText(getBaseContext(),"We're all finished here.",Toast.LENGTH_SHORT).show();
	    		//precompiled_cmd_iter=inp.fn_list.iterator();
	    		//mHandler.postDelayed(runner, 0);
	    		return;}
	    	
	    	//Log.e("RUNNING",fn_call.toString());
	    	
	    	if(fn_call instanceof interpreter_input.sleep ){
	    		interpreter_input.sleep fn = (interpreter_input.sleep)fn_call;
	    		mHandler.postDelayed(runner, fn.delay);return;
	    		}
	    	else if(fn_call instanceof interpreter_input.plot){
	    		interpreter_input.plot fn = (interpreter_input.plot)fn_call;
	    		ejplot.xlabel=fn.xlabel;
	    		ejplot.ylabel=fn.ylabel;
	    		ejplot.clearPlots();
	    		ejplot.setWorld(ej.ej.ejdata.t1[0], ej.ej.ejdata.t1[ej.ej.ejdata.l1-1], -5, 5);
	    		if(fn.ch1){ejplot.line(ej.ej.ejdata.t1,ej.ej.ejdata.ch1,ej.ej.ejdata.l1,0);}
	    		if(fn.ch2){ejplot.line(ej.ej.ejdata.t2,ej.ej.ejdata.ch2,ej.ej.ejdata.l2,1);}
	    		if(fn.ch3){ejplot.line(ej.ej.ejdata.t3,ej.ej.ejdata.ch3,ej.ej.ejdata.l3,2);}
	    		if(fn.ch4){ejplot.line(ej.ej.ejdata.t4,ej.ej.ejdata.ch4,ej.ej.ejdata.l4,3);}
	    		ejplot.updatePlots();
	    	}
	    	else if(fn_call instanceof interpreter_input.log){
	    		interpreter_input.log fn = (interpreter_input.log)fn_call;
	    		results_txt.append(fn.mnemonic+" : ");
	    		if(fn.ddata)results_txt.append(ej.df3.format(ej.ej.ejdata.ddata));
	    		if(fn.timestamp)results_txt.append(ej.ej.ejdata.timestamp+"   ");
	    		results_txt.append("\n");
	    		sv.post(new Runnable() {

	    		   @Override
	    		   public void run() {
	    		     sv.fullScroll(View.FOCUS_DOWN);
	    		   }
	    		});
	    		
	    		
	    		
	    	}
	    	else if(fn_call instanceof interpreter_input.cmd){
	    		interpreter_input.cmd fn = (interpreter_input.cmd)fn_call;
	    		//executor.execute(fn.command);
	    		try {
					fn.meth.invoke(ej.ej,fn.params);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
	    		}
	    	
	    	mHandler.postDelayed(runner, 0);
	    	
	      
	    }
	};

	
	
	
	
	
	
	
	
	
	
	
	
	public int toInt(EditText txt) {
		String val = txt.getText().toString();

		if (val == null || val.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(val);
		}
	}
	
	public int toInt(String txt) {
		if (txt == null || txt.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(txt);
		}
	}
	

}
