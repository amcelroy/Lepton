/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tahi.Lepton.Server;

/**
 *
 * @author amcelroy
 */
public interface LeptonListener {
    
    public void rebooting();
    
    public void runningFFC();
    
    public void rebootFinished();
    
    public void finishedFFC();
    
}