package javaMidi;

public class GetNoteIDReturn {
	
	public short note;
	public boolean allowHalbtonC;
	public boolean allowHalbtonB;
	public boolean noteDown;
	
	public GetNoteIDReturn(short note, boolean allowHalbtonC, boolean allowHalbtonB, boolean noteDown) {
		this.note = note;
		this.allowHalbtonC = allowHalbtonC;
		this.allowHalbtonB = allowHalbtonB;
		this.noteDown = noteDown;
	}

}
