package org.mozilla.fennec_tfairey;
import java.lang.Boolean;
import com.jayway.android.robotium.solo.Solo;	
import org.mozilla.roboexample.test.R;
import android.test.ActivityInstrumentationTestCase2;
import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.app.Activity;
import android.database.Cursor;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.view.Display;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import java.lang.Class;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import android.os.SystemClock;


@SuppressWarnings("unused")
public class ExampleTest extends ActivityInstrumentationTestCase2{
	
	public ExampleTest(Class activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}
	private	static final String	TARGET_PACKAGE_ID =	"org.mozilla.gecko";	
	private static final String LAUNCH_ACTIVITY_FULL_CLASSNAME="org.mozilla.fennec_tfairey.App";
	//private static final String LAUNCH_ACTIVITY_FULL_CLASSNAME="org.mozilla.gecko.AwesomeBar";
	
	private static Class<?> launcherActivityClass;
	
	static{
		  try{
			  launcherActivityClass = Class.forName(LAUNCH_ACTIVITY_FULL_CLASSNAME);	
		  } catch (ClassNotFoundException e){
			  throw new RuntimeException(e);	
		  }	
		}	

	@SuppressWarnings("unchecked")	
	public ExampleTest() throws ClassNotFoundException {
		super(TARGET_PACKAGE_ID, launcherActivityClass);	
	}	

	private Solo solo;
	private Activity activity;
	private Activity awesome;
	private Object assertValue;
	private Instrumentation inst;
	public boolean asleep = false;

	@Override 
	protected void setUp() throws Exception
	{ 
		  //setActivityInitialTouchMode(true);
		  activity = getActivity();

		  inst = getInstrumentation();
		  solo = new Solo(inst, activity);	
	} 	
	
	public void testExampleTest() {
		try {
			
			waitForFennecContent();
			//Thread.sleep(5500);
			//Click on Awesomebar
			selectAwesomeBar();
			waitForAwesomeBar();
			//Thread.sleep(5500);
			//Switch to using AwesomeBar activity
			selectURLBar();
			//Thread.sleep(5500);
			//Input Webpage
			inputText("www.");
			String[] firstAll = getAwesomeBarTabs();
			inputText("mozilla.com");
			String[] secondAll = getAwesomeBarTabs();
			String[] thirdAll = getAwesomeBarTabs();
			//Assert URL, and that the AwesomeBarTabs changed, and then didn't
			assertURL("www.mozilla.com");
			assertEquals(areSArraysEqual(firstAll, secondAll), false);
			assertEquals(areSArraysEqual(secondAll, thirdAll), true);
			//Assert that content isn't showing yet
			assertEquals(contentShown(), false);
			//Press Enter
			sendKeys(KeyEvent.KEYCODE_ENTER);
			waitForFennecContent();
			//Assert Content is Showing.
			assertEquals(contentShown(), true);
			Thread.sleep(20000);
			for(int i = 0; i < 10; i++) {
				solo.drag(600, 600, 400, 100, 1);
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	class wakeInvocationHandler implements InvocationHandler {
		public wakeInvocationHandler(){};
		
		public Object invoke(Object proxy, Method method, Object[] args) {
			Log.i("Testing", "Wake up!");
			asleep = false;
			return null;
		}
	}
	
	public void waitForEvent(String geckoEvent) {
		try {
		ClassLoader cs = activity.getClassLoader();
		Class gel = cs.loadClass("org.mozilla.gecko.GeckoEventListener");
		Class gas = cs.loadClass("org.mozilla.gecko.GeckoAppShell");
		Class [] parameters = new Class[2];
		parameters[0] = String.class;
		parameters[1] = gel;
		Class [] interfaces = new Class[1];
		interfaces[0] = gel;
		Method registerGEL = gas.getMethod("registerGeckoEventListener", parameters);
		Method unregisterGEL = gas.getMethod("unregisterGeckoEventListener", parameters);
		
		Log.i("Testing", registerGEL.toString());
		Object[] finalParams = new Object[2];
		//finalParams[0] = "DOMContentLoaded";
		finalParams[0] = geckoEvent;
		finalParams[1] = Proxy.newProxyInstance(cs, interfaces, new wakeInvocationHandler());
		registerGEL.invoke(null, finalParams);
		asleep=true;
		while(asleep);
		unregisterGEL.invoke(null, finalParams);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void waitForFennecContent() {
		waitForEvent("DOMContentLoaded");
	}
	
	
	public void waitForAwesomeBar() {
		awesome = solo.getCurrentActivity();
		while(!awesome.toString().contains("AwesomeBar")) {
			awesome = solo.getCurrentActivity();
		}
	}
	
	public boolean contentShown() {
		awesome = solo.getCurrentActivity();
		//RelativeLayout rl = (RelativeLayout)awesome.findViewById(0x7f090015);
		RelativeLayout rl = (RelativeLayout)solo.getView(0x7f090011);
		if(rl == null)
		{
			return false;
		}
	    return true;
	}
	
	public boolean areSArraysEqual(String [] a, String [] b) {
		boolean eq = true;
		if(a.length != b.length) {
			return false;
		}
		for(int i = 0; i < a.length; i++) {
			if(!a[i].equals(b[i])) {
				return false;
			}
		}
		return true;
	}
	
	public String [] getAwesomeBarTabs() {
		awesome = solo.getCurrentActivity();
	    awesome.runOnUiThread(
			      new Runnable() {
			        public void run() {
			          String[] result;
			          ListView lv = (ListView)awesome.findViewById(0x7f090006);
			          int length = lv.getCount();
			          result = new String[length];
			          Cursor cursor;
			          for(int i = 0; i < length; i++) {
			        	  cursor = (Cursor)lv.getItemAtPosition(i); 
			              result[i] = cursor.getString(cursor.getColumnIndexOrThrow("url"));
			          }
			          
			          assertValue = result;
			        } // end of run() method definition
			      } // end of anonymous Runnable object instantiation
	    	    );
		
	    try { Thread.sleep(500); } 
	    catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (String[])assertValue;
	}
	
	
	public void assertURL(String s)
	{
		awesome = solo.getCurrentActivity();
	    awesome.runOnUiThread(
			      new Runnable() {
			        public void run() {
			          EditText et = (EditText)awesome.findViewById(0x7f090004);
			          assertValue = et.getText().toString();
			        } // end of run() method definition
			      } // end of anonymous Runnable object instantiation
	    	    );
	    try { Thread.sleep(5000); } 
	    catch (InterruptedException e) {
			e.printStackTrace();
		}
	    assertEquals(s,(String)assertValue);
	}

	public void inputText(String input)
	{
		for(int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if( c >= 'a' && c <='z') {
				sendKeys(29+(int)(c-'a'));
				continue;
			}
			else if( c >= 'A' && c <='Z') {
				//sendKeys(KeyEvent.KEYCODE_CAPS_LOCK);
				sendKeys(29+(int)(c-'a'));
				//sendKeys(KeyEvent.KEYCODE_CAPS_LOCK);
				continue;
			}
			else if( c >= '0' && c <='9') {
				sendKeys(29+(int)(c-'0'));
				continue;
			}
			switch (c) {
			case '.':
				sendKeys(KeyEvent.KEYCODE_PERIOD);
				break;
			case '/':
				sendKeys(KeyEvent.KEYCODE_SLASH);
				break;
			case '\\':
				sendKeys(KeyEvent.KEYCODE_BACKSLASH);
			    break;
			case '-':
				sendKeys(KeyEvent.KEYCODE_MINUS);
			    break;
			case '+':
				sendKeys(KeyEvent.KEYCODE_PLUS);
			    break;
			case ',':
				sendKeys(KeyEvent.KEYCODE_COMMA);
			    break;
			default:
			}
		}
	}

	public void selectAwesomeBar()
	{
		activity.runOnUiThread(
				new Runnable() {
					public void run() {
						Button awesomebar = (Button)activity.findViewById(0x7f09000a);
						awesomebar.performClick();
					}
				});
	}
	public void selectURLBar()
	{
		awesome = solo.getCurrentActivity();
	    awesome.runOnUiThread(
			      new Runnable() {
			        public void run() {
			          EditText et = (EditText)awesome.findViewById(0x7f090004);
			          et.requestFocus();
			        } // end of run() method definition
			      } // end of anonymous Runnable object instantiation
	    	    );
	}
	
	@Override	
	public void tearDown() throws Exception {	
		try	{	
			solo.finalize();	
		}catch (Throwable e){	
			e.printStackTrace();	
		}	
		getActivity().finish();	
		super.tearDown();	
	}		
}