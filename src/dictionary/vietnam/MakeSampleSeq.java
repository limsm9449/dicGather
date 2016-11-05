package dictionary.vietnam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import dictionary.CommConstant;

public class MakeSampleSeq {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("VietHan Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            Statement s = conn.createStatement();

            //단어의 뜻 샘플 Seq 수정
            System.out.println("Query : " + VietHanQuery.getUpdFindeSeqDicMeanSample());
            s.executeUpdate(VietHanQuery.getUpdFindeSeqDicMeanSample());

            //단어의 샘플  Seq 수정
            System.out.println("Query : " + VietHanQuery.getUpdFindeSeqDicWordSample());
    		s.executeUpdate(VietHanQuery.getUpdFindeSeqDicWordSample());

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
