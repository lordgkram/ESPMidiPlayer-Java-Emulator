package javaMidi;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Window {

	JTextArea input;
	JTextArea chat;

	public Window() {
		JFrame jf = new JFrame("ProjektionTV Midi Emulator");
		jf.setSize(1280, 720);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		input = new JTextArea(5, 500);
		input.setEditable(true);
		JScrollPane jspi = new JScrollPane(input);

		chat = new JTextArea(10, 50);
		chat.setEditable(false);
		JScrollPane jspc = new JScrollPane(chat);

		JPanel settings = new JPanel(new GridLayout(1, 0));

		JTextField name = new JTextField(10);
		JButton send = new JButton("Spiele");
		JSpinner maxBuffer = new JSpinner();
		maxBuffer.setValue(1000);
		JSpinner maxPlayTime = new JSpinner();
		maxPlayTime.setValue(16);
		JSpinner prio = new JSpinner();
		prio.setValue(0);
		JCheckBox doBuffer = new JCheckBox("Aktivire Buffer");
		
		JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);

		settings.add(new JLabel("name:"));
		settings.add(name);
		settings.add(new JLabel("maximale Spieleit:"));
		settings.add(maxPlayTime);
		settings.add(new JLabel("maximale Puffer groesse:"));
		settings.add(maxBuffer);
		settings.add(new JLabel("Puffer prioritaet:"));
		settings.add(prio);
		settings.add(doBuffer);
		settings.add(send);

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String head = "{\"aktiviereBuffer\":" + (doBuffer.isSelected() ? "true" : "false") + ",\"laenge\":" + String.valueOf(maxPlayTime.getValue()) + ",";
				head += "\"maximaleBufferGroesse\":" + String.valueOf(maxBuffer.getValue()) + ",\"prioritaet\":" + String.valueOf(prio.getValue()) + ",\"nutzer\":\"";
				head += name.getText() + "\",\"midi\":\"";
				String suffix = "\"}";
				String[] toPlay = input.getText().split("\n");
				progress.setMaximum(toPlay.length);
				for(int i = 0; i < toPlay.length; i++) {
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

		jf.setVisible(true);
	}

}
