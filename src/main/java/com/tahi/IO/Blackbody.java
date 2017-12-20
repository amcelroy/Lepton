package com.tahi.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tahi.Algorithms.Polynomial;
import com.tahi.Lepton.Server.Lepton;
import com.tahi.Lepton.Server.LeptonException;
import com.tahi.Lepton.Server.LeptonROI;
import com.tahi.Lepton.Server.LeptonStatusListener;
import com.tahi.Logging.Log;
import com.tahi.Logging.Xml;
import java.util.Vector;

public class Blackbody implements LeptonStatusListener{
	private float Temperature;
	
	private int roiID;
	
	private final ArrayBlockingQueue<Float> History;
	
	private float MaxValueDetected;
	
	private Polynomial PreviousPoly;
	
	private LeptonROI ROI;
	
	public Blackbody() {
            History = new ArrayBlockingQueue<>(5000);
            PreviousPoly = new Polynomial(0.0f, 1.0f, 0.0f);
	}
	
	public void update(float ROITemp) throws InterruptedException{
	
	}
	
	public Polynomial getCurrentPolynomial() {
		return PreviousPoly;
	}
	
	public Polynomial getNewPolynomial() {
            MaxValueDetected = Collections.max(History);

            //Convert back to un-adjusted units,linear for now
            float tmp_MaxValueDetected = Temperature - MaxValueDetected;
		
		//Update the previous poly based on the uncorrected MaxValueDetected
	    PreviousPoly = new Polynomial(0.0f, 1.0f, tmp_MaxValueDetected + PreviousPoly.getC());
	    
	    History.clear();
	    
	    return PreviousPoly;
	}
	
	public float getTemperature(){
            return Temperature;
	}
	
	public void setTemperature(float t){
            Temperature = t;
	}
	
	public void setROIID(int id){
            this.roiID = id;
	}
	
	public int getROIID(){
            return roiID;
	}
	
	public void updateAllRelayROI(HashMap<Integer, LeptonROI> dict){
            ROI = dict.get(roiID);
	}
	
	public void setPolynomial(Polynomial poly) {
            PreviousPoly = poly;
            History.clear();
	}
	
	@Override
	public void FrameReady() {
            Float[] f = Lepton.TemperatureFrame.toArray(new Float[Lepton.Width*Lepton.Height]);
            
            ROI.update(f, Lepton.Width, Lepton.Height);
            float ROITemp = ROI.getMean();

            if(History.size() < 3600) {
                History.add(ROITemp);
            }else {
                //remove one, add one
                try {
                    History.take();
                    History.add(ROITemp);
                } catch (InterruptedException e) {
                    MaxValueDetected = 0;
                    Log.get().LogEvent(e.getMessage());
                    e.printStackTrace(System.out);
                }			
            }	
	}
	
	public String toXML(){
            return "<Blackbody temp=\"" + Temperature + "\" roi-id=\"" + roiID + "\"/>";
	}
	
	public static Blackbody fromXML(Node n) throws LeptonException, NumberFormatException{
            if(!n.hasAttributes())
                throw new LeptonException("Error in Blackbody.fromXML(...) - must be of the order <Pryometer temp=\"23.0\" roi-id=\"1\"/>");
		
            Blackbody p = new Blackbody();
            for(int i = 0; i < n.getAttributes().getLength(); i++)
            {
        	Node nn = n.getAttributes().item(i);
			Float entry;
			
			
                switch (nn.getNodeName().toLowerCase())
                {
                    case "temp":
                            entry = Float.parseFloat(nn.getNodeValue());
                            p.setTemperature(entry);
                            break;

                    case "roi-id":
                            p.setROIID(Integer.parseInt(nn.getNodeValue()));
                            break;
                }
            }
        
            return p;
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, NumberFormatException, LeptonException{
            Blackbody p = new Blackbody();
            p.setTemperature(180.0f);
            String xml = p.toXML();
            Document d = Xml.fromString(xml);

            NodeList n = d.getElementsByTagName("Blackbody");

            Blackbody q = Blackbody.fromXML(n.item(0));
            int x = 0;
	}

    @Override
    public void LeptonStatusUpdate(LEPTON_STATUS s) {

    }

    @Override
    public void StatusReady() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
