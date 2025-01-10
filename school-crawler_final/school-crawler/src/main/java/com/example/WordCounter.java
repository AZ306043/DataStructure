package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WordCounter {
    private String urlStr;
    private String content;

    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
    }

   
    private String fetchContent() throws IOException {
    	//爬取特定url的關鍵字
        URL url = new URL(this.urlStr);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        StringBuilder retVal = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            retVal.append(line).append("\n");
        }
        return retVal.toString();
    }

    //用Boyer-Moore爬
    public int BoyerMoore(String T, String P) {
        int n = T.length();
        int m = P.length();
        int i = m - 1;
        int j = m - 1;
        int count = 0;

        while (i < n) {
            if (T.charAt(i) == P.charAt(j)) {
                if (j == 0) {
                    count++;
                    i += m;
                    j = m - 1;
                } else {
                    i--;
                    j--;
                }
            } else {
                int lastOccurrence = last(T.charAt(i), P);
                i = i + m - Math.min(j, 1 + lastOccurrence);
                j = m - 1;
            }
        }

        return count;
    }

    
    public int last(char c, String P) {
        for (int i = P.length() - 1; i >= 0; i--) {
            if (P.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    // 計算關鍵字出現次數
    public int countKeyword(String keyword) throws IOException {
        if (content == null) {
            content = fetchContent();
        }

        content = content.toUpperCase();
        keyword = keyword.toUpperCase();

        int count = 0;
        int index = content.indexOf(keyword);

        while (index != -1) {
            count++;
            index = content.indexOf(keyword, index + keyword.length());
        }

        return count;
    }
}
