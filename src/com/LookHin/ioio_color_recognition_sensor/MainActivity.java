package com.LookHin.ioio_color_recognition_sensor;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/*
 * # TSC230 Color Sensor & IOIO
 * 
 * S0 = PIN 1
 * S1 = PIN 2
 * 
 * S2 = PIN 4
 * S3 = PIN 5
 * 
 * OUT = PIN 35
 * 
 * OE = PIN GND
 * 
 * VCC = PIN +5V
 * GND = PIN GND
 */

public class MainActivity extends IOIOActivity {
	
	private ToggleButton toggleButton1;
	
	private TextView textView1;
	private TextView textView3;
	private TextView textView5;
	private TextView textView7;
	private TextView textView9;
	
	private float FrequencyRed;
	private float FrequencyBlue;
	private float FrequencyGreen;
	private float FrequencyClear;
	
	private int intR;
	private int intG;
	private int intB;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textView1 = (TextView) findViewById(R.id.textView1);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView7 = (TextView) findViewById(R.id.textView7);
        textView9 = (TextView) findViewById(R.id.textView9);
        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_about:
        	//Toast.makeText(getApplicationContext(), "Show About", Toast.LENGTH_SHORT).show();
        	
        	Intent about = new Intent(this, AboutActivity.class);
    		startActivity(about);
    		
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    } 
    
    
    class Looper extends BaseIOIOLooper {
		
    	private DigitalOutput digital_led0;
    	
    	private DigitalOutput pin_s2;
    	private DigitalOutput pin_s3;
    	
		private PulseInput PulseFrequency;

		@Override
		protected void setup() throws ConnectionLostException {
			
			digital_led0 = ioio_.openDigitalOutput(0,true);
			
			// Output Frequency
			// 0 0 = No
			// 0 1 = 2%
			// 1 0 = 20%
			// 1 1 = 100%
			ioio_.openDigitalOutput(1,true);
			ioio_.openDigitalOutput(2,true);
			
			// Filter Type
			pin_s2 = ioio_.openDigitalOutput(4,false);
			pin_s3 = ioio_.openDigitalOutput(5,false);
						
			PulseFrequency = ioio_.openPulseInput(35, PulseMode.FREQ);
							
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "IOIO Connect", Toast.LENGTH_SHORT).show();
				}
			});
			
		}

		@Override
		public void loop() throws ConnectionLostException {
			
			try{
				digital_led0.write(!toggleButton1.isChecked());			
				
				// Filter Color
				// 0 0 = Red
				// 1 1 = Green
				// 0 1 = Blue
				// 1 0 = Clear
				
				// Red
				pin_s2.write(false);
				pin_s3.write(false);
				Thread.sleep(1000);
				FrequencyRed = PulseFrequency.getFrequency();
				
				// Green
				pin_s2.write(true);
				pin_s3.write(true);
				Thread.sleep(1000);
				FrequencyGreen = PulseFrequency.getFrequency();
				
				// Blue
				pin_s2.write(false);
				pin_s3.write(true);
				Thread.sleep(1000);
				FrequencyBlue = PulseFrequency.getFrequency();
				
				// Clear
				pin_s2.write(true);
				pin_s3.write(false);
				Thread.sleep(1000);
				FrequencyClear = PulseFrequency.getFrequency();
	
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
							
						textView1.setText(String.valueOf((int) FrequencyRed));
						textView3.setText(String.valueOf((int) FrequencyGreen));
						textView5.setText(String.valueOf((int) FrequencyBlue));
						textView7.setText(String.valueOf((int) FrequencyClear));
						
						intR = (int) (FrequencyRed/FrequencyClear*255);
						intG = (int) (FrequencyGreen/FrequencyClear*255);
						intB = (int) (FrequencyBlue/FrequencyClear*255);

						textView9.setBackgroundColor(Color.rgb(intR, intG, intB));
						textView9.setText("RGB("+intR+","+intG+","+intB+")");
					}
				});
	
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			
		}
	}
    

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	    
}
