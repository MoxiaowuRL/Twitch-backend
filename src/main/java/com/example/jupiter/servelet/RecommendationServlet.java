package com.example.jupiter.servelet;

import com.example.jupiter.entity.Item;
import com.example.jupiter.recommendation.ItemRecommender;
import com.example.jupiter.recommendation.RecommendationException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RecommendationServlet", value = "/recommendation")
public class RecommendationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        ItemRecommender itemRecommender = new ItemRecommender();
        Map<String, List<Item>> itemMap;
        try{
            if(session == null){
                itemMap = itemRecommender.recommendItemsByDefault();
            } else{
                String userId = (String) request.getSession().getAttribute("user_id");
                itemMap = itemRecommender.recommendItemsByUser(userId);
            }
        } catch (RecommendationException e){
            throw new ServletException();
        }
        ServletUtil.writeItemMap(response, itemMap);
    }
}
