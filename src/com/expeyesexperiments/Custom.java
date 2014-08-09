package com.expeyesexperiments;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import explib.ejPlot;
import explib.headers;
import explib.headers.argument;
import explib.headers.function_call;
 
public class Custom extends Activity{
 
	Button button;
	expeyesCommon ej;
	int[] channel_colors = {Color.DKGRAY,Color.RED,Color.rgb(0, 155, 0),Color.BLUE};
	ejPlot ejplot;
	SeekBar timebase;
	String filename = new String();
    private File dataDirectory;
    private Handler mHandler;
    private Button command_list;
    Display display;
    LinearLayout mainlayout,editorlayout,prlayout;
    ArrayAdapter<String> cmdlist;
    Builder reconnect_message;
    Iterator<String> cmd_iter;
    headers heads;
    TextView prog_txt;
    boolean interpreter_running=false;
    ScrollView sv;
    long start_time=0;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_experiment);
		
		display = getWindowManager().getDefaultDisplay();
		mainlayout = (LinearLayout)findViewById(R.id.ll1);
		prlayout = (LinearLayout)findViewById(R.id.pr);
		
		editorlayout = (LinearLayout)findViewById(R.id.editor_layout);
		
		prog_txt = (TextView)findViewById(R.id.prog_txt);
		
		
		
		dataDirectory = new File(Environment.getExternalStorageDirectory()+"/expeyes/custom/");
		Log.e("DIR",dataDirectory.getName());
		dataDirectory.mkdirs();

		
		ej=expeyesCommon.getInstance();
     	setTitle(ej.title+"Make your own program");
        
		
	    reconnect_message = ej.makeReconnectDialog(this);
	    
	    
		command_list = (Button) findViewById(R.id.cmd);
		heads=new headers(getResources().getStringArray(R.array.funcs));
		String[] cmd_list = new String[heads.function_names.size()];
		cmd_list = heads.function_names.toArray(cmd_list);
		
		cmdlist = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,  cmd_list );
	    
		command_list.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
 				open_command_list();
 			}
 		});
		
		
        
	}



    public void add_widget(function_call head){
    	LinearLayout LL = new LinearLayout(this);
        LL.setBackgroundColor(Color.CYAN);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        //LL.setWeightSum(6f);
        LL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        
        TextView lbl=new TextView(this);
        lbl.setText(head.func_name);
        lbl.setTextSize(18);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 4f));
        LL.addView(lbl);
        Iterator<argument> iter = head.args.iterator();
        while(iter.hasNext()){
        		argument a = iter.next();
        		if(a.type.equals("int")){
        				EditText arg = new EditText(this);
        				//arg.setFilters(new InputFilter[]{ new InputFilterMinMax(a.min, a.max)});
        				arg.setHint(a.name);
        				arg.setInputType(InputType.TYPE_CLASS_NUMBER);
        				arg.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 4f));
        				LL.addView(arg);
        				}
        		}
        
        editorlayout.addView(LL);
    	

    }

    
    public void add_plot_widget(View v){
    	View view = getLayoutInflater().inflate(R.layout.plot_layout, null);
        editorlayout.addView(view);
    }
    public void add_log_widget(View v){
    	View view = getLayoutInflater().inflate(R.layout.display_layout, null);
        editorlayout.addView(view);
    }

    public void add_sleep_widget(View v){
    	View view = getLayoutInflater().inflate(R.layout.sleep_widget_layout, null);
        editorlayout.addView(view);
    }

    public void add_loop_start_widget(View v){
    	View view = getLayoutInflater().inflate(R.layout.loop_layout, null);
        editorlayout.addView(view);
    	
    }
    public void add_loop_stop_widget(View v){
    	View view = getLayoutInflater().inflate(R.layout.loop_stop_layout, null);
        editorlayout.addView(view);
    	
    }
    public void open_command_list(){
    	new AlertDialog.Builder(this)
    	  .setTitle("Select the command you require")
    	  .setAdapter(cmdlist, new DialogInterface.OnClickListener() {

    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	      // TODO: user specific action
    	    	Log.e("CLICKED",heads.fetch_function(cmdlist.getItem(which)).func_name+"" );
    	    	add_widget(heads.fetch_function(cmdlist.getItem(which)));   	
    	    	
    	      dialog.dismiss();
    	    }
    	  }).create().show();
    }
    
	@Override
	public void onDestroy(){
		super.onDestroy();
		
	}
 
	public void code_it(View vvv){
		prog_txt.setText("");
		int childcount = editorlayout.getChildCount();
		for (int i=0; i < childcount; i++){
		      LinearLayout v = (LinearLayout) editorlayout.getChildAt(i);
		      
		      String name = ((TextView) v.getChildAt(0)).getText().toString();
		      String s=name+"(";
		      if(name.equals("SLEEP")){ s+=toInt((EditText) v.getChildAt(1))+")" ; }
		      else if(name.equals("PLOT")){
		    	  s+=((TextView) v.getChildAt(1)).getText() +","+((TextView) v.getChildAt(2)).getText() ;
		      	  for(int k=3;k<7;k++){
		      		  CheckBox c = (CheckBox) v.getChildAt(k);
		      		  if(c.isChecked())s+=",1";//prog_txt.append(",1");
		      		  else s+=",0";//prog_txt.append(",0");
		      	  	}
		      	 s+=")";
		      	
		      }
		      else if(name.equals("LOG")){
		    	  s+=((TextView) v.getChildAt(1)).getText();
		      	  CheckBox c = (CheckBox) v.getChildAt(2);
		      	  if(c.isChecked())s+=",1";
		      	  else s+=",0";
		      	  CheckBox c2 = (CheckBox) v.getChildAt(3);
		      	  if(c2.isChecked())s+=",1";
		      	  else s+=",0";
	      	  	 s+=")";
		      }
		      else if(name.equals("LOOP START")){
		    	  s+=((TextView) v.getChildAt(1)).getText();
		      	  s+=")";
		      }
		      else if(name.equals("LOOP END")){
		    	  s+=")";
		      }
		      else if(heads.function_name_list.contains(name)){
		    	  int subchildcount = v.getChildCount();
		    	  for (int j=1; j < subchildcount-1; j++){
			    	  EditText v2 = (EditText) v.getChildAt(j);
			    	  s+=v2.getText()+",";
			      }
		    	  EditText v2 = (EditText) v.getChildAt(subchildcount-1);
		    	  s+=v2.getText()+")";
		      }
		      else{
		    	  Log.e("COMPILE ERROR",s); 
		    	  continue;
		      }
		      
		      prog_txt.append(s+"\n");
		}

		
		
		
		
		
	}
	
	public boolean compile_it(){
        String[] ss = prog_txt.getText().toString().split("\n");
        if(ss.length>0){
        	interpreter_input inp = interpreter_input.getInstance();
	    	inp.source_code = Arrays.asList(ss);
	    	
	    	return true;
	    }
	    else{
	    	Toast.makeText(getBaseContext(),"Try adding some commands, and then click on 'COMPILE'.",Toast.LENGTH_SHORT).show();
	    	return false;
	    }

	}
	
	public void run_it(View v){
		if(!compile_it())return;
		final Context context = this;
		
		Intent intent = new Intent(context, custom_program.class);
        startActivity(intent);
		
	}

		
	
	public int toInt(EditText txt) {
		String val = txt.getText().toString();

		if (val == null || val.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(val);
		}
	}
	
}
