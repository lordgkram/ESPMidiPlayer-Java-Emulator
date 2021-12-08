package javaMidi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
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

	public static void sendControlChange(short cn, short cv, short channel) {
		if (channel < 1 || channel > 16)
			return;
		if(JavaMain.main.txtToMidi.recording){
			try{
				JavaMain.main.txtToMidi.track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel - 1, cn, cv), JavaMain.main.txtToMidi.currentTime));
			}catch(InvalidMidiDataException e){}
			return;
		}
		if (cn == 7) {
			synth.getChannels()[channel - 1].controlChange(cn, (int) (cv * volumeF));
			volume[channel - 1] = cv;
			return;
		}
		synth.getChannels()[channel - 1].controlChange(cn, cv);
	}

	public static void sendProgramChange(short Instrument, short channel) {
		if (channel < 1 || channel > 16)
			return;
		if(JavaMain.main.txtToMidi.recording){
			try{
				JavaMain.main.txtToMidi.track.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel - 1, Instrument, 0), JavaMain.main.txtToMidi.currentTime));
			}catch(InvalidMidiDataException e){}
			return;
		}
		synth.getChannels()[channel - 1].programChange(Instrument);
	}

	public static void sendNoteOn(short note, short velocity, short channel) {
		if(JavaMain.main.window.shoudStop)
			return;
		if(JavaMain.main.txtToMidi.recording){
			try{
				JavaMain.main.txtToMidi.track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel - 1, note, velocity), JavaMain.main.txtToMidi.currentTime));
			}catch(InvalidMidiDataException e){}
			return;
		}
		if (channel < 1 || channel > 16)
			return;
		synth.getChannels()[channel - 1].noteOn(note, velocity);
	}

	public static void sendNoteOff(short note, short velocity, short channel) {
		if (channel < 1 || channel > 16)
			return;
		if(JavaMain.main.txtToMidi.recording){
			try{
				JavaMain.main.txtToMidi.track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, channel - 1, note, velocity), JavaMain.main.txtToMidi.currentTime));
			}catch(InvalidMidiDataException e){}
			return;
		}
		synth.getChannels()[channel - 1].noteOff(note, velocity);
	}

	public static void sendPitchBend(int val, short channel) {
		if (channel < 1 || channel > 16)
			return;
		if(JavaMain.main.txtToMidi.recording){
			try{
				int d = val + 8192;
				JavaMain.main.txtToMidi.track.add(new MidiEvent(new ShortMessage(ShortMessage.PITCH_BEND, channel - 1, (d & 0xFF), (d & 0xFF00) >> 8), JavaMain.main.txtToMidi.currentTime));
			}catch(InvalidMidiDataException e){}
			return;
		}
		synth.getChannels()[channel - 1].setPitchBend(val + 8192);
	}

}
