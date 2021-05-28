package javaMidi;

public class Instrument {
    public String name;
    public short instrument;
    public short bank_MSB;
    public short bank_LSB;

    public Instrument(){
        this.bank_LSB = 0;
        this.bank_MSB = 0;
        this.instrument = 0;
        this.name = "";
    }
}
