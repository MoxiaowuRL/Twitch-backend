package com.example.jupiter.servelet;

import com.example.jupiter.entity.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServletUtil {
    public static void writeItemMap(HttpServletResponse response, Map<String, List<Item>> itemMap) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));
    }

    public static String encryptPassword(String userId, String password) throws IOException{
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password).toLowerCase());
    }
}
