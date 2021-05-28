package javaMidi.cppconv;

import javaMidi.JavaMain;
import javaMidi.ReadNumberReturn;

public class Admin {
    public static void parseAdminCommand(String command, String user) {
        command = StrMidiUtil.deleteSpace(command);
        int state = 0;
        while (command.length() != 0) {
            char c = command.charAt(0);
            command = command.substring(1);
            command = StrMidiUtil.deleteSpace(command);
            // interpret
            switch (state) {
                // base
                case 0:
                    switch (c) {
                        case 'P':
                        case 'p': {
                            int time = 16;
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                command = rnr2.outPut;
                                time = rnr2.number;
                                command = StrMidiUtil.deleteSpace(command);
                            }
                            Song.playSong(command, time);
                            return;
                        }
                        case '~':
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                int time = 16;
                                if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                    ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                    command = rnr2.outPut;
                                    time = rnr2.number;
                                    command = StrMidiUtil.deleteSpace(command);
                                }
                                Song.playSong(JavaMain.main.notenBuffer[id].daten, time);
                                JavaMain.main.notenBuffer[id].besitzer = "";
                                JavaMain.main.notenBuffer[id].daten = "";
                                JavaMain.main.notenBuffer[id].maximaleLaenge = 0;
                                JavaMain.main.notenBuffer[id].priority = 0;
                            }
                            break;
                        case 'q':
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                int time = 16;
                                if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                    ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                    command = rnr2.outPut;
                                    time = rnr2.number;
                                    command = StrMidiUtil.deleteSpace(command);
                                }
                                Song.playSong(JavaMain.main.notenBuffer[id].daten, time);
                            }
                            break;
                        case 'B':
                        case 'b':
                            state = 1;
                            break;
                        case 'I':
                        case 'i':
                            state = 2;
                            break;
                        case 'L':
                        case 'l':
                            state = 3;
                            break;
                    }
                    break;
                // buffer
                case 1:
                    switch (c) {
                        case ';':
                            state = 0;
                            break;
                        case 'L':
                        case 'l':
                            Interface.schreibeChatNachricht("(MIDI) @" + user + " Liste von Puffern:");
                            for (int i = 0; i < JavaMain.NOTEN_BUFFER_LAENGE; i++) {
                                Interface.schreibeChatNachricht(
                                        "(MIDI) @" + user + " " + i + ": " + JavaMain.main.notenBuffer[i].besitzer
                                                + " - " + JavaMain.main.notenBuffer[i].daten.length() + "/"
                                                + JavaMain.main.notenBuffer[i].maximaleLaenge);
                            }
                            break;
                        case 'O':
                        case 'o':
                            // set owner
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                String e = "";
                                while ((command.length() != 0) && (command.charAt(0) != ' ')
                                        && (command.charAt(0) != ';')) {
                                    e += "" + command.substring(0, 1);
                                    command = command.substring(1);
                                }
                                JavaMain.main.notenBuffer[id].besitzer = e;
                            }
                            break;
                        case 'D':
                        case 'd':
                            // set data
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.notenBuffer[id].daten = command;
                                return;
                            }
                            break;
                        case 'A':
                        case 'a':
                            // add data
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.notenBuffer[id].daten += command;
                                return;
                            }
                            break;
                        case 'C':
                        case 'c':
                            // clear
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.notenBuffer[id].besitzer = "";
                                JavaMain.main.notenBuffer[id].daten = "";
                                JavaMain.main.notenBuffer[id].maximaleLaenge = 0;
                                JavaMain.main.notenBuffer[id].priority = 0;
                            }
                            break;
                        case 'P':
                        case 'p':
                            // print
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                Interface.schreibeChatNachricht("(MIDI) @" + user + " Daten vom Puffer " + id + ": "
                                        + JavaMain.main.notenBuffer[id].daten);
                            }
                            break;
                    }
                    break;
                // instrument
                case 2:
                    switch (c) {
                        case ';':
                            state = 0;
                            break;
                        case 'L':
                        case 'l':
                            // list
                            Interface.schreibeChatNachricht("(MIDI) @" + user + " Liste von Instrumenten:");
                            for (int i = 0; i < JavaMain.MENGE_PRESET_INSTRUMENTE; i++) {
                                Interface.schreibeChatNachricht(
                                        "(MIDI) @" + user + " " + i + ": " + JavaMain.main.instrumente[i].name
                                                + " - instr=" + JavaMain.main.instrumente[i].instrument + ", msb="
                                                + JavaMain.main.instrumente[i].bank_MSB + ", lsb="
                                                + JavaMain.main.instrumente[i].bank_LSB);
                            }
                            break;
                        case 'N':
                        case 'n':
                            // set name
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                String e = "";
                                while ((command.length() != 0) && (command.charAt(0) != ' ')
                                        && (command.charAt(0) != ';')) {
                                    e += "" + command.substring(0, 1);
                                    command = command.substring(1);
                                }
                                JavaMain.main.instrumente[id].name = e;
                            }
                            break;
                        case 'M':
                        case 'm':
                            // set msb
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                    ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                    command = rnr2.outPut;
                                    int val = rnr2.number;
                                    command = StrMidiUtil.deleteSpace(command);
                                    JavaMain.main.instrumente[id].bank_MSB = (short) val;
                                }
                            }
                            break;
                        case 'I':
                        case 'i':
                            // set instrument
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                    ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                    command = rnr2.outPut;
                                    int val = rnr2.number;
                                    command = StrMidiUtil.deleteSpace(command);
                                    JavaMain.main.instrumente[id].instrument = (short) val;
                                }
                            }
                            break;
                        case 'D':
                        case 'd':
                            // set lsb
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                    ReadNumberReturn rnr2 = StrMidiUtil.readNumber(command);
                                    command = rnr2.outPut;
                                    int val = rnr2.number;
                                    command = StrMidiUtil.deleteSpace(command);
                                    JavaMain.main.instrumente[id].bank_LSB = (short) val;
                                }
                            }
                            break;
                    }
                    break;
                // lied
                case 3:
                    switch (c) {
                        case ';':
                            state = 0;
                            break;
                        case 'l':
                        case 'L':
                            Interface.schreibeChatNachricht("(MIDI) @" + user + " Liste von Preset Liedern:");
                            for (int i = 0; i < JavaMain.MENGE_PRESET_LIEDER; i++) {
                                Interface.schreibeChatNachricht(
                                        "(MIDI) @" + user + " " + i + ": " + JavaMain.main.presetLieder[i].name);
                            }
                            break;
                        case 'D':
                        case 'd':
                            // set data
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.presetLieder[id].daten = command;
                                return;
                            }
                            break;
                        case 'A':
                        case 'a':
                            // add data
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.presetLieder[id].daten += command;
                                return;
                            }
                            break;
                        case 'C':
                        case 'c':
                            // clear
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                JavaMain.main.presetLieder[id].daten = "";
                                JavaMain.main.presetLieder[id].name = "";
                            }
                            break;
                        case 'P':
                        case 'p':
                            // print
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                Interface.schreibeChatNachricht("(MIDI) @" + user + " Daten vom Lied " + id + ": "
                                        + JavaMain.main.presetLieder[id].daten);
                            }
                            break;
                        case 'N':
                        case 'n':
                            // set name
                            if (command.length() > 0 && StrMidiUtil.isNumber(command.charAt(0))) {
                                ReadNumberReturn rnr = StrMidiUtil.readNumber(command);
                                command = rnr.outPut;
                                int id = rnr.number;
                                command = StrMidiUtil.deleteSpace(command);
                                String e = "";
                                while ((command.length() != 0) && (command.charAt(0) != ' ')
                                        && (command.charAt(0) != ';')) {
                                    e += "" + command.substring(0, 1);
                                    command = command.substring(1);
                                }
                                JavaMain.main.presetLieder[id].name = e;
                            }
                            break;
                    }
                    break;
                // end
            }
            command = StrMidiUtil.deleteSpace(command);
        }
    }

}
