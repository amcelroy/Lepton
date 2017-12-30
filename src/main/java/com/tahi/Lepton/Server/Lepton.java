package com.tahi.Lepton.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import com.tahi.Algorithms.Polynomial;
import com.tahi.Lepton.Server.LeptonStatusListener.LEPTON_STATUS;
import com.tahi.Logging.Log;
import com.tahi.Logging.Xml;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

public class Lepton implements LeptonServerListener, Runnable{
    int i2c;

    int SPIVideo;
    
    public enum LEPTON_VERSION { TWO, THREE };

    private final List<LeptonStatusListener> LeptonStatusListeners = new ArrayList<LeptonStatusListener>();

    public Polynomial TemperaturePoly;

    final static public int Width = 80;
    final static public int Height = 60;
    final static public int PacketSize = (80 + 2)* 2;
    final static public int PacketWidth = 80;
    final static public int PacketHeight = 60;
    final static public LEPTON_VERSION LeptonVersion = LEPTON_VERSION.TWO;
    
    static volatile JSONObject JSONStatus;
    
    Byte[] packet;
    private byte[] Frame;
    public static volatile List<Float> TemperatureFrame;

    public int Status;
    
    private boolean Rebooting = false;
     
    LEPTON_STATUS _Status = LEPTON_STATUS.NORMAL;

    @Override
    public void rebooting() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void runningFFC() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        _Status = LEPTON_STATUS.FFC_IN_PROGRESS;
    }

    @Override
    public void rebootFinished() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finishedFFC() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class REGISTER
    {
        public final static int STATUS = 0x2;
    	public final static int COMMAND = 0x4;
    	public final static int DATALENGTH = 0x6;
    	public final static int LEP_I2C_DATA_0_REG = 0x08;
    	public final static int LEP_I2C_DATA_1_REG = 0xA;
    	public final static int LEP_I2C_DATA_2_REG = 0xC;
    	public final static int LEP_I2C_DATA_3_REG = 0xE;
    	public final static int LEP_I2C_DATA_4_REG = 0x10;
    	public final static int LEP_I2C_DATA_5_REG = 0x12;
    	public final static int LEP_I2C_DATA_6_REG = 0x14;
    	public final static int LEP_I2C_DATA_7_REG = 0x16;
    	public final static int LEP_I2C_DATA_8_REG = 0x18;
    	public final static int LEP_I2C_DATA_9_REG = 0x1A;
    	public final static int LEP_I2C_DATA_10_REG = 0x1C;
    	public final static int LEP_I2C_DATA_11_REG = 0x1E;
    	public final static int LEP_I2C_DATA_12_REG = 0x20;
    	public final static int LEP_I2C_DATA_13_REG = 0x22;
    	public final static int LEP_I2C_DATA_14_REG = 0x24;
    	public final static int LEP_I2C_DATA_15_REG = 0x26;
    }
    
    public class RADIOMETERY_CONTROL{
    	public final static int LEP_RAD_DISABLE = 0;
    	public final static int LEP_RAD_ENABLE = 1;
    }

    public class SYSGAINMODE
    {
    	public final static int LEP_SYS_GAIN_MODE_HIGH = 0;
    	public final static int LEP_SYS_GAIN_MODE_LOW = 1;
    	public final static int LEP_SYS_GAIN_MODE_AUTO = 2;
    	public final static int LEP_SYS_END_GAIN_MODE = 3;
    }

    public class COMMAND
    {
    	public final static int GET = 0;
    	public final static int SET = 1;
    	public final static int RUN = 2;
    	public final static int INVALID = 3;
    }

    public class TEMPERATURE
    {
    	public final static int COUNT = 0;
    	public final static int CELCIUS = 1;
    	public final static int FARENHEIT = 2;
    	public final static int KELVIN = 3;
    }

    public class MODULES
    {
    	public final static int AGC = 0x1;
    	public final static int SYS = 0x2;
    	public final static int VID = 0x3;
    	public final static int OEM = 0x48;
    	public final static int RAD = 0x4E;
    }

    public class LEP_AGC_ENABLE_TAG
    {
    	public final static int LEP_AGC_DISABLE = 0;
    	public final static int LEP_AGC_ENABLE = 1;
    	public final static int LEP_END_AGC_ENABLE = 2;
    }

    public class AVERAGE
    {
    	public final static int LEP_STATUS_FA_DIV_1 = 0;
    	public final static int LEP_STATUS_FA_DIV_2 = 1;
    	public final static int LEP_STATUS_FA_DIV_4 = 2;
    	public final static int LEP_STATUS_FA_DIV_8 = 3;
    	public final static int LEP_STATUS_FA_DIV_16 = 4;
    	public final static int LEP_STATUS_FA_DIV_32 = 5;
    	public final static int LEP_STATUS_FA_DIV_64 = 6;
    	public final static int LEP_STATUS_FA_DIV_128 = 7;
    	public final static int LEP_STATUS_FA_ERROR = 8;
    }

    public class LEP_SYS_FFC_SHUTTER_MODE
    {
    	public final static int LEP_SYS_FFC_SHUTTER_MODE_MANUAL = 0;
    	public final static int LEP_SYS_FFC_SHUTTER_MODE_AUTO = 1;
    	public final static int LEP_SYS_FFC_SHUTTER_MODE_EXTERNAL = 2;
    	public final static int LEP_SYS_FFC_SHUTTER_MODE_END = 3;

    }

    public class LEP_SYS_SHUTTER_TEMP_LOCKOUT_STATE
    {
    	public final static int LEP_SYS_SHUTTER_LOCKOUT_INACTIVE = 0;  /* not locked out */
    	public final static int LEP_SYS_SHUTTER_LOCKOUT_HIGH = 1;    /* lockout due to high temp */
    	public final static int LEP_SYS_SHUTTER_LOCKOUT_LOW = 2;/* lockout due to low temp */
    }

    public class LEP_SYS_ENABLE
    {
    	public final static int LEP_SYS_DISABLE = 0;
    	public final static int LEP_SYS_ENABLE = 1;
    	public final static int LEP_END_SYS_ENABLE = 2;
    }
    
    public class SHUTTER_POSISTION
    {
    	public final static int LEP_SYS_SHUTTER_POSITION_UNKNOWN = -1;
    	public final static int LEP_SYS_SHUTTER_POSITION_IDLE = 0;
    	public final static int LEP_SYS_SHUTTER_POSITION_OPEN = 1;
    	public final static int LEP_SYS_SHUTTER_POSITION_CLOSED = 2;
    	public final static int LEP_SYS_SHUTTER_POSITION_BRAKE_ON = 3;
    }

    public class FFC_STATUS
    {
        public final static int LEP_SYS_STATUS_WRITE_ERROR  = -2;
        public final static int LEP_SYS_STATUS_ERROR = -1;
        public final static int LEP_SYS_STATUS_READY = 0;
        public final static int LEP_SYS_STATUS_BUSY = 1;
        public final static int LEP_SYS_FRAME_AVERAGE_COLLECTING_FRAMES = 2;
    }
    
    public class FFC_STATES
    {
        public final static int LEP_SYS_FFC_NEVER_COMMANDED  = 0;
        public final static int LEP_SYS_FFC_IMMINENT = 1;
        public final static int LEP_SYS_FFC_IN_PROCESS = 2;
        public final static int LEP_SYS_FFC_DONE = 3;
    }

    ArrayBlockingQueue<QueueObject> spiQueue;
    
    public Lepton(ArrayBlockingQueue<QueueObject> Q)
    {
        Frame = new byte[PacketHeight * PacketWidth * 2];
        TemperatureFrame = Arrays.asList(new Float[Lepton.Width*Lepton.Height]);
        packet = new Byte[PacketSize];

        //(.05872f * x*x - 479.23f + 100.0f - 32f) * 5 / 9;
        TemperaturePoly = new Polynomial();
        TemperaturePoly.setA(0.0f);
        TemperaturePoly.setB(1.0f);
        TemperaturePoly.setC(0.0f);
        
        JSONStatus = new JSONObject();
        
        spiQueue = Q;
    }

    I2CDevice LeptonI2C;
    I2CBus Bus;
    
    int _StaleFrameCount = 0;
    
    public float Uptime;
    
    public float MeanFrameTemp = 0;
    
    public void init() throws Exception
    {
        Bus = I2CFactory.getInstance(I2CBus.BUS_1);
        LeptonI2C = Bus.getDevice(0x2a);

        while(getStatus() != 6){
                Thread.sleep(50);
        }

        System.out.println("Lepton Status: Started");
        Log.get().LogEvent("Lepton Status: Started");
        System.out.println("Lepton Status: Checking Busy Bit");
        Log.get().LogEvent("Lepton Status: Checking Busy Bit");

        while(isBusy()){
                Thread.sleep(50);
        }

        System.out.println("Lepton Status: Busy bit clear");        	
        Log.get().LogEvent("Lepton Status: Busy bit clear");

        getOEMStatus();
        int status = getStatus();

        //enableRadiometery();
        getFFCSettings();
        uptime();
        getAUXTemp();
        disableAGC();
        lowGain();
        setFFCMode(LEP_SYS_FFC_SHUTTER_MODE.LEP_SYS_FFC_SHUTTER_MODE_MANUAL);

        byte error = setNumFramesAverage(AVERAGE.LEP_STATUS_FA_DIV_1);
        runFFC();
    }

    public void addLeptonStatusListener(LeptonStatusListener l){
    	if(l == null)
    		return;
    	
    	LeptonStatusListeners.add(l);
    }
    
    public void removeStatusLeptonListener(LeptonStatusListener l){
    	if(l == null)
    		return;
    	
    	LeptonStatusListeners.remove(l);
    }
    
    private void writeToRegister(int register, int[] dataBuffer) throws IOException
    {
         byte[] data = new byte[2 + (dataBuffer.length * 2)];
         
         // register.getRegisterAddress() returns the address of the register as integer value (e.g. CommandID-Register: 0x0004)
         data[0] = (byte)((register >> 8) & 0xFF);
         data[1] = (byte)((register) & 0xFF);
         for (int i = 0; i < dataBuffer.length; i++)
         {
              data[2+(2*i)] = (byte) ((dataBuffer[i] >> 8) & 0xFF);
              data[2+(2*i) + 1] = (byte) ((dataBuffer[i]) & 0xFF);
         }
         
         LeptonI2C.write(data);
         
 		waitBusy();    
    }
    
    private void readRegister(int register, int[] dataBuffer) throws IOException
    {
         // register.getRegisterAddress() returns the address of the register as integer value (e.g. CommandID-Register: 0x0004)
         byte[] writeBuffer = new byte[] {(byte)((register >> 8) & 0xFF), (byte)((register) & 0xFF)};
         byte[] readBuffer = new byte[dataBuffer.length * 2];
         LeptonI2C.read(writeBuffer, 0, writeBuffer.length, readBuffer, 0, readBuffer.length);
     
    //   // Can also be written in two single statements.
    //   i2c.write(writeBuffer);
    //   i2c.read(readBuffer, 0, readBuffer.length);
     
         for (int i = 0; i < dataBuffer.length; i++)
         { 
        	 dataBuffer[i] = (((readBuffer[(2*i)] & 0xFF) << 8) | (readBuffer[(2*i) + 1] & 0xFF)); 
         }
    }

    public boolean isBusy() throws IOException
    {
    	try{
    		int[] reg = new int[1];
    		readRegister(REGISTER.STATUS, reg);
    		return ((reg[0] & 0x1) == 1);
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }

    public int getStatus() throws IOException
    {
    	try{
            int[] reg = new int[1];
            readRegister(REGISTER.STATUS, reg);  
            int error = reg[0] >> 8;
            byte e = (byte)error; 
            int x = (reg[0] & 0x6);
            return x;
    	}catch(Exception e){
            e.printStackTrace();
            return -1;
    	}
    }
    
    public byte getError(String fnc) throws IOException
    {
		if(Rebooting)
			return 0;
    	
    	int[] reg = new int[1];
    	readRegister(REGISTER.STATUS, reg);
    	byte error = (byte)(reg[0] >> 8);
    	if(error != 0)
    		Log.get().LogEvent("Lepton returned an error of " + error + " when calling function " + fnc);
    	return error;
    }

    private LeptonData[] getDataRegisters() throws IOException
    {
        if(Rebooting){
            return null;
        }
    	
    	LeptonData[] results;
    
        int[] reg = new int[1];   
        readRegister(REGISTER.DATALENGTH, reg);
        int total = reg[0]/2;
        
        results = new LeptonData[total];
        for(int i = 0; i < total; i++){
            int[] r = new int[1];
            readRegister(REGISTER.LEP_I2C_DATA_0_REG + i*2 , r);
            results[i] = new LeptonData(r[0]);
        }
        
        return results;
    }
    
    private void setDataRegisters(LeptonData[] ld) throws IOException
    {
		if(Rebooting)
			return;
    	
        int[] reg = new int[ld.length];   
        writeToRegister(REGISTER.DATALENGTH, new int[] { ld.length });

        for(int i = 0; i < ld.length; i++){
        	writeToRegister(REGISTER.LEP_I2C_DATA_0_REG + i*2 , new int[] { ld[i].asInt() });
        }
    }

    public String statusAsXML() throws ParserConfigurationException, TransformerException, DOMException, IOException
    {
        Document d = Xml.getDocument();

        Element root = d.createElement("LeptonStatus");
        d.appendChild(root);
        
        Element e = d.createElement("Temperature");
        e.setAttribute("FPATemp", getFPATemp().toString());
        e.setAttribute("AUXTemp", getAUXTemp().toString());
        root.appendChild(e);
        
        return Xml.documentToString(d);
    }
    
    public void enableRadiometery() throws IOException{
		if(Rebooting)
			return;
    	
    	writeToRegister(REGISTER.DATALENGTH, new int[] { 2 });
        writeToRegister(REGISTER.LEP_I2C_DATA_0_REG, new int[] { RADIOMETERY_CONTROL.LEP_RAD_ENABLE });
        writeToRegister(REGISTER.LEP_I2C_DATA_1_REG, new int[] { 0 });
        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.AGC << 8, COMMAND.SET});
        
        getError("Enable Radiometery");
        
        Status = getStatus();
    }
    
    public void disableRadiometery() throws IOException{
		if(Rebooting)
			return;
    	
    	writeToRegister(REGISTER.DATALENGTH, new int[] { 2 });
        writeToRegister(REGISTER.LEP_I2C_DATA_0_REG, new int[] { RADIOMETERY_CONTROL.LEP_RAD_DISABLE });
        writeToRegister(REGISTER.LEP_I2C_DATA_1_REG, new int[] { 0 });
        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.AGC << 8, COMMAND.SET});

        writeToRegister(REGISTER.COMMAND, new int[] {MODULES.RAD << 8 | 0x10});  
        byte e = getError("Disable Radiometery");
        LeptonData[] results = getDataRegisters();
        
        getError("Disable Radiometery");
        
        Status = getStatus();
    }

    public void disableAGC() throws IOException
    {
		if(Rebooting)
			return;
    	
        //Write that 2 bytes are in the data length register
        writeToRegister(REGISTER.DATALENGTH, new int[] { 2 });
        writeToRegister(REGISTER.LEP_I2C_DATA_0_REG, new int[] { 0 });
        writeToRegister(REGISTER.LEP_I2C_DATA_1_REG, new int[] { 0 });
        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.AGC << 8, COMMAND.SET});

        getError("Disable AGC");
        
        Status = getStatus();
    }
    
    public void lowGain() throws IOException{
		if(Rebooting)
			return;
    	
        //Write that 2 bytes are in the data length register
        writeToRegister(REGISTER.DATALENGTH, new int[] { 2 });
        writeToRegister(REGISTER.LEP_I2C_DATA_0_REG, new int[] { 1 });
        writeToRegister(REGISTER.LEP_I2C_DATA_1_REG, new int[] { 0 });
        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x49 });

        getError("Set Low Gain");
        
        Status = getStatus();
    }

    public void enableAGC()
    {
//        writeToRegister(REGISTER.DATALENGTH, new LeptonData(0, 2));
//        writeToRegister(REGISTER.LEP_I2C_DATA_0_REG, new LeptonData(0, 1));
//        writeToRegister(REGISTER.LEP_I2C_DATA_1_REG, new LeptonData(0, 0));
//        writeToRegister(REGISTER.COMMAND, new LeptonData(MODULES.AGC, COMMAND.SET));
//
//        LeptonData[] results = getSequence(new LeptonData(MODULES.AGC, 0x00));
//
//        Status = getStatus();
    }

    public Float getAUXTemp() throws IOException
    {
        if(Rebooting)
            return 0.0f;
    	
        float temp = 0;

        writeToRegister(REGISTER.COMMAND, new int[] {MODULES.SYS << 8 | 0x10});  
        byte e = getError("AUXTemp");

        LeptonData[] results = getDataRegisters();

        if (results == null)
            return 0.0f;

        temp = results[0].asInt() / 100.0f;

        return temp;
    }

    public Float getFPATemp() throws IOException
    {
		if(Rebooting)
			return 0.0f;
    	
        float temp = 0;

        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x14 }); 
        byte e = getError("FPATemp");
        
        LeptonData[] results = getDataRegisters();

        if (results == null)
            return 0.0f;

        temp = results[0].asInt() / 100.0f;

        return temp;
    }

/*    public SceneStatistics getSceneStatistics()
    {
    	LeptonData[] results = getSequence(new Data(0x2, 0x2C));

        SceneStatistics ss;
        ss.MeanIntensity = results[0].asUnsignedShort();
        ss.MaxIntensity = results[1].asUnsignedShort();
        ss.MinIntensity = results[2].asUnsignedShort();
        ss.NumPixels = results[3].asUnsignedShort();            

        return ss;
    }*/

    public int getShutterPosition()
    {
//        LeptonData[] results = getSequence(new LeptonData(0x2, 0x38));
//
//        int sp = results[0].Low;
//
//        return sp;
    	return 0;
    }

    public void runFFC() throws IOException
    {	
    	if(Rebooting)
			return;
    	
    	try{
    		setShutter(SHUTTER_POSISTION.LEP_SYS_SHUTTER_POSITION_CLOSED);    	
    		writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x42 } );
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	while(!isFFCComplete()){
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}  
    	
    	setShutter(SHUTTER_POSISTION.LEP_SYS_SHUTTER_POSITION_OPEN);
    }
    
    public boolean isFFCComplete() throws IOException{
		if(Rebooting)
			return false;
    	
		try{
	    	writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x44 } );
	    	
	    	try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
	    	LeptonData[] ld = getDataRegisters();
	    	if(ld == null){
	    		return false;
	    	}
	    	
	    	if(ld[0].Low == 0){
	    		return true;
	    	}else{
	    		return false;
	    	}
		}catch(Exception e){
			return false;
		}
    }
    
    public int getNumFramesAverage() throws IOException
    {
        if(Rebooting)
            return 0;
    	//LeptonData[] results = getSequence(new LeptonData(MODULES.SYS, 0x24));

    	int[] reg = new int[2];
    	reg[0] = MODULES.SYS;
    	reg[1] = 0x42;
    	writeToRegister(REGISTER.COMMAND, reg);
    	readRegister(REGISTER.DATALENGTH, reg);
    	
        return 0;
    }

    public byte setNumFramesAverage(int avg) throws IOException
    {
        if(Rebooting)
                return 0;
    	
    	writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x24 });
    	
    	LeptonData[] ld = getDataRegisters();
    	ld[0].Low = avg;
    	setDataRegisters(ld);
    	writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x25 });
    	
        return getError("Number of Frames to Average");
    }

    public float uptime() throws IOException
    {
        if(Rebooting)
                return 0.0f;
    	
    	
        float uptime = 0;

        int[] reg = new int[] { MODULES.SYS << 8 | 0x0C };
        writeToRegister(REGISTER.COMMAND, reg);
        
        byte e = getError("Uptime");
        int status = getStatus();
        
        LeptonData[] results = getDataRegisters();

        long x = (long)(results[1].High << 24);
        long i = (long)((long)(results[1].High << 24) + (long)(results[1].Low << 16) + (long)(results[0].High << 8) + results[0].Low);
        uptime = (float)i / 1000.0f;

        return uptime;
    }

	
    public LeptonData[] getFFCSettings() throws IOException{
        if(Rebooting)
                return null;

        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x3C });
        LeptonData[] results = getDataRegisters();

        return results;
    }
	
    public int getFFCStates() throws IOException{
        if(Rebooting)
                return FFC_STATUS.LEP_SYS_STATUS_BUSY;

        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x4C });
        LeptonData[] results = getDataRegisters();

        if(results != null){
            if(results.length == 0){
                return FFC_STATES.LEP_SYS_FFC_DONE;
            }else{
                return results[0].Low;
            }
        }
        return FFC_STATES.LEP_SYS_FFC_NEVER_COMMANDED;
    }
	
    public int getFFCStatus() throws IOException{
        if(Rebooting)
            return FFC_STATUS.LEP_SYS_STATUS_BUSY;

        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x44 });
        LeptonData[] results = getDataRegisters();

        if(results != null){
                switch(results[0].Low){
                case FFC_STATUS.LEP_SYS_STATUS_READY:
                        return FFC_STATUS.LEP_SYS_STATUS_READY;
                default:
                        return FFC_STATUS.LEP_SYS_STATUS_BUSY;

                }
        }
        return FFC_STATUS.LEP_SYS_STATUS_BUSY;
    }
	
    public void setFFCMode(int Mode) throws IOException{
        if(Rebooting)
                return;

        LeptonData[] ffcSettings = getFFCSettings();

        ffcSettings[0].High = 0;
        ffcSettings[0].Low = Mode;

        setDataRegisters(ffcSettings);
        writeToRegister(REGISTER.COMMAND, new int[] { MODULES.SYS << 8 | 0x3D });
        byte err = getError("FFC Mode");

        ffcSettings = getFFCSettings();
        return;
    }
	
    private void waitBusy() throws IOException{
        //if(Rebooting)
        //	return;

        int x = 0;
        while(isBusy()){
            try {
                    Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.get().LogEvent(e.getMessage());
            }
            x += 1;
            if(x >= 10){
                Log.get().LogEvent("Error - Busy bit high for 500ms");
                updateStatus(LEPTON_STATUS.HARD_REBOOT);
            }
        }
    }
	
    public void setShutter(int State) throws IOException{
        if(Rebooting)
                return;

        writeToRegister(REGISTER.COMMAND, new int[] {MODULES.SYS << 8 | 0x38 });

        LeptonData[] ld = getDataRegisters();
        ld[0].Low = State;

        setDataRegisters(ld);
        writeToRegister(REGISTER.COMMAND, new int[] {MODULES.SYS << 8 | 0x39 });

        getError("Set Shutter");
    }
	
    public void getOEMStatus() throws IOException{
        writeToRegister(REGISTER.COMMAND, new int[] {MODULES.OEM << 8 | 0x48 });
        int status = getStatus();

        LeptonData[] ld = getDataRegisters();

        getError("Get OEM Status");
    }

    public void getFrameAsTemperature(byte[] frame)
    {
        Float[] tmp_Frame = new Float[frame.length/2];
        for(int y = 0; y < frame.length; y+=2){
            byte msb = frame[y + 1];
            byte lsb = frame[y];
            byte[] a = { lsb, msb };
            Short xx = ByteBuffer.wrap(a).getShort();
            int yy = (int)xx & 0xFFFF;
            float tmp = ((float)yy / 100.0f) - 273.0f;//TemperaturePoly.yUShort(x);
            tmp_Frame[y/2] = tmp;  
        }
        TemperatureFrame = Arrays.asList(tmp_Frame);
    }
    
    public boolean isRebooting() {
    	return Rebooting;
    }
    
    public void setRebooting(boolean isRebooting) throws IOException, InterruptedException{
    	Rebooting = isRebooting;
    }
    
    private void updateStatus(LEPTON_STATUS s){
    	_Status = s;
    	if(LeptonStatusListeners.size() > 0){
            for(LeptonStatusListener ll : LeptonStatusListeners){
                if(ll != null)
                    ll.LeptonStatusUpdate(s);
            }
    	}
    }
    
    int packetsWithoutFrame = 0;
    
    public static JSONObject getJSONStatus(){
        return JSONStatus;
    }
  
    
    int m_CompleteFrameCounter = 0;
    
    byte[] m_TmpRow;
    
    byte[] m_SuperFrame = null;
    
    int m_CountReset = 60;
   
    
    private void resetFrameLoop(){
        packetsWithoutFrame = 0;
        _StaleFrameCount = 0;

        try{                                    
            //don't pass on FFC Frames
            if(getFFCStates() == FFC_STATES.LEP_SYS_FFC_IN_PROCESS){
                updateStatus(LEPTON_STATUS.FFC_IN_PROGRESS);
            }else{
                if(_Status != LEPTON_STATUS.NORMAL){
                    updateStatus(LEPTON_STATUS.NORMAL);
                }

                if(LeptonStatusListeners.size() > 0){
                    for(LeptonStatusListener ll : LeptonStatusListeners){
                        if(ll != null){
                            ll.FrameReady();
                            if(m_CompleteFrameCounter > m_CountReset){
                                ll.StatusReady();
                            }
                        }
                    }
                }
            }

            m_CompleteFrameCounter += 1;
            if(m_CompleteFrameCounter > m_CountReset){
                JSONStatus = new JSONObject();
                JSONStatus.put("type", "status");
                JSONStatus.put("FPATemp", getFPATemp());
                JSONStatus.put("AUXTemp", getAUXTemp());
                JSONStatus.put("Uptime", uptime()/3600);
                m_CompleteFrameCounter = 0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    int ccitt_16Table[] = {
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
        0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
        0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
        0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
        0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
        0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
        0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
        0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
        0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
        0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
        0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
        0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
        0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
        0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
        0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
        0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
        0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
        0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
        0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
        0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
        0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
        0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
        0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
     };
    
    private int crc16rowBE(final byte[] bytes) {
        int crcin = 0x0000;
        
        for (int b = 0; b < bytes.length; b += 2) {
            crcin = crc16(bytes[b], crcin);
            crcin = crc16(bytes[b + 1], crcin);
        }        
        
        return crcin;
    }
    
    private int crc16rowLE(final byte[] bytes) {
        int crcin = 0x0000;
        
        for (int b = 0; b < bytes.length; b += 2) {
            crcin = crc16(bytes[b + 1], crcin);
            crcin = crc16(bytes[b], crcin);
        }        
        
        return crcin;
    }
    
    private int crc16(byte value, int crcin){     
        int bb = value & 0xff;
        crcin = (crcin << 8) ^  ccitt_16Table[((crcin >> 8) ^ bb) & 255];  
        crcin &= 0xffff;
        return crcin;
    }
    
    @Override
    public void run()
    {
    	//Note!!! Change the SPI Buffer size! https://raspberrypi.stackexchange.com/questions/65595/spi-transfer-fails-with-buffer-size-greater-than-4096
        
        int segment = 0;
        
        while(true){
            try{
                m_TmpRow = spiQueue.poll().Packet;
                if(m_TmpRow == null){
                    try {
                        Thread.sleep(15);
                        packetsWithoutFrame += 1;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Lepton.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{  
                    int frameCount = (int)m_TmpRow[1] & 0xFF;// + (packet[PacketSize * k + 2];

                    int crc_pack = (((int)m_TmpRow[2] & 0xFF) << 8) + ((int)m_TmpRow[3] & 0xFF);

                    if(LeptonVersion == LEPTON_VERSION.THREE){
                        if(frameCount == 20){
                            segment = (int)m_TmpRow[0] & 0xFF;
                            segment &= 0x70;
                            segment = segment >> 4;
                        }
                    }

                    m_TmpRow[0] &= 0x0f;
                    m_TmpRow[2] = 0;
                    m_TmpRow[3] = 0;

                    int crc_BE = crc16rowBE(m_TmpRow);

                    if(crc_BE == crc_pack){  
                        try{
                            System.arraycopy(m_TmpRow, 4, Frame, frameCount*(PacketWidth * 2), (PacketWidth * 2)); 

                            if(frameCount == PacketHeight - 1){                        
                                if(LeptonVersion == LEPTON_VERSION.THREE){
                                    if(m_SuperFrame == null){
                                        m_SuperFrame = new byte[Width*Height*2];
                                    }

                                    if(segment > 0 && segment <= 4){
                                        //Copy the segment to the superframe
                                        int frameSize = PacketHeight*PacketWidth*2;
                                        System.arraycopy(Frame, 0, m_SuperFrame, (segment - 1)*frameSize, frameSize);

                                        if(segment == 4){
                                            getFrameAsTemperature(m_SuperFrame);
                                            resetFrameLoop();
                                        }
                                    }                            
                                }else{
                                    //Do Lepton 2 stuff
                                    getFrameAsTemperature(Frame);
                                    resetFrameLoop();                 
                                }
                            }
                        }catch(IndexOutOfBoundsException e){
                            int x = 0;
                        }
                    }else{
                        packetsWithoutFrame += 1;
                        if(packetsWithoutFrame > 450){

                            spiQueue.clear();

                            //TODO: Add reboot back in
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    updateStatus(LEPTON_STATUS.HARD_REBOOT);
                                }
                            });   
                            t.start();

                            _StaleFrameCount = 0;
                            packetsWithoutFrame = 0;
                        }
                    }              
                }        
            }catch(Exception e){
                int x = 0;
            }
        }
    }
}
