package javaMidi.cppconv;

import javaMidi.JavaMain;
import javaMidi.MIDI;

public class MidiInterface {
  public static void parser2note(short note) {
    JavaMain.main.zuletztGenannteNote = note;
    if (((JavaMain.main.activeNotes[note] >> JavaMain.main.currentChannel) & 1) != 1) {
      // start note
      JavaMain.main.activeNotes[note] |= (1 << JavaMain.main.currentChannel);
      MIDI.sendNoteOn(note, (short) 127, JavaMain.main.currentChannel);
    } else {
      // stop note
      JavaMain.main.activeNotes[note] &= ~(1 << JavaMain.main.currentChannel);
      MIDI.sendNoteOff(note, (short) 0, JavaMain.main.currentChannel);
    }
  }

  public static void parser2allOFF() {
    JavaMain.main.zuletztGenannteNote = 2000;
    for (short i = 0; i < 129; i++) {
      if (JavaMain.main.activeNotes[i] != 0) {
        for (short j = 0; j < 32; j++) {
          if (((JavaMain.main.activeNotes[i] >> j) & 1) == 1) {
            MIDI.sendNoteOff(i, (short) 0, j);
          }
        }
        JavaMain.main.activeNotes[i] = 0;
      }
    }
  }

  public static void playNote(short note, int length) {
    MIDI.sendNoteOn(note, (short) 127, JavaMain.main.currentChannel);
    JavaMain.delay(JavaMain.main.vierBeatZeit / length);
    MIDI.sendNoteOff(note, (short) 0, JavaMain.main.currentChannel);
  }

  public static void playNote(int note, int length) {
    playNote((short) note, length);
  }
}
