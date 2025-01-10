package com.example.crawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.example.model.School;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;  // 引入 @Component 註解

@Component
public class SchoolCrawler {
   
    public List<School> fetchSchools(String searchKeyword, String city, String subject, String other) {
        List<School> schools = new ArrayList<>();
        //組合並搜尋關鍵字
        
        searchKeyword = String.format("%s %s %s 補習班", 
                                      city != null ? city : "", 
                                      subject != null ? subject : "", 
                                      other != null ? other : "").trim();

        if (searchKeyword.isEmpty()) {
            System.err.println("搜尋關鍵字為空，請提供正確的搜尋條件！");
            return schools;
        }

       //正式爬取
        String googleSearchUrl = "https://www.google.com/search?q=" + searchKeyword + "&num=10";

        try {
           
            Document doc = Jsoup.connect(googleSearchUrl).get();
            Elements links = doc.select("h3");
            
            for (Element link : links) {
                String title = link.text();
                String url = link.parent().attr("href");

                  Boolean specialChoice = null;
				School school = new School(title, city, subject, url,specialChoice);
                    schools.add(school);
                
            }
        } catch (IOException e) {
          
            System.err.println("Error processing content from URL: " + googleSearchUrl);
            e.printStackTrace();
        } catch (Exception e) {
            
            System.err.println("Unexpected error during the search process.");
            e.printStackTrace();
        }

        return schools;
    
    }

    
 

}
