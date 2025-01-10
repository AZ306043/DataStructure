package com.example.util;

import com.example.model.School;

import com.example.WordCounter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScoreCalculator {

    public void calculateScores(List<School> schools, String city, String subject, String other) {
    	List<Node> keywordWeights = new ArrayList<>();
    	//設定關鍵字和權重
        keywordWeights.add(new Node("薪哲", 2));
        keywordWeights.add(new Node("得勝者", 2));
        keywordWeights.add(new Node("台大明明", 2));
        keywordWeights.add(new Node("陳立", 2));
        keywordWeights.add(new Node("頂大教師", 1));
        keywordWeights.add(new Node("試聽", 3));
        keywordWeights.add(new Node("升學", 3));
        keywordWeights.add(new Node("名師", 3));
        keywordWeights.add(new Node("一對一", 3));
        keywordWeights.add(new Node("團體課", 2));
        keywordWeights.add(new Node("學測", 3));
        keywordWeights.add(new Node("指考", 3));
        keywordWeights.add(new Node("專業證照", 2));
        keywordWeights.add(new Node("短期衝刺", 3));
        keywordWeights.add(new Node("學霸", 2));
        keywordWeights.add(new Node("輔導課程", 2));
        keywordWeights.add(new Node("數學", 3));
        keywordWeights.add(new Node("英文", 3));
        keywordWeights.add(new Node("自然科學", 2));
        keywordWeights.add(new Node("國文", 2));
        keywordWeights.add(new Node("高分", 3));
        keywordWeights.add(new Node("作文", 2));
        keywordWeights.add(new Node("閱讀測驗", 2));
        keywordWeights.add(new Node("考前衝刺", 3));
        keywordWeights.add(new Node("專題製作", 1));
        keywordWeights.add(new Node("自主學習", 1));
        keywordWeights.add(new Node("個別輔導", 3));
        keywordWeights.add(new Node("名校推薦", 2));
        keywordWeights.add(new Node("線上課程", 2));
        keywordWeights.add(new Node("小班制", 3));
        keywordWeights.add(new Node("錄取率", 3));
        keywordWeights.add(new Node("基礎加強", 2));
        keywordWeights.add(new Node("升學輔導", 3));
        keywordWeights.add(new Node("親子共學", 1));
        keywordWeights.add(new Node("師資優良", 3));
        keywordWeights.add(new Node("課程彈性", 2));
        keywordWeights.add(new Node("教材多元", 2));
        keywordWeights.add(new Node("補救教學", 1));
        keywordWeights.add(new Node("職涯規劃", 1));

        
        
        for (School school : schools) {
            int score = 0;

            //地區符合+35
            if (city != null && city.equalsIgnoreCase(school.getCity())) {
                score += 35;  
            }

            //科目符合+35
            if (subject != null && subject.equalsIgnoreCase(school.getSubject())) {
                score += 35;  
            }

            //把使用者的關鍵字納入分數計算
            if (other != null && !other.trim().isEmpty()) {
                String[] keywords = other.split(",");  
                int count=0;
                
                for (String keyword : keywords) {
                    keyword = keyword.trim();  
                    count++;

                    try {
                        WordCounter wordCounter = new WordCounter(school.getUrl());
                        int matchCount = wordCounter.countKeyword(keyword);  

                      
                        double weight = 0.2 * ((double) matchCount / count);
                        score += weight;  
                    } catch (IOException e) {
                        System.out.println("Error fetching or processing the content from URL: " + school.getUrl());
                    }
                }
            }
            
            //拿我們設定的關鍵字加分
            for (Node node : keywordWeights) {
                String keyword = node.getKeyword();
                int weight = node.getWeight();

                try {
                    
                    WordCounter wordCounter = new WordCounter(school.getUrl());
                    int matchCount = wordCounter.countKeyword(keyword); 

                    
                    score += matchCount * weight/keywordWeights.size()*20*0.3;  
                } catch (IOException e) {
                    System.out.println("Error fetching or processing the content from URL: " + school.getUrl());
                }
            }
            school.setScore(score);
        }
    }
    
    
    class Node {
        String keyword;
        int weight;

        public Node(String keyword, int weight) {
            this.keyword = keyword;
            this.weight = weight;
        }

        public String getKeyword() {
            return keyword;
        }

        public int getWeight() {
            return weight;
        }
    }
}
