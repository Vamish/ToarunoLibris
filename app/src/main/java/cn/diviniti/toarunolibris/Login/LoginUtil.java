package cn.diviniti.toarunolibris.Login;

import org.apache.http.HttpHeaders;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginUtil {
    private String userNum;
    private String userPwd;

    public LoginUtil(String userNum, String userPwd) {
        this.userNum = userNum;
        this.userPwd = userPwd;
    }

    public String getSession() throws SocketTimeoutException, IOException {
        final String url = "http://libinfo.jmu.edu.cn/cuser/";

        final Map<String, String> params = new LinkedHashMap<>();
        params.put("__VIEWSTATE", "/wEPDwULLTE3OTEyNjY3NjEPZBYCAgMPZBYCAgsPDxYGHgRUZXh0BRvnlKjmiLflkI3miJblr4bnoIHplJnor6/vvIEeCUZvcmVDb2xvcgqNAR4EXyFTQgIEZGRkei/L2q/Q2ShlyWBAMbzKVTaXfpgK3HpQkyBl5XSsMSc=");
        params.put("__EVENTVALIDATION", "/wEWBAKH14rCAQLcgpeMBwLGmdGVDAKM54rGBmcmiPP8UWvSgW6e+go9TWFHdtUYfvFJZ9Z1K+fsNwrE");
        //TODO 别傻了，这边得填他们的！
        params.put("user", userNum);
        params.put("pwd", userPwd);
        params.put("Button1", "登录");
        Connection.Response response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .header(HttpHeaders.CONTENT_TYPE, "x-www-form-urlencoded")
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                .data(params)
                .timeout(1000 * 30)
                .execute();

        String iPlanetDirectoryPro = response.cookie("iPlanetDirectoryPro");

        return iPlanetDirectoryPro;
    }

    private void getUserDetailInfo() {

    }
}
