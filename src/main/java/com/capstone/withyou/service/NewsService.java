package com.capstone.withyou.service;

import com.capstone.withyou.dao.News;
import com.capstone.withyou.dao.StockPrediction;
import com.capstone.withyou.dto.NewsDTO;
import com.capstone.withyou.dto.StockPredictionDTO;
import com.capstone.withyou.repository.NewsRepository;
import com.capstone.withyou.repository.StockPredictionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsService {

    @Value("${naver.client.id}")
    private String CLIENT_ID;

    @Value("${naver.client.secret}")
    private String CLIENT_SECRET;

    private static final List<PressInfo> SITE_LIST = getPressList(); // 언론사 정보
    private static final long CACHE_DURATION = 10 * 60 * 1000; // 뉴스 유효 시간(10분)

    private final NewsRepository newsRepository;
    private final StockPredictionRepository stockPredictionRepository;
    private final ChatGptService chatGptService;

    public NewsService(NewsRepository newsRepository, StockPredictionRepository stockPredictionRepository, ChatGptService chatGptService) {
        this.newsRepository = newsRepository;
        this.stockPredictionRepository = stockPredictionRepository;
        this.chatGptService = chatGptService;
    }

    @Getter @Setter
    @AllArgsConstructor
    // 언론사 내부 클래스
    private static class PressInfo {

        private String site; // 도메인
        private String pressName; // 언론사명
    }

    // 뉴스 조회
    public List<NewsDTO> getNews(String stockName, String category, int page) {
        List<News> cachedNews = newsRepository.findByStockName(stockName);

        // 뉴스 데이터가 존재하고, 유효하면 NewsDTO로 변환 후 리턴
        if(!cachedNews.isEmpty()
                && isCacheValid(cachedNews.get(0).getLastViewTime())){
            return convertToDTO(cachedNews);
        }

        // 그렇지 않으면 기존 뉴스 데이터 삭제 + 새로운 뉴스 데이터 가져와서 DB에 업데이트
        List<NewsDTO> newsList = fetchNewsFromNaver(stockName, category, page); // 뉴스 정보
        String prediction = chatGptService.predictStockResult(stockName, newsList); // 예측 결과

        updateNewsCache(stockName, newsList, prediction);
        return convertToDTO(newsRepository.findByStockName(stockName));
    }

    // 주식 예측 결과 조회
    public StockPredictionDTO getPrediction(String stockName) {
        StockPrediction prediction = stockPredictionRepository.findTopByStockName(stockName);
        return new StockPredictionDTO(prediction.getStockName(), prediction.getPredictedResult());
    }

    // 네이버에서 특정 주식에 대한 뉴스 가져오기
    private List<NewsDTO> fetchNewsFromNaver(String stockName, String category, int page) {
        List<NewsDTO> newsList = new ArrayList<>();

        try {
            // 검색어와 카테고리 URL 인코딩
            String query;
            query = URLEncoder.encode(stockName + " " + category, StandardCharsets.UTF_8);

            // Api URL 설정
            String apiURL = buildApiUrl(query, page);

            // HTTP 요청 설정
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

            // 응답 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // JSON 파싱
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray items = jsonResponse.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                NewsDTO newsDTO = createNewsDTO(item);
                newsList.add(newsDTO);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsList;
    }

    // 뉴스 데이터 캐시 유효 체크
    private boolean isCacheValid(LocalDateTime lastViewTime) {
        return Duration.between(lastViewTime, LocalDateTime.now())
                .toMillis()<CACHE_DURATION;
    }

    // 기존 뉴스 데이터 지우고 새로 업데이트
    private void updateNewsCache(String stockName, List<NewsDTO> newsList, String prediction) {
        newsRepository.deleteByStockName(stockName);
        stockPredictionRepository.deleteByStockName(stockName);

        StockPrediction newPrediction = new StockPrediction(stockName, prediction);
        stockPredictionRepository.save(newPrediction);

        List<News> newsEntities = convertToEntity(newsList, stockName, newPrediction);
        newsRepository.saveAll(newsEntities);
    }

    // API URL 생성
    private String buildApiUrl(String query, int page) {
        int start = (page - 1) * 10 + 1; // 페이지 계산
        return "https://openapi.naver.com/v1/search/news.json?query=" + query +
                "&display=10&start=" + start + "&sort=sim";
    }

    // NewsDTO 생성, 반환
    private NewsDTO createNewsDTO(JSONObject item) {
        NewsDTO newsDTO = new NewsDTO();
        newsDTO.setTitle(item.getString("title").replaceAll("<.*?>", ""));
        newsDTO.setLink(item.getString("link"));
        newsDTO.setSummary(item.getString("description").replaceAll("<.*?>", ""));
        newsDTO.setPress(getPress(item.getString("originallink")));
        newsDTO.setDate(getDate(item.getString("pubDate")));
        newsDTO.setImageUrl(getImageURL(newsDTO.getLink()));
        return newsDTO;
    }

    // Entity -> DTO 변환(컨트롤러로 반환할 때)
    private List<NewsDTO> convertToDTO(List<News> newsList) {
        return newsList.stream().map(news -> {
            NewsDTO newsDTO = new NewsDTO();
            newsDTO.setTitle(news.getTitle());
            newsDTO.setLink(news.getLink());
            newsDTO.setSummary(news.getSummary());
            newsDTO.setPress(getPress(news.getLink()));
            newsDTO.setDate(news.getDate());
            newsDTO.setImageUrl(news.getImageUrl());
            return newsDTO;
        }).collect(Collectors.toList());
    }

    // DTO -> Entity 변환(DB에 저장해야할 때)
    private List<News> convertToEntity(List<NewsDTO> newsDTOList, String stockName, StockPrediction prediction) {
        return newsDTOList.stream().map(newsDTO ->{
            News news = new News();
            news.setStockName(stockName);
            news.setLastViewTime(LocalDateTime.now());
            news.setTitle(newsDTO.getTitle());
            news.setLink(newsDTO.getLink());
            news.setSummary(newsDTO.getSummary());
            news.setPress(newsDTO.getPress());
            news.setDate(newsDTO.getDate());
            news.setImageUrl(newsDTO.getImageUrl());
            news.setStockPrediction(prediction);
            return news;
        }).collect(Collectors.toList());
    }

    // 언론사 가져오기
    private String getPress(String link) {
        for (PressInfo info : SITE_LIST) {
            if (link.contains(info.getSite())) {
                return info.getPressName();
            }
        }
        try {
            URL url = new URL(link);
            String hostname = url.getHost();
            hostname = hostname.replaceAll("^(www|news)\\.", "")
                    .replaceAll("\\.(co|kr|com|net|org|jp)$", "");
            return hostname;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // 날짜 가져오기
    private String getDate(String pubDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = inputFormat.parse(pubDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubDate;
    }

    // 이미지 URL 가져오기
    private String getImageURL(String link) {
        try {
            Document doc=Jsoup.connect(link).get();
            Element img=doc.selectFirst("[property=og:image]");
            return img!=null?img.attr("content"):null;}catch(Exception e){return null;}
    }

    // 언론사 정보 리스트
    private static List<PressInfo> getPressList() {
        return Arrays.asList(
                new PressInfo("khan.co", "경향신문"),
                new PressInfo("naeil", "내일신문"),
                new PressInfo("nextdaily.co.kr", "넥스트데일리"),
                new PressInfo("biz.newdaily.co", "뉴데일리경제"),
                new PressInfo("newspim", "뉴스핌"),
                new PressInfo("news1", "뉴스1"),
                new PressInfo("tf.co", "더팩트"),
                new PressInfo("dnews.co.kr", "대한경제"),
                new PressInfo("ddaily.co.kr", "디지털데일리"),
                new PressInfo("dt.co.kr", "디지털타임스"),
                new PressInfo("digitaltoday.co.kr", "디지털 투데이"),
                new PressInfo("mk.co.kr", "매일경제"),
                new PressInfo("mt.co.kr", "머니투데이"),
                new PressInfo("byline.network", "바이라인네트워크"),
                new PressInfo("boannews.com", "보안뉴스"),
                new PressInfo("businesspost.co.kr", "비즈니스포스트"),
                new PressInfo("bizwatch.co.kr", "비즈워치"),
                new PressInfo("bizhankook.com", "비즈한국"),
                new PressInfo("sedaily.com", "서울경제"),
                new PressInfo("seoulfn.com", "서울파이낸스"),
                new PressInfo("meconomynews", "시장경제"),
                new PressInfo("asiae.co.kr", "아시아경제"),
                new PressInfo("asiatoday.co", "아시아투데이"),
                new PressInfo("ajunews.com", "아주경제"),
                new PressInfo("ekn.kr", "에너지경제"),
                new PressInfo("yna.co", "연합뉴스"),
                new PressInfo("enewstoday.co.kr", "이뉴스투데이"),
                new PressInfo("edaily.co.kr", "이데일리"),
                new PressInfo("etoday.co.kr", "이투데이"),
                new PressInfo("etnews.com", "전자신문"),
                new PressInfo("biz.chosun", "조선비즈"),
                new PressInfo("joseilbo.com", "조세일보"),
                new PressInfo("joongang.co", "중앙일보"),
                new PressInfo("zdnet.co.kr", "지디넷코리아"),
                new PressInfo("choicenews.co.kr", "초이스경제"),
                new PressInfo("fnnews.com", "파이낸셜뉴스"),
                new PressInfo("primeeconomy.co.kr", "프라임경제"),
                new PressInfo("hankyung.com", "한국경제"),
                new PressInfo("heraldcorp.com", "헤럴드경제"),
                new PressInfo("it.chosun", "IT조선"),
                new PressInfo("ytn.co", "YTN")
        );
    }
}
