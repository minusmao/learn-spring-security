package com.example.security.filter;

import com.example.security.exception.KaptchaNotMatchException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//自定义验证码的 filter
public class KaptchaFilter extends UsernamePasswordAuthenticationFilter {

    private static final String FORM_KAPTCHA_KEY = "kaptcha";

    private String kaptchaParameter = FORM_KAPTCHA_KEY;

    public String getKaptchaParameter() {
        return kaptchaParameter;
    }

    public void setKaptchaParameter(String kaptchaParameter) {
        this.kaptchaParameter = kaptchaParameter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        //1.从请求中获取验证码
        String verifyCode = request.getParameter(getKaptchaParameter());
        //2.与 session 中验证码进行比较
        String sessionVerifyCode = (String) request.getSession().getAttribute("kaptcha");
        if (!ObjectUtils.isEmpty(verifyCode) && !ObjectUtils.isEmpty(sessionVerifyCode) &&
                verifyCode.equalsIgnoreCase(sessionVerifyCode)) {
            // 调用父类的用户密码验证
            return super.attemptAuthentication(request, response);
        }
        throw new KaptchaNotMatchException("验证码不匹配!");
    }
}
