package com.tahi.IO;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

import com.tahi.Algorithms.PID;
import com.tahi.Lepton.Server.Lepton;
import com.tahi.Lepton.Server.LeptonException;
import com.tahi.Lepton.Server.LeptonROI;

public class Relay {
    public int ID;

    public int DIO;

    public String Name;
    
    protected float _DutyCycle;

    protected int _12BitDutyCycle;

    public PID PID;

    public List<Integer> ROIids;

    public List<LeptonROI> LeptonROI = new ArrayList<>();   

    float _DutyCycleAvg = 0;
    
    float _PrevDutyCycleAvg = 0;
    
    float _DutyCycleHistoryCount = 1;
    
    public Relay(){
    	
    }

    public String toXML()
    {
        String XML = "";

        XML += "<Relay";
        XML += " id=\"" + ID + "\"";
        XML += " DIO=\"" + DIO + "\"";
        XML += " Name=\"" + Name + "\"";

        if (ROIids != null)
        {
            XML += " roi=\"";
            for (int i = 0; i < ROIids.size(); i++)
            {
                if (i < ROIids.size() - 1)
                {
                    XML += ROIids.get(i) + ",";
                }
                else
                {
                    XML += ROIids.get(i);
                }
            }
            XML += "\"";
        }

        XML += " " + PID.toAttribute();
        XML += " Setpoint=\"" + PID.getSetPoint() + "\"";
        XML += "></Relay>";

        return XML;
    }

    public static Relay fromXML(Node n) throws LeptonException
    {
        if (n.hasAttributes())
        {
            int rid = -1;
            int rdio = -1;
            PID pid = null;
            List<Integer> roiID = new ArrayList<Integer>();
            String rname = "";
            float setpoint = 273.15f;
            for(int i = 0; i < n.getAttributes().getLength(); i++)
            {
            	Node nn = n.getAttributes().item(i);
            	
                switch (nn.getNodeName().toLowerCase())
                {
                    case "id":
                        rid = Integer.parseInt(nn.getNodeValue());
                        break;

                    case "dio":
                        rdio = Integer.parseInt(nn.getNodeValue());
                        break;

                    case "pid":
                        //Add PID class here
                        pid = new PID();
                        String[] pidString = nn.getNodeValue().split(",");
                        pid.setP(Float.parseFloat(pidString[0]));
                        pid.setI(Float.parseFloat(pidString[1]));
                        pid.setD(Float.parseFloat(pidString[2]));
                        break;

                    case "setpoint":
                        setpoint = Float.parseFloat(nn.getNodeValue());
                        break;

                    case "roi":
                        String[] rois = nn.getNodeValue().split(",");
                        for(String roiid : rois)
                        {
                            roiID.add(Integer.parseInt(roiid));
                        }

                        break;

                    case "name":
                        rname = nn.getNodeValue();
                        break;
                }
            }

            if (pid == null)
            {
                throw new LeptonException("ROI requires a PID attribute i.e. PID=\"1,1,0\"");
            }

            pid.setSetPoint(setpoint);
            Relay r = new Relay();
            r.ID = rid;
            r.DIO = rdio;
            r.PID = pid;
            r.ROIids = new ArrayList<Integer>(roiID);
            r.Name = rname;
            return r;
        }
        else
        {
            throw new LeptonException("Relay Node needs at least an id and a DIO attribute");
        }
    }

    public void updateROI(float[] celciusFrame)
    {

        float avgROITemp = 0;

        for(LeptonROI r : LeptonROI)
        {
            r.update(celciusFrame, Lepton.Width, Lepton.Height);
            avgROITemp += r.getMean();
        }

        avgROITemp /= LeptonROI.size();

        PID.update(avgROITemp);
        
        setDutyCycle(PID.getU()/100.0f);
    }
    
    public float getDutyCycleAvg(){
        return _DutyCycleAvg;
    }

    public void setDutyCycle(float dc)
    {
        _DutyCycle = dc;

        _DutyCycle = Math.max(0, _DutyCycle);
        _DutyCycle = Math.min(100, _DutyCycle);
        
        //From https://stackoverflow.com/questions/12636613/how-to-calculate-moving-average-without-keeping-the-count-and-data-total
        _DutyCycleAvg = _PrevDutyCycleAvg + (dc - _PrevDutyCycleAvg)/_DutyCycleHistoryCount;
             
        _PrevDutyCycleAvg = _DutyCycleAvg;
        _DutyCycleHistoryCount += 1;

        if (Float.isNaN(dc))
        {
            _12BitDutyCycle = 0;
        } else {
            _12BitDutyCycle = (int)(4095 * dc);
        }
    }

    public void setROIs(HashMap<Integer, LeptonROI> roiDictionary)
    {
        for(int roi : ROIids)
        {
            LeptonROI lroi = roiDictionary.get(roi);
            LeptonROI.add(lroi);
        }
    }

    public int get12BitDutyCycle()
    {
        return _12BitDutyCycle;
    }
}
