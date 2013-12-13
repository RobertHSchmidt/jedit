/* Generated By:JavaCC: Do not edit this line. FunctionListParser.java */
package gatchan.phpparser.methodlist;
import java.io.*;
import java.util.*;


public class FunctionListParser implements FunctionListParserConstants {
        //{{{ main() method
        public static void main(String args[]) throws IOException
        {
                FunctionListParser parser = new FunctionListParser(new StringReader(""));
                BufferedReader in = new BufferedReader(new FileReader(
                        "c:\u005c\u005cUsers\u005c\u005cChocoPC\u005c\u005cdev\u005c\u005cjEdit\u005c\u005cplugins\u005c\u005cPHPParser\u005c\u005csrc\u005c\u005cgatchan\u005c\u005cphpparser\u005c\u005cmethodlist\u005c\u005ctest"));
                String line = in.readLine();
                Map<String, Function> functions = new HashMap<String, Function>();
                while (line != null)
                {
                        if (!line.isEmpty())
                        {
                                parser.ReInit(new StringReader(line));
                                try
                                {
                                        Function function = parser.function();
                                        functions.put(function.getName(), function);
                                }
                                catch (TokenMgrError e)
                                {
                                        System.err.println(line);
                                }
                                catch (ParseException e)
                                {
                                        System.err.println(line);
                                }
                        }
                        line = in.readLine();
                }
        }

//{{{ parse() method
  final public Map<String,Function> parse() throws ParseException {
        Function function;
        Map<String,Function> functions = new HashMap<String,Function>();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VOID:
      case STRING:
      case BOOL:
      case INTEGER:
      case NUMBER:
      case DOUBLE:
      case INT:
      case ARRAY:
      case LONG:
      case RESOURCE:
      case OBJECT:
      case MIXED:
      case FUNCTION_IDENTIFIER:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      function = function();
                        Function f = functions.get(function.getName());
                        if (f == null)
                                functions.put(function.getName(), function);
                        else
                        {
                                f.setAlternative(function);
                        }
    }
    jj_consume_token(0);
                {if (true) return functions;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ function() method
  final public Function function() throws ParseException {
        String type;
        String methodName;
        List<Argument> arguments = null;
    type = returnType();
    methodName = functionIdentifier();
    jj_consume_token(LPAREN);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VARARGSIDENTIFIER:
    case REF:
    case LBRACKET:
    case VOID:
    case STRING:
    case BOOL:
    case INTEGER:
    case NUMBER:
    case DOUBLE:
    case INT:
    case ARRAY:
    case LONG:
    case RESOURCE:
    case OBJECT:
    case MIXED:
    case FUNCTION_IDENTIFIER:
      arguments = firstArgument();
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
                if (arguments == null)
                {
                        {if (true) return new Function(type, methodName, new Argument[0]);}
                }

                Argument[] args = new Argument[arguments.size()];
                args = arguments.toArray(args);
                {if (true) return new Function(type, methodName, args);}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ firstArgument()
  final public List<Argument> firstArgument() throws ParseException {
        Argument argument;
        List<Argument> arguments = new ArrayList<Argument>();
        List<Argument> nextArgs;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VOID:
      jj_consume_token(VOID);
      break;
    case VARARGSIDENTIFIER:
    case REF:
    case STRING:
    case BOOL:
    case INTEGER:
    case NUMBER:
    case DOUBLE:
    case INT:
    case ARRAY:
    case LONG:
    case RESOURCE:
    case OBJECT:
    case MIXED:
    case FUNCTION_IDENTIFIER:
      argument = argument();
                                        arguments.add(argument);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                               arguments.addAll(nextArgs);
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
      break;
    case LBRACKET:
      jj_consume_token(LBRACKET);
      argument = argument();
                        argument.setOptional(true);
                        arguments.add(argument);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                for (Argument arg : nextArgs)
                                {
                                        arg.setOptional(true);
                                        arguments.add(arg);
                                }
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
      jj_consume_token(RBRACKET);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                               arguments.addAll(nextArgs);
        break;
      default:
        jj_la1[4] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return arguments;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ nextArguments() method
  final public List<Argument> nextArguments() throws ParseException {
        Argument argument;
        List<Argument> arguments = new ArrayList<Argument>();
        List<Argument> nextArgs;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case COMMA:
      jj_consume_token(COMMA);
      argument = argument();
                                        arguments.add(argument);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                               arguments.addAll(nextArgs);
        break;
      default:
        jj_la1[6] = jj_gen;
        ;
      }
      break;
    case LBRACKET:
      jj_consume_token(LBRACKET);
      jj_consume_token(COMMA);
      argument = argument();
                        argument.setOptional(true);
                        arguments.add(argument);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                for (Argument arg : nextArgs)
                                {
                                        arg.setOptional(true);
                                        arguments.add(arg);
                                }
        break;
      default:
        jj_la1[7] = jj_gen;
        ;
      }
      jj_consume_token(RBRACKET);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
      case LBRACKET:
        nextArgs = nextArguments();
                                               arguments.addAll(nextArgs);
        break;
      default:
        jj_la1[8] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return arguments;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ argument() method
  final public Argument argument() throws ParseException {
        String type;
        String argumentName;
        String alternateType = null;
        boolean ref = false;
        String initializer = null;
        boolean varargs = false;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VARARGSIDENTIFIER:
      jj_consume_token(VARARGSIDENTIFIER);
                        Argument arg = new Argument("unknown", "...");
                        arg.setVarargs(true);
                        {if (true) return arg;}
      break;
    case REF:
    case STRING:
    case BOOL:
    case INTEGER:
    case NUMBER:
    case DOUBLE:
    case INT:
    case ARRAY:
    case LONG:
    case RESOURCE:
    case OBJECT:
    case MIXED:
    case FUNCTION_IDENTIFIER:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case REF:
        jj_consume_token(REF);
                         ref = true;
        break;
      default:
        jj_la1[10] = jj_gen;
        ;
      }
      type = type();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PIPE:
        jj_consume_token(PIPE);
        alternateType = type();
        break;
      default:
        jj_la1[11] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case REF:
        jj_consume_token(REF);
                         ref = true;
        break;
      default:
        jj_la1[12] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STAR:
        jj_consume_token(STAR);
        break;
      default:
        jj_la1[13] = jj_gen;
        ;
      }
      argumentName = argumentIdentifier();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASSIGN:
      case VARARGS:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case VARARGS:
          jj_consume_token(VARARGS);
                                    varargs = true;
          break;
        case ASSIGN:
          jj_consume_token(ASSIGN);
          initializer = initializer();
          label_2:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case PIPE:
              ;
              break;
            default:
              jj_la1[14] = jj_gen;
              break label_2;
            }
            jj_consume_token(PIPE);
                                        String secondInitializer;
            secondInitializer = initializer();
                                                                    initializer += " | " + secondInitializer;
          }
          break;
        default:
          jj_la1[15] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[16] = jj_gen;
        ;
      }
                        Argument argument = new Argument(type, argumentName);
                        argument.setAlternateType(alternateType);
                        argument.setReference(ref);
                        argument.setVarargs(varargs);
                        if ("$...".equals(argumentName))
                                argument.setVarargs(true);
                        argument.setDefaultValue(initializer);
                        {if (true) return argument;}
      break;
    default:
      jj_la1[17] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ initializer()
  final public String initializer() throws ParseException {
        String ret;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LITERAL:
      jj_consume_token(LITERAL);
                            ret = token.image;
      break;
    case SINGLEQUOTE_LITERAL:
      jj_consume_token(SINGLEQUOTE_LITERAL);
                                        ret = token.image;
      break;
    case MINUS:
      jj_consume_token(MINUS);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FUNCTION_IDENTIFIER:
        jj_consume_token(FUNCTION_IDENTIFIER);
        break;
      case INTEGER_LITERAL:
        jj_consume_token(INTEGER_LITERAL);
        break;
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                                                ret = "-"+token.image;
      break;
    default:
      jj_la1[19] = jj_gen;
      if (jj_2_1(2)) {
        ret = functionCall();
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case FUNCTION_IDENTIFIER:
          jj_consume_token(FUNCTION_IDENTIFIER);
                                       ret = token.image;
          break;
        case IDENTIFIER:
          jj_consume_token(IDENTIFIER);
                              ret = token.image;
          break;
        case STRING:
          jj_consume_token(STRING);
                           ret = token.image;
          break;
        case INTEGER_LITERAL:
          jj_consume_token(INTEGER_LITERAL);
                                    ret = token.image;
          break;
        case FLOATING_POINT_LITERAL:
          jj_consume_token(FLOATING_POINT_LITERAL);
                                           ret = token.image;
          break;
        default:
          jj_la1[20] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SLASH:
    case PLUS:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        break;
      case SLASH:
        jj_consume_token(SLASH);
        break;
      default:
        jj_la1[21] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                        String next;
      next = initializer();
                        {if (true) return ret + " + " + next;}
      break;
    default:
      jj_la1[22] = jj_gen;
      ;
    }
                {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ functionCall() method
  final public String functionCall() throws ParseException {
        String ret;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FUNCTION_IDENTIFIER:
      jj_consume_token(FUNCTION_IDENTIFIER);
      break;
    case ARRAY:
      jj_consume_token(ARRAY);
      break;
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                ret = token.image;
    jj_consume_token(LPAREN);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case MINUS:
    case STRING:
    case ARRAY:
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case FUNCTION_IDENTIFIER:
    case IDENTIFIER:
    case LITERAL:
    case SINGLEQUOTE_LITERAL:
      initializer();
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[24] = jj_gen;
          break label_3;
        }
        jj_consume_token(COMMA);
        initializer();
      }
      break;
    default:
      jj_la1[25] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
                {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ argumentIdentifier() method
  final public String argumentIdentifier() throws ParseException {
        String ret;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENTIFIER:
      jj_consume_token(IDENTIFIER);
                               ret = token.image;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SLASH:
        jj_consume_token(SLASH);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IDENTIFIER:
          jj_consume_token(IDENTIFIER);
          break;
        case FUNCTION_IDENTIFIER:
          jj_consume_token(FUNCTION_IDENTIFIER);
          break;
        default:
          jj_la1[26] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
                                                                    ret += "/" + token.image;
        break;
      default:
        jj_la1[27] = jj_gen;
        ;
      }
      break;
    case OBJECT:
      jj_consume_token(OBJECT);
                           ret = token.image;
      break;
    case VARARGSIDENTIFIER:
      jj_consume_token(VARARGSIDENTIFIER);
                                      ret = token.image;
      break;
    default:
      jj_la1[28] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ functionIdentifier() method
  final public String functionIdentifier() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FUNCTION_IDENTIFIER:
      jj_consume_token(FUNCTION_IDENTIFIER);
      break;
    case ARRAY:
      jj_consume_token(ARRAY);
      break;
    default:
      jj_la1[29] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return token.image;}
    throw new Error("Missing return statement in function");
  }

  //}}}


//{{{ returnType() method
  final public String returnType() throws ParseException {
        String ret;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VOID:
      jj_consume_token(VOID);
                         ret = "void";
      break;
    case STRING:
    case BOOL:
    case INTEGER:
    case NUMBER:
    case DOUBLE:
    case INT:
    case ARRAY:
    case LONG:
    case RESOURCE:
    case OBJECT:
    case MIXED:
    case FUNCTION_IDENTIFIER:
      ret = type();
      break;
    default:
      jj_la1[30] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  //}}}

//{{{ type() method
  final public String type() throws ParseException {
        String ret;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING:
      jj_consume_token(STRING);
                     ret = "string";
      break;
    case BOOL:
      jj_consume_token(BOOL);
                   ret = "boolean";
      break;
    case INT:
      jj_consume_token(INT);
                  ret = "integer";
      break;
    case INTEGER:
      jj_consume_token(INTEGER);
                      ret = "integer";
      break;
    case NUMBER:
      jj_consume_token(NUMBER);
                     ret = "number";
      break;
    case OBJECT:
      jj_consume_token(OBJECT);
                     ret = "object";
      break;
    case ARRAY:
      jj_consume_token(ARRAY);
                    ret = "array";
      break;
    case LONG:
      jj_consume_token(LONG);
                   ret = "long";
      break;
    case RESOURCE:
      jj_consume_token(RESOURCE);
                       ret = "resource";
      break;
    case DOUBLE:
      jj_consume_token(DOUBLE);
                     ret = "double";
      break;
    case MIXED:
      jj_consume_token(MIXED);
                    ret = "mixed";
      break;
    case FUNCTION_IDENTIFIER:
      jj_consume_token(FUNCTION_IDENTIFIER);
                                        ret = token.image;
      if (jj_2_2(2)) {
        jj_consume_token(OBJECT);
                                                                                        ret = ret + " object";
      } else {
        ;
      }
      break;
    default:
      jj_la1[31] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_3_1() {
    if (jj_3R_4()) return true;
    return false;
  }

  private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(34)) {
    jj_scanpos = xsp;
    if (jj_scan_token(27)) return true;
    }
    if (jj_scan_token(LPAREN)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_scan_token(OBJECT)) return true;
    return false;
  }

  /** Generated Token Manager. */
  public FunctionListParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[32];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0xfff00000,0xfff42800,0x48000,0x48000,0x48000,0xfff42800,0x48000,0x48000,0x48000,0x48000,0x2000,0x4000,0x2000,0x200,0x4000,0x1400,0x1400,0xffe02800,0x0,0x100,0x200000,0xc0,0xc0,0x8000000,0x8000,0x8200100,0x0,0x40,0x40000800,0x8000000,0xfff00000,0xffe00000,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x4,0x4,0x0,0x0,0x0,0x4,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x4,0x5,0xc0,0xf,0x0,0x0,0x4,0x0,0xcf,0xc,0x0,0x8,0x4,0x4,0x4,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[2];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public FunctionListParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public FunctionListParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new FunctionListParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public FunctionListParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FunctionListParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public FunctionListParser(FunctionListParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(FunctionListParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 32; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[40];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 32; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 40; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 2; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

          //}}}
}
