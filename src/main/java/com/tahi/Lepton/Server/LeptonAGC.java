package com.tahi.Lepton.Server;

public class LeptonAGC {
	boolean _enabled;

    int[] agcROI;

    int maxBinSizeCount;

    int minBinSizeCount;

    int dampening;

    int emptybin;

    public LeptonAGC()
    {

    }

    public void setMaxBinSize(int size)
    {
        maxBinSizeCount = size;
    }

    public void setMinBinSize(int size)
    {
        minBinSizeCount = size;
    }

    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }

    public boolean isEnabled()
    {
        return _enabled;
    }

    public void setBinSizes(int min, int max)
    {
        maxBinSizeCount = max;
        minBinSizeCount = min;
    }

    public int getMaxBinSize()
    {
        return maxBinSizeCount;
    }

    public int getMinBinSize()
    {
        return minBinSizeCount;
    }

    public void setDampening(int damp)
    {
        dampening = damp;
    }

    public int getDampening()
    {
        return dampening;
    }

    public void setEmptyBins(int empty)
    {
        emptybin = empty;
    }

    public void setROI(int[] x1y1x2y2)
    {
        agcROI = x1y1x2y2;
    }

    public int[] getROI()
    {
        return agcROI;
    }
}
