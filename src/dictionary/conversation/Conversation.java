package dictionary.conversation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Conversation {
	static String domainUrl = "http://phrasebook.naver.com/";

	public static void main(String[] args) {
		System.out.println("Conversation Start");

		//System.setProperty("http.proxyHost", "17.251.10.6");
		//System.setProperty("http.proxyPort", "8080");

		try {
			gatherConversationLarge("http://phrasebook.naver.com/detail.nhn?targetLanguage=en");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Conversation End");
	}

	public static void gatherConversationLarge(String url) throws Exception {
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
		Elements a_es = doc.select("a");
		for (Element a_es_r : a_es) {
			// 중분류 찾기..
			if (a_es_r.attr("href").indexOf("bigCategoryNo") > -1
					&& a_es_r.attr("href").indexOf("middleCategoryNo") < 0) {
				// System.out.println(a_es_r.text() + " : " +
				// a_es_r.attr("href"));
				gatherConversationMiddle(a_es_r.text(), domainUrl + a_es_r.attr("href"));
			}
		}
	}

	public static void gatherConversationMiddle(String kind, String url) throws Exception {
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
		Elements a_es = doc.select("a");
		// System.out.println(a_es);
		for (Element a_es_r : a_es) {
			// 대분류 찾기..
			if (a_es_r.attr("href").indexOf("bigCategoryNo") > -1
					&& a_es_r.attr("href").indexOf("middleCategoryNo") > -1
					&& a_es_r.attr("href").indexOf("smallCategoryNo") < 0) {
				// System.out.println(kind + " : " + a_es_r.text() + " : " +
				// a_es_r.attr("href"));
				gatherConversationSmall(kind + " > " + a_es_r.text(), domainUrl + a_es_r.attr("href"));
			}
		}
	}

	public static void gatherConversationSmall(String kind, String url) throws Exception {
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
		Elements a_es = doc.select("a");
		// System.out.println(a_es);
		for (Element a_es_r : a_es) {
			// 소분류 찾기..
			if (a_es_r.attr("href").indexOf("bigCategoryNo") > -1
					&& a_es_r.attr("href").indexOf("middleCategoryNo") > -1
					&& a_es_r.attr("href").indexOf("smallCategoryNo") > -1 && !"자주 쓰이는 표현".equals(a_es_r.text())) {
				// System.out.println(kind + " > " + a_es_r.text() + " : " +
				// a_es_r.attr("href"));
				gatherConversation(kind + " > " + a_es_r.text(), domainUrl + a_es_r.attr("href"));
			}
		}
	}

	public static void gatherConversation(String kind, String url) throws Exception {
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
		Elements a_es = doc.select("div");
		// System.out.println(a_es);
		int idx = 1;
		for (Element a_es_r : a_es) {
			if (a_es_r.attr("class").equals("dic_cont")) {
				// 대화
				Elements li_es = a_es_r.select("li");
				String ab = "A";
				for (Element li_es_r : li_es) {
					if (li_es_r.attr("class").indexOf("cont_info") > -1
							&& li_es_r.attr("class").indexOf("unisex2") < 0) {
						System.out.println(kind + " | " + idx++ + " | " + ab + ". " + li_es_r.child(1).child(0).text()
								+ " : " + li_es_r.child(0).text());
					}
					ab = ("A".equals(ab) ? "B" : "A");
				}
			} else if (a_es_r.attr("class").equals("dic_cont cont_type")) {
				// 문장
				Elements li_es = a_es_r.select("li");
				for (Element li_es_r : li_es) {
					if (li_es_r.attr("class").indexOf("cont_info") > -1
							&& li_es_r.attr("class").indexOf("unisex2") < 0) {
						System.out.println(kind + " | " + idx++ + " | " + li_es_r.child(1).child(0).text() + " : "
								+ li_es_r.child(0).text());
					}
				}
			}
		}
	}

}
