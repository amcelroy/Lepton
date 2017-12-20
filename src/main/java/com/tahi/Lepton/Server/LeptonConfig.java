package com.tahi.Lepton.Server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.pi4j.io.gpio.GpioController;

import com.tahi.Algorithms.Polynomial;
import com.tahi.IO.Blackbody;
import com.tahi.IO.Relay;
import com.tahi.IO.RelayBank;
import com.tahi.IO.RelayBankStopTime;
import com.tahi.Logging.Xml;

public class LeptonConfig {
    private List<LeptonROI> ROIList;

    private LeptonAGC AGC;

    private RelayBank Relays;
    
    private RelayBankStopTime StopTime;
    
    private Blackbody _Blackbody;
    
    private GpioController _GpioController;

    public LeptonConfig(GpioController gpi){
	    ROIList = new ArrayList<LeptonROI>();
	    AGC = new LeptonAGC();
	    _Blackbody = new Blackbody();
	    _GpioController = gpi;
    }
    
    public RelayBank getRelayBank(){
    	return Relays;
    }

    public List<LeptonROI> getROIs(){
	    return ROIList;
    }

    public LeptonAGC getAGC(){
	    return AGC;
    }
    
    public LocalDateTime getStopTime(){
    	return StopTime.getStopTime();
    }
    
    public Blackbody getBlackbody(){
    	return _Blackbody;
    }

    public LeptonROI findROIByID(int id){
    	for(LeptonROI r : ROIList){
    		if(r.ID == id){
    			return r;
    		}
    	}
    		
		return null;
    }
    
    public String toXML()
    {
        String xml = "<LeptonConfig TempUnits=\"C\">\r\n";

        for(LeptonROI roi : ROIList)
        {
            xml += roi.toXML() + "\r\n";
        }

        for(Relay r : Relays.getRelays())
        {
            xml += r.toXML() + "\r\n";
        }
        
        xml += StopTime.toXML() + "\r\n";
        
        xml += _Blackbody.toXML() + "\r\n";

        xml += "</LeptonConfig>";

        return xml;
    }

    void _parse(Document d) throws LeptonException
    {
        List<Relay> relayList = new ArrayList<Relay>();

        NodeList relayNodeList = d.getElementsByTagName("Relay");
        NodeList roiNodeList = d.getElementsByTagName("ROI");
        NodeList duration = d.getElementsByTagName("Duration");
        NodeList pyrometer = d.getElementsByTagName("Blackbody");
        
        StopTime = null;
        
        for(int i = 0; i < roiNodeList.getLength(); i++){
        	ROIList.add(LeptonROI.fromXML(roiNodeList.item(i)));
        }
        
        for(int i = 0; i < relayNodeList.getLength(); i++){
        	relayList.add(Relay.fromXML(relayNodeList.item(i)));
        }
        
        for(int i = 0; i < duration.getLength(); i++){
        	StopTime = RelayBankStopTime.fromXML(duration.item(i));
        }
        
        if(StopTime == null){
        	StopTime = new RelayBankStopTime(LocalDateTime.MAX);
        }
        
        if(pyrometer != null){
        	_Blackbody = Blackbody.fromXML(pyrometer.item(0));
        }

        Relays = new RelayBank(relayList, _GpioController);
    }

    public void init(String XML) throws LeptonException, SAXException, IOException, ParserConfigurationException
    {
    	_parse(Xml.fromString(XML));
    }
}
