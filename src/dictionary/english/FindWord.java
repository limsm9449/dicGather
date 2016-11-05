package dictionary.english;

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

public class FindWord {
	private static Connection conn;

	public static void main(String[] args) {
		System.out.println("FindWord Start");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(Constant.url, Constant.id, Constant.pw);

			conn.setAutoCommit(false);

	        gatherWord();
	        
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		System.out.println("FindWord End");
	}

	// http://vndic.naver.com/search.nhn?query=gia%20%C4%91%C3%ACnh
	public static boolean gatherWord() throws Exception {
		int wordCnt = 0;
		int sampleCnt = 0;

		PreparedStatement psDic = conn.prepareStatement(Query.getInsDicQuery());
		PreparedStatement psDicMean = conn.prepareStatement(Query.getInsDicMeanQuery());
		PreparedStatement psDicMeanSample = conn.prepareStatement(Query.getInsDicMeanSampleQuery());
		PreparedStatement psTempSample = conn.prepareStatement(Query.getInsTempSampleQuery());

		Statement s = conn.createStatement();
		s.executeQuery(Query.getFindWordQuery());
		ResultSet rs = s.getResultSet();
		while (rs.next()) {
			String findKind = rs.getString("KIND");
			String findWord = rs.getString("WORD");
			//findWord = "absolutely";
			System.out.println(Calendar.getInstance().getTime() + " : " + findWord);

			try {
				if ( !MUtil.isOkWord(findWord) ) {
					//System.out.println("Skip : " + findWord);
					continue;
				}
				
				if (!Query.isExistSameWord(conn, findWord)) {
					// 단어 갯수를 가져온다.
					String url = "http://endic.naver.com/search.nhn?sLn=kr&searchOption=all&query="
							+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
					// System.out.println(url);
					wordCnt = 0;
					sampleCnt = 0;
					
					Document doc = Jsoup.connect(url).timeout(10000).get();
					Elements h3_es = doc.select("h3");
					for (Element h3_es_1 : h3_es) {
						// System.out.println(e.attr("class"));
						if (h3_es_1.children().size() > 0 && h3_es_1.child(0).tagName().equals("img")
								&& h3_es_1.child(0).attr("alt").equals("단어/숙어")) {
							wordCnt = Integer.parseInt(h3_es_1.child(1).text().replaceAll("[(),건]", ""));
						} else if (h3_es_1.children().size() > 0 && h3_es_1.child(0).tagName().equals("img")
								&& h3_es_1.child(0).attr("alt").equals("예문")) {
							sampleCnt = Integer.parseInt(h3_es_1.child(1).text().replaceAll("[(),건]", ""));
							break;
						}
					}
					System.out.println("findWord : " + findWord + ", url : " + url + ", wordCnt : " + wordCnt
							+ ", sampleCnt : " + sampleCnt);
	
					// 단어 리스트를 가져온다.
					ArrayList words = null;
					if (wordCnt > 0) {
						words = naverDicWordList("http://endic.naver.com/search.nhn?sLn=kr&searchOption=entry_idiom&query="
								+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20"), wordCnt, findWord);
					}
	
					// 샘플 리스트를 가져온다.
					ArrayList samples = null;
					/*
					if (sampleCnt > 0) {
						samples = naverDicSampleList("http://endic.naver.com/search_example.nhn?sLn=kr&query="
								+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20"), sampleCnt);
					}
					*/
	
					if ( words != null ) {
						for (int i = 0; i < words.size(); i++) {
							//System.out.println(words.get(i));
							HashMap row = (HashMap)words.get(i);
			    			
			    			String word = (String)row.get("WORD");
			    			
			    			if ( !MUtil.isOkWord(word) ) {
								//System.out.println("Skip : " + words.get(i));
			    				continue;
			    			}
			    			
			    			String entryId = (((String)row.get("ENTRYID")).split("="))[1].trim();
			    			String spelling = (String)row.get("SPELLING");
			    			if ("".equals(spelling) || spelling == null ) {
			    				spelling = "*";
			    			}
			    			String mean = (String)row.get("MEAN");
			    			String hanmun = (String)row.get("HANMUN");
			    			if ("".equals(hanmun) || hanmun == null ) {
			    				hanmun = "";
			    			}
			    			String type = (String)row.get("TYPE");
			    			String tense = (String)row.get("TENSE");
			    			boolean isDetail = (boolean)row.get("ISDETAIL");
			    			boolean isHan = (boolean)row.get("ISHAN");
			    			
			    			if ( !Query.isExistDicForEntryId(conn, entryId) ) {
			    				int idx = 1;
			    				psDic.setString(idx++, entryId);
			    				psDic.setString(idx++, MUtil.getLangKind(isHan));
			    				psDic.setString(idx++, new String(word.getBytes(), "UTF-8"));
			    				psDic.setString(idx++, mean);
			    				psDic.setString(idx++, spelling);
			    				psDic.setString(idx++, hanmun);
			    				psDic.setString(idx++, MUtil.getString(type));
			    				psDic.setString(idx++, MUtil.getString(tense));
			    				psDic.executeUpdate();
			    				
			    				//System.out.println(row);
			    				if (isDetail ) {
			    					int meanNum = 1;
			    					ArrayList al = (ArrayList)row.get("DETAILS");
			    					for ( int al_i = 0; al_i < al.size(); al_i++ ) {
			    						String classType = (String)((HashMap)al.get(al_i)).get("CLASS");	    								
			    						ArrayList meansAl = (ArrayList)((HashMap)al.get(al_i)).get("MEANS");
			    						for ( int meansAl_i = 0; meansAl_i < meansAl.size(); meansAl_i++ ) {
			    							String classMean = (String)((HashMap)meansAl.get(meansAl_i)).get("MEAN");
			    							
			    							idx = 1;
			    							psDicMean.setString(idx++, entryId);
			    							psDicMean.setInt(idx++, meanNum);
			    							psDicMean.setString(idx++, MUtil.getString(classType));
			    							psDicMean.setString(idx++, MUtil.getString(classMean));
			    							psDicMean.executeUpdate();
			    							
			    							ArrayList sampleAl = (ArrayList)((HashMap)meansAl.get(meansAl_i)).get("SAMPLE");
			    							if ( sampleAl != null ) {
				    							for ( int sampleAl_i = 0; sampleAl_i < sampleAl.size(); sampleAl_i++ ) {
				    								if ( sampleAl.get(sampleAl_i) != null ) {
						    							String[] classMeanSample = ((String)sampleAl.get(sampleAl_i)).split(":");
						    							//System.out.println(classMeanSample[0] +  " : " + classMeanSample[1]);
						    							
						    							idx = 1;
						    							psDicMeanSample.setString(idx++, entryId);
						    							psDicMeanSample.setInt(idx++, meanNum);
						    							psDicMeanSample.setString(idx++, MUtil.getString(classMeanSample[0]));
						    							psDicMeanSample.setString(idx++, MUtil.getString(classMeanSample[1]));
						    							psDicMeanSample.executeUpdate();
				    								}
				    							}
			    							}
			    							
			    							meanNum++;
			    						}
			    					}
			    				}
			    			}
						}
					}
						
					if ( samples != null ) {
						for (int i = 0; i < samples.size(); i++) {
							//System.out.println(samples.get(i));
							
							String[] split = ((String)samples.get(i)).split(":"); 
			    			
			    			psTempSample.setString(1, split[0].trim());
			    			psTempSample.setString(2, split[1].trim());
			    			try {
			    				psTempSample.executeUpdate();
			    			} catch ( Exception e) {
			    				System.out.println("오류 --> Foreign : " + split[0] + ", Han : " + split[1]);
			    			}
						}
					}
	
					s.executeUpdate(Query.getInsFindedWordQuery(findKind, findWord));
		    		s.executeUpdate(Query.getDelFindWordQuery(findKind, findWord));
				} else {
					System.out.println(findWord + " 있음");
					s.executeUpdate(Query.getDelFindWordQuery(findKind, findWord));
				}
				
				conn.commit();
			} catch ( Exception e ) {
				//e.printStackTrace();
				System.out.println("Error : " + e.getMessage());
			}
			//break;
		}
		rs.close();
		s.close();
		
		return true;
	}

	// 단어 검색
	public static ArrayList naverDicWordList(String url, int cnt, String findWord) throws Exception {
		ArrayList words = new ArrayList<HashMap>();
		int inCnt = 0;
		String word = "";
		String entryId = "";
		String hanmun = "";
		String mean = "";
		String spelling = "";
		String type = ""; // 품사..
		boolean isWord = true;
		boolean isDetail = false;
		boolean isBreak = false;
		boolean isHan = false;
		// System.out.println(url + " ========================> ");
		for (int i = 1; i <= (cnt / 20) + 1; i++) {
		//for ( int i = 1; i <= 50; i++ ) {
			//페이지 끝...
			if ( i == 101 ) {
				break;
			}
			
			//System.out.print(i + ", ");
			Document doc = Jsoup.connect(url + "&pageNo=" + i).timeout(10000).get();
			Elements dl_es = doc.select("dl");
			for (Element dl_es_r : dl_es) {
				// System.out.println(e.attr("class"));
				if (dl_es_r.attr("class").equals("list_e2 mar_left")) {
					for (int is = 0; is < dl_es_r.children().size(); is = is + 2) {
						entryId = "";
						hanmun = "";
						word = "";
						mean = "";
						spelling = "";
						type = "";
						isWord = true;
						isDetail = false;

						Element word_e = dl_es_r.child(is);
						for (int iss = 0; iss < word_e.children().size(); iss++) {
							if ("fnt_e30".equals(word_e.child(iss).attr("class"))) {
								entryId = word_e.child(iss).child(0).attr("href");
								word = word_e.child(iss).child(0).text();

								hanmun = word_e.child(iss).ownText().trim();
								hanmun = hanmun.replace("←", "");
								if (entryId.indexOf("enkrEntry.nhn") > -1 || entryId.indexOf("enkrIdiom.nhn") > -1) {
									isHan = false;
								} else if (entryId.indexOf("krenEntry.nhn") > -1
										|| entryId.indexOf("krenIdiom.nhn") > -1) {
									isHan = true;
								}
								if (entryId.indexOf("enkrEntry.nhn") < 0 && entryId.indexOf("enkrIdiom.nhn") < 0
										&& entryId.indexOf("krenEntry.nhn") < 0
										&& entryId.indexOf("krenIdiom.nhn") < 0) {
									isWord = false;
								} else {
									for (int isss = 0; isss < word_e.child(iss).children().size(); isss++) {
										if ("img".equals(word_e.child(iss).child(isss).tagName())
												&& "웹수집".equals(word_e.child(iss).child(isss).attr("alt"))) {
											isWord = false;
											break;
										} else if ("span".equals(word_e.child(iss).child(isss).tagName())
												&& "오픈사전".equals(word_e.child(iss).child(isss).text())) {
											isWord = false;
											break;
										}
									}
								}
							} else if ("fnt_e25".equals(word_e.child(iss).attr("class"))) {
								spelling = word_e.child(iss).text();
							}
						}

						if ("img".equals(dl_es_r.child(is + 1).child(0).tagName())) {
							// 단어 상세 검색 여부
							Element mean_e = dl_es_r.child(is + 1).child(1);
							for (int iss = 0; iss < mean_e.children().size(); iss++) {
								if ("pad6".equals(mean_e.child(iss).attr("class"))) {
									isDetail = true;
									break;
								}
							}

							// 단어 종류, 뜻
							mean_e = dl_es_r.child(is + 1).child(1).child(0);
							for (int iss = 0; iss < mean_e.children().size(); iss++) {
								if ("fnt_k09".equals(mean_e.child(iss).attr("class")) && "".equals(type)) {
									type = mean_e.child(iss).text();
								} else if ("fnt_k05".equals(mean_e.child(iss).attr("class"))) {
									mean = mean_e.child(iss).text();
									break;
								}
							}
						} else {
							// 단어 상세 검색 여부
							Element mean_e = dl_es_r.child(is + 1).child(0);
							for (int iss = 0; iss < mean_e.children().size(); iss++) {
								if ("pad6".equals(mean_e.child(iss).attr("class"))) {
									isDetail = true;
									break;
								}
							}

							// 단어 종류, 뜻
							mean_e = dl_es_r.child(is + 1).child(0).child(0);
							for (int iss = 0; iss < mean_e.children().size(); iss++) {
								if ("fnt_k09".equals(mean_e.child(iss).attr("class")) && "".equals(type)) {
									type = mean_e.child(iss).text();
								} else if ("fnt_k05".equals(mean_e.child(iss).attr("class"))) {
									mean = mean_e.child(iss).text();
									break;
								}
							}
						}

						if (isWord && !"".equals(mean)) {
							//System.out.println(", word : " + word + ", mean : " + mean + ", spelling : " + spelling + ", type : " + type + ", ISDETAIL : " + isDetail + ", isWord : " + isWord);

							HashMap row = new HashMap();
							row.put("ENTRYID", getEntryId(entryId));
							row.put("HANMUN", hanmun);
							row.put("WORD", word);
							row.put("MEAN", mean);
							row.put("SPELLING", spelling);
							row.put("TYPE", type);
							row.put("ISDETAIL", isDetail);
							row.put("ISHAN", isHan);

							if (isDetail) {
								//System.out.println("Detail : " + word);
								HashMap means = null;
								if (isHan) {
									means = naverDicWordDetailHan(getEntryId(entryId), findWord);
								} else {
									means = naverDicWordDetail(getEntryId(entryId), findWord);
								}
								row.put("TENSE", means.get("TENSE"));
								row.put("DETAILS", means.get("DETAILS"));
							}

							words.add(row);
						}
					}

					break;
				}
				if (isBreak) {
					break;
				}
			}
		}
		//System.out.println("");

		return words;
	}

	// 단어 상세 검색
	public static HashMap naverDicWordDetail(String entryId, String findWord) throws Exception {
		HashMap hm = new HashMap();

		ArrayList means_al = new ArrayList();

		String url = "";
		String tense = "";

		if (entryId.indexOf("entryId") > -1) {
			url = "http://endic.naver.com/enkrEntry.nhn?sLn=kr&" + entryId + "&query="
					+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
		} else {
			url = "http://endic.naver.com/enkrIdiom.nhn?sLn=kr&" + entryId + "&query="
					+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
		}
		// System.out.println("Detail : " + url);

		// 동아 entryId를 찾는다.
		Document doc = Jsoup.connect(url).timeout(10000).get();
		Elements ul_es = doc.select("ul");
		String dongaEntry_id = "";
		for (Element ul_es_r : ul_es) {
			if ("dicTypeListDiv".equals(ul_es_r.attr("id"))) {
				for (int i = 0; i < ul_es_r.children().size(); i++) {
					if ("dicType_D".equals(ul_es_r.child(i).attr("id"))) {
						dongaEntry_id = ul_es_r.child(i).child(0).attr("entryId");
						break;
					}
				}
			}
		}

		if ("".equals(dongaEntry_id)) {
			// 그냥 상세 단어 상세 내용
			Elements dl_es = doc.select("dl");
			for (Element dl_es_r : dl_es) {
				if ("list_a3 list_a3_2 list_a3_3".equals(dl_es_r.attr("class"))) {
					HashMap classHm = new HashMap();
					classHm.put("CLASS", "");

					ArrayList class_mean_al = new ArrayList();
					Elements dd_es = dl_es_r.children();
					for (int i = 0; i < dd_es.size(); i++) {
						if ("dt".equals(dd_es.get(i).tagName())) {
							HashMap meanHm = new HashMap();
							meanHm.put("MEAN", dd_es.get(i).child(1).text());

							// 예제
							ArrayList sampleAl = new ArrayList();
							int is = 0;
							for (is = i + 1; is < dd_es.size(); is++) {
								if ("dd".equals(dd_es.get(is).tagName())) {
									sampleAl.add(dd_es.get(is).child(0).text() + " : " + dd_es.get(is).child(1).text());
								} else {
									break;
								}
							}
							meanHm.put("SAMPLE", sampleAl);

							class_mean_al.add(meanHm);
						}
					}
					classHm.put("MEANS", class_mean_al);

					means_al.add(classHm);
				}
			}
		} else {
			// 동아 단어 상세 내용
			if (entryId.indexOf("entryId") > -1) {
				url = "http://endic.naver.com/enkrEntry.nhn?sLn=kr&entryId=" + dongaEntry_id + "&query="
						+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
			} else {
				url = "http://endic.naver.com/enkrIdiom.nhn?sLn=kr&idiomId=" + dongaEntry_id + "&query="
						+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
			}
			// System.out.println("2 : " + url);

			doc = Jsoup.connect(url).timeout(10000).get();

			Elements dl_es = doc.select("dl");
			for (Element dl_es_c : dl_es) {
				if ("sync".equals(dl_es_c.attr("class"))) {
					Elements dl_es_c_c = dl_es_c.children();
					for (int i = 0; i < dl_es_c_c.size(); i++) {
						if ("dd".equals(dl_es_c_c.get(i).tagName())) {
							for (Element dl_es_c_c_c : dl_es_c_c.get(i).child(0).children()) {
								for (Element dl_es_c_c_c_c : dl_es_c_c_c.children()) {
									if ("fnt_k10".equals(dl_es_c_c_c_c.attr("class"))) {
										tense += dl_es_c_c_c_c.text() + "^";
									} else if ("fnt_e07".equals(dl_es_c_c_c_c.attr("class"))) {
										tense += dl_es_c_c_c_c.text() + ",";
									}
								}
							}
						}
					}
				}
			}
			if (!"".equals(tense)) {
				tense = tense.substring(0, tense.length() - 1);
			}
			
			Elements div_es = doc.select("div");
			for (Element div_es_r : div_es) {
				if (div_es_r.attr("class").indexOf("box_wrap1 dicType_D") > -1) {
					HashMap classHm = new HashMap();
					Element classE = getElement(div_es_r, "h3", 0);
					classHm.put("CLASS", getElementText(classE));

					ArrayList class_mean_al = new ArrayList();

					// 품사가 있는 경우 없는 경우가 있다.
					Elements dd_es = getChildElement(div_es_r, "dl", 0);
					for (int i = 0; i < dd_es.size(); i++) {
						if ("dt".equals(dd_es.get(i).tagName())) {
							HashMap meanHm = new HashMap();
							int emPos = (dd_es.get(i).children().size() == 1 ? 0 : 1); // 순번이 있거나 없을때 뜻 위치..
							String mean = dd_es.get(i).child(emPos).text().replace("((", "(").replace("))", ")")
									.replace("[[", "[").replace("]]", "]");
							if (mean.indexOf("예문닫기") > -1) {
								mean = mean.substring(0, mean.length() - 4);
							}
							meanHm.put("MEAN", mean.trim());

							// 예제
							ArrayList sampleAl = new ArrayList();
							int is = 0;
							for (is = i + 1; is < dd_es.size(); is++) {
								if ("dd".equals(dd_es.get(is).tagName()) && dd_es.get(is).children().size() == 2) {
									sampleAl.add(dd_es.get(is).child(0).text() + " : " + dd_es.get(is).child(1).text());
								} else {
									break;
								}
							}
							meanHm.put("SAMPLE", sampleAl);

							class_mean_al.add(meanHm);
						}
					}
					classHm.put("MEANS", class_mean_al);

					means_al.add(classHm);
				}
			}
		}
		// System.out.println(means_al);

		hm.put("TENSE", tense);
		hm.put("DETAILS", means_al);

		return hm;
	}

	// 한국어 단어 상세 검색
	public static HashMap naverDicWordDetailHan(String entryId, String findWord) throws Exception {
		HashMap hm = new HashMap();

		ArrayList means_al = new ArrayList();

		String url = "";
		String tense = "";

		if (entryId.indexOf("entryId") > -1) {
			url = "http://endic.naver.com/krenEntry.nhn?sLn=kr&" + entryId + "&query="
					+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
		} else {
			url = "http://endic.naver.com/krenIdiom.nhn?sLn=kr&" + entryId + "&query="
					+ URLEncoder.encode(findWord, "UTF-8").replaceAll("[+]", "%20");
		}
		// System.out.println("Detail : " + url);
		
		ArrayList class_mean_al = new ArrayList();
		
		Document doc = Jsoup.connect(url).timeout(10000).get();
		Elements dl_es = doc.select("dl");
		for (Element dl_es_c : dl_es) {
			if ("list_a11 list_a11_idom".equals(dl_es_c.attr("class"))) {
				for (int i = 0; i < dl_es_c.children().size(); i++) {
					if ("dt".equals(dl_es_c.child(i).tagName())) {
						HashMap meanHm = new HashMap();
						meanHm.put("MEAN", dl_es_c.child(i).child(1).text());
						class_mean_al.add(meanHm);
					}
				}
			}
		}

		HashMap classHm = new HashMap();
		classHm.put("MEANS", class_mean_al);
		means_al.add(classHm);
		hm.put("DETAILS", means_al);

		return hm;
	}

	public static String getEntryId(String url) throws Exception {
		String rtn = "";

		if (url.indexOf("?") < 0) {
			return "";
		}
		String[] split_url = url.split("[?]");
		String[] split_param = split_url[1].split("[&]");
		for (int i = 0; i < split_param.length; i++) {
			String[] split_row = split_param[i].split("[=]");
			if ("entryId".equals(split_row[0]) || "idiomId".equals(split_row[0])) {
				rtn = split_row[0] + "=" + split_row[1];
			}
		}

		return rtn;
	}

	// 예제 검색
	public static ArrayList naverDicSampleList(String url, int cnt) throws Exception {
		ArrayList samples = new ArrayList<HashMap>();
		boolean isLast = false;

		//System.out.print(url + " : ");
		for (int i = 1; i <= (cnt / 20 > 100 ? 100 : (cnt / 20) + 1); i++) {
		//for ( int i = 1; i <= 2; i++ ) {
			//System.out.print(i + ", ");
			Document doc = Jsoup.connect(url + "&pageNo=" + i).timeout(10000).get();
			Elements ul_es = doc.select("ul");
			for (Element ul_e : ul_es) {
				if (ul_e.attr("class").equals("list_a list_a_mar")) {
					for (int is = 0; is < ul_e.children().size(); is++) {
						Element ul_li_e = ul_e.child(is);

						String sample = "";

						// 영어 예문
						for (Element ul_li_e_1 : ul_li_e.child(0).children()) {
							if (ul_li_e_1.tagName().equals("span") && ul_li_e_1.attr("class").equals("fnt_e09")) {

								sample = ul_li_e_1.text() + " : ";
								break;
							}
						}
						// 영어 해석
						if (ul_li_e.child(1).attr("class").equals("mar_top1")) {
							for (Element ul_li_e_1 : ul_li_e.child(1).child(0).children()) {
								if (ul_li_e_1.tagName().equals("a")
										&& ul_li_e_1.attr("class").equals("N=a:xmp.detail")) {

									sample += ul_li_e_1.text();
									break;
								}
							}

							samples.add(sample);
						} else {
							// 미해석...
							isLast = true;
						}
					}

					break;
				}
			}

			if (isLast) {
				break;
			}
		}

		return samples;
	}
	
	public static Elements getChildElement( Element tElement, String tag, int pos ) throws Exception {
		int findPos = 0;
		for (int i = 0; i < tElement.children().size(); i++) {
			if ( tag.equals(tElement.child(i).tagName()) ) {
				if ( findPos == pos ) {
					return tElement.child(i).children();
				} else {
					findPos++;
				}
			}
		}

		return null;
	}

	public static Element getElement( Element tElement, String tag, int pos ) throws Exception {
		int findPos = 0;
		for (int i = 0; i < tElement.children().size(); i++) {
			if ( tag.equals(tElement.child(i).tagName()) ) {
				if ( findPos == pos) {
					return tElement.child(i);
				} else {
					findPos++;
				}
			}
		}

		return null;
	}

	public static String getElementText( Element tElement ) throws Exception {
		if ( tElement == null ) {
			return "";
		} else {
			return tElement.text();
		}
	}

}