/* Vlad Ciobanu 		 */	
/* C15716369   			 */
/* DT354 – 2			 */
/* OOSD 3    	         */

package LapTimer;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class LapTimer extends JFrame{

	private Font counterFont = new Font("Arial", Font.BOLD, 20);
	private Font totalFont = new Font("Arial", Font.PLAIN, 14);

	private JLabel lapLabel = new JLabel("Seconds running:");
	private JTextField lapField = new JTextField(15);
	private JLabel totalLabel = new JLabel("Total seconds:");
	private JTextField totalField = new JTextField(15);


	private JButton startButton = new JButton("START");
	private JButton lapButton = new JButton("LAP");
	private JButton stopButton = new JButton("STOP");
	private JButton resetButton = new JButton("RESET");

	// The text area and the scroll pane in which it resides
	private JTextArea display;

	private JScrollPane myPane;

	// These represent the menus
	private JMenuItem saveData = new JMenuItem("Save data", KeyEvent.VK_S);
	private JMenuItem displayData = new JMenuItem("Display data", KeyEvent.VK_D);
	
	private JMenu options = new JMenu("Options");

	private JMenuBar menuBar = new JMenuBar();

	private boolean started;

	private float totalSeconds = (float)0.0;
	//private float lapSeconds = (float)0.0;

	private int lapCounter = 1;

	private LapTimerThread lapThread;

	private Session currentSession;
	
	private final JLabel lblLapTimeGoal = new JLabel("Lap time goal:");
	private final JTextField goalTextField = new JTextField();
	private final JPanel SetTimePanel = new JPanel();
	private final JLabel lblSeconds = new JLabel("seconds");
	
	private String[] goal_message = {"GOAL REACHED", "GOAL NOT REACHED"};
	
	public static LapTimer thisInstance;
	
	private float goalNumber = 0.0f;
	

	public LapTimer(){
		thisInstance = this; //create a static reference to this instance which can be used statically by other classes without passing this instance as a parameter
		
		setTitle("Lap Timer Application");
		MigLayout layout = new MigLayout("fillx");
		JPanel panel = new JPanel(layout);
		getContentPane().add(panel);

		options.add(saveData);
		options.add(displayData);
		menuBar.add(options);

		panel.add(menuBar, "spanx, north, wrap");

		MigLayout centralLayout = new MigLayout("fillx", "[]", "[][][]");

		JPanel centralPanel = new JPanel(centralLayout);
		
		GridLayout timeLayout = new GridLayout(0,2);

		JPanel timePanel = new JPanel(timeLayout);

		lapField.setEditable(false);
		lapField.setFont(counterFont);
		lapField.setText("00:00:00.0");

		totalField.setEditable(false);
		totalField.setFont(totalFont);
		totalField.setText("00:00:00.0");
		timePanel.add(lblLapTimeGoal);
		
		timePanel.add(SetTimePanel);
		goalTextField.setText("60");
		goalTextField.setColumns(10);
		SetTimePanel.add(goalTextField);
		SetTimePanel.add(lblSeconds);

		// Setting the alignments of the components
		lblLapTimeGoal.setHorizontalAlignment(SwingConstants.RIGHT);
		goalTextField.setHorizontalAlignment(SwingConstants.CENTER);
		totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lapLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lapField.setHorizontalAlignment(JTextField.CENTER);
		totalField.setHorizontalAlignment(JTextField.CENTER);

		timePanel.add(lapLabel);
		timePanel.add(lapField);
		timePanel.add(totalLabel);
		timePanel.add(totalField);

		centralPanel.add(timePanel, "cell 0 0");

		GridLayout buttonLayout = new GridLayout(1, 3);

		JPanel buttonPanel = new JPanel(buttonLayout);

		buttonPanel.add(startButton);
		buttonPanel.add(lapButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(resetButton);

		centralPanel.add(buttonPanel, "cell 0 1,growx");

		panel.add(centralPanel, "wrap");

		display = new JTextArea(100,150);
		display.setMargin(new Insets(5,5,5,5));
		display.setEditable(false);
		myPane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(myPane, "alignybottom, h 100:320, wrap");


		// Initial state of system
		started = false;
		currentSession = new Session();

		// Allowing interface to be displayed
		setSize(400, 520);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		/* This method should allow the user to specify a file name to which 
		   to save the contents of the text area using a  JFileChooser. You 
		   should check to see that the file does not already exist in the 
		   system. */
		
		
		saveData.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				if (!started){//if there is no running session
					File f = browseFile("Choose destination file","Any file");//this will show a file selection window and will return the destination file or null if the cancel button was pressed
					if (f!=null){//if the user hasn't pressed the cancel button, but chose a file instead
						try{//catching possible errors (disk full, IO errors)
							writeDataFile(f);//save the log to the file
							JOptionPane.showMessageDialog(thisInstance, "Save successfull.", "Information Message", JOptionPane.INFORMATION_MESSAGE);
						}catch (Exception e1){
							JOptionPane.showMessageDialog(thisInstance, "There was an error writing to the file.", "Error Message", JOptionPane.ERROR_MESSAGE);
						}
					}
					else
						JOptionPane.showMessageDialog(thisInstance, "File not chosen.", "Information Message", JOptionPane.INFORMATION_MESSAGE);
				}
				else				
					JOptionPane.showMessageDialog(thisInstance, "The timer is still going. Please stop it in order save data.", "Error Message", JOptionPane.ERROR_MESSAGE);
			}
		});

		/* This method should retrieve the contents of a file representing a 
		   previous report using a JFileChooser. The result should be displayed 
		   as the contents of a dialog object. */
		displayData.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){

				if (!started){//if there is no running session
					File f = browseFile("Choose source file","Any file");//this will show a file selection window and will return the destination file or null if the cancel button was pressed
					if (f!=null){//if the user hasn't pressed the cancel button, but chose a file instead
						try {//catching possible errors (disk full, IO errors)
							String read = readDataFile(f);//read the data from the file
							JOptionPane.showMessageDialog(thisInstance, "Read successfull.", "Information Message", JOptionPane.INFORMATION_MESSAGE);
							setTextArea(read);//if we reached this point, there was no error reading from the file and we can safely set the text area with the read text
						}catch (Exception e1){ 						
							JOptionPane.showMessageDialog(thisInstance, "There was an error reading from the file. It may be corrupt.", "Error Message", JOptionPane.ERROR_MESSAGE);
						}
					}
					else
						JOptionPane.showMessageDialog(thisInstance, "File not chosen.", "Information Message", JOptionPane.INFORMATION_MESSAGE);
						}
				else				
					JOptionPane.showMessageDialog(thisInstance, "The timer is still going. Please stop it in order read data.", "Error Message", JOptionPane.ERROR_MESSAGE);
				}
		});

		/* This method should check to see if the application is already running, 
		   and if not, launch a LapTimerThread object, but if there is another 
		   session already under way, it should ask the user whether they want to 
		   restart - if they do then the existing thread and session should be 
		   reset. The lap counter should be set to 1 and a new Session object 
		   should be created. A new LapTimerThread object should be created with 
		   totalSeconds set to 0.0 and the display area should be cleared. When the 
		   new thread is started, make sure the goal textField is disabled */

		
		
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				started=true;//marking that there is a running timer and save/load data should be disabled
						
					
				try{//catching possible exceptions thrown by parsing a string to a float
					goalNumber = Float.parseFloat(getGoalValue());	//interpreting the goal value as a number; If the value is not a number an exception will be thrown and the code will continue execution at catch
				
					if (goalNumber <= 0){//if the entry is less or equal to 0 all code bellow will run 
						JOptionPane.showMessageDialog(null, "Wrong entry: Goal can not be set to 0", "Error Message", JOptionPane.ERROR_MESSAGE); //error message 
						goalTextField.setText("60"); //set goal text field to 60
						return; //return to top
						
					}
					startButton.setEnabled(false); //enabling and disabling buttons that should/should not be pressed while the timer is running
					lapButton.setEnabled(true);
					stopButton.setEnabled(true);
					resetButton.setEnabled(true);
					currentSession.clearSession(); //clearing the previus session (if any) 
					EnableGoalEditing(false); //the user cannot change the goal while the timer is running
					updateLapDisplay(0.0f);//set the time displays to 0
					updateTotalDisplay(0.0f);
					thisInstance.lapCounter = 1;//counting this lap as being the first one
					thisInstance.totalSeconds = 0;//since we just started this session, total seconds are 0
					setTextArea("");//empty the text area
					lapThread = new LapTimerThread(thisInstance,thisInstance.totalSeconds);	//initializing an asyncrhonous thread that acts like a timer	
				}
				catch(Exception e2){//if there was an error (most likely at parsing)
					JOptionPane.showMessageDialog(null, "Wrong entry: Unsupported goal format.", "Error Message", JOptionPane.ERROR_MESSAGE);
					startButton.setEnabled(true);//resetting the buttons so the user can re-attempt starting a new session
					lapButton.setEnabled(false);
					stopButton.setEnabled(false);
					resetButton.setEnabled(false);	
					e2.printStackTrace();//this helps us see where the exception was thrown
				}					
			}
		});

		lapButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				lapThread.stop();//stop the previous lap
				float lapSeconds = lapThread.getLapSeconds();
				float totalSeconds = lapThread.getTotalSeconds(); //getting data from the previous lap
				Lap p = new Lap(lapCounter,lapSeconds); //already initializing a new one
				currentSession.addLap(p);//adding the newly created lap to the session
				thisInstance.totalSeconds = totalSeconds; //keeping track of the total seconds 
				
				String tmpTxt = display.getText(); 																						//
				tmpTxt+="Lap:"+lapCounter+":\t"+convertToHMSString(lapSeconds)+"\t"+goal_message[(lapSeconds<=goalNumber)?0:1]+"\n";    // updating the text area
				setTextArea(tmpTxt);																									//
						
				lapCounter++;//incrementing the lap counter by 1
				lapThread = new LapTimerThread(thisInstance,thisInstance.totalSeconds); //starting the new lap timer
			}
		});
		lapButton.setEnabled(false); //making sure the user does not click the lap button without starting a session first
		/* This method should have most of the same functionality as the Lap
		   button's action listener, except that a new LapTimerThread object is
		   NOT started. In addition, the total time for all the laps should be 
		   calculated and displayed in the text area, along with the average lap
		   time and the numbers and times of the fastest and slowest laps. */
		stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				started=false;//enable saving/loading
				startButton.setEnabled(true); //resetting the buttons so the user can start a new session
				lapButton.setEnabled(false);
				stopButton.setEnabled(false);
				resetButton.setEnabled(true);
				
				EnableGoalEditing(true); 
				lapThread.stop();
				float lapSeconds = lapThread.getLapSeconds();
				float totalSeconds = lapThread.getTotalSeconds();
				Lap p = new Lap(lapCounter,lapSeconds);
				currentSession.addLap(p);
				thisInstance.totalSeconds = totalSeconds;
				
				String tmpTxt = display.getText();
				tmpTxt+="Lap:"+lapCounter+":\t"+convertToHMSString(lapSeconds)+"\t"+goal_message[(lapSeconds<=goalNumber)?0:1]+"\n";
				tmpTxt+="\n";
				tmpTxt+="Stopped at: "+convertToHMSString(thisInstance.totalSeconds)+"\n";
				tmpTxt+="Average lap time: "+convertToHMSString(currentSession.calculateAverageTime())+"\n";
				tmpTxt+="\n";
				tmpTxt+="Fastest Lap:"+currentSession.getFastestLap().getId()+"\t\t"+convertToHMSString(currentSession.getFastestLap().getLapTime())+"\n";
				tmpTxt+="\n";
				tmpTxt+="Slowest Lap:"+currentSession.getSlowestLap().getId()+"\t\t"+convertToHMSString(currentSession.getSlowestLap().getLapTime())+"\n";
				setTextArea(tmpTxt);								
			}
		});
		stopButton.setEnabled(false);//making sure the user does not click the stop button without starting a session first
		/* This method will reset all stats to default */
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				started=false;
				startButton.setEnabled(true);
				lapButton.setEnabled(false);
				stopButton.setEnabled(false);
				resetButton.setEnabled(false);				
				currentSession.clearSession();
				EnableGoalEditing(true);
				updateLapDisplay(0.0f);
				updateTotalDisplay(0.0f);
				thisInstance.lapCounter = 1;
				thisInstance.totalSeconds = 0;
				setTextArea("");
				goalTextField.setText("60");
				lapThread.stop();				
			}
		});
		resetButton.setEnabled(false);		//making sure the user does not click the reset button without starting a session first
	}	
	/* These two methods are used by the LapTimerThread to update the values
	   displayed in the two text fields. Each value is formatted as a 
	   hh:mm:ss.S string by calling the convertToHMSString method below/. */

	public void updateLapDisplay(float value){

		lapField.setText(convertToHMSString(value));
	}

	public void updateTotalDisplay(float value){

		totalField.setText(convertToHMSString(value));
	}
	
	/* These methods are here to help access the
	 *  goaltextField in the GUI */
	
	public String getGoalValue(){
		return  goalTextField.getText();
	}
	
	public void EnableGoalEditing(boolean makeEditable){
		goalTextField.setEditable(makeEditable);
	}
	
	public void setTextArea(String str){
		display.setText(str);
	}

	private String convertToHMSString(float seconds) {
		long msecs, secs, mins, hrs;
		// String to be displayed
		String returnString = "";

		// Split time into its components

		long secondsAsLong = (long)(seconds * 10);

		msecs = secondsAsLong % 10;
		secs = (secondsAsLong / 10) % 60;
		mins = ((secondsAsLong / 10) / 60) % 60;
		hrs = ((secondsAsLong / 10) / 60) / 60;

		// Insert 0 to ensure each component has two digits
		if (hrs < 10)
			returnString = returnString + "0" + hrs;
		
		else returnString = returnString + hrs;
		returnString = returnString + ":";

		if (mins < 10)
			returnString = returnString + "0" + mins;		
		
		else returnString = returnString + mins;
		returnString = returnString + ":";

		if (secs < 10)
			returnString = returnString + "0" + secs;		
		else
			returnString = returnString + secs;

		returnString = returnString + "." + msecs;

		return returnString;
	}

	/* These methods will be used by the action listeners attached
	   to the two menu items. */

	public synchronized void writeDataFile(File f) throws IOException, FileNotFoundException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			out.writeObject(display.getText());
		} 
		catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "File Not Found Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "I/O Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		finally{
			out.close();
		}
	}

	public synchronized String readDataFile(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream in = null;
		String result = new String();
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			result = (String)in.readObject();
		}
		catch (ClassCastException ex) {
			JOptionPane.showMessageDialog(this, "Class Cast Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "I/O Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			in.close();
		}   
		return result;
	}
	//This method will allow the user to open a file browsing window and select a file of choice
	public static File browseFile(String title, final String filetype){
		
    	    JFileChooser chooser = new JFileChooser(); //we are using the JFileChooser class
    	    chooser.setCurrentDirectory(new java.io.File("."));//setting the directory that will be first shown as the directory the application is running from
    	    chooser.setDialogTitle(title);//setting the title because it looks fancy
    	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);//setting it so that we can only choose files (not directories as well)
    	    chooser.setFileFilter(new FileFilter(){ //create a filter so we can only choose specific files

                @Override
                public boolean accept(File f){
                   if (f.isFile())//if the selected item is a file, it will be displayed. We can also do other checks here if we wanted to filter the files by extension, size, etc.
                	   return true;                   
                   else            //otherwise, it won't.
                	   return false;                 
                }

                public String getDescription(){ //this is where we set the wanted file type(which can be "All Files", etc) to our custom description               
                    return filetype;
                }
            });
    	    chooser.setAcceptAllFileFilterUsed(false); //if this was set to true, our filter would have been ignored
    	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) // if the user has clicked on "select"
    	    	return chooser.getSelectedFile();//then we return the selected file
    	    else 
    	    	return null;//else we return null    	  
    }
	public static void main(String[] args){
		LapTimer timer = new LapTimer();
		timer.setVisible(true);
	}
}