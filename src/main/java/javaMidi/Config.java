package javaMidi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.JsonObject;

public class Config {

    private File cfgDir;
    private File cfgUI;

    public JsonObject ui;

    public Config(){
        cfgDir = new File(System.getProperty("user.home"), ".midiPlayerEmulator");
        if(!cfgDir.exists())
            cfgDir.mkdirs();
        // UI
        cfgUI = new File(cfgDir, "uiConfig.json");
        if(!cfgUI.exists()){
            copyInternal(cfgUI, "/config/uiConfig.json");
        }
        try{
            ui = JavaMain.main.gson.fromJson(new InputStreamReader(new FileInputStream(cfgUI), "utf-8"), JsonObject.class);
        } catch(IOException e) { ui = new JsonObject(); }
        if(ui == null)
            ui = new JsonObject();
    }

    public boolean save(){
        try{
            OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(cfgUI), "utf-8");
            JavaMain.main.gson.toJson(ui, JavaMain.main.gson.newJsonWriter(os));
            os.flush();
            os.close();
            return true;
        } catch(IOException e) { return false; }
    } 

    public static boolean copyInternal(File out, String in){
        try{
            OutputStream os = new FileOutputStream(out);
            InputStream is = JavaMain.main.getClass().getResourceAsStream(in);
            byte[] buff = new byte[4096];
            int a = is.read(buff, 0, buff.length);
            os.write(buff, 0, a);
            while (a == buff.length){
                a = is.read(buff, 0, buff.length);
                os.write(buff, 0, a);
            }
            os.close();
            is.close();
            return true;
        }catch(IOException e){
            return false;
        }
    }
    
}
