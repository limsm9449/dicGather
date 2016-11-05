package dictionary.english;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import dictionary.CommConstant;

public class MakeSample {
	private static Connection conn;
	
	public static void main(String[] args) {
		System.out.println("MakeSample Start");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");                     
			conn=DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);    
			
            conn.setAutoCommit(false);
            
            Statement s = conn.createStatement();

            //임시 샘풀중 등록이 안된 샘플은 등록함
            System.out.println("Query : " + Query.getInsSampleFromTempSampleQuery());
            s.executeUpdate(Query.getInsSampleFromTempSampleQuery());

            //임시 샘풀은 삭제함
            System.out.println("Query : " + Query.getDelTempSampleQuery());
    		s.executeUpdate(Query.getDelTempSampleQuery());

    		//뜻 예제도 샘플에 등록함
            System.out.println("Query : " + Query.getInsSampleFromMeanSampleQuery());
            s.executeUpdate(Query.getInsSampleFromMeanSampleQuery());

            conn.commit();
				
		} catch ( Exception e ) {
			try {
				conn.rollback();
			} catch ( Exception e1 ) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		System.out.println("MakeSample End");
	}

}
