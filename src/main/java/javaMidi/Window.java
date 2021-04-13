package javaMidi;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Window {

	JTextArea input;
	JTextArea chat;
	JSlider volumen;

	public Window() {
		JFrame jf = new JFrame("ProjektionTV Midi Emulator v" + Main.MAJOR_VERSION + "." + Main.MINOR_VERSION + "");
		jf.setSize(1280, 720);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		input = new JTextArea(5, 500);
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
		volumen = new JSlider(JSlider.HORIZONTAL, 0, 127, 127);

		JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);

		settings.add(new JLabel("name:"));
		settings.add(new JLabel("maximale Spieleit:"));
		settings.add(new JLabel("maximale Puffer groesse:"));
		settings.add(new JLabel("Puffer prioritaet:"));
		settings.add(new JLabel("Lautstaerke:"));
		settings.add(doBuffer);

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
				String head = "{\"aktiviereBuffer\":" + (doBuffer.isSelected() ? "true" : "false") + ",\"laenge\":"
						+ String.valueOf(maxPlayTime.getValue()) + "," + "\"maximaleBufferGroesse\":"
						+ String.valueOf(maxBuffer.getValue()) + ",\"prioritaet\":" + String.valueOf(prio.getValue())
						+ ",\"nutzer\":\"" + name.getText() + "\",\"midi\":\"";
				String suffix = "\"}";
				String[] toPlay = input.getText().split("\n");
				progress.setMaximum(toPlay.length);
				new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < toPlay.length; i++) {
							progress.setValue(i + 1);
							String t = toPlay[i];
							MqttMessage msg = new MqttMessage((head + t + suffix).getBytes());
							try {
								Main.main.messageArrived(Main.TOPIC_MIDI, msg);
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
		dChat.add(progress, BorderLayout.CENTER);
		dChat.add(jspc, BorderLayout.SOUTH);

		JPanel content = new JPanel(new BorderLayout());
		content.add(jspi, BorderLayout.CENTER);
		content.add(dChat, BorderLayout.SOUTH);
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

		jf.setJMenuBar(jmb);

		jf.setVisible(true);
	}

	public JFrame getMidiImport() {
		JFrame e = new JFrame(
				"ProjektionTV Midi Emulator v" + Main.MAJOR_VERSION + "." + Main.MINOR_VERSION + " .mid Convertierer");
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
		JFrame e = new JFrame(
				"ProjektionTV Midi Emulator v" + Main.MAJOR_VERSION + "." + Main.MINOR_VERSION + " Twitch Formatter");
		e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
