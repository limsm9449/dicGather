package dictionary.english;

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

public class MakeOther {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("MakeOther Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            //updDicOrd();
            
            //updSampleSeq();
            
            //gatherWordSpelling();
            
            //updData();
            
            updDicSampleOrd();
            
            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeOther End");
	}
	
	public static void updData() throws Exception {
		Statement s = conn.createStatement();
		
		System.out.println(Query.updVhSpelling());
		s.executeUpdate(Query.updVhSpelling());
		
		System.out.println(Query.updForiegnSpellingBlank());
		s.executeUpdate(Query.updForiegnSpellingBlank());
		
		System.out.println(Query.updHanSpelling());
		s.executeUpdate(Query.updHanSpelling());
	
		System.out.println(Query.updForiegnMean());
		s.executeUpdate(Query.updForiegnMean());
		
		conn.commit();
		s.close();
	}

	public static void updSampleSeq() throws Exception {
		Statement s = conn.createStatement();
		
		System.out.println(Query.updMeanSampleSeq());
		s.executeUpdate(Query.updMeanSampleSeq());

		conn.commit();
		s.close();
	}
	
	public static void gatherWordSpelling() throws Exception {
		int cnt = 0;
		
		HashMap hm = Query.getWorSpelling(conn);
		
		PreparedStatement psUpdSpellingDic = conn.prepareStatement(Query.getUpdSpellingDicQuery());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getFindWordNoSpellingQuery());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findWord = rs.getString("WORD");
			String findEntryId = rs.getString("ENTRY_ID");
			
			String[] findWordArr = findWord.split(" ");
			String wordSpelling = "";
			for ( int i = 0; i < findWordArr.length; i++ ) {
				String spelling = Query.getSpellingForWordQuery(conn, findWordArr[i]);
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
	
	public static void updDicOrd() throws Exception {
		int cnt = 0;
		int ord = 1;
		
		PreparedStatement psUpdDicOrd = conn.prepareStatement(Query.updDicOrderQuery());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getForiegnDicOrder());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String findEntryId = rs.getString("ENTRY_ID");
			
			psUpdDicOrd.setInt(1, ord++);
			psUpdDicOrd.setString(2, findEntryId);
			psUpdDicOrd.executeUpdate();
    		if ( cnt++ % Constant.findDicOrdCount == 0 ) {
    			System.out.println("Foriegn : " + (cnt++ / Constant.findDicOrdCount));
    		}
		}

		cnt = 0;
		s.executeQuery(Query.getHanDicOrder());
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
	

	public static void insDicTense() throws Exception {
		int cnt = 0;
		int ord = 1;
		
		PreparedStatement psIns = conn.prepareStatement(Query.insTense());

		Statement s = conn.createStatement();
		
		s.execute(Query.delTense());
		
		s.executeQuery(Query.getTense());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String tense = rs.getString("TENSE");
			System.out.println(rs.getString("word") + " : " + tense);
			String[] t1 = tense.replace(", |","|").split(",");
			
			for ( int i = 0; i < t1.length; i++ ) {
				System.out.println(" = " + t1[i]);
				String[] t2 = t1[i].split("[/^]");
				String[] t3 = t2[1].split("[/|]");
				for ( int m = 0; m < t3.length; m++ ) {
					System.out.println(" = " + t3[m].trim());
					psIns.setString(1, t3[m].trim());
					psIns.setString(2, rs.getString("WORD"));
					psIns.executeUpdate();
				}
			} 
    		if ( cnt++ % Constant.findDicOrdCount == 0 ) {
    			System.out.println("Foriegn : " + (cnt++ / Constant.findDicOrdCount));
    		}
		}
		//명사형^coverer,형용사형^coverable | covert | coverless,과거^covered,과거분사^covered,현재분사^covering,3인칭 단수 현재^covers
		
		conn.commit();
		
		rs.close();
		psIns.close();
		s.close();
	}
	
	public static void updDicSampleOrd() throws Exception {
		int cnt = 0;
		int ord = 1;
		
		PreparedStatement psUpdDicOrd = conn.prepareStatement(Query.updDicSampleOrderQuery());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getDicSampleOrderQuery());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String seq = rs.getString("SEQ");
			
			psUpdDicOrd.setInt(1, cnt + 1);
			psUpdDicOrd.setString(2, seq);
			psUpdDicOrd.executeUpdate();
    		if ( cnt++ % Constant.findDicOrdCount == 0 ) {
    			System.out.println("Foriegn : " + (cnt++ / Constant.findDicOrdCount));
    		}
		}
		
		conn.commit();
		
		rs.close();
		psUpdDicOrd.close();
		s.close();
	}
	
}
