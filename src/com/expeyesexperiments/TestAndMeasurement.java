package com.expeyesexperiments;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class TestAndMeasurement extends Activity {
	expeyesCommon ej;
	TextView timebase_label, msg, A1, A2, IN1, IN2, SEN;
	SeekBar sqr1_slider, sqr2_slider, sqrs_slider, dac_slider;
	EditText sqr1, sqr2, sqrs, dac, sqrs_phase;
	TextView sqr1_val, sqr2_val, dac_val, cap_val, res_val, freq_val;
	private Handler mHandler;
	long time = 0, t2 = 0, n = 0;
	String sA1 = new String(), sA2 = new String(), sIN1 = new String(),
			sIN2 = new String(), sSEN = new String();
	DecimalFormat df = new DecimalFormat("#.###");
	DecimalFormat df1 = new DecimalFormat("#.#");
	DecimalFormat df0 = new DecimalFormat("#.");
	private Builder reconnect_message;

	boolean running = true, busy = false;
	// ,setOD1=false,setCCS=false,setSqrs=false,setSqr1=false,setSqr2=false,setDac=false,getCap=false,getRes=false;
	CompoundButton OD1, CCS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testandmeasurement);
		Toast.makeText(getBaseContext(), "Test And Measurement Suite",
				Toast.LENGTH_SHORT).show();

		ej = expeyesCommon.getInstance();
		setTitle(ej.title + "Test and Measurement Suite");
		reconnect_message = ej.makeReconnectDialog(this);

		msg = (TextView) findViewById(R.id.msg);
		A1 = (TextView) findViewById(R.id.A1);
		A2 = (TextView) findViewById(R.id.A2);
		IN1 = (TextView) findViewById(R.id.IN1);
		IN2 = (TextView) findViewById(R.id.IN2);
		SEN = (TextView) findViewById(R.id.SEN);

		sqr1_val = (TextView) findViewById(R.id.sqr1_value);
		sqr2_val = (TextView) findViewById(R.id.sqr2_value);
		dac_val = (TextView) findViewById(R.id.dac_value);
		cap_val = (TextView) findViewById(R.id.cap_value);
		res_val = (TextView) findViewById(R.id.res_value);
		freq_val = (TextView) findViewById(R.id.freq_value);

		sqr1_slider = (SeekBar) findViewById(R.id.sqr1_slider);
		sqr2_slider = (SeekBar) findViewById(R.id.sqr2_slider);
		sqrs_slider = (SeekBar) findViewById(R.id.sqrs_slider);
		dac_slider = (SeekBar) findViewById(R.id.dac_slider);

		sqr1 = (EditText) findViewById(R.id.sqr1_entry);
		sqr2 = (EditText) findViewById(R.id.sqr2_entry);
		sqrs = (EditText) findViewById(R.id.DURATION);
		sqrs_phase = (EditText) findViewById(R.id.sqrs_phase);
		dac = (EditText) findViewById(R.id.dac_entry);


	}

	@Override
	public void onResume(){
		super.onResume();
		sqr1_slider
		.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar,
					int progress, boolean fromUser) {
				sqr1.setText(progress + "");
			}
		});

		sqr2_slider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						sqr2.setText(progress + "");
					}
				});
		
		sqrs_slider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						sqrs.setText(progress + "");
					}
				});
		
		dac_slider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}
		
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						dac.setText(progress + "");
					}
				});
		
		OD1 = (CompoundButton) findViewById(R.id.OD1);
		OD1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (OD1.isChecked())
					ej.ej.set_state(10, 1);
				else
					ej.ej.set_state(10, 0);
			}
		});
		
		CCS = (CompoundButton) findViewById(R.id.CCS);
		CCS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				while (busy)
					;
				busy = true;
				if (CCS.isChecked())
					ej.ej.set_state(11, 1);
				else
					ej.ej.set_state(11, 0);
				busy = false;
			}
		});

		mHandler = new Handler();
		t.start(); // fetches values in the background

	}
	
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(getBaseContext(), "Returning to main menu",
				Toast.LENGTH_SHORT).show();
		running = false;
		t.interrupt();
	}

	public double toDouble(EditText txt) {
		String val = txt.getText().toString();

		if (val == null || val.isEmpty()) {
			return 0.0;
		} else {
			return Double.parseDouble(val);
		}
	}

	public void set_sqrs(View v) {
		while (busy)
			;
		busy = true;
		double freq = toDouble(sqrs), phase = toDouble(sqrs_phase);
		ej.ej.set_sqrs(freq, phase);
		sqr1_val.setText(df1.format(ej.ej.ejdata.ddata) + "Hz");
		sqr2_val.setText(df1.format(ej.ej.ejdata.ddata) + "Hz");
		busy = false;
	}

	public void set_sqr1(View v) {
		while (busy)
			;
		busy = true;
		double freq = toDouble(sqr1);
		ej.ej.set_sqr1(freq);
		sqr1_val.setText(df1.format(ej.ej.ejdata.ddata) + "Hz");

		busy = false;
	}

	public void set_sqr2(View v) {
		while (busy)
			;
		busy = true;
		double freq = toDouble(sqr2);
		ej.ej.set_sqr2(freq);
		sqr2_val.setText(df1.format(ej.ej.ejdata.ddata) + "Hz");
		busy = false;
	}

	public void set_dac(View v) {
		while (busy)
			;
		busy = true;

		double val = toDouble(dac) / 1000.0;
		ej.ej.set_voltage(val);
		dac_val.setText(df0.format(ej.ej.ejdata.ddata * 1000) + "mV");
		busy = false;
	}

	public void get_cap(View v) {
		while (busy)
			;
		busy = true;
		ej.ej.measure_cap();
		cap_val.setText(df.format(ej.ej.ejdata.ddata) + "pF");
		busy = false;
	}

	public void get_res(View v) {
		while (busy)
			;
		busy = true;
		ej.ej.measure_res();
		if (ej.ej.ejdata.ddata > 0)
			res_val.setText(df.format(ej.ej.ejdata.ddata) + "Ohm");
		else
			res_val.setText("Resistance value NOT in range (100 Ohm to 100kOhm)");
		busy = false;
		
	}

	public void get_freq(View v) {
		while (busy)
			;
		busy = true;
		ej.ej.get_frequency(3);
		if (ej.ej.ejdata.ddata > 0)
			freq_val.setText(df.format(ej.ej.ejdata.ddata) + "Hz");
		else
			freq_val.setText("Timeout Error");
		busy = false;
	}

	Runnable multimeter = new Runnable() {
		@Override
		public void run() {

			if (!ej.ej.connected)reconnect_message.show();

			A1.setText(sA1);
			A2.setText(sA2);
			IN1.setText(sIN1);
			IN2.setText(sIN2);
			SEN.setText(sSEN);
			if (running)t.run();

		}
	};

	// --------------------THIS THREAD FETCHES THE ACTUAL
	// VALUES-----------------
	Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			if (ej.ej.connected == false) {
				running = false;
				mHandler.postDelayed(multimeter, 1);
				return;
			}

			while (busy);
			busy = true;

			ej.ej.get_voltage(1);
			sA1 = df.format(ej.ej.ejdata.ddata) + " V";
			ej.ej.get_voltage(2);
			sA2 = df.format(ej.ej.ejdata.ddata) + " V";
			ej.ej.get_voltage(3);
			sIN1 = df.format(ej.ej.ejdata.ddata) + " V";
			ej.ej.get_voltage(4);
			sIN2 = df.format(ej.ej.ejdata.ddata) + " V";
			ej.ej.get_voltage(5);
			sSEN = df.format(ej.ej.ejdata.ddata) + " V";

			if (ej.ej.commandStatus != ej.ej.SUCCESS) { // error return
				Toast.makeText(getBaseContext(), "Read error. Thread killed.",
						Toast.LENGTH_SHORT).show();
				running = false;
				mHandler.postDelayed(multimeter, 1);
				return;
			}
			busy = false;

			mHandler.postDelayed(multimeter, 100);
			// multimeter.run();

		}
	});

}
