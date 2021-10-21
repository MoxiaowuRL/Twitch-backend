package com.example.jupiter.servelet;

import com.example.jupiter.db.MySQLConnection;
import com.example.jupiter.db.MySQLException;
import com.example.jupiter.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Read User data from the request body
        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(request.getReader(), User.class);
        if(user == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        boolean isUserAdded = false;
        MySQLConnection connection = null;
        try{
            //Add the new user to the database
            connection = new MySQLConnection();
            user.setPassword(ServletUtil.encryptPassword(user.getUserId(), user.getPassword()));
            isUserAdded = connection.addUser(user);
        } catch (MySQLException e){
            throw new ServletException(e);
        } finally {
            connection.close();
        }
        if(!isUserAdded){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
