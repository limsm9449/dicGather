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
		//System.out.println("Jusic Start");

		try {
			jusic100_1();
			jusic100_2();
			//upjong();
			//theme();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("Jusic End");
	}

	public static void jusic100_1() throws Exception {
		String url = "https://finance.naver.com/sise/sise_market_sum.nhn";

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

	public static void jusic100_2() throws Exception {
		String url = "https://finance.naver.com/sise/sise_market_sum.nhn?&page=2";

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

	public static void upjong() throws Exception {
		String url = "https://finance.naver.com/sise/sise_group.nhn?type=upjong";

		Document doc = Jsoup.connect(url).timeout(10000).get();
		//System.out.println(doc);
		Elements eAs = doc.select("table tr td a");
		for (Element e_eAs : eAs) {
			if ( getUrlParameter(e_eAs.attr("href"), "no") != "" ) {
				//System.out.println("<option value='" + getUrlParameter(e_eAs.attr("href"), "no") + "'>" + e_eAs.text() + "</option>");
				upjongDetail(getUrlParameter(e_eAs.attr("href"), "no"));
			}
		}
	}

	public static void upjongDetail(String no) throws Exception {
		String url = "https://finance.naver.com/sise/sise_group_detail.nhn?type=upjong&no=" + no;

		System.out.println("idx = 0;");
		System.out.println("jusicArr['upjong_" + no + "'] = []");
		
		Document doc = Jsoup.connect(url).timeout(10000).get();
		//System.out.println(doc);
		Elements eAs = doc.select(".type_5 tbody tr td .name_area a");
		for (Element e_eAs : eAs) {
			System.out.println("jusicArr['upjong_" + no + "'][idx++] = lfn_createJusicInfo('" + e_eAs.text() + "', '" + getUrlParameter(e_eAs.attr("href"), "code") + "', 0, 0);");
		}
	}

	public static void theme() throws Exception {
		String url = "https://finance.naver.com/sise/sise_group.nhn?type=theme";

		Document doc = Jsoup.connect(url).timeout(10000).get();
		//System.out.println(doc);
		Elements eAs = doc.select("table tr td a");
		for (Element e_eAs : eAs) {
			if ( getUrlParameter(e_eAs.attr("href"), "no") != "" ) {
				//System.out.println("<option value='" + getUrlParameter(e_eAs.attr("href"), "no") + "'>" + e_eAs.text() + "</option>");
				themeDetail(getUrlParameter(e_eAs.attr("href"), "no"));
			}
		}
	}

	public static void themeDetail(String no) throws Exception {
		String url = "https://finance.naver.com/sise/sise_group_detail.nhn?type=theme&no=" + no;

		System.out.println("idx = 0;");
		System.out.println("jusicArr['theme_" + no + "'] = []");
		
		Document doc = Jsoup.connect(url).timeout(10000).get();
		//System.out.println(doc);
		Elements eAs = doc.select(".type_5 tbody tr td .name_area a");
		for (Element e_eAs : eAs) {
			System.out.println("jusicArr['theme_" + no + "'][idx++] = lfn_createJusicInfo('" + e_eAs.text() + "', '" + getUrlParameter(e_eAs.attr("href"), "code") + "', 0, 0);");
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