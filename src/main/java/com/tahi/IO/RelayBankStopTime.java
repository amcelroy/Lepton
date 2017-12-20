package com.tahi.IO;

import java.time.Duration;
import java.time.LocalDateTime;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RelayBankStopTime {

	LocalDateTime StopTime;
	
	public RelayBankStopTime(LocalDateTime stopTime){
		StopTime = stopTime;
	}
	
	public LocalDateTime getStopTime(){
		return StopTime;
	}
	
	public String toXML(){
		String xml = "";
		
		Duration d = Duration.between(LocalDateTime.now(), StopTime);
		long totalHours = d.toHours();
		long totalMinutes = d.toMinutes();
		
		double fractionalMinutes = (double)totalMinutes/60 - (double)totalHours;
		fractionalMinutes *= 60;
		
		xml = "<Duration Hours=\"" + totalHours + "\" Minutes=\"" + Math.floor(fractionalMinutes) + "\"/>";		
		
		return xml;
	}
	
	public static RelayBankStopTime fromXML(Node n) throws NumberFormatException{		
		int hours = 0;
		int minutes = 0;
		
		NamedNodeMap nnm = n.getAttributes();
		for(int i = 0; i < nnm.getLength(); i++){
			Node nn = nnm.item(i);
			
			switch(nn.getNodeName().toLowerCase()){
			case "hours":
				hours = (int)Double.parseDouble(nn.getNodeValue());
				break;
				
			case "minutes":
				minutes = (int)Double.parseDouble(nn.getNodeValue());
				break;
			}
		}
		
		LocalDateTime lt = LocalDateTime.now();
		lt = lt.plusHours(hours).plusMinutes(minutes);		
		RelayBankStopTime rst = new RelayBankStopTime(lt);
		
		return rst;
	}
}
