# ESP32MidiPlayer Java Emulator

Emulator für [ESP32MidiPlayer](https://github.com/ProjektionTV/Esp32MidiPlayer)

## Bauen

### Windows
`gradlew.bat getDeps build`
### Linux
`gradlew getDeps build`

## Ausfürn
Zum ausfürn werden die 3 jar files in `build/libs` benötigt sowie eine sountfont im gleichen ordner wie die 3 jar-Dateien namens `sf.sf2` zum beispiel [diese](https://member.keymusician.com/Member/FluidR3_GM/FluidR3_GM.zip)

Um den Mqtt-Player modus zu aktivieren muss man das argument `-mqtt` anhängen. z.B.: `java -jar javaMidi.jar -mqtt`

## UI erklärung

* Die Oberste Leiste sind die Einstelleungen für das [JSON](https://github.com/ProjektionTV/Esp32MidiPlayer#json).
* Das Textfeld sind die Noten ein Zeilenumbruch bedeutet das die sin einem neuen `playmidi` befehl befindet.
* Das Untere feld ist das was In den Twitch chat geschriben werden würde.

## Noten Erklärung
[hier](https://github.com/ProjektionTV/Esp32MidiPlayer#playmidi-syntax)
