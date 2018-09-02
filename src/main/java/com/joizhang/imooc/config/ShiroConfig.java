package com.joizhang.imooc.config;

import com.joizhang.imooc.realm.ShiroRealm;
import com.joizhang.imooc.session.*;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.HashMap;

import static org.apache.shiro.codec.Base64.decode;

/**
 * @author joizhang
 */
@Configuration
public class ShiroConfig {

    private final RedisTemplate redisTemplate;

    @Autowired
    public ShiroConfig(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public DefaultWebSecurityManager webSecurityManager() {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(getRealm());
        //defaultWebSecurityManager.setRememberMeManager(rememberMeManager());
        defaultWebSecurityManager.setSessionManager(sessionManager());
        return defaultWebSecurityManager;
    }

    @Bean
    public Realm getRealm() {
        return new ShiroRealm();
    }

//    @Bean
//    public CookieRememberMeManager rememberMeManager() {
//        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
//        rememberMeManager.setCookie(rememberMeCookie());
//        rememberMeManager.setCipherKey(decode("5AvVhmFLUs0KTA3Kprsdag=="));
//        return rememberMeManager;
//    }

//    @Bean
//    public SimpleCookie rememberMeCookie() {
//        SimpleCookie cookie = new SimpleCookie("rememberMe");
//        cookie.setHttpOnly(true);
//        cookie.setMaxAge(2592000);
//        return cookie;
//    }

    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(1800);
        sessionManager.setDeleteInvalidSessions(false);
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionValidationInterval(1800);
        sessionManager.setSessionFactory(sessionFactory());
        sessionManager.setSessionDAO(shiroCachingSessionDao());
        sessionManager.setSessionIdCookie(new SimpleCookie("SHRIOSESSIONID"));
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionListeners(Collections.singletonList(shiroSessionListener()));
        return sessionManager;
    }

    @Bean
    public SessionFactory sessionFactory() {
        return new ShiroSessionFactory();
    }

    @Bean
    public ShiroCachingSessionDao shiroCachingSessionDao() {
        return new ShiroCachingSessionDao(redisTemplate);
    }

//    @Bean
//    public ShiroSessionRepository shiroSessionRepository() {
//        ShiroSessionRepository shiroSessionRepository = new ShiroSessionRepository();
//        assert redisTemplate != null;
//        shiroSessionRepository.setRedisTemplate(redisTemplate);
//        return shiroSessionRepository;
//    }

    @Bean
    public ShiroSessionListener shiroSessionListener() {
        ShiroSessionListener shiroSessionListener = new ShiroSessionListener();
        shiroSessionListener.setSessionDao(shiroCachingSessionDao());
        shiroSessionListener.setShiroSessionService(shiroSessionService());
        return shiroSessionListener;
    }

    @Bean
    public ShiroSessionService shiroSessionService() {
        ShiroSessionService shiroSessionService = new ShiroSessionService();
        shiroSessionService.setRedisTemplate(redisTemplate);
        shiroSessionService.setSessionDao(shiroCachingSessionDao());
        return shiroSessionService;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(webSecurityManager());
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/");
        shiroFilterFactoryBean.setUnauthorizedUrl("/login");

        HashMap<String, String> map = new HashMap<>(16);
        map.put("/favicon.ico", "anon");
        map.put("/hello", "anon");
        map.put("/login", "anon");
        map.put("/assets/**", "anon");
        map.put("/404", "anon");
        map.put("/403", "anon");
        map.put("/500", "anon");

        //需要登陆
        map.put("/**", "authc");
        //map.put("/**", "user");//通过记住我登陆
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);
        return shiroFilterFactoryBean;
    }

}
