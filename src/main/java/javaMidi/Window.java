package javaMidi;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.Desktop.Action;
import java.awt.Image;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javaMidi.cppconv.Interface;

public class Window {

	RSyntaxTextArea input;
	public JTextArea chat;
	JSlider volumen;
	public Image icon;
	JFrame changeLogWin;
	JMenu themeMenu;
	ButtonGroup themeMenuButtonGroup;
	JLabel timeText;
	long timeStart;
	long timeEnd;
	long timeSegmentStart;
	boolean playing;
	Thread player;
	boolean shoudStop = false;
	JFrame exportUI;
	RSyntaxTextArea exportUIText;
	JSpinner exportUICfgCfgConvTime;
	JTextField exportUICfgCfgPath;
	public FileFilter midiFileFilter;
	JMenuItem jmiMidiExport;
	Config cfg;
	String loadLaterPath;
	boolean loadLaterInternal = false;
	Font font;

	Theme theme;

	public Window() {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/playMidi", "javaMidi.MidiSyntaxHighlighter");
		theme = new Theme();
		theme.loadInternal("light");
		cfg = JavaMain.main.config;
		midiFileFilter = new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.getName().toUpperCase().endsWith(".MIDI") || f.getName().toUpperCase().endsWith(".MID") || f.isDirectory();
			}
			@Override
			public String getDescription() {
				return ".midi / .mid";
			}
		};
		try {
			icon = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
		} catch (IOException | IllegalArgumentException e) {
		}
		JFrame jf = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ "" + (JavaMain.INDEV ? "-indev" : ""));
		jf.setSize(1280, 720);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theme.addUiToHandle(jf);
		if (icon != null)
			jf.setIconImage(icon);

		input = new RSyntaxTextArea(10, 500);
		input.setEditable(true);
		input.setSyntaxEditingStyle("text/playMidi");
		theme.addSyntaxAreaToHandel(input);
		JScrollPane jspi = new JScrollPane(input);

		chat = new JTextArea(10, 50);
		chat.setEditable(false);
		JScrollPane jspc = new JScrollPane(chat);

		JPanel settings = new JPanel(new GridLayout(2, 0));

		JTextField name = new JTextField(10);
		JButton send = new JButton("Spiele");
		JSpinner maxBuffer = new JSpinner();
		maxBuffer.setValue(1000);
		JSpinner maxPlayTime = new JSpinner();
		maxPlayTime.setValue(16);
		JSpinner prio = new JSpinner();
		prio.setValue(0);
		JCheckBox doBuffer = new JCheckBox("Aktiviere Buffer");
		JCheckBox doAdmin = new JCheckBox("Aktiviere Admin");
		volumen = new JSlider(JSlider.HORIZONTAL, 0, 127, 127);
		timeText = new JLabel("00:00");
		timeText.setHorizontalTextPosition(JLabel.CENTER);

		JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);

		playing = false;

		chat.setAutoscrolls(true);
		input.setAutoscrolls(true);

		settings.add(new JLabel("name:"));
		settings.add(new JLabel("maximale Spielzeit:"));
		settings.add(new JLabel("maximale Puffer groesse:"));
		settings.add(new JLabel("Puffer prioritaet:"));
		settings.add(new JLabel("Lautstaerke:"));
		settings.add(doBuffer);
		settings.add(doAdmin);

		settings.add(name);
		settings.add(maxPlayTime);
		settings.add(maxBuffer);
		settings.add(prio);
		settings.add(volumen);
		settings.add(send);
		settings.add(timeText);

		volumen.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				MIDI.setVolume((float) (volumen.getValue()) / (float) (volumen.getMaximum()));
			}
		});

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!send.isEnabled())
					return;
				if(!playing){
					playing = true;
					send.setText("Stopp");
					String[] toPlay = input.getText().split("\n");
					JsonObject o = new JsonObject();
					o.addProperty("aktiviereBuffer", doBuffer.isSelected());
					o.addProperty("laenge", (int) maxPlayTime.getValue());
					o.addProperty("maximaleBufferGroesse", (int) maxBuffer.getValue());
					o.addProperty("prioritaet", (int) prio.getValue());
					o.addProperty("nutzer", name.getText());
					o.addProperty("adminModus", doAdmin.isSelected());
					progress.setMaximum(toPlay.length);
					player = new Thread(new Runnable() {
						@Override
						public void run() {
							timeStart = System.currentTimeMillis();
							for (int i = 0; i < toPlay.length; i++) {
								if(shoudStop)
									break;
								progress.setValue(i + 1);
								String t = toPlay[i];
								try {
									o.addProperty("midi", t);
									timeSegmentStart = System.currentTimeMillis();
									Interface.mqttCallback(JavaMain.TOPIC_MIDI, JavaMain.main.gson.toJson(o));
									jf.repaint();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							send.setText("Spiele");
							shoudStop = false;
							send.setEnabled(true);
							progress.setValue(0);
							progress.setMaximum(0);
							playing = false;
							timeEnd = System.currentTimeMillis();
						}
					});
					player.start();
				} else {
					send.setText("Stoppe...");
					send.setEnabled(false);
					if(player.isAlive()){
						shoudStop = true;
						JavaMain.main.timeout = System.currentTimeMillis();
					}else{
						send.setText("Spiele");
						progress.setValue(0);
						progress.setMaximum(0);
						playing = false;
						shoudStop = false;
						send.setEnabled(true);
						timeEnd = System.currentTimeMillis();
					}
				}
			}
		});

		JPanel dChat = new JPanel(new BorderLayout());
		dChat.add(progress, BorderLayout.NORTH);
		dChat.add(jspc, BorderLayout.CENTER);

		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspi, dChat);

		JPanel content = new JPanel(new BorderLayout());
		content.add(jsp, BorderLayout.CENTER);
		content.add(settings, BorderLayout.NORTH);

		jf.setContentPane(content);

		JMenuBar jmb = new JMenuBar();

		JMenu jmTools = new JMenu("Tools");

		JMenuItem jmiMidiFileConverter = new JMenuItem(".mid converter");
		jmiMidiFileConverter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame imp = getMidiImport();
				if (imp == null)
					return;
				imp.setSize(1280, 720);
				centerWindow(imp, jf);
				imp.setVisible(true);
			}
		});
		jmTools.add(jmiMidiFileConverter);

		JMenuItem jmiTWFormetter = new JMenuItem("Twitch Formetter");
		jmiTWFormetter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame imp = getTwitchFormatter();
				if (imp == null)
					return;
				imp.setSize(1280, 720);
				centerWindow(imp, jf);
				imp.setVisible(true);
			}
		});
		jmTools.add(jmiTWFormetter);

		jmiMidiExport = new JMenuItem(".mid export");
		jmiMidiExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportUI.setSize(1280, 720);
				exportUIText.setText(input.getText());
				exportUICfgCfgConvTime.setValue(16);
				exportUICfgCfgPath.setText("");
				centerWindow(exportUI, jf);
				exportUI.setVisible(true);
				exportUI.requestFocus();
			}
		});
		jmTools.add(jmiMidiExport);

		jmb.add(jmTools);

		JMenu jmAnsicht = new JMenu("Ansicht");

		if(cfg.ui.has("lineWrap") && cfg.ui.get("lineWrap").getAsBoolean()){
			chat.setLineWrap(true);
			input.setLineWrap(true);
		}
		JCheckBoxMenuItem jcmiLineWrap = new JCheckBoxMenuItem("Zeilenumbruch", cfg.ui.has("lineWrap") ? cfg.ui.get("lineWrap").getAsBoolean() : false);
		jcmiLineWrap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean nv = jcmiLineWrap.isSelected();
				cfg.ui.addProperty("lineWrap", nv);
				chat.setLineWrap(nv);
				input.setLineWrap(nv);
				cfg.save();
			}
		});
		jmAnsicht.add(jcmiLineWrap);

		themeMenu = new JMenu("Theme");
		themeMenuButtonGroup = new ButtonGroup();
		jmAnsicht.add(themeMenu);
		populateThemeMenu();

		jmb.add(jmAnsicht);

		JMenu jmHilfe = new JMenu("Hilfe");

		JMenuItem jmiLNoten = new JMenuItem("Noten dukumentation");
		jmiLNoten.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE))
					try {
						Desktop.getDesktop()
								.browse(new URI("https://github.com/ProjektionTV/Esp32MidiPlayer#playmidi-syntax"));
					} catch (IOException | URISyntaxException e) {
					}
				else
					JOptionPane.showMessageDialog(jf,
							(Object) "Die url: \"https://github.com/ProjektionTV/Esp32MidiPlayer#playmidi-syntax\" konte nicht geofnet werden",
							"Fehler", JOptionPane.WARNING_MESSAGE, new ImageIcon(icon));

			}
		});
		jmHilfe.add(jmiLNoten);

		JMenuItem jmiLEmu = new JMenuItem("Emulator dukumentation");
		jmiLEmu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE))
					try {
						Desktop.getDesktop().browse(new URI(
								"https://github.com/lordgkram/ESPMidiPlayer-Java-Emulator#esp32midiplayer-java-emulator"));
					} catch (IOException | URISyntaxException e) {
					}
				else
					JOptionPane.showMessageDialog(jf,
							(Object) "Die url: \"https://github.com/lordgkram/ESPMidiPlayer-Java-Emulator#esp32midiplayer-java-emulator\" konte nicht geofnet werden",
							"Fehler", JOptionPane.WARNING_MESSAGE, new ImageIcon(icon));
			}
		});
		jmHilfe.add(jmiLEmu);

		JMenuItem jmiueber = new JMenuItem("Über");
		jmiueber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = "ProjektionTV Esp32 MidiPlayer - Java Emulator\n" + "v" + JavaMain.MAJOR_VERSION + "."
						+ JavaMain.MINOR_VERSION + (JavaMain.INDEV ? "-indev" : "") + "\n" + "von gkram\n";
				JOptionPane.showMessageDialog(jf, text, "Über", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(icon));
			}
		});
		jmHilfe.add(jmiueber);

		JMenuItem jmichangeLog = new JMenuItem("Changelog");
		jmichangeLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeLogWin.setSize(1280, 720);
				centerWindow(changeLogWin, jf);
				changeLogWin.setVisible(true);
				changeLogWin.requestFocus();
			}
		});
		jmHilfe.add(jmichangeLog);

		JMenuItem jmiLProjektion = new JMenuItem("Zu ProjektionTV");
		jmiLProjektion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE))
					try {
						Desktop.getDesktop().browse(new URI("https://www.twitch.tv/projektiontv"));
					} catch (IOException | URISyntaxException e) {
					}
				else
					JOptionPane.showMessageDialog(jf,
							(Object) "Die url: \"https://www.twitch.tv/projektiontv\" konte nicht geofnet werden",
							"Fehler", JOptionPane.WARNING_MESSAGE, new ImageIcon(icon));
			}
		});
		jmHilfe.add(jmiLProjektion);

		jmb.add(jmHilfe);

		jf.setJMenuBar(jmb);

		changeLogWin = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ "" + (JavaMain.INDEV ? "-indev" : "") + " Changelog");
		String log = "";
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream("/changelog.html")), "utf-8"));
			String line;
			while ((line = br.readLine()) != null){
				log += line + System.lineSeparator();
			}
		}catch(Exception e){
			log = "Changelog konte nicht geladen Werden.";
		}
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new URI("https://raw.githubusercontent.com/lordgkram/ESPMidiPlayer-Java-Emulator/master/src/main/resources/changelog.html").toURL().openStream()), "utf-8"));
			String line;
			log = "";
			while ((line = br.readLine()) != null){
				log += line + System.lineSeparator();
			}
		}catch(Exception e){
		}
		JEditorPane cl = new JEditorPane("text/html", log);
		cl.setEditable(false);
		theme.addChangeLogToHandel(cl, log);
		if (icon != null)
			changeLogWin.setIconImage(icon);
		changeLogWin.setContentPane(new JScrollPane(cl));
		changeLogWin.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		theme.addUiToHandle(changeLogWin);

		// create Timer
		Timer t = new Timer(500, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				long segmentTime;
				long playTime;
				if(playing){
					long curr = System.currentTimeMillis();
					segmentTime = curr - timeSegmentStart;
					playTime = curr - timeStart;
				} else {
					playTime = timeEnd - timeStart;
					segmentTime = -1;
				}
				timeText.setText(msToString(playTime) + (playing ? " | " + msToString(segmentTime) : ""));
			}
		});
		t.start();

		// crate export UI
		exportUI = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
			+ (JavaMain.INDEV ? "-indev" : "") + " .mid export");
		if (icon != null)
			exportUI.setIconImage(icon);
		exportUI.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		theme.addUiToHandle(exportUI);

		JPanel exportUIPnl = new JPanel(new BorderLayout());
		exportUI.setContentPane(exportUIPnl);

		exportUIText = new RSyntaxTextArea();
		exportUIText.setSyntaxEditingStyle("text/playMidi");
		exportUIText.setEditable(true);
		exportUIText.setLineWrap(true);
		RTextScrollPane exportUITextScroll = new RTextScrollPane(exportUIText, true);
		theme.addSyntaxAreaToHandel(exportUIText);
		exportUIPnl.add(exportUITextScroll, BorderLayout.CENTER);

		JPanel exportUICfgPnl = new JPanel(new BorderLayout());
		exportUIPnl.add(exportUICfgPnl, BorderLayout.SOUTH);

		JPanel exportUICfgCfgPnl = new JPanel(new GridLayout(2, 3));

		exportUICfgCfgPnl.add(new JLabel("Datei:"));
		exportUICfgCfgPath = new JTextField();
		exportUICfgCfgPnl.add(exportUICfgCfgPath);
		JButton exportUICfgCfgFileChoose = new JButton("Datei wählen");
		exportUICfgCfgFileChoose.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setMultiSelectionEnabled(false);
				if(exportUICfgCfgPath.getText().toUpperCase().endsWith(".MIDI") || exportUICfgCfgPath.getText().toUpperCase().endsWith(".MID"))
					jfc.setSelectedFile(new File(exportUICfgCfgPath.getText()));
				jfc.setFileFilter(midiFileFilter);
				int option = jfc.showSaveDialog(exportUI);
				if(option == JFileChooser.APPROVE_OPTION){
					exportUICfgCfgPath.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		exportUICfgCfgPnl.add(exportUICfgCfgFileChoose);

		exportUICfgCfgPnl.add(new JLabel("Max konvertirungszeit:"));
		exportUICfgCfgConvTime = new JSpinner();
		exportUICfgCfgPnl.add(exportUICfgCfgConvTime);
		exportUICfgCfgConvTime.setValue(16);

		exportUICfgPnl.add(exportUICfgCfgPnl, BorderLayout.CENTER);

		JButton exportUIDoBtn = new JButton("export");
		exportUIDoBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				exportUI.setVisible(false);
				jmiMidiExport.setEnabled(false);
				send.setEnabled(false);
				send.setText("exportiere...");
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Sequence e = JavaMain.main.txtToMidi.record(input.getText(), (int) exportUICfgCfgConvTime.getValue(), true);
							if(e == null){
								JOptionPane.showMessageDialog(exportUI, "Es ist ein Fehler beim exportieren aufgetreten.", "Fehler", JOptionPane.ERROR_MESSAGE);
							}else{
								MidiSystem.write(e, 0, new File(exportUICfgCfgPath.getText()));
								JOptionPane.showMessageDialog(exportUI, "Die Daten wurden erfolgreich Exportiert.", "Fertig", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (Exception e) {
							JOptionPane.showMessageDialog(exportUI, "Es ist ein Fehler beim exportieren aufgetreten.", "Fehler", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						send.setText("Spiele");
						send.setEnabled(true);
						jmiMidiExport.setEnabled(true);
					}
				}).start();
			}
		});
		exportUICfgPnl.add(exportUIDoBtn, BorderLayout.SOUTH);

		if(loadLaterPath.length() != 0){
			if(loadLaterInternal)
				theme.loadInternal(loadLaterPath);
			else
				theme.loadExternal(loadLaterPath);
		}

		// show UI
		jf.setVisible(true);
	}

	public JFrame getMidiImport() {
		JFrame e = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ (JavaMain.INDEV ? "-indev" : "") + " .mid Convertierer");
		theme.addUiToHandle(e);
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (icon != null)
			e.setIconImage(icon);

		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(false);
		jfc.setFileFilter(midiFileFilter);
		int rtn = jfc.showOpenDialog(e);

		if (rtn == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			e.setTitle(e.getTitle() + " : " + f.getName());
			String bpmstr = JOptionPane.showInputDialog(e, "Import bpm", "120");
			int bpm = Integer.parseInt(bpmstr);
			String[] data = MIDItoTXT.convert(f, bpm);

			JTabbedPane jtb = new JTabbedPane();
			e.setContentPane(jtb);

			for (int i = 0; i < data.length; i++) {
				RSyntaxTextArea jta = new RSyntaxTextArea(data[i]);
				jta.setSyntaxEditingStyle("text/playMidi");
				theme.addSyntaxAreaToHandel(jta);
				jta.setEditable(false);
				jta.setLineWrap(true);

				JScrollPane jsp = new JScrollPane(jta);

				JPanel order = new JPanel(new BorderLayout());
				order.add(jsp, BorderLayout.CENTER);

				JButton jb = new JButton("Copy to Clipbord");
				jb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(jta.getText()),
								null);
					}
				});
				order.add(jb, BorderLayout.SOUTH);

				jtb.addTab("Track " + (i + 1), order);
			}
		} else {
			e = null;
		}

		return e;
	}

	public JFrame getTwitchFormatter() {
		JFrame e = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ (JavaMain.INDEV ? "-indev" : "") + " Twitch Formatter");
		theme.addUiToHandle(e);
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (icon != null)
			e.setIconImage(icon);

		JPanel content = new JPanel(new BorderLayout());

		RSyntaxTextArea jta = new RSyntaxTextArea(input.getText(), 2, 500);
		jta.setSyntaxEditingStyle("text/playMidi");
		jta.setEditable(true);
		theme.addSyntaxAreaToHandel(jta);
		JScrollPane jsc = new JScrollPane(jta);
		content.add(jsc, BorderLayout.NORTH);

		JPanel pnl = new JPanel(new BorderLayout());
		content.add(pnl, BorderLayout.CENTER);

		JButton jb = new JButton("Formatieren");
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String in = jta.getText();
				pnl.removeAll();
				ArrayList<String> daten = new ArrayList<>();
				if (in.length() <= 500) {
					daten.add(in);
				} else {
					String[] e = in.split(" ");
					ArrayList<String> datas = new ArrayList<>();
					String local = ";ln";
					for (int i = 0; i < e.length; i++) {
						if ((local + " " + e[i]).length() <= 500) {
							local += " " + e[i];
						} else {
							datas.add(local);
							local = new String();
							local = "; " + e[i];
						}
					}
					datas.add(local.substring(2));
					daten.addAll(datas);
				}
				String dat = "";
				for(int i = 0; i < daten.size(); i++){
					dat += daten.get(i);
					if(i + 1 < daten.size())
						dat += "\n";
				}
				RSyntaxTextArea jtaT = new RSyntaxTextArea(dat);
				jtaT.setSyntaxEditingStyle("text/playMidi");
				jtaT.setEditable(false);
				jtaT.setLineWrap(true);
				RTextScrollPane rtsp = new RTextScrollPane(jtaT, true);
				theme.addSyntaxAreaToHandel(jtaT);
				pnl.add(rtsp, BorderLayout.CENTER);

				JPanel ctcb = new JPanel(new GridLayout(0, 1));
				for(int i = 0; i < daten.size(); i++){
					JButton b = new JButton("Copy to Clipbord " + (i + 1));
					b.setActionCommand(daten.get(i));
					b.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent arg0) {
							Toolkit.getDefaultToolkit().getSystemClipboard()
									.setContents(new StringSelection(arg0.getActionCommand()), null);
						}
					});
					ctcb.add(b);
				}
				pnl.add(ctcb, BorderLayout.EAST);

				pnl.revalidate();
				pnl.repaint();
			}
		});
		content.add(jb, BorderLayout.SOUTH);

		e.setContentPane(content);

		return e;
	}

	public void addThemeToMenu(String name, boolean internal, String path){
		addThemeToMenu(name, internal, path, false);
	}

	public void addThemeToMenu(String name, boolean internal, String path, boolean set){
		JRadioButtonMenuItem itm = new JRadioButtonMenuItem(name, set);
		itm.setActionCommand(name + ";" + (internal ? "I" : "E") + path);
		itm.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String t = e.getActionCommand();
				String[] o = t.split(";", 2);
				String cmd = o[1];
				char op = cmd.charAt(0);
				if(op == 'I'){
					theme.loadInternal(cmd.substring(1));
				}else if(op == 'E'){
					theme.loadExternal(cmd.substring(1));
				}
				cfg.ui.addProperty("theme", o[0]);
				cfg.save();
			}
		});
		themeMenu.add(itm);
		themeMenuButtonGroup.add(itm);
	}

	public static String msToString(long tms){
		long ms = tms % 1000;
		long ts = (tms - ms) / 1000;
		long s = ts % 60;
		long tm = (ts - s) / 60;
		String st = String.valueOf(s);
		if(st.length() == 1) st = "0" + st;
		String mt = String.valueOf(tm);
		if(mt.length() == 1) mt = "0" + mt;
		return mt + ":" + st;
	}

	public static void centerWindow(JFrame f, JFrame r){
		int xd = (r.getWidth() - f.getWidth()) / 2;
		int yd = (r.getHeight() - f.getHeight()) / 2;
		if(yd < 0)
			yd = 0;
		f.setLocation(r.getX() + xd, r.getY() + yd);
	}

	public void populateThemeMenu(){
		themeMenu.removeAll();
		String selectedTheme = cfg.ui.has("theme") ? cfg.ui.get("theme").getAsString() : "Hell";
		if(selectedTheme.equalsIgnoreCase("Hell")){
			addThemeToMenu("Hell", true, "light", true);
			loadLaterPath = "light";
			loadLaterInternal = true;
		}else{
			addThemeToMenu("Hell", true, "light");
		}
		if(selectedTheme.equalsIgnoreCase("Dunkel")){
			addThemeToMenu("Dunkel", true, "dark", true);
			loadLaterPath = "dark";
			loadLaterInternal = true;
		}else{
			addThemeToMenu("Dunkel", true, "dark");
		}
		if(cfg.ui.has("coustomThemes")){
			JsonArray themes = cfg.ui.get("coustomThemes").getAsJsonArray();
			boolean addedSeperator = false;
			int untc = 0;
			for(JsonElement o : themes){
				JsonObject o2 = o.getAsJsonObject();
				String name = o2.has("name") ? o2.get("name").getAsString() : "Unbenantes Theme " + (untc + 1);
				untc += o2.has("name") ? 0 : 1;
				String path = o2.has("path") ? o2.get("path").getAsString() : "";
				boolean internal = o2.has("internal") && o2.get("internal").getAsBoolean();
				if(path.length() != 0 && name.equalsIgnoreCase(selectedTheme)){	
					loadLaterPath = path;
					loadLaterInternal = internal;
				}
				if(!(o2.has("hidden") && o2.get("hidden").getAsBoolean())){
					if(!addedSeperator){
						addedSeperator = true;
						themeMenu.add(new JSeparator());
					}
					if(path.length() != 0 && name.equalsIgnoreCase(selectedTheme))
						addThemeToMenu(name, internal, path, true);
					else
						addThemeToMenu(name, internal, path);
				}
			}
		}
	}

}
