package ca.uwaterloo.Lab4_202_24;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.Lab4_202_24.MapLoader;
import ca.uwaterloo.Lab4_202_24.NavigationalMap;
import ca.uwaterloo.Lab4_202_24.MapView;
import ca.uwaterloo.Lab4_202_24.PositionListener;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.widget.Button;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements PositionListener {

	MapView mapView;
	PointF start= new PointF(0f,0f);    // Starting point
	PointF userloc= new PointF(0f,0f);  // User's current location
	PointF end= new PointF(0f,0f);		// Destination
	
	double stepCounter;		// Regular step counter
	float stepCounterN;		// Step counter for north/south
	float stepCounterE;		// Step count for east/west
	float sinval;			// Sine of the direction angle 
	float cosval;			// Cosine of the direction angle
	String label= "";		// Empty string for label
	
	List<PointF> userpath= new ArrayList<PointF>();  // The user's path

	PointF checkPoint= new PointF(0f,0f);	// Used to update user location when valid
	
	PointF point1= new PointF(3.5f,9f);		// Waypoints
	PointF point2= new PointF(7.4f,9f);
	PointF point3= new PointF(11.5f,9f);
	PointF point4= new PointF(16f,9f);
	
	double lowerbound;						
	double upperbound;
	
	PointF intermediate1 = new PointF(0f,0f);	// Waypoint closest to origin
	PointF intermediate2 = new PointF(0f,0f);	// Waypoint closest to destination

	float coordinate_x;		// Coordinates for navigation
	float coordinate_y; 

	PointF oldintermediate;
	
	boolean isManual= false; // Used for manual (button) navigation
	
	float[][] obstacles= {{3f, 1.9f, 11.65f, 10.6f},
			{5.1f, 4.75f, 11.65f, 10.6f}, 
			{11.65f, 9.9f, 11.65f, 10.6f},
			{15.8f, 14f, 11.65f, 10.75f},
			{6.3f, 4.5f, 8.2f, 1.9f},
			{10.4f, 8.5f, 8.3f, 1.9f},
			{14.5f, 12.6f, 8.3f, 1.9f},
			{16.9f, 1.9f, 11.6f, 1.9f}};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView tv = (TextView) findViewById(R.id.label1);
	    tv.setText("ECE155\nLAB4\nTharaga Balachandran\nKai Ran Hong\n\n");
	    
        // A 600 x 600 map with x and y axes of size 30
        mapView = new MapView ( getApplicationContext (), 700 , 700 , 40, 40);
     
        mapView.addListener(this);

        // Returns the intial and destination points once set
        start= mapView.getOriginPoint();
        end= mapView.getDestinationPoint();
        
		//Creates variable "lin" to Refer to Linear Layout
		LinearLayout lin = (LinearLayout) findViewById(R.id.parentid);
		lin.setOrientation(LinearLayout.VERTICAL); //sets orientation to vertical

		registerForContextMenu ( mapView );
		
		NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null),
				"Lab-room-peninsula.svg");
				mapView.setMap(map);
				
	    Button reset = (Button) findViewById(R.id.reset_button);
		reset.setText("Reset Button");
		
		// Buttons for navigation
		Button up= (Button) findViewById(R.id.up_button);
        up.setText("Up");
        
        Button down= (Button) findViewById(R.id.down_button);
        down.setText("Down");
        
        Button left= (Button) findViewById(R.id.left_button);
        left.setText("Left");
        
        Button right= (Button) findViewById(R.id.right_button);
        right.setText("Right");
       
	    TextView acc = new TextView(getApplicationContext()); 		
		TextView mag = new TextView(getApplicationContext()); 
		TextView orientation = new TextView(getApplicationContext());
		TextView linacc = new TextView(getApplicationContext());
		TextView destination= new TextView(getApplicationContext());
	
	 	SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

	 	// Linear acceleration for step counter:
	 	Sensor aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    SensorEventListener a = new AccSensorEventListener(linacc, mapView, map, destination);
	 				sensorManager.registerListener(a, aSensor,
	 				SensorManager.SENSOR_DELAY_FASTEST);
	 		
	 	// Magnetic field for geomagnetic values
	 	Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	 		
	 	// Accelerometer for gravity values
	 	Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    
		SensorEventListener o = new Orientation(orientation, acc, mag);
			
		sensorManager.registerListener(o, accSensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(o, mSensor,
					SensorManager.SENSOR_DELAY_NORMAL);      

		mapView.addLabeledPoint(point1, label);  // Adds waypoints to map
		mapView.addLabeledPoint(point2, label);
		mapView.addLabeledPoint(point4, label);
		mapView.addLabeledPoint(point3, label);
		
		mapView.setVisibility(View.VISIBLE);
		lin.addView(destination);
		lin.addView(mapView);
		lin.addView(linacc);
		lin.addView(orientation);
	}
        

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onCreateContextMenu ( ContextMenu menu , View v, ContextMenuInfo menuInfo ) {
	super . onCreateContextMenu (menu , v, menuInfo );
	mapView. onCreateContextMenu (menu , v, menuInfo );
	}
	
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.up_button:
				moveButton(0,-0.5f);
			break;
			case R.id.down_button:
				moveButton(0,0.5f);
			case R.id.left_button:
				moveButton(-0.5f,0);
			break;
			case R.id.right_button:
				moveButton(0.5f,0);
			break;
		}
	}
	
	// Reset button: clears the path and sets all points back to origin
	public void resetPath(View view) {
		isManual= false;

		start.x=0;
		start.y=0;
		userloc.x=0;
		userloc.y=0;
		end.x=0;
		end.y=0;
		intermediate1.x=0;
		intermediate1.y=0;
		intermediate2.x=0;
		intermediate2.y=0;
		
		userpath.clear();
    	mapView.setUserPath(userpath);

    	stepCounter=0;
    	stepCounterN=0;
    	stepCounterE=0;
	}
	
	public void moveButton(float xMove, float yMove){
		isManual= true; // isManual must be true to override automatic navigation

		checkPoint.x= userloc.x+ xMove;
		checkPoint.y=userloc.y+ yMove;
		
		boolean isFree;
		
		isFree=isFree(checkPoint);
		
		if (isFree== true)
			{
				mapView.setUserPoint(checkPoint.x, checkPoint.y);
				userloc= mapView.getUserPoint();
			}
	}
	
	
	@Override
	public boolean onContextItemSelected ( MenuItem item ) {
	return super.onContextItemSelected ( item ) || mapView . onContextItemSelected ( item );
	}

	// When the user selects the start point, the userpoint is updated and the first part of the path is calculated
	@Override
	public void originChanged(MapView source, PointF loc) {
		
    	mapView.setUserPoint(loc);
    	userpath.add(userloc);
    	userpath.add(intermediate1);
    	mapView.setUserPath(userpath);
	}

	// When the user selects the destination, the last portion of the user path is calculated
	@Override
	public void destinationChanged(MapView source, PointF dest) {
		
		float ddistance1=dis(end,point1);
		float ddistance2=dis(end,point2);
		float ddistance3=dis(end,point3);
		float ddistance4=dis(end,point4);
		
		float enddistance= Math.min(ddistance1, Math.min(ddistance2, Math.min(ddistance3, ddistance4)));
		
		if (enddistance== ddistance1)
		{
			intermediate2=new PointF(point1.x, point1.y);	
		}
		else if (enddistance== ddistance2)
		{
			intermediate2=new PointF(point2.x, point2.y);	
		}
		else if (enddistance== ddistance3)
		{
			intermediate2=new PointF(point3.x, point3.y);	
		}
		else if (enddistance== ddistance4)
		{
			intermediate2=new PointF(point4.x, point4.y);	
		}
		
		userpath.add(intermediate2);

		userpath.add(end);
	
		upperbound=intermediate2.x+1;
		lowerbound=intermediate2.x-1;

	
		if (userloc.x< (intermediate2.x+1) && userloc.x>(intermediate2.x-1))
		{
			userpath.remove(intermediate2);
		}
		
		mapView.setUserPath(userpath);
		}
	
	
	// Method for calculating distance between two points
	public static float dis(PointF start, PointF end)
	{
		return (float)java.lang.Math.sqrt((float) (Math.pow(end.x - start.x, 2) +  Math.pow(end.y - start.y, 2)));
	}
		
	// Restricted area: bottom left corner
	public boolean avoidObstacles(PointF checkPoint, float x_upper, float x_lower, float y_upper, float y_lower)
	{
		boolean isFree;		 
	 
		if (checkPoint.x>x_lower && checkPoint.x<x_upper
				&& checkPoint.y>y_lower && checkPoint.y<y_upper)
		{
			isFree=false;
		}
		
		else
		{
			isFree=true;
		}
		
		return isFree;
	}
	
	public boolean isFree(PointF checkPoint)
	{

		for (int i=0; i<7; i++)
		{
			if (!(avoidObstacles(checkPoint, obstacles[i][0], obstacles[i][1], obstacles[i][2], obstacles[i][3])))
			{
				return false;
			}
		}
		
		return !(avoidObstacles(checkPoint, obstacles[7][0], obstacles[7][1], obstacles[7][2], obstacles[7][3]));
	}
	
	class Orientation implements SensorEventListener {
		TextView orOutput;
		TextView accOutput;
		TextView magOutput;
		float[] gravity;
		float[] geomag;
		float[] rotate;
		float[] incline;
		float[] orientation;
		float smoothAzimuth;
		float[] smoothedAccel;
	
		
		public Orientation (TextView orOutputView, TextView accOutputView, TextView magOutputView) {
			orOutput = orOutputView;
			accOutput= accOutputView;
			magOutput= magOutputView;
			gravity = new float[3];
			geomag = new float[3];
			rotate= new float[16];
			incline= new float[16];
			orientation= new float[3];
			smoothedAccel= new float[3];
			stepCounterN=0;
			stepCounterE=0;	
		}
	
    	public void onAccuracyChanged(Sensor s, int i) {
		}

    	public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {			
				geomag[0] = event.values[0];
				geomag[1] = event.values[1];
				geomag[2] = event.values[2];
			}
			
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				
				// Smoothes the accelerometer values
				for (int i = 0; i <= 2; i++) {
					smoothedAccel[i] += (event.values[i] - smoothedAccel[i]) / 100;
				}
				
				gravity[0] = smoothedAccel[0];
				gravity[1] = smoothedAccel[1];
				gravity[2] = smoothedAccel[2];
			}
		
			if(gravity!= null && geomag!=null){
				SensorManager.getRotationMatrix(rotate, incline, gravity, geomag);
				SensorManager.getOrientation(rotate, orientation);
			}	
			
			// Calculates cosine and sine values of orientation values for use in step counter
			cosval=(float) Math.cos(orientation[0]);	
			sinval=(float) Math.sin(orientation[0]);
			
			if (orientation!= null) {
				orOutput.setText("Orientation values:\n" 
			    + String.format("(%.2f,%.2f,%.2f)",
				orientation[0],
				orientation[1],
				orientation[2]
				));
			}
		}
	}
	
	class AccSensorEventListener implements SensorEventListener {
		
		//Declaring Variables
		TextView accoutput;
		double[] temp;
		double[] max;  
		float[] smoothedAccel;	 
		float [] v; 			// Stores value of previous and current reading
		float tempmax;
		float finalmax;
		double bound;
		
		int statevalue;			// Stores value of current state
		
		NavigationalMap map;
		boolean isFree;
		TextView destination;
		float olddistance=0;
		float odistance1;	
		float odistance2;
		float odistance3;
		float odistance4;
		
				
		public AccSensorEventListener(TextView accoutputView, MapView mv, NavigationalMap m, TextView dest) {
			accoutput = accoutputView;
			mapView= mv;
			map= m;
			destination= dest;
			
			//Set dimension for arrays
			temp = new double[3];
			max = new double[3];
			smoothedAccel= new float[3];
			v= new float[2];
			
			//Set the initial values of all fields of temp as 0
			temp[0] = 0;
			temp[1] = 0;
			temp[2] = 0;
					
			v[0]=0; // previous reading value
			v[1]=0; //current reading value
					
			stepCounter= 0;
					
			tempmax=0;
			finalmax=10;
			bound=9;
					
			userloc= mapView.getUserPoint();
			
			isFree= false;

			olddistance=0;
			
		}

		public void onAccuracyChanged(Sensor s, int i) {
		}
				
				
		public int stateMachine(int state, float[] value, double bound)
		{
		
		  switch (state)
		  {
		  case 0:
			if ((v[1]>bound)& (v[0]<bound))
			{
				state=1;
			}
			break;
			
		  default:
			break;
		  }
			
		  return state;
		}
		
		public void onSensorChanged(SensorEvent acce) {
					
			if (acce.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {				
				//Passes the absolute value of the x,y,z readings
				//into the temp array
				for (int i = 0; i <= 2; i++) {
					temp[i] = Math.abs(acce.values[i]);
					}
						
				//Passes any temp value greater than the previous max value
				//into the corresponding field of the max array
				for (int i = 0; i <= 2; i++) {
					if (temp[i] > max[i]) {
						max[i] = temp[i];
						}
					}
							
				v[0]=v[1]; //previous reading value of round x = current reading value of round x-1
				v[1]=smoothedAccel[1]; //current reading value of round x= sensor reading
							
							
				//Counting steps
				//anytime the acceleration value transitions from 
				//lower than the bound to higher than the bound
				//we count one step
				
				statevalue=stateMachine(statevalue,v,bound);
				
				
				if (statevalue==1) {	
					
					if (isManual==false){	// If manual navigation is off
					stepCounter++;			// Count a step
					stepCounterN=stepCounterN+cosval;
					stepCounterE=stepCounterE+sinval;
					
					checkPoint= new PointF(userloc.x+(sinval),userloc.y-(cosval)); // Store the user's coordinates
					statevalue=0;
				}
					
				isFree=isFree(checkPoint);
					
				if (isFree== true) 	// If the checkpoint is in the valid range
					{
						mapView.setUserPoint(checkPoint.x, checkPoint.y);	//Update the user's location on the map
						userloc= mapView.getUserPoint();				
					}
				}
				
				//setting temporary maximum when current value is greater than previous value
				if (v[1]>v[0])
				{
					tempmax=v[1];
				}
						
				// find the final max when the acceleration function is decreasing
				//obtains amplitude 
				if(v[1]<v[0])
				{
					finalmax=tempmax;
				}
						
				// if the value of finalmax is within the fluctuation range, 
				//set the value for the bound as 1/2 of the finalmax value
				if(finalmax>0.1)
					//&(finalmax<0.3)
				{
					bound=0.5*finalmax; 
				}
				
				if(intermediate1!=null)
				{
					oldintermediate=intermediate1;
				}
				
				// Find the point closest to the user's location
				odistance1=dis(userloc,point1);
				odistance2=dis(userloc,point2);
				odistance3=dis(userloc,point3);
				odistance4=dis(userloc,point4);
				
				float distance= Math.min(odistance1, Math.min(odistance2, Math.min(odistance3, odistance4)));
				
				if (distance== odistance1)
				{
					intermediate1=new PointF(point1.x, point1.y);
				}
				else if (distance == odistance2 )
				{
					intermediate1=new PointF(point2.x, point2.y);
				}
				else if (distance == odistance3)
				{
					intermediate1=new PointF(point3.x, point3.y);
				}
				else if (distance== odistance4)
				{
					intermediate1=new PointF(point4.x, point4.y);
				}
				
				if (olddistance!=distance)
			    {	
					userpath.clear();
					
			    	userpath.add(userloc); 
			    	userpath.add(intermediate1);
			    	userpath.add(intermediate2);
			    	userpath.add(end);
			    	
			    	mapView.setUserPath(userpath);
			    	
			    }
				
				olddistance=distance;	
				
				accoutput.setText(
						
					"\nNavigation:\n"	
					+ "\nSteps north:" 
					+ String.format("%.2f", (-1.5)*coordinate_x)
					+"\nSteps east:" 
					+ String.format("%.2f", (1.5)*coordinate_y)
					);
				
				// If the user point is not at the origin (initial location)
				if (userloc.x!=0 && userloc.y!= 0 && end.x!=0 && end.y!=0)
				{
					// If it is within a + or - 0.4 cuurdinate range of the destination
					if (userloc.x<(end.x+0.4) && userloc.x>(end.x-0.4) && userloc.y>(end.y-0.4) && userloc.y<(end.y+0.4))
					{
						destination.setText("You have reached your destination");
				
						userpath.clear();
						mapView.setUserPath(userpath);
					}
				}
				
				else
				{
					destination.setText("Keep walking");
				}
				
				// If the user is in the "hallway" (close to horizontal line of waypoints)
				// remove the line from the userpoint to the first waypoint
				// and connect the userpoint to the destination's waypoint
				if ((userloc.y>=9 && userloc.y<=10)&&(!(userloc.x<upperbound && userloc.x>lowerbound )))
				{
					coordinate_x=intermediate2.x-userloc.x;
    				coordinate_y=intermediate2.y-userloc.y;
    				
                    userpath.clear();
					
			    	userpath.add(userloc); 
			    	
			    	userpath.add(intermediate2);
			    	userpath.add(end);
			    	
			    	mapView.setUserPath(userpath);
				}
				
				// If there are no intersections between the userpoint and the destination
				// connect the userpoint directly to the endpoint
				else if (userloc.x<upperbound && userloc.x>lowerbound )
				{
			    	coordinate_x=end.x-userloc.x;
					coordinate_y=end.y-userloc.y;
					
					userpath.clear();
					
			    	userpath.add(userloc); 
			   	
			    	userpath.add(end);
			    	
			    	mapView.setUserPath(userpath);
				
				}
				
				// If there are intersections between the userpoint and destination
				// and the user is not in the "hallway" of waypoints
				// include user location, first and second waypoint, and destination in the path
				else
				{		    	
		    	coordinate_x=intermediate1.x-userloc.x;
 				coordinate_y=intermediate1.y-userloc.y;
 				userpath.clear();
				
		    	userpath.add(userloc); 
		    	userpath.add(intermediate1);
		    	userpath.add(intermediate2);
		    
		    	userpath.add(end);
		    	
		    	mapView.setUserPath(userpath);
				}

			}

			for (int i = 0; i <= 2; i++) {
				smoothedAccel[i] += (acce.values[i] - smoothedAccel[i]) / 100;
			}	
		}
		
	}
	 
}

