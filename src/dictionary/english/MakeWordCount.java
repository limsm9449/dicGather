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

public class MakeWordCount {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("MakeWordCount Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            updDicOrd();
            
            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeWordCount End");
	}

	public static void updDicOrd() throws Exception {
		int cnt = 0;
		int ord = 1;
		
		PreparedStatement psUpd = conn.prepareStatement(Query.updDicSampleWordCountrQuery());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getDicSample());
		ResultSet rs = s.getResultSet();
		while ( rs.next() ) {
			String seq = rs.getString("SEQ");
			String foreign = rs.getString("LANG_FOREIGN");
			
			psUpd.setInt(1, getBlankCount(foreign));
			psUpd.setString(2, seq);
			psUpd.executeUpdate();
    		if ( cnt++ % 1000 == 0 ) {
    			System.out.println("count : " + (cnt++ / 1000));
    		}
		}

		conn.commit();
		
		rs.close();
		psUpd.close();
		s.close();
	}
	
	public static int getBlankCount(String foreign) throws Exception {
		int rtn = 0;
		
		for ( int i = 0; i < foreign.length() - 1; i++ ) {
			if ( " ".equals(foreign.substring(i, i + 1)) ) {
				rtn++;
			}
		}
		return rtn;
	}
	
}
