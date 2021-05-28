package javaMidi.cppconv;

import javaMidi.GetNoteIDReturn;
import javaMidi.ReadHalbtonReturn;
import javaMidi.ReadNumberReturn;
import javaMidi.ReadOktaveOffsetReturn;

public class StrMidiUtil {
    
    public static boolean isNumber(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
        }
        return false;
    }

    public static ReadNumberReturn readNumber(String s) {
        int curr = 0;
        while (s.length() > 0 && isNumber(s.charAt(0))) {
            curr *= 10;
            curr += s.charAt(0) - '0'; // get numerical value of number
            s = s.substring(1);
        }
        return new ReadNumberReturn(curr, s);
    }

    public static ReadOktaveOffsetReturn readOktaveOffset(String s) {
        short offset = 0;
        for (short i = 0; i < 3; i++) {
            if (s.length() > 0 && s.charAt(0) == '\'') {
                offset++;
                s = s.substring(1);
            }
        }
        return new ReadOktaveOffsetReturn(offset, s);
    }

    public static ReadHalbtonReturn readHalbton(String s) {
        boolean halbtonC;
        boolean halbtonB;
        halbtonC = (s.length() > 0 && s.charAt(0) == '#');
        if (halbtonC)
            s = s.substring(1);
        halbtonB = (s.length() > 0 && s.charAt(0) == 'b');
        if (halbtonB)
            s = s.substring(1);
        return new ReadHalbtonReturn(halbtonC, halbtonB, s);
    }

    public static GetNoteIDReturn getNoteID(char note) {
        boolean allowHabtonC = false;
        boolean allowHabtonB = false;
        boolean noteDown = false;
        short noteID = 2000;
        switch (note) {
            case 'C':
                noteDown = true;
                allowHabtonB = false;
                allowHabtonC = true;
                noteID = 48;
                break;
            case 'D':
                noteDown = true;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 50;
                break;
            case 'E':
                noteDown = true;
                allowHabtonB = true;
                allowHabtonC = false;
                noteID = 52;
                break;
            case 'F':
                noteDown = true;
                allowHabtonB = false;
                allowHabtonC = true;
                noteID = 53;
                break;
            case 'G':
                noteDown = true;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 55;
                break;
            case 'A':
                noteDown = true;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 57;
                break;
            case 'H':
                noteDown = true;
                allowHabtonB = true;
                allowHabtonC = false;
                noteID = 59;
                break;

            case 'c':
                noteDown = false;
                allowHabtonB = false;
                allowHabtonC = true;
                noteID = 60;
                break;
            case 'd':
                noteDown = false;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 62;
                break;
            case 'e':
                noteDown = false;
                allowHabtonB = true;
                allowHabtonC = false;
                noteID = 64;
                break;
            case 'f':
                noteDown = false;
                allowHabtonB = false;
                allowHabtonC = true;
                noteID = 65;
                break;
            case 'g':
                noteDown = false;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 67;
                break;
            case 'a':
                noteDown = false;
                allowHabtonB = true;
                allowHabtonC = true;
                noteID = 69;
                break;
            case 'h':
                noteDown = false;
                allowHabtonB = true;
                allowHabtonC = false;
                noteID = 71;
                break;
        }
        return new GetNoteIDReturn(noteID, allowHabtonC, allowHabtonB, noteDown);
    }

    public static short convertNote(short noteId, short oktavenOffset, boolean habtonC, boolean habtonB,
            boolean allowHabtonC, boolean allowHabtonB, boolean noteDown) {
        return (short) (noteId + (oktavenOffset * 12 * (noteDown ? -1 : 1)) + (allowHabtonC && habtonC ? 1 : 0)
                - (allowHabtonB && habtonB ? 1 : 0));
    }

    public static String deleteSpace(String s) {
        while (s.length() > 0 && s.charAt(0) == ' ') {
            s = s.substring(1);
        }
        return s;
    }

}
