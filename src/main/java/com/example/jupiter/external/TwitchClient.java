package com.example.jupiter.external;

import com.example.jupiter.entity.Game;
import com.example.jupiter.entity.Item;
import com.example.jupiter.entity.ItemType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {
    private static final String TOKEN = "2e00wa4qhy06qgbhzh44h0x3wmk9cw";
    private static final String CLIENT_ID = "4jhjvjk9u0rsxs0nqaj2ca7397w757";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    // Build the request URL which will be used when calling Twitch APIs,
    // e.g. https://api.twitch.tv/helix/games/top when trying to get top games.
    private String buildGameURL(String url, String gameName, int limit){
        if(gameName.equals("")){
            return String.format(url, limit);
        } else{
            try{
                //Encode special Characters in URL
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    private String buildSearchURL(String url, String gameId, int limit){
        try{
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }

    private String searchTwitch(String url) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //Define the response handler to parse and return HTTP response body returned from Twitch

        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if(responseCode != 200){
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            HttpEntity entity = response.getEntity();
            if(entity == null){
                throw new TwitchException("Failed to get result from Twitch API");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpClient.execute(request, responseHandler);
        } catch(IOException e){
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try{
                httpClient.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private List<Game> getGameList(String data) throws TwitchException{
        ObjectMapper mapper= new ObjectMapper();
        try{
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonMappingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    // Integrate search() and getGameList() together, return the top x popular games from Twitch.
    public List<Game> topGames(int limit) throws Exception {
        if(limit <= 0){
            limit = DEFAULT_GAME_LIMIT;
        }
        return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));
    }

    //Integrate search() and getGameList() together, returns the dedicated game based on the game name.

    public Game searchGame(String gameName) throws Exception {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if(gameList.size() != 0){
            return gameList.get(0);
        }
        return null;
    }
    // Similar to getGameList, convert the json data returned from Twitch to a list of Item objects.
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    // Returns the top x streams based on game ID.
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = null;
        try {
            streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }

    // Returns the top x clips based on game ID.
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = null;
        try {
            clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    // Returns the top x videos based on game ID.
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = null;
        try {
            videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }

}
