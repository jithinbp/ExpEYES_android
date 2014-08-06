package com.expeyesexperiments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

public class command_executor {
	private expeyesCommon ej;
	public command_executor(){
		ej=expeyesCommon.getInstance();
		
	}
	
	private int toInt(String arg) {
		if (arg == null || arg.isEmpty()) {
			return -1;
		} else {
			return Integer.parseInt(arg);
		}
	}
	
	public void execute(String name){
		try {
			//Log.e("EXECUTING",name);
			String[] pieces = name.split("(\\()|(\\))");
			String[] args = pieces[1].split(",");
			Class[] param_classes = new Class[args.length];
			Object[] params = new Object[args.length];
			
			for(int i=0;i<args.length;i++){
				 param_classes[i]	=	int.class; //scr.hasNextDouble() ? double.class :
		                			   //scr.hasNextInt() ? int.class : String.class;
		         int val = toInt(args[i]);
		         if(val==-1){Log.e("ARG ERROR","UGH!");return;}
		         params[i] = val;
		         //Log.e("PAR",val+"");
			 }
			Method meth = ej.ej.getClass().getDeclaredMethod(pieces[0],param_classes);
			meth.invoke(ej.ej,params);
			
			} catch (SecurityException e) {
			  // ...
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			  // ...
				e.printStackTrace();
			}
		catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	

	
	public Object[] get_params_from_string(String name){
			//Log.e("EXECUTING",name);
			String[] pieces = name.split("(\\()|(\\))");
			String[] args = pieces[1].split(",");
			Object[] params = new Object[args.length];
			
			for(int i=0;i<args.length;i++){
			     int val = toInt(args[i]);
		         if(val==-1){Log.e("ARG ERROR","UGH!");return null;}
		         params[i] = val;
			}
		    return params;
		}

	public Method get_method_from_string(String name){
		try {
			String[] pieces = name.split("(\\()|(\\))");
			String[] args = pieces[1].split(",");
			Class[] param_classes = new Class[args.length];
			
			for(int i=0;i<args.length;i++){
				 param_classes[i]	=	int.class; //scr.hasNextDouble() ? double.class :
		                			   //scr.hasNextInt() ? int.class : String.class;
		     }
			return ej.ej.getClass().getDeclaredMethod(pieces[0],param_classes);
			
			} catch (SecurityException e) {
			  // ...
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			  // ...
				e.printStackTrace();
			}

		return null;
	}

	
}
