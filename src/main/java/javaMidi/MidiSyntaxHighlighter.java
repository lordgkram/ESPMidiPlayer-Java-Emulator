package javaMidi;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javaMidi.cppconv.StrMidiUtil;

public class MidiSyntaxHighlighter extends AbstractTokenMaker{

    public static final int TOKEN_MODUS = TokenTypes.FUNCTION;
    public static final int TOKEN_WIEDERHOLUNG = TokenTypes.OPERATOR;
    public static final int TOKEN_NOTE = TokenTypes.RESERVED_WORD;
    public static final int TOKEN_LENGE = TokenTypes.LITERAL_NUMBER_DECIMAL_INT;
    public static final int TOKEN_BPM = TokenTypes.VARIABLE;
    public static final int TOKEN_CONTROLL = TokenTypes.REGEX;
    public static final int TOKEN_PRESET = TokenTypes.ANNOTATION;
    public static final int TOKEN_INSTRUMENT = TokenTypes.RESERVED_WORD_2;
    public static final int TOKEN_BUFFER = TokenTypes.DATA_TYPE;
    public static final int TOKEN_ERROR = TokenTypes.COMMENT_MULTILINE;
    public static final int TOKEN_ERROR_EOL = TokenTypes.COMMENT_EOL;
    public static final int TOKEN_NULL = TokenTypes.NULL;
    public static final int TOKEN_NONE = TokenTypes.WHITESPACE;

    private int currentTokenType;
    private int currentTokenStart;
    private int oldTokenStart;
    private int tokenState;
    private int tokenCtr;
    private boolean advanced;
    private int startCTR;
    private int wdhctr;
    private char[] lts;
    
    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap tokenMap = new TokenMap();
        return tokenMap;
    }

    @Override
    public Token getTokenList(Segment text, int startTokenType, int startOffset) {
        resetTokenList();

        lts = text.array;
        int off = text.offset;
        int cnt = text.count;
        int end = off + cnt;

        int nso = startOffset - off;

        currentTokenType = startTokenType;
        currentTokenStart = off;

        tokenState = 0;
        tokenCtr = 0;
        startCTR = 0;
        wdhctr = 0;

        advanced = false;

        for (int i = off; i < end; i++) {
            char c = lts[i];
            switch (currentTokenType){
            case TOKEN_NULL:
                switch(c){
                    case ';':
                        currentTokenType = TOKEN_BUFFER;
                        currentTokenStart = i;
                        tokenState = 0;
                        break;
                    case '~':
                        currentTokenType = TOKEN_PRESET;
                        currentTokenStart = i;
                        tokenState = 0;
                        break;
                    default:
                        doNewToken(c, TOKEN_NULL, i);
                        break;
                }
                break;
            case TOKEN_BUFFER:
                switch(c){
                case 'l':
                    if(tokenState != 0){
                        addToken(text, currentTokenStart, i - 1, TOKEN_BUFFER, nso + currentTokenStart);
                        doNewToken(c, TOKEN_BUFFER, i);
                    }else{
                        tokenState = 1;
                    }
                    break;
                case 'n':
                    if(tokenState == 2){
                        addToken(text, currentTokenStart, i - 1, TOKEN_BUFFER, nso + currentTokenStart);
                        doNewToken(c, TOKEN_BUFFER, i);
                    }else{
                        tokenState = 2;
                    }
                    break;
                default:
                    addToken(text, currentTokenStart, i - 1, TOKEN_BUFFER, nso + currentTokenStart);
                    doNewToken(c, TOKEN_BUFFER, i);
                    break;
                }
                break;
            case TOKEN_PRESET:
                if(!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')){
                    addToken(text, currentTokenStart, i - 1, TOKEN_PRESET, nso + currentTokenStart);
                    currentTokenType = TOKEN_ERROR_EOL;
                    currentTokenStart = i;
                }
                break;
            case TOKEN_LENGE:
                if(tokenCtr == 0){
                    if(!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')){
                        if(c == '.'){
                            tokenCtr = 1;
                        }else{
                            addToken(text, currentTokenStart, i - 1, TOKEN_LENGE, nso + currentTokenStart);
                            doNewToken(c, TOKEN_LENGE, i);
                        }
                    }
                }else{
                    addToken(text, currentTokenStart, i - 1, TOKEN_LENGE, nso + currentTokenStart);
                    doNewToken(c, TOKEN_LENGE, i);
                }
                break;
            case TOKEN_CONTROLL:
                if(!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')){
                    addToken(text, currentTokenStart, i - 1, TOKEN_CONTROLL, nso + currentTokenStart);
                    doNewToken(c, TOKEN_CONTROLL, i);
                }
                break;
            case TOKEN_ERROR_EOL:
                break;
            case TOKEN_INSTRUMENT:
                if(tokenState == 1){
                    if(!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')){
                        addToken(text, currentTokenStart, i - 1, TOKEN_INSTRUMENT, nso + currentTokenStart);
                        doNewToken(c, TOKEN_INSTRUMENT, i);
                    }
                }else{
                    if(tokenCtr - 1 > 0){
                        tokenCtr--;
                    } else {
                        addToken(text, currentTokenStart, i - 1, TOKEN_INSTRUMENT, nso + currentTokenStart);
                        doNewToken(c, TOKEN_INSTRUMENT, i);
                    }
                }
                break;
            case TOKEN_MODUS:
                advanced = !advanced;
                addToken(text, currentTokenStart, i - 1, TOKEN_MODUS, nso + currentTokenStart);
                doNewToken(c, TOKEN_MODUS, i);
                break;
            case TOKEN_WIEDERHOLUNG:
                addToken(text, currentTokenStart, i - 1, TOKEN_WIEDERHOLUNG, nso + currentTokenStart);
                doNewToken(c, TOKEN_WIEDERHOLUNG, i);
                break;
            case TOKEN_NOTE:
                if(tokenState == 0 || tokenState == 1 || tokenState == 2){
                    switch (c){
                        case '\'':
                            if(!((tokenState == 0 || tokenState == 1) && tokenCtr < 3)){
                                addToken(text, currentTokenStart, i - 1, TOKEN_NOTE, nso + currentTokenStart);
                                doNewToken(c, TOKEN_NOTE, i);
                            }else{
                                tokenCtr++;
                                tokenState = 1;
                            }
                            break;
                        case '#':
                            if(tokenCtr < 4){
                                tokenState = 2;
                                tokenCtr = 4;
                            }else{
                                addToken(text, currentTokenStart, i - 1, TOKEN_NOTE, nso + currentTokenStart);
                                doNewToken(c, TOKEN_NOTE, i);
                            }
                            break;
                        case 'b':
                            if(tokenCtr < 5){
                                tokenState = 2;
                                tokenCtr = 5;
                            }else{
                                addToken(text, currentTokenStart, i - 1, TOKEN_NOTE, nso + currentTokenStart);
                                doNewToken(c, TOKEN_NOTE, i);
                            }
                            break;
                        default:
                            addToken(text, currentTokenStart, i - 1, TOKEN_NOTE, nso + currentTokenStart);
                            doNewToken(c, TOKEN_NOTE, i);
                            break;
                    }
                }else if(tokenState == 3){
                    addToken(text, currentTokenStart, i - 1, TOKEN_NOTE, nso + currentTokenStart);
                    doNewToken(c, TOKEN_NOTE, i);
                }
                break;
            case TOKEN_BPM:
                if(tokenState == 1){
                    if(tokenCtr == 0){
                        tokenCtr++;
                        if(c != 'p'){
                            startNewToken(TOKEN_ERROR, currentTokenStart);
                            doNewToken(c, TOKEN_ERROR, i);
                        }
                    }else if (tokenCtr == 1){
                        tokenCtr++;
                        if(c != 'm'){
                            startNewToken(TOKEN_ERROR, currentTokenStart);
                            doNewToken(c, TOKEN_ERROR, i);
                        }else{
                            tokenState=0;
                        }
                    }
                }else{
                    if(!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')){
                        addToken(text, currentTokenStart, i - 1, TOKEN_BPM, nso + currentTokenStart);
                        doNewToken(c, TOKEN_BPM, i);
                    }
                }
                break;
            case TOKEN_ERROR:
                if(doNewToken(c, TOKEN_ERROR, i)){
                    addToken(text, oldTokenStart, i - 1, TOKEN_ERROR, nso + oldTokenStart);
                }
                break;
            case TOKEN_NONE:
                if(doNewToken(c, TOKEN_NONE, i)){
                    addToken(text, oldTokenStart, i - 1, TOKEN_NONE, nso + oldTokenStart);
                }
                break;
            }
        }

        switch(currentTokenType){
            default:
                addToken(text, currentTokenStart, end - 1, currentTokenType, nso + currentTokenStart);
                addNullToken();
                break;
        }

        return firstToken;
    }

    private boolean doNewToken(char curr, int lastToken, int off){
        if(startCTR < 3){
            String s = String.copyValueOf(lts);
            s = s.substring(off);
            if(s.length() > 0 && StrMidiUtil.isNumber(s.charAt(0))){
                startNewToken(TOKEN_INSTRUMENT, off);
                tokenState = 1;
                startCTR = 3;
                return true;
            }
            for(Instrument i : JavaMain.main.instrumente){
                if(s.startsWith(i.name)){
                    startNewToken(TOKEN_INSTRUMENT, off);
                    tokenCtr = i.name.length();
                    startCTR = 3;
                    return true;
                }
            }
        }
        switch(curr){
            case ' ':
                if(lastToken != TOKEN_NONE){
                    startNewToken(TOKEN_NONE, off);
                    return true;
                }
                break;
            case '-':
                if(startCTR == 0){
                    startNewToken(TOKEN_MODUS, off);
                    startCTR = 1;
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                return true;
            case 'b':
                if(startCTR == 1 || startCTR == 0){
                    startNewToken(TOKEN_BPM, off);
                    startCTR = 2;
                    tokenState = 1;
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                return true;
            case 'w':
            case 'W':
                if(wdhctr != 0){
                    wdhctr = 0;
                }else{
                    wdhctr = 1;
                }
                startNewToken(TOKEN_WIEDERHOLUNG, off);
                startCTR = 3;
                return true;
            case 'n':
            case 'N':
                if(wdhctr == 1){
                    wdhctr = 2;
                    startNewToken(TOKEN_WIEDERHOLUNG, off);
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 'u':
            case 'U':
                if(wdhctr == 2){
                    startNewToken(TOKEN_WIEDERHOLUNG, off);
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 'm':
            case 'M':
                if(lastToken == TOKEN_NONE){
                    startNewToken(TOKEN_MODUS, off);
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 's':
            case 'S':
            case 'l':
            case 'L':
                if(advanced){
                    startNewToken(TOKEN_NOTE, off);
                    tokenState = 3;
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 'p':
            case 'P':
                startNewToken(TOKEN_NOTE, off);
                tokenState = 3;
                startCTR = 3;
                return true;
            case 'q':
            case 'Q':
                if(advanced){
                    startNewToken(TOKEN_BPM, off);
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 'i':
            case 'I':
                if(advanced){
                    String s = String.copyValueOf(lts);
                    s = s.substring(off + 1);
                    if(s.length() > 0 && StrMidiUtil.isNumber(s.charAt(0))){
                        startNewToken(TOKEN_INSTRUMENT, off);
                        tokenState = 1;
                        startCTR = 3;
                        return true;
                    }
                    for(Instrument i : JavaMain.main.instrumente){
                        if(s.startsWith(i.name)){
                            startNewToken(TOKEN_INSTRUMENT, off);
                            tokenCtr = i.name.length() + 1;
                            startCTR = 3;
                            return true;
                        }
                    }
                }
                startNewToken(TOKEN_ERROR, off);
                startCTR = 3;
                return true;
            case 'k':
            case 'K':
            case 'v':
            case 'V':
            case 'x':
            case 'X':
            case 'y':
            case 'Y':
            case 'j':
            case 'J':
            case 'o':
            case 'O':
                if(advanced){
                    startNewToken(TOKEN_CONTROLL, off);
                }else{
                    startNewToken(TOKEN_ERROR, off);
                }
                startCTR = 3;
                return true;
            case 'a':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'A':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
                startNewToken(TOKEN_NOTE, off);
                startCTR = 3;
                return true;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                startNewToken(TOKEN_LENGE, off);
                startCTR = 3;
                return true;
            default:
                if(lastToken != TOKEN_ERROR){
                    startNewToken(TOKEN_ERROR, off);
                    return true;
                }
        }
        return false;
    }

    private void startNewToken(int type, int off){
        oldTokenStart = currentTokenStart;
        currentTokenStart = off;
        currentTokenType = type;
        tokenCtr = 0;
        tokenState = 0;
    }

}
