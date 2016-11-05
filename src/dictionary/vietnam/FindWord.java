package dictionary.vietnam;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dictionary.CommUtil;

public class FindWord {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("VietHan Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn = DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            while ( true ) {
            	try {
	            	gatherWord();
            	} catch ( Exception e1 ) {
            		e1.printStackTrace();
            	}
            }
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("VietHan End");
	}
	
	//http://vndic.naver.com/search.nhn?query=gia%20%C4%91%C3%ACnh
	public static void gatherWord() throws Exception {
		int wordCnt = 0;
		int examCnt = 0;
		int cnt = 0;

		PreparedStatement psDic = conn.prepareStatement(VietHanQuery.getInsDicQuery());
		PreparedStatement psTempSample = conn.prepareStatement(VietHanQuery.getInsTempSampleQuery());
		
		Statement s = conn.createStatement();
		s.executeQuery(VietHanQuery.getFindWordQuery());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findKind = rs.getString("KIND");
			String findWord = rs.getString("WORD");
			
			if ( !VietHanQuery.isExistSameWord(conn, findWord) ) {
	            //단어 갯수를 가져온다.
				String url = "http://vndic.naver.com/search.nhn?query=" + URLEncoder.encode(findWord,"UTF-8").replaceAll("[+]", "%20");
				Document doc = Jsoup.connect(url).timeout(10000).get();
	    		Elements els = doc.select("div");
	    		for ( Element els_e : els ) {
	    			//System.out.println(e.attr("class"));
	    			if ( els_e.attr("class").indexOf("srch_result srch_result_no") > -1 ) {
	    				break;
	    			} else if ( els_e.attr("class").indexOf("section_word") > -1 ) {
	    				//베한 단어
	    				Elements lis = els_e.child(0).children();
	    				String cntStr = (els_e.child(0).children()).first().text();
	    				wordCnt = Integer.parseInt(cntStr.replaceAll("[(]", "").replaceAll("[)]", "").replaceAll("[,]", ""));
	    			} else if ( els_e.attr("class").indexOf("section_exam") > -1 ) {
	    				//예문
	    				Elements lis = els_e.child(0).children();
	    				String cntStr = (els_e.child(0).children()).first().text();
	    				examCnt = Integer.parseInt(cntStr.replaceAll("[(]", "").replaceAll("[)]", "").replaceAll("[,]", ""));
	    			}
	    		}
	    		System.out.print("findWord : " + findWord + ", url : " + url + " -> " + wordCnt + ", " + examCnt);
	    		
	    		//단어 리스트를 가져온다.
	    		ArrayList words = null;
	    		if ( wordCnt > 0 ) {
		    		words = naverDicWordList(url, wordCnt);
		    		
		    		//단어를 저장한다.
		    		for ( int i = 0; i < words.size(); i++ ) {
		    			HashMap row = (HashMap)words.get(i);
		    			
		    			String word = (String)row.get("WORD");
		    			String entryId = (String)row.get("ENTRYID");
		    			String spelling = (String)row.get("SPELLING");
		    			if ("".equals(spelling) || spelling == null ) {
		    				spelling = "*";
		    			}
		    			String mean = (String)row.get("MEAN");
		    			String hanmun = (String)row.get("HANMUN");
		    			if ("".equals(hanmun) || hanmun == null ) {
		    				hanmun = "";
		    			}
		    			
		    			//System.out.println(", word : " + word + ", entryId : " + entryId + ", spelling : " + spelling + ", mean : " + mean + ", hanmun : " + hanmun);
		    			
		    			if ( !VietHanQuery.isExistDicForEntryId(conn, entryId) ) {
		    				int idx = 1;
		    				psDic.setString(idx++, entryId);
		    				psDic.setString(idx++, CommUtil.isHangleCode(word));
		    				psDic.setString(idx++, new String(word.getBytes(), "UTF-8"));
		    				psDic.setString(idx++, mean);
		    				psDic.setString(idx++, spelling);
		    				psDic.setString(idx++, hanmun);
		    				psDic.executeUpdate();
		    			}
		    		}
	    		}
	
	    		ArrayList samples = null;
	    		if ( examCnt > 0 ) {
		    		//단어 샘플을 수집한다.
					samples = naverDicInfoAllSample(findWord, examCnt);
					for ( int i = 0; i < samples.size(); i++ ) {
		    			HashMap row = (HashMap)samples.get(i);
		    			
		    			String langViet = (String)row.get("LANG_VIET");
		    			String langHan = (String)row.get("LANG_HAN");
		    			
		    			//System.out.println(", langViet : " + langViet + ", langHan : " + langHan);
		    			
		    			psTempSample.setString(1, langHan);
		    			psTempSample.setString(2, langViet);
		    			try {
		    				psTempSample.executeUpdate();
		    			} catch ( Exception e) {
		    				System.out.println("오류 --> langViet : " + langViet + ", langHan : " + langHan);
		    			}
		    			
		    		}
	    		}
	    		
	    		s.executeUpdate(VietHanQuery.getInsFindedWordQuery(findKind, findWord));
	    		s.executeUpdate(VietHanQuery.getDelFindWordQuery(findKind, findWord));
	
	    		System.out.println(" -> " + (words != null ? words.size() : 0) + ", " + (samples != null ? samples.size() : 0));
	    		
	    		conn.commit();
	    		
	    		cnt++;
	    		if ( cnt == Constant.findWordCount ) {
	    			break;
	    		}
			} else {
				System.out.println(findWord + " 있음");
				s.executeUpdate(VietHanQuery.getDelFindWordQuery(findKind, findWord));
			}
		}
		rs.close();
		psDic.close();
		psTempSample.close();
		s.close();
	}
	
	
	//단어 검색
	public static ArrayList naverDicWordList(String url, int cnt) throws Exception {
		ArrayList words = new ArrayList<HashMap>();
		int inCnt = 0;
		for ( int i = 1; i <= (cnt / 20) + 1; i++ ) {
		//for ( int i = 1; i <= 10; i++ ) {
			Document doc = Jsoup.connect(url.replaceAll("all", "entry") + "&range=entry&pageNo=" + i).timeout(10000).get();
			//System.out.println(doc);
			Elements els = doc.select("div");
			for ( Element els_e : els ) {
				//System.out.println(e.attr("class"));
				if ( els_e.attr("class").indexOf("section_word") > -1 ) {
					//System.out.println("*****" + els_e.toString());
					Elements lis = els_e.child(1).children();
					for ( Element lis_e : lis ) {
						//System.out.println("*****" + lis_e.toString());
						if ( "li".equals(lis_e.nodeName()) &&
								(lis_e.attr("class").indexOf("word_other") > -1 || "".equals(lis_e.attr("class").toString()))  && 
								lis_e.toString().indexOf("윅셔너리") < 0 &&
								lis_e.toString().indexOf("웹수집") < 0 &&
								lis_e.toString().indexOf("오픈사전") < 0 ) {
							HashMap hm = new HashMap();

							//단어
							Elements as = lis_e.getElementsByTag("a");
							//동일 단어 첨자는 제거한다. ex : 먹다1, 먹다2
							Elements as_t = as.first().children();
							for ( Element as_t_e : as_t ) {
								if ( "sup".equals(as_t_e.nodeName()) ) {
									as_t_e.remove();
								}
							}
							hm.put("WORD", as.first().text());

							//ENTRY ID
							String href = as.attr("href").toString();
							hm.put("ENTRYID", href.substring(href.indexOf("entryId") + 8));

							//스펠링
							Elements spans = lis_e.getElementsByTag("span");
							for ( Element spans_e : spans ) {
								if ( spans_e.attr("class").indexOf("sdmark") > -1 ) {
									hm.put("SPELLING", spans_e.text());
								} else if ( spans_e.attr("class").indexOf("txt_orignl") > -1 ) {
									hm.put("HANMUN", spans_e.text().replaceAll("[()]", ""));
								}
							}
							
							//뜻
							Elements means_li = lis_e.getElementsByTag("ul").first().getElementsByTag("li");
							String mean = "";
							for ( Element means_li_e : means_li ) {
								mean += ("".equals(mean) ? "" : "<br>") + means_li_e.text();
							}
							hm.put("MEAN", mean);
							
							//System.out.println(hm);
							
							words.add(hm);
						}
					}
				}
			}
			//System.out.println(url.replaceAll("all", "entry") + "&pageNo=" + i + " : " + words.size());
		}
		
		return words;
	}

	// 전체 예문
	public static ArrayList naverDicInfoAllSample(String findWord, int cnt) throws Exception {
		ArrayList exams = new ArrayList<HashMap>();
		int inCnt = 0;
		for ( int i = 1; i <= (cnt / 20) + 1; i++ ) {
		//for ( int i = 1; i <= 3; i++ ) {
			//System.out.println("http://vndic.naver.com/search.nhn?range=example&query=" + URLEncoder.encode(findWord,"UTF-8").replaceAll("[+]", "%20") + "&pageNo=" + i);
			Document doc = Jsoup.connect("http://vndic.naver.com/search.nhn?range=example&query=" + URLEncoder.encode(findWord,"UTF-8").replaceAll("[+]", "%20") + "&pageNo=" + i).timeout(10000).get();
			//System.out.println(doc);
			Elements els = doc.select("div");
			for ( Element els_e : els ) {
				//System.out.println(e.attr("class"));
				if ( els_e.attr("class").indexOf("section_exam search_example_div") > -1 ) {
					Elements lis = els_e.child(1).children();
					//System.out.println(lis);
					for ( Element lis_e : lis ) {
						//System.out.println(lis_e.getElementsByTag("li").get(1).text() + " : " + lis_e.getElementsByTag("li").get(2).text());
						HashMap hm = new HashMap();
						
						//System.out.println(lis_e.getElementsByTag("li").size());

						//구분이 비는 경우도 있어서 3인지를 체크해서 입력을 한다.
						if ( lis_e.getElementsByTag("li").size() == 3 ) {
							hm.put("LANG_VIET", lis_e.getElementsByTag("li").get(1).text());
							hm.put("LANG_HAN", lis_e.getElementsByTag("li").get(2).text());

							exams.add(hm);
						}
					}
				}
			}
		}
		
		return exams;
	}
	
}
