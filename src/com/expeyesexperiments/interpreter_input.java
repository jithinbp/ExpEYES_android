package com.expeyesexperiments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;


public class interpreter_input{
	   private static interpreter_input instance = null;
	   public List<String> source_code = new ArrayList<String>();
	   public Iterator<Object> native_iterator;
	   private List<Object> fn_list;
	   private Object last_position;
	   public Iterator<String> cmd_iter;
	   command_executor ex;
	    protected interpreter_input() {
	    ex = new command_executor();
	    }
	    
	    public static interpreter_input getInstance() {
	      if(instance == null) {
	         instance = new interpreter_input();
	      }
	      return instance;
	   }

	    
		   public Object function_from_string(String source){
			     //Log.e("TAG", source);
			    String[] pieces = source.split("(\\()|(\\))");
		    	if(pieces[0].equals("SLEEP")){
		    			return new sleep(toInt(pieces[1]));
		    	}else if(pieces[0].equals("PLOT")){
		    			String[] args = pieces[1].split(",");
		    			return new plot(args[0],args[1],toInt(args[2]),toInt(args[3]),toInt(args[4]),toInt(args[5]));
		    	}else if(pieces[0].equals("LOG")){
		    			String[] args = pieces[1].split(",");
		    			return new log(args[0],toInt(args[1]),toInt(args[2]) );
		    	}else if(pieces[0].equals("LOOP START")){
	    			String[] args = pieces[1].split(",");
	    			return new forloop(toInt(args[0]) );
		    	}else if(pieces[0].equals("LOOP END")){
	    			return new forloop_end();
		    	}else{
		    			return new cmd(source);
		    	}
		   }

	    
	   public void generate_method_list(){
		   fn_list = new ArrayList<Object>();
		   cmd_iter = source_code.iterator();
		   fn_list.clear();
		   while(cmd_iter.hasNext()){
		    	String src = cmd_iter.next();
		    	Object fn = function_from_string(src);
		    	Log.e("ADDING IN MAIN",src);
		    	fn_list.add(fn);
		   }
		   
		   
	   }
	    
	   public void show_children(){
		   while(native_iterator.hasNext()){
			   Object a = native_iterator.next();
			   Log.e("MAIN",a.getClass().getSimpleName());
			   if(a instanceof forloop)((forloop) a).show_children();
		   }
		   init_worm();
	   }
	   
	   public class plot{
	    	public boolean ch1,ch2,ch3,ch4;
	    	public String xlabel,ylabel;
	    	plot(String xl,String yl,int a,int b,int c,int d){
	    		xlabel = xl;
	    		ylabel = yl;
	    		ch1=a==1?true:false;
	    		ch2=b==1?true:false;
	    		ch3=c==1?true:false;
	    		ch4=d==1?true:false;
	    	}
	    }
	   
	   public class sleep{
		   public int delay;
		   sleep(int t){
			   delay=t;
		   }
	   }
	   
	   public class log{
		   public String mnemonic;
		   public boolean ddata,timestamp;
		   log(String s, int a, int b){
			   mnemonic = s;
			   ddata=a==1?true:false;
			   timestamp=b==1?true:false;
		   }
	   }
	   
	   public class cmd{
		   public String command;
		   public Object[] params;
		   public Method meth;
		   cmd(String c){
			   command=c;
			   params = ex.get_params_from_string(c);
			   meth = ex.get_method_from_string(c);
		   }
	   }
	   
	   public class button_widget{
		   public List<Object> fn_list = new ArrayList<Object>();
		   
	   }
	   
	   public class forloop{
			   private int n,pos;
			   private List<Object> loop_fn_list;
			   private Iterator<Object> loop_fn_iter;
			   private Object last_position=null;
			   forloop(int iters){
				   loop_fn_list = new ArrayList<Object>();
				   n=iters;
				   while(cmd_iter.hasNext()){
				    	String src = cmd_iter.next();
				    	Object fn = function_from_string(src);
				    	if(fn instanceof forloop_end){Log.e("FILLED ONE LOOP","JJ"); break;} 
				    	Log.e("ADDING IN LOOP",src);
				    	loop_fn_list.add(fn);
				   }
				   init_worm();
				   return;
				   
				   
			   }
			   public void show_children(){
				   while(loop_fn_iter.hasNext()){
					   Object a = loop_fn_iter.next();
					   Log.e("LOOP",pos+"-----"+a.getClass().getSimpleName());
					   if(a instanceof forloop)((forloop) a).show_children();
				   }
				   init_worm();
			   }
			   
			   
			   public void init_worm(){
				   pos=0;
				   reset_position();
			   }
			   public void reset_position(){
				   loop_fn_iter = loop_fn_list.iterator();
				   while(loop_fn_iter.hasNext()){Object a = loop_fn_iter.next();if(a instanceof forloop)((forloop) a).init_worm();}
				   loop_fn_iter = loop_fn_list.iterator();
				   
			   }
			   public boolean has_next(){
				   return loop_fn_iter.hasNext();
				   
			   }
			   public Object get_next(){
				   Log.e("LOOP says","pos:"+pos);
				   if(pos<n){
					   if(has_next()){return loop_fn_iter.next();}
					   else {pos++;reset_position();return get_next();}
				   }else{
					   return null;
				   }
				   
			   }
		   }
	   
	   public class forloop_end{
		   forloop_end(){}
	   }
	   public class button{
		   button(){}
		   
	   }
	   public class button_end{
		   button_end(){}
	   }
	   
	    public void init_worm(){
	    	native_iterator = fn_list.iterator();
	    	while(native_iterator.hasNext()){Object a = native_iterator.next();if(a instanceof forloop)((forloop) a).init_worm();}
	    	native_iterator = fn_list.iterator();
	    	last_position = null;
	    }
	    public boolean has_next(){
	    	return native_iterator.hasNext();
	    }
	    
	    public Object get_next(){
	    	if(last_position == null){
	    		//Log.e("FOUND NULL","yup. null");
	    		if(has_next()){last_position = native_iterator.next();return get_next();}
	    		else return null;
	    	}else if(last_position instanceof forloop){
	    		forloop tmp = ((forloop) last_position);
	    		//Log.e("LOOP RT",""+last_position.getClass().getSimpleName());
	    		Object fn = tmp.get_next();
	    		if(fn==null) {
	    			//Log.e("LOOP OVER","");
	    			last_position = null;
	    			return get_next();
	    			}
	    		else {Log.e("return type",fn.getClass().getSimpleName());return fn;}
	    		}
	    	else{
	    		Object tmp = last_position;
	    		last_position = null;
	    		//Log.e("STANDARD RT",""+tmp.getClass().getSimpleName());
	    		return tmp;	
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

