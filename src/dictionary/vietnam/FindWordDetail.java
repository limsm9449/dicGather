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

public class FindWordDetail {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("VietHan Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            while ( true ) {
            	try {
            		gatherWordDetail();
	                
	                conn.commit();
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
	public static void gatherWordDetail() throws Exception {
		int cnt = 0;
		
		PreparedStatement psDicMean = conn.prepareStatement(VietHanQuery.getInsDicMeanQuery());
		PreparedStatement psDicMeanSample = conn.prepareStatement(VietHanQuery.getInsDicMeanSampleQuery());
		PreparedStatement psDicWordSample = conn.prepareStatement(VietHanQuery.getInsDicWordSampleQuery());
		PreparedStatement psUpdFindedDicDetail = conn.prepareStatement(VietHanQuery.getUpdFindedDicDetailQuery());

		Statement s = conn.createStatement();
		s.executeQuery(VietHanQuery.getFindWordForDetailQuery());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findWord = rs.getString("WORD");
			String findEntryId = rs.getString("ENTRY_ID");
			String word = "";
			String spelling = "";
			String hanmun = "";
			String mean = "";
			ArrayList wordSample = new ArrayList<HashMap>();
			ArrayList wordMeanSample = new ArrayList<HashMap>();
			
			System.out.println("http://vndic.naver.com/entry.nhn?sLn=kr&entryId=" + findEntryId);
			Document doc = Jsoup.connect("http://vndic.naver.com/entry.nhn?sLn=kr&entryId=" + findEntryId).get();
			//System.out.println(doc);
			
			Elements divs = doc.select("div");
			for ( Element divs_e : divs ) {
				//System.out.println(ps_e.attr("class"));
				if ( divs_e.attr("class").indexOf("tit") > -1 ) {
					//System.out.println(els_e.toString());
					word = ((Elements)divs_e.select("h3")).get(0).text();

					Elements spans = divs_e.select("span");
					for ( Element span_e : spans ) {
						//System.out.println(ps_e.attr("class"));
						if ( span_e.attr("class").indexOf("sdmark") > -1 ) {
							//System.out.println(els_e.toString());
							spelling =  span_e.text();
						} else if ( span_e.attr("class").indexOf("txt_orignl") > -1 ) {
							//System.out.println(els_e.toString());
							hanmun = span_e.text();
						}
					}
				}
			}
			
			//단어 뜻
			Elements pse = doc.select("p");
			for ( Element ps_e : pse ) {
				//System.out.println(ps_e.attr("class"));
				if ( ps_e.attr("class").indexOf("p_info") > -1 ) {
					//System.out.println(els_e.toString());
					mean =  ps_e.text();
				}
			}

			//단어 예문
			Elements els = doc.select("div");
			for ( Element els_e : els ) {
				//System.out.println(e.attr("class"));
				if ( els_e.attr("class").indexOf("search_entry_div section_all") > -1 ) {
					//단어 뜻별 예문
					//System.out.println(els_e.toString());
					int idx = 1;
					
					Elements lis = els_e.child(0).children();
					for ( Element lis_e : lis ) {
						//System.out.println(lis_e);
						HashMap hm = new HashMap();
						ArrayList al = new ArrayList<HashMap>();
						
						//의미가 1개 또는 여려개일 경우 분리
						Elements lis_t = null;
						if ( "em".equals(lis_e.child(0).tagName()) ) {
							lis_t = lis_e.child(1).children();
						} else {
							lis_t = lis_e.child(0).children();
						}
						//System.out.println(lis_t);
						for ( Element lis_t_e : lis_t ) {
							//System.out.println("Log 1 : " + lis_t_e);
							if ( lis_t_e.toString().indexOf("\"lst_txt\"") > -1 ) {
								hm.put("MEAN", lis_t_e.text());
								mean += ("".equals(mean) ? "" : ", ") + lis_t_e.text();
							} else {
								HashMap li = new HashMap();
								Element p_e = lis_t_e.child(0);
								li.put("LANG_VIET", p_e.child(0).text());

								p_e.child(0).remove();
								li.put("LANG_HAN", lis_t_e.text().replaceAll("예문보기", ""));
								
								al.add(li);
							}
						}
						hm.put("SAMPLES", al);
						hm.put("IDX", idx++);
						wordMeanSample.add(hm);
						//System.out.println(hm);
					}
				} else if ( els_e.attr("class").indexOf("section_exam search_entry_div") > -1 ) {
					//단어 예문
					//System.out.println(els_e.toString());
					int idx = 1;
					
					Elements lis = els_e.child(1).children();
					for ( Element lis_e : lis ) {
						//System.out.println(lis_e);
						HashMap hm = new HashMap();
						
						Elements lis_t = lis_e.child(0).children();
						//System.out.println(lis_t);
						HashMap li = null;
						for ( Element lis_t_e : lis_t ) {
							//System.out.println("Log 1 : " + lis_t_e);
							if ( lis_t_e.toString().indexOf("lst_txt ") > -1 ) {
								hm.put("LANG_VIET", lis_t_e.text());
							} else {
								hm.put("LANG_HAN", lis_t_e.text());
							}
						}
						hm.put("IDX", idx++);
						wordSample.add(hm);
						//System.out.println(hm);
					}
				}
			}

			System.out.println("findEntryId : " + findEntryId + ", word : " + word + ", spelling : " + spelling + ", hanmun : " + hanmun + ", mean : " + mean);
			//System.out.println(wordSample.toString());
			//System.out.println(wordMeanSample.toString());

			for ( int i = 0; i < wordMeanSample.size(); i++ ) {
				HashMap row  = (HashMap)wordMeanSample.get(i);

				//뜻 저장
				psDicMean.setString(1, findEntryId);
				psDicMean.setInt(2, (int)row.get("IDX"));
				psDicMean.setString(3, (String)row.get("MEAN"));
				psDicMean.executeUpdate();

				//뜻별 샘픔 저장
				ArrayList sampleRow = (ArrayList)row.get("SAMPLES");
				for ( int k = 0; k < sampleRow.size(); k++ ) {
					HashMap row2  = (HashMap)sampleRow.get(k);

					System.out.println((String)row2.get("LANG_HAN") + (String)row2.get("LANG_VIET"));
					try {
						psDicMeanSample.setString(1, findEntryId);
						psDicMeanSample.setInt(2, (int)row.get("IDX"));
						psDicMeanSample.setString(3, (String)row2.get("LANG_HAN"));
						psDicMeanSample.setString(4, (String)row2.get("LANG_VIET"));
						psDicMeanSample.executeUpdate();
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
			}

			for ( int i = 0; i < wordSample.size(); i++ ) {
				HashMap row  = (HashMap)wordSample.get(i);

				try {
					psDicWordSample.setString(1, findEntryId);
					psDicWordSample.setString(2, (String)row.get("LANG_HAN"));
					psDicWordSample.setString(3, (String)row.get("LANG_VIET"));
					psDicWordSample.executeUpdate();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			psUpdFindedDicDetail.setString(1, findEntryId);
			psUpdFindedDicDetail.executeUpdate();
			
			conn.commit();
			
    		cnt++;
    		if ( cnt == Constant.findWordDetailCount ) {
    			break;
    		}
		}
		rs.close();
		psDicMean.close();
		psDicMeanSample.close();
		psDicWordSample.close();
		s.close();
	}
	
	
	
}
