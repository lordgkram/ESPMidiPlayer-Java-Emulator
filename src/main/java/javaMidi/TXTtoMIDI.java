package javaMidi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import com.google.gson.JsonObject;

import javaMidi.cppconv.Interface;

public class TXTtoMIDI {

    public long currentTime = 0;
    public boolean recording = false;

    public Track track;

    public Sequence record(String data, long lineTimeout, boolean buffer){
        if(recording)
            return null;
        recording = true;
        try{
            Sequence q = new Sequence(Sequence.PPQ, 500, 1);
            currentTime = 0;
            track = q.getTracks()[0];
            //record
            String[] toPlay = data.split("\n");
			JsonObject o = new JsonObject();
			o.addProperty("aktiviereBuffer", buffer);
			o.addProperty("laenge", lineTimeout);
			o.addProperty("maximaleBufferGroesse", data.length());
			o.addProperty("prioritaet", 0);
			o.addProperty("nutzer", "TXTtoMIDI");
			o.addProperty("adminModus", false);
            for (int i = 0; i < toPlay.length; i++) {
                String t = toPlay[i];
                try {
                    o.addProperty("midi", t);
                    Interface.mqttCallback(JavaMain.TOPIC_MIDI, JavaMain.main.gson.toJson(o));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //stop recording
            recording = false;
            return q;
        }catch(InvalidMidiDataException e){
            recording = false;
            return null;
        }
    }
    
}
