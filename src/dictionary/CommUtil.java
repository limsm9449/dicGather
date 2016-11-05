package dictionary;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CommUtil {
   

	
	/**
	 * 두 변수가 같은지를 비교한다.
	 * @param val1
	 * @param val2
	 * @return
	 * @throws Exception
	 */
	public static boolean isEqual(String val1, String val2) throws Exception {
		String compVal1 = (val1 == null ? "" : val1);
		String compVal2 = (val2 == null ? "" : val2);
		
		if ( compVal1.equals(compVal2) )
			return true;
		else
			return false;
	}
	
	/**
	 * 한글 여부를 판단한다.
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static boolean isHangule(String pStr) throws Exception {
		boolean isHangule = false;
		String str = (pStr == null ? "" : pStr);
		try {
			if(str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
				isHangule = true;
			} else {
				isHangule = false;
			}
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		}
		
		return isHangule;
	}
	
	public static String isHangleCode(String str) throws Exception {
		if ( isHangule(str) ) {
			return "HV";
		} else {
			return "VH";
		}
	}
	

	public static void logQueryPrint(String sql) {
		System.out.println(sql);
	}
	
	public static void logPrint(String log) {
		System.out.println(log);
	}

	public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        String tmpSentence = sentence + " ";

        int startPos = 0;
        for ( int i = 0; i < tmpSentence.length(); i++ ) {
            if ( CommConstant.sentenceSplitStr.indexOf(tmpSentence.substring(i, i + 1)) > -1 ) {
                if ( i == 0 ) {
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                } else {
                    if ( i != startPos ) {
                        al.add(tmpSentence.substring(startPos, i));
                    }
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }
	
	public static String getString(String str) throws Exception {
		if ( str == null )
			return "";
		else
			return str.trim();
	}
	
	public static Document getDocument(String url) throws Exception {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(60000).get();
				break;
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		return doc;
	}

	public static Element findElementSelect(Document doc, String tag, String attr, String value) throws Exception {
		Elements es = doc.select(tag);
		for (Element es_r : es) {
			if (value.equals(es_r.attr(attr))) {
				return es_r;
			}
		}

		return null;
	}

	public static Element findElementForTag(Element e, String tag, int findIdx) throws Exception {
		if (e == null) {
			return null;
		}

		int idx = 0;
		for (int i = 0; i < e.children().size(); i++) {
			if (tag.equals(e.child(i).tagName())) {
				if (idx == findIdx) {
					return e.child(i);
				} else {
					idx++;
				}
			}
		}

		return null;
	}

	public static Element findElementForTagAttr(Element e, String tag, String attr, String value) throws Exception {
		if (e == null) {
			return null;
		}

		for (int i = 0; i < e.children().size(); i++) {
			if (tag.equals(e.child(i).tagName()) && value.equals(e.child(i).attr(attr))) {
				return e.child(i);
			}
		}

		return null;
	}

	public static String getAttrForTagIdx(Element e, String tag, int findIdx, String attr) throws Exception {
		if (e == null) {
			return null;
		}

		int idx = 0;
		for (int i = 0; i < e.children().size(); i++) {
			if (tag.equals(e.child(i).tagName())) {
				if (idx == findIdx) {
					return e.child(i).attr(attr);
				} else {
					idx++;
				}
			}
		}

		return "";
	}

	public static String getElementText(Element e) throws Exception {
		if (e == null) {
			return "";
		} else {
			return e.text();
		}
	}

	public static String getElementHtml(Element e) throws Exception {
		if (e == null) {
			return "";
		} else {
			return e.html();
		}
	}

	public static String getUrlParamValue(String url, String param) throws Exception {
		String rtn = "";

		if (url.indexOf("?") < 0) {
			return "";
		}
		String[] split_url = url.split("[?]");
		String[] split_param = split_url[1].split("[&]");
		for (int i = 0; i < split_param.length; i++) {
			String[] split_row = split_param[i].split("[=]");
			if (param.equals(split_row[0])) {
				rtn = split_row[1];
			}
		}

		return rtn;
	}

	
	public static Elements getChildElement( Element tElement, String tag, int pos ) throws Exception {
		int findPos = 0;
		for (int i = 0; i < tElement.children().size(); i++) {
			if ( tag.equals(tElement.child(i).tagName()) ) {
				if ( findPos == pos ) {
					return tElement.child(i).children();
				} else {
					findPos++;
				}
			}
		}

		return null;
	}

	public static Element getElement( Element tElement, String tag, int pos ) throws Exception {
		int findPos = 0;
		for (int i = 0; i < tElement.children().size(); i++) {
			if ( tag.equals(tElement.child(i).tagName()) ) {
				if ( findPos == pos) {
					return tElement.child(i);
				} else {
					findPos++;
				}
			}
		}

		return null;
	}


}
