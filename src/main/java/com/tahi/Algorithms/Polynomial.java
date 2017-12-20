package com.tahi.Algorithms;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tahi.Lepton.Server.LeptonException;
import com.tahi.Logging.Xml;

public class Polynomial {
    public Float a;
    public Float b;
    public Float c;
    
    public Polynomial(){
    	
    }
    
    public Polynomial(Float a, Float b, Float c){
    	this.a = a;
    	this.b = b;
    	this.c = c;
    }

    public float y(float x)
    {
        return a * x * x + b * x + c;
    }
    
    public float yUShort(short u)
    {
    	float x = u & 0xFFFF;
        return a * x * x + b * x + c;
    }

    public Float[] y(Float[] x)
    {
    	Arrays.setAll(x, i -> i * a * a + i * b + c);
        return x;
    }

	public Float getA() {
		return a;
	}

	public void setA(Float a) {
		this.a = a;
	}

	public Float getB() {
		return b;
	}

	public void setB(Float b) {
		this.b = b;
	}

	public Float getC() {
		return c;
	}

	public void setC(Float c) {
		this.c = c;
	}
	
	public String toXML(){
		return "<Temperature a=\"" + a + "\" " + "b=\"" + b + "\" " + "c=\"" + c + "\"" + "></Temperature>";
	}
	
	public static Polynomial fromXML(Node n) throws LeptonException, NumberFormatException{
		if(!n.hasAttributes())
			throw new LeptonException("Error in Polynomial.fromXML(...) - Improper format, must be of the order <Temperature a=\"0\" b=\"0\" c=\"0\"/>");			

		
		Polynomial p = new Polynomial();
        for(int i = 0; i < n.getAttributes().getLength(); i++)
        {
        	Node nn = n.getAttributes().item(i);
			Float entry;
			
            switch (nn.getNodeName().toLowerCase())
            {
			case "a":
				entry = Float.parseFloat(nn.getNodeValue());
				p.setA(entry);
				break;
				
			case "b":
				entry = Float.parseFloat(nn.getNodeValue());
				p.setB(entry);
				break;
				
			case "c":
				entry = Float.parseFloat(nn.getNodeValue());
				p.setC(entry);
				break;
			default:
				throw new LeptonException("Error in Polynomial.fromXML(...) - only polynomial entries a,b, and c are currently supported");
            }
        }
		
		return p;
	}
	
	public static void main(String[] args) throws NumberFormatException, LeptonException, SAXException, IOException, ParserConfigurationException{
		Polynomial p = new Polynomial(1.0f, 2.0f, 3.0f);
		String s = p.toXML();		
		Document d = Xml.fromString(s);
		
        NodeList temperature = d.getElementsByTagName("Temperature");
		
		Polynomial q = Polynomial.fromXML(temperature.item(0));
		int z = 0;
	}
}
