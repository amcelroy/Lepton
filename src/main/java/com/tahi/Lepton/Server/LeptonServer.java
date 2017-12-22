package com.tahi.Lepton.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

import com.tahi.Algorithms.Polynomial;
import com.tahi.IO.Blackbody;
import com.tahi.IO.RelayBank;
import com.tahi.Lepton.Server.Lepton.SHUTTER_POSISTION;
import com.tahi.Logging.Log;
import com.tahi.leptonwebapp.LeptonEndpoint;
import java.util.ArrayList;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LeptonServer implements Runnable, LeptonStatusListener, GpioPinListenerDigital, ServletContextListener, ServerMessageInterface {        
    JSONObject _ServerSettings;

    LeptonFrame CurrentFrame;

    LeptonConfig lConfig;

    Lepton lepton;

    RelayBank _RelayBank;

    static volatile LeptonConfig _LeptonConfig;

    boolean  RelaysOn = false;

    int StaleFrameCounter = 0;

    Path logPath = FileSystems.getDefault().getPath("/tahi", "LeptonTemp.log");

    boolean BlackbodyCal = false;

    String ROILogName = "";
    boolean ROILoggingEnabled = false;

    GpioController _GpioController;

    public static final String Config = "/tahi/config.cfg";

    public static final String Relays = "/tahi/relay.a";

    private Blackbody _Blackbody;

    private GpioPinDigitalInput Alarm;

    private GpioPinDigitalOutput LeptonPWR;

    private Timer AlarmTimer;

    int SPICounter = 0;

    boolean _RecordTemps = false;

    LocalDateTime StartTime = LocalDateTime.now();

    boolean disableBlackbody = false;
    
    private LeptonEndpoint _LeptonEndpoint;
    
    Timer BlackbodyUpdateTimer;
    
    Timer roiLogTimer;
    
    Timer statusTimer;
    
    LeptonSPI _LeptonSPI = new LeptonSPI();
    Thread _LeptonSPIThread;
    
    Thread _LeptonProcessThread;
    
    ArrayList<LeptonServerListener> m_ServerListener = new ArrayList<>();
    
    public LeptonServer()  {

    }
    
    public void addServerListener(LeptonServerListener l){
        if(!m_ServerListener.contains(l)){
            m_ServerListener.add(l);
        }
    }
    
    public void removeServerListener(LeptonServerListener l){
        if(!m_ServerListener.contains(l)){
            m_ServerListener.remove(l);
        }
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static JSONObject getJSONConfig(){
        JSONObject o = new JSONObject();
        o.put("type", "config");
        o.put("config", _LeptonConfig.toXML());
        return o;
    }

    public void init() {
        //ServletContextListener.super.contextInitialized(sce); //To change body of generated methods, choose Tools | Templates.
        
        String defaultXML = "<LeptonConfig>" +
            "<ROI id=\"0\" TempBounds=\"20,185\">17,16,29,27</ROI>" +
            "<ROI id=\"1\" TempBounds=\"20,185\">29,11,45,27</ROI>" +
            "<ROI id=\"2\" TempBounds=\"20,185\">45,12,60,27</ROI>" +
            "<ROI id=\"3\" TempBounds=\"20,185\">17,27,29,37</ROI>" +
            "<ROI id=\"4\" TempBounds=\"20,185\">29,27,45,37</ROI>" +
            "<ROI id=\"5\" TempBounds=\"20,185\">45,27,60,37</ROI>" +
            "<ROI id=\"6\" TempBounds=\"20,185\">19,37,30,45</ROI>" +
            "<ROI id=\"7\" TempBounds=\"20,185\">30,37,45,45</ROI>" +
            "<ROI id=\"8\" TempBounds=\"20,185\">45,37,58,44</ROI>" +
            "<AutomaticGainControl enabled=\"0\" roi=\"0,0,79,59\" maxbinsize=\"4800\" minbinsize=\"512\" dampening=\"64\" emptybin=\"2\" ></AutomaticGainControl>" +
            "<Relay id=\"100\" DIO=\"0\" Name=\"SSR1\" roi=\"0\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"101\" DIO=\"1\" Name=\"SSR2\" roi=\"1\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"102\" DIO=\"2\" Name=\"SSR3\" roi=\"2\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"103\" DIO=\"3\" Name=\"SSR4\" roi=\"3\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"104\" DIO=\"4\" Name=\"SSR5\" roi=\"4\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"105\" DIO=\"5\" Name=\"SSR6\" roi=\"5\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"106\" DIO=\"6\" Name=\"SSR7\" roi=\"6\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"107\" DIO=\"7\" Name=\"SSR8\" roi=\"7\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Relay id=\"108\" DIO=\"8\" Name=\"SSR9\" roi=\"8\" PID=\"50,0,0\" Setpoint=\"180\"></Relay>" +
            "<Blackbody temp=\"23.0\" roi-id=\"1\"/>" + 
            //"<Algorithm a=\"1\" b=\"0\" c=\"0\">" + 
            //"<Duration Hours=\"23\" Minutes=\"00\"></Duration>" +
            "</LeptonConfig>";
        try {	
            File f = new File(Config);
            if(f.exists()){
                //load file
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buff = Files.readAllBytes(Paths.get(Config));
                    defaultXML = new String(buff);
                }              
            }      
            
            Thread leptonThread = new Thread(this);
            init(defaultXML);
            leptonThread.start();
        } catch (LeptonException | IOException e1) {
            Log.get().LogEvent(e1.getMessage());
        } catch (SAXException ex) {
            Log.get().LogEvent(ex.getMessage());
            Logger.getLogger(LeptonServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Log.get().LogEvent(ex.getMessage());
            Logger.getLogger(LeptonServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception e){
            Log.get().LogEvent(e.getMessage());
        }  
    }

    public void init(String ConfigString) throws LeptonException, IOException, SAXException, ParserConfigurationException{
        _LeptonEndpoint = new LeptonEndpoint();
        _LeptonEndpoint.registerMessageInterface(this);
        
        GpioUtil.enableNonPrivilegedAccess();
        _GpioController = GpioFactory.getInstance();

        Alarm = _GpioController.provisionDigitalInputPin(RaspiPin.GPIO_21);
        Alarm.setPullResistance(PinPullResistance.PULL_UP);
        Alarm.addListener(this);

        LeptonPWR = _GpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07);
        LeptonPWR.setPullResistance(PinPullResistance.PULL_UP);
        LeptonPWR.setState(true);

        _ServerSettings = new JSONObject();
        _ServerSettings.put("type", "settings");
        _ServerSettings.put("width", "80");
        _ServerSettings.put("height", "60");
        send(_ServerSettings);

        _LeptonConfig = new LeptonConfig(_GpioController);
        _LeptonConfig.init(ConfigString); 

        roiLogTimer = new Timer("ROI Log Timer");
        roiLogTimer.schedule(new TimerTask(){
            @Override
            public void run() {	
                    _RecordTemps = true;
                    
                    if(ROILoggingEnabled){
                        //TODO: Check file exists, if no, create
                        try{
                            
                            File f = new File("/tahi/ROI/" + ROILogName);
                            if(f.exists()){
                                _RelayBank.writeROILog(f);
                            }else{
                                _RelayBank.createROILog(f);
                            }
                        }catch(NullPointerException e){
                            
                        } catch (IOException e) {
                            Log.get().LogEvent(e.getMessage());
                            e.printStackTrace();
                        }
                    }
            }
        }, 0, 1000);

        BlackbodyUpdateTimer = new Timer("BB Update Timer");
        BlackbodyUpdateTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                BlackbodyCal = true;
            }
        }, 0, 120000);
    }
	
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if(event.getState().isHigh()){
            //Working normally
            if(AlarmTimer != null){
                    AlarmTimer.cancel();
            }
        }else{
            //Create new timer and check if the alarm is still high after 10 seconds
            AlarmTimer = new Timer();
            AlarmTimer.schedule(new TimerTask(){
                @Override
                public void run() {
                    if(_RelayBank != null){
                        sendMessage("Alarm triggered - shutting down relays");
                        _RelayBank.disable();
                    }
                }
            }, 10000);			
        }
    }
	
    private void _init(LeptonConfig lc){
        _LeptonConfig = lc;
        _RelayBank = lc.getRelayBank();
        _Blackbody = _LeptonConfig.getBlackbody();

        HashMap<Integer, LeptonROI> ROIDictionary = new HashMap<>();
        for(LeptonROI r : lc.getROIs())
        {
           ROIDictionary.put(r.ID, r);
        }
        //need to update the Lepton TempPoly
        BlackbodyCal = false;		

        _RelayBank.updateAllRelayROI(ROIDictionary);
        _RelayBank.disable();

        _Blackbody.updateAllRelayROI(ROIDictionary);
    }

    public void send(JSONObject o) throws IOException{
        if(o != null)
            _LeptonEndpoint.sendMessage(o);
    }
    
    public void sendMessage(String s){
        System.out.println(s);
    	Log.get().LogEvent(s);
        JSONObject o = new JSONObject();
        o.put("type", "message");
        o.put("message", s);
        try {
            send(o);
        } catch (IOException ex) {
            Logger.getLogger(LeptonServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
    @Override
    public void onMessage(String s) {
        try {
            String ss[] = s.split(":");
            
            File f;
            
            switch(ss[0].toLowerCase()){
                case "enablerelays":
                    _RelayBank.enable();
                    RelaysOn = true;
                    sendMessage("Relays on");
                    f = new File(Relays);
                    f.createNewFile();
                    break;
                    
                case "stopsaving":
                    ROILoggingEnabled = false;
                    break;
                    
                case "logroi":
                    if(ss.length > 1){
                        ROILogName = ss[1];
                        ROILoggingEnabled = true;
                    }else{
                        ROILoggingEnabled = false;
                    }
                    break;
                    
                case "listroilogs":
                    File folder = new File("/tahi/ROI");
                    File[] list = folder.listFiles();
                    String files = "";
                    for(File file : list){
                        files += file.getName() + "\t";
                    }   
                    JSONObject obj = new JSONObject();
                    obj.put("type", "roifiles");
                    obj.put("files", files);
                    send(obj);
                    break;
                    
                case "upload-config":
                    if(ss.length > 1){
                        try{
                            sendMessage("Config Uploaded");
                            _RelayBank.disable();
                            _RelayBank.shutdown();
                            
                            
                            //lepton.haltFFCThread();
                            lepton.removeStatusLeptonListener(_RelayBank);
                            lepton.removeStatusLeptonListener(_RelayBank);
                            lepton.removeStatusLeptonListener(_Blackbody);
                            
                            Polynomial tmpPoly = _Blackbody.getCurrentPolynomial();
                            
                            LeptonConfig tmp_config = new LeptonConfig(_GpioController);
                            tmp_config.init(ss[1]);
                            
                            _init(tmp_config);
                            lepton.addLeptonStatusListener(_RelayBank);
                            lepton.addLeptonStatusListener(_RelayBank);
                            lepton.addLeptonStatusListener(_Blackbody);
                            //lepton.startFFCThread();
                            
                            _Blackbody.setPolynomial(tmpPoly);
                            String rois = "";
                            for(LeptonROI r : _LeptonConfig.getROIs()){
                                rois += r.toXML();
                            }
                            /*JSONObject tmp_roi = new JSONObject();
                            tmp_roi.put("type", "roi");
                            tmp_roi.put("ROI", tmp_config.toXML());
                            send(tmp_roi);*/
                            sendMessage("Config Set");
                            
                            f = new File(Config);
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(tmp_config.toXML().getBytes());
                            fos.close();
                            
                            clearLog();
                        }catch(Exception e){
                            sendMessage(e.getMessage());
                        }
                    }
                    break;
                    
                case "fetch-config":
                    String a = _LeptonConfig.toXML();
                    sendMessage("fetch-config:" + a);
                    sendMessage("Log sent");
                    break;
                    
                case "disablerelays":
                    _RelayBank.disable();
                    RelaysOn = false;
                    sendMessage("Relays off");
                    f = new File(Relays);
                    if(f.exists()){
                        f.delete();
                    }
                    break;
                    
                case "ffc":
                    lepton.runFFC();
                    sendMessage("FFC Complete");
                    break;
                    
                case "blackbody":
                    String bb_msg = _LeptonConfig.getBlackbody().getCurrentPolynomial().getC().toString();
                    sendMessage("Current BB Offset: " + bb_msg);
                    break;
                    
                case "closeshutter":
                    lepton.setShutter(SHUTTER_POSISTION.LEP_SYS_SHUTTER_POSITION_CLOSED);
                    sendMessage("Shutter Closed");
                    break;
                    
                case "openshutter":
                    lepton.setShutter(SHUTTER_POSISTION.LEP_SYS_SHUTTER_POSITION_IDLE);
                    sendMessage("Shutter Open");
                    break;
            }
        } catch (IOException ex) {
            Log.get().LogEvent(ex.getLocalizedMessage());
        }
    }
	
    private void clearLog(){
        try{
            Files.delete(logPath);
            sendMessage("Log cleared");
        }catch(Exception e){
            sendMessage(e.getMessage());
        }
    }

    @Override
    public void run() {
        _LeptonSPI = new LeptonSPI();
        _LeptonSPIThread = new Thread(_LeptonSPI);
        _LeptonSPIThread.setName("SPI Thread");
        _LeptonSPIThread.start();
        
        lepton = new Lepton(_LeptonSPI.getSPIQueue());

        try{
            lepton.init();
        }catch(Exception e){
            Log.get().LogEvent(e.getMessage());
            System.exit(15);
        }

        _init(_LeptonConfig);

        //ORDER MATTERS!!
        lepton.addLeptonStatusListener(_RelayBank);
        lepton.addLeptonStatusListener(_LeptonEndpoint);
        lepton.addLeptonStatusListener(_Blackbody);
        lepton.addLeptonStatusListener(this);
        CurrentFrame = new LeptonFrame();
        
        addServerListener(_LeptonSPI);

        JSONObject lepConfig = new JSONObject();
        lepConfig.put("Config", _LeptonConfig.toXML());
        
        _LeptonProcessThread = new Thread(lepton);
        _LeptonProcessThread.setName("Lepton Processing Thread");
        _LeptonProcessThread.start();
        
        while(true){           
            try {   
                Thread.sleep(250);
                
                send(lepton.getJSONStatus());   
                

//                if(_RecordTemps){
//                    lepton.setRecordTemps(_RecordTemps);
//                    _RecordTemps = false;
//                }else{
//                    lepton.setRecordTemps(_RecordTemps);
//                }

                if(BlackbodyCal) {
                    if(_Blackbody.getTemperature() <= 0){
                        lepton.TemperaturePoly = new Polynomial(0.0f, 1.0f, 0.0f);
                    }else{
                        lepton.TemperaturePoly = _Blackbody.getNewPolynomial();
                        BlackbodyCal = false;
                    }
                }			
            } catch(Exception e){
                if(e.getMessage() != null){
                    sendMessage(e.getMessage());
                }
            }

            //Build finished
            if(LocalDateTime.now().isAfter(_LeptonConfig.getStopTime())){
                sendMessage("Build completed, turning off relays");
                _RelayBank.disable();
            }
        }
    }
	
    @Override
    public void LeptonStatusUpdate(LEPTON_STATUS s) {
        try{
            switch(s){
                case BUSY:
                    break;
                case NORMAL:
                    sendMessage("Lepton Normal");
                    break;
                case HARD_REBOOT:
                    sendMessage("SPI Resync failed, hard reboot needed.");
 
                    emitReboot();
                    LeptonPWR.setState(false);                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    LeptonPWR.setState(true);
                    
                    //Wait for reboot
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    
                    emitFFC();
                    lepton.runFFC();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                    
                    emitFFCFinished();
                    emitRebootFinished();
                    
                    break;			
                default:
                    break;

            }
        }catch(Exception e){
            Log.get().LogEvent(e.getMessage());
        }
    }
    
    private void emitReboot(){
        for(LeptonServerListener l : m_ServerListener){
            l.rebooting();
        }
    }
    
    private void emitRebootFinished(){
        for(LeptonServerListener l : m_ServerListener){
            l.rebootFinished();
        }
    }
    
    public void emitFFC(){
        for(LeptonServerListener l : m_ServerListener){
            l.runningFFC();
        }
    }
    
    public void emitFFCFinished(){
        for(LeptonServerListener l : m_ServerListener){
            l.finishedFFC();
        }
    }

    @Override
    public void FrameReady() {

    }

    @Override
    public void StatusReady() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
