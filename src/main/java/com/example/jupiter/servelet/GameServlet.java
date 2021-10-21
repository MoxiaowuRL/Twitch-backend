package com.example.jupiter.servelet;

import com.example.jupiter.entity.Game;
import com.example.jupiter.external.TwitchClient;
import com.example.jupiter.external.TwitchException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "GameServlet", value = "/game")
public class GameServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        // Let the client know the returned data is in JSON format
        response.setContentType("application/json;charset=UTF-8");
        try{
            //Return the dedicated game information if gameName is provided in the request URL, otherwise return the top x games.
            if(gameName != null){
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            } else{
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (Exception e){
            throw new ServletException(e);
        }
    }

}
