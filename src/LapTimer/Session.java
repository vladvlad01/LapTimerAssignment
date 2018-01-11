/* Vlad Ciobanu 		 */	
/* C15716369   			 */
/* DT354 – 2			 */
/* OOSD 3    	         */

package LapTimer;
import java.util.ArrayList;

public class Session{

	ArrayList<Lap> laps = new ArrayList<Lap>();
	
	public Session(){
		laps = new ArrayList<Lap>();
	}
	
	public void addLap(Lap l){
		synchronized(laps){ // will assure that there are no concurrent threads accessing the arraylist at the same time
			laps.add(l);//add a lap into the session
		}
	}
	
	public void clearSession(){
		synchronized(laps){ // will assure that there are no concurrent threads accessing the arraylist at the same time
			laps.clear();//clear the session
		}
	}
	
	public float calculateAverageTime(){
		
		/* This method should calculate the
		   average time of all laps in the 
		   collection. It needs to return a 
		   float value */
		
		
		float avgTime = 0.0f; 
		synchronized(laps){ // will assure that there are no concurrent threads accessing the arraylist at the same time
			for (Lap l:laps) // for each lap added into the session
				avgTime+=l.getLapTime();//update the average time with this lap's time

			return avgTime/(float)laps.size(); //calculate the average time using arithmetic average
		}
	}
	
	
	public Lap getFastestLap(){
		
		/* This method should step through the
		   collection, and return the Lap object
		   whose lap time is smallest (fastest). */
		
		float tmpTime = Float.MAX_VALUE; //initialize maximum lap time with the largest value possible for a float
		Lap fastastLap = null; //this will keep track of the fastest lap, initialize it with null
		synchronized(laps){ // will assure that there are no concurrent threads accessing the arraylist at the same time
			for (Lap l:laps){// for each lap added into the session
				if (l.getLapTime()<=tmpTime){//if the time for the current lap is lower than the maximum lap time
					tmpTime = l.getLapTime();//replace the maximum lap time with this lap's time
					fastastLap = l; //also keep the fastest lap associated with the lap's time
				}
			}
			return fastastLap;//return the fastest lap found (will never return null because there will always be a lap with a lower time than the largest value possbile for a float
		}
	}
	
	public Lap getSlowestLap(){
		
		/* This method should step through the
		   collection, and return the Lap object
		   whose lap time is largest (slowest). */
		
		float tmpTime = 0.0f; //initialize maximum lap time with the minimum value possible for a lap
		Lap slowestLap = null;//this will keep track of the slowest lap, initialize it with null
		synchronized(laps){ // will assure that there are no concurrent threads accessing the arraylist at the same time
			for (Lap l:laps){// for each lap added into the session
				if (l.getLapTime()>=tmpTime){//if the time for the current lap is higher than the minimum lap time
					tmpTime = l.getLapTime();//replace the maximum lap time with this lap's time
					slowestLap = l;//also keep the fastest lap associated with the lap's time
				}
			}
			return slowestLap;//return the slowest lap found (will never return null because there will always be a lap with a higher or equal time than 0
		}		
	}
}
