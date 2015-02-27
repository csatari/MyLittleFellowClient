package hu.jex.mylittlefellow.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
/**
 * Naplózást kezelõ osztály
 * @author Albert
 *
 */
public class Logger {
	/**
	 * Írható-e a mappa
	 * @return
	 */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	/**
	 * Sikerült elkészíteni a naplófájlt
	 * @return
	 */
	public static File isCreated() {
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				"Logs");
	    if (!file.mkdirs()) {
	    }
	    //file.setWritable(true);
	    File a = new File(file.getAbsolutePath()+File.separator+"logs.txt");
	    return a;
	}
	/**
	 * Egy kivételt tud kinaplózni
	 * @param e A kivétel
	 */
	public static void writeException(Exception e) {
		/*StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		writeToLog(getFileName()+": "+getLineNumber()+" - "+sw.toString());
		*/
	}
	/**
	 * Lekéri a sor számát
	 * @return A sor száma
	 */
	public static int getLineNumber() {
	    return Thread.currentThread().getStackTrace()[4].getLineNumber();
	}
	/**
	 * Lekéri a fájl nevét
	 * @return A fájl neve
	 */
	public static String getFileName() {
	    return Thread.currentThread().getStackTrace()[4].getFileName();
	}
	
	/**
	 * Kiír egy sort a naplóba
	 * @param data A sor
	 */
	public static void writeToLog(String data) {
		/*Log.d("hu.jex.mylittlefellow",getFileName()+": "+getLineNumber()+" - "+data);
		File file = isCreated();
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		data = today.format("%Y. %m. %d. %H:%M:%S")+" "+ getFileName()+": "+getLineNumber()+" - "+data + "\r\n";
		try {
            FileWriter out = new FileWriter(file,true);
            out.write(data);
            out.close();
        } catch (IOException e) {
        	Log.d("hu.jex.mylittlefellow", "failed to create directory"+e);
        }*/
	}
	/**
	 * Kiír egy sort a naplóba, de nem kezd új sort késõbb
	 * @param data A sor
	 */
	public static void writeToLogNoNewline(String data) {
		Log.d("hu.jex.mylittlefellow",data);
		File file = isCreated();
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		//data = today.format("%Y. %m. %d. %H:%M:%S")+" "+ data;
		try {
            FileWriter out = new FileWriter(file,true);
            out.write(data);
            out.close();
        } catch (IOException e) {
        	Log.d("hu.jex.mylittlefellow", "failed to create directory"+e);
        }
	}
	
}
