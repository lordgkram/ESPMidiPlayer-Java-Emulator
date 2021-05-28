package javaMidi.cppconv;

import javaMidi.MIDI;

public class Main {
    public static void setup() {
        for (short i = 1; i < 17; i++) {
            MIDI.sendProgramChange((short) 0, i);
            MIDI.sendPitchBend(0, i);
        }
        Interface.setMusicStatus(true);
        Defaults.fuellePresetLieder();
        Defaults.fuellePresetInstrumente();
    }
}
