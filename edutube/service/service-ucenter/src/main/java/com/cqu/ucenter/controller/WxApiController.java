package com.cqu.ucenter.controller;

import com.cqu.commonutils.JwtUtils;
import com.cqu.servicebase.exceptionhandler.MyException;
import com.cqu.ucenter.entity.Member;
import com.cqu.ucenter.service.MemberService;
import com.cqu.ucenter.utils.ConstantWxUtils;
import com.cqu.ucenter.utils.HttpClientUtils;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * @author CGT
 * @create 2021-07-15 10:22
 */
@Controller
@CrossOrigin
@RequestMapping("/api/ucenter/wx")
public class WxApiController {
    @Autowired
    private MemberService memberService;
    @ApiOperation(value = "获取回调")
    @GetMapping("callback")
    public String callback(String code, String state, HttpSession httpSession){
        System.out.println(code);
        System.out.println(state);
        String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                        "?appid=%s" +
                        "&secret=%s" +
                        "&code=%s" +
                        "&grant_type=authorization_code";
        String accessTokenUrl = String.format(baseAccessTokenUrl,
                ConstantWxUtils.WX_OPEN_APP_ID,
                ConstantWxUtils.WX_OPEN_APP_SECRET,
                code);
        String result=null;
        try {
            result=HttpClientUtils.get(accessTokenUrl);
            System.out.println(result);
            Gson gson = new Gson();
            HashMap map = gson.fromJson(result, HashMap.class);
            String accessToken = (String)map.get("access_token");
            String openid = (String)map.get("openid");
            System.out.println(openid);
            //访问微信的资源服务器，获取用户信息
            String resultUserInfo = null;
                //向数据库中插入一条记录
                Member member = memberService.getByOpenId(openid);
                if(member==null) {
                    System.out.println("新用户注册");
                    String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" + "?access_token=%s" + "&openid=%s";
                    String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openid);
                    resultUserInfo = HttpClientUtils.get(userInfoUrl);
                    //解析json
                    HashMap<String, Object> mapUserInfo = gson.fromJson(resultUserInfo,HashMap.class);
                    String nickname = (String)mapUserInfo.get("nickname");
                    String headimgurl = (String)mapUserInfo.get("headimgurl");
                    member = new Member();
                    member.setNickname(nickname);
                    member.setOpenid(openid);
                    member.setAvatar(headimgurl);
                    memberService.save(member);
                }
                String jwtToken=JwtUtils.getJwtToken(member.getId(),member.getNickname());

                return "redirect:http://localhost:3000?token="+jwtToken;
            } catch (Exception e) {
                e.printStackTrace();
                throw new MyException(20001,"Login Error");
            }
    }
    @ApiOperation(value = "生成二维码")
    @GetMapping("login")
    public String getWxCode(){
        String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=%s" +
                "&redirect_uri=%s" +
                "&response_type=code" +
                "&scope=snsapi_login" +
                "&state=%s" +
                "#wechat_redirect";
        String redirctUrl=ConstantWxUtils.WX_OPEN_REDIRECT_URL;
        try {
            redirctUrl = URLEncoder.encode(redirctUrl, "utf-8");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        String url=String.format(
                baseUrl,
                ConstantWxUtils.WX_OPEN_APP_ID,
                redirctUrl,
                "EduTube"
        );
        return "redirect:"+url ;
    }
}
