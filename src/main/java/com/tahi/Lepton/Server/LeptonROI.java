package com.tahi.Lepton.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

import com.tahi.Logging.Log;

public class LeptonROI {
    int ID;

    float UpperTemp = Float.MAX_VALUE;
    float LowerTemp = Float.MIN_VALUE;

    List<Point> _PointList;

    volatile float Mean;

    volatile float Std;
    
    volatile float Max;
    
    volatile float UnfilteredMean;
    
    public LeptonROI()
    {
        _PointList = new ArrayList<>();

        Mean = 0;
        Std = 0;
    }

    public int getID() {
        return ID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public float getMean() {
        return Mean;
    }

    public float getUnfilteredMean() {
        return UnfilteredMean;
    }

    public float getStd() {
        return Std;
    }

    public float getUpperTemp()
    {
        return UpperTemp;
    }

    public float getLowerTemp()
    {
        return LowerTemp;
    }

    public void setBounds(float UpperTemp, float LowerTemp)
    {
        this.UpperTemp = UpperTemp;
        this.LowerTemp = LowerTemp;
    }

    public void addPoint(Point p)
    {
        _PointList.add(p);
    }

    public Point upperLeftPoint()
    {
        return _PointList.get(0);
    }

    public Point lowerRightPoint()
    {
        return _PointList.get(1);
    }

    public void setPoints(List<Point> X1Y1X2Y2)
    {
        _PointList = X1Y1X2Y2;
    }
    
    public float getMax() {
    	return Max;
    }

    public String toXML()
    {
        String XML = "<ROI";
        XML += " id=\"" + ID + "\"";
        XML += " TempBounds=\"" + LowerTemp + "," + UpperTemp + "\"";
        XML += ">" + upperLeftPoint().X + "," + upperLeftPoint().Y + "," + lowerRightPoint().X + "," + lowerRightPoint().Y;
        XML += "</ROI>";
        return XML;
    }

    public static LeptonROI fromXML(Node n) throws LeptonException
    {
        int id = -1;
        float upper = Float.MAX_VALUE;
        float lower = Float.MIN_VALUE;
        //Fetch node atributes
        if (n.hasAttributes())
        {
            for(int i = 0; i < n.getAttributes().getLength(); i++)
            {
            	Node nn = n.getAttributes().item(i);
                switch (nn.getNodeName().toLowerCase())
                {

                    case "id":
                        id = Integer.parseInt(nn.getNodeValue());
                        break;

                    case "tempbounds":
                        String[] temps = nn.getNodeValue().split(",");
                        if (temps.length != 2)
                        {
                            Log.get().LogEvent("TempBounds attribute needs 2 values only, lower, upper");
                        }
                        float first;
                        float second;

                        first = Float.parseFloat(temps[0]);
                        second = Float.parseFloat(temps[1]);

                        upper = Math.max(first, second);
                        lower = Math.min(first, second);
                        break;
                }
            }
        }
        else
        {
            throw new LeptonException("ROI element has no attributes! Must have at least an id");
        }

        LeptonROI roi;
        roi = new LeptonROI();
        roi.setID(id);
        roi.setBounds(upper, lower);        

        String contents = n.getTextContent();
        String[] xy = contents.split(",");
        if (xy.length != 4)
        {
            throw new LeptonException("Error parsing ROI: more than 2 points given, should be a rectangle defined by x1,y1,x2,y2");
        }
        else
        {
            for (int z = 0; z < xy.length; z += 2)
            {
                try
                {
                    Point p = new Point();
                    p.X = Integer.parseInt(xy[z]);
                    p.Y = Integer.parseInt(xy[z + 1]);
                    roi.addPoint(p);
                }
                catch (NumberFormatException fe)
                {
                    throw new LeptonException(fe.getMessage());
                }
            }
        }

        return roi;
    }

    public void update(Float[] frame, int Width, int Height)
    {
        if (frame == null)
        {
            return;
        }
        int StartX = upperLeftPoint().X;
        int StartY = upperLeftPoint().Y;
        int EndX = lowerRightPoint().X;
        int EndY = lowerRightPoint().Y;

        List<Float> roi = new ArrayList<>(Math.abs(StartX - EndX)*Math.abs(EndY - StartY));
        
        float l_mean = 0;
        float l_unfilteredMean = 0;
        float l_std = 0;
        float l_max;
        
        for (int y = StartY; y < EndY; y++)
        {
            for (int x = StartX; x < EndX; x++)
            {
                float a = frame[y * Width + x];
                l_unfilteredMean += a;
                roi.add(a);
                a = Math.max(LowerTemp, a);
                a = Math.min(UpperTemp, a);
                l_mean += a;
            }
        }

        l_mean = l_mean / roi.size();

        //Mean = Math.max(LowerTemp, Mean);
        //Mean = Math.min(UpperTemp, Mean);

        l_unfilteredMean = l_unfilteredMean / roi.size();
        
        l_max = Collections.max(roi);

        for(Float f : roi)
        {
            l_std += Math.pow((f - Mean), 2);
        }

        l_std = l_std / roi.size();
        l_std = (float)Math.sqrt(l_std);   
        
        Mean = l_mean;
        UnfilteredMean = l_unfilteredMean;
        Std = l_std;
        Max = l_max;
    }
}
