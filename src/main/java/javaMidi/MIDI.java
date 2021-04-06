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
	
	public static void init() throws MidiUnavailableException, InvalidMidiDataException, IOException {
		synth = MidiSystem.getSynthesizer();
		Soundbank sb = MidiSystem.getSoundbank(new File("sf.sf2"));
		synth.open();
		synth.loadAllInstruments(sb);
	}
	 
	public static void sendProgramChange(short Instrument, short chanal) {
		if(chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].programChange(Instrument);
	}
	
	public static void sendNoteOn(short note, short velocity, short chanal) {
		if(chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].noteOn(note, velocity);
	}
	
	public static void sendNoteOff(short note, short velocity, short chanal) {
		if(chanal < 1 || chanal > 16)
			return;
		synth.getChannels()[chanal - 1].noteOff(note, velocity);
	}

}
