package javaMidi.cppconv;

import javaMidi.JavaMain;
import javaMidi.MIDI;
import javaMidi.ReadNumberReturn;

public class Song {
    public static void playSong(String input, int timeOutSeconds){

        if(input.startsWith("~")){
          input = input.substring(1);
          if(StrMidiUtil.isNumber(input.charAt(0))){
            ReadNumberReturn rnr = StrMidiUtil.readNumber(input);
            input = rnr.outPut;
            int num = rnr.number;
            if(num >= 0 && num < JavaMain.MENGE_PRESET_LIEDER)
              input = JavaMain.main.presetLieder[num].daten;
            else
              input = JavaMain.SONG_NOT_EXISTS;
          }
        }
    
        System.out.println("PLAY ERKANNT");
    
        JavaMain.main.currentChannel = JavaMain.DEFALT_MIDI_CHANNEL;
    
        while (input.startsWith(" "))
          input = input.substring(1);
    
        if(input.startsWith("-")) {
        if(JavaMain.ALLOW_PARSER_2){
          JavaMain.main.parserV2 = true;
                }else{
                    JavaMain.main.parserV2 = false;
                    }
          input = input.substring(1);
        } else {
            JavaMain.main.parserV2 = false;
        }
    
        while (input.startsWith(" "))
          input = input.substring(1);
    
        if(input.startsWith("bpm")){
          input = input.substring(3);
          if(StrMidiUtil.isNumber(input.charAt(0))){
            ReadNumberReturn rnr = StrMidiUtil.readNumber(input);
            input = rnr.outPut;
            JavaMain.main.bpm = rnr.number;
          }else{
            JavaMain.main.bpm = JavaMain.DEFALT_BPM;
          }
        }else{
            JavaMain.main.bpm = JavaMain.DEFALT_BPM;
        }
    
        while (input.startsWith(" "))
          input = input.substring(1);
        
        input = SongUtil.readInstrument(input);
    
        JavaMain.main.vierBeatZeit = (int) ((float) (240000) / ((float) JavaMain.main.bpm));
    
        while (input.startsWith(" "))
          input = input.substring(1);
    
        input = SongUtil.expandLoops(input);
    
        JavaMain.delay(250);
    
        JavaMain.main.timeout = System.currentTimeMillis() + timeOutSeconds * 1000;
    
        Interface.setMusicStatus(false);
    
        for(String s : input.split(" ")){
            if(System.currentTimeMillis() < JavaMain.main.timeout){
                SongUtil.parserT(s);
            }
        }
        
        MidiInterface.parser2allOFF();
    
        for(short i = 1; i < 17; i++){
           MIDI.sendProgramChange((short) 0,i);
           MIDI.sendControlChange((short) 7, (short) 127, i);
           MIDI.sendControlChange((short) 0, (short) 0, i);
           MIDI.sendControlChange((short) 32, (short) 0, i);
           MIDI.sendControlChange((short) 72, (short) 63, i);
           MIDI.sendControlChange((short) 73, (short) 63, i);
        }
        Interface.setMusicStatus(true);
    }
}
