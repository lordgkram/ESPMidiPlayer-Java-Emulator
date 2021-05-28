package javaMidi.cppconv;

import com.google.gson.JsonObject;

import javaMidi.JavaMain;
import javaMidi.classC.PsClient;

public class Interface {

  public static void schreibeChatNachricht(String s) {
    if (s.length() > 500) {
      while (s.length() > 500) {
        PsClient.publish(JavaMain.MQTT_IRC_TX, s.substring(0, 500));
        s = s.substring(500);
        if (s.length() > 0) {
          PsClient.publish(JavaMain.MQTT_IRC_TX, s);
        }
      }
    } else
      PsClient.publish(JavaMain.MQTT_IRC_TX, s);
  }

  public static void setMusicStatus(boolean newStatus) {
    if (newStatus)
      PsClient.publish(JavaMain.MQTT_MUSIC_ON_TOPIC, JavaMain.MQTT_MUSIC_ON_MASSAGE);
    else
      PsClient.publish(JavaMain.MQTT_MUSIC_OFF_TOPIC, JavaMain.MQTT_MUSIC_OFF_MASSAGE);
  }

  public static void mqttCallback(String topic, String payload) {
    System.out.print("Message arrived [");
    System.out.print(topic);
    System.out.print("] ");
    System.out.println(payload);

    String strTopic = topic;
    if (strTopic.equals(JavaMain.TOPIC_MIDI)) {
      System.out.println("play midi vom mqtt erkannt");
      // rueckwertz kompatiblitaet
      if (payload.charAt(0) != '{') {
        Song.playSong((String) payload, 16);
        return;
      }
      // JSON MIDI
      JsonObject data = JavaMain.main.gson.fromJson(payload, JsonObject.class);
      if (data.has("adminModus") && data.get("adminModus").getAsBoolean()) {
        String midi = data.has("midi") ? data.get("midi").getAsString() : "";
        String nutzer = data.has("nutzer") ? data.get("nutzer").getAsString() : "";
        Admin.parseAdminCommand(midi, nutzer);
        return;
      }
      if (data.has("aktiviereBuffer") && data.get("aktiviereBuffer").getAsBoolean()) {
        String midi = data.has("midi") ? data.get("midi").getAsString() : "";
        String nutzer = data.has("nutzer") ? data.get("nutzer").getAsString() : "";
        // buffer funktionen
        if (midi.startsWith(";")) {
          midi = midi.substring(1);
          // loesche buffer aktion
          if (midi.startsWith("l")) {
            midi = midi.substring(1);
            boolean wurdeGeloescht = false;
            for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
              if (nutzer.equalsIgnoreCase(JavaMain.main.notenBuffer[i].besitzer)) {
                JavaMain.main.notenBuffer[i].besitzer = "";
                JavaMain.main.notenBuffer[i].daten = "";
                JavaMain.main.notenBuffer[i].maximaleLaenge = 0;
                JavaMain.main.notenBuffer[i].priority = 0;
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
          int bufferID = 0;
          for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
            if (nutzer.equalsIgnoreCase(JavaMain.main.notenBuffer[i].besitzer)) {
              benutzerBufferGefunden = true;
              bufferID = i;
            }
          }
          if (benutzerBufferGefunden) {
            // daten zum buffer hinzu fügen
            if (JavaMain.main.notenBuffer[bufferID].daten
                .length() == JavaMain.main.notenBuffer[bufferID].maximaleLaenge) {
              schreibeChatNachricht("(MIDI) @" + nutzer + " dein Puffer ist Voll!");
            } else if ((JavaMain.main.notenBuffer[bufferID].daten.length()
                + midi.length()) > JavaMain.main.notenBuffer[bufferID].maximaleLaenge) {
              JavaMain.main.notenBuffer[bufferID].daten = JavaMain.main.notenBuffer[bufferID].daten + midi + " ";
              JavaMain.main.notenBuffer[bufferID].daten = JavaMain.main.notenBuffer[bufferID].daten.substring(0,
                  JavaMain.main.notenBuffer[bufferID].maximaleLaenge);
              schreibeChatNachricht("(MIDI) @" + nutzer
                  + " daten wurden zu deinem Puffer hinzugefügt. Achtung es wurden Daten entfernt da der puffer überfüllt wurde ("
                  + JavaMain.main.notenBuffer[bufferID].daten.length() + "/"
                  + JavaMain.main.notenBuffer[bufferID].maximaleLaenge + ").");
            } else {
              JavaMain.main.notenBuffer[bufferID].daten = JavaMain.main.notenBuffer[bufferID].daten + midi + " ";
              schreibeChatNachricht("(MIDI) @" + nutzer + " daten wurden zu deinem Puffer hinzugefügt ("
                  + JavaMain.main.notenBuffer[bufferID].daten.length() + "/"
                  + JavaMain.main.notenBuffer[bufferID].maximaleLaenge + ").");
            }
          } else {
            if (midi.startsWith("n")) {
              midi = midi.substring(1);
              short prioritaet = data.has("prioritaet") ? data.get("prioritaet").getAsShort() : 0;
              // erschaffe neuen buffer
              bufferID = 0;
              boolean erschaffeBuffer = false;
              boolean ueberSchreibeBuffer = true;
              for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
                if (JavaMain.main.notenBuffer[i].besitzer.equalsIgnoreCase("") && ueberSchreibeBuffer) {
                  // erschaffe neuen buffer
                  ueberSchreibeBuffer = false;
                  erschaffeBuffer = true;
                  bufferID = i;
                }
              }
              if (ueberSchreibeBuffer) {
                for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
                  if (JavaMain.main.notenBuffer[i].priority < prioritaet && !(ueberSchreibeBuffer)) {
                    // erschaffe neuen buffer
                    ueberSchreibeBuffer = true;
                    erschaffeBuffer = true;
                    bufferID = i;
                  }
                }
              }
              if (erschaffeBuffer) {
                int maximaleBufferGroesse = data.has("maximaleBufferGroesse")
                    ? data.get("maximaleBufferGroesse").getAsInt()
                    : 0;
                JavaMain.main.notenBuffer[bufferID].besitzer = nutzer;
                JavaMain.main.notenBuffer[bufferID].priority = prioritaet;
                JavaMain.main.notenBuffer[bufferID].maximaleLaenge = maximaleBufferGroesse;
                JavaMain.main.notenBuffer[bufferID].daten = midi + " ";
                if (JavaMain.main.notenBuffer[bufferID].daten
                    .length() > JavaMain.main.notenBuffer[bufferID].maximaleLaenge) {
                  JavaMain.main.notenBuffer[bufferID].daten = JavaMain.main.notenBuffer[bufferID].daten.substring(0,
                      JavaMain.main.notenBuffer[bufferID].maximaleLaenge);
                }
                schreibeChatNachricht("(MIDI) @" + nutzer + " puffer wurde erfolgreich erschaffen ("
                    + JavaMain.main.notenBuffer[bufferID].daten.length() + "/"
                    + JavaMain.main.notenBuffer[bufferID].maximaleLaenge + ").");
              } else {
                schreibeChatNachricht("(MIDI) @" + nutzer + " puffer konte nicht erschaffen werden.");
              }
            } else {
              // spiele daten
              Song.playSong(midi, data.has("laenge") ? data.get("laenge").getAsInt() : 0);
            }
          }
        } else {
          boolean benutzerBufferGefunden = false;
          for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
            if (nutzer.equalsIgnoreCase(JavaMain.main.notenBuffer[i].besitzer)) {
              benutzerBufferGefunden = true;
              Song.playSong(JavaMain.main.notenBuffer[i].daten + midi,
                  data.has("laenge") ? data.get("laenge").getAsInt() : 0);
              JavaMain.main.notenBuffer[i].besitzer = "";
              JavaMain.main.notenBuffer[i].daten = "";
              JavaMain.main.notenBuffer[i].maximaleLaenge = 0;
              JavaMain.main.notenBuffer[i].priority = 0;
            }
          }
          if (!benutzerBufferGefunden)
            Song.playSong(midi, data.has("laenge") ? data.get("laenge").getAsInt() : 0);
        }
      } else {
        Song.playSong(data.has("midi") ? data.get("midi").getAsString() : "",
            data.has("laenge") ? data.get("laenge").getAsInt() : 0);
      }
    }

  }
}
