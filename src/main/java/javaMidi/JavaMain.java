package javaMidi;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javaMidi.cppconv.Interface;
import javaMidi.cppconv.Main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

public class JavaMain {

	public static String BROKER = "tcp://127.0.0.1";
	public static String CLIENT_ID = "MIDI";
	public static String MQTT_IRC_TX = "irc/tx";
	public static String TOPIC_MIDI = "playmidi";

	public static final short DEFALT_MIDI_CHANNEL = 1;
	public static final boolean ALLOW_PARSER_2 = true;
	public static final int DEFALT_BPM = 240;
	public static int NOTEN_BUFFER_LAENGE = 8;
	public static final boolean ENABLE_PARSER_1_1 = true;
	public static final boolean ALLOW_MULTI_CHANNEL_MIDI = true;
	public static String MQTT_MUSIC_ON_TOPIC = "stream/music";
	public static String MQTT_MUSIC_OFF_TOPIC = MQTT_MUSIC_ON_TOPIC;
	public static String MQTT_MUSIC_ON_MASSAGE = "on";
	public static String MQTT_MUSIC_OFF_MASSAGE = "off";
	public static String SONG_NOT_EXISTS = "H' E'";

	public static final int MAX_PLAYREQUESTS = 2;
	public static final int MENGE_PRESET_LIEDER = 16;
	public static final int MENGE_PRESET_INSTRUMENTE = 8;

	public static final int MAJOR_VERSION = 0;
	public static final int MINOR_VERSION = 8;
	public static final boolean INDEV = false;

	public static short MIDI_INSTRUMENT_piano = 0;
	public static short MIDI_INSTRUMENT_vibes = 11;
	public static short MIDI_INSTRUMENT_organ = 19;
	public static short MIDI_INSTRUMENT_guitar = 30;
	public static short MIDI_INSTRUMENT_brass = 62;

	public MqttClient client;
	public Gson gson;
	public static JavaMain main;

	public NotenBufferEintrag notenBuffer[];
	public Instrument instrumente[];
	public Lied presetLieder[];
	public short zuletztGenannteNote = 2000;
	public short currentChannel = DEFALT_MIDI_CHANNEL;
	public boolean parserV2 = false;
	public int bpm = DEFALT_BPM;
	public int activeNotes[];
	public int vierBeatZeit = 1000;
	public long timeout = 0;
	public boolean mqtt = true;
	public Window window;
	public TXTtoMIDI txtToMidi;
	public Config config;

	public static void main(String[] args) {
		System.out.println("loading System v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION + ""
				+ (INDEV ? "-indev" : ""));
		File sf = new File("sf.sf2");
		if (!sf.exists()) {
			try {
				int opt = JOptionPane.showConfirmDialog(null, "Soll die Soundfont \"FluidR3_GM\" herunterladen werden?",
						"fehlende Soundfont!", JOptionPane.OK_CANCEL_OPTION);
				if (opt == JOptionPane.OK_OPTION) {
					if (!downloadSoundfont(sf)) {
						JOptionPane.showMessageDialog(null, "Die datei konnte nicht herunter geladen werden!", "INFO",
								JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Eine Soundfont wird zum ausführen benötigt!", "INFO",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			} catch (Exception e) {
				System.out.println("Keine Soundfont gefunden!");
				System.out.println("Mochten sie die Soundfont \"FluidR3_GM\" herunterladen? [y/n]");
				Scanner s = new Scanner(System.in);
				String o = s.nextLine();
				s.close();
				if (o.equalsIgnoreCase("") || o.equalsIgnoreCase("y")) {
					if (!downloadSoundfont(sf)) {
						System.out.println("Die datei konnte nicht herunter geladen werden!");
						System.exit(0);
					}
				} else {
					System.out.println("INFO: Eine Soundfont wird zum ausführen benötigt!");
					System.exit(0);
				}
			}
		}
		Options options = new Options();
		Option optBroker = new Option("b", "protucoll and ip of mqtt broker");
		optBroker.setArgs(1);
		optBroker.setLongOpt("broker");
		options.addOption(optBroker);
		Option optMidi = new Option("m", "topic of Midi activity");
		optMidi.setArgs(1);
		optMidi.setLongOpt("topicMidi");
		options.addOption(optMidi);
		Option optChat = new Option("c", "topic of Twitch chat output");
		optChat.setArgs(1);
		optChat.setLongOpt("topicChat");
		options.addOption(optChat);
		Option optClientID = new Option("i", "mqtt clientID");
		optClientID.setArgs(1);
		optClientID.setLongOpt("clientID");
		options.addOption(optClientID);
		Option optNBL = new Option("a", "amount of note buffers");
		optNBL.setArgs(1);
		optNBL.setLongOpt("noteBufferLenght");
		options.addOption(optNBL);
		Option optMqtt = new Option("q", "if Mqtt should be used");
		optMqtt.setArgs(0);
		optMqtt.setLongOpt("mqtt");
		options.addOption(optMqtt);
		Option optHelp = new Option("h", "shows help");
		optHelp.setArgs(0);
		optHelp.setLongOpt("help");
		options.addOption(optHelp);
		DefaultParser parser = new DefaultParser();
		try {
			CommandLine cli = parser.parse(options, args);
			if (cli.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("javaMidi", "ProjektionTV Java-MIDI Emulator", options,
						"by gkram & (parser,(mqttcallback),(playsong) & playNote:ProjektionTV)");
				return;
			}
			if (cli.hasOption("a")) {
				NOTEN_BUFFER_LAENGE = Integer.parseInt(cli.getOptionValue("a"));
			}
			if (cli.hasOption("q")) {
				if (cli.hasOption("b")) {
					BROKER = cli.getOptionValue("b");
				}
				if (cli.hasOption("i")) {
					CLIENT_ID = cli.getOptionValue("i");
				}
				if (cli.hasOption("c")) {
					MQTT_IRC_TX = cli.getOptionValue("c");
				}
				if (cli.hasOption("m")) {
					TOPIC_MIDI = cli.getOptionValue("m");
				}
				new JavaMain(true);
			} else {
				new JavaMain(false);
			}
			System.out.println("loading MIDI");
			MIDI.init();
			Main.setup();
		} catch (Exception e) {
			e.printStackTrace();
			new JavaMain(false);
		}
	}

	public static void midiDelay(long delay) {
		if(main.window.shoudStop){
			main.timeout = System.currentTimeMillis();
			return;
		}
		if((System.currentTimeMillis() + delay) > main.timeout){
			delay(main.timeout - System.currentTimeMillis());
		}else{
			delay(delay);
		}
	}

	public static void delay(long delay) {
		if(main.txtToMidi.recording){
			main.txtToMidi.currentTime += delay;
			return;
		}
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public JavaMain(boolean doMqtt) {
		main = this;
		System.out.println("loading JSON");
		gson = new Gson();
		System.out.println("loading Config");
		config = new Config();
		txtToMidi = new TXTtoMIDI();
		activeNotes = new int[129];
		notenBuffer = new NotenBufferEintrag[NOTEN_BUFFER_LAENGE];
		for (int i = 0; i < NOTEN_BUFFER_LAENGE; i++)
			notenBuffer[i] = new NotenBufferEintrag();
		instrumente = new Instrument[MENGE_PRESET_INSTRUMENTE];
		for (int i = 0; i < MENGE_PRESET_INSTRUMENTE; i++)
			instrumente[i] = new Instrument();
		presetLieder = new Lied[MENGE_PRESET_LIEDER];
		for (int i = 0; i < MENGE_PRESET_LIEDER; i++)
			presetLieder[i] = new Lied();
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
						Interface.mqttCallback(topic, new String(message.getPayload()));
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

	public static boolean downloadSoundfont(File sf) {
		try {
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
					new URL("https://member.keymusician.com/Member/FluidR3_GM/FluidR3_GM.zip").openStream()));
			ZipEntry ent = zis.getNextEntry();
			while (ent != null) {
				if (ent.getName().contains(".sf2")) {
					FileOutputStream fos = new FileOutputStream(sf);
					long si = ent.getSize();
					byte[] buff = new byte[65536];
					while (si > 0) {
						if (si > buff.length) {
							si -= buff.length;
							zis.read(buff, 0, buff.length);
							fos.write(buff, 0, buff.length);
						} else {
							zis.read(buff, 0, (int) si);
							fos.write(buff, 0, (int) si);
							si -= si;
						}
					}
					fos.close();
					break;
				}
				ent = zis.getNextEntry();
			}
			zis.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
