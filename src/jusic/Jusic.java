package jusic;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dictionary.english.Constant;

public class Jusic {
	private static Connection conn;

	public static void main(String[] args) {
		System.out.println("Jusic Start");

		try {
			jusic100();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Jusic End");
	}

	public static void jusic100() throws Exception {
		String url = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0";

		// 동아 entryId를 찾는다.
		Document doc = Jsoup.connect(url).timeout(10000).get();
		//System.out.println(doc);
		Elements eTables = doc.select("table");
		for (Element e_eTables : eTables) {
			if ("type_2".equals(e_eTables.attr("class"))) {
				Elements tTds = e_eTables.select("tbody tr td .tltle");
				for (Element e_tTds : tTds) {
					System.out.println("tJusics[idx++] = lfn_createJusicInfo('" + e_tTds.text() + "', '" + getUrlParameter(e_tTds.attr("href"), "code") + "', 0, 0);");
				}
			}
		}
	}


	public static String getUrlParameter(String url, String parameter) throws Exception {
		String rtn = "";

		if (url.indexOf("?") < 0) {
			return "";
		}
		String[] split_url = url.split("[?]");
		String[] split_param = split_url[1].split("[&]");
		for (int i = 0; i < split_param.length; i++) {
			String[] split_row = split_param[i].split("[=]");
			if ( parameter.equals(split_row[0]) ) {
				rtn = split_row[1];
			}
		}

		return rtn;
	}
	

}