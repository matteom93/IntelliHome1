package com.auh.opencomune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class Principale extends Activity {

	private static final int STOPSPLASH = 0;
	   //time in milliseconds
	private static final long SPLASHTIME = 3000;
	   
	final Handler splashHandler = new Handler() {
	      
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
		      	case STOPSPLASH:
		            
		      		Intent lancio = new Intent(Principale.this, App.class);
		    		startActivity(lancio);
		            break;
		         }
		         super.handleMessage(msg);
		      }
		   };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		overridePendingTransition( R.animator.left_in, R.animator.left_out );
		
		Message msg = new Message();
		msg.what = STOPSPLASH;	   
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
		
		   
		    
		
		
	}

    
		
}
