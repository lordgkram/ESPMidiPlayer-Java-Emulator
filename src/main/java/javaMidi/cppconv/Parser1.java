package javaMidi.cppconv;

import javaMidi.GetNoteIDReturn;
import javaMidi.JavaMain;
import javaMidi.ReadHalbtonReturn;
import javaMidi.ReadNumberReturn;
import javaMidi.ReadOktaveOffsetReturn;

public class Parser1 {
    public static void parser(String buffer) {
        // C', C, c, c'
        System.out.printf("Parser: %s\n", buffer);

        char note = buffer.charAt(0);
        buffer = buffer.substring(1);

        short oktaveOffset = 0;

        if (buffer.length() > 0 && buffer.charAt(0) == '\'') {
            oktaveOffset++;
            buffer = buffer.substring(1);
        }

        if (buffer.length() > 0 && buffer.charAt(0) == '\'') {
            oktaveOffset++;
            buffer = buffer.substring(1);
        }

        if (buffer.length() > 0 && buffer.charAt(0) == '\'') {
            oktaveOffset++;
            buffer = buffer.substring(1);
        }

        boolean istHalbton = false;

        if (buffer.length() > 0 && buffer.charAt(0) == '#') {
            istHalbton = true;
            buffer = buffer.substring(1);
        }

        int length = 4;
        try {
            length = Integer.parseInt(buffer);
        } catch (Exception e) {
        }

        if (length == 0)
            length = 4;

        switch (note) {
            case 'P':
            case 'p':
                JavaMain.delay(1000 / length);
                break;
            case 'C':
                if (!istHalbton)
                    MidiInterface.playNote(48 - (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(49 - (oktaveOffset * 12), length);
                break;
            case 'D':
                if (!istHalbton)
                    MidiInterface.playNote(50 - (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(51 - (oktaveOffset * 12), length);
                break;
            case 'E':
                MidiInterface.playNote(52 - (oktaveOffset * 12), length);
                break;
            case 'F':
                if (!istHalbton)
                    MidiInterface.playNote(53 - (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(54 - (oktaveOffset * 12), length);
                break;
            case 'G':
                if (!istHalbton)
                    MidiInterface.playNote(55 - (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(56 - (oktaveOffset * 12), length);
                break;
            case 'A':
                if (!istHalbton)
                    MidiInterface.playNote(57 - (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(58 - (oktaveOffset * 12), length);
                break;
            case 'H':
                MidiInterface.playNote(59 - (oktaveOffset * 12), length);
                break;

            case 'c':
                if (!istHalbton)
                    MidiInterface.playNote(60 + (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(61 + (oktaveOffset * 12), length);
                break;
            case 'd':
                if (!istHalbton)
                    MidiInterface.playNote(62 + (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(63 + (oktaveOffset * 12), length);
                break;
            case 'e':
                MidiInterface.playNote(64 + (oktaveOffset * 12), length);
                break;
            case 'f':
                if (!istHalbton)
                    MidiInterface.playNote(65 + (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(66 + (oktaveOffset * 12), length);
                break;
            case 'g':
                if (!istHalbton)
                    MidiInterface.playNote(67 + (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(68 + (oktaveOffset * 12), length);
                break;
            case 'a':
                if (!istHalbton)
                    MidiInterface.playNote(69 + (oktaveOffset * 12), length);
                else
                    MidiInterface.playNote(70 + (oktaveOffset * 12), length);
                break;
            case 'h':
                MidiInterface.playNote(71 + (oktaveOffset * 12), length);
                break;
        }
    }

    public static void parser1_1(String buffer) {
        System.out.println("Parser1.1: " + buffer);

        if (System.currentTimeMillis() > JavaMain.main.timeout)
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
            return;
        }

        if(buffer == "")
            return;

        char note = buffer.charAt(0);
        buffer = buffer.substring(1);

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
        boolean play = true;
        if (noteID == 2000)
            play = false;
        if (note == 'p' || note == 'P')
            play = false;
        if (play)
            MidiInterface.parser2note(StrMidiUtil.convertNote(noteID, oktaveOffset, habtonC, habtonB, allowHabtonC,
                    allowHabtonB, noteDown));
        if (buffer.length() != 0)
            parser1_1(buffer);
        else {
            if (play || note == 'p' || note == 'P')
                JavaMain.delay(JavaMain.main.vierBeatZeit / 4);
        }
    }
}
