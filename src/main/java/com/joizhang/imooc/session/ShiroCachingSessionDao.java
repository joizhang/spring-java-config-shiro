package com.joizhang.imooc.session;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author joizhang
 */
@RequiredArgsConstructor()
@Slf4j
public class ShiroCachingSessionDao extends CachingSessionDAO {

    private static final String REDIS_SHIRO_SESSION = "shiro-session:";

    private static final int SESSION_VAL_TIME_SPAN = 1800;

    /**
     * 保存到Redis中key的前缀 prefix+sessionId
     */
    @Setter
    private String redisShiroSessionPrefix = REDIS_SHIRO_SESSION;

    /**
     * 设置会话的过期时间
     */
    @Setter
    private int redisShiroSessionTimeout = SESSION_VAL_TIME_SPAN;

    @NonNull
    private RedisTemplate<String, Session> redisTemplate;

    /**
     * 重写CachingSessionDAO中readSession方法，如果Session中没有登陆信息就调用doReadSession方法从Redis中重读
     * session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null 代表没有登录，登录后Shiro会放入该值
     */
    @Override
    public Session readSession(final Serializable sessionId) {
        Session session = getCachedSession(sessionId);
        if (session == null || session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
            session = this.doReadSession(sessionId);
            if (session == null) {
                throw new UnknownSessionException("There is no session with id [" + sessionId + "]");
            } else {
                // 缓存
                cache(session, session.getId());
            }
        }
        return session;
    }

    /**
     * 根据session ID获取session 并redis中重置过期时间
     *
     * @param sessionId 会话ID
     * @return ShiroSession
     */
    @Override
    protected Session doReadSession(final Serializable sessionId) {
        log.debug("begin doReadSession {} ", sessionId);
        Session session = null;
        try {
            session = getSession(sessionId);
            if (session != null) {
                // 重置Redis中缓存过期时间
                refreshSession(sessionId);
                log.debug("sessionId {} name {} 被读取", sessionId, session.getClass().getName());
            }
        } catch (Exception e) {
            log.warn("读取Session失败", e);
        }
        return session;
    }

    /**
     * 从Redis中读取，但不重置Redis中缓存过期时间
     */
    public Session doReadSessionWithoutExpire(final Serializable sessionId) {
        Session session = null;
        try {
            session = getSession(sessionId);
        } catch (Exception e) {
            log.warn("读取Session失败", e);
        }
        return session;
    }

    /**
     * 如DefaultSessionManager在创建完session后会调用该方法；
     * 如保存到关系数据库/文件系统/NoSQL数据库；即可以实现会话的持久化；
     * 返回session ID；主要此处返回的ID.equals(session.getId())；
     */
    @Override
    protected Serializable doCreate(final Session session) {
        // 创建一个Id并设置给Session
        Serializable sessionId = this.generateSessionId(session);
        assignSessionId(session, sessionId);
        try {
            saveSession(session);
            log.info("sessionId {} name {} 被创建", sessionId, session.getClass().getName());
        } catch (Exception e) {
            log.error("创建Session失败", e);
        }
        return sessionId;
    }

    /**
     * 更新会话；如更新会话最后访问时间/停止会话/设置超时时间/设置移除属性等会调用
     */
    @Override
    protected void doUpdate(final Session session) {
        //如果会话过期/停止 没必要再更新了
        try {
            if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
                return;
            }
        } catch (Exception e) {
            log.error("ValidatingSession error");
        }
        try {
            if (session instanceof ShiroSession) {
                // 如果没有主要字段(除lastAccessTime以外其他字段)发生改变
                ShiroSession ss = (ShiroSession) session;
                if (!ss.isChanged()) {
                    return;
                }
                ss.setChanged(false);
                ss.setLastAccessTime(new Date());

                updateSession(session);

                log.debug("sessionId {} name {} 被更新", session.getId(), session.getClass().getName());
            } else {
                log.error("sessionId {} name {} 更新失败", session.getId(), session.getClass().getName());
            }
        } catch (Exception e) {
            log.error("更新Session失败", e);
        }
    }


    @Override
    public void update(Session session) {
        this.doUpdate(session);
    }

    /**
     * 删除会话；当会话过期/会话停止（如用户退出时）会调用
     */
    @Override
    public void doDelete(final Session session) {
        log.debug("begin doDelete {} ", session);
        try {
            deleteSession(session.getId());
            this.deleteCache(session.getId());
            log.debug("shiro session id {} 被删除", session.getId());
        } catch (Exception e) {
            log.error("删除Session失败", e);
        }
    }

    /**
     * 删除cache中缓存的Session
     */
    public void deleteCache(final Serializable sessionId) {
        try {
            Session session = super.getCachedSession(sessionId);
            super.uncache(session);
            log.debug("本地 cache中缓存的Session id {} 失效", sessionId);
        } catch (Exception e) {
            log.error("删除本地 cache中缓存的Session 失败", e);
        }
    }


    /**
     * 返回本机Ehcache中Session
     */
    public Collection<Session> getEhCacheActiveSessions() {
        return super.getActiveSessions();
    }

    /**
     * 保存session
     */
    private void saveSession(final Session session) {
        try {
            redisTemplate.opsForValue()
                    .set(buildRedisSessionKey(session.getId()), session, redisShiroSessionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("save session to redis error");
        }
    }

    /**
     * 更新session
     */
    private void updateSession(final Session session) {
        try {
            redisTemplate.boundValueOps(buildRedisSessionKey(session.getId()))
                    .set(session, redisShiroSessionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("update session error");
        }
    }


    /**
     * 刷新session
     */
    private void refreshSession(final Serializable sessionId) {
        redisTemplate.expire(buildRedisSessionKey(sessionId), redisShiroSessionTimeout, TimeUnit.SECONDS);
    }


    /**
     * 删除session
     */
    private void deleteSession(final Serializable id) {
        try {
            redisTemplate.delete(buildRedisSessionKey(id));
        } catch (Exception e) {
            log.error("delete session error");
        }
    }


    /**
     * 获取session
     */
    private Session getSession(final Serializable id) {
        Session session = null;
        try {
            session = redisTemplate.boundValueOps(buildRedisSessionKey(id)).get();
        } catch (Exception e) {
            log.info("get session error");
        }
        return session;
    }

    /**
     * 通过sessionId获取sessionKey
     */
    private String buildRedisSessionKey(final Serializable sessionId) {
        return redisShiroSessionPrefix + sessionId;
    }
}
