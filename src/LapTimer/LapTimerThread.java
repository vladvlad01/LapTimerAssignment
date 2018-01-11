/* Vlad Ciobanu 		 */	
/* C15716369   			 */
/* DT354 – 2			 */
/* OOSD 3    	         */

package LapTimer;
public class LapTimerThread implements Runnable{

	private boolean running;	
	private float lapSeconds, totalSeconds;
	private LapTimer parent;	
	private float increment = (float)0.1;

	/* The constructor takes the parent frame and the total
	   seconds that the application has been running for so
	   far as parameters, and then launches a thread. */
	public LapTimerThread(LapTimer parent, float totalSeconds){
		this.parent = parent;		
		this.running = true;		
		this.lapSeconds = 0.0f;		
		this.totalSeconds = totalSeconds;
		(new Thread(this)).start();	//creating a new asynchronous thread which will run in parallel with the main thread, so the GUI doesn't become unresponsive	
	}

	@Override
	/* This method should keep incrementing the two counters - 
	   lapSeconds and totalSeconds - by the increment each
	   tenth of a second. It should then update the corresponding
	   text fields in the main display. */
	
	// This method bellow makes the timer running
	public void run(){

		while (this.running){//while running is true
			this.lapSeconds+=increment; // increments seconds by 0.1
			this.totalSeconds+=increment; // increments total seconds by 0.1
			this.parent.updateLapDisplay(this.lapSeconds); //update lap display by lapSeconds
			this.parent.updateTotalDisplay(this.totalSeconds); //update total display by totalSeconds
			niceSleep(100); //thread will sleep for 100 ms
		}
	}
	
	/* This method stops the thread. */
	public void stop(){
		this.running = false;
	}
	// Getters
	public float getLapSeconds(){
		return lapSeconds;		
	}
	
	public float getTotalSeconds(){		
		return totalSeconds;		
	}
	
	//This method will only suspend the thread for a specified amount of milliseconds and will not throw an exception, thus halting the thread
	public void niceSleep(int ms){
		try{
			Thread.sleep(ms); // calling this method will suspend the thread form execution for a specific time in milliseconds 
	
		} 
		catch (InterruptedException e){
			;//nothing to do
		}
	}
}
