# ESP32MidiPlayer Java Emulator

Emulator für [ESP32MidiPlayer](https://github.com/ProjektionTV/Esp32MidiPlayer)

## Bauen

### Windows
`gradlew.bat construct`
### Linux
`gradlew construct`

## Ausführen
Zum Ausführen werden die jar datei in `build/libs` benötigt sowie eine sountfont im gleichen Ordner wie die jar namens `sf.sf2` zum Beispiel [diese](https://member.keymusician.com/Member/FluidR3_GM/FluidR3_GM.zip) 

Info: Fals keine `sf.sf2` vorhanden ist fragt das Programm in der Konsole ob eine heruntergeladen werden soll.

### MQTT Mode
Um den Mqtt-Player Modus zu aktivieren, muss man das Argument `--mqtt` anhängen. z.B.: `java -jar javaMidi.jar --mqtt`

### TCP Mode
Um den TCP-Player Modus zu aktivieren, muss man das Argument `--tcp` anhängen. z.B.: `java -jar javaMidi.jar --tcp`

Um es unter Linux zu testen, kann z.B. der folgende Befehl genutzt werden:

`echo "a c ac" | nc 127.0.0.1 4242`

## UI Erklärung

* Die oberste Leiste sind die Einstellungen für das [JSON](https://github.com/ProjektionTV/Esp32MidiPlayer#json).
* Das Textfeld sind die Noten, ein Zeilenumbruch bedeutet, dass das folgende sich in einem neuen `playmidi` Befehl befindet.
* Das untere Feld ist das was in den Twitch-chat geschrieben werden würde.

## Noten Erklärung
[hier](https://github.com/ProjektionTV/Esp32MidiPlayer#playmidi-syntax)

## CLI
* `-a/--noteBufferLenght (zahl)` - anzahl der noten buffer
* `-b/--broker (protukoll)://(ip)[:(port)]` - protukoll und ip vom mqtt broker z.B.: `tcp://127.0.0.1`
* `-c/--topicChat (topic)` - der mqtt-topic für den Chat ausgang
* `-h/--help` - zeigt die hilfe
* `-i/--clientID (id)` - Mqtt-Client id
* `-m/--topicMidi (topic)` - der mqtt-topic für den playmidi trafic
* `-q/--mqtt` - soll mqtt als eingang genutzt werden
* `-o/--mqttOut` - soll die ui als Fernbedinung genutzt werden
* `-t/--tcp` - soll tcp als eingang genutzt werden
