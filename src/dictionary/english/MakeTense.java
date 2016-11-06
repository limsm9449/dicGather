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

public class MakeTense {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("MakeTense Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            insDicTense();
            
            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeTense End");
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
	
}
