package dictionary.english;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class MUtil {
   
	public static String getString(String str) throws Exception {
		if ( str == null )
			return "";
		else
			return str.trim();
	}
	
	public static String getLangKind(boolean isHan) throws Exception {
		if ( isHan ) {
			return Constant.kind_h;
		} else {
			return Constant.kind_f;
		}
	}

	public static boolean isOkWord(String word) throws Exception {
		int blankLength = 0;
		for ( int i = 0; i < word.length(); i++ ) {
			if ( " ".equals(word.substring(i, i + 1)) ) {
				blankLength++;
			}
		}
		
		if ( word.length() == 1 ) {
			return false;
		} else if ( blankLength <= 5 ) { 
			return true;
		} else {
			return false;
		}
	}

}
