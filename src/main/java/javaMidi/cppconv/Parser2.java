package javaMidi.cppconv;

import javaMidi.GetNoteIDReturn;
import javaMidi.JavaMain;
import javaMidi.MIDI;
import javaMidi.ReadHalbtonReturn;
import javaMidi.ReadNumberReturn;
import javaMidi.ReadOktaveOffsetReturn;

public class Parser2 {
    public static void parser2(String buffer) {
        System.out.printf("Parser2: %s\n", buffer);

        if (System.currentTimeMillis() > JavaMain.main.timeout)
            return;

        if(buffer == "")
            return;

        if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
            ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
            buffer = rnr.outPut;
            int length = rnr.number;
            if (length == 0)
                length = 4;
            JavaMain.delay(JavaMain.main.vierBeatZeit / length);
            if (buffer.length() > 0 && buffer.charAt(0) == '.') {
                JavaMain.delay((JavaMain.main.vierBeatZeit / length) / 2);
                buffer = buffer.substring(1);
            }
            if (buffer.length() != 0)
                parser2(buffer);
        } else {
            char note = buffer.charAt(0);
            buffer = buffer.substring(1);
            if (note == 's' || note == 'S') {
                MidiInterface.parser2allOFF();
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'l' || note == 'L') {
                if (JavaMain.main.zuletztGenannteNote != 2000) {
                    MidiInterface.parser2note(JavaMain.main.zuletztGenannteNote);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'q' || note == 'Q') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    JavaMain.main.vierBeatZeit = (int) ((float) (240000) / ((float) nv));
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'v' || note == 'V') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    if (nv < 128 && nv >= 0)
                        MIDI.sendControlChange((short) 7, nv, JavaMain.main.currentChanal);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'x' || note == 'X') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    if (nv < 128 && nv >= 0)
                        MIDI.sendControlChange((short) 0, nv, JavaMain.main.currentChanal);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'y' || note == 'Y') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    if (nv < 128 && nv >= 0)
                        MIDI.sendControlChange((short) 32, nv, JavaMain.main.currentChanal);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'j' || note == 'J') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    if (nv < 128 && nv >= 0)
                        MIDI.sendControlChange((short) 72, nv, JavaMain.main.currentChanal);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'o' || note == 'O') {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nv = (short) rnr.number;
                    if (nv < 128 && nv >= 0)
                        MIDI.sendControlChange((short) 73, nv, JavaMain.main.currentChanal);
                }
                if (buffer.length() != 0)
                    parser2(buffer);
            } else if (note == 'i' || note == 'I') {
                buffer = SongUtil.readInstrument(buffer);
                if (buffer.length() != 0)
                    parser2(buffer);
            }

            else if (JavaMain.ALLOW_MULTI_CHANAL_MIDI && (note == 'k' || note == 'K')) {
                if (buffer.length() > 0 && StrMidiUtil.isNumber(buffer.charAt(0))) {
                    ReadNumberReturn rnr = StrMidiUtil.readNumber(buffer);
                    buffer = rnr.outPut;
                    short nc = (short) rnr.number;
                    JavaMain.main.currentChanal = nc;
                    if (buffer.length() != 0)
                        parser2(buffer);
                } else if (buffer.length() != 0)
                    parser2(buffer);
            }

            else {
                ReadOktaveOffsetReturn roo = StrMidiUtil.readOktaveOffset(buffer);
                buffer = roo.str;
                short oktaveOffset = roo.num;
                ReadHalbtonReturn rhr = StrMidiUtil.readHalbton(buffer);
                boolean habtonC = rhr.habltonC;
                boolean habtonB = rhr.halbtonB;
                buffer = rhr.str;
                GetNoteIDReturn gnir = StrMidiUtil.getNoteID(note);
                short noteID = gnir.note;
                boolean noteDown = gnir.noteDown;
                boolean allowHabtonC = gnir.allowHalbtonC;
                boolean allowHabtonB = gnir.allowHalbtonB;
                if (noteID != 2000)
                    MidiInterface.parser2note(StrMidiUtil.convertNote(noteID, oktaveOffset, habtonC, habtonB,
                            allowHabtonC, allowHabtonB, noteDown));
                if (buffer.length() != 0)
                    parser2(buffer);
            }
        }
    }
}
