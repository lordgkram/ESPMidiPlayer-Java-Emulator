package javaMidi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.IntelliJTheme;
import com.google.gson.JsonObject;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class Theme {

    public static String BASE_SYNTAX_TOKEN = "\t\t<style token=\"$NAME\" fg=\"$FG\"$BG bold=\"$BL\" italic=\"$IT\" underline=\"$UL\" />\n";
    public static String BASE_SYNTAX_TOKEN_BG = " bg=\"$BG\"";
    private String syntaxTheme;
    private org.fife.ui.rsyntaxtextarea.Theme syntaxThemeCompiled;

    HashMap<Integer, String[]> syntaxTokeMap;
    HashMap<String, Integer> syntaxTokenNamesMap;
    HashMap<String, JsonObject> syntaxTokenDataMap;
    ArrayList<RSyntaxTextArea> syntaxAreasToHandel;

    HashMap<String, String> changeLogColors;
    HashMap<JEditorPane, String> changeLogsToHandel;

    ArrayList<JFrame> uisToHandle;

    public Theme(){
        syntaxTokenNamesMap = new HashMap<>();
        syntaxTokeMap = new HashMap<>();
        syntaxTokenDataMap = new HashMap<>();
        syntaxAreasToHandel = new ArrayList<>();
        changeLogColors = new HashMap<>();
        changeLogsToHandel = new HashMap<>();
        uisToHandle = new ArrayList<>();
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_MODUS, new String[]{"FUNCTION"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_WIEDERHOLUNG, new String[]{"OPERATOR"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_NOTE, new String[]{"RESERVED_WORD"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_LENGE, new String[]{"LITERAL_NUMBER_DECIMAL_INT"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_BPM, new String[]{"VARIABLE"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_CONTROLL, new String[]{"REGEX"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_PRESET, new String[]{"ANNOTATION"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_INSTRUMENT, new String[]{"RESERVED_WORD_2"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_BUFFER, new String[]{"DATA_TYPE"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_ERROR, new String[]{"COMMENT_MULTILINE"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_ERROR_EOL, new String[]{"COMMENT_EOL"});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_NULL, new String[]{});
        syntaxTokeMap.put(MidiSyntaxHighlighter.TOKEN_NONE, new String[]{"WHITESPACE"});
        syntaxTokenNamesMap.put("TOKEN_MODUS", MidiSyntaxHighlighter.TOKEN_MODUS);
        syntaxTokenNamesMap.put("TOKEN_WIEDERHOLUNG", MidiSyntaxHighlighter.TOKEN_WIEDERHOLUNG);
        syntaxTokenNamesMap.put("TOKEN_NOTE", MidiSyntaxHighlighter.TOKEN_NOTE);
        syntaxTokenNamesMap.put("TOKEN_LENGE", MidiSyntaxHighlighter.TOKEN_LENGE);
        syntaxTokenNamesMap.put("TOKEN_BPM", MidiSyntaxHighlighter.TOKEN_BPM);
        syntaxTokenNamesMap.put("TOKEN_CONTROLL", MidiSyntaxHighlighter.TOKEN_CONTROLL);
        syntaxTokenNamesMap.put("TOKEN_PRESET", MidiSyntaxHighlighter.TOKEN_PRESET);
        syntaxTokenNamesMap.put("TOKEN_INSTRUMENT", MidiSyntaxHighlighter.TOKEN_INSTRUMENT);
        syntaxTokenNamesMap.put("TOKEN_BUFFER", MidiSyntaxHighlighter.TOKEN_BUFFER);
        syntaxTokenNamesMap.put("TOKEN_ERROR", MidiSyntaxHighlighter.TOKEN_ERROR);
        syntaxTokenNamesMap.put("TOKEN_ERROR_EOL", MidiSyntaxHighlighter.TOKEN_ERROR_EOL);
        syntaxTokenNamesMap.put("TOKEN_NONE", MidiSyntaxHighlighter.TOKEN_NONE);
    }

    public boolean loadExternal(String name){
        try{
            load(JavaMain.main.gson.fromJson(new BufferedReader(new InputStreamReader(new FileInputStream(name), "utf-8")), JsonObject.class));
            return true;
        } catch(UnsupportedEncodingException | FileNotFoundException e) { return false; }
    }

    public boolean loadInternal(String name){
        try{
            load(JavaMain.main.gson.fromJson(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/themes/" + name + ".json"), "utf-8")), JsonObject.class));
            return true;
        } catch(UnsupportedEncodingException e) { return false; }
    }

    public void load(JsonObject o){
        syntaxTokenDataMap.clear();
        loadSyntax(o.has("syntax") ? o.get("syntax").getAsJsonObject() : new JsonObject());
        loadChangeLog(o.has("changeLog") ? o.get("changeLog").getAsJsonObject() : new JsonObject());
        loadSwing(o.has("swing") ? o.get("swing").getAsJsonObject() : new JsonObject());
    }

    private void loadChangeLog(JsonObject o){
        // create theme
        changeLogColors.clear();
        changeLogColors.put("$FG", o.has("fg") ? o.get("fg").getAsString() : "000000");
        changeLogColors.put("$BG", o.has("bg") ? o.get("bg").getAsString() : "FFFFFF");
        changeLogColors.put("$PR", o.has("person") ? o.get("person").getAsString() : "00DDDD");
        changeLogColors.put("$BF", o.has("bugfix") ? o.get("bugfix").getAsString() : "AA0000");
        changeLogColors.put("$ST", "style=\"color: #$FG; background-color: #$BG;\"");
        // apply theme
        for(JEditorPane p : changeLogsToHandel.keySet())
            applyChangeLog(p, changeLogsToHandel.get(p));
    }

    public void addChangeLogToHandel(JEditorPane p, String text){
        applyChangeLog(p, text);
        changeLogsToHandel.put(p, text);
    }

    public void applyChangeLog(JEditorPane p, String text){
        for(int i = 0; i < 2; i++)
            for(String s : changeLogColors.keySet()){
                text = text.replace(s, changeLogColors.get(s));
            }
        p.setText(text);
        p.revalidate();
        p.repaint();
    }

    private void loadSwing(JsonObject o){
        try{
            int t = o.has("style") ? o.get("style").getAsInt() : 0;
            String clazz = "com.formdev.flatlaf.FlatLightLaf";
            LookAndFeel laf = null;
            switch(t){
                case 0:
                    clazz = "com.formdev.flatlaf.FlatLightLaf";
                    break;
                case 1:
                    clazz = "com.formdev.flatlaf.FlatDarkLaf";
                    break;
                case 2:
                    clazz = "com.formdev.flatlaf.FlatIntelliJLaf";
                    break;
                case 3:
                    clazz = "com.formdev.flatlaf.FlatDarculaLaf";
                    break;
                case 4:
                    if(o.has("clazz")) clazz = o.get("clazz").getAsString();
                    break;
                case 5:
                    if(o.has("theme")) laf = IntelliJTheme.createLaf(new ByteArrayInputStream(JavaMain.main.gson.toJson(o.get("theme")).getBytes("utf-8")));
                    break;
            }
            if(laf == null)
                UIManager.setLookAndFeel(clazz);
            else
                UIManager.setLookAndFeel(laf);
        } catch(ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | IOException e){
        }
        // update ui's
        for(JFrame f : uisToHandle)
            if(f != null)
                applyUi(f);
            else
                uisToHandle.remove(f);
    }

    public void addUiToHandle(JFrame f){
        applyUi(f);
        uisToHandle.add(f);
    }

    public void applyUi(JFrame f){
        SwingUtilities.updateComponentTreeUI(f);
    }

    private void loadSyntax(JsonObject o){
        if(o.has("token")){
            JsonObject o2 = o.get("token").getAsJsonObject();
            for(String s : o2.keySet()){
                syntaxTokenDataMap.put("TOKEN_" + s.toUpperCase(), o2.get(s).getAsJsonObject());
            }
        }
        // create Theme
        syntaxTheme = "";
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/syntaxTheme.xml"), "utf-8"));
			String line;
			while ((line = br.readLine()) != null){
				syntaxTheme += line + System.lineSeparator();
			}
		}catch(Exception e){
		}
        String datas = "";
        for(String s : syntaxTokenNamesMap.keySet()){
            String[] types = syntaxTokeMap.get(syntaxTokenNamesMap.get(s));
            JsonObject o2 = syntaxTokenDataMap.get(s);
            if(o2 == null)
                o2 = new JsonObject();
            String base = BASE_SYNTAX_TOKEN
                .replace("$FG", o2.has("fg") ? o2.get("fg").getAsString() : "000000")
                .replace("$BG", o2.has("bg") ? BASE_SYNTAX_TOKEN_BG.replace("$BG", o2.get("bg").getAsString()) : "")
                .replace("$BL", (o2.has("bold") && o2.get("bold").getAsBoolean()) ? "true" : "false")
                .replace("$IT", (o2.has("italic") && o2.get("italic").getAsBoolean()) ? "true" : "false")
                .replace("$UL", (o2.has("underline") && o2.get("underline").getAsBoolean()) ? "true" : "false");
            for(String s2 : types)
                datas += base.replace("$NAME", s2);
        }
        syntaxTheme = syntaxTheme.replace("$DATA", datas)
            .replace("$BG", o.has("bg") ? o.get("bg").getAsString() : "FFFFFF")
            .replace("$LINE_HL", o.has("currentLine") ? o.get("currentLine").getAsString() : "FFFF00")
            .replace("$SELECT_BG", o.has("selection") ? o.get("selection").getAsString() : "00FFFF")
            .replace("$CARET", o.has("caret") ? o.get("caret").getAsString() : "000000")
            .replace("$LINE_B", o.has("lineBorder") ? o.get("lineBorder").getAsString() : "000000")
            .replace("$LINE_N", o.has("lineNumber") ? o.get("lineNumber").getAsString() : "000000");
        // compile Theme
        try{
            syntaxThemeCompiled = org.fife.ui.rsyntaxtextarea.Theme.load(new ByteArrayInputStream(syntaxTheme.getBytes("utf-8")));
        } catch(IOException e) {
            e.printStackTrace();
        }
        // apply theme
        for(RSyntaxTextArea a : syntaxAreasToHandel)
            if(a != null)
                applySyntax(a);
            else
                syntaxAreasToHandel.remove(a);
    }

    public void addSyntaxAreaToHandel(RSyntaxTextArea a){
        applySyntax(a);
        syntaxAreasToHandel.add(a);
    }

    public void applySyntax(RSyntaxTextArea a){
        syntaxThemeCompiled.apply(a);
    }
    
}
