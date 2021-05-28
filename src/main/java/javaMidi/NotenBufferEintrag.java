package javaMidi;

public class NotenBufferEintrag {
	
	public short priority;
	public String besitzer;
	public String daten;
	public int maximaleLaenge;
	
	public NotenBufferEintrag() {
		priority = 0;
		besitzer = "";
		daten = "";
		maximaleLaenge = 0;
	}

}
