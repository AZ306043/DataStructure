
package com.example.controller;

import com.example.crawler.SchoolCrawler;
import com.example.util.ScoreCalculator;
import com.example.model.School;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;



@Controller
public class SearchController {

    @Autowired
    private SchoolCrawler schoolCrawler;

    @Autowired
    private ScoreCalculator scoreCalculator;

    private SchoolTrie schoolTrie = new SchoolTrie(); 
    //不必要的網頁中會有的詞
    private static final HashSet<String> FILTER_KEYWORDS = new HashSet<>(List.of("徵才", "人力銀行", "達人網", "指南","業配","排行","商品","報導","新聞","高級中學","國民小學","國民中學","推薦","360"));


    
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String searchForm() {
        return "search"; // 回傳到搜尋介面(search.html)
    }

    // 修改為處理搜尋請求
    @RequestMapping(value = "/search/results", method = RequestMethod.GET)
    public String searchResults(@RequestParam(name = "city", required = false) String city,
                                 @RequestParam(name = "customCity", required = false) String customCity,
                                 @RequestParam(name = "subject", required = false) String subject,
                                 @RequestParam(name = "customSubject", required = false) String customSubject,
                                 @RequestParam(name = "other", required = false) String other,
                                 Model model) throws IOException {

        // 如果在前端選 "其他" 地區或科目，就把自訂義輸入變成正式city或subject
        if ("其他".equals(city)) {
            city = customCity;
        }
        if ("其他".equals(subject)) {
            subject = customSubject;
        }

        // 把city,subject跟other組合
        String searchKeyword = String.format("%s %s %s 補習班",
                city != null ? city : "",
                subject != null ? subject : "",
                other != null ? other : "").trim();

        //資料開爬
        List<School> schools = schoolCrawler.fetchSchools(searchKeyword, city, subject, other);

        // 如果搜尋失敗
        if (schools == null || schools.isEmpty()) {
            System.out.println("搜尋失敗，沒有找到任何結果");
        } else {
            // 成功就插入到 Trie 資料結構
            for (School school : schools) {
                schoolTrie.insert(city, subject, other, school);
                System.out.println("搜尋到網頁:"+school.getName());
                System.out.println("網址:"+school.getUrl());
                System.out.println("-----------------------");
            }
        }

      
        List<School> matchedSchools = schoolTrie.search(city, subject, other);

        if (matchedSchools != null) {
            matchedSchools.removeIf(school -> {
            	//刪掉有不必要字元的網頁
                boolean shouldRemove = FILTER_KEYWORDS.stream().anyMatch(keyword -> 
                    school.getName().contains(keyword) || school.getName().contains(keyword));
                
                
                if (shouldRemove) {
                    System.out.println("刪除網站: " + school.getName() + ", 網址: " + school.getUrl());
                }
                return shouldRemove;
            });
            
            matchedSchools.removeIf(school -> {
                // 檢查學校名稱是否包含「地圖」或「更多地點」等非必要標題
                boolean shouldRemove = school.getName().contains("地圖") || school.getName().contains("更多地點");
                
                if (shouldRemove) {
                    System.out.println("刪除網站: " + school.getName() + ", 網址: " + school.getUrl());
                }
                return shouldRemove;
            });
        }

       
        if (matchedSchools != null) {
            Map<School, School> parentToChildMap = new HashMap<>(); 

            scoreCalculator.calculateScores(matchedSchools, city, subject, other);

            //把子母頁面分數加總
            for (School school : matchedSchools) {
                if (isChildPage(school)) {  
                    School parentSchool = findParentSchool(school, matchedSchools);
                    if (parentSchool != null) {
                        parentToChildMap.put(parentSchool, school);
                        parentSchool.setScore(parentSchool.getScore() + school.getScore());
                    }
                }
            }

            // 特佳選擇，高於100者為true
            for (School school : matchedSchools) {
                if (school.getScore() > 100) {
                    school.setSpecialChoice(true); 
                }
            }

            
            PriorityQueue<School> maxHeap = new PriorityQueue<>((a, b) -> b.getScore() - a.getScore());
            maxHeap.addAll(matchedSchools);

            matchedSchools.clear();
            while (!maxHeap.isEmpty()) {
                matchedSchools.add(maxHeap.poll());
            }
        }

      
        model.addAttribute("schools", matchedSchools);
        return "searchResults"; // 把結果傳給搜尋結果頁面(searchResult.html)
    }

    private boolean isChildPage(School school) {
        if (school.getUrl() == null || school.getUrl().isEmpty()) {
            return false;
        }

        try {
            // 避免觸發 400 錯誤
            org.jsoup.Connection connection = Jsoup.connect(school.getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000); // 設定超時時間，防止卡住

            // 爬母頁面
            org.jsoup.nodes.Document doc = connection.get();

            // 找出所有的 <a> 標籤
            Elements links = doc.select("a[href]");

            // 記錄子頁面的 URL
            Set<String> childUrls = new HashSet<>();

            // 檢查是否有指向相同母頁的 URL
            for (org.jsoup.nodes.Element link : links) {
                String href = link.attr("href");
                if (href.startsWith(school.getUrl()) && !href.equals(school.getUrl())) {
                    childUrls.add(href);  // 儲存子頁面 URL
                }
            }

            // 總共只爬取兩層子頁面，補習班通常沒有子頁面，有也不會很多層
            if (!childUrls.isEmpty()) {
                for (String childUrl : childUrls) {
                    
                    org.jsoup.nodes.Document childDoc = Jsoup.connect(childUrl).get();
                    Elements childLinks = childDoc.select("a[href]");
                    for (org.jsoup.nodes.Element childLink : childLinks) {
                        String secondLevelHref = childLink.attr("href");
                    
                        if (secondLevelHref.startsWith(school.getUrl()) && !secondLevelHref.equals(childUrl)) {
                            return true;  
                        }
                    }
                }
            }
        } catch (IOException e) {
           
            System.err.println("Error fetching URL: " + school.getUrl() + " - " + e.getMessage());
        }

        return false;  
    }



   
    private School findParentSchool(School childSchool, List<School> matchedSchools) {
       
        for (School school : matchedSchools) {
            if (childSchool.getUrl().startsWith(school.getUrl()) && !childSchool.equals(school)) {
                return school;  
            }
        }
        return null; 
    }




    // 補習班資料用Trie儲存，用innerClass比較方便
    private static class TrieNode {
        Map<String, TrieNode> children; 
        List<School> schools; 

        public TrieNode() {
            this.children = new HashMap<>();
            this.schools = new ArrayList<>();
        }
    }

    // Trie的結構
    private static class SchoolTrie {
        private TrieNode root;

        public SchoolTrie() {
            root = new TrieNode();
        }

        public void insert(String city, String subject, String other, School school) {
            TrieNode node = root;

            for (String key : Arrays.asList(city, subject, other)) {
                node.children.putIfAbsent(key, new TrieNode());
                node = node.children.get(key);
            }
            node.schools.add(school); 
        }

        public List<School> search(String city, String subject, String other) {
            TrieNode node = root;

            for (String key : Arrays.asList(city, subject, other)) {
                if (!node.children.containsKey(key)) {
                    return Collections.emptyList(); 
                }
                node = node.children.get(key);
            }
            return node.schools; 
        }
    }
}

