package javaMidi.cppconv;

import javaMidi.JavaMain;
import javaMidi.MIDI;
import javaMidi.ReadNumberReturn;

public class SongUtil {

    public static String readInstrument(String s) {
        if (s.length() > 0 && StrMidiUtil.isNumber(s.charAt(0))) {
            ReadNumberReturn rnr = StrMidiUtil.readNumber(s);
            s = rnr.outPut;
            short midiInstrument = (short) rnr.number;
            if (midiInstrument < 128)
                MIDI.sendProgramChange(midiInstrument, JavaMain.main.currentChanal);
        }

        for (int i = 0; i < JavaMain.MENGE_PRESET_INSTRUMENTE; i++) {
            if (s.startsWith(JavaMain.main.instrumente[i].name)) {
                s = s.substring(JavaMain.main.instrumente[i].name.length());
                MIDI.sendProgramChange(JavaMain.main.instrumente[i].instrument, JavaMain.main.currentChanal);
                MIDI.sendControlChange((short) 0, JavaMain.main.instrumente[i].bank_MSB, JavaMain.main.currentChanal); // MSB
                MIDI.sendControlChange((short) 32, JavaMain.main.instrumente[i].bank_LSB, JavaMain.main.currentChanal); // LSB
            }
        }
        return s;
    }

    public static String expandLoops(String input) {
        boolean inLoop = false;
        String out = "";
        String buff = "";
        boolean rec = false;
        while (input.length() > 0) {
            char loc = input.charAt(0);
            input = input.substring(1);
            if (inLoop) {
                switch (loc) {
                    case 'w':
                    case 'W':
                        inLoop = false;
                        out += buff;
                        buff = "";
                        break;
                    case 'u':
                    case 'U':
                        rec = false;
                        out += buff;
                        break;
                    case 'n':
                    case 'N':
                        rec = false;
                        break;

                    default:
                        out += loc;
                        if (rec)
                            buff += loc;
                        break;
                }
            } else {
                switch (loc) {
                    case 'w':
                    case 'W':
                        inLoop = true;
                        buff = "";
                        rec = true;
                        break;

                    default:
                        out += loc;
                        break;
                }
            }
        }
        return out;
    }

    public static void parserT(String buffer) {
        if (buffer.length() > 0 && (buffer.charAt(0) == 'm' || buffer.charAt(0) == 'M')) {
            buffer = buffer.substring(1);
            if (JavaMain.ALLOW_PARSER_2) {
                JavaMain.main.parserV2 = !JavaMain.main.parserV2;
            }
        }
        if (JavaMain.main.parserV2) {
            Parser2.parser2(buffer);
        } else {
            if (JavaMain.ENABLE_PARSER_1_1) {
                Parser1.parser1_1(buffer);
                MidiInterface.parser2allOFF();
            } else {
                Parser1.parser(buffer);
            }
        }
    }

}
