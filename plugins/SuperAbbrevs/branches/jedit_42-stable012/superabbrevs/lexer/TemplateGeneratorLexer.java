/* The following code was generated by JFlex 1.3.5 on 5/20/06 1:00 PM */

package superabbrevs.lexer;

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.3.5
 * on 5/20/06 1:00 PM from the specification file
 * <tt>file:/home/sune/projects/jedit/plugins/SuperAbbrevs_jedit4.2/superabbrevs/lexer/TemplateGenerator.lex</tt>
 */
public class TemplateGeneratorLexer {

  /** This character denotes the end of file */
  final public static int YYEOF = -1;

  /** initial size of the lookahead buffer */
  final private static int YY_BUFFERSIZE = 16384;

  /** lexical states */
  final public static int CODE = 1;
  final public static int CODE_OUTPUT_FIELD = 2;
  final public static int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  final private static String yycmap_packed = 
    "\11\0\1\12\1\2\1\0\1\3\1\1\22\0\1\3\2\0\1\5"+
    "\6\0\1\10\4\0\1\7\14\0\1\4\1\6\1\11\uffc1\0";

  /** 
   * Translates characters to character classes
   */
  final private static char [] yycmap = yy_unpack_cmap(yycmap_packed);

  /** 
   * Translates a state to a row index in the transition table
   */
  final private static int yy_rowMap [] = { 
        0,    11,    22,    33,    44,    33,    55,    66,    33,    33, 
       33,    77,    88,    99,   110,   121,   132,    55,    66,   143, 
      154,   165,    33,    33,    33,   176,    33,    33,    33,   187, 
       33,   165,    33
  };

  /** 
   * The packed transition table of the DFA (part 0)
   */
  final private static String yy_packed0 = 
    "\1\4\1\5\1\6\1\7\1\10\5\4\1\7\1\11"+
    "\1\12\1\13\1\11\1\14\1\15\1\11\1\16\1\17"+
    "\1\11\1\12\1\11\1\12\1\13\1\11\1\20\1\21"+
    "\1\11\1\16\1\17\1\11\1\12\15\0\1\6\13\0"+
    "\1\22\1\23\5\0\1\22\5\0\1\24\12\0\1\25"+
    "\16\0\1\26\10\0\1\27\1\30\11\0\1\31\10\0"+
    "\1\32\16\0\1\33\7\0\1\34\12\0\1\35\5\0"+
    "\1\36\1\37\1\40\6\0\1\40\6\0\1\41\6\0"+
    "\1\37\10\0";

  /** 
   * The transition table of the DFA
   */
  final private static int yytrans [] = yy_unpack();


  /* error codes */
  final private static int YY_UNKNOWN_ERROR = 0;
  final private static int YY_ILLEGAL_STATE = 1;
  final private static int YY_NO_MATCH = 2;
  final private static int YY_PUSHBACK_2BIG = 3;

  /* error messages for the codes above */
  final private static String YY_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Internal error: unknown state",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * YY_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private final static byte YY_ATTRIBUTE[] = {
     0,  0,  0,  9,  1,  9,  1,  1,  9,  9,  9,  1,  1,  1,  1,  1, 
     1,  0,  0,  1,  1,  1,  9,  9,  9,  1,  9,  9,  9,  1,  9,  0,  9
  };

  /** the input device */
  private java.io.Reader yy_reader;

  /** the current state of the DFA */
  private int yy_state;

  /** the current lexical state */
  private int yy_lexical_state = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char yy_buffer[] = new char[YY_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int yy_markedPos;

  /** the textposition at the last state to be included in yytext */
  private int yy_pushbackPos;

  /** the current text position in the buffer */
  private int yy_currentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int yy_startRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int yy_endRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn; 

  /** 
   * yy_atBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean yy_atBOL = true;

  /** yy_atEOF == true <=> the scanner is at the EOF */
  private boolean yy_atEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean yy_eof_done;

  /* user code: */
  StringBuffer code = new StringBuffer();
  StringBuffer text = new StringBuffer();
  
  String whiteSpace = null;
  
  boolean end = false;
  boolean inLineComment = false;
  boolean inRangeComment = false;
  boolean endOfLine = false;
  
  private Token token(int type, Object value) {
	Token t = new Token(type);
	t.addValue(value);
    return t;
  }
  
  private Token token(int type, Object value1, Object value2) {
	Token t = new Token(type);
	t.addValue(value1);
	t.addValue(value2);
    return t;
  }


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public TemplateGeneratorLexer(java.io.Reader in) {
    this.yy_reader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public TemplateGeneratorLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the split, compressed DFA transition table.
   *
   * @return the unpacked transition table
   */
  private static int [] yy_unpack() {
    int [] trans = new int[198];
    int offset = 0;
    offset = yy_unpack(yy_packed0, offset, trans);
    return trans;
  }

  /** 
   * Unpacks the compressed DFA transition table.
   *
   * @param packed   the packed transition table
   * @return         the index of the last entry
   */
  private static int yy_unpack(String packed, int offset, int [] trans) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do trans[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] yy_unpack_cmap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 38) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   IOException  if any I/O-Error occurs
   */
  private boolean yy_refill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (yy_startRead > 0) {
      System.arraycopy(yy_buffer, yy_startRead, 
                       yy_buffer, 0, 
                       yy_endRead-yy_startRead);

      /* translate stored positions */
      yy_endRead-= yy_startRead;
      yy_currentPos-= yy_startRead;
      yy_markedPos-= yy_startRead;
      yy_pushbackPos-= yy_startRead;
      yy_startRead = 0;
    }

    /* is the buffer big enough? */
    if (yy_currentPos >= yy_buffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[yy_currentPos*2];
      System.arraycopy(yy_buffer, 0, newBuffer, 0, yy_buffer.length);
      yy_buffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = yy_reader.read(yy_buffer, yy_endRead, 
                                            yy_buffer.length-yy_endRead);

    if (numRead < 0) {
      return true;
    }
    else {
      yy_endRead+= numRead;  
      return false;
    }
  }


  /**
   * Closes the input stream.
   */
  final public void yyclose() throws java.io.IOException {
    yy_atEOF = true;            /* indicate end of file */
    yy_endRead = yy_startRead;  /* invalidate buffer    */

    if (yy_reader != null)
      yy_reader.close();
  }


  /**
   * Closes the current stream, and resets the
   * scanner to read from a new input stream.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>YY_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  final public void yyreset(java.io.Reader reader) throws java.io.IOException {
    yyclose();
    yy_reader = reader;
    yy_atBOL  = true;
    yy_atEOF  = false;
    yy_endRead = yy_startRead = 0;
    yy_currentPos = yy_markedPos = yy_pushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    yy_lexical_state = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  final public int yystate() {
    return yy_lexical_state;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  final public void yybegin(int newState) {
    yy_lexical_state = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  final public String yytext() {
    return new String( yy_buffer, yy_startRead, yy_markedPos-yy_startRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  final public char yycharat(int pos) {
    return yy_buffer[yy_startRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  final public int yylength() {
    return yy_markedPos-yy_startRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void yy_ScanError(int errorCode) {
    String message;
    try {
      message = YY_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = YY_ERROR_MSG[YY_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  private void yypushback(int number)  {
    if ( number > yylength() )
      yy_ScanError(YY_PUSHBACK_2BIG);

    yy_markedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void yy_do_eof() throws java.io.IOException {
    if (!yy_eof_done) {
      yy_eof_done = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   IOException  if any I/O-Error occurs
   */
  public Token nextToken() throws java.io.IOException {
    int yy_input;
    int yy_action;

    // cached fields:
    int yy_currentPos_l;
    int yy_startRead_l;
    int yy_markedPos_l;
    int yy_endRead_l = yy_endRead;
    char [] yy_buffer_l = yy_buffer;
    char [] yycmap_l = yycmap;

    int [] yytrans_l = yytrans;
    int [] yy_rowMap_l = yy_rowMap;
    byte [] yy_attr_l = YY_ATTRIBUTE;

    while (true) {
      yy_markedPos_l = yy_markedPos;

      boolean yy_r = false;
      for (yy_currentPos_l = yy_startRead; yy_currentPos_l < yy_markedPos_l;
                                                             yy_currentPos_l++) {
        switch (yy_buffer_l[yy_currentPos_l]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          yy_r = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          yy_r = true;
          break;
        case '\n':
          if (yy_r)
            yy_r = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          yy_r = false;
          yycolumn++;
        }
      }

      if (yy_r) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean yy_peek;
        if (yy_markedPos_l < yy_endRead_l)
          yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
        else if (yy_atEOF)
          yy_peek = false;
        else {
          boolean eof = yy_refill();
          yy_markedPos_l = yy_markedPos;
          yy_buffer_l = yy_buffer;
          if (eof) 
            yy_peek = false;
          else 
            yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
        }
        if (yy_peek) yyline--;
      }
      yy_action = -1;

      yy_startRead_l = yy_currentPos_l = yy_currentPos = 
                       yy_startRead = yy_markedPos_l;

      yy_state = yy_lexical_state;


      yy_forAction: {
        while (true) {

          if (yy_currentPos_l < yy_endRead_l)
            yy_input = yy_buffer_l[yy_currentPos_l++];
          else if (yy_atEOF) {
            yy_input = YYEOF;
            break yy_forAction;
          }
          else {
            // store back cached positions
            yy_currentPos  = yy_currentPos_l;
            yy_markedPos   = yy_markedPos_l;
            boolean eof = yy_refill();
            // get translated positions and possibly new buffer
            yy_currentPos_l  = yy_currentPos;
            yy_markedPos_l   = yy_markedPos;
            yy_buffer_l      = yy_buffer;
            yy_endRead_l     = yy_endRead;
            if (eof) {
              yy_input = YYEOF;
              break yy_forAction;
            }
            else {
              yy_input = yy_buffer_l[yy_currentPos_l++];
            }
          }
          int yy_next = yytrans_l[ yy_rowMap_l[yy_state] + yycmap_l[yy_input] ];
          if (yy_next == -1) break yy_forAction;
          yy_state = yy_next;

          int yy_attributes = yy_attr_l[yy_state];
          if ( (yy_attributes & 1) == 1 ) {
            yy_action = yy_state; 
            yy_markedPos_l = yy_currentPos_l; 
            if ( (yy_attributes & 8) == 8 ) break yy_forAction;
          }

        }
      }

      // store back cached position
      yy_markedPos = yy_markedPos_l;

      switch (yy_action) {

        case 15: 
        case 16: 
          {  
		if (!inRangeComment){
			code.append(yytext());
		}
	 }
        case 34: break;
        case 8: 
        case 11: 
        case 12: 
        case 13: 
        case 14: 
          {  
		if (!inRangeComment){
			code.append(yytext());
		}
	 }
        case 35: break;
        case 3: 
        case 6: 
        case 7: 
          {  
		endOfLine = false;
		text.append(yytext());
	 }
        case 36: break;
        case 10: 
          {  
		if (!inRangeComment){
			inLineComment = false;
			code.append("\n");
		}
	 }
        case 37: break;
        case 26: 
          {  
		if (!inLineComment && !inRangeComment){
			yybegin(YYINITIAL);
			
			if(!endOfLine || whiteSpace.equals("")){
				whiteSpace = null;
			}
			endOfLine = false;
			
			return token(Token.CODE_OUTPUT_FIELD, code.toString(), whiteSpace);
		} 
	 }
        case 38: break;
        case 27: 
          { 
		
		whiteSpace = yytext().substring(0,yytext().length()-3);
		text.append(whiteSpace);
		
		yybegin(CODE_OUTPUT_FIELD); 
		code.setLength(0);
		if (text.length() != 0){
			
			Token t = token(Token.TEXT_FIELD,text.toString());
			text.setLength(0);
			return t;
		} else { 
			text.setLength(0); 
		}
	 }
        case 39: break;
        case 4: 
        case 5: 
          { 
		endOfLine = true;
		text.append(yytext());
	 }
        case 40: break;
        case 21: 
          { 
		endOfLine = false;
		whiteSpace = null;
		if (!inLineComment && !inRangeComment){
			yybegin(YYINITIAL); 
			return token(Token.CODE, code.toString());
		}
	 }
        case 41: break;
        case 25: 
          { 
		if (!inLineComment && !inRangeComment){
			yybegin(CODE);
			String temp = "<#"+code.toString();
			code.setLength(0);
			endOfLine = false;
			return token(Token.TEXT_FIELD,temp);
		}
	 }
        case 42: break;
        case 29: 
        case 30: 
          { 
		endOfLine = true;
		whiteSpace = null;
		if (!inLineComment && !inRangeComment){
			yybegin(YYINITIAL); 
			return token(Token.CODE, code.toString());
		}
	 }
        case 43: break;
        case 23: 
          { 
		inRangeComment = true;
	 }
        case 44: break;
        case 22: 
          { 
		inLineComment = true;
	 }
        case 45: break;
        case 24: 
          { 
		inRangeComment = false;
	 }
        case 46: break;
        case 9: 
          { 
		code.append(yytext());
	 }
        case 47: break;
        case 32: 
          { 
		if (!inLineComment && !inRangeComment){
			String temp = "<#="+code.toString();
			code.setLength(0);
			endOfLine = false;
			return token(Token.TEXT_FIELD,temp);
		}
	 }
        case 48: break;
        case 20: 
          { 
		if (!inLineComment && !inRangeComment){
			String temp = "<#"+code.toString();
			code.setLength(0);
			endOfLine = false;
			return token(Token.TEXT_FIELD,temp);
		}
	 }
        case 49: break;
        case 28: 
          { 
		if (!inLineComment && !inRangeComment){
			yybegin(CODE_OUTPUT_FIELD);
			String temp = "<#="+code.toString();
			code.setLength(0);
			endOfLine = false;
			return token(Token.TEXT_FIELD,temp);
		}
	 }
        case 50: break;
        case 19: 
          {  
		endOfLine = false;
		yybegin(CODE); 
		code.setLength(0);
		if (text.length() != 0){
			Token t = token(Token.TEXT_FIELD,text.toString());
			text.setLength(0);
			return t;
		} else { 
			text.setLength(0); 
		}
	 }
        case 51: break;
        default: 
          if (yy_input == YYEOF && yy_startRead == yy_currentPos) {
            yy_atEOF = true;
            yy_do_eof();
            switch (yy_lexical_state) {
            case CODE:
              {  
		if (end || code.length() == 0) { return null; }
		else {
			end = true; 
			return token(Token.TEXT_FIELD,code.toString()); 
		}
	 }
            case 34: break;
            case CODE_OUTPUT_FIELD:
              {  
		if (end || code.length() == 0) { 
			return null; 
		} else {
			end = true; 
			return token(Token.TEXT_FIELD,code.toString()); 
		} 
	 }
            case 35: break;
            case YYINITIAL:
              {  
		if (end || text.length() == 0) { return null; }
		else {
			end = true;
			return token(Token.TEXT_FIELD,text.toString()); 
		} 
	 }
            case 36: break;
            default:
            return null;
            }
          } 
          else {
            yy_ScanError(YY_NO_MATCH);
          }
      }
    }
  }


}
