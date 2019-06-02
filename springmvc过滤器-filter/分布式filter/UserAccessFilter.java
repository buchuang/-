package com.vr.vrfilterclient.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vr.commonutils.exception.ThisException;
import com.vr.commonutils.utils.ErrorEnum;
import com.vr.commonutils.utils.JsonUtil2;
import com.vr.commonutils.utils.R;
import com.vr.userserviceapi.entity.UserInfoDto;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public abstract class UserAccessFilter implements Filter {

    private static Cache<String, UserInfoDto> cache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(3, TimeUnit.MINUTES).build();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = null;
        token = getToken(request, token);
        if (StringUtils.isBlank(token)) {
            rander(response);
        }
        UserInfoDto userInfoDto = cache.getIfPresent(token);
        if (userInfoDto == null) {
            userInfoDto = getUserInfoFromAuthentication(token);
            if (userInfoDto == null) {
                rander(response);
            } else {
                cache.put("token", userInfoDto);
            }
        }
        flushLoginTime(token);
        login(request, response, userInfoDto);
        filterChain.doFilter(request, response);
    }

    private  void flushLoginTime(String token){
        String url = "http://" + getServiceNam() + "/flushLoginTime";
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("token", token);
        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                ThisException.exception(ErrorEnum.REQUEST_FAILED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract String getServiceNam();

    public abstract void login(HttpServletRequest request, HttpServletResponse response, UserInfoDto userInfoDto);

    private UserInfoDto getUserInfoFromAuthentication(String token) {
        String url = "http://" + getServiceNam() + "/authentication";
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("token", token);
        InputStream inputStream = null;
        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                ThisException.exception(ErrorEnum.REQUEST_FAILED);
            }
            inputStream = response.getEntity().getContent();
            byte[] bytes = new byte[1024];
            int len = 0;
            StringBuilder builder = new StringBuilder();
            while ((len = inputStream.read(bytes)) > 0) {
                builder.append(new String(bytes, 0, len));
            }
            if(StringUtils.isBlank(builder.toString())){
                return null;
            }
            UserInfoDto o = (UserInfoDto) JsonUtil2.fromJson(builder.toString(), UserInfoDto.class);
            return o;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private void rander(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            String json = JsonUtil2.toJson(R.error(ErrorEnum.TOKEN_VAILD));
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getTokenFromCookie(Cookie[] cookies, String token) {
        if (!StringUtils.isBlank(token)) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if ("token".equals(name)) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    private String getToken(HttpServletRequest request, String token) {
        token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
            if (StringUtils.isBlank(token)) {
                Cookie[] cookies = request.getCookies();
                token = getTokenFromCookie(cookies, token);
            }
        }
        return token;
    }

    @Override
    public void destroy() {

    }
}
