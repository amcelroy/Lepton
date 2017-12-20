/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tahi.Lepton.Server;

import com.pi4j.wiringpi.Spi;
import com.tahi.Logging.Log;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.commons.lang3.ArrayUtils;


/**
 *
 * @author amcelroy
 */
public class LeptonSPI implements Runnable, LeptonServerListener {    
    boolean noError = true;

    boolean _Pause = false;
    
    boolean m_Rebooting = false;
    
    boolean m_FFC = false;
    
    volatile ArrayBlockingQueue<Byte[]> _PacketQueue;
    
    ArrayBlockingQueue<Byte[]> getSPIQueue(){
        return _PacketQueue;
    }
    
    public LeptonSPI(){
        //Init SPI
        float SPISpeed = (int) 4e6;
        int SPIVideo = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, (int)SPISpeed, Spi.MODE_3);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Log.get().LogEvent(ex.getLocalizedMessage());
        }
        
        _PacketQueue = new ArrayBlockingQueue<>(1000);
    }
    
    @Override
    public void run() {        
        int packetsWithoutFrame = 0;
        while(noError){
//            try {
//                Thread.sleep(15);
//            } catch (InterruptedException ex) {
//                Log.get().LogEvent(ex.getLocalizedMessage());
//            }
            
            try{
                //Allocate new Packet
                byte[] packet = new byte[Lepton.PacketSize*Lepton.Height*(Lepton.Height/Lepton.PacketHeight)];
                //Fetch new packet from Lepton
                int error = Spi.wiringPiSPIDataRW(0, packet);

                //Check for -1 Error - See WiringPi library for more details.
                if(error == -1){
                    //noError = false;
                }

                int packetCount = packet.length / Lepton.PacketSize;
                
                //Loop through packets searching for valid packets
                for(int j = 0; j < packetCount; j++){            	
                    int frameCount = (int)packet[j*Lepton.PacketSize + 1] & 0xFF;// + (packet[PacketSize * k + 2];
                    int packetCheck = (int)packet[j*Lepton.PacketSize] & 0xFF;
                    
                    if((packetCheck & 0x0F) == 0x0F){
                        
                    }else{                    
                        //If packet is valid, send to consumer queue
                        if(frameCount < Lepton.PacketHeight){
                            try{
                                packetsWithoutFrame = 0;
                                int start = j*Lepton.PacketSize;
                                int end = start + Lepton.PacketSize;
                                byte[] row = ArrayUtils.subarray(packet, start, end);
                                Byte[] rowObj = ArrayUtils.toObject(row);
                                if(m_Rebooting == false && m_FFC == false){
                                    _PacketQueue.add(rowObj);
                                }else{
                                    _PacketQueue.add(null);
                                }
                            }catch(IllegalStateException e){
                                _PacketQueue.clear();
                            }catch(Exception e){
                                Log.get().LogEvent(e.getLocalizedMessage());
                            }
                        }
                    }
                }    
            }catch(Exception e){
                Log.get().LogEvent(e.getMessage());
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
