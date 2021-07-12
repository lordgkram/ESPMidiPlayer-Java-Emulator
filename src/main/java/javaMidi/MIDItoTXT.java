package javaMidi;

import java.io.File;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class MIDItoTXT {

    public static String[] convert(File input, int bpm) {
        try {
            Sequence seq = MidiSystem.getSequence(input);
            Track[] tracks = seq.getTracks();
            System.out.println(tracks.length + " tracks");
            int ppq = seq.getResolution();
            String[] out = new String[tracks.length];
            for (int i = 0; i < tracks.length; i++) {
                out[i] = trackToString(tracks[i], bpm, ppq);
                System.out.println(out[i]);
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String trackToString(Track t, int bpm, int ppq) {
        String out = "";
        int lastBpm = bpm;
        int ln = 2000;
        long lt = 0;
        int letzterChanal = 0;
        for (int i = 0; i < t.size(); i++) {
            MidiEvent ev = t.get(i);
            MidiMessage msg = ev.getMessage();
            long tick = ev.getTick();
            long tlDiff = (long) ((tick - lt) * (60000f / (lastBpm * ppq)));
            int sysPause = (int) ((240000f / ((float) lastBpm)) / tlDiff);
            if (sysPause < 128)
                out += "" + sysPause + " ";
            byte[] data = msg.getMessage();
            if (data.length == 3) {
                int datan = (data[0] & 0xF0) >> 4;
                int chanal = (data[0] & 0xF);
                if(chanal != letzterChanal){
                    letzterChanal = chanal;
                    out += "k" + (chanal + 1);
                }
                if (datan == 0b1000 || datan == 0b1001) {
                    // note on off
                    if (data[1] == ln) {
                        out += "l";
                    } else {
                        out += note2str(data[1]);
                    }
                    ln = data[1];
                } else if (datan == 0b1011) {
                    // controll change
                    int controll = (data[0] & 0x7F);
                    if (controll == 123) {
                        // alloff
                        out += "s";
                    } else if (controll == 7) {
                        // volume
                        out += "v" + data[1] + "";
                    } else {
                        System.out.println("Unknown controll:" + controll);
                    }
                } else {
                    System.out.println("Unknown command:" + datan + " = " + data[1] + ", " + data[2]);
                }
            } else if (data.length == 2) {
                int datan = (data[0] & 0xF0) >> 4;
                int chanal = (data[0] & 0xF);
                if(chanal != letzterChanal){
                    letzterChanal = chanal;
                    out += "k" + (chanal + 1);
                }
                if (datan == 0b1100) {
                    // prg change
                    out += "i" + data[1] + "";
                } else {
                    System.out.println("2byte Unknown command:" + datan);
                }
            } else {
                System.out.println(data.length + " paket unknown! 1. byte: " + data[0]);
            }
            lt = tick;
        }
        return "-bpm" + bpm + " 0 " + out;
    }

    public static String note2str(int n) {
        String e = "";
        String s = "";
        switch (n % 12) {
        case 0:
            e = "c";
            s = "";
            break;
        case 1:
            e = "c";
            s = "#";
            break;
        case 2:
            e = "d";
            s = "";
            break;
        case 3:
            e = "d";
            s = "#";
            break;
        case 4:
            e = "e";
            s = "";
            break;
        case 5:
            e = "f";
            s = "";
            break;
        case 6:
            e = "f";
            s = "#";
            break;
        case 7:
            e = "g";
            s = "";
            break;
        case 8:
            e = "g";
            s = "#";
            break;
        case 9:
            e = "a";
            s = "";
            break;
        case 10:
            e = "a";
            s = "#";
            break;
        case 11:
            e = "h";
            s = "";
            break;
        }
        String str = "";
        int oktavep = (n - (n % 12)) / 12;
        if (oktavep <= 4) {
            for (int i = 0; i < 4 - oktavep; i++)
                str += "'";
            e = e.toUpperCase();
        } else {
            for (int i = 0; i < oktavep - 5; i++)
                str += "'";
        }
        return e + str + s;
    }

}
