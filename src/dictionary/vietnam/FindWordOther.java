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

public class FindWordOther {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("VietHan Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            //findSampleSeq();
            
            gatherWordSpelling();
            
            //makeDicOrd();
            
            //updData();
            
            conn.commit();
				
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
	
	public static void updData() throws Exception {
		Statement s = conn.createStatement();
		
		System.out.println(VietHanQuery.updVhSpelling());
		s.executeUpdate(VietHanQuery.updVhSpelling());
		
		System.out.println(VietHanQuery.updVhSpellingBlank());
		s.executeUpdate(VietHanQuery.updVhSpellingBlank());
		
		System.out.println(VietHanQuery.updHvSpelling());
		s.executeUpdate(VietHanQuery.updHvSpelling());
	
		System.out.println(VietHanQuery.updVhMean());
		s.executeUpdate(VietHanQuery.updVhMean());
		
		conn.commit();
		s.close();
	}

	public static void findSampleSeq() throws Exception {
		Statement s = conn.createStatement();
		
		System.out.println(VietHanQuery.updMeanSampleSeq());
		s.executeUpdate(VietHanQuery.updMeanSampleSeq());
		
		System.out.println(VietHanQuery.updWordSampleSeq());
		s.executeUpdate(VietHanQuery.updWordSampleSeq());

		conn.commit();
		s.close();
	}
	
	public static void gatherWordSpelling() throws Exception {
		int cnt = 0;
		
		HashMap hm = VietHanQuery.getWorSpelling(conn);
		
		PreparedStatement psUpdSpellingDic = conn.prepareStatement(VietHanQuery.getUpdSpellingDicQuery());

		Statement s = conn.createStatement();
		s.executeQuery(VietHanQuery.getFindWordNoSpellingQuery());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findWord = rs.getString("WORD");
			String findEntryId = rs.getString("ENTRY_ID");
			
			String[] findWordArr = findWord.split(" ");
			String wordSpelling = "";
			for ( int i = 0; i < findWordArr.length; i++ ) {
				String spelling = VietHanQuery.getSpellingForWordQuery(conn, findWordArr[i]);
				//찾았는데 *가 나오면 다른 방식으로 찾음
				if ( "*".equals(spelling) ) {
					if ( hm.containsKey(findWordArr[i]) ) {
						spelling = (String)hm.get(findWordArr[i]);
					} else {
						spelling = "*";
					}
				}
				wordSpelling += (wordSpelling.length() == 0 ? "" : " ") + spelling;
			}
			psUpdSpellingDic.setString(1, "[" + wordSpelling + "]");
			psUpdSpellingDic.setString(2, findEntryId);
			psUpdSpellingDic.executeUpdate();
			
			System.out.println("findWord : " + findWord + ", wordSpelling : " + wordSpelling + ", findEntryId : " + findEntryId);
			
    		if ( cnt++ % Constant.findSpellingNOWordCount == 0 ) {
    			System.out.println("commit");
    			conn.commit();
    		}
		}

		conn.commit();
		//conn.rollback();

		rs.close();
		psUpdSpellingDic.close();
		s.close();
	}
	
	public static void makeDicOrd() throws Exception {
		int cnt = 0;
		int ord = 1;
		
		PreparedStatement psUpdDicOrd = conn.prepareStatement(VietHanQuery.updDicOrderQuery());

		Statement s = conn.createStatement();
		s.executeQuery(VietHanQuery.getVhDicOrder());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findEntryId = rs.getString("ENTRY_ID");
			
			psUpdDicOrd.setInt(1, ord++);
			psUpdDicOrd.setString(2, findEntryId);
			psUpdDicOrd.executeUpdate();
    		if ( cnt++ % Constant.findDicOrdCount == 0 ) {
    			System.out.println("vh : " + (cnt++ / Constant.findDicOrdCount));
    		}
		}

		cnt = 0;
		s.executeQuery(VietHanQuery.getHvDicOrder());
		rs = s.getResultSet();
		while ( rs.next() ) {
			String findEntryId = rs.getString("ENTRY_ID");
			
			psUpdDicOrd.setInt(1, ord++);
			psUpdDicOrd.setString(2, findEntryId);
			psUpdDicOrd.executeUpdate();
				
			if ( cnt++ % Constant.findDicOrdCount == 0 ) {
    			System.out.println("hv : " + (cnt++ / Constant.findDicOrdCount));
    		}
		}
		
		conn.commit();
		
		rs.close();
		psUpdDicOrd.close();
		s.close();
	}
	
}
