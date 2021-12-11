package javaMidi.tcpmode;

public class TcpMidiData {
    private String midi;
    private int laenge;

    public TcpMidiData(String midi, int laenge) {
        this.midi = midi;
        this.laenge = laenge;
    }

    public String getMidi() {
        return midi;
    }

    public int getLaenge() {
        return laenge;
    }
}
