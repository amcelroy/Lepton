package com.tahi.Logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Log implements AutoCloseable{

    static Log INSTANCE = null;

    private String Filename;

    private boolean LogCreated = false;

    static public Log get()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new Log();
            INSTANCE.LogCreated = false;
            try {
				INSTANCE.createLogFile();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            INSTANCE.LogCreated = true;
        }
        return INSTANCE;
    }
    
    public synchronized void LogEvent(String msg)
    {
        try
        {
        	SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        	fmt.setCalendar(new GregorianCalendar());
            String event = fmt.format(new Date()) + " - " + msg + "\r\n";
            
            //Write to log
            FileWriter fw = new FileWriter("/tahi_logs/" + Filename, true);
            fw.write(event);
            fw.flush();
            fw.close();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }

    public void createLogFile() throws FileNotFoundException, IOException
    {
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        String filename = fmt.format(new Date()) + ".log";

        File f = new File("/tahi_logs/" + filename);
        if(f.createNewFile()){
        	Filename = filename;
        	LogCreated = true;
        }else{
        	System.out.println("Coudln't create file");
        	throw new FileNotFoundException();
        }        	
    }

	@Override
	public void close() throws Exception {
		
	}
}
