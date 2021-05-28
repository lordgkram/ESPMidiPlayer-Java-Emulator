package javaMidi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class MIDI {

	public static Synthesizer synth;
	private static int[] volume;
	private static float volumeF;

	public static void init() throws MidiUnavailableException, InvalidMidiDataException, IOException {
		synth = MidiSystem.getSynthesizer();
		Soundbank sb = MidiSystem.getSoundbank(new File("sf.sf2"));
		synth.open();
		synth.loadAllInstruments(sb);
		volume = new int[16];
		for (int i = 0; i < 16; i++) {
			volume[i] = 127;
		}
		volumeF = 1;
	}

	public static void setVolume(float vol) {
		volumeF = vol;
		for (int i = 0; i < 16; i++) {
			synth.getChannels()[i].controlChange(7, (int) (volume[i] * volumeF));
		}
	}

	public static void sendControlChange(short cn, short cv, short chanal) {
		if (chanal < 1 || chanal > 16)
			return;
		if (cn == 7) {
			synth.getChannels()[chanal - 1].controlChange(cn, (int) (cv * volumeF));
			volume[chanal - 1] = cv;
			return;
		}
		synth.getChannels()[chanal - 1].controlChange(cn, cv);
	}

	public static void sendProgramChange(short Instrument, short chanal) {
		if (chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].programChange(Instrument);
	}

	public static void sendNoteOn(short note, short velocity, short chanal) {
		if (chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].noteOn(note, velocity);
	}

	public static void sendNoteOff(short note, short velocity, short chanal) {
		if (chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].noteOff(note, velocity);
	}

	public static void sendPitchBend(int val, short chanal) {
		if (chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].setPitchBend(val + 8192);
	}

}
