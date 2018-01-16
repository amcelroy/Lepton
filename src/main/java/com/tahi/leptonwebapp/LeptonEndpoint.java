package com.tahi.leptonwebapp;

import com.tahi.Lepton.Server.Lepton;
import com.tahi.Lepton.Server.LeptonStatusListener;
import com.tahi.Lepton.Server.LeptonServer;
import com.tahi.Lepton.Server.ServerMessageInterface;
import com.tahi.Logging.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.simple.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author amcelroy
 */

@ApplicationScoped
@ServerEndpoint("/")
public class LeptonEndpoint implements LeptonStatusListener {
    static List<Session> _Sessions = new ArrayList<>();
    
    JSONObject m_ServerSettings;
    
    static boolean sendConfig = false;
    
    static ServerMessageInterface _SendMessagesTo;
    
    public LeptonEndpoint(){
        
    }
    
    public void registerMessageInterface(ServerMessageInterface i){
        _SendMessagesTo = i;
    }
      
    @OnOpen
    public void onOpen(Session s) throws IOException {
        _Sessions.add(s);
        sendConfig = true;
    }

    @OnClose
    public void onClose(Session s) throws IOException {
        _Sessions.remove(s);
        s.close();
    }

    @OnError
    public void onError(Throwable t) {
    }

    @OnMessage
    public void onMessage(String text, Session s) {
        _SendMessagesTo.onMessage(text);
    }
    
    @OnMessage
    public void onMessageBinary(byte[] b, Session s){
        
    }   
    
    public void sendMessage(JSONObject o){
        try{
            for(Session ses : _Sessions){
                try {
                    ses.getBasicRemote().sendText(o.toJSONString());
                } catch (IOException ex) {
                    Log.get().LogEvent(ex.getMessage());
                    //Logger.getLogger(LeptonEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }catch(Exception e){
            _Sessions.clear();
            Log.get().LogEvent("Error sending message, clearing Sessions - " + e.getMessage());
        }
    }
    
    public void sendSettings(JSONObject o) throws IOException{
        m_ServerSettings = o;
        for(Session ses : _Sessions){
            //send server settings
            ses.getBasicRemote().sendText(m_ServerSettings.toJSONString());
        }
    }

    @Override
    public void FrameReady() {
        
        if(sendConfig){
            JSONObject o = LeptonServer.getJSONConfig();
            sendMessage(o);
            sendConfig = false;
        }
        
        float[] f = Lepton.getFrame();

        ByteBuffer bb  = ByteBuffer.allocate(f.length*4);
        bb.asFloatBuffer().put(f);
        bb.rewind();
        
        for(Session ses : _Sessions){
            try {
                //send server settings
                ses.getBasicRemote().sendBinary(bb);
            } catch (IOException ex) {
                Log.get().LogEvent(ex.getMessage());
                //Logger.getLogger(LeptonEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        bb.clear();
    }

    @Override
    public void LeptonStatusUpdate(LEPTON_STATUS s) {
        
    }

    @Override
    public void StatusReady() {
        JSONObject s = Lepton.getJSONStatus();
        for(Session ses : _Sessions){
            try {
                //send server settings
                ses.getBasicRemote().sendText(s.toJSONString());
            } catch (IOException ex) {
                Log.get().LogEvent(ex.getMessage());
                //Logger.getLogger(LeptonEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
