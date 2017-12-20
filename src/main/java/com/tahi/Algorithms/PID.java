package com.tahi.Algorithms;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tahi.Logging.Xml;

public class PID {
	float[] _errorHistory;
    float[] _dtHistory;
    int _historySize = 400;
    int _errorCounter = 0;

    public float Temp;

    float P;
    float I;
    float D;

    float P_Contribution;
    float I_Contribution;
    float D_Contribution;
    float U;

    private List<Float> P_Contribution_History;
    private List<Float> I_Contribution_History;// { get; set; }
    private List<Float> D_Contribution_History;// { get; set; }
    private List<Float> U_History;// { get; set; }
    private List<GregorianCalendar> Time_History;// { get; set; }

    float Error;
    float SetPoint;

    private float LastError = 0;

    private float dt = 0;

    GregorianCalendar LastCall;

    public PID()
    {
        _errorHistory = new float[_historySize];
        _dtHistory = new float[_historySize];
        LastCall = new GregorianCalendar();

        P_Contribution_History = new ArrayList<Float>();
        I_Contribution_History = new ArrayList<Float>();
        D_Contribution_History = new ArrayList<Float>();
        U_History = new ArrayList<Float>();
        Time_History = new ArrayList<GregorianCalendar>();
    }

    public float getTemp() {
		return Temp;
	}

	public void setTemp(float temp) {
		Temp = temp;
	}

	public float getP() {
		return P;
	}

	public void setP(float p) {
		P = p;
	}

	public float getI() {
		return I;
	}

	public void setI(float i) {
		I = i;
	}

	public float getD() {
		return D;
	}

	public void setD(float d) {
		D = d;
	}

	public float getP_Contribution() {
		return P_Contribution;
	}

	public void setP_Contribution(float p_Contribution) {
		P_Contribution = p_Contribution;
	}

	public float getI_Contribution() {
		return I_Contribution;
	}

	public void setI_Contribution(float i_Contribution) {
		I_Contribution = i_Contribution;
	}

	public float getD_Contribution() {
		return D_Contribution;
	}

	public void setD_Contribution(float d_Contribution) {
		D_Contribution = d_Contribution;
	}

	public float getU() {
		return U;
	}

	public void setU(float u) {
		U = u;
	}

	public float getError() {
		return Error;
	}

	public void setError(float error) {
		Error = error;
	}

	public float getSetPoint() {
		return SetPoint;
	}

	public void setSetPoint(float setPoint) {
		SetPoint = setPoint;
	}

	public String toAttribute()
    {
        return "PID=\"" + P + "," + I + "," + D + "\"";
    }

    public float integrateHistory()
    {
        float sum = 0;
        for(int i = 0; i < _errorHistory.length; i++)
        {
            sum += _errorHistory[i] * _dtHistory[i];
        }

        return sum;
    }

    public void resetPID()
    {
        for (int i = 0; i < _errorHistory.length; i++)
        {
            _errorHistory[i] = 0;
            _dtHistory[i] = 0;
        }
    }

    public String getPIDasXML() throws ParserConfigurationException, TransformerException
    {
        String xml  = "";

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = db.newDocument();

        synchronized(P_Contribution_History){
        	Element e = d.createElement("PID_History");
        	d.appendChild(e);
        	
        	e = d.createElement("P");
        	d.adoptNode(e);        	
        	Attr a = d.createAttribute("contribution");
        	a.setValue(P_Contribution_History.get(P_Contribution_History.size() - 1).toString());
        	d.appendChild(a);
        	
        	e = d.createElement("I");
        	d.adoptNode(e);
        	a = d.createAttribute("contribution");
        	a.setValue(I_Contribution_History.get(I_Contribution_History.size() - 1).toString());
        	d.appendChild(a);
        	
        	e = d.createElement("D");
        	d.adoptNode(e);
        	a = d.createAttribute("contribution");
        	a.setValue(D_Contribution_History.get(D_Contribution_History.size() - 1).toString());
        	d.appendChild(a);
        	
        	e = d.createElement("U");
        	d.adoptNode(e);
        	a = d.createAttribute("contribution");
        	a.setValue(Float.toString(U));
        	d.appendChild(a);
	
        	e = d.createElement("Error");
        	d.adoptNode(e);
        	a = d.createAttribute("contribution");
        	a.setValue(Float.toString(Error));
        	d.appendChild(a);
        	
        	e = d.createElement("Temp");
        	d.adoptNode(e);
        	a = d.createAttribute("contribution");
        	a.setValue(Float.toString(Temp));
        	d.appendChild(a);
        }

        xml = Xml.documentToString(d);
        
        return xml;
    }

    public void update(float current)
    {
        Temp = current;

        //dt = DateTime.Now.Subtract(LastCall).Milliseconds;
        dt = GregorianCalendar.getInstance().getTimeInMillis() - LastCall.getTimeInMillis();
        dt /= 1000f;

        LastError = Error;
        Error = SetPoint - current;

        _errorHistory[_errorCounter] = Error;
        _dtHistory[_errorCounter] = dt;

        LastCall = new GregorianCalendar();

        float interror = integrateHistory();

        P_Contribution = P * Error;
        D_Contribution = D * (Error - LastError) / dt;

        if (Float.isNaN(D_Contribution))
        {
            D_Contribution = 0;
        }

        I_Contribution = I * interror;
        U = P_Contribution + I_Contribution + D_Contribution;
        
        U = Math.max(U, 0);
        U = Math.min(U, 100);

        synchronized(P_Contribution_History){
	        U_History.add(U);
	        P_Contribution_History.add(P_Contribution);
	        I_Contribution_History.add(I_Contribution);
	        D_Contribution_History.add(D_Contribution);
	        Time_History.add(LastCall);
	
	        if (U_History.size() > 500)
	        {
	            U_History.subList(0, 500).clear();
	            P_Contribution_History.subList(0, 500).clear();
	            I_Contribution_History.subList(0, 500).clear();
	            D_Contribution_History.subList(0, 500).clear();
	            Time_History.subList(0, 500).clear();
	        }
        }

        if (Float.isNaN(U))
        {
            U = 0;
        }

        _errorCounter += 1;

        if(_errorCounter >= _errorHistory.length)
        {
            _errorCounter = 0;
        }
    }
}
