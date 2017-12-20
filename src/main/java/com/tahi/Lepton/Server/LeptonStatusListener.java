package com.tahi.Lepton.Server;

public interface LeptonStatusListener {
	public enum LEPTON_STATUS { SPI_BAD_FRAME, NORMAL, BUSY, HARD_REBOOT,FFC_IN_PROGRESS, FFC_DONE, FFC_IMMENENT };
	
        public void LeptonStatusUpdate(LEPTON_STATUS s);
	
        public void FrameReady();  
        
        public void StatusReady();
}
