package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.Map;

public class AccentTokenFilter extends TokenFilter {
	private static Map<Character, String> am;

	static {
		am = new HashMap<Character, String>();
		am.put('À',"A");
		am.put('Á',"A");
		am.put('Â',"A");
		am.put('Ã',"A");
		am.put('Ä',"A");
		am.put('Å',"A");
		am.put('Æ',"AE");
		am.put('Ç',"C");
		am.put('È',"E");
		am.put('É',"E");
		am.put('Ê',"E");
		am.put('Ë',"E");
		am.put('Ì',"I");
		am.put('Í',"I");
		am.put('Î',"I");
		am.put('Ï',"I");
		am.put('Ĳ',"IJ");
		am.put('Ð',"D");
		am.put('Ñ',"N");
		am.put('Ò',"O");
		am.put('Ó',"O");
		am.put('Ô',"O");
		am.put('Õ',"O");
		am.put('Ö',"O");
		am.put('Ø',"O");
		am.put('Œ',"OE");
		am.put('Ù',"U");
		am.put('Ú',"U");
		am.put('Û',"U");
		am.put('Ü',"U");
		am.put('Ý',"Y");
		am.put('Ÿ',"Y");
		am.put('à',"a");
		am.put('á',"a");
		am.put('â',"a");
		am.put('ã',"a");
		am.put('ä',"a");
		am.put('å',"a");
		am.put('æ',"ae");
		am.put('ç',"c");
		am.put('è',"e");
		am.put('é',"e");
		am.put('ê',"e");
		am.put('ë',"e");
		am.put('ì',"i");
		am.put('í',"i");
		am.put('î',"i");
		am.put('ï',"i");
		am.put('ĳ',"ij");
		am.put('ð',"d");
		am.put('ñ',"n");
		am.put('ò',"o");
		am.put('ó',"o");
		am.put('ô',"o");
		am.put('õ',"o");
		am.put('ö',"o");
		am.put('ø',"o");
		am.put('œ',"oe");
		am.put('ß',"ss");
		am.put('þ',"th");
		am.put('ù',"u");
		am.put('ú',"u");
		am.put('û',"u");
		am.put('ü',"u");
		am.put('ý',"y");
		am.put('ÿ',"y");
		am.put('ﬀ',"ff");
		am.put('ﬁ',"fi");
		am.put('ﬂ',"fl");
		am.put('ﬃ',"ffi");
		am.put('ﬄ',"ffl");
		am.put('ﬅ',"ft");
		am.put('ﬆ',"st");
	}
	public AccentTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		Token token;
		StringBuffer sb;
		String val;
		char[] tokenBuffer;
		if(ts.hasNext()) {
			token = ts.next();
			sb = new StringBuffer();
			tokenBuffer = token.getTermBuffer();
			for(int i=0; i<tokenBuffer.length; i++) {
				val = am.get(tokenBuffer[i]);
				if(val != null) {
					sb.append(val);
				} else {
					sb.append(tokenBuffer[i]);
				}
			}
			ts.getCurrent().setTermText(sb.toString());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

}
