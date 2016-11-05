package dictionary.vietnam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dictionary.CommUtil;
import java.util.HashMap;

public class VietHanQuery {

	public static void logQuery(String logStr) {
		System.out.println(logStr);
	}
	
	public static String getFindWordQuery() {
		String sql = "";
		
		sql += "SELECT DISTINCT KIND, WORD FROM T_FIND_DIC ORDER BY KIND DESC, WORD";
		
		return sql;
	}
	
	public static boolean isExistSameWord(Connection conn, String word) throws Exception  {
		boolean rtn = false;
	
		String sql = "";
		sql += "SELECT COUNT(WORD) AS CNT FROM DIC WHERE LOWER(WORD) = LOWER(?)";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, word);
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
	
	public static boolean isExistWord(Connection conn, String word) throws Exception  {
		boolean rtn = false;
	
		String sql = "";
		sql += "SELECT COUNT(WORD) AS CNT FROM DIC WHERE LOWER(WORD) COLLATE utf8_bin = LOWER(?)";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, word);
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

	public static boolean isExistDicForEntryId(Connection conn, String entryId) throws Exception  {
		boolean rtn = false;
	
		String sql = "";
		sql += "SELECT COUNT(WORD) AS CNT FROM DIC WHERE ENTRY_ID = ?";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, entryId);
		ResultSet rs = ps.executeQuery();
		if ( rs.next() ) {
			String cnt = rs.getString("CNT");
			if ( Integer.parseInt(cnt) > 0 ) {
				//CommUtil.logPrint(entryId + " : " + cnt);
				rtn = true;
			}
		}
		rs.close();
		ps.close();
		
		return rtn;
	}
	
	public static String getInsFindedWordQuery(String kind, String word) {
		String sql = "";
		
		sql += "INSERT INTO T_FINDED_DIC(KIND, WORD) VALUES('" + kind + "','" + word +"')";
		
		return sql;
	}

	public static String getDelFindWordQuery(String kind, String word) {
		String sql = "";
		
		sql += "DELETE FROM T_FIND_DIC WHERE KIND = '" + kind + "' AND WORD COLLATE utf8_bin = '" + word +"'";
		
		return sql;
	}

	public static String getInsDicQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC (ENTRY_ID, KIND, WORD, MEAN, SPELLING, HANMUN) VALUES(?, ?, ?, ?, ?, ?)";
		
		return sql;
	}
	
	public static String getInsTempSampleQuery() {
		String sql = "";
		
		sql += "INSERT INTO T_SAMPLE(LANG_HAN, LANG_VIET) VALUES(?, ?)";
		
		return sql;
	}

	public static String getInsSampleFromTempSampleQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC_SAMPLE(LANG_HAN, LANG_VIET, FLAG) ";
		sql += "SELECT DISTINCT LANG_HAN, LANG_VIET, 'N' ";
		sql += "  FROM T_SAMPLE ";
		sql += " WHERE (LANG_HAN, LANG_VIET) NOT IN ( SELECT LANG_HAN, LANG_VIET FROM DIC_SAMPLE ) ";
		
		return sql;
	}

	public static String getInsSampleFromMeanSampleQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC_SAMPLE(LANG_HAN, LANG_VIET, FLAG) ";
		sql += "SELECT DISTINCT LANG_HAN, LANG_VIET, 'N' ";
		sql += "  FROM DIC_MEAN_SAMPLE ";
		sql += " WHERE (LANG_HAN, LANG_VIET) NOT IN ( SELECT LANG_HAN, LANG_VIET FROM DIC_SAMPLE ) ";
		
		return sql;
	}

	public static String getInsSampleFromWordSampleQuery() {
		String sql = "";
		
		sql += "INSERT INTO DIC_SAMPLE(LANG_HAN, LANG_VIET, FLAG) ";
		sql += "SELECT DISTINCT LANG_HAN, LANG_VIET, 'N' ";
		sql += "  FROM DIC_WORD_SAMPLE ";
		sql += " WHERE (LANG_HAN, LANG_VIET) NOT IN ( SELECT LANG_HAN, LANG_VIET FROM DIC_SAMPLE ) ";
		
		return sql;
	}

	public static String getDelTempSampleQuery() {
		String sql = "";
		
		sql += "DELETE FROM T_SAMPLE ";
		
		return sql;
	}
	
	public static String getFindWordForDetailQuery() {
		String sql = "";
		
		sql += "SELECT * FROM DIC WHERE FLAG = 'N'";
		
		return sql;
	}

	public static String getInsDicMeanQuery() {
		String sql = "";
		sql += "INSERT INTO DIC_MEAN(ENTRY_ID, NUM, MEAN) VALUES (?, ?, ?) ";

		return sql;
	}

	public static String getInsDicMeanSampleQuery() {
		String sql = "";
		sql += "INSERT INTO DIC_MEAN_SAMPLE(ENTRY_ID, NUM, LANG_HAN, LANG_VIET, SAMPLE_SEQ) VALUES (?, ?, ?, ?, -1) ";

		return sql;
	}

	public static String getInsDicWordSampleQuery() {
		String sql = "";
		sql += "INSERT INTO DIC_WORD_SAMPLE(ENTRY_ID, LANG_HAN, LANG_VIET, SAMPLE_SEQ) VALUES (?, ?, ?, -1) ";

		return sql;
	}

	public static String getUpdFindedDicDetailQuery() {
		String sql = "";
		sql += "UPDATE DIC SET FLAG = 'Y' WHERE ENTRY_ID = ? ";

		return sql;
	}

	public static String getUpdFlagYDicSample() {
		String sql = "";
		
		sql += "UPDATE DIC_SAMPLE SET FLAG = 'Y' WHERE FLAG = 'N'";
		
		return sql;
	}

	public static String getDelFindDic() {
		String sql = "";
		
		sql += "DELETE FROM T_FIND_DIC WHERE WORD IN (SELECT WORD FROM T_FINDED_DIC)";
		
		return sql;
	}
	
	public static String getInsFindDic() {
		String sql = "";
		
		sql += "INSERT INTO T_FIND_DIC(KIND, WORD) VALUES (?, ?)";
		
		return sql;
	}	
	
	public static String getDicSampleFlagY() {
		String sql = "";
		
		sql += "SELECT * FROM DIC_SAMPLE WHERE FLAG = 'N' AND LANG_HAN IS NOT NULL AND LANG_VIET IS NOT NULL";
		
		return sql;
	}

	public static String getUpdFindeSeqDicMeanSample() {
		String sql = "";
		sql += "UPDATE DIC_MEAN_SAMPLE A SET SAMPLE_SEQ (SELECT MAX(SEQ) FROM DIC_SAMPLE WHERE LANG_VIET = A.LANG_VIET) WHERE SAMPLE_SEQ > -1 ";

		return sql;
	}

	public static String getUpdFindeSeqDicWordSample() {
		String sql = "";
		sql += "UPDATE DIC_WORD_SAMPLE A SET SAMPLE_SEQ (SELECT MAX(SEQ) FROM DIC_SAMPLE WHERE LANG_VIET = A.LANG_VIET) WHERE SAMPLE_SEQ > -1 ";

		return sql;
	}
	
	public static String getFindWordNoSpellingQuery() {
		String sql = "";
		
		sql += "SELECT * FROM DIC WHERE SPELLING LIKE '%*%' AND KIND = 'VH' ";
		
		return sql;
	}

	public static String getSpellingForWordQuery(Connection conn, String word) throws Exception  {
		String rtn = "*";
	
		String sql = "";
		sql += "SELECT SPELLING FROM DIC WHERE LOWER(WORD) COLLATE utf8_bin = LOWER(?) ORDER BY SPELLING DESC";
		//CommUtil.logQueryPrint(sql);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, word);
		ResultSet rs = ps.executeQuery();
		if ( rs.next() ) {
			rtn = rs.getString("SPELLING").replace("[", "").replace("]", "").replace(" ", "");
			if ( rtn == null || "".equals(rtn) || "null".equals(rtn) ) {
				rtn = "*";
			}
		}
		rs.close();
		ps.close();
		
		//System.out.println(rtn);
		return rtn;
	}
	
	public static HashMap getWorSpelling(Connection conn) throws Exception  {
		HashMap hm = new HashMap();
		
		String sql = "";
		sql += "SELECT WORD, SPELLING FROM DIC WHERE KIND = 'VH'";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			String[] aWord = rs.getString("WORD").split(" ");
			String[] aSpelling = rs.getString("SPELLING").replace("[", "").replace("]", "").split(" ");
			if ( aWord.length == aSpelling.length ) {
				for ( int i = 0; i < aWord.length; i++ ) {
					if ( !"*".equals(aSpelling[i]) && !hm.containsKey(aWord[i]) ) {
						hm.put(aWord[i], aSpelling[i]);
					}
				}
			}
		}
		rs.close();
		ps.close();

		System.out.println("갯수 : " + hm.size());
		
		return hm;
	}
	
	public static String getUpdSpellingDicQuery() {
		String sql = "";
		sql += "UPDATE DIC SET SPELLING = ? WHERE ENTRY_ID = ? ";

		return sql;
	}
	
	public static String updMeanSampleSeq() {
		String sql = "";
		
		sql += "UPDATE DIC_MEAN_SAMPLE A";
		sql += "   SET SAMPLE_SEQ = (SELECT SEQ FROM DIC_SAMPLE WHERE LANG_VIET = A.LANG_VIET AND LANG_HAN = A.LANG_HAN)";
		
		return sql;
	}

	public static String updWordSampleSeq() {
		String sql = "";
		
		sql += "UPDATE DIC_WORD_SAMPLE A";
		sql += "   SET SAMPLE_SEQ = (SELECT SEQ FROM DIC_SAMPLE WHERE LANG_VIET = A.LANG_VIET AND LANG_HAN = A.LANG_HAN)";
		
		return sql;
	}
	
	public static String getVhDicOrder() {
		String sql = "";
		
		sql += "SELECT * FROM DIC WHERE KIND = 'VH' ORDER BY LEFT(WORD,1), WORD COLLATE UTF8_BIN";
		
		return sql;
	}

	public static String getHvDicOrder() {
		String sql = "";
		
		sql += "SELECT 1 O, SEQ, ENTRY_ID, KIND, WORD, MEAN, SPELLING, HANMUN, ORD FROM DIC WHERE KIND = 'HV' AND LEFT(WORD,1) NOT IN ('(','-','…','–','ㄱ','ㄹ','M','T')" + 
				"UNION " +
				"SELECT 2 O, SEQ, ENTRY_ID, KIND, WORD, MEAN, SPELLING, HANMUN, ORD FROM DIC WHERE KIND = 'HV' AND LEFT(WORD,1) IN ('(','-','…','–','ㄱ','ㄹ','M','T') ORDER BY O, WORD";

		
		return sql;
	}
	
	public static String updDicOrderQuery() {
		String sql = "";
		sql += "UPDATE DIC SET ORD = ? WHERE ENTRY_ID = ? ";

		return sql;
	}
	
	public static String updVhSpelling() {
		String sql = "";
		
		sql += "UPDATE DIC ";
		sql += "   SET SPELLING = CONCAT('[',replace(replace(SPELLING,'[',''),']',''),']') ";
		sql += " WHERE KIND = 'VH' ";
		sql += "   AND SPELLING IS NOT NULL ";
		
		return sql;
	}
	
	public static String updVhSpellingBlank() {
		String sql = "";
		
		sql += "UPDATE DIC ";
		sql += "   SET SPELLING = ' ' ";
		sql += " WHERE KIND = 'VH' ";
		sql += "   AND SPELLING IS NULL ";
		
		return sql;
	}
	
	public static String updHvSpelling() {
		String sql = "";
		
		sql += "UPDATE DIC ";
		sql += "   SET SPELLING = ' ' ";
		sql += " WHERE KIND = 'HV' ";
		
		return sql;
	}

	
	public static String updVhMean() {
		String sql = "";
		
		sql += "UPDATE DIC ";
		sql += "   SET MEAN = replace(replace(MEAN,'[label]','['),'[/label]',']') ";
		sql += " WHERE KIND = 'VH' ";
		
		return sql;
	}
}
