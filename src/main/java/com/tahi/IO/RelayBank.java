package com.tahi.IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.HashMap;

import com.tahi.Algorithms.PID;
import com.tahi.Lepton.Server.Lepton;
import com.tahi.Lepton.Server.LeptonROI;
import com.tahi.Lepton.Server.LeptonStatusListener;
import com.tahi.Logging.Log;
import com.tahi.Logging.Xml;
import java.text.DecimalFormat;

public class RelayBank implements LeptonStatusListener, Runnable{
	private List<Relay> _Relays;

    public final static int TicksPerSecond = 40;

    int _RelayBankSpi;

    boolean _SPIInit = false;

    //private final int Bits = 288;
    private final int Ports = 24;
    
    GpioController gpioController;
    GpioPinDigitalOutput XLAT = null;
    GpioPinDigitalOutput BLANK = null;
    GpioPinDigitalOutput DATA = null;
    GpioPinDigitalOutput CLK = null;

    boolean _SPIInitialized = false;

    boolean _RelaysOn = false;
    
    boolean _ActualRelayState = false;
    
    boolean Free = true;
    
    Semaphore PWMSemaphore;
    
    int counter = 0;
    
    DecimalFormat _DecimalFormatter = new DecimalFormat();
    
    public RelayBank(List<Relay> relays, GpioController gpicont)
    {
        _DecimalFormatter.setMaximumFractionDigits(2);
        
    	gpioController = gpicont;
    	
    	if(XLAT == null)
    		XLAT = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
        
    	XLAT.setShutdownOptions(true, PinState.LOW);
        XLAT.setPullResistance(PinPullResistance.OFF);
        XLAT.high();
        XLAT.low();
        
        
        if(BLANK == null)
        	BLANK = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_27, PinState.LOW);
        
    	BLANK.setShutdownOptions(true, PinState.LOW); 
        BLANK.setPullResistance(PinPullResistance.OFF);
        BLANK.high();
        BLANK.low();
        
        if(DATA == null)
        	DATA = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_28, PinState.LOW);
        
        DATA.setShutdownOptions(true, PinState.LOW);
        DATA.setPullResistance(PinPullResistance.OFF);
        
        if(CLK == null)
        	CLK = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_29, PinState.LOW);
        
        CLK.setShutdownOptions(true, PinState.LOW);
        CLK.setPullResistance(PinPullResistance.OFF);
        disable();

        initSPI();
        _Relays = relays;
        
        PWMSemaphore = new Semaphore(1);
    }
    
    public void shutdown(){
    	try{
	    	if(CLK != null)
	    		gpioController.unprovisionPin(CLK);
	    	
	    	if(DATA != null)
	    		gpioController.unprovisionPin(DATA);
	    	
	    	if(BLANK != null)
	    		gpioController.unprovisionPin(BLANK);
	    	
	    	if(XLAT != null)
	    		gpioController.unprovisionPin(XLAT);
    	}catch(Exception e){
            Log.get().LogEvent("Error shutting down RelayBank");
    	}finally{

    	}
    }
    
    public Relay findRelayByID(int id){
    	for(Relay r : _Relays){
    		if(r.ID == id){
    			return r;
    		}
    	}
    	
    	return null;
    }

    private void initSPI()
    {
    	//_RelayBankSpi = Spi.wiringPiSPISetupMode(Spi.CHANNEL_1, 1000000, Spi.MODE_3);
    	_SPIInitialized = true;
    	disable();
    }

    public void enable()
    {
        BLANK.low();
        _RelaysOn = true;
        _ActualRelayState = true;
        Log.get().LogEvent("[RELAYBANK] - Relays on");
    }

    public void disable()
    {
        BLANK.high();
        _RelaysOn = false;
        _ActualRelayState = false;
        Log.get().LogEvent("[RELAYBANK] - Relays off");
    }
    
    private void _disable(){
    	BLANK.high();
        _ActualRelayState = false;
    	Log.get().LogEvent("[RELAYBANK] - Relays off, relays are request to be in the " + _RelaysOn + " state");
    }

    public void updateAllRelayROI(HashMap<Integer, LeptonROI> dict)
    {
        for(Relay r : _Relays)
        {
            r.setROIs(dict);
        }
    }

    public void update(float[] Frame)
    {
        try
        {
            if (_SPIInitialized)
            {
                int[] dutycycles = new int[Ports];

                for (int i = 0; i < _Relays.size(); i++)
                {
                	if(_Relays.get(i) instanceof ManualRelay){
                            ManualRelay mr = (ManualRelay)_Relays.get(i);
                            _Relays.get(i).setDutyCycle(mr.DutyCycle);
                	}else{
                            dutycycles[_Relays.get(i).DIO] = _Relays.get(i).get12BitDutyCycle();
                	}
                }
                _write(dutycycles);
            }
        }
        catch (Exception e)
        {
            Log.get().LogEvent(e.getMessage());
            //System.out.println(e.getMessage());
        }
    }
    
    private void _write(int[] data) throws InterruptedException{
    	for(int x = 23; x >= 0; x--){
    		for(int i = 11; i >= 0; i--){
        		CLK.low();
        		int tmp = data[x] >> i & 0x1;
    			if(tmp == 1){
        			DATA.high();
        		}else{
        			DATA.low();
        		}  
        		CLK.high();
    		}
    	}
		DATA.low();	
		CLK.low();  
		
		Thread.sleep(5);
		
        XLAT.high();
        XLAT.low();
    }

    public List<Relay> getRelays()
    {
        return _Relays;
    }

    public void updateDutyCycle(List<LeptonROI> ROIList)
    {
        //throw new NotImplementedException();
    }

    public String getRelayPIDasXML() throws ParserConfigurationException, TransformerException
    {
        Document d = Xml.getDocument();

    	Element root = d.createElement("PID_History");	
    	d.appendChild(root);
    	
    	String RelayName = "";
    	String Error = "";
    	String P = "";
    	String I = "";
    	String D = "";
    	String U = "";
    	String Temp = "";
    	
        for (Relay r : _Relays)
        {
            RelayName += r.Name + ",";
            Error += r.PID.getError() + ",";
            P += r.PID.getP() + ",";
            I += r.PID.getI() + ",";
            D += r.PID.getD() + ",";
            U += r.PID.getU() + ",";
            Temp += r.PID.getTemp() + ",";
        }
        
    	Element e = d.createElement("Relays");
    	e.appendChild(d.createTextNode(RelayName));
        root.appendChild(e);
        
    	e = d.createElement("Error");
    	e.appendChild(d.createTextNode(Error));
        root.appendChild(e);
        
    	e = d.createElement("P");
    	e.appendChild(d.createTextNode(P));
        root.appendChild(e);
        
    	e = d.createElement("I");
    	e.appendChild(d.createTextNode(I));
    	root.appendChild(e);
        
    	e = d.createElement("D");
    	e.appendChild(d.createTextNode(D));
    	root.appendChild(e);
        
    	e = d.createElement("U");
    	e.appendChild(d.createTextNode(U));
    	root.appendChild(e);
        
    	e = d.createElement("Temp");
    	e.appendChild(d.createTextNode(Temp));
    	root.appendChild(e);

        return Xml.documentToString(d);
    }
    
    @Override
    public String toString() {    	
    	return super.toString();
    }
    
    @Override
    public void run() {
        try {
            if(PWMSemaphore.tryAcquire(75, TimeUnit.MILLISECONDS)){
                    Thread t = new Thread(this);
                    t.start();
            }
        } catch (InterruptedException e) {
            Log.get().LogEvent(e.getMessage());
        }
    }

    @Override
    public void FrameReady() {
        float[] frame = Lepton.getFrame();


        for (int i = 0; i < _Relays.size(); i++)
        {
            _Relays.get(i).updateROI(frame);
        }

        if(_RelaysOn){
            update(frame);
        }
        PWMSemaphore.release();
    }
	
	@Override
	public void LeptonStatusUpdate(LEPTON_STATUS s) {
		switch(s){
		case BUSY:
			break;
		case NORMAL:
                    if(_RelaysOn){
                        Log.get().LogEvent("[RELAYBANK] Relays on");
                        enable();
                    }else{
                        Log.get().LogEvent("[RELAYBANK] Relays off");
                        disable();
                    }
                    break;		
		case HARD_REBOOT:
                    Log.get().LogEvent("[RELAYBANK] Disabling for Hard Reboot of Lepton - Relays off");
                    _disable();
                    break;			
		default:
			break;
		
		}
	}
	
	public void createROILog(File f) throws IOException {
		FileWriter fw;
		
		fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		
		String header = "Time\t\t";
		
		Set<LeptonROI> roi = new HashSet<>();
		
		for(Relay r : _Relays){
			for(LeptonROI rr : r.LeptonROI){
				roi.add(rr);
			}
		}	
		
		for(LeptonROI r : roi){
			header += r.getID() + "\t";
		}
		
		bw.write(header + "\r\n");
		bw.flush();
		bw.close();
		
		fw.close();
	}
	
	public void writeROILog(File f) throws IOException{
            FileWriter fw;

            fw = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fw);

            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
            fmt.setCalendar(new GregorianCalendar());
            String line = fmt.format(new Date()) + "\t" + (_ActualRelayState == true) + "\t";

            Set<LeptonROI> roi = new HashSet<>();

            for(Relay r : _Relays){
                for(LeptonROI rr : r.LeptonROI){
                    float dutyCycle = (float)r.get12BitDutyCycle() / 4096.0f;
                    line += String.format("%.2f", rr.getMean()) + "," + 
                            _DecimalFormatter.format(rr.getStd()) + "\t"; //+ ","; 
                            //String.format("%.2f", rr.getStd()) + "," + 
                            //String.format("%.2f", dutyCycle) + "," + 
                            //String.format("%.2f", r.getDutyCycleAvg()) + "\t";
                }
            }	

            bw.write(line + "\r\n");
            bw.flush();
            bw.close();

            fw.close();
	}
	
	public static void main(String[] args) throws InterruptedException{
			Float[] frame = new Float[80*60];
			
			List<Relay> rlist = new ArrayList<>(); 
			List<Integer> roi = new ArrayList<>();
			roi.add(0);
			roi.add(10);
			roi.add(40);
			roi.add(60);
			
			for(int i = 0; i < 2; i++){
				PID pid = new PID();
				pid.setSetPoint(80);
				Relay r = new Relay();	
				r.ID = i;
				r.DIO = i;
				r.PID = pid;
				r.ROIids = roi;
				rlist.add(r);
			}
			
			
			RelayBank rb = new RelayBank(rlist, GpioFactory.getInstance());

	}

    @Override
    public void StatusReady() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
