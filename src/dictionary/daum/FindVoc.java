package dictionary.daum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import dictionary.CommUtil;
import dictionary.english.Constant;
import dictionary.english.MUtil;

public class FindVoc {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("FindVoc Start");

		try {
			//Class.forName("com.mysql.jdbc.Driver");
			//conn = DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);
			
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:d:/eng_db.db");
			
			conn.setAutoCommit(false);
			
			//최신순 순서로 갱신
			gather("http://wordbook.daum.net/open/wordbook/list.do?dic_type=endic&order=recent");
			
			//즐겨찾기 순서로 갱신
			//gather("http://wordbook.daum.net/open/wordbook/list.do?dic_type=endic&order=scrap");
			
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
		}

		System.out.println("FindVoc End");
	}

	public static void gather(String url) throws Exception {
		gatherCategory(url + "&theme=1", "TOEIC");
		gatherCategory(url + "&theme=2", "TOEFL");
		gatherCategory(url + "&theme=3", "TEPS");
		gatherCategory(url + "&theme=4", "수능영어");
		gatherCategory(url + "&theme=5", "NEAT/NEPT");
		gatherCategory(url + "&theme=12", "초중고영어");
		gatherCategory(url + "&theme=9", "회화");
		gatherCategory(url + "&theme=13", "기타");
	}
	
	public static void gatherCategory(String url, String kind) throws Exception {
		int pageIdx = 1;
		while ( true ) {
			Document doc = CommUtil.getDocument(url + "&page=" + pageIdx);
			System.out.println(url + "&page=" + pageIdx);
			
			Element table_e = CommUtil.findElementSelect(doc, "table", "class", "tbl_wordbook");
			Element tbody_e = CommUtil.findElementForTag(table_e, "tbody", 0);
			for (int m = 0; m < tbody_e.children().size(); m++) {
				Element category = CommUtil.findElementForTag(tbody_e.child(m), "td", 1);

				String categoryId = CommUtil.getUrlParamValue(category.child(0).attr("href"), "id").replace("\n", "");
				
				HashMap info = Query.getCategoryInfo(conn, categoryId);
				String categoryName = category.text();
				String wCnt = CommUtil.findElementForTag(tbody_e.child(m), "td", 3).text();
				String bookmarkCnt = CommUtil.findElementForTag(tbody_e.child(m), "td", 4).text();
				String updDate = CommUtil.findElementForTag(tbody_e.child(m), "td", 5).text();
				System.out.println("categoryId : " + categoryId + " ,categoryName : " + categoryName + " ,wCnt : " + wCnt + " ,bookmarkCnt : " + bookmarkCnt + " ,updDate : " + updDate);
				
				int idx = 0;
				if ( info.containsKey("CATEGORY_ID") ) {
					PreparedStatement psUpdCategory = conn.prepareStatement(Query.getUpdCategoryQuery());
					
					idx = 1;
					psUpdCategory.setString(idx++, categoryName);
					psUpdCategory.setString(idx++, wCnt);
					psUpdCategory.setString(idx++, bookmarkCnt);
					psUpdCategory.setString(idx++, updDate);
					psUpdCategory.setString(idx++, categoryId);
					psUpdCategory.executeUpdate();
					
					PreparedStatement psDelVocabulary = conn.prepareStatement(Query.getDelVocabularyQuery());
					
					idx = 1;
					psDelVocabulary.setString(idx++, categoryId);
					psDelVocabulary.executeUpdate();
					
					System.out.println("categoryId Upd: " + categoryId);
				} else {
					PreparedStatement psInsCategory = conn.prepareStatement(Query.getInsCategoryQuery());
	
					idx = 1;
					psInsCategory.setString(idx++, categoryId);
					psInsCategory.setString(idx++, categoryName);
					psInsCategory.setString(idx++, kind);
					psInsCategory.setString(idx++, wCnt);
					psInsCategory.setString(idx++, bookmarkCnt);
					psInsCategory.setString(idx++, updDate);
					psInsCategory.executeUpdate();
					System.out.println("categoryId Ins: " + categoryId);
				}
				
				ArrayList wordsAl = gatherCategoryWord("http://wordbook.daum.net/open/wordbook.do?id=" + categoryId, Integer.parseInt(wCnt));
				PreparedStatement psInsDictionary = conn.prepareStatement(Query.getInsVocabularyQuery());
				for (int is = 0; is < wordsAl.size(); is++) {
					HashMap row = (HashMap) wordsAl.get(is);

					idx = 1;
					psInsDictionary.setString(idx++, categoryId);
					psInsDictionary.setString(idx++, (String)row.get("WORD"));
					psInsDictionary.setString(idx++, (String)row.get("MEAN"));
					psInsDictionary.setString(idx++, (String)row.get("SPELLING"));
					psInsDictionary.setString(idx++, (String)row.get("SAMPLES"));
					psInsDictionary.setString(idx++, (String)row.get("MEMO"));
					psInsDictionary.executeUpdate();
				}
			}
			
			conn.commit();
			
			HashMap pageHm = new HashMap();
            Element div_paging = CommUtil.findElementSelect(doc, "div", "class", "paging_comm paging_type1");
            for (int is = 0; is < div_paging.children().size(); is++) {
                if ("a".equals(div_paging.child(is).tagName())) {
                    HashMap row = new HashMap();

                    String page = CommUtil.getUrlParamValue(div_paging.child(is).attr("href"), "page");
                    pageHm.put(page, page);
                }
            }
            // 페이지 정보중에 다음 페이지가 없으면 종료...
            if (!pageHm.containsKey(Integer.toString(pageIdx + 1))) {
                break;
            }
            
            pageIdx++;
		}
	}
	
	public static void gatherCategoryRecentInfo(String url, String kind) throws Exception {
		int i = 1;
		boolean isBreak = false;
		while ( true ) {
			Document doc = CommUtil.getDocument(url + "&page=" + i);
			System.out.println(url + "&page=" + i);
			
			Element table_e = CommUtil.findElementSelect(doc, "table", "class", "tbl_wordbook");
			Element tbody_e = CommUtil.findElementForTag(table_e, "tbody", 0);
			for (int m = 0; m < tbody_e.children().size(); m++) {
				Element category = CommUtil.findElementForTag(tbody_e.child(m), "td", 1);

				String categoryId = CommUtil.getUrlParamValue(category.child(0).attr("href"), "id").replace("\n", "");
				
				HashMap info = Query.getCategoryInfo(conn, categoryId);
				String categoryName = category.text();
				String wCnt = CommUtil.findElementForTag(tbody_e.child(m), "td", 3).text();
				String bookmarkCnt = CommUtil.findElementForTag(tbody_e.child(m), "td", 4).text();
				String updDate = CommUtil.findElementForTag(tbody_e.child(m), "td", 5).text();
				System.out.println("categoryId : " + categoryId + " ,categoryName : " + categoryName + " ,wCnt : " + wCnt + " ,bookmarkCnt : " + bookmarkCnt + " ,updDate : " + updDate);
				
				int idx = 0;
				if ( info.containsKey("CATEGORY_ID") ) {
					System.out.println("  = categoryId : " + categoryId + " ,categoryName : " + info.get("CATEGORY_NAME") + " ,wCnt : " + info.get("WORD_CNT") + " ,bookmarkCnt : " + info.get("BOOKMARK_CNT") + " ,updDate : " + info.get("UPD_DATE"));
					if ( !CommUtil.getString(categoryName).equals(info.get("CATEGORY_NAME")) ||
							!CommUtil.getString(wCnt).equals(info.get("WORD_CNT")) ||
							!CommUtil.getString(bookmarkCnt).equals(info.get("BOOKMARK_CNT")) ||
							!CommUtil.getString(updDate).equals(info.get("UPD_DATE")) ) {
						PreparedStatement psUpdCategory = conn.prepareStatement(Query.getUpdCategoryQuery());
						
						idx = 1;
						psUpdCategory.setString(idx++, categoryName);
						psUpdCategory.setString(idx++, wCnt);
						psUpdCategory.setString(idx++, bookmarkCnt);
						psUpdCategory.setString(idx++, updDate);
						psUpdCategory.setString(idx++, categoryId);
						psUpdCategory.executeUpdate();
						
						PreparedStatement psDelVocabulary = conn.prepareStatement(Query.getDelVocabularyQuery());
						
						idx = 1;
						psDelVocabulary.setString(idx++, categoryId);
						psDelVocabulary.executeUpdate();
						
						System.out.println("categoryId Upd: " + categoryId);
					} else {
						System.out.println("categoryId Break: " + categoryId);

						isBreak = true;
						break;
					}
				} else {
					PreparedStatement psInsCategory = conn.prepareStatement(Query.getInsCategoryQuery());
					
	
					idx = 1;
					psInsCategory.setString(idx++, categoryId);
					psInsCategory.setString(idx++, categoryName);
					psInsCategory.setString(idx++, kind);
					psInsCategory.setString(idx++, wCnt);
					psInsCategory.setString(idx++, bookmarkCnt);
					psInsCategory.setString(idx++, updDate);
					psInsCategory.executeUpdate();
					System.out.println("categoryId Ins: " + categoryId);
				}
				
				ArrayList wordsAl = gatherCategoryWord("http://wordbook.daum.net/open/wordbook.do?id=" + categoryId, Integer.parseInt(wCnt));
				PreparedStatement psInsDictionary = conn.prepareStatement(Query.getInsVocabularyQuery());
				for (int is = 0; is < wordsAl.size(); is++) {
					HashMap row = (HashMap) wordsAl.get(is);

					idx = 1;
					psInsDictionary.setString(idx++, categoryId);
					psInsDictionary.setString(idx++, (String)row.get("WORD"));
					psInsDictionary.setString(idx++, (String)row.get("MEAN"));
					psInsDictionary.setString(idx++, (String)row.get("SPELLING"));
					psInsDictionary.setString(idx++, (String)row.get("SAMPLES"));
					psInsDictionary.setString(idx++, (String)row.get("MEMO"));
					psInsDictionary.executeUpdate();
				}
			}
			
			conn.commit();
			
			if ( isBreak ) {
				break;
			}
			
			/*
			HashMap pageHm = new HashMap();
            Element div_paging = CommUtil.findElementSelect(doc, "div", "class", "paging_comm paging_type1");
            for (int is = 0; is < div_paging.children().size(); is++) {
                if ("a".equals(div_paging.child(is).tagName())) {
                    HashMap row = new HashMap();

                    String page = CommUtil.getUrlParamValue(div_paging.child(is).attr("href"), "page");
                    pageHm.put(page, page);
                }
            }
            // 페이지 정보중에 다음 페이지가 없으면 종료...
            if (!pageHm.containsKey(Integer.toString(i + 1))) {
                break;
            }
            */
		}
	}
	
	public static ArrayList gatherCategoryWord(String url, int wordCnt) throws Exception {
		ArrayList wordAl = new ArrayList();
		for (int i = 1; i <= (wordCnt / 10) + 1; i++) {
			Document doc = CommUtil.getDocument(url + "&page=" + i);
			Element div_e = CommUtil.findElementSelect(doc, "div", "class", "list_word on");
			for (int is = 0; is < div_e.children().size(); is++) {
				if ("div".equals(div_e.child(is).tagName())) {
					HashMap row = new HashMap();

					Element wordDiv = CommUtil.findElementForTagAttr(div_e.child(is), "div", "class", "txt_word");
					Element meanDiv = CommUtil.findElementForTagAttr(div_e.child(is), "div", "class", "mean_info");

					if ( meanDiv != null ) {
						String sample = "";
						for (int iss = 0; iss < meanDiv.children().size(); iss++) {
							if ("div".equals(meanDiv.child(iss).tagName())
									&& "desc_example".equals(meanDiv.child(iss).attr("class"))) {
								sample += (sample.length() == 0 ? "" : "^")
										+ CommUtil.getElementText(CommUtil.findElementForTagAttr(meanDiv.child(iss), "em", "class", "txt_example"))
										+ " : " + CommUtil.getElementText(CommUtil.findElementForTagAttr(meanDiv.child(iss), "p", "class", "txt_trans"));
							}
						}
	
						row.put("WORD", wordDiv.child(0).child(0).text());
						row.put("SPELLING", CommUtil.getElementText(CommUtil.findElementForTagAttr(wordDiv.child(0), "span", "class", "pron_wordbook")));
						row.put("SAMPLES", sample);
	
						Element meanE = CommUtil.findElementForTag(meanDiv, "p", 0);
						Element meanE2 = CommUtil.findElementForTagAttr(meanE, "span", "class", "link_mean");
						row.put("MEAN", CommUtil.getElementText(CommUtil.findElementForTagAttr(meanE, "span", "class", "link_mean")));
	
						Element memoE = CommUtil.findElementForTagAttr(meanDiv, "div", "class", "wrap_memo on");
						Element memoE2 = CommUtil.findElementForTagAttr(memoE, "fieldset", "class", "fld_memo");
						Element memoE3 = CommUtil.findElementForTagAttr(memoE2, "div", "class", "box_memo");
						Element memoE4 = CommUtil.findElementForTagAttr(memoE3, "p", "class", "txt_memo");
						row.put("MEMO", CommUtil.getElementHtml(memoE4).replaceAll("<br />", "\n"));
	
						wordAl.add(row);
					}
				}
			}
		}

		return wordAl;
	}

}
