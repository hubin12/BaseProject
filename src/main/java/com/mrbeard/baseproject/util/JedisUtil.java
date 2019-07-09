package com.mrbeard.baseproject.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * JedisUtil.java
 *
 * @author mrbeard
 * @version 创建时间：2018-10-25
 */
@Configuration
@EnableCaching
public class JedisUtil {
    static Logger logger = LoggerFactory.getLogger(JedisUtil.class);

    private static JedisPool jedisPool;

    private static int errorflag = 0;
    private static long errortime = 0;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

    @Bean
    public JedisPool redisPoolFactory() {
        logger.info("{JedisPool}====>JedisPool注入成功！！");
        logger.info("{JedisPool}====>redis地址：" + host + ":" + port);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        if (password != null && !"".equals(password)) {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password, 0);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, null, 0);
        }
        return jedisPool;
    }

    /**
     * get jedis from pool
     *
     * @return
     */
    public static Jedis getJedis() {
        return getJedis(0);
    }

    public static Jedis getJedis(String host, Integer port) {
        Jedis jedis = new Jedis(host, port);
        jedis.connect();
        if (jedis.isConnected()) {
            return jedis;
        }
        return null;
    }

    public static Jedis getJedis(String host, Integer port, String password) {
        try {
            Jedis jedis = new Jedis(host, port);
            jedis.auth(password);
            jedis.connect();
            if (jedis.isConnected()) {
                return jedis;
            }
        } catch (Exception e) {
            if (e instanceof JedisConnectionException) {
                logger.error("get jedis JedisConnectionException : "
                        + e.getMessage());
            }
        }
        return null;
    }

    public static Jedis getJedis(String host, Integer port, String password,
                                 int database) {
        try {
            Jedis jedis = new Jedis(host, port);
            jedis.auth(password);
            jedis.select(database);
            jedis.connect();
            if (jedis.isConnected()) {
                return jedis;
            }
        } catch (Exception e) {
            if (e instanceof JedisConnectionException) {
                logger.error("get jedis JedisConnectionException : "
                        + e.getMessage());
            }
        }
        return null;
    }

    public static Jedis getJedis(int failedNum) {
        Jedis jedis = null;
        if (failedNum < 2) {
            try {
                jedis = jedisPool.getResource();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("get jedis error : " + e.getMessage());
                if (jedis != null) {
                    jedis.close();
                }
                try {
                    failedNum++;
                    Thread.sleep(500 * failedNum);
                    jedis = getJedis(failedNum);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return jedis;
    }

    /**
     * this method will be block until return Jedis client
     *
     * @return
     */
    public static Jedis bgetJedis() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get jedis error : " + e.getMessage());
            if (jedis != null) {
                jedis.close();
            }
            try {
                Thread.sleep(500);
                jedis = bgetJedis();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        return jedis;
    }

    /**
     * return jedis to pool
     *
     * @param jedis
     */
    public static void returnJedis(Jedis jedis) {
        try {
            jedis.close();
        } catch (JedisConnectionException e) {
            returnBrokenJedis(jedis);
            //jedisPool.returnBrokenResource(jedis);
        } catch (Exception e) {
            jedis = null;
        }
    }

    public static void returnJedis(JedisPool theJedisPool, Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (JedisConnectionException e) {
            returnBrokenJedis(jedis);
        } catch (Exception e) {
            jedis.quit();
        }
    }

    /**
     * return broken jedis to pool
     *
     * @param jedis
     */
    public static void returnBrokenJedis(Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            jedis.quit();
        }
    }

    public static String get(String key) {
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.get(key);
        } catch (Exception e) {
            logger.error("get value from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return value;
    }

    /**
     * this method will be block, until timeout
     *
     * @param key
     * @param timeout(millisecond)
     * @return
     */
    public String get(String key, long timeout) {
        Jedis jedis = getJedis();
        String value = null;
        long t1 = System.currentTimeMillis();
        try {
            while (true) {
                value = jedis.get(key);
                if (value != null && !"".equals(value)) {
                    break;
                }
                if (System.currentTimeMillis() - t1 > timeout) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            logger.error("get value from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return value;
    }

    public byte[] get(byte[] key) {
        Jedis jedis = getJedis();
        byte[] value = null;
        try {
            value = jedis.get(key);
        } catch (Exception e) {
            try {
                logger.error("get value from redis error[key:" + new String(key, "utf-8") + "]", e);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return value;
    }

    /**
     * this method will be block, until timeout
     *
     * @param key
     * @param timeout(millisecond)
     * @return
     */
    public byte[] get(byte[] key, long timeout) {
        Jedis jedis = getJedis();
        byte[] value = null;
        long t1 = System.currentTimeMillis();
        try {
            while (true) {
                value = jedis.get(key);
                if (value != null) {
                    break;
                }
                if (System.currentTimeMillis() - t1 > timeout) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            try {
                logger.error("get value from redis error[key:" + new String(key, "utf-8") + "]", e);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return value;
    }

    public void add(String key, String value) {
        add(key, value, 0, 0);
    }

    /**
     * @param key
     * @param value
     * @param expireTime seconds
     */
    public static void add(String key, String value, int expireTime) {
        add(key, value, expireTime, 0);
    }

    public static void add(String key, String value, int expireTime,
                           int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.set(key, value);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            } catch (Exception e) {
                logger.error("add key[" + key + "] to redis error[" + failedNum
                        + "] ", e);
                returnBrokenJedis(jedis);
                add(key, value, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    public void add(byte[] key, byte[] value) {
        add(key, value, 0, 0);
    }

    public void add(byte[] key, byte[] value, int expireTime) {
        add(key, value, expireTime, 0);
    }

    public void add(byte[] key, byte[] value, int expireTime,
                    int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.set(key, value);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            } catch (Exception e) {
                try {
                    logger.error("add key[" + new String(key, "utf-8") + "] to redis error[" + failedNum
                            + "] ", e);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                returnBrokenJedis(jedis);
                add(key, value, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    /**
     * publish message to special channel
     *
     * @param channel
     * @param message
     */
    public void publish(String channel, String message) {
        publish(channel, message, 0);
    }

    public void publish(String channel, String message, int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.publish(channel, message);
            } catch (Exception e) {
                logger.error("publish message[" + message + "] to channel["
                        + channel + "] error[" + failedNum + "] : "
                        + e.getMessage());
                e.printStackTrace();
                returnBrokenJedis(jedis);
                publish(channel, message, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    /**
     * publish message to special channel and queue
     *
     * @param channel
     * @param message
     */
    public void queuePublish(String key, String channel, String message) {
        queuePublish(key, channel, message, 0);
    }

    public void queuePublish(String key, String channel, String message,
                             int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.lpush(key, message);
                jedis.publish(channel, message);
            } catch (Exception e) {
                logger.error("queuePublish message[" + message
                        + "] to channel[" + channel + "] error[" + failedNum
                        + "] : " + e.getMessage());
                e.printStackTrace();
                returnBrokenJedis(jedis);
                queuePublish(key, channel, message, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    /**
     * subscribe special channel
     *
     * @param listener
     * @param channel
     */
    public void subscribe(JedisPubSub listener, String channel) {
        subscribe(listener, channel, 0);
    }

    public void subscribe(JedisPubSub listener, String channel,
                          int failedNum) {
        Jedis jedis = getJedis();
        try {
            jedis.subscribe(listener, channel);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
            failedNum++;
            logger.error("subscribe error ! channel[" + channel
                    + "] failedNum[" + failedNum + "]");
            if (failedNum < 11) {
                try {
                    Thread.sleep(1000 * failedNum);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                subscribe(listener, channel, failedNum);
            }
        }
    }

    public void listAdd(String key, String... value) {
        Jedis jedis = getJedis();
        try {
            jedis.lpush(key, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public String listPop(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.lpop(key);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public List<String> listAll(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.lrange(key, 0, -1);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public List<String> listPopAll(String key) {
        Jedis jedis = getJedis();
        try {
            long len = jedis.llen(key);
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < len; i++) {
                list.add(jedis.lpop(key));
            }
            return list;
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    /**
     * delete special value
     *
     * @param key
     * @param count delete numbers
     * @param value
     */
    public long listDel(String key, int count, String value) {
        Jedis jedis = getJedis();
        try {
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return 0;
    }

    public void listDelAll(String key) {
        Jedis jedis = getJedis();
        try {
            long len = jedis.llen(key);
            for (int i = 0; i < len; i++) {
                jedis.rpop(key);
            }
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * @param key
     * @param value
     * @return 1:add success	0:value is existed 	other:key is not a set type
     */
    public long setAdd(String key, String... value) {
        Jedis jedis = getJedis();
        try {
            return jedis.sadd(key, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return 0;
    }

    /**
     * @param key
     * @param value
     * @return 1:add success	0:value is existed 	other:key is not a set type
     */
    public long setDel(String key, String... value) {
        Jedis jedis = getJedis();
        try {
            return jedis.srem(key, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return 0;
    }

    public void setDelAll(String key) {
        Jedis jedis = getJedis();
        try {
            long total = jedis.scard(key);
            for (int i = 0; i < total; i++) {
                jedis.spop(key);
            }
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public long setCount(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.scard(key);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return 0;
    }

    public Set<String> setAll(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.smembers(key);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public void expire(String key, int seconds) {
        Jedis jedis = getJedis();
        try {
            jedis.expire(key, seconds);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public void setJedisPool(JedisPool jedisPool) {
        JedisUtil.jedisPool = jedisPool;
    }


    /**
     * 追加数据到已有的缓存数据 <br>
     * （基于保存Key,Map数据队列）
     *
     * @return
     */
    public boolean addObjectInMap(String rkey, String mkey, Object value) {
        Jedis jedis = getJedis();

        try {
            jedis.hset(rkey, mkey, (String) value);
        } catch (Exception e) {
        } finally {
            JedisUtil.returnJedis(jedis);
        }
        return true;
    }

    /**
     * 从已有的缓存数据里面删除一个数据 <br>
     * （基于保存Key,Map数据队列）
     *
     * @return
     */
    public Object removeObjectInMap(String rkey, String mkey) {
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.hget(rkey, mkey);
            jedis.hdel(rkey, mkey);
        } catch (Exception e) {
        } finally {
            JedisUtil.returnJedis(jedis);
        }
        return value;
    }

    /**
     * 从已有的缓存数据里面获取一个数据 <br>
     * （基于保存Key,Map数据队列）
     *
     * @return
     */
    public Object findObjectInMap(String rkey, String mkey) {
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.hget(rkey, mkey);
        } catch (Exception e) {
        } finally {
            JedisUtil.returnJedis(jedis);
        }
        return value;
    }

    /**
     * @param key
     * @Description: redis  k-v 删除
     * @Author: hurd
     * @Date: 2015年7月31日 下午2:03:55
     */
    public static void remove(String key) {
        Jedis jedis = getJedis();
        try {
            jedis.del(key);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }

    }

    public Set<String> hkeys(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.hkeys(key);
        } catch (Exception e) {
            logger.error("hkeys keys from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
            return null;
        } finally {
            returnJedis(jedis);
        }
    }

    public Set<String> keys(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.keys(key);
        } catch (Exception e) {
            logger.error("keys from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
            return null;
        } finally {
            returnJedis(jedis);
        }
    }

    public boolean exists(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            return false;
        } finally {
            returnJedis(jedis);
        }
    }

    public void hmset(String key, Map<String, String> hash) {
        hmset(key, hash, 0, 0);
    }

    public void hmset(String key, Map<String, String> hash, int expireTime) {
        hmset(key, hash, expireTime, 0);
    }

    public void hmset(String key, Map<String, String> hash, int expireTime, int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.hmset(key, hash);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            } catch (Exception e) {
                logger.error("hmset key[" + key + "] to redis error[" + failedNum + "] ", e);
                returnBrokenJedis(jedis);
                hmset(key, hash, expireTime, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    public List<String> hmget(String key, long timeout, String... field) {
        Jedis jedis = getJedis();
        List<String> value = null;
        long t1 = System.currentTimeMillis();
        try {
            while (true) {
                value = jedis.hmget(key, field);
                if (value != null && value.size() > 0) {
                    break;
                }
                if (System.currentTimeMillis() - t1 > timeout) {
                    break;
                }
                Thread.sleep(1);
            }
        } catch (Exception e) {
            logger.error("hmget [field:" + "] value from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return value;
    }

    public Map<String, String> hgetAll(String key, long timeout) {
        Jedis jedis = getJedis();
        Map<String, String> result = null;
        long t1 = System.currentTimeMillis();
        try {
            while (true) {
                result = jedis.hgetAll(key);
                if (result != null && !result.isEmpty()) {
                    break;
                }
                if (System.currentTimeMillis() - t1 > timeout) {
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("hgetAll [key:" + key + "] value from redis error ", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    public String hget(String key, String field, long timeout) {
        Jedis jedis = getJedis();
        String result = null;
        long t1 = System.currentTimeMillis();
        try {
            while (true) {
                result = jedis.hget(key, field);
                if (result != null && !result.isEmpty()) {
                    break;
                }
                if (System.currentTimeMillis() - t1 > timeout) {
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("hgetAll [key:" + key + "] value from redis error ", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    public boolean hExist(String key, String field) {
        Jedis jedis = getJedis();
        try {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            logger.error("hexist [field:" + field.toString() + "] value from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return false;
    }

    public void hset(String key, String field, String value) {
        Jedis jedis = getJedis();
        try {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * @param key
     * @param field
     * @Description: 从hash表删除filed
     * @Author: gongfp@hundsun.com
     * @Date: 2015年11月12日 下午3:02:56
     */
    public void hdel(String key, String field) {
        Jedis jedis = getJedis();
        try {
            jedis.hdel(key, field);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * redis 排序
     *
     * @param key
     * @return
     */
    public List<String> sort(String key) {
        Jedis jedis = getJedis();
        List<String> sortList = new ArrayList<String>();
        try {
            sortList = jedis.sort(key);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return sortList;
    }

    /**
     * 有序set添加
     *
     * @param key
     * @param score
     * @param member
     */
    public void zadd(String key, double score, String member) {
        Jedis jedis = getJedis();
        try {
            jedis.zadd(key, score, member);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 有序正向set集合
     *
     * @param key
     * @return
     */
    public Set<String> setSortForward(String key) {
        Jedis jedis = getJedis();
        Set<String> setRecord = new HashSet<String>();
        try {
            setRecord = jedis.zrange(key, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return setRecord;
    }

    /**
     * 有序反向set集合
     *
     * @param key
     * @return
     */
    public Set<String> setSortBackward(String key) {
        Jedis jedis = getJedis();
        Set<String> setRecord = new HashSet<String>();
        try {
            setRecord = jedis.zrevrange(key, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return setRecord;
    }

    /**
     * 有序set删除
     *
     * @param key
     * @param member
     */
    public void zdel(String key, String member) {
        Jedis jedis = getJedis();
        try {
            jedis.zrem(key, member);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * set集合 index值（0，1，2，……）
     *
     * @param key
     * @return
     */
    public Long indexSet(String key, String member) {
        Jedis jedis = getJedis();
        Long index = null;
        try {
            index = jedis.zrank(key, member);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return index;
    }

    /**
     * set集合 第N的值（0，1，2，……）
     *
     * @param key
     * @return
     */
    public Set<String> getIndexSet(String key, Long endIndex) {
        Jedis jedis = getJedis();
        Set<String> set = new HashSet<String>();
        try {
            set = jedis.zrange(key, 0, endIndex);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return set;
    }

    /**
     * set 获取分数满足表达式startScore < score <= endScore的成员
     *
     * @param key
     * @param startScore
     * @param endScore
     * @return
     */
    public Set<String> getByScoreSet(String key, Long startScore, Long endScore) {
        Jedis jedis = getJedis();
        Set<String> set = new HashSet<String>();
        try {
            set = jedis.zrangeByScore(key, startScore, endScore);
        } catch (Exception e) {
            e.printStackTrace();
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }
        return set;
    }

    public void rpush(String key, String value) {
        Jedis jedis = getJedis();
        try {
            jedis.rpush(key, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public long llen(String key) {
        Jedis jedis = getJedis();
        long value = 0;
        try {
            value = jedis.llen(key);
        } catch (Exception e) {
            logger.error("get value from redis error[key:" + key + "]", e);
            returnBrokenJedis(jedis);
        } finally {
            returnJedis(jedis);
        }

        return value;
    }

    public void listAddAfterLeft(String key, String... value) {
        Jedis jedis = getJedis();
        try {
            jedis.lpush(key, value);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public String blistPop(String key, int expireTime) {
        Jedis jedis = getJedis();
        try {
            List<String> list = jedis.blpop(expireTime, key);
            if (list != null && list.size() == 2) {
                return list.get(1);
            }
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public void addExpireTime(String key, String value, int expireTime) {
        addExpire(key, value, expireTime, 0);
    }

    public void addExpire(String key, String value, int expireTime, int failedNum) {
        if (failedNum < 3) {
            Jedis jedis = getJedis();
            try {
                jedis.set(key, value);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            } catch (Exception e) {
                logger.error("add key[" + key + "] to redis error[" + failedNum + "] ", e);
                returnBrokenJedis(jedis);
                add(key, value, expireTime, ++failedNum);
            } finally {
                returnJedis(jedis);
            }
        }
    }

    /**
     * 获取map的key对应的value
     *
     * @param key
     * @return
     */
    public List<String> getAllMapValue(String key) {
        Jedis jedis = getJedis();
        try {
            List<String> list = jedis.hvals(key);
            if (null != list) {
                return list;
            } else {
                return null;
            }
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    /**
     * 设置对象
     *
     * @param key
     * @param obj
     */
    public void setObject(String key, Object obj) {
        Jedis jedis = getJedis();
        try {
            obj = obj == null ? new Object() : obj;
            jedis.set(key, JSON.toJSONString(obj));
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获取对象
     *
     * @param key
     * @return Object
     */
    public Object getObject(String key) {
        Jedis jedis = getJedis();
        try {
            if (jedis == null || !jedis.exists(key)) {
                return null;
            }
            String data = jedis.get(key);
            return JSON.parseObject(data);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    /**
     * 设置对象有效期
     *
     * @param key
     * @param obj
     */
    public void setObject(String key, int seconds, Object obj) {
        Jedis jedis = getJedis();
        try {
            obj = obj == null ? new Object() : obj;
            jedis.set(key, JSON.toJSONString(obj));
            jedis.setex(key, seconds, JSON.toJSONString(obj));
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 设置List集合
     *
     * @param key
     * @param list
     */
    public void setList(String key, List<?> list) {
        Jedis jedis = getJedis();
        try {
            if (list != null) {
                jedis.set(key, JSONArray.toJSONString(list));
            } else {//如果list为空,则设置一个空
                jedis.set(key.getBytes("utf-8"), "".getBytes("utf-8"));
            }
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获取List集合
     *
     * @param key
     * @return
     */
    public List<?> getList(String key) {
        Jedis jedis = getJedis();
        try {
            if (jedis == null || !jedis.exists(key)) {
                return null;
            }
            byte[] data = jedis.get(key.getBytes("utf-8"));
            return (List<?>) JSONArray.toJSON(data);
        } catch (Exception e) {
            returnBrokenJedis(jedis);
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

}
