package com.tahi.Lepton.Server;

public class LeptonData {
    public int Low;
    public int High;

    public LeptonData(int High, int Low)
    {
        this.High = (byte)(High & 0x000000FF);
        this.Low = (byte)(Low & 0x000000FF);
    }
    
    public LeptonData(int i){
    	this.High = (i & 0x0000FF00) >> 8;
    	this.Low = (i & 0x000000FF);
    }

    public int[] asIntArray(){
    	int[] buff = new int[2];
    	
    	buff[0] = this.High;
    	buff[1] = this.Low;
    	
    	return buff;
    }
    
    public int getLow() {
		return Low;
	}

	public void setLow(int low) {
		Low = low;
	}

	public int getHigh() {
		return High;
	}

	public void setHigh(byte high) {
		High = high;
	}

    public int asInt()
    {
        int x = (int)((int)High << 8);
        x += (int)Low;
        return x;
    }
}
