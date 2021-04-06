package javaMidi;

public class NotenBufferEintrag {
	
	short priority;
	String besitzer;
	String daten;
	int maximaleLaenge;
	
	public NotenBufferEintrag() {
		priority = 0;
		besitzer = "";
		daten = "";
		maximaleLaenge = 0;
	}

}
