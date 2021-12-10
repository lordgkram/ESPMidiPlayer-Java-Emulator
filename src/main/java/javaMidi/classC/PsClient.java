package javaMidi.classC;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javaMidi.JavaMain;

public class PsClient {

    public static void publish (String topic, String data){
        if(JavaMain.mqttOut){
            MqttMessage msg = new MqttMessage();
            try{
                msg.setPayload(data.getBytes("utf-8"));
                JavaMain.main.client.publish(topic, msg);
            }catch (MqttException | UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }else{
            if (JavaMain.main.window == null){
                System.out.printf("--> %s: %s%n", topic, data);
                return;
            }
            JavaMain.main.window.chat.setText(JavaMain.main.window.chat.getText() + topic + ": " + data + "\n");
        }
    }
    
}
