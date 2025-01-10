搜尋過程：
使用關鍵字構建搜尋條件：
系統會根據用戶提供的city（所在城市）、subject （欲補習科目）和 other （其他要求）字段來組合搜尋條件，並使用這些條件進行 Google 搜尋。搜尋結果會返回以該條件進行權重分數計算後排列的補習班列表。
發送搜尋請求：
系統構建好搜尋 URL（如 "https://www.google.com/search?q=city subject other 補習班"）後，使用 Jsoup 發送 HTTP 請求並解析返回的 HTML 頁面。
解析搜尋結果：
在搜尋結果中，系統解析出 h3標籤的內容，這些標籤通常包含搜索結果的標題，並且抓取該標題的連結。
生成補習班對象並評分：
每個搜索結果會被轉換為 School 物件，並存儲其中，包括學校名稱、城市、科目、URL 和特殊選擇標記，再根據後端四項評分標準計算各網頁及其子頁面加權分數。
返回結果：
系統將排列後結果返回給前端，依分數由高至低進行展示。


1.前端
search.html
搜尋頁面模板，使用 HTML 與 CSS 設計輸入表單，包含選擇地區、科目及自訂條件的欄位。
支援 RWD，確保不同裝置上的使用體驗一致。
將地區和科目使用下拉式選單顯示，供使用者方便且快速選擇，並設有“其他”選項供使用者輸入非預設選項。
將其他條件設為輸入區，使用者可自行輸入所需關鍵字，並用,隔開（如 學測,台北車站,高中,台大,……)，系統就會將以上關鍵字列入權重計算。
searchResults.html
搜尋結果頁面模板，動態渲染補習班清單，顯示學校名稱、網址及分數。
特佳選擇用醒目文字標記顯示。
styles.css
提供統一的樣式支持，設計簡潔且易於閱讀的視覺介面。
包含陰影、背景顏色及過渡效果以增強使用者體驗。
    2.後端
Application.java
Spring Boot 啟動類，初始化並使整個應用程式開始運行。
SearchController
負責處理搜尋請求，根據使用者條件（地區、科目等）調用爬蟲及分數計算邏輯。
 連結前後端，提供 /search 和 /search/results 兩個路由。(/search: 處理搜尋介面 search.html，/search/results: 接收使用者輸入的地區、科目及其他條件，將搜尋結果傳遞至 searchResults.html 顯示)
使用 Trie 資料結構高效儲存和檢索搜尋結果。
過濾不必要的網站資料（如含廣告或不相關內容的網站）。
進行子頁面爬取及算分，因大部分補習班網頁子頁面深度不深故只爬取兩層，除子頁面城市和科目外之分數加到補習班總分。
標註總分高於100的補習班為特佳選擇(specialChoice=true)。
SchoolCrawler
基於 Jsoup 爬取補習班相關網站資訊，將結果整理為 School 類型列表。
用city+subject+other補習班進行搜尋，再以Google 搜尋之結果，爬取網站標題和連結。
ScoreCalculator
根據使用者輸入條件及預設關鍵字計算每個補習班的分數。
權重分數依據是否符合所選地區和科目，以及關鍵字出現次數和匹配程度計算。
用node定義使用者輸入的自定義關鍵字，依出現次數增加額外分數。
School
資料類，描述補習班的基本屬性，包括名稱、地區、科目、網址、分數和是否為特加選擇。
WordCounter
用於計算目標網站內關鍵字出現次數，使用Boyer-Moore 字符串匹配算法。




評分過程：
設定關鍵字及權重：
系統設定了一組關鍵字（如短期衝刺、升學、學測、視聽等），每個關鍵字有不同的權重，用於計算補習班的評分。
檢查城市和科目：
如果補習班的 city 和 subject 與用戶選擇的匹配，則為補習班加上額外的分數。具體來說：
城市匹配：如果補習班的城市與用戶提供的城市匹配，學校會獲得 35 分。
科目匹配：如果補習班的科目與用戶提供的科目匹配，學校會獲得 35 分。
計算關鍵字匹配分數：
用戶在 other 欄位提供的關鍵字會與學校的網頁內容進行匹配，系統會計算關鍵字在補習班中出現的次數，並根據出現次數為補習班加分。
加權關鍵字匹配：
系統還根據預設的關鍵字（例如，名師, 學測, 個別輔導, 數學, 專業證照 等）在補習班網站內容中的出現次數進行額外加分。每個關鍵字的權重會影響分數的計算。
例如，如果補習班網站中提到學測，且這個關鍵字的權重是 3，那麼就會為學校加上 count * weight 的分數。
計算最終評分：
綜合考慮以上因素，最終得出每個補習班的評分。評分是根據匹配條件和關鍵字出現的頻次計算的，分數越高，學校的評價就愈高，同時高於100分的搜尋結果會在前端特別標示成特特佳選擇，供使用者快速參考。
