/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tahi.Lepton.Server;

import com.pi4j.io.spi.SpiDevice;
import com.pi4j.wiringpi.Spi;
import com.tahi.Logging.Log;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author amcelroy
 */
public class LeptonSPI implements Runnable, LeptonListener {    
    boolean noError = true;

    boolean _Pause = false;
    
    boolean m_Rebooting = false;
    
    boolean m_FFC = false;
    
    int m_SPIVideo;
    
    SpiDevice m_SPI;
    
    boolean m_Pause = false;
    
    ArrayBlockingQueue<QueueObject> _PacketQueue;
    
    ArrayBlockingQueue<QueueObject> getSPIQueue(){
        return _PacketQueue;
    }
    
    public void pause(){
        m_Pause = true;
    }
    
    public void play(){
        m_Pause = false;
    }
    
    public LeptonSPI(){
        //Init SPI
        int SPISpeed = (int) 4e6;

        m_SPIVideo = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, (int)SPISpeed, Spi.MODE_3);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Log.get().LogEvent(ex.getLocalizedMessage());
        }
        
        _PacketQueue = new ArrayBlockingQueue<>(2000, true);
    }
        
    byte[] m_Packet = new byte[Lepton.Height*Lepton.PacketSize];
    
    @Override
    public void run() {        
        //Allocate new Packet
        //*(Lepton.Height/Lepton.PacketHeight)];
        
        while(noError){
            
            if(m_Pause){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Log.get().LogEvent(ex.getLocalizedMessage());
                }
            }else{            
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Log.get().LogEvent(ex.getLocalizedMessage());
                }

                try{
                    //Fetch new packet from Lepton
                    int error = Spi.wiringPiSPIDataRW(m_SPIVideo, m_Packet);

                    if(error < 0){
                        int SPISpeed = (int) 4e6;
                        m_SPIVideo = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, (int)SPISpeed, Spi.MODE_3);
                        Thread.sleep(2000);
                        Log.get().LogEvent("Severe error - SPI could not be read, error -1, stopping thread...");
                    }

                    //Loop through packets searching for valid packets
                    for(int j = 0; j < Lepton.Height; j++){   
                        int frameCount = (int)m_Packet[j*Lepton.PacketSize + 1] & 0xFF;// + (packet[PacketSize * k + 2];
                        int crc_pack = (((int)m_Packet[j*Lepton.PacketSize + 2] & 0xFF) << 8) + ((int)m_Packet[j*Lepton.PacketSize + 3] & 0xFF);

                        //If packet is valid, send to consumer queue
                        if(frameCount < Lepton.PacketHeight && crc_pack != 0){
                            try{
                                int start = j*Lepton.PacketSize;
                                int end = start + Lepton.PacketSize;
                                if(m_Rebooting == false && m_FFC == false){
                                    byte[] tmp = new byte[Lepton.PacketSize];
                                    System.arraycopy(m_Packet, start, tmp, 0, Lepton.PacketSize);
                                    _PacketQueue.add(new QueueObject(tmp));
                                }
                            }catch(IllegalStateException e){
                                _PacketQueue.clear();
                            }catch(Exception e){
                                Log.get().LogEvent(e.getLocalizedMessage());
                            }
                        }
                    }    
                }catch(Exception e){
                    Log.get().LogEvent(e.getMessage());
                }
            }
        }
    }

    @Override
    public void rebooting() {
        m_Rebooting = true;
    }

    @Override
    public void runningFFC() {
        m_FFC = true;
    }

    @Override
    public void rebootFinished() {
        m_Rebooting = false;
    }

    @Override
    public void finishedFFC() {
        m_FFC = false;
    }
    
}
