/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tahi.Lepton.Server;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.wiringpi.Spi;
import com.tahi.Logging.Log;
import java.io.IOException;
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
    
    int m_SPIVideo;
    
    SpiDevice m_SPI;
    
    ArrayBlockingQueue<QueueObject> _PacketQueue;
    
    ArrayBlockingQueue<QueueObject> getSPIQueue(){
        return _PacketQueue;
    }
    
    public LeptonSPI(){
        //Init SPI
        int SPISpeed = (int) 4e6;

        m_SPIVideo = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, (int)SPISpeed, Spi.MODE_3);
        try {
//            m_SPI = SpiFactory.getInstance(SpiChannel.CS0,
//                SPISpeed, // default spi speed 1 MHz
//                SpiMode.MODE_3); // default spi mode 0
            
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Log.get().LogEvent(ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            Log.get().LogEvent(ex.getLocalizedMessage());
        }
        
        _PacketQueue = new ArrayBlockingQueue<>(1000);
    }
        
    byte[] m_Packet = new byte[Lepton.Height*Lepton.PacketSize];
    
    @Override
    public void run() {        
        //Allocate new Packet
        //*(Lepton.Height/Lepton.PacketHeight)];
        
        while(noError){
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                Log.get().LogEvent(ex.getLocalizedMessage());
            }
            
            try{
                //Fetch new packet from Lepton
                int error = Spi.wiringPiSPIDataRW(m_SPIVideo, m_Packet);
                //m_Packet = m_SPI.write(m_Packet);
                
//                //Check for -1 Error - See WiringPi library for more details.
//                if(error == -1){
//                    //noError = false;
//                }

                //Loop through packets searching for valid packets
                for(int j = 0; j < Lepton.Height; j++){   
                    int frameCount = (int)m_Packet[j*Lepton.PacketSize + 1] & 0xFF;// + (packet[PacketSize * k + 2];
                    int crc_pack = (((int)m_Packet[j*Lepton.PacketSize + 2] & 0xFF) << 8) + ((int)m_Packet[j*Lepton.PacketSize + 3] & 0xFF);
                                   
                    //If packet is valid, send to consumer queue
                    if(frameCount < Lepton.PacketHeight && crc_pack != 0){
                        try{
                            int start = j*Lepton.PacketSize;
                            int end = start + Lepton.PacketSize;
                            if(m_Rebooting == false && m_FFC == false ){
                                _PacketQueue.add(new QueueObject(ArrayUtils.subarray(m_Packet, start, end)));
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
