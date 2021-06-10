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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.gson.JsonObject;

import javaMidi.cppconv.Interface;

public class Window {

	JTextArea input;
	public JTextArea chat;
	JSlider volumen;
	public Image icon;
	JFrame changeLogWin;

	public Window() {
		try {
			icon = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
		} catch (IOException | IllegalArgumentException e) {
		}
		JFrame jf = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ "" + (JavaMain.INDEV ? "-indev" : ""));
		jf.setSize(1280, 720);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (icon != null)
			jf.setIconImage(icon);

		input = new JTextArea(10, 500);
		input.setEditable(true);
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
		JCheckBox doBuffer = new JCheckBox("Aktivire Buffer");
		JCheckBox doAdmin = new JCheckBox("Aktivire Admin");
		volumen = new JSlider(JSlider.HORIZONTAL, 0, 127, 127);

		JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);

		chat.setAutoscrolls(true);
		input.setAutoscrolls(true);

		settings.add(new JLabel("name:"));
		settings.add(new JLabel("maximale Spieleit:"));
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

		volumen.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				MIDI.setVolume((float) (volumen.getValue()) / (float) (volumen.getMaximum()));
			}
		});

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!send.isEnabled())
					return;
				send.setEnabled(false);
				String[] toPlay = input.getText().split("\n");
				JsonObject o = new JsonObject();
				o.addProperty("aktiviereBuffer", doBuffer.isSelected());
				o.addProperty("laenge", (int) maxPlayTime.getValue());
				o.addProperty("maximaleBufferGroesse", (int) maxBuffer.getValue());
				o.addProperty("prioritaet", (int) prio.getValue());
				o.addProperty("nutzer", name.getText());
				o.addProperty("adminModus", doAdmin.isSelected());
				progress.setMaximum(toPlay.length);
				new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < toPlay.length; i++) {
							progress.setValue(i + 1);
							String t = toPlay[i];
							try {
								o.addProperty("midi", t);
								Interface.mqttCallback(JavaMain.TOPIC_MIDI, JavaMain.main.gson.toJson(o));
								jf.repaint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						progress.setValue(0);
						progress.setMaximum(0);
						send.setEnabled(true);
					}
				}).start();
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
				imp.setVisible(true);
			}
		});
		jmTools.add(jmiTWFormetter);

		jmb.add(jmTools);

		JMenu jmAnsicht = new JMenu("Ansicht");

		JCheckBoxMenuItem jcmiLineWrap = new JCheckBoxMenuItem("Zeilenumbruch");
		jcmiLineWrap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean nv = jcmiLineWrap.isSelected();
				chat.setLineWrap(nv);
				input.setLineWrap(nv);
			}
		});
		jmAnsicht.add(jcmiLineWrap);

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
		JEditorPane cl = new JEditorPane("text/html", log);
		cl.setEditable(false);
		if (icon != null)
			changeLogWin.setIconImage(icon);
		changeLogWin.setContentPane(new JScrollPane(cl));
		changeLogWin.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		jf.setVisible(true);
	}

	public JFrame getMidiImport() {
		JFrame e = new JFrame("ProjektionTV Midi Emulator v" + JavaMain.MAJOR_VERSION + "." + JavaMain.MINOR_VERSION
				+ (JavaMain.INDEV ? "-indev" : "") + " .mid Convertierer");
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (icon != null)
			e.setIconImage(icon);

		JFileChooser jfc = new JFileChooser();
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
				JTextArea jta = new JTextArea(data[i]);
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
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (icon != null)
			e.setIconImage(icon);

		JPanel content = new JPanel(new BorderLayout());

		JTextArea jta = new JTextArea(input.getText(), 1, 500);
		jta.setEditable(true);
		JScrollPane jsc = new JScrollPane(jta);
		content.add(jsc, BorderLayout.NORTH);

		JTabbedPane jtp = new JTabbedPane();
		jtp.addTab("", new JLabel("Bitte Formatieren drücken."));
		content.add(jtp, BorderLayout.CENTER);

		JButton jb = new JButton("Formatieren");
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String in = jta.getText();
				jtp.removeAll();
				if (in.length() <= 500) {
					JPanel t = new JPanel(new BorderLayout());
					JTextArea jtaT = new JTextArea(in);
					jtaT.setEditable(false);
					JScrollPane jspT = new JScrollPane(jtaT);
					t.add(jspT, BorderLayout.CENTER);
					JButton jb = new JButton("Copy to Clipbord");
					jb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							Toolkit.getDefaultToolkit().getSystemClipboard()
									.setContents(new StringSelection(jtaT.getText()), null);
						}
					});
					t.add(jb, BorderLayout.NORTH);
					jtp.addTab("1", t);
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
					for (int i = 0; i < datas.size(); i++) {
						JPanel t = new JPanel(new BorderLayout());
						JTextArea jtaT = new JTextArea(datas.get(i));
						jtaT.setEditable(false);
						JScrollPane jspT = new JScrollPane(jtaT);
						t.add(jspT, BorderLayout.CENTER);
						JButton jb = new JButton("Copy to Clipbord");
						jb.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								Toolkit.getDefaultToolkit().getSystemClipboard()
										.setContents(new StringSelection(jtaT.getText()), null);
							}
						});
						t.add(jb, BorderLayout.NORTH);
						jtp.addTab((i + 1) + "", t);
					}
				}
			}
		});
		content.add(jb, BorderLayout.SOUTH);

		e.setContentPane(content);

		return e;
	}

}
