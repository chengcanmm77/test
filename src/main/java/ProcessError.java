import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class ProcessError {
	
	public static final String  PRO_SUFFIX = "0000000000";
	
	public static final String HTML_SUFFIX = ".html";
	
	public static final String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2013/";
	
	public static List<String> list = new ArrayList<String>();
	
	public static List<ErrorList> error = new ArrayList<ErrorList>();
					
	
	public static void main(String[] args) throws IOException {
		 String indexUrl = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2013/index.html";
		 parseProvice(indexUrl);
		 writeSql();
		 
		 System.out.println("=====´íÎó¼ÇÂ¼ÌõÊý="+error.size());
		 
		 for(int i=0;i<error.size();i++){
			 processErrorRecord(error.get(i),i);
		 }
	}
	
	public static void processErrorRecord(ErrorList errorRecord,int i) {
		try {
			if("CITY".equals(errorRecord.getType())){
				parseCity(errorRecord.getUrl(),errorRecord.getParentId());
			}else if("COUNTY".equalsIgnoreCase(errorRecord.getType())) {
				parseCountry(errorRecord.getUrl(),errorRecord.getParentId());
			}else if("TOWNER".equalsIgnoreCase(errorRecord.getType())) {
				parseTowner(errorRecord.getUrl(),errorRecord.getParentId());
			}else if("VILLAGE".equalsIgnoreCase(errorRecord.getType())) {
				parseVillage(errorRecord.getUrl(),errorRecord.getParentId());
			}
			error.remove(i);
		} catch (Exception e) {
			error.add(errorRecord);
			e.printStackTrace();
		}
	}
	
	public static void parseProvice(String proUrl) throws IOException {
		Document doc = getDocument(proUrl);
		 
		 Elements proviceTrs = doc.select(".provincetr");
		 Iterator<Element> iter = proviceTrs.iterator();
		 
		 while(iter.hasNext()) {
			 Element proviceTr = iter.next();
			 Elements provices = proviceTr.getElementsByTag("a");
			 for (Element provice : provices) {
                String pageUrl = provice.attr("href");
                String proviceName = provice.text().trim();
                String proviceShortId = pageUrl.substring(0, pageUrl.length()-5);
                Long proviceId = Long.parseLong(proviceShortId+PRO_SUFFIX);
                
                buildSql(proviceName,proviceShortId+PRO_SUFFIX,null);
                System.out.println("proviceName"+proviceName);
                try {
					parseCity(buildUrl(proUrl, pageUrl),proviceId);
				} catch (Exception e) {
					buildError(buildUrl(proUrl, pageUrl),proviceName,proviceId,"CITY");
					e.printStackTrace();
				}
            }

		 }
	}
	
	public static void buildError(String errUrl,String name,Long parentId,String type) {
		ErrorList err = new ErrorList();
		err.setName(name);
		err.setParentId(parentId);
		err.setUrl(errUrl);
		err.setType(type);
		error.add(err);
		System.out.println(err.toString());
	}
	
	public static void parseCity(String cityUrl,Long parentId) throws IOException{
		if(cityUrl == null || "".equals(cityUrl)){
			return;
		}
		
		
		 Document doc = getDocument(cityUrl);
		 Elements cityTrs = doc.select(".citytr");
		 Iterator<Element> iter = cityTrs.iterator();
		 while(iter.hasNext()) {
			 Element cityTr = iter.next();
			 Elements citys = cityTr.getElementsByTag("a");
			 String pageUrl = citys.get(0).attr("href");
			 String cityId = citys.get(0).text().trim();
			 String cityName = citys.get(1).text().trim();
			 
				if(("11"+PRO_SUFFIX).equals(Long.toString(parentId))||("31"+PRO_SUFFIX).equals(Long.toString(parentId))||
				 ("12"+PRO_SUFFIX).equals(Long.toString(parentId))||("50"+PRO_SUFFIX).equals(Long.toString(parentId))){
					 try {
						parseCountry(buildUrl(cityUrl, pageUrl),parentId);
					} catch (Exception e) {
						buildError(buildUrl(cityUrl, pageUrl), cityName, parentId,"COUNTY");
						e.printStackTrace();
					}
				 }else{
					try {
						buildSql(cityName, cityId, parentId);
						parseCountry(buildUrl(cityUrl, pageUrl),Long.parseLong(cityId));
					} catch (Exception e) {
						buildError(buildUrl(cityUrl, pageUrl), cityName, Long.parseLong(cityId),"COUNTY");
						e.printStackTrace();
					}
				}
		 }
	}
	
	public static void parseCountry(String countryUrl,Long parentId) throws IOException {
		if(countryUrl == null || "".equals(countryUrl)){
			return;
		}
		
		 Document doc = getDocument(countryUrl);
		 Elements countyTrs = doc.select(".countytr");
		 Iterator<Element> iter = countyTrs.iterator();
		 while(iter.hasNext()) {
			 Element countyTr = iter.next();
			 Elements countys = countyTr.getElementsByTag("a");
			 if(countys.isEmpty()) {
				 continue;
			 }
			 String pageUrl = countys.get(0).attr("href");
			 String countyId = countys.get(0).text().trim();
			 String countyName = countys.get(1).text().trim();
			 buildSql(countyName,countyId,parentId);
			 try {
				parseTowner(buildUrl(countryUrl, pageUrl),Long.parseLong(countyId));
			} catch (Exception e) {
				buildError(buildUrl(countryUrl, pageUrl),countyName,Long.parseLong(countyId),"TOWNER");
				e.printStackTrace();
			}
		 }
	}
	
	public static void parseTowner(String townUrl,Long parentId) throws IOException {
		if(townUrl == null || "".equals(townUrl)){
			return;
		}
		
		Document doc = getDocument(townUrl);
		 Elements townTrs = doc.select(".towntr");
		 Iterator<Element> iter = townTrs.iterator();
		 while(iter.hasNext()) {
			 Element townTr = iter.next();
			 Elements town = townTr.getElementsByTag("a");
			 if(town.isEmpty()) {
				 continue;
			 }
			 String pageUrl = town.get(0).attr("href");
			 String townId = town.get(0).text().trim();
			 String townName = town.get(1).text().trim();
			 buildSql(townName,townId,parentId);
			 try {
				parseVillage(buildUrl(townUrl, pageUrl),Long.parseLong(townId));
			} catch (Exception e) {
				buildError(buildUrl(townUrl, pageUrl),townName,Long.parseLong(townId),"VILLAGE");
				e.printStackTrace();
			}
		 }
	}
	
	public static void parseVillage(String villageUrl,Long parentId) throws IOException {
		if(villageUrl == null || "".equals(villageUrl)){
			return;
		}
		
		Document doc = getDocument(villageUrl);
		 Elements villageTrs = doc.select(".villagetr");
		 Iterator<Element> iter = villageTrs.iterator();
		 while(iter.hasNext()) {
			 Element villageTr = iter.next();
			 Elements village = villageTr.getElementsByTag("td");
			 if(village.isEmpty()) {
				 continue;
			 }
			 String villageId = village.get(0).text().trim();
			 String villageName = village.get(2).text().trim();
			 buildSql(villageName,villageId,parentId);
		 }
	}
	
	public static String buildUrl(String currentUrl,String pageUrl) {
		int lastIndex = currentUrl.lastIndexOf("/");
		String priUrl = currentUrl.substring(0, lastIndex+1);
		return priUrl+pageUrl;
	}
	
	public static Document getDocument(String path) throws IOException {
		
		return Jsoup.connect(path).userAgent("Mozilla").timeout(0).get();
		
//		boolean flag = true;
//		Document doc = null;
//		
//		while(true) {
//			
//			 try {
//				doc = Jsoup.connect(path).userAgent("Mozilla").timeout(0).get();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			 
//			 if(doc != null) {
//				 flag = false;
//				 break;
//			 }
//			 
//			 try {
//				Thread.currentThread().sleep(30000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return doc;
		
	}
	
	public static void buildSql(String name,String id,Long parentId) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into area_info(id,name,traditionalName,parent_id) values ( ");
		sql.append(Long.parseLong(id));
		sql.append(",\'");
		sql.append(name);
		sql.append("\',\'");
		sql.append(name);
		sql.append("\',");
		sql.append(parentId);
		sql.append("); \r\n ");
		String sqlStr = sql.toString();
		list.add(sqlStr);
		if(list.size()>1000){
			list.add(" commit; \r\n");
			writeSql();
		}
	}
	
	public static void writeSql() {
		if(list.size() == 0) {
			return;
		}
		
		FileOutputStream fs = null;
		try {
			File file = new File("d:\\sql.sql");
			if(!file.exists()) {
				file.createNewFile();
			}
			
			 fs = new FileOutputStream(file, true); 
			for(int i=0;i<list.size();i++){
				String str = list.get(i);
				fs.write(str.getBytes());
			}
			fs.flush();
			list.clear();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} finally{
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
