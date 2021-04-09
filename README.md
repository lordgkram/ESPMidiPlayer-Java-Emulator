# ESP32MidiPlayer Java Emulator

Emulator für [ESP32MidiPlayer](https://github.com/ProjektionTV/Esp32MidiPlayer)

## Bauen

### Windows
`gradlew.bat getDeps build`
### Linux
`gradlew getDeps build`

## Ausführen
Zum Ausführen werden die 3 jar files in `build/libs` benötigt sowie eine sountfont im gleichen Ordner wie die 3 jar-Dateien namens `sf.sf2` zum Beispiel [diese](https://member.keymusician.com/Member/FluidR3_GM/FluidR3_GM.zip) 

Um den Mqtt-Player Modus zu aktivieren muss man das Argument `-mqtt` anhängen. z.B.: `java -jar javaMidi.jar -mqtt`

## UI Erklärung

* Die oberste Leiste sind die Einstellungen für das [JSON](https://github.com/ProjektionTV/Esp32MidiPlayer#json).
* Das Textfeld sind die Noten, ein Zeilenumbruch bedeutet, dass das folgende sich in einem neuen `playmidi` Befehl befindet.
* Das untere Feld ist das was in den Twitch-chat geschrieben werden würde.

## Noten Erklärung
[hier](https://github.com/ProjektionTV/Esp32MidiPlayer#playmidi-syntax)
