package dictionary.english;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import dictionary.CommConstant;

public class MakeWordFromSample {
	private static Connection conn;
	
/*
 * update dic_sample set flag = 'N'
 * delete from T_FIND_DIC;
 * select * from t_find_dic
 */
	public static void main(String[] args) {
		System.out.println("MakeWordFromSample Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            HashMap wordHm = new HashMap();
            PreparedStatement iudPs = conn.prepareStatement(Query.getInsFindDic());
            
            //수집할 단어가 있는 샘플 문장을 가져와 단어를 입력한다.
    		PreparedStatement ps = conn.prepareStatement(Query.getDicSampleFlagY());
    		ResultSet rs = ps.executeQuery();
    		while ( rs.next() ) {
    			String langHan = rs.getString("LANG_HAN");
    			String langForeign = rs.getString("LANG_FOREIGN");

    			/*
    			String[] hanArr = langHan.split(CommConstant.sentenceSplitStr);
    			for ( int i = 0; i < hanArr.length; i++ ) {
    				if ( !"".equals(hanArr[i]) && !wordHm.containsKey(hanArr[i]) ) {
    					iudPs.setString(1, Constant.kind_h);
    					iudPs.setString(2, hanArr[i]);
    					iudPs.executeUpdate();
	    				
	    				wordHm.put(hanArr[i], hanArr[i]);
    				}
    			}
    			*/
    			String[] vietArr = langForeign.split(CommConstant.sentenceSplitStr);
    			for ( int i = 0; i < vietArr.length; i++ ) {
    				String word = vietArr[i].replaceAll(CommConstant.removeStr, "");
    				
    				if ( word.length() > 1 && !"".equals(vietArr[i]) && !wordHm.containsKey(vietArr[i]) ) {
    					iudPs.setString(1, Constant.kind_f);
    					iudPs.setString(2, word);
    					iudPs.executeUpdate();
	    				
	    				wordHm.put(vietArr[i], vietArr[i]);
    				}
    			}
    		}
    		rs.close();
            
    		//샘플 문장에 완료 표시를 한다.
    		iudPs = conn.prepareStatement(Query.getUpdFlagYDicSample());
    		iudPs.executeUpdate();

    		//이미 찾은 단어는 삭제한다.
    		iudPs = conn.prepareStatement(Query.getDelFindDic());
    		iudPs.executeUpdate();

            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeWordFromSample End");
	}

}
