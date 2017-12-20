package com.tahi.Lepton.Server;

public class LeptonException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7300214109564811528L;
	
	public LeptonException(String msg){
		super(msg);
		System.out.println(msg);
	}
}
