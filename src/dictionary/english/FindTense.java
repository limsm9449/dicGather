package dictionary.english;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FindTense {
	private static Connection conn;

	public static void main(String[] args) {
		System.out.println("FindTense Start");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);

			conn.setAutoCommit(false);

			gatherTense();
	        
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		System.out.println("FindWord End");
	}

	public static boolean gatherTense() throws Exception {
		int cnt = 0;

		PreparedStatement psTense = conn.prepareStatement(Query.getInsTense());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getFindWordTenseQuery());
		ResultSet rs = s.getResultSet();
		while (rs.next()) {
			String entryId = rs.getString("ENTRY_ID");
			
			try {
				String tense = getTense(entryId);
				//System.out.println(Calendar.getInstance().getTime() + " : " + entryId + ", " + tense);
				
				int idx = 1;
				psTense.setString(idx++, entryId);
				psTense.setString(idx++, tense);
				psTense.executeUpdate();
				
				if ( cnt++ % 500 == 0 ) {
					conn.commit();
				}
			} catch ( Exception e ) {
				System.out.println(Calendar.getInstance().getTime() + " : " + entryId + " timeout");
			}
		}
		conn.commit();
		
		rs.close();
		s.close();
		
		return true;
	}


	// 단어 상세 검색
	public static String getTense(String entryId) throws Exception {
		String url = "";
		String tense = "";

		url = "http://endic.naver.com/enkrEntry.nhn?sLn=kr&entryId=" + entryId;

		Document doc = Jsoup.connect(url).timeout(10000).get();
		Elements dl_es = doc.select("dl");
		for (Element dl_es_c : dl_es) {
			if ("sync".equals(dl_es_c.attr("class"))) {
				Elements dl_es_c_c = dl_es_c.children();
				for (int i = 0; i < dl_es_c_c.size(); i++) {
					if ("dd".equals(dl_es_c_c.get(i).tagName())) {
						for (Element dl_es_c_c_c : dl_es_c_c.get(i).child(0).children()) {
							for (Element dl_es_c_c_c_c : dl_es_c_c_c.children()) {
								if ("fnt_k10".equals(dl_es_c_c_c_c.attr("class"))) {
									tense += dl_es_c_c_c_c.text() + "^";
								} else if ("fnt_e07".equals(dl_es_c_c_c_c.attr("class"))) {
									tense += dl_es_c_c_c_c.text() + ",";
								}
							}
						}
					}
				}
			}
		}

		return tense;
	}
}