package dictionary.vietnam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class MakeSpelling {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("MakeSpelling Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            HashMap spellingHm = new HashMap();
    		PreparedStatement ps = conn.prepareStatement("select * from dic where kind = 'VH'");
    		ResultSet rs = ps.executeQuery();
    		while ( rs.next() ) {
    			spellingHm.put(rs.getString("WORD").toLowerCase(), rs.getString("SPELLING"));
			}
    		rs.close();
    		ps.close();
    		
    		PreparedStatement iudPs = conn.prepareStatement("update dic_sample set spelling = ? where seq = ?");
    		
    		int cnt = 0;
    		
    		ps = conn.prepareStatement("select * from dic_sample");
    		rs = ps.executeQuery();
    		while ( rs.next() ) {
    			String langViet = rs.getString("LANG_VIET").toLowerCase();
    			String[] splitStr = sentenceSplit(langViet);
    			
    			String spelling = "";
    			for ( int m = 0; m < splitStr.length; m++ ) {
    				if ( spellingHm.containsKey(splitStr[m]) && "[*]".equals(spellingHm.get(splitStr[m])) ) {
	                	spelling += splitStr[m];
	                } else if ( spellingHm.containsKey(splitStr[m]) ) {
	                	spelling += spellingHm.get(splitStr[m].toLowerCase());
	                } else if ( m < splitStr.length - 2 && 
	                		spellingHm.containsKey(splitStr[m] + " " + splitStr[m+1]) ) {
	                	spelling += spellingHm.get(splitStr[m] + " " + splitStr[m+1]);
	                	m = m + 2;
	                	continue;
	                } else if ( m < splitStr.length - 3 && 
	                		spellingHm.containsKey(splitStr[m] + " " + splitStr[m+1] + " " + splitStr[m+2]) ) {
	                	spelling += spellingHm.get(splitStr[m] + " " + splitStr[m+1] + " " + splitStr[m+2]);
	                	m = m + 3;
	                	continue;
	                } else {
	                	spelling += splitStr[m];
	                }
	            }
    			
    			iudPs.setString(1, spelling.replaceAll("[\\[\\]]", ""));
				iudPs.setString(2, rs.getString("SEQ"));
				iudPs.executeUpdate();
				
				if ( cnt++ / 1000 == 0 ) {
					conn.commit();
				}
			}
    		rs.close();
    		ps.close();

            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeSpelling End");
	}

	public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        String tmpSentence = sentence + " ";

        int startPos = 0;
        for ( int i = 0; i < tmpSentence.length(); i++ ) {
            if ( "()[]<>\"',.?/= ".indexOf(tmpSentence.substring(i, i + 1)) > -1 ) {
                if ( i == 0 ) {
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                } else {
                    if ( i != startPos ) {
                        al.add(tmpSentence.substring(startPos, i));
                    }
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }
}
