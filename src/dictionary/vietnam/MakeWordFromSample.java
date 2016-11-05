package dictionary.vietnam;

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
		System.out.println("VietHan Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            HashMap wordHm = new HashMap();
            PreparedStatement iudPs = conn.prepareStatement(VietHanQuery.getInsFindDic());
            
            //수집할 단어가 있는 샘플 문장을 가져와 단어를 입력한다.
    		PreparedStatement ps = conn.prepareStatement(VietHanQuery.getDicSampleFlagY());
    		ResultSet rs = ps.executeQuery();
    		while ( rs.next() ) {
    			String langHan = rs.getString("LANG_HAN");
    			String langViet = rs.getString("LANG_VIET");
    			   			
    			String[] hanArr = langHan.split(CommConstant.sentenceSplitStr);
    			for ( int i = 0; i < hanArr.length; i++ ) {
    				if ( !"".equals(hanArr[i]) && !wordHm.containsKey(hanArr[i]) ) {
    					iudPs.setString(1, "HV");
    					iudPs.setString(2, hanArr[i]);
    					iudPs.executeUpdate();
	    				
	    				wordHm.put(hanArr[i], hanArr[i]);
    				}
    			}
    			String[] vietArr = langViet.split(CommConstant.sentenceSplitStr);
    			for ( int i = 0; i < vietArr.length; i++ ) {
    				if ( !"".equals(vietArr[i]) && !wordHm.containsKey(vietArr[i]) ) {
    					iudPs.setString(1, "VH");
    					iudPs.setString(2, vietArr[i]);
    					iudPs.executeUpdate();
	    				
	    				wordHm.put(vietArr[i], vietArr[i]);
    				}
    			}
    		}
    		rs.close();
            
    		//샘플 문장에 완료 표시를 한다.
    		iudPs = conn.prepareStatement(VietHanQuery.getUpdFlagYDicSample());
    		iudPs.executeUpdate();

    		//이미 찾은 단어는 삭제한다.
    		iudPs = conn.prepareStatement(VietHanQuery.getDelFindDic());
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
		
		System.out.println("VietHan End");
	}

}
