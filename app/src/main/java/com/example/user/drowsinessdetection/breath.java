package com.example.user.drowsinessdetection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

//import android.os.CountDownTimer;


//import ioio.lib.util.AbstractIOIOActivity; //deprecated
//import android.os.CountDownTimer;

//public class breath extends AbstractIOIOActivity implements TextToSpeech.OnInitListener { //this is the old one which was deprecated
public class breath extends IOIOActivity implements TextToSpeech.OnInitListener { //this is the old one which was deprecated
//public class breath extends IOIOActivity implements TextToSpeech.OnInitListener {	

	private  int alcoholPinNumber = 35; //the pin used on IOIO for the alchohol sensor input
	private  int heatPinNumber = 6; //the pin used on IOIO for the alcohol sensor heat pin is now 1
	//private static final int heatPinNumber = 1; //the original one
	//private static final int batteryPinNumber = 41; //the pin used on IOIO for the alchohol sensor input
	//private static final int alcoholPinNumber = 40; 
	private com.example.user.drowsinessdetection.RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private android.graphics.Matrix matrix2;
	private TextView textView_;
	private TextView textView2_;
	private TextView breathalyzerDetected_;	
	private TextView breathalyzerDetected2_;	
	private TextView mainStatus_;
	private TextView mainStatus2_;
	private TextView countdownTimer_;
	private TextView breathResults_;
	private ImageButton breathStartButton_;
	private ImageButton charButton_;
	private ImageButton battery_;
		
	//private AnalogInput alcohol;	
	//private AnalogInput battery;	
	//private DigitalOutput led_;
	//private DigitalOutput heatPin;
	
	private float reading;
	private int baselineReading;	
	//private int minBaseline = 350; //this is the minimum alcohol reading for the alcohol sensor to be declared ready	
	private int breathReady;
	private int highReading;
	private int simMeterReading;
	private int finalReading;
	//private int maxPossibleReading = 850; //the highest value the alcohol sensor can output
	private int maxDifference;  // maxDifference = maxPossibleReading - baselineReading
	private int readingIncrement;
	private long startTime = 8000; 	//5000 is the countdown timer starting number (in milliseconds)
	private long interval = 1000; //1000 is the countdown timer to count down each time (in milliseconds)
	private int currentValue = 0;
	private int lastValue = 0;
	private int resetCounter = 0;
	private int resetCounter2 = 0;
	private int resetCounter3 = 0;
	private int resetBaseline = 0;
	private int breathalyzerFound = 0;
	private int simCounter = 0;
	private int debugCounter = 0;
	
	private BreathCountDownTimer breathTimer; //this is the timer used for the breathalyzer countdown
	private UpdateGaugeTimer updateGaugeTimer; //this is the timer used to just update the gauge
	private long gaugeUpdateInterval = 100; //10 = 100 samples per cent, 100 = 10 samples per second, 1000 = 1 sample per second
	private AnalyzingCountDownTimer analyzingTimer; //this is the timer used for the breathalyzer countdown
	private ResetTimer resetTimer; //this is the timer used to find out if the sensor is stable
	//private BatteryTimer batteryTimer;
	private ConnectTimer connectTimer; 
	private ClearTextTimer clearTextTimer;
	private MediaPlayer mediaPlayer;
	//private int playbackPosition=0;	
	private ProgressDialog pDialog = null;
	private ProgressDialog pResetting = null;
	private ProgressBar breathIndicator;
	//private ProgressBar breathIndicator2;
	private int breathBarStatus = 0;
	private int gaugeReading = 0;
	private float breathConverted;
	private float batteryVoltage;
	private String blowText;	
	private Resources resources;
	private TextView debug_ = null;
	final String TAG = getClass().getName();
		
	//****** preferences variables
	private int character;
	private boolean show_alcohol_value;
	private boolean show_user_acceptance;
	private boolean simulation;
	private boolean video_response;
	private boolean call_taxi = true; //this was in the preferences but took it out 	
	private boolean popups;
	private int countdown;
	private int analyzingTimerDuration = 4000;
	private int min_baseline;
	private int max_value;	
	private int warmupOffset = 200;
	private String tts_no_drinks;
	private String tts_few_drinks;
	private String tts_buzzed;
	private String tts_drunk;
	private boolean call_friend = true; //this was in the preferences but took it out 	
	private String friend_phone;
	private String taxi_phone;
	private boolean disable_sleep;
	private boolean find_friend = true; //this was in the preferences but took it out 	
	private boolean whereami = true; //this was in the preferences but took it out 	
	private boolean send_tweets;
	private boolean send_tweets_gps;
	private String tweet_no_drinks;
	private String tweet_few_drinks;
	private String tweet_buzzed;	
	private String tweet_drunk;
	private String tweet_temp;	
	private boolean facebook_post;
	private boolean facebook_friends;
	private boolean debug;      
	private int taxi_sms_number;
	private boolean shake_start = false;
	private boolean disable_actions;
    //******************************
	boolean tts_on = false;
	
	private AssetFileDescriptor blowMP3;
	private AssetFileDescriptor noDrinksMP3;
	private AssetFileDescriptor fewDrinksMP3;
	private AssetFileDescriptor buzzedMP3;
	private AssetFileDescriptor drunkMP3;
	private AssetFileDescriptor waitMP3;	
	private AssetFileDescriptor BeepMP3;
	private AssetFileDescriptor ReadyBeepMP3;	
	private AssetFileDescriptor connectionLostMP3;
		
	private int state = 0; //if state = 0, we are warming up/idle  if state = 1, we are taking a reading
	
	private TextToSpeech tts;
	private static final int MY_DATA_CHECK_CODE = 1234;
	private TelephonyManager teleMgr = null;
	
	private int result;	
	private int gaugeValue;
	
	private SharedPreferences prefs;
	//private final Handler mTwitterHandler = new Handler();
	private int color_vals[]={R.color.black,R.color.black,R.color.red,R.color.red}; //colors used in the breathalyzer results screen
	///********** Localization String from @string *************
	private String userAcceptanceString; 
	private String userAcceptanceStringTitle;
	private String setupInstructionsString; 
	private String setupInstructionsStringTitle;
	private String notFoundString; 
	private String notFoundStringTitle;
	
	private String instructionsString; 
	private String instructionsStringTitle;
	
	private String batteryLifeString;
	private String batteryLifeStringTitle;
	
	private String blewTooSoonString; 
	private String blewTooSoonStringTitle;
	private String OKText;
	private String AcceptText;
	
	private String level1Result;
	private String level2Result;
	private String level3Result;
	private String level4Result;
	
	private String analyzingText;
	private String justAmomentText;
		
	private String blowForText;
	private String pleaseWaitText;
	private String statusSimulationModeText;
	private String statusResettingText;	
	private String statusInprogressText;
	private String tapTobeginText;
	private String statusReadyText;
	private String statusNotconnectedText;
	private String blowPrompt;
	
	//private short[] frame_ = new short[512];
	private short[] frame_;
  	public static final Bitmap.Config FAST_BITMAP_CONFIG = Bitmap.Config.RGB_565;
  	private byte[] BitmapBytes;
  	private InputStream BitmapInputStream;
  	private Bitmap canvasBitmap;
  	private Bitmap originalImage;	
  	private Bitmap resizedBitmap;  	
  	private int resizedFlag = 0;
    private com.example.user.drowsinessdetection.RgbLedMatrix matrix_;
    private int meterMatch = 0;
	
	//**********************************************************
	
	//private Facebook facebook;
	//**********************************************************
	public static final String APP_ID = "305557519478133";
    private TextView mText;
    private ImageView mUserPic;
    private Handler mHandler;
    ProgressDialog dialog;
    private ListView list; 
    //**********************************************	

	private SharedPreferences mPrefs; //for facebook
	private SharedPreferences resultPrefs; //so the different intents can access the alcohol detector results
	private SensorManager mSensorManager;
	private ShakeListener mSensorListener;
	private String app_ver;	
	private final String tag = "PIXEL Breath";
	ViewFlipper flipper;
	Gauge meter2;
	private boolean KioskMode_ = false;
	private boolean AutoSelectPanel_ = true;
	private int matrix_model;
	private String matrixIdentifier = "m";

	private static String pixelFirmware = "Not Connected";
	private static String pixelBootloader = "Not Connected";
	private static String pixelHardwareID = "Not Connected";
	private static String IOIOLibVersion = "Not Connected";
	private static VersionType v;
	
	private static int resIdPlsWait;
	private static int resIdReady;
	private static int resIdBlow;
	private static int resIdNoDrinks;
	private static int resIdFewDrinks;
	private static int resIdBuzz;
	private static int resIdDrunk;
	private static int resIdIndicator1;
	private static int resIdIndicator2;
	private static int resIdIndicator3;
	private static int resIdIndicator4;
	private static int resIdIndicator5;
	private static int resIdIndicator6;
	private static int resIdIndicator7;
	private static int resIdIndicator8;
	private static int resIdIndicator9;
	private static int resIdIndicator10;
	private static int resIdIndicator11;
	
	private boolean onHomeScreen = true;
	
	
	private Animation inFromRightAnimation() {

			Animation inFromRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			);
			inFromRight.setDuration(500);
			inFromRight.setInterpolator(new AccelerateInterpolator());
			return inFromRight;
			}
			private Animation outToLeftAnimation() {
			Animation outtoLeft = new TranslateAnimation(
			  Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
			  Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			);
			outtoLeft.setDuration(500);
			outtoLeft.setInterpolator(new AccelerateInterpolator());
			return outtoLeft;
		}

		private Animation inFromLeftAnimation() {
			Animation inFromLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			);
			inFromLeft.setDuration(500);
			inFromLeft.setInterpolator(new AccelerateInterpolator());
			return inFromLeft;
			}
			private Animation outToRightAnimation() {
			Animation outtoRight = new TranslateAnimation(
			  Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
			  Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			);
			outtoRight.setDuration(500);
			outtoRight.setInterpolator(new AccelerateInterpolator());
			return outtoRight;
		}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        mHandler = new Handler(); //used by toast
        setContentView(R.layout.main);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        meter2    = (Gauge) findViewById(R.id.meter2);        
           	
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e)
        {
            Log.v(tag, e.getMessage());
        }
               
       //Check if TTS is installed, text to speech
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        Context context = getApplicationContext();
        
        if (debug == true) {
	        CharSequence text = "Checking if the Android Text to Speech Engine is installed";
	        int duration = Toast.LENGTH_SHORT;	
	        Toast toast = Toast.makeText(context, text, duration);
	        toast.show();
        }
        
        //tts.setLanguage(Locale.getDefault());
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode
      
        //******** preferences code
        resources = this.getResources();
        debug_ = (TextView)findViewById(R.id.debug);
        setPreferences();
        //***************************
  
        if (disable_sleep == true) {        	      	
        	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        	//this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }	
   
        textView_ = (TextView)findViewById(R.id.alcohol_reading);
        textView2_ = (TextView)findViewById(R.id.alcohol_reading2);
        breathalyzerDetected_ = (TextView)findViewById(R.id.BreathalyzerDetected); 
        breathalyzerDetected2_ = (TextView)findViewById(R.id.BreathalyzerDetected2); 
        breathResults_ = (TextView)findViewById(R.id.breathResults); 
        
        breathTimer = new BreathCountDownTimer((long)countdown*1000, interval); 
        updateGaugeTimer = new UpdateGaugeTimer((long)countdown*1000 + analyzingTimerDuration, gaugeUpdateInterval); 
        analyzingTimer = new AnalyzingCountDownTimer(analyzingTimerDuration, 1000);
        resetTimer = new ResetTimer(120000, 500); //30 seconds total warm up time but check every second if it's ready yet and cancel timer if it is
    //    batteryTimer = new BatteryTimer(120000, 5000); 
        connectTimer = new ConnectTimer(20000,5000); //pop up a message if it's not connected by this timer
        clearTextTimer = new ClearTextTimer(8000,1000); //pop up a message if it's not connected by this timer
        mainStatus_ = (TextView)findViewById(R.id.mainStatus); 
        mainStatus2_ = (TextView)findViewById(R.id.mainStatus2); 
          
        breathStartButton_ = (ImageButton)findViewById(R.id.beerButton);
      //  breathStartButton_.setVisibility(View.GONE);
        charButton_ = (ImageButton)findViewById(R.id.char_button);    
      //  battery_ = (ImageButton)findViewById(R.id.batteryStatus);    
        breathIndicator = (ProgressBar)findViewById(R.id.breathBar);
       // breathIndicator.setVisibility(View.GONE);
        
        //**** Alert Box Dialogs, the text comes from strings.xml for locatlization *****
        userAcceptanceString = getResources().getString(R.string.userAcceptanceString);
        userAcceptanceStringTitle = getResources().getString(R.string.userAcceptanceStringTitle);   
        
        setupInstructionsString = getResources().getString(R.string.setupInstructionsString);
        setupInstructionsStringTitle = getResources().getString(R.string.setupInstructionsStringTitle);   
        
        notFoundString = getResources().getString(R.string.notFoundString);
        notFoundStringTitle = getResources().getString(R.string.notFoundStringTitle);   
        
        instructionsString = getResources().getString(R.string.instructionsString);
        instructionsStringTitle = getResources().getString(R.string.instructionsStringTitle);   
        
        batteryLifeString = getResources().getString(R.string.batteryInfoString);
        batteryLifeStringTitle = getResources().getString(R.string.batteryInfoStringTitle);   
        
        blewTooSoonString = getResources().getString(R.string.blewTooSoonString);
        blewTooSoonStringTitle = getResources().getString(R.string.blewTooSoonStringTitle);  
        
        OKText = getResources().getString(R.string.OKText);  
        AcceptText = getResources().getString(R.string.AcceptText);  
        
        analyzingText = getResources().getString(R.string.analyzingText);
        justAmomentText = getResources().getString(R.string.justAmomentText);      
        
        level1Result = getResources().getString(R.string.level1Result);
        level2Result = getResources().getString(R.string.level2Result);
        level3Result = getResources().getString(R.string.level3Result);
        level4Result = getResources().getString(R.string.level4Result);
        
        blowForText = getResources().getString(R.string.blowForText);
        blowText = getResources().getString(R.string.blowText);
        pleaseWaitText = getResources().getString(R.string.pleaseWaitText);
        statusSimulationModeText = getResources().getString(R.string.statusSimulationModeText);
        statusResettingText = getResources().getString(R.string.statusResettingText);
        statusInprogressText = getResources().getString(R.string.statusInprogressText);
        tapTobeginText = getResources().getString(R.string.tapTobeginText);
        statusReadyText = getResources().getString(R.string.statusReadyText);
        statusNotconnectedText = getResources().getString(R.string.statusNotconnectedText);
        blowPrompt = getResources().getString(R.string.blowPrompt);
        
    	//*********************************************************************************  
      
        if (show_user_acceptance) {
        	AlertDialog.Builder alert=new AlertDialog.Builder(this);
        	alert.setTitle(userAcceptanceStringTitle).setIcon(R.drawable.icon).setMessage(userAcceptanceString).setNeutralButton(AcceptText, null).show();
        }
        
      //  breathIndicator.setMax(max_value - warmupOffset); //this sets the max which is defined in the preferences, default 850
        breathIndicator.setMax(max_value);
       // teleMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);        
         
        if (shake_start == true) {
        
	        mSensorListener = new ShakeListener();
	        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	        mSensorManager.registerListener(mSensorListener,
	            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	            SensorManager.SENSOR_DELAY_UI);
	
	        mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
	
	            public void onShake() {
	              //Toast.makeText(breath.this, "Shake!", Toast.LENGTH_SHORT).show();
	              breathStartButtonGo();
	            }
	          });
	        
	        mSensorManager.unregisterListener(mSensorListener);
        
        }
        
        if (simulation == true && breathalyzerFound == 0) {
    	  enableUi(true);
    	  setDetected(statusSimulationModeText);
    	  setStatus (tapTobeginText);
    	  
    	  if (shake_start == true) {
    	  
	    	  mSensorManager.registerListener(mSensorListener,
				        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				        SensorManager.SENSOR_DELAY_UI);  	  
    	  }
    	  
      }
      else {
    	  enableUi(false);   
    	  connectTimer.start(); //this timer will pop up a message box if the Breathalyzer is not found
      }
        
       
        
    }
    
    
    
    public void say(String text2say) {
    	tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
    	tts.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);    	
    }
  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.mainmenu, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
       
	  if (item.getItemId() == R.id.menu_instructions) {
	    	AlertDialog.Builder alert=new AlertDialog.Builder(this);
	      	alert.setTitle(setupInstructionsStringTitle).setIcon(R.drawable.icon).setMessage(setupInstructionsString).setNeutralButton(OKText, null).show();
	   }
	  
	  if (item.getItemId() == R.id.menu_about) {
		  
		    AlertDialog.Builder alert=new AlertDialog.Builder(this);
	      	alert.setTitle(getString(R.string.menu_about_title)).setIcon(R.drawable.icon).setMessage(getString(R.string.menu_about_summary) + "\n\n" + getString(R.string.versionString) + " " + app_ver).setNeutralButton(OKText, null).show();	
	   }
    	
    	if (item.getItemId() == R.id.menu_prefs)
       {
    		
    		//we need to kill any running timers or we'll get a frame size error if the LED panel was changed in prefs
    		if (breathTimer != null) breathTimer.cancel(); 
     		if (updateGaugeTimer != null) updateGaugeTimer.cancel();
     		
    		Intent intent = new Intent()
           		.setClass(this,
           				com.example.user.drowsinessdetection.preferences.class);
       
           this.startActivityForResult(intent, 1);
       }
    	
    //	if (item.getItemId() == R.id.menu_socialmediaaccounts) //social media accounts screen
     //   {
     		
     //		Intent intent = new Intent()
       //     		.setClass(this,
         //   				com.ledpixelart.breath.SocialMediaAccounts.class);
        
       //     this.startActivityForResult(intent, 0);
       // }    	
              
       return true;
    }
    
    public void charButtonEvent(View view) {
		 
       	
    	Intent intent = new Intent()
   		.setClass(this,
   				com.example.user.drowsinessdetection.preferences.class);
    	this.startActivityForResult(intent, 0);
		
	}
    
    public void iButtonEvent (View view) {
    	
 	    	AlertDialog.Builder alert=new AlertDialog.Builder(this); 	    	
 	      	alert.setTitle(instructionsStringTitle).setIcon(R.drawable.icon).setMessage(instructionsString).setNeutralButton(OKText, null).show();
    }
    
    public void batteryInfoEvent (View view) {
    	
	    	AlertDialog.Builder alert=new AlertDialog.Builder(this); 	    	
	      	alert.setTitle(batteryLifeStringTitle).setIcon(R.drawable.icon).setMessage(batteryLifeString).setNeutralButton(OKText, null).show();
    }
    
    
    private void addPreferencesFromResource(int preferences) {
		// TODO Auto-generated method stub
		
	}

	//now let's get data back from the preferences activity below
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) //we'll go into a reset after this
    {
    	super.onActivityResult(reqCode, resCode, data);
    	
    	// if (debug == true) {
    	//	 Toast.makeText(getBaseContext(), "On Activity Result Code: " + reqCode, Toast.LENGTH_LONG).show();
        // }      	
    	
    	//setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
    	
    	//breathIndicator.setMax(max_value - warmupOffset); //this sets the max which is defined in the preferences
        
    	 if (simulation == true && breathalyzerFound == 0) {
    		  enableUi(true);
    		  setDetected(statusSimulationModeText);
    		  setStatus (tapTobeginText);
    		  connectTimer.cancel();
    	}
    	else {
    		  enableUi(false);   
    		  setDetected(statusNotconnectedText);		  
    	} 
    	 
    	 if (reqCode == 1) { //we came back from preferences so let's take it back to the home beer screen
    		 
    		 setPreferences();
    		 
    		 //only go back if we're not on the home screen
    		 
    		 if (!onHomeScreen) {
	    		 flipper.setInAnimation(inFromLeftAnimation());
	             flipper.setOutAnimation(outToRightAnimation());
	             flipper.showPrevious(); 
	             onHomeScreen = true;
    		 }
    	 }
    	
    	if (reqCode == MY_DATA_CHECK_CODE)
        {
            if (resCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                // TTS installed, create instance
                tts = new TextToSpeech(this, this);
                Context context = getApplicationContext();
                
                if (debug == true) {
	                CharSequence text = "Text to Speech Engine is installed";
	                int duration = Toast.LENGTH_SHORT;
	                Toast toast = Toast.makeText(context, text, duration);
	                toast.show();
                }                
            }
            else
            {
                // TTS not installed, install it

                Context context = getApplicationContext();
               
                CharSequence text = "The Android Text to Speech Engine is NOT installed, you will now be prompted to install it";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
               

                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    	

    	//String extraData=data.getStringExtra("ComingFrom");
    	//debug_.setText(extraData);    	
    	
    	
    }
    
   private void setPreferences() //here is where we read the shared preferences into variables
    {
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

     character = Integer.valueOf(prefs.getString(   //the selected character 0, 1, 2, 3, 4
    	        resources.getString(R.string.selected_character),
    	        resources.getString(R.string.character_default_value)));   
     
     countdown = Integer.valueOf(prefs.getString(   
 	        resources.getString(R.string.pref_countdown),
 	        resources.getString(R.string.countdown_duration)));   
     
     alcoholPinNumber = Integer.valueOf(prefs.getString(   
  	        resources.getString(R.string.pref_alcoholPin),
  	        resources.getString(R.string.alcoholPinDefault)));   
        
     //countdown = Integer.valueOf(prefs.getString("pref_countdown", resources.getString(R.string.countdown_duration))); //countdown duration
     
    
     KioskMode_ =  prefs.getBoolean("pref_KioskMode", false);
     
     if (KioskMode_) {
    	 countdown = 1000000000; //make it really high for the gauge timer as we want it to run the whole time
     }
     
     show_alcohol_value =  prefs.getBoolean("pref_show_alcohol_value", false); //show the numeric alcohol value or not
     show_user_acceptance =  prefs.getBoolean("pref_show_user_acceptance", false); //if true, a user acceptance box will come up on each startup
     simulation =  prefs.getBoolean("pref_simulation", false); //whether or not to run in simulation mode with no sensor
    // video_response =  prefs.getBoolean("pref_video_response", false); //whether or not to show a video animation of the response
    // call_taxi =  prefs.getBoolean("pref_taxi", false); //whether or not to call taxi on buzzed or drunk   
    // find_friend =  prefs.getBoolean("pref_findFriend", true); //whether or not to call taxi on buzzed or drunk 
    // whereami = prefs.getBoolean("pref_whereami", true);
    // call_friend = prefs.getBoolean("pref_friend", true); //whether or not to call taxi on buzzed or drunk   
    // popups =  prefs.getBoolean("pref_popups", false); //whether or not to show warming up and resettings as popups
     disable_sleep = prefs.getBoolean("pref_sleep", true);
     send_tweets = prefs.getBoolean("pref_tweet", false);   
     send_tweets_gps = prefs.getBoolean("pref_tweet_gps", false);        
     facebook_post = prefs.getBoolean("pref_facebook_post", true);   //whether or not to post results to facebook
     facebook_friends = prefs.getBoolean("pref_facebook_friends", false);  //whether or not to find facebook friends on buzzed or drunk
     debug = prefs.getBoolean("pref_debug", false);  //whether or not to show debug text
    // shake_start = prefs.getBoolean("pref_shake_start", false);  //whether or not to show debug text
     disable_actions = prefs.getBoolean("pref_actions", true);  //whether or not to show the actions screen
    
     
     tweet_no_drinks = prefs.getString( 
 	        resources.getString(R.string.pref_tweet_no_drinks), 	        
 	        resources.getString(R.string.tweet_no_drinks)); 
     
     tweet_few_drinks = prefs.getString(   
  	        resources.getString(R.string.pref_tweet_few_drinks),
  	        resources.getString(R.string.tweet_few_drinks));  
     
     tweet_buzzed = prefs.getString(   
   	        resources.getString(R.string.pref_tweet_buzzed),
   	        resources.getString(R.string.tweet_buzzed));  
     
     tweet_drunk = prefs.getString(   
    	        resources.getString(R.string.pref_tweet_drunk),
    	        resources.getString(R.string.tweet_drunk));       
     
     min_baseline = Integer.valueOf(prefs.getString(   
  	        resources.getString(R.string.pref_min_baseline),
  	        resources.getString(R.string.min_baseline)));  
     
     max_value = Integer.valueOf(prefs.getString(   
   	        resources.getString(R.string.pref_max_value),
   	        resources.getString(R.string.max_value)));  
     
     taxi_sms_number = Integer.valueOf(prefs.getString(   
    	        resources.getString(R.string.pref_taxi_sms),
    	        resources.getString(R.string.taxi_sms_default)));  
     
     
     tts_no_drinks = prefs.getString(   
    	        resources.getString(R.string.pref_tts_no_drinks),
    	        resources.getString(R.string.tts_no_drinks));  
     
     tts_few_drinks = prefs.getString(   
    	        resources.getString(R.string.pref_tts_few_drinks),
    	        resources.getString(R.string.tts_few_drinks));  
     
     tts_buzzed = prefs.getString(   
 	        resources.getString(R.string.pref_tts_buzzed),
 	        resources.getString(R.string.tts_buzzed));  
     
     tts_drunk = prefs.getString(   
 	        resources.getString(R.string.pref_tts_drunk),
 	        resources.getString(R.string.tts_drunk)); 
     
     friend_phone = prefs.getString(   
  	        resources.getString(R.string.pref_friend_phone),
  	        resources.getString(R.string.friend_phone)); 
     
     taxi_phone = prefs.getString(   
   	        resources.getString(R.string.pref_taxi_phone),
   	        resources.getString(R.string.taxi_phone)); 
       
   //  pref_tweet_login_status = "test";
     
     
     if (character == 4) { //if text to speech
    	 tts_on = true;
     }
     else { 
    	 tts_on = false;
     }
     
     //here we are setting the char icon button dynamically by calling the respective image selector xml
    charButton_ = (ImageButton)findViewById(R.id.char_button);  //had to re-declare this one, was crashes if not, I suppose it's because preferences is a separate activity
    switch (character) {
 	case 0:		//old english			
 		charButton_.setImageResource(R.drawable.englishselector);
 		break;					
 	case 1:		//pirate
 		charButton_.setImageResource(R.drawable.pirateselector);
 		break;		
 	case 2:		//spooky halloween			
 		charButton_.setImageResource(R.drawable.spookyselector);
 		break;		
 	case 3:		//insult (new york cabbie)			
 		charButton_.setImageResource(R.drawable.insultselector);
 		break;		
 	case 4:	//text to speech, change this later				
 		charButton_.setImageResource(R.drawable.ttsselector);
 		break;		
   }
    
    
    AutoSelectPanel_ = prefs.getBoolean("pref_AutoSelectPanel", true);
    
	   
    matrix_model = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
   	        resources.getString(R.string.selected_matrix),
   	        resources.getString(R.string.matrix_default_value))); 
    
    /* PIXEL V2 supported panels
    I: 32x16 adafruit
    P: 32x32 seeed, the panel for PIXEL V2
    S: 64x64 seeed, super pixel original
    K: 32x32 seeed kiosk no writing
    V: 64x64 seeed kiosk no writing

    PIXEL V2.5 additional supported panels
    Q: 32x32 adafruit d pin
    C: 32x32 adafruit color swap
    R: 64x32 adafruit d pin
    T: 64x64 adafruit
    X: 64x64 adafruit kiosk no writing
    Y: 32x32 adafruit d pin kiosk no writing

    L: 32x16 low power and 32x16
    M: 32x32 low power and Seeed 32x32
    N: 32x32 low power and Adafruit 32x32 D-Pin*/
    
    //if (AutoSelectPanel_ && !pixelHardwareID.substring(0,4).equals("PIXL") && pixelHardwareID.substring(0,4).equals("XXXXX")) { //if auto select is on and it's NOT a PIXEL V2 board AND it's a PIXEL V2.5 or above board
   
	  if (AutoSelectPanel_ && pixelHardwareID.substring(0,4).equals("PIXL") && !pixelHardwareID.substring(4,5).equals("0")) { // PIXL0008 or PIXL0009 is the normal so if it's just a 0 for the 5th character, then we don't go here
	    	
	    	 	//let's first check if we have a matching firmware to auto-select and if not, we'll just go what the matrix from preferences
		  
		  		if (pixelHardwareID.substring(4,5).equals("Q")) {
	    	 		matrix_model = 11;
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
	    	 		matrixIdentifier = "m";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("T")) {
	    	 		matrix_model = 14;
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_64x64;
	    	 		matrixIdentifier = "s";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("I")) {
	    	 		matrix_model = 1; 
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
	    	 		matrixIdentifier = "i";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("L")) { //low power
	    	 		matrix_model = 1; 
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
	    	 		matrixIdentifier = "i";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("C")) {
	    	 		matrix_model = 12; 
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32_ColorSwap;
	    	 		matrixIdentifier = "m";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("R")) {
	    	 		matrix_model = 13; 
	    	 		KIND = com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_64x32;
	    	 		matrixIdentifier = "w";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("M")) { //low power
	    	 		 matrix_model = 3;
	    	 		 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //pixel v2
	    	 		 matrixIdentifier = "m";
	    	 	}
	    	 	else if (pixelHardwareID.substring(4,5).equals("N")) { //low power
	    	 		 matrix_model = 11;
	    	 		 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32; //pixel v2.5
	    	 		 matrixIdentifier = "m";
	    	 	}
	    	 	else {  //in theory, we should never go here
	    	 		KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
	    	 		matrixIdentifier = "m";
	    	 	}
	  		}	
	  
	       else {
		     switch (matrix_model) {  //get this from the preferences
			     case 0:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;
			    	 matrixIdentifier = "i";
			    	 break;
			     case 1:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
			    	 matrixIdentifier = "i";
			    	 break;
			     case 2:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1, this matrix was never used
			    	 matrixIdentifier = "m";
			    	 break;
			     case 3:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2
			    	 matrixIdentifier = "m";
			    	 break;
			     case 4:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_64x32;
			    	 matrixIdentifier = "w";
			    	 break;
			     case 5:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x64;
			    	 matrixIdentifier = "w";
			    	 break;	 
			     case 6:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_2_MIRRORED;
			    	 matrixIdentifier = "w";
			    	 break;	 	 
			     case 7: //this one doesn't work and we don't use it rigth now
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_4_MIRRORED;
			    	 matrixIdentifier = "s";
			    	 break;
			     case 8:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_128x32; //horizontal
			    	 matrixIdentifier = "s";
			    	 break;	 
			     case 9:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x128; //vertical mount
			    	 matrixIdentifier = "s";
			    	 break;	 
			     case 10:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_64x64;
			    	 matrixIdentifier = "s";
			    	 break;
			     case 11:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
			    	 matrixIdentifier = "m";
			    	 break;	 
			     case 12:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_32x32_ColorSwap;
			    	 matrixIdentifier = "m";
			    	 break;	 	 
			     case 13:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_64x32;
			    	 matrixIdentifier = "w";
			    	 break;	
			     case 14:
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.ADAFRUIT_64x64;
			    	 matrixIdentifier = "s";
			    	 break;	 	 	
			     default:	    		 
			    	 KIND =  com.example.user.drowsinessdetection.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default
			    	 matrixIdentifier = "m";
			     }
	    	 }
    
    
	  resIdPlsWait = getResources().getIdentifier(matrixIdentifier + "plswait" , "raw", this.getPackageName());
	  resIdReady = getResources().getIdentifier(matrixIdentifier + "ready" , "raw", this.getPackageName());
	  resIdBlow = getResources().getIdentifier(matrixIdentifier + "blow" , "raw", this.getPackageName());
	  resIdNoDrinks = getResources().getIdentifier(matrixIdentifier + "nodrinks" , "raw", this.getPackageName());
	  resIdFewDrinks = getResources().getIdentifier(matrixIdentifier + "fewdrinks" , "raw", this.getPackageName());
	  resIdBuzz = getResources().getIdentifier(matrixIdentifier + "buzz" , "raw", this.getPackageName());
	  resIdDrunk = getResources().getIdentifier(matrixIdentifier + "drunk" , "raw", this.getPackageName());
	  
	  resIdIndicator1 = getResources().getIdentifier(matrixIdentifier + "1" , "raw", this.getPackageName());
	  resIdIndicator2 = getResources().getIdentifier(matrixIdentifier + "2" , "raw", this.getPackageName());
	  resIdIndicator3 = getResources().getIdentifier(matrixIdentifier + "3" , "raw", this.getPackageName());
	  resIdIndicator4 = getResources().getIdentifier(matrixIdentifier + "4" , "raw", this.getPackageName());
	  resIdIndicator5 = getResources().getIdentifier(matrixIdentifier + "5" , "raw", this.getPackageName());
	  resIdIndicator6 = getResources().getIdentifier(matrixIdentifier + "6" , "raw", this.getPackageName());
	  resIdIndicator7 = getResources().getIdentifier(matrixIdentifier + "7" , "raw", this.getPackageName());
	  resIdIndicator8 = getResources().getIdentifier(matrixIdentifier + "8" , "raw", this.getPackageName());
	  resIdIndicator9 = getResources().getIdentifier(matrixIdentifier + "9" , "raw", this.getPackageName());
	  resIdIndicator10 = getResources().getIdentifier(matrixIdentifier + "10" , "raw", this.getPackageName());
	  resIdIndicator11 = getResources().getIdentifier(matrixIdentifier + "11" , "raw", this.getPackageName());
	  
	 //KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default
	BitmapInputStream = getResources().openRawResource(resIdPlsWait);
	
	frame_ = new short [KIND.width * KIND.height];
    BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
		 
    loadRGB565(); //this function loads a raw RGB565 image to the matrix
    
    int BoardID = 0;
	
 	try {
 		BoardID = Integer.parseInt(pixelHardwareID.substring(6,8));  //PIXL0025
 		//showToast(String.valueOf(BoardID));
 	} catch(NumberFormatException nfe) {
 	  // Handle parse error.
 	}
 	
 	 if (matrix_model > 10 && BoardID < 24 && BoardID !=0) { //we have a PIXEL V2 board pixl0025 or IOIO0401   25 or 01 pixl0025 TO DO Change this later to hardware ID
 		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setTitle(getResources().getString(R.string.unsupportedPanel)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.unsupportedPanelMsg)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
 	 }
 }
   
   private void updatePrefs() //here is where we read the shared preferences into variables
   {
   	 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);     
   	 setPreferences();
   }
   
  
	
	//class IOIOThread extends AbstractIOIOActivity.IOIOThread {  //only goes here if the Breathalyzer (IOIO) was detected
    class Looper extends BaseIOIOLooper {
    	private AnalogInput alcohol;	
    	private AnalogInput battery;	
    	private DigitalOutput led_;
    	private DigitalOutput heatPin;	
    	
    	public void setup() throws ConnectionLostException {
	    //protected void setup() throws ConnectionLostException {
    		
    		//**** let's get IOIO version info for the About Screen ****
  			pixelFirmware = ioio_.getImplVersion(v.APP_FIRMWARE_VER);
  			pixelBootloader = ioio_.getImplVersion(v.BOOTLOADER_VER);
  			pixelHardwareID = ioio_.getImplVersion(v.HARDWARE_VER); 
  			IOIOLibVersion = ioio_.getImplVersion(v.IOIOLIB_VER);
  			//**********************************************************
    		
    		
    		//matrix_ = ioio_.openRgbLedMatrix(KIND);
  			//matrix_.frame(frame_);  //write select pic to the matrix
  			
  			/*if (AutoSelectPanel_ && pixelHardwareID.substring(0,4).equals("PIXL") && !pixelHardwareID.substring(4,5).equals("0")) { //only go here if we have a firmware that is set to auto-detect, otherwise we can skip this
	  			runOnUiThread(new Runnable()
	  			{
	  			   public void run()
	  			   {

	  				   updatePrefs();

	  				   try {
	  					 matrix_ = ioio_.openRgbLedMatrix(KIND);
	 	  	  		     matrix_.frame(frame_); //stream "select image" text to PIXEL
					} catch (ConnectionLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	  			   }
	  			});
  			}

  		   else { //we didn't auto-detect so just go the normal way
  			  matrix_ = ioio_.openRgbLedMatrix(KIND);
  	  		  matrix_.frame(frame_); //stream "select image" text to PIXEL
  		   }*/
						
			try {
				alcohol = ioio_.openAnalogInput(alcoholPinNumber);				
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true); //LED_PIN is the stat LED, it's the LED on the IOIO board, just internal and not part of the Breathalyzer function			
				heatPin = ioio_.openDigitalOutput(heatPinNumber,false);   //heat up the alcohol sensor, was false before	
			//	battery = ioio_.openAnalogInput(batteryPinNumber);	
				
				setDetected("Connected");
				breathalyzerFound = 1;
				connectTimer.cancel(); //we can stop this since it was found
				setStatus (pleaseWaitText); 
				setBreathStatus(statusResettingText);
				
				simulation = false; //if we went here, then the alcohol sensor was detected so turn this off even if it was on in preferences
				
				//*** since we may have gone here after a reset, reset all the variables
				state = 0;
				resetCounter2 = 0;
				resetCounter = 0;
				if (character == 4) { //if text to speech
				     tts_on = true;
				   }
				 else { 
				    tts_on = false;
				 }
				//*********************************************************************
				
				resetTimer.cancel(); //need to call as it may have already been running as we could now be re-starting
				resetTimer.start(); //this timer is used here as the warmup timer
				
			//	batteryTimer.start();
				
				try {         	
					 playWaitMP3();
			     } catch (Exception e) {
			           e.printStackTrace();
			       }	
				
				enableUi(false);
				
				
			} catch (ConnectionLostException e) {
			
				enableUi(false);
				setDetected(statusNotconnectedText);
				setStatus(" "); 			
				
				try {   //play connection lost beep      	
		     		playConnectionLostMP3();
			         } catch (Exception y) {
			           y.printStackTrace();
			    }		

				 if (simulation == true && breathalyzerFound == 0) {
					 enableUi(true);
			      }
			      else {
			    	 enableUi(false);  
			      }	
				throw e;
			}
		}
		
		public void loop() throws ConnectionLostException { //we go here 100 times per second
			
			
			try {
				Thread.sleep(100);
				reading = alcohol.read();				
				
				if (show_alcohol_value == true ) {
					showAlcoholValue(convertReadingText(reading)); //setBreath writes the alcohol value & convertReading converts the raw decimal value to a whole number from 0 to 1000   
		        }
				
							
				if (state == 0) { //no reading is taking place and we are on the home screen
					//breathBarStatus = convertReadingInt(reading) - warmupOffset;
					breathBarStatus = convertReadingIntWarmUp(reading);
					//breathConverted = (breathBarStatus / 3) ; //have to do this to convert to the scale used by the gauge control since the max of that one is 200
					
					
			//	try {  //this code from the ioiomint portable version with the lipo
					//	batteryVoltage = battery.getVoltage(); 
				//	} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
					//	e1.printStackTrace();
				//	}
					
					//runOnUiThread(new Runnable() { //forces it to run on the main ui thread //moved this to a timer so it's no updated so frequently
					  //   public void run() {

					    	// setBatteryMeter();

					   // }
					//});
					
					
					runOnUiThread(new Runnable() { //forces it to run on the main ui thread
					     public void run() {

					    	 //meter1.setValue(breathConverted);
					    	 breathIndicator.setProgress(breathBarStatus);

					    }
					});
					
				}
				
			
			//	Log.v(TAG,"resetcounter3: " + resetCounter3);
		    //	sleep(10);	//deprecated
				
				
				
				
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				
				 if (simulation == true && breathalyzerFound == 0) {
					 enableUi(true);
			      }
			      else {
			    	 enableUi(false);  
			      }	
				
				try {   //play a ready beep      	
		     		playConnectionLostMP3();
			         } catch (Exception y) {
			           y.printStackTrace();
			    }		
				
				enableUi(false);
				setDetected(statusNotconnectedText);
				setStatus(" ");
			
				throw e;
			}
		}
		
		
		
		
	}

	//@Override //this was deprecated
	//protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
	//	return new IOIOThread();
	//}
	
	@Override	
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	//@Override
	//protected IOIOLooper createIOIOLooper() {
		//return new Looper();
	//}
	
	   public void loadImage() {
		  	
		  		int y = 0;
		  		for (int i = 0; i < frame_.length; i++) {
		  			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
		  			y = y + 2;
		  		}
		  		
		  		//we're done with the images so let's recycle them to save memory
			    canvasBitmap.recycle();
			    originalImage.recycle(); 
			    
			    if ( resizedFlag == 1) {
			    	resizedBitmap.recycle(); //only there if we had to resize an image
			    }
			   		

		  	}
	 
	 private void loadRGB565() {

			try {
	   			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
	   																				// the
	   																				// input
	   																				// stream
	   																				// into
	   																				// a
	   																				// byte
	   																				// array
	   			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
	   		} catch (IOException e) {
	   			e.printStackTrace();
	   		}

	   		int y = 0;
	   		for (int i = 0; i < frame_.length; i++) {
	   			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	   			y = y + 2;
	   		}

	   }
	 
	

	private void PrefUI() {
		runOnUiThread(new Runnable() {
			public void run() {
				 if (show_alcohol_value == false ) {
					   textView_.setVisibility(View.INVISIBLE);    
					   textView2_.setVisibility(View.INVISIBLE);    
				 }
			}
		});
	}
	
	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathStartButton_.setEnabled(enable);	
				if (enable == true) {
					breathStartButton_.requestFocus();
					 
				}
				else {
					 breathStartButton_.clearFocus();					 
				}						
			}
		});
	}
	
	
	private boolean checkInternetConnection() { 
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
	 
	    // test for connection 
	    if (cm.getActiveNetworkInfo() != null 
	            && cm.getActiveNetworkInfo().isAvailable() 
	            && cm.getActiveNetworkInfo().isConnected()) { 
	        return true; 
	    } else { 
	        //no conection 
	    	 Toast.makeText(getBaseContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
	        return false; 
	    } 
	} 

	
	public String convertReadingText(float num ) {
	//	num = 1 - num;
		num = num * 1000;
		String numtoString = new DecimalFormat("0").format(num);		
		return(numtoString);
	}
	
	public int convertReadingInt(float num ) {
	//	num = 1 - num;
		num = num * 1000;		
		int FloattoInt = (int)num;
		return(FloattoInt);
	}
	
	public int convertReadingIntWarmUp(float num ) {
		num = 1 - num;
		num = num * 1000;		
		int FloattoInt = (int)num;
		return(FloattoInt);
	}
	
	
	
	private void showAlcoholValue(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				textView_.setText(str);
				textView2_.setText(str);
			}
		});
	}
	
	private void setDetected(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathalyzerDetected_.setText(str);
			}
		});
	}
	
	private void setDetected2(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathalyzerDetected2_.setText(str);
			}
		});
	}
	
	private void setStatus(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				mainStatus_.setText(str);
			}
		});
	}
	
	private void setStatus2(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				mainStatus2_.setText(str);
			}
		});
	}
	
	private void setLevelStatus(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathResults_.setTextColor(getResources().getColor(color_vals[result])); //set the color of the text
				breathResults_.setText(str);
			}
		});
	}
	
	private void setBreathStatus(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathalyzerDetected_.setText(str);
			}
		});
	}
	
	private void setBreathStatus2(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				breathalyzerDetected2_.setText(str);
			}
		});
	}
	
	private void showNotFound() {	
				AlertDialog.Builder alert=new AlertDialog.Builder(this);
				//alert.setTitle("Breathalyzer Not Found").setIcon(R.drawable.icon).setMessage("Please ensure USB Debugging is turned on from Settings --> Applications --> Development --> USB debugging." + "\n" + "\n" + "If using the Breathalyzer in wireless mode (Bluetooth), ensure the Breathalyzer has been paired. The Bluetooth pairing code is: 4545.").setNeutralButton("OK", null).show();		
				alert.setTitle(notFoundStringTitle).setIcon(R.drawable.icon).setMessage(notFoundString).setNeutralButton(OKText, null).show();		
	}
	
	private void showPleaseWait(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				pDialog = ProgressDialog.show(breath.this,"Please wait", str, true);
				pDialog.setCancelable(true);
			}
		});
	}
	
	private void showResettingDialog(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				pResetting = ProgressDialog.show(breath.this,"Please wait", str, true);
				pResetting.setCancelable(true);
			}
		});
	}
	
 //  private void setBatteryMeter() {
	   
	   
	//   if (batteryVoltage > 1.98) {
		 //  battery_.setImageResource(R.drawable.battery5selector);
	//   }
	   
	 //  if (batteryVoltage < 1.98 && batteryVoltage > 1.86) {
		  // battery_.setImageResource(R.drawable.battery4selector);
	  // }	
	   
	 //  if (batteryVoltage < 1.86 && batteryVoltage > 1.74) {
		 //  battery_.setImageResource(R.drawable.battery3selector);
	//   }	
	   
	//   if (batteryVoltage < 1.74 && batteryVoltage > 1.62) {
		  // battery_.setImageResource(R.drawable.battery2selector);
	//   }	
	   
	//   if (batteryVoltage < 1.62) {
		  // battery_.setImageResource(R.drawable.battery1selector);
	 //  }	
	   
  // }
   
	
	public void breathStartButtonEvent(View view) { //go here for beer icon click
		
		if (shake_start == true) {
			mSensorManager.unregisterListener(mSensorListener); //we've started so kill the shake listener
		}
		
		if (KioskMode_) {
			state = 1;
			breathStartKiosk();
		}
		else {
			breathStartNow(); //no buzzer		
		}
	 }
	
	public void breathStartButtonGo() { //go straight here if it was a shake
		if (shake_start == true) {
			mSensorManager.unregisterListener(mSensorListener); //we've started so kill the shake listener
		}
		Vibrator myVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		myVib.vibrate(200);
		breathStartNow(); 
	 }
	
	public void breathStartKiosk() { //the touch event for the screen, no buzzer
		if (shake_start == true) {
			mSensorManager.unregisterListener(mSensorListener); //we've started so kill the shake listener
		}
		
		    flipper.setInAnimation(inFromRightAnimation());
	        flipper.setOutAnimation(outToLeftAnimation());
	        flipper.showNext();  
	        onHomeScreen = false;
		
		    setStatus2 (blowText);
			setBreathStatus2 (statusInprogressText);
			enableUi(false); //we've started to grey out the start button
			//charButton_.setVisibility(View.INVISIBLE); 
		
			try {         	
	           playBlowMP3();
	         } catch (Exception e) {
	             e.printStackTrace();
	         }
		
		    state = 1; //set this state
			baselineReading = convertReadingInt(reading);
			 
			if (debug == true) {
				showToast("Alcohol Baseline Reading: " + baselineReading);
			}	
			 maxDifference = max_value - baselineReading; //now let's find out what the maximum difference can be between the baseline
			 readingIncrement = maxDifference / 10; //depending on how close the user is to the max corresponds to how much they've had to drink
		     highReading = convertReadingInt(reading); //let's also establish the high reading
		     //start the timer
		
			updateGaugeTimer.start();
	}
	
	public void breathStartNow() { //the touch event for the screen, no buzzer
		if (shake_start == true) {
			mSensorManager.unregisterListener(mSensorListener); //we've started so kill the shake listener
		}
		
		if (state == 0) {
			//let's first check that the user didn't blow into the sensor before clicking the button, if the current value is higher than the resetBaseline then the user did
			//resetCounter3 = 0; //this needs to be 0 if we are here again
			
			 if (simulation == true && breathalyzerFound == 0) {
				 setStatus2 (blowForText);
				// setBreathStatus(statusSimulationModeText);
				 setBreathStatus2(statusSimulationModeText); //this is for the second screen
				 enableUi(false); //we've started to grey out the start button
				 //charButton_.setVisibility(View.INVISIBLE);  
		   			try {         	
		   	           playBlowMP3();
		   	         } catch (Exception e) {
		   	             e.printStackTrace();
		   	         }
		   		     
		   		     //now take the baseline
		   		     state = 1; //set this state
		   			 baselineReading = 300;
		   			 //breathIndicator2.setMax(max_value - baselineReading); //this sets the max which is defined in the preferences
		   		     //highReading = 300; //let's also establish the high reading
		   		     //start the timer
		   		     breathTimer.start();
		      }
		      else {
		    		if (convertReadingInt(reading) - resetBaseline > 250) { //the user blew too soon
		   			 setStatus2 (pleaseWaitText);
		   			 setBreathStatus2 (statusResettingText);
		   			 try {         	
		   				 	playWaitMP3();
		   		         } catch (Exception e) {
		   		             e.printStackTrace();
		   		         }
		   			resetTimer.cancel(); //cancel it just in case it was running
		   			resetTimer.start();
		   			enableUi(false);	
		   		    AlertDialog.Builder alert=new AlertDialog.Builder(this);
		   			//alert.setTitle("Info").setIcon(R.drawable.icon).setMessage("You blew too soon. Please blow after touching the button.").setNeutralButton("OK", null).show();
		   			alert.setTitle(blewTooSoonStringTitle).setIcon(R.drawable.icon).setMessage(blewTooSoonString).setNeutralButton(OKText, null).show();
		   		}
		   		else {
		   		
		   			
		   			flipper.setInAnimation(inFromRightAnimation());
		   	        flipper.setOutAnimation(outToLeftAnimation());
		   	        flipper.showNext(); 
		   	        onHomeScreen = false;
		   			setStatus2 (blowForText);
		   			setBreathStatus2 (statusInprogressText);
		   			enableUi(false); //we've started to grey out the start button
		   			//charButton_.setVisibility(View.INVISIBLE); 
		   		
		   			try {         	
		   	           playBlowMP3();
		   	         } catch (Exception e) {
		   	             e.printStackTrace();
		   	         }
		   		     
		   		     //now take the baseline
		   		     state = 1; //set this state
		   			 baselineReading = convertReadingInt(reading);
		   			 
		   			if (debug == true) {
						showToast("Alcohol Baseline Reading: " + baselineReading);
					}	
		   			 maxDifference = max_value - baselineReading; //now let's find out what the maximum difference can be between the baseline
					 readingIncrement = maxDifference / 10; //depending on how close the user is to the max corresponds to how much they've had to drink
		   			 //breathIndicator2.setMax(max_value - baselineReading); //this sets the max which is defined in the preferences
		   		     highReading = convertReadingInt(reading); //let's also establish the high reading
		   		     //start the timer
		   		     breathTimer.start();	
		   		     updateGaugeTimer.start();
		   		}
		      }	
		}
		else {
			 enableUi(false);
			 setDetected2 (statusResettingText);
			 setStatus2 (pleaseWaitText);
			 state = 0;
		}
		
	}
	
	 private void playBlowMP3() throws Exception {
					
		switch (character) {
			case 0:		//old english			
				 blowMP3 = getResources().openRawResourceFd(R.raw.blow0);
				break;					
			case 1:		//pirate
				 blowMP3 = getResources().openRawResourceFd(R.raw.blow1);
				break;		
			case 2:		//spooky halloween			
				 blowMP3 = getResources().openRawResourceFd(R.raw.blow2);
				break;		
			case 3:		//insult (new york cabbie)			
				 blowMP3 = getResources().openRawResourceFd(R.raw.blow3);
				break;		
			case 4:	//text to speech				
				 //blowMP3 = getResources().openRawResourceFd(R.raw.blow0);
				break;		
		}	
		
		 if (tts_on == true) {
	    	 say(blowPrompt);
	     }
	     else {
	    	 
	          if (blowMP3 != null) {
	
		            mediaPlayer = new MediaPlayer();
		            mediaPlayer.setDataSource(blowMP3.getFileDescriptor(), blowMP3.getStartOffset(), blowMP3.getLength());
		            blowMP3.close();
		            mediaPlayer.prepare();
		            mediaPlayer.start();
		        }
	 		} 
		 
		 if (simulation == true && breathalyzerFound == 0) {
				
				simCounter++;
				
				switch (simCounter) { //this way we'll cyle through all the responses when in sim mode
				case 1:		//drunk		
					highReading = 180;
					break;					
				case 2:		//buzzed
					highReading = 120;
					break;		
				case 3:		//few drinks			
					highReading = 50;
					break;		
				case 4:		//no drinks			
					highReading = 0;
					break;					
			}
				
				runOnUiThread(new Runnable() {
				     public void run() {

				    	 meter2.setValue(highReading);

				    }
				});
		}
	 }
	 
	 private void playBeepMP3() throws Exception {  //took this out because older phones could not play the beeps quick enough
		 
		 // SoundPool mySP = new SoundPool (10, AudioManager.STREAM_MUSIC,0);
	     // int BeepSP = mySP.load(this,R.raw.beep, 1);    
		 
		// BeepMP3 = getResources().openRawResourceFd(R.raw.beep);
		 
	     //   if (BeepMP3 != null) {

	      //      mediaPlayer = new MediaPlayer();
	      //      mediaPlayer.setDataSource(BeepMP3.getFileDescriptor(), BeepMP3.getStartOffset(), BeepMP3.getLength());
	      //      BeepMP3.close();
	      //      mediaPlayer.prepare();
	      //      mediaPlayer.start();
	      //  }
	        
	       // rate = 1/rate;
	       // mySP.play(BeepSP,1f,1f,1,1,rate);
	        
	    }
	 
	 private void playReadyBeepMP3() throws Exception {
  
		 ReadyBeepMP3 = getResources().openRawResourceFd(R.raw.readybeep);
		 
	        if (ReadyBeepMP3 != null) {

	            mediaPlayer = new MediaPlayer();
	            mediaPlayer.setDataSource(ReadyBeepMP3.getFileDescriptor(), ReadyBeepMP3.getStartOffset(), ReadyBeepMP3.getLength());
	            ReadyBeepMP3.close();
	            mediaPlayer.prepare();
	            mediaPlayer.start();
	        }
	    }
	 
	 private void playConnectionLostMP3() throws Exception {
 
		  connectionLostMP3 = getResources().openRawResourceFd(R.raw.beeplost); 
		 
		 if (connectionLostMP3 != null) {

	            mediaPlayer = new MediaPlayer();
	            mediaPlayer.setDataSource(connectionLostMP3.getFileDescriptor(), connectionLostMP3.getStartOffset(), connectionLostMP3.getLength());
	            connectionLostMP3.close();
	            mediaPlayer.prepare();
	            mediaPlayer.start();
	        }
	    }
	 
	 	 
	 private void playnoDrinksMP3() throws Exception {
		 
		 result = 0;
		 
		 BitmapInputStream = getResources().openRawResource(resIdNoDrinks);
	     loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 matrix_.frame(frame_);  //write to the matrix   
		 
		 switch (character) {
			case 0:		//old english			
				noDrinksMP3 = getResources().openRawResourceFd(R.raw.nothing0);
				break;					
			case 1:		//pirate
				noDrinksMP3 = getResources().openRawResourceFd(R.raw.nothing1);
				break;		
			case 2:		//spooky halloween			
				noDrinksMP3 = getResources().openRawResourceFd(R.raw.nothing2);
				break;		
			case 3:		//insult (new york cabbie)			
				noDrinksMP3 = getResources().openRawResourceFd(R.raw.nothing3);
				break;		
			case 4:	//text to speech				
				break;		
		 }	
		 
	     if (tts_on == true) {
	    	 say(tts_no_drinks);
	     }
	     else {
			 if (noDrinksMP3 != null) {
	
		            mediaPlayer = new MediaPlayer();
		            mediaPlayer.setDataSource(noDrinksMP3.getFileDescriptor(), noDrinksMP3.getStartOffset(), noDrinksMP3.getLength());
		            noDrinksMP3.close();
		            mediaPlayer.prepare();
		            mediaPlayer.start();
		        }
	     }
	     
	 }
	 
	 private void playfewDrinksMP3() throws Exception {
			 
		 result = 1;
		 
		 switch (character) {
			case 0:		//old english			
				fewDrinksMP3 = getResources().openRawResourceFd(R.raw.few0);
				break;					
			case 1:		//pirate
				fewDrinksMP3 = getResources().openRawResourceFd(R.raw.few1);
				break;		
			case 2:		//spooky halloween			
				fewDrinksMP3 = getResources().openRawResourceFd(R.raw.few2);
				break;		
			case 3:		//insult (new york cabbie)			
				fewDrinksMP3 = getResources().openRawResourceFd(R.raw.few3);
				break;		
			case 4:	//text to speech, change this later				
				break;		
		 }		
		 
		 BitmapInputStream = getResources().openRawResource(resIdFewDrinks);
	     loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 matrix_.frame(frame_);  //write to the matrix   
		 
	     if (tts_on == true) {
	    	 say(tts_few_drinks);
	     }
	     else {
		 
		 	if (fewDrinksMP3 != null) {

	            mediaPlayer = new MediaPlayer();
	            mediaPlayer.setDataSource(fewDrinksMP3.getFileDescriptor(), fewDrinksMP3.getStartOffset(), fewDrinksMP3.getLength());
	            fewDrinksMP3.close();
	            mediaPlayer.prepare();
	            mediaPlayer.start();
	        }
	     }
	  }
	 
	 private void playbuzzedMP3() throws Exception {
				 
		 result = 2;
		 
		 switch (character) {
			case 0:		//old english			
				buzzedMP3 = getResources().openRawResourceFd(R.raw.buzzed0);
				break;					
			case 1:		//pirate
				buzzedMP3 = getResources().openRawResourceFd(R.raw.buzzed1);
				break;		
			case 2:		//spooky halloween			
				buzzedMP3 = getResources().openRawResourceFd(R.raw.buzzed2);
				break;		
			case 3:		//insult (new york cabbie)			
				buzzedMP3 = getResources().openRawResourceFd(R.raw.buzzed3);
				break;		
			case 4:	//text to speech			
				break;		
		 }	
		 
		 BitmapInputStream = getResources().openRawResource(resIdBuzz);
	     loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 matrix_.frame(frame_);  //write to the matrix   
		 
		 if (tts_on == true) {
	    	 say(tts_buzzed);
	     }
		 else {
		 
			 if (buzzedMP3 != null) {
	
		            mediaPlayer = new MediaPlayer();
		            mediaPlayer.setDataSource(buzzedMP3.getFileDescriptor(), buzzedMP3.getStartOffset(), buzzedMP3.getLength());
		            buzzedMP3.close();
		            mediaPlayer.prepare();
		            mediaPlayer.start();
		        }
		 } 
	 }
	 
	 private void playdrunkMP3() throws Exception {
		 
		 result = 3;
		 
		 switch (character) {
			case 0:		//old english			
				drunkMP3 = getResources().openRawResourceFd(R.raw.drunk0);
				break;					
			case 1:		//pirate
				drunkMP3 = getResources().openRawResourceFd(R.raw.drunk1);
				break;		
			case 2:		//spooky halloween			
				drunkMP3 = getResources().openRawResourceFd(R.raw.drunk2);
				break;		
			case 3:		//insult (new york cabbie)			
				drunkMP3 = getResources().openRawResourceFd(R.raw.drunk3);
				break;		
			case 4:	//text to speech			
				drunkMP3 = getResources().openRawResourceFd(R.raw.drunk0);
				break;		
		 }	
		 
		 BitmapInputStream = getResources().openRawResource(resIdDrunk);
	     loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 matrix_.frame(frame_);  //write to the matrix   
		 
		 if (tts_on == true) {
	    	 say(tts_drunk);
	     }
		 else {   
		 
			 if (drunkMP3 != null) {
	
		            mediaPlayer = new MediaPlayer();
		            mediaPlayer.setDataSource(drunkMP3.getFileDescriptor(), drunkMP3.getStartOffset(), drunkMP3.getLength());
		            drunkMP3.close();
		            mediaPlayer.prepare();
		            mediaPlayer.start();
		        }
		 } 
	 }
	 
	 private void playWaitMP3() throws Exception {
		 
		 if (debug == true) {
				Toast.makeText(getBaseContext(), "Reset Timer from Please Wait", Toast.LENGTH_LONG).show();
		 }
		 //resetTimer.cancel(); //need to call as it may have already been running as we could now be re-starting
		 //resetTimer.start(); //
				 
		 switch (character) {
			case 0:		//old english			
				waitMP3 = getResources().openRawResourceFd(R.raw.wait0);
				break;					
			case 1:		//pirate
				waitMP3 = getResources().openRawResourceFd(R.raw.wait1);
				break;		
			case 2:		//spooky halloween			
				waitMP3 = getResources().openRawResourceFd(R.raw.wait2);
				break;		
			case 3:		//insult (new york cabbie)			
				waitMP3 = getResources().openRawResourceFd(R.raw.wait3);
				break;		
			case 4:					
				break;		
		 }	
		 
		 BitmapInputStream = getResources().openRawResource(resIdPlsWait);
	     loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 matrix_.frame(frame_);  //write to the matrix   
		 
		 if (tts_on == true) {
	    	 say("Please Wait");
	     }
	     else {   
		 
			 if (waitMP3 != null) {
	
		            mediaPlayer = new MediaPlayer();
		            mediaPlayer.setDataSource(waitMP3.getFileDescriptor(), waitMP3.getStartOffset(), waitMP3.getLength());
		            waitMP3.close();
		            mediaPlayer.prepare();
		            mediaPlayer.start();
		        }
		     }
	    }
		
	
	 public class AnalyzingCountDownTimer extends CountDownTimer
		{

			public AnalyzingCountDownTimer(long startTime, long interval)
				{
					super(startTime, interval);
				}

			@Override
			public void onFinish() //now give the reading
				{
					
					pDialog.dismiss();	//dismiss the please wait/analyzing dialog 
					
					 
					if (convertReadingInt(reading) > highReading) { //now take the last sample
					    	highReading = convertReadingInt(reading);                               
					}	
					
					 if (simulation == true && breathalyzerFound == 0) {
						
						//already set the simCounter++ in the blowmp3 routine above so not setting it again here
						
						switch (simCounter) { //this way we'll cyle through all the responses when in sim mode
						case 1:		//drunk		
							highReading = 850;
							simMeterReading = 180;
							break;					
						case 2:		//buzzed
							highReading = 600;
							simMeterReading = 120;
							break;		
						case 3:		//few drinks			
							highReading = 400;
							simMeterReading = 50;
							break;		
						case 4:		//no drinks			
							highReading = 0;
							simCounter = 0; //reset it
							simMeterReading = 0;
							break;	
						default: //should never make it here but just in case 
							highReading = 850;
							simCounter = 0; //reset it
							simMeterReading = 180;
							break;
					}
						 meter2.setValue(highReading);
				}
				     
				
				 maxDifference = max_value - baselineReading; //now let's find out what the maximum difference can be between the baseline
				 readingIncrement = maxDifference / 10; //depending on how close the user is to the max corresponds to how much they've had to drink
				 
				 if (highReading <= baselineReading) {
					 result = 0;
					 gaugeValue = 0;
					 
					 setLevelStatus (level1Result); 
					  try {         	
						  playnoDrinksMP3();
				         } catch (Exception e) {
				           e.printStackTrace();
				         }					
				 }
				 
				 if (highReading > baselineReading && highReading < (baselineReading + readingIncrement)) { //1
					 result = 1;
					 gaugeValue = 1;
					 setLevelStatus (level1Result); 
					
					  try {         	
						  playnoDrinksMP3();
				      } catch (Exception e) {
				           e.printStackTrace();
				         }			
				 }
				 
				 if (highReading > (baselineReading + readingIncrement) && highReading < (baselineReading + readingIncrement*2)) { //2
					 result = 1;
					 gaugeValue = 2;
					 setLevelStatus (level2Result); 
					 try {         	
						  playfewDrinksMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }		
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*2) && highReading < (baselineReading + readingIncrement*3)) { //3
					 result = 1;
					 gaugeValue = 3;
					 setLevelStatus (level2Result); 
					 try {         	
						  playfewDrinksMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }		
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*3) && highReading < (baselineReading + readingIncrement*4)) { //4
					 result = 1;
					 gaugeValue = 4;
					 setLevelStatus (level2Result); 
					 try {         	
						  playfewDrinksMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }		
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*4) && highReading < (baselineReading + readingIncrement*5)) { //5
					 result = 1;
					 gaugeValue = 5;
					 setLevelStatus (level2Result);
					 try {         	
						 playfewDrinksMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*5) && highReading < (baselineReading + readingIncrement*6)) { //6
					 result = 2;
					 gaugeValue = 6;
					 setLevelStatus (level3Result); 
					 try {         	
						 playbuzzedMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*6) && highReading < (baselineReading + readingIncrement*7)) { //7
					 result = 2;
					 gaugeValue = 7;
					 setLevelStatus (level3Result); 
					 try {         	
						 playbuzzedMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*7) && highReading < (baselineReading + readingIncrement*8)) { //8
					 result = 2;
					 gaugeValue = 8;
					 setLevelStatus (level3Result); 
					 try {         	
						 playbuzzedMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*8) && highReading < (baselineReading + readingIncrement*9)) { //9
					 result = 3;
					 gaugeValue = 9;
					 setLevelStatus (level4Result);
					 try {         	
						 playdrunkMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				 if (highReading > (baselineReading + readingIncrement*9) && convertReadingInt(reading) != 0) { //9
					 result = 3;
					 gaugeValue = 10;
					 setLevelStatus (level4Result);
					 try {         	
						 playdrunkMP3();
				     } catch (Exception e) {
				           e.printStackTrace();
				       }	
				 }
				 
				//now let's store the results in the preferences so other intents can use it
				resultPrefs = getSharedPreferences("resultsData",MODE_PRIVATE); 
				Editor rEditor = resultPrefs.edit();
				rEditor.putString("pref_level", Integer.toString(gaugeValue));
				rEditor.putString("pref_result", Integer.toString(result));
				rEditor.commit();
				//*****************************************************************************	
				 
				 gaugeValue = gaugeValue * 20;	//need to do this because of the scale limitations of the gauge, it needs a max of 200
					 
					runOnUiThread(new Runnable() {
					     public void run() {
					    	 meter2.setValue(gaugeValue);

					    }
					});
					
					if (debug == true) {
						 //Toast.makeText(getBaseContext(), "Alcohol Final Reading: " + highReading, Toast.LENGTH_LONG).show();		
						showToast("Alcohol Final Reading: " + highReading);
					}				
					
				 
				 //we're done so let's reset everything   
				 highReading = min_baseline; //reset it
				// state = 0;				
				 clearTextTimer.start(); //give a delay so the Breathalyzer reading can occur first			
				
				}

			@Override
			public void onTick(long millisUntilFinished)				{
					
						//do nothing here
				}
			}
	 
	 
	 // CountDownTimer class
	public class BreathCountDownTimer extends CountDownTimer
		{

			public BreathCountDownTimer(long startTime, long interval)
				{
					super(startTime, interval);
				}

			@Override
			public void onFinish()
				{
					
				//here we need another 2 seconds or so, maybe a please wait / analyzing box before taking the last sample 
				
				if (convertReadingInt(reading) > highReading) { //take the last sample
				    	highReading = convertReadingInt(reading);                               
                 }	
				     
				setStatus2(""); //clear the blox text
				pDialog = ProgressDialog.show(breath.this,analyzingText, justAmomentText, true);	
				//pDialog = ProgressDialog.show(breath.this,"Analyzing", "Just a moment...", true);	
				 //now let's start a timer for 2-3 seconds and then get the results, had to add this due to the alcohol sensor delay
				analyzingTimer.start();				
				
				}

			@Override
			public void onTick(long millisUntilFinished)				{
					
				//String both = name + "-" + dest;
				String blowTextCountdown = blowForText + "  " + String.valueOf(millisUntilFinished / 1000);
				setStatus2 (blowTextCountdown);
				//countdownTimer_.setText(String.valueOf(millisUntilFinished / 1000));
				   
				    if (convertReadingInt(reading) > highReading) { //here we will take an alcohol sample once a second and then take the highest for the final reading
				    	highReading = convertReadingInt(reading);                               
                    }	  
				 
				    //if ((int)(millisUntilFinished / 1000) < (int)((startTime / 1000) - 2)) { //don't play the beep until two seconds after blow MP3 has played
				    
						  //  try {         	
						    	//  playBeepMP3();
						    	//  } catch (Exception e) {
						        //   e.printStackTrace();
						       //  }
				   // }
				}
			}
	
	// CountDownTimer class
		public class UpdateGaugeTimer extends CountDownTimer
			{
			public UpdateGaugeTimer(long startTime, long interval)					
			
					{
						super(startTime, interval);
					}

				@Override
				public void onFinish()
					{
						
							//do nothing here
							if (KioskMode_) {
								updateGaugeTimer.start();
							}
					
					}

				@Override
				public void onTick(long millisUntilFinished)				{						
					
						
					debugCounter++;
					
					gaugeReading = convertReadingInt(reading); 
					BitmapInputStream = getResources().openRawResource(resIdIndicator1); //just in case no match below
						
						 
						 if (gaugeReading <= baselineReading) {
 							 gaugeValue = 0;	
 							 meterMatch = 1;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator1);
 							 
 							
 						 }
 						 
 						 if (gaugeReading > baselineReading && gaugeReading < (baselineReading + readingIncrement)) { //1
 							 gaugeValue = 1;
 							 meterMatch = 2;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator2);
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement) && gaugeReading < (baselineReading + readingIncrement*2)) { //2
 							 gaugeValue = 2;
 							 meterMatch = 3;
							 BitmapInputStream = getResources().openRawResource(resIdIndicator3);
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*2) && gaugeReading < (baselineReading + readingIncrement*3)) { //3
 							 gaugeValue = 3;
 							 meterMatch = 4;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator4);
 						     							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*3) && gaugeReading < (baselineReading + readingIncrement*4)) { //4
 							 gaugeValue = 4;
 							 meterMatch = 5;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator5);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*4) && gaugeReading < (baselineReading + readingIncrement*5)) { //5
 							 gaugeValue = 5;
 							 meterMatch = 6;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator6);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*5) && gaugeReading < (baselineReading + readingIncrement*6)) { //6
 							 gaugeValue = 6;
 							 meterMatch = 7;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator7);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*6) && gaugeReading < (baselineReading + readingIncrement*7)) { //7
 							 gaugeValue = 7;
 							 meterMatch = 8;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator8);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*7) && gaugeReading < (baselineReading + readingIncrement*8)) { //8
 							 gaugeValue = 8;
 							 meterMatch = 9;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator9);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*8) && gaugeReading < (baselineReading + readingIncrement*9)) { //9
 							 gaugeValue = 9;
 							 meterMatch = 10;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator10);
 							
 						 }
 						 
 						 if (gaugeReading > (baselineReading + readingIncrement*9) && convertReadingInt(reading) != 0) { 
 							 gaugeValue = 10;
 							 meterMatch = 11;
 							 BitmapInputStream = getResources().openRawResource(resIdIndicator11);
 							
						 }
 						 
 						//if the alcohol sensor returned a value that didn't match so let's just use the last reading for PIXEL here
 						 if (meterMatch == 0) {  
 							 
 							
 							 switch (meterMatch) {
 							
	  						 	case 1:					
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator1);
	  						 		break;					
	  						 	case 2:		
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator2);
	  						 		break;		
	  						 	case 3:				
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator3);
	  						 		break;		
	  						 	case 4:				
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator4);
	  						 		break;		
	  						 	case 5:					
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator5);
	  						 		break;	
	  						 	case 6:					
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator6);
	  						 		break;					
	  						 	case 7:		
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator7);
	  						 		break;		
	  						 	case 8:				
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator8);
	  						 		break;		
	  						 	case 9:				
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator9);
	  						 		break;		
	  						 	case 10:					
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator10);
	  						 		break;	
	  						 	case 11:					
	  						 		BitmapInputStream = getResources().openRawResource(resIdIndicator11);
	  						 		break;	
  						   }
 							 
 						 }
 						 
 						  loadRGB565(); //this function loads a raw RGB565 image to the matrix
						 	 try {
								matrix_.frame(frame_);
							} catch (ConnectionLostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}  //write to the matrix   
						 	//meterMatch = 0; //reset this one, TO DO should we get rid of this?
						
 						//Log.i("gauge:"+debugCounter, "GaugeReading " + String.valueOf(gaugeReading));
 						//Log.i("gauge:"+debugCounter, "GaugeValue " +String.valueOf(gaugeValue));
 						//Log.i("gauge:"+debugCounter, "baselineReading " +String.valueOf(baselineReading));
 						 
 						gaugeValue = gaugeValue * 20;	//need to do this because of the scale limitations of the gauge, it needs a max of 200
 						 
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 meter2.setValue(gaugeValue); //set the gauge on the android phone
						    }
						});
					    
					}
				}
	
	public class ResetTimer extends CountDownTimer
	{

		public ResetTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{	
					setStatus (tapTobeginText);
					setBreathStatus(statusReadyText);
					try {   //play a ready beep      	
				     		playReadyBeepMP3();
					         } catch (Exception e) {
					           e.printStackTrace();
					}
					enableUi(true);
					resetCounter2 = 0;
				    resetCounter = 0;
				    resetBaseline = convertReadingInt(reading);
				    
				    if (shake_start == true) {
				    	 mSensorManager.registerListener(mSensorListener,
							        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							        SensorManager.SENSOR_DELAY_UI);
					}
				    resetTimer.start(); //restart it
				}
			

		@Override
		public void onTick(long millisUntilFinished)				{
			 resetCounter++;
			 if (debug == true) {
				 debug_.setText(resetCounter + " " + min_baseline + " " + resetCounter2);
				 //debug_.setText(resetCounter + " " + convertReadingInt(reading) + " " + min_baseline + " " + resetCounter2);
			 }
				 if (resetCounter > 10 && (convertReadingInt(reading) < min_baseline)) { //wait at least 10 seconds and also don't go here unless we've reached the minimum baseline
					 if (Math.abs(lastValue - convertReadingInt(reading)) < 5) { //here we will take an alcohol sample once a second and then take the highest for the final reading
						resetCounter2++;
						if (resetCounter2 > 3) {			
							
							setStatus (tapTobeginText); 	
					     	setBreathStatus(statusReadyText);
					   
					     	BitmapInputStream = getResources().openRawResource(resIdReady);
					     	loadRGB565(); //this function loads a raw RGB565 image to the matrix
					     	try {
					     		matrix_.frame(frame_);
					     	} catch (ConnectionLostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
					     	}  //write to the matrix   
					     
					     	
					     	 try {   //play a ready beep      	
					     		playReadyBeepMP3();
						         } catch (Exception e) {
						           e.printStackTrace();
						         }
					     	
							enableUi(true);
							//resetCounter3 = 1; //need to do this since you cannot cancel the timer within onTick, Android limitation
							resetCounter2 = 0;
						    resetCounter = 0;
						    resetBaseline = convertReadingInt(reading);
						    
						    //************* re-enable the shake listener ********
						    if (shake_start == true) {						    
							    mSensorManager.registerListener(mSensorListener,
								        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
								        SensorManager.SENSOR_DELAY_UI);
							}
						
						    //*********************************************************************
											    
							resetTimer.cancel(); //doesn't work from within onTick so had to put it in the loop above, that sucks
						}
					              
			         }	   
				}			   
				lastValue = convertReadingInt(reading);
				}
			 
		}
	
	
	
	
	
	
	
	public class ConnectTimer extends CountDownTimer
	{

		public ConnectTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				if (breathalyzerFound == 0) {
					showNotFound (); 					
				}
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
	
	public class ClearTextTimer extends CountDownTimer
	{

		public ClearTextTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish() //we've resetted so put things back here
			{
								
			//Now reset things for the next reading 			 
			//make a phone call here on buzzed or drunk if user pref says so
			
			//if (result == 1 || result == 2 || result == 3) { //bring the pop up if tipsy or drunk
				//if (call_friend == true || find_friend == true || call_taxi == true) {	//need to make sure one of these is on or we shouldn't show the popup			
					//Vibrator myVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					//myVib.vibrate(100);
					//actionButtons();
				//}
			//}			
		
			//made a change so this popup comes up all the time regardless of result				
			
			
		//	if (disable_actions == false) {   //causes a crash, add this back later
		//		Vibrator myVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		//		myVib.vibrate(100);
		//		actionButtons();
		//	}
				
			
			mainStatus_.setTextColor(getResources().getColor(R.color.black)); //set the color of the text
			
			flipper.setInAnimation(inFromLeftAnimation());
	        flipper.setOutAnimation(outToRightAnimation());
	        flipper.showPrevious();    
	        onHomeScreen = true;
							
	        if (simulation == true && breathalyzerFound == 0) {
				 setStatus (tapTobeginText); 
				 setLevelStatus("");
				 enableUi(true);
				 state = 0;
				 resetBaseline = 250;
				 
			   //************* re-enable the shake event listener ********
			    if (shake_start == true) {				 
					 mSensorManager.registerListener(mSensorListener,
					        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					        SensorManager.SENSOR_DELAY_UI);	
			    }
			    //*********************************************************************
			    
			}
			else {
				resetTimer.start(); //here we need to start the reset timer
				setStatus (pleaseWaitText); 
				setBreathStatus(statusResettingText);
				setLevelStatus("");
				state = 0;		//now set the state back to the visual progress bar shows back to warming up mode
				//breathIndicator.setMax(max_value - warmupOffset); //set the scale back to the way it was
				 BitmapInputStream = getResources().openRawResource(resIdPlsWait);
			     loadRGB565(); //this function loads a raw RGB565 image to the matrix
			 	 try {
					matrix_.frame(frame_);
				} catch (ConnectionLostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}  //write to the matrix   
								 
			}
		}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
		
	@Override
    public void onDestroy() {
		
		if (breathTimer != null) breathTimer.cancel(); 
 		if (updateGaugeTimer != null) updateGaugeTimer.cancel();
 		if (connectTimer != null) connectTimer.cancel(); 
 		if (analyzingTimer != null) analyzingTimer.cancel();
 		if (resetTimer != null) resetTimer.cancel(); 
 		if (clearTextTimer != null) clearTextTimer.cancel();
		
		/*connectTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
    	breathTimer.cancel(); 
        analyzingTimer.cancel();
        resetTimer.cancel(); 
     //   batteryTimer.cancel(); 
        clearTextTimer.cancel(); */
    	
    	
    	if (tts != null) {
    		tts.stop();
    		tts.shutdown();
    	}  
    	
    	super.onDestroy();
    }

	
	//public void onPause(int status) {
		
		//if (debug == true) {
			//Toast.makeText(getBaseContext(), "On Pause", Toast.LENGTH_LONG).show();
		//}
		
	//	resetTimer.cancel(); //need to call as it may have already been running as we could now be re-starting
		//resetTimer.start(); //
		
	//}
	
	@Override
	public void onInit(int status) { //this is needed by the TexttoSpeech stuff

	}

	
	@Override
	protected void onResume() {
		
		Log.i(TAG, "On Resume .....");
		
		//if (debug == true) {
		//	showToastShort("On Resume");
		//}		
		super.onResume();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updatePrefs();
	}

	
	@Override
	protected void onStart() {
	
		
		Log.i(TAG, "On Start .....");
		//if (debug == true) {
		//	showToastShort("On Start");
		//}
		
		//resetTimer.cancel(); //need to call as it may have already been running as we could now be re-starting
		//resetTimer.start(); //
		super.onStart();

	}
	
	@Override
	protected void onRestart() { //we got here right before returning to the main screen (from another activity that came up)
	    //this needs to be here because there is about a 2 second delay from when the activity is returned
		
		if (simulation == false) {
			setStatus (pleaseWaitText);
			setBreathStatus(statusResettingText);	
			enableUi(false); //we've started to grey out the start button
			Log.i(TAG, "On Start .....");
		}
		super.onRestart();

	}

	
	
public void showToast(final String msg) {
    mHandler.post(new Runnable() {
        @Override
        public void run() {
            Toast toast = Toast.makeText(breath.this, msg, Toast.LENGTH_LONG);
            toast.show();
        }
    });
}

public void showToastShort(final String msg) {
    mHandler.post(new Runnable() {
        @Override
        public void run() {
            Toast toast = Toast.makeText(breath.this, msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    });
}


	
	
} //end package
