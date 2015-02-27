package hu.jex.mylittlefellow.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
/**
 * Napl�z�st kezel� oszt�ly
 * @author Albert
 *
 */
public class Logger {
	/**
	 * �rhat�-e a mappa
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
	 * Siker�lt elk�sz�teni a napl�f�jlt
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
	 * Egy kiv�telt tud kinapl�zni
	 * @param e A kiv�tel
	 */
	public static void writeException(Exception e) {
		/*StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		writeToLog(getFileName()+": "+getLineNumber()+" - "+sw.toString());
		*/
	}
	/**
	 * Lek�ri a sor sz�m�t
	 * @return A sor sz�ma
	 */
	public static int getLineNumber() {
	    return Thread.currentThread().getStackTrace()[4].getLineNumber();
	}
	/**
	 * Lek�ri a f�jl nev�t
	 * @return A f�jl neve
	 */
	public static String getFileName() {
	    return Thread.currentThread().getStackTrace()[4].getFileName();
	}
	
	/**
	 * Ki�r egy sort a napl�ba
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
	 * Ki�r egy sort a napl�ba, de nem kezd �j sort k�s�bb
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
