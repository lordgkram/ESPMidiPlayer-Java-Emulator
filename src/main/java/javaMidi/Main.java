package javaMidi;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Main {
	public static final String BROKER = "tcp://192.168.178.7";
	public static final String CLIENT_ID = "MIDI";
	public static final String TOPIC_IRC_TX = "irc/tx";
	public static final String TOPIC_MIDI = "playmidi";
	public static final short DEFALT_MIDI_CHANAL = 1;
	public static final boolean ALLOW_PARSER_2 = true;
	public static final int DEFALT_BPM = 240;
	public static final int NOTEN_BUFFER_LAENGE = 8;
	public static final boolean ENABLE_PARSER_1_1 = true;
	public static final boolean ALLOW_MULTI_CHANAL_MIDI = true;

	public static short MIDI_INSTRUMENT_piano = 0;
	public static short MIDI_INSTRUMENT_vibes = 11;
	public static short MIDI_INSTRUMENT_organ = 19;
	public static short MIDI_INSTRUMENT_guitar = 30;
	public static short MIDI_INSTRUMENT_brass = 62;

	MqttClient client;
	Gson gson;
	public static Main main;

	NotenBufferEintrag notenBuffer[];
	short zuletztGenannteNote = 2000;
	short currentChanal = DEFALT_MIDI_CHANAL;
	boolean parserV2 = false;
	int bpm = DEFALT_BPM;
	int activeNotes[];
	int vierBeatZeit = 100;
	long timeout = 0;
	boolean mqtt = true;
	Window window;

	public static void main(String[] args) {
		try {
			System.out.println("loading System v0.3");
			if (args.length != 0 && args[0].equalsIgnoreCase("-mqtt"))
				main = new Main(true);
			else
				main = new Main(false);
			System.out.println("loading MIDI");
			MIDI.init();
		} catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	public Main(boolean doMqtt) {
		System.out.println("loading JSON");
		gson = new Gson();
		activeNotes = new int[129];
		notenBuffer = new NotenBufferEintrag[NOTEN_BUFFER_LAENGE];
		for (int i = 0; i < NOTEN_BUFFER_LAENGE; i++)
			notenBuffer[i] = new NotenBufferEintrag();
		if (doMqtt) {
			System.out.println("loading MQTT");
			MqttClientPersistence persistence = new MemoryPersistence();
			try {
				client = new MqttClient(BROKER, CLIENT_ID, persistence);
				MqttConnectOptions mqttOpts = new MqttConnectOptions();
				mqttOpts.setCleanSession(true);
				client.setCallback(new MqttCallback() {
					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {
						Main.main.messageArrived(topic, message);
					}

					@Override
					public void deliveryComplete(IMqttDeliveryToken token) {
					}

					@Override
					public void connectionLost(Throwable cause) {
						cause.printStackTrace();
					}
				});
				client.connect(mqttOpts);
				client.subscribe(TOPIC_MIDI);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		} else {
			window = new Window();
			mqtt = false;
		}
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.print("Message arrived [");
		System.out.print(topic);
		System.out.print("] ");
		String msg = new String(message.getPayload());
		System.out.print(msg);
		System.out.println();

		if (topic.equals(TOPIC_MIDI)) {
			System.out.println("play midi vom mqtt erkannt");
			// rueckwertz kompatiblitaet
			if (message.getPayload()[0] != '{') {
				playSong(msg, 16);
				return;
			}
			// JSON MIDI
			JsonObject data = gson.fromJson(msg, JsonObject.class);
			boolean erlaubeBuffer = false;
			if (data.has("aktiviereBuffer"))
				erlaubeBuffer = data.get("aktiviereBuffer").getAsBoolean();
			if (erlaubeBuffer) {
				String midi = data.get("midi").getAsString();
				String nutzer = data.get("nutzer").getAsString();
				// buffer funktionen
				if (midi.startsWith(";")) {
					midi = midi.substring(1);
					// loesche buffer aktion
					if (midi.startsWith("l")) {
						midi = midi.substring(1);
						boolean wurdeGeloescht = false;
						for (short i = 0; i < NOTEN_BUFFER_LAENGE; i++) {
							if (nutzer.equalsIgnoreCase(notenBuffer[i].besitzer)) {
								notenBuffer[i].besitzer = "";
								notenBuffer[i].daten = "";
								notenBuffer[i].maximaleLaenge = 0;
								notenBuffer[i].priority = 0;
								wurdeGeloescht = true;
							}
						}
						if (wurdeGeloescht) {
							schreibeChatNachricht("(MIDI) @" + nutzer + " dein Puffer wurde erfolgreich gelöscht!");
						} else {
							schreibeChatNachricht("(MIDI) @" + nutzer + " du hast keinen Puffer!");
						}
					}
					boolean benutzerBufferGefunden = false;
					short bufferID = 0;
					for (short i = 0; i < NOTEN_BUFFER_LAENGE; i++) {
						if (nutzer.equalsIgnoreCase(notenBuffer[i].besitzer)) {
							benutzerBufferGefunden = true;
							bufferID = i;
						}
					}
					if (benutzerBufferGefunden) {
						// daten zum buffer hinzu fügen
						if (notenBuffer[bufferID].daten.length() == notenBuffer[bufferID].maximaleLaenge) {
							schreibeChatNachricht("(MIDI) @" + nutzer + " dein Puffer ist Voll!");
						} else if ((notenBuffer[bufferID].daten.length() + midi.length()) > notenBuffer[bufferID].maximaleLaenge) {
							notenBuffer[bufferID].daten = notenBuffer[bufferID].daten + midi + " ";
							notenBuffer[bufferID].daten = notenBuffer[bufferID].daten.substring(0, notenBuffer[bufferID].maximaleLaenge);
							schreibeChatNachricht("(MIDI) @" + nutzer + " daten wurden zu deinem Puffer hinzugefügt. Achtung es wurden Daten entfernt da der puffer überfüllt wurde ("
									+ notenBuffer[bufferID].daten.length() + "/" + notenBuffer[bufferID].maximaleLaenge + ").");
						} else {
							notenBuffer[bufferID].daten = notenBuffer[bufferID].daten + midi + " ";
							schreibeChatNachricht(
									"(MIDI) @" + nutzer + " daten wurden zu deinem Puffer hinzugefügt (" + notenBuffer[bufferID].daten.length() + "/" + notenBuffer[bufferID].maximaleLaenge + ").");
						}
					} else {
						if (midi.startsWith("n")) {
							midi = midi.substring(1);
							short prioritaet = data.get("prioritaet").getAsShort();
							// erschaffe neuen buffer
							bufferID = 0;
							boolean erschaffeBuffer = false;
							boolean ueberSchreibeBuffer = true;
							for (short i = 0; i < NOTEN_BUFFER_LAENGE; i++) {
								if (notenBuffer[i].besitzer.equalsIgnoreCase("") && ueberSchreibeBuffer) {
									// erschaffe neuen buffer
									ueberSchreibeBuffer = false;
									erschaffeBuffer = true;
									bufferID = i;
								}
							}
							if (ueberSchreibeBuffer) {
								for (short i = 0; i < NOTEN_BUFFER_LAENGE; i++) {
									if (notenBuffer[i].priority < prioritaet && !(ueberSchreibeBuffer)) {
										// erschaffe neuen buffer
										ueberSchreibeBuffer = true;
										erschaffeBuffer = true;
										bufferID = i;
									}
								}
							}
							if (erschaffeBuffer) {
								int maximaleBufferGroesse = data.get("maximaleBufferGroesse").getAsInt();
								notenBuffer[bufferID].besitzer = nutzer;
								notenBuffer[bufferID].priority = prioritaet;
								notenBuffer[bufferID].maximaleLaenge = maximaleBufferGroesse;
								notenBuffer[bufferID].daten = midi + " ";
								if (notenBuffer[bufferID].daten.length() > notenBuffer[bufferID].maximaleLaenge) {
									notenBuffer[bufferID].daten = notenBuffer[bufferID].daten.substring(0, notenBuffer[bufferID].maximaleLaenge);
								}
								schreibeChatNachricht(
										"(MIDI) @" + nutzer + " puffer wurde erfolgreich erschaffen (" + notenBuffer[bufferID].daten.length() + "/" + notenBuffer[bufferID].maximaleLaenge + ").");
							} else {
								schreibeChatNachricht("(MIDI) @" + nutzer + " puffer konte nicht erschaffen werden.");
							}
						} else {
							// spiele daten
							playSong(midi, data.get("laenge").getAsInt());
						}
					}
				} else {
					boolean benutzerBufferGefunden = false;
					for (short i = 0; i < NOTEN_BUFFER_LAENGE; i++) {
						if (nutzer.equalsIgnoreCase(notenBuffer[i].besitzer)) {
							benutzerBufferGefunden = true;
							playSong(notenBuffer[i].daten + midi, data.get("laenge").getAsInt());
							notenBuffer[i].besitzer = "";
							notenBuffer[i].daten = "";
							notenBuffer[i].maximaleLaenge = 0;
							notenBuffer[i].priority = 0;
						}
					}
					if (!benutzerBufferGefunden)
						playSong(midi, data.get("laenge").getAsInt());
				}
			} else {
				playSong(data.get("midi").getAsString(), data.get("laenge").getAsInt());
			}
		}
	}

	public void playSong(String input, int timeOutSeconds) {
		System.out.println("PLAY ERKANNT");

		currentChanal = DEFALT_MIDI_CHANAL;

		while (input.startsWith(" "))
			input = input.substring(1);

		if (input.startsWith("-")) {
			parserV2 = ALLOW_PARSER_2;
			input = input.substring(1);
		} else {
			parserV2 = false;
		}

		while (input.startsWith(" "))
			input = input.substring(1);

		if (input.startsWith("bpm")) {
			input = input.substring(3);
			if (input.length() != 0 && isNumber(input.charAt(0))) {
				ReadNumberReturn rnr = readNumber(input);
				input = rnr.outPut;
				bpm = rnr.number;
			} else {
				bpm = DEFALT_BPM;
			}
		} else {
			bpm = DEFALT_BPM;
		}

		while (input.startsWith(" "))
			input = input.substring(1);

		if (input.length() != 0 && isNumber(input.charAt(0))) {
			ReadNumberReturn rnr = readNumber(input);
			input = rnr.outPut;
			short midiInstrument = (short) rnr.number;
			if (midiInstrument < 128)
				MIDI.sendProgramChange(midiInstrument, currentChanal);
		}

		if (input.startsWith("piano")) {
			input = input.substring(6);
			MIDI.sendProgramChange(MIDI_INSTRUMENT_piano, currentChanal);
		}

		if (input.startsWith("vibes")) {
			input = input.substring(6);
			MIDI.sendProgramChange(MIDI_INSTRUMENT_vibes, currentChanal);
		}

		if (input.startsWith("organ")) {
			input = input.substring(6);
			MIDI.sendProgramChange(MIDI_INSTRUMENT_organ, currentChanal);
		}

		if (input.startsWith("guitar")) {
			input = input.substring(7);
			MIDI.sendProgramChange(MIDI_INSTRUMENT_guitar, currentChanal);
		}

		if (input.startsWith("brass")) {
			input = input.substring(6);
			MIDI.sendProgramChange(MIDI_INSTRUMENT_brass, currentChanal);
		}

		vierBeatZeit = (int) ((float) (240000) / ((float) bpm));

		while (input.startsWith(" "))
			input = input.substring(1);
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String[] pch = input.split(" ");

		timeout = System.currentTimeMillis() + timeOutSeconds * 1000;

		for (int i = 0; i < pch.length; i++) {
			if (System.currentTimeMillis() < timeout) {
				parserT(pch[i]);
			}
		}
		parser2allOFF();

		for (short i = 0; i < 17; i++) {
			MIDI.sendProgramChange(MIDI_INSTRUMENT_piano, i);
			MIDI.sendControlChange((short) 7, (short) 127, i);
		}
	}

	public void parserT(String buffer) {
		if (buffer.length() == 0)
			return;
		if (buffer.charAt(0) == 'm' || buffer.charAt(0) == 'M') {
			buffer = buffer.substring(1);
			if (ALLOW_PARSER_2)
				parserV2 = !parserV2;
		}
		if (parserV2) {
			parser2(buffer);
		} else {
			if (ENABLE_PARSER_1_1) {
				parser1_1(buffer);
				parser2allOFF();
			} else {
				parser(buffer);
			}
		}
	}

	public void parser2(String buffer) {
		System.out.println("Parser2: " + buffer);

		if (System.currentTimeMillis() > timeout)
			return;

		if (buffer.length() == 0)
			return;

		if (isNumber(buffer.charAt(0))) {
			ReadNumberReturn rnr = readNumber(buffer);
			buffer = rnr.outPut;
			int length = rnr.number;
			if (length == 0)
				length = 4;
			try {
				Thread.sleep(vierBeatZeit / length);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (buffer.length() != 0 && buffer.charAt(0) == '.') {
				try {
					Thread.sleep((vierBeatZeit / length) / 2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				buffer = buffer.substring(1);
			}
			if (buffer.length() != 0)
				parser2(buffer);
		} else {
			char note = buffer.charAt(0);
			buffer = buffer.substring(1);
			if (note == 's' || note == 'S') {
				parser2allOFF();
				if (buffer.length() != 0)
					parser2(buffer);
			} else if (note == 'l' || note == 'L') {
				if (zuletztGenannteNote != 2000) {
					parser2note(zuletztGenannteNote);
				}
				if (buffer.length() != 0)
					parser2(buffer);
			} else if (note == 'v' || note == 'V') {
				if (buffer.length() != 0 && isNumber(buffer.charAt(0))) {
					ReadNumberReturn rnr = readNumber(buffer);
					buffer = rnr.outPut;
					short nv = (short) rnr.number;
					if (nv < 128 && nv >= 0)
						MIDI.sendControlChange((short) 7, nv, currentChanal);
				}
				if (buffer.length() != 0)
					parser2(buffer);
			} else if (note == 'i' || note == 'I') {
				if (buffer.startsWith("piano")) {
					MIDI.sendProgramChange(MIDI_INSTRUMENT_piano, currentChanal);
					buffer = buffer.substring(5);
				} else if (buffer.startsWith("vibes")) {
					MIDI.sendProgramChange(MIDI_INSTRUMENT_vibes, currentChanal);
					buffer = buffer.substring(5);
				} else if (buffer.startsWith("organ")) {
					MIDI.sendProgramChange(MIDI_INSTRUMENT_organ, currentChanal);
					buffer = buffer.substring(5);
				} else if (buffer.startsWith("guitar")) {
					MIDI.sendProgramChange(MIDI_INSTRUMENT_guitar, currentChanal);
					buffer = buffer.substring(6);
				} else if (buffer.startsWith("brass")) {
					MIDI.sendProgramChange(MIDI_INSTRUMENT_brass, currentChanal);
					buffer = buffer.substring(5);
				} else {
					if (buffer.length() != 0 && isNumber(buffer.charAt(0))) {
						ReadNumberReturn rnr = readNumber(buffer);
						buffer = rnr.outPut;
						short ni = (short) rnr.number;
						MIDI.sendProgramChange(ni, currentChanal);
					}
				}
				if (buffer.length() != 0)
					parser2(buffer);
			}

			else if (ALLOW_MULTI_CHANAL_MIDI && (note == 'k' || note == 'K')) {
				if (buffer.length() != 0 && isNumber(buffer.charAt(0))) {
					ReadNumberReturn rnr = readNumber(buffer);
					buffer = rnr.outPut;
					short nc = (short) rnr.number;
					currentChanal = nc;
					if (buffer.length() != 0)
						parser2(buffer);
				} else if (buffer.length() != 0)
					parser2(buffer);
			} else {
				ReadOktaveOffsetReturn roor = readOktaveOffset(buffer);
				buffer = roor.str;
				short oktaveOffset = roor.num;
				ReadHalbtonReturn rhr = readHalbton(buffer);
				buffer = rhr.str;
				boolean habtonC = rhr.habltonC;
				boolean habtonB = rhr.halbtonB;
				GetNoteIDReturn gnidr = getNoteID(note);
				short noteID = gnidr.note;
				boolean noteDown = gnidr.noteDown;
				boolean allowHabtonC = gnidr.allowHalbtonC;
				boolean allowHabtonB = gnidr.allowHalbtonB;
				if (noteID != 2000)
					parser2note(convertNote(noteID, oktaveOffset, habtonC, habtonB, allowHabtonC, allowHabtonB, noteDown));
				if (buffer.length() != 0)
					parser2(buffer);
			}
		}
	}

	public void playNote(int note, int length) {
		MIDI.sendNoteOn((short) note, (short) 127, currentChanal);
		try {
			Thread.sleep(vierBeatZeit / length);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		MIDI.sendNoteOff((short) note, (short) 0, currentChanal);
	}

	public void parser(String buffer) {
		// C', C, c, c'
		System.out.println("Parser: " + buffer);

		if (buffer.length() == 0)
			return;

		char note = buffer.charAt(0);
		buffer = buffer.substring(1);

		short oktaveOffset = 0;

		if (buffer.length() != 0 && buffer.charAt(0) == '\'') {
			oktaveOffset++;
			buffer = buffer.substring(1);
		}

		if (buffer.length() != 0 && buffer.charAt(0) == '\'') {
			oktaveOffset++;
			buffer = buffer.substring(1);
		}

		if (buffer.length() != 0 && buffer.charAt(0) == '\'') {
			oktaveOffset++;
			buffer = buffer.substring(1);
		}

		boolean istHalbton = false;

		if (buffer.length() != 0 && buffer.charAt(0) == '#') {
			istHalbton = true;
			buffer = buffer.substring(1);
		}

		int length = Integer.parseInt(buffer);

		if (length == 0)
			length = 4;

		switch (note) {
		case 'P':
		case 'p':
			try {
				Thread.sleep(1000 / length);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case 'C':
			if (!istHalbton)
				playNote(48 - (oktaveOffset * 12), length);
			else
				playNote(49 - (oktaveOffset * 12), length);
			break;
		case 'D':
			if (!istHalbton)
				playNote(50 - (oktaveOffset * 12), length);
			else
				playNote(51 - (oktaveOffset * 12), length);
			break;
		case 'E':
			playNote(52 - (oktaveOffset * 12), length);
			break;
		case 'F':
			if (!istHalbton)
				playNote(53 - (oktaveOffset * 12), length);
			else
				playNote(54 - (oktaveOffset * 12), length);
			break;
		case 'G':
			if (!istHalbton)
				playNote(55 - (oktaveOffset * 12), length);
			else
				playNote(56 - (oktaveOffset * 12), length);
			break;
		case 'A':
			if (!istHalbton)
				playNote(57 - (oktaveOffset * 12), length);
			else
				playNote(58 - (oktaveOffset * 12), length);
			break;
		case 'H':
			playNote(59 - (oktaveOffset * 12), length);
			break;

		case 'c':
			if (!istHalbton)
				playNote(60 + (oktaveOffset * 12), length);
			else
				playNote(61 + (oktaveOffset * 12), length);
			break;
		case 'd':
			if (!istHalbton)
				playNote(62 + (oktaveOffset * 12), length);
			else
				playNote(63 + (oktaveOffset * 12), length);
			break;
		case 'e':
			playNote(64 + (oktaveOffset * 12), length);
			break;
		case 'f':
			if (!istHalbton)
				playNote(65 + (oktaveOffset * 12), length);
			else
				playNote(66 + (oktaveOffset * 12), length);
			break;
		case 'g':
			if (!istHalbton)
				playNote(67 + (oktaveOffset * 12), length);
			else
				playNote(68 + (oktaveOffset * 12), length);
			break;
		case 'a':
			if (!istHalbton)
				playNote(69 + (oktaveOffset * 12), length);
			else
				playNote(70 + (oktaveOffset * 12), length);
			break;
		case 'h':
			playNote(71 + (oktaveOffset * 12), length);
			break;
		}
	}

	public void parser1_1(String buffer) {
		System.out.println("Parser1.1: " + buffer);

		if (System.currentTimeMillis() > timeout)
			return;

		if (buffer.length() == 0)
			return;

		if (isNumber(buffer.charAt(0))) {
			ReadNumberReturn rnr = readNumber(buffer);
			buffer = rnr.outPut;
			int length = rnr.number;
			if (length == 0)
				length = 4;
			try {
				Thread.sleep(vierBeatZeit / length);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (buffer.length() != 0 && buffer.charAt(0) == '.') {
				try {
					Thread.sleep((vierBeatZeit / length) / 2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				buffer = buffer.substring(1);
			}
			return;
		}

		char note = buffer.charAt(0);
		buffer = buffer.substring(1);

		ReadOktaveOffsetReturn roor = readOktaveOffset(buffer);
		buffer = roor.str;
		short oktaveOffset = roor.num;
		ReadHalbtonReturn rhr = readHalbton(buffer);
		buffer = rhr.str;
		boolean habtonC = rhr.habltonC;
		boolean habtonB = rhr.halbtonB;
		GetNoteIDReturn gnidr = getNoteID(note);
		short noteID = gnidr.note;
		boolean noteDown = gnidr.noteDown;
		boolean allowHabtonC = gnidr.allowHalbtonC;
		boolean allowHabtonB = gnidr.allowHalbtonB;
		boolean play = true;
		if (noteID == 2000)
			play = false;
		if (note == 'p' || note == 'P')
			play = false;
		if (play)
			parser2note(convertNote(noteID, oktaveOffset, habtonC, habtonB, allowHabtonC, allowHabtonB, noteDown));
		if (buffer.length() != 0)
			parser1_1(buffer);
		else {
			if (play || note == 'p' || note == 'P')
				try {
					Thread.sleep(vierBeatZeit / 4);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	public GetNoteIDReturn getNoteID(char note) {
		short noteID = 2000;
		boolean noteDown = false;
		boolean allowHabtonB = false;
		boolean allowHabtonC = false;
		switch (note) {
		case 'C':
			noteDown = true;
			allowHabtonB = false;
			allowHabtonC = true;
			noteID = 48;
			break;
		case 'D':
			noteDown = true;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 50;
			break;
		case 'E':
			noteDown = true;
			allowHabtonB = true;
			allowHabtonC = false;
			noteID = 52;
			break;
		case 'F':
			noteDown = true;
			allowHabtonB = false;
			allowHabtonC = true;
			noteID = 53;
			break;
		case 'G':
			noteDown = true;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 55;
			break;
		case 'A':
			noteDown = true;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 57;
			break;
		case 'H':
			noteDown = true;
			allowHabtonB = true;
			allowHabtonC = false;
			noteID = 59;
			break;

		case 'c':
			noteDown = false;
			allowHabtonB = false;
			allowHabtonC = true;
			noteID = 60;
			break;
		case 'd':
			noteDown = false;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 62;
			break;
		case 'e':
			noteDown = false;
			allowHabtonB = true;
			allowHabtonC = false;
			noteID = 64;
			break;
		case 'f':
			noteDown = false;
			allowHabtonB = false;
			allowHabtonC = true;
			noteID = 65;
			break;
		case 'g':
			noteDown = false;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 67;
			break;
		case 'a':
			noteDown = false;
			allowHabtonB = true;
			allowHabtonC = true;
			noteID = 69;
			break;
		case 'h':
			noteDown = false;
			allowHabtonB = true;
			allowHabtonC = false;
			noteID = 71;
			break;
		}
		return new GetNoteIDReturn(noteID, allowHabtonC, allowHabtonB, noteDown);
	}

	public void parser2note(short note) {
		zuletztGenannteNote = note;
		if (((activeNotes[note] >> currentChanal) & 1) != 1) {
			// start note
			activeNotes[note] |= (1 << currentChanal);
			MIDI.sendNoteOn(note, (short) 127, currentChanal);
		} else {
			// stop note
			activeNotes[note] &= ~(1 << currentChanal);
			MIDI.sendNoteOff(note, (short) 0, currentChanal);
		}
	}

	public void parser2allOFF() {
		zuletztGenannteNote = 2000;
		for (short i = 0; i < 129; i++) {
			if (activeNotes[i] != 0) {
				for (byte j = 0; j < 32; j++) {
					if (((activeNotes[i] >> j) & 1) == 1) {
						MIDI.sendNoteOff(i, (short) 0, j);
					}
				}
				activeNotes[i] = 0;
			}
		}
	}

	boolean isNumber(char c) {
		switch (c) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return true;
		}
		return false;
	}

	public ReadNumberReturn readNumber(String s) {
		int curr = 0;
		while (s.length() != 0 && isNumber(s.charAt(0))) {
			curr *= 10;
			curr += s.charAt(0) - '0'; // get numerical value of number
			s = s.substring(1);
		}
		return new ReadNumberReturn(curr, s);
	}

	public ReadHalbtonReturn readHalbton(String s) {
		if (s.length() == 0)
			return new ReadHalbtonReturn(false, false, s);
		boolean halbtonC = (s.charAt(0) == '#');
		if (halbtonC)
			s = s.substring(1);
		if (s.length() == 0)
			return new ReadHalbtonReturn(halbtonC, false, s);
		boolean halbtonB = (s.charAt(0) == 'b');
		if (halbtonB)
			s = s.substring(1);
		return new ReadHalbtonReturn(halbtonC, halbtonB, s);
	}

	public short convertNote(short noteId, short oktavenOffset, boolean habtonC, boolean habtonB, boolean allowHabtonC, boolean allowHabtonB, boolean noteDown) {
		return (short) (noteId + (oktavenOffset * 12 * (noteDown ? -1 : 1)) + (allowHabtonC && habtonC ? 1 : 0) - (allowHabtonB && habtonB ? 1 : 0));
	}

	public ReadOktaveOffsetReturn readOktaveOffset(String s) {
		short offset = 0;
		for (short i = 0; i < 3; i++) {
			if (s.length() != 0 && s.charAt(0) == '\'') {
				offset++;
				s = s.substring(1);
			}
		}
		return new ReadOktaveOffsetReturn(offset, s);
	}

	public void schreibeChatNachricht(String s) {
		if (mqtt) {
			MqttMessage msg = new MqttMessage(s.getBytes());
			msg.setQos(2);
			try {
				client.publish(TOPIC_IRC_TX, msg);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		} else {
			window.chat.append(s + "\n");
		}
	}
}
