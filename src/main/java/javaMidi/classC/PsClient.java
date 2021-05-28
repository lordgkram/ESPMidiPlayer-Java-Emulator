package javaMidi.classC;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javaMidi.JavaMain;

public class PsClient {

    public static void publish (String t, String d){
        if(JavaMain.main.mqtt){
            MqttMessage msg = new MqttMessage();
            try{
                msg.setPayload(d.getBytes("utf-8"));
                JavaMain.main.client.publish(t, msg);
            }catch (MqttException | UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }else{
            JavaMain.main.window.chat.setText(JavaMain.main.window.chat.getText() + t + ": " + d + "\n");
        }
    }
    
}
