package dictionary.daum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dictionary.CommUtil;
import java.util.HashMap;

public class Query {

	public static void logQuery(String logStr) {
		System.out.println(logStr);
	}
	
	
	public static String getInsCategoryQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC_DAUM_CATEGORY(CATEGORY_ID, CATEGORY_NAME, KIND, WORD_CNT, BOOKMARK_CNT, UPD_DATE) VALUES(?, ?, ?, ?, ?, ?)";
		
		return sql;
	}
	
	public static String getUpdCategoryQuery() {
		String sql = "";
		
		sql += "UPDATE DIC_DAUM_CATEGORY SET CATEGORY_NAME = ?, WORD_CNT = ?, BOOKMARK_CNT = ?, UPD_DATE = ? WHERE CATEGORY_ID = ?";
		
		return sql;
	}
	
	public static String getUpdBookmarkQuery() {
		String sql = "";
		
		sql += "UPDATE DIC_DAUM_CATEGORY SET BOOKMARK_CNT = ? WHERE CATEGORY_ID = ?";
		
		return sql;
	}

	public static String getInsVocabularyQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC_DAUM_VOCABULARY(CATEGORY_ID, WORD, MEAN, SPELLING, SAMPLES, MEMOS) VALUES(?, ?, ?, ?, ?, ?)";
		
		return sql;
	}
	
	public static String getDelFindWordQuery() {
		String sql = "";
		
		sql += "DELETE FROM DIC_DAUM";
		
		return sql;
	}
	
	public static String getDelVocabularyQuery() {
		String sql = "";
		
		sql += "DELETE FROM DIC_DAUM_VOCABULARY WHERE CATEGORY_ID = ?";
		
		return sql;
	}
	
	public static boolean isExistCategory(Connection conn, String categoryId) throws Exception  {
		boolean rtn = false;
	
		String sql = "";
		sql += "SELECT COUNT(WORD) AS CNT FROM DIC_CATEGORY WHERE CATEGORY_ID = ?";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, categoryId);
		ResultSet rs = ps.executeQuery();
		if ( rs.next() ) {
			String cnt = rs.getString("CNT");
			if ( Integer.parseInt(cnt) > 0 ) {
				//CommUtil.logPrint(word + " : " + cnt);
				rtn = true;
			}
		}
		rs.close();
		ps.close();
		
		return rtn;
	}
	
	public static HashMap getCategoryInfo(Connection conn, String categoryId) throws Exception  {
		HashMap rtn = new HashMap();
	
		String sql = "";
		sql += "SELECT * FROM DIC_DAUM_CATEGORY WHERE CATEGORY_ID = ?";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, categoryId);
		ResultSet rs = ps.executeQuery();
		if ( rs.next() ) {
			rtn.put("CATEGORY_ID", rs.getString("CATEGORY_ID"));
			rtn.put("CATEGORY_NAME", rs.getString("CATEGORY_NAME"));
			rtn.put("WORD_CNT", rs.getString("WORD_CNT"));
			rtn.put("UPD_DATE", rs.getString("UPD_DATE"));
			rtn.put("BOOKMARK_CNT", rs.getString("BOOKMARK_CNT"));
		}
		rs.close();
		ps.close();
		
		return rtn;
	}
	
}
