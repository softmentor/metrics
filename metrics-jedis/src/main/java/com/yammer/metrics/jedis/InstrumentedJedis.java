package com.yammer.metrics.jedis;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Slowlog;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

/**
 * 
 * @author softmentor
 * 
 */
public class InstrumentedJedis extends Jedis implements IJedisProxy {

	private InstrumentedJedisPool jedisPool = null;

	/**
	 * @return the jedisPool
	 */
	public InstrumentedJedisPool getJedisPool() {
		return jedisPool;
	}



	/**
	 * @param jedisPool the jedisPool to set
	 */
	public void setJedisPool(InstrumentedJedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}



	/**
	 * @param host
	 * @param port
	 * @param timeout
	 */
	public InstrumentedJedis(String host, int port, int timeout) {
		super(host, port, timeout);
		// TODO Auto-generated constructor stub
	}



	/**
	 * @param host
	 * @param port
	 */
	public InstrumentedJedis(String host, int port) {
		super(host, port);
		// TODO Auto-generated constructor stub
	}



	/**
	 * @param host
	 */
	public InstrumentedJedis(String host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	
	private Jedis obtainJedis() {
		Jedis jedis;
		try {
			jedis = jedisPool.getResource();
			if (jedis instanceof Jedis){
				System.out.println("class = " + jedis.getClass() + "checkouted out-" + jedis.hashCode());
			}
		} catch (JedisConnectionException e) {
			throw new JedisException(
					"Unable to get Jedis resource before timeout.", e);
		}
		return jedis;
	}

	//====================================================================
	// All inherited methods instrumented with metrics
	//====================================================================
	

	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#ping()
	 */
	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return super.ping();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#set(java.lang.String, java.lang.String)
	 */
	@Override
	public String set(String key, String value) {
		// TODO Auto-generated method stub
		return super.set(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		// TODO Auto-generated method stub
		return super.get(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#quit()
	 */
	@Override
	public String quit() {
		// TODO Auto-generated method stub
		return super.quit();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#exists(java.lang.String)
	 */
	@Override
	public Boolean exists(String key) {
		// TODO Auto-generated method stub
		return super.exists(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#del(java.lang.String[])
	 */
	@Override
	public Long del(String... keys) {
		// TODO Auto-generated method stub
		return super.del(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#type(java.lang.String)
	 */
	@Override
	public String type(String key) {
		// TODO Auto-generated method stub
		return super.type(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#flushDB()
	 */
	@Override
	public String flushDB() {
		// TODO Auto-generated method stub
		return super.flushDB();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#keys(java.lang.String)
	 */
	@Override
	public Set<String> keys(String pattern) {
		// TODO Auto-generated method stub
		return super.keys(pattern);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#randomKey()
	 */
	@Override
	public String randomKey() {
		// TODO Auto-generated method stub
		return super.randomKey();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#rename(java.lang.String, java.lang.String)
	 */
	@Override
	public String rename(String oldkey, String newkey) {
		// TODO Auto-generated method stub
		return super.rename(oldkey, newkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#renamenx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long renamenx(String oldkey, String newkey) {
		// TODO Auto-generated method stub
		return super.renamenx(oldkey, newkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#expire(java.lang.String, int)
	 */
	@Override
	public Long expire(String key, int seconds) {
		// TODO Auto-generated method stub
		return super.expire(key, seconds);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#expireAt(java.lang.String, long)
	 */
	@Override
	public Long expireAt(String key, long unixTime) {
		// TODO Auto-generated method stub
		return super.expireAt(key, unixTime);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#ttl(java.lang.String)
	 */
	@Override
	public Long ttl(String key) {
		// TODO Auto-generated method stub
		return super.ttl(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#select(int)
	 */
	@Override
	public String select(int index) {
		// TODO Auto-generated method stub
		return super.select(index);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#move(java.lang.String, int)
	 */
	@Override
	public Long move(String key, int dbIndex) {
		// TODO Auto-generated method stub
		return super.move(key, dbIndex);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#flushAll()
	 */
	@Override
	public String flushAll() {
		// TODO Auto-generated method stub
		return super.flushAll();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#getSet(java.lang.String, java.lang.String)
	 */
	@Override
	public String getSet(String key, String value) {
		// TODO Auto-generated method stub
		return super.getSet(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#mget(java.lang.String[])
	 */
	@Override
	public List<String> mget(String... keys) {
		// TODO Auto-generated method stub
		return super.mget(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#setnx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long setnx(String key, String value) {
		// TODO Auto-generated method stub
		return super.setnx(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#setex(java.lang.String, int, java.lang.String)
	 */
	@Override
	public String setex(String key, int seconds, String value) {
		// TODO Auto-generated method stub
		return super.setex(key, seconds, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#mset(java.lang.String[])
	 */
	@Override
	public String mset(String... keysvalues) {
		// TODO Auto-generated method stub
		return super.mset(keysvalues);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#msetnx(java.lang.String[])
	 */
	@Override
	public Long msetnx(String... keysvalues) {
		// TODO Auto-generated method stub
		return super.msetnx(keysvalues);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#decrBy(java.lang.String, long)
	 */
	@Override
	public Long decrBy(String key, long integer) {
		// TODO Auto-generated method stub
		return super.decrBy(key, integer);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#decr(java.lang.String)
	 */
	@Override
	public Long decr(String key) {
		// TODO Auto-generated method stub
		return super.decr(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#incrBy(java.lang.String, long)
	 */
	@Override
	public Long incrBy(String key, long integer) {
		// TODO Auto-generated method stub
		return super.incrBy(key, integer);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#incr(java.lang.String)
	 */
	@Override
	public Long incr(String key) {
		// TODO Auto-generated method stub
		return super.incr(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#append(java.lang.String, java.lang.String)
	 */
	@Override
	public Long append(String key, String value) {
		// TODO Auto-generated method stub
		return super.append(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#substr(java.lang.String, int, int)
	 */
	@Override
	public String substr(String key, int start, int end) {
		// TODO Auto-generated method stub
		return super.substr(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hset(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long hset(String key, String field, String value) {
		// TODO Auto-generated method stub
		return super.hset(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hget(java.lang.String, java.lang.String)
	 */
	@Override
	public String hget(String key, String field) {
		// TODO Auto-generated method stub
		return super.hget(key, field);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hsetnx(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long hsetnx(String key, String field, String value) {
		// TODO Auto-generated method stub
		return super.hsetnx(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hmset(java.lang.String, java.util.Map)
	 */
	@Override
	public String hmset(String key, Map<String, String> hash) {
		// TODO Auto-generated method stub
		return super.hmset(key, hash);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hmget(java.lang.String, java.lang.String[])
	 */
	@Override
	public List<String> hmget(String key, String... fields) {
		// TODO Auto-generated method stub
		return super.hmget(key, fields);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hincrBy(java.lang.String, java.lang.String, long)
	 */
	@Override
	public Long hincrBy(String key, String field, long value) {
		// TODO Auto-generated method stub
		return super.hincrBy(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hexists(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean hexists(String key, String field) {
		// TODO Auto-generated method stub
		return super.hexists(key, field);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hdel(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long hdel(String key, String... fields) {
		// TODO Auto-generated method stub
		return super.hdel(key, fields);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hlen(java.lang.String)
	 */
	@Override
	public Long hlen(String key) {
		// TODO Auto-generated method stub
		return super.hlen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hkeys(java.lang.String)
	 */
	@Override
	public Set<String> hkeys(String key) {
		// TODO Auto-generated method stub
		return super.hkeys(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hvals(java.lang.String)
	 */
	@Override
	public List<String> hvals(String key) {
		// TODO Auto-generated method stub
		return super.hvals(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#hgetAll(java.lang.String)
	 */
	@Override
	public Map<String, String> hgetAll(String key) {
		// TODO Auto-generated method stub
		return super.hgetAll(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#rpush(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long rpush(String key, String... strings) {
		// TODO Auto-generated method stub
		return super.rpush(key, strings);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lpush(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long lpush(String key, String... strings) {
		// TODO Auto-generated method stub
		return super.lpush(key, strings);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#llen(java.lang.String)
	 */
	@Override
	public Long llen(String key) {
		// TODO Auto-generated method stub
		return super.llen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lrange(java.lang.String, long, long)
	 */
	@Override
	public List<String> lrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.lrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#ltrim(java.lang.String, long, long)
	 */
	@Override
	public String ltrim(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.ltrim(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lindex(java.lang.String, long)
	 */
	@Override
	public String lindex(String key, long index) {
		// TODO Auto-generated method stub
		return super.lindex(key, index);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lset(java.lang.String, long, java.lang.String)
	 */
	@Override
	public String lset(String key, long index, String value) {
		// TODO Auto-generated method stub
		return super.lset(key, index, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lrem(java.lang.String, long, java.lang.String)
	 */
	@Override
	public Long lrem(String key, long count, String value) {
		// TODO Auto-generated method stub
		return super.lrem(key, count, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lpop(java.lang.String)
	 */
	@Override
	public String lpop(String key) {
		// TODO Auto-generated method stub
		return super.lpop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#rpop(java.lang.String)
	 */
	@Override
	public String rpop(String key) {
		// TODO Auto-generated method stub
		return super.rpop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#rpoplpush(java.lang.String, java.lang.String)
	 */
	@Override
	public String rpoplpush(String srckey, String dstkey) {
		// TODO Auto-generated method stub
		return super.rpoplpush(srckey, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sadd(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long sadd(String key, String... members) {
		// TODO Auto-generated method stub
		return super.sadd(key, members);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#smembers(java.lang.String)
	 */
	@Override
	public Set<String> smembers(String key) {
		// TODO Auto-generated method stub
		return super.smembers(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#srem(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long srem(String key, String... members) {
		// TODO Auto-generated method stub
		return super.srem(key, members);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#spop(java.lang.String)
	 */
	@Override
	public String spop(String key) {
		// TODO Auto-generated method stub
		return super.spop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#smove(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long smove(String srckey, String dstkey, String member) {
		// TODO Auto-generated method stub
		return super.smove(srckey, dstkey, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#scard(java.lang.String)
	 */
	@Override
	public Long scard(String key) {
		// TODO Auto-generated method stub
		return super.scard(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sismember(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean sismember(String key, String member) {
		// TODO Auto-generated method stub
		return super.sismember(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sinter(java.lang.String[])
	 */
	@Override
	public Set<String> sinter(String... keys) {
		// TODO Auto-generated method stub
		return super.sinter(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sinterstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long sinterstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return super.sinterstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sunion(java.lang.String[])
	 */
	@Override
	public Set<String> sunion(String... keys) {
		// TODO Auto-generated method stub
		return super.sunion(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sunionstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long sunionstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return super.sunionstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sdiff(java.lang.String[])
	 */
	@Override
	public Set<String> sdiff(String... keys) {
		// TODO Auto-generated method stub
		return super.sdiff(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sdiffstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return super.sdiffstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#srandmember(java.lang.String)
	 */
	@Override
	public String srandmember(String key) {
		// TODO Auto-generated method stub
		return super.srandmember(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zadd(java.lang.String, double, java.lang.String)
	 */
	@Override
	public Long zadd(String key, double score, String member) {
		// TODO Auto-generated method stub
		return super.zadd(key, score, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zadd(java.lang.String, java.util.Map)
	 */
	@Override
	public Long zadd(String key, Map<Double, String> scoreMembers) {
		// TODO Auto-generated method stub
		return super.zadd(key, scoreMembers);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrange(java.lang.String, long, long)
	 */
	@Override
	public Set<String> zrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.zrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrem(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long zrem(String key, String... members) {
		// TODO Auto-generated method stub
		return super.zrem(key, members);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zincrby(java.lang.String, double, java.lang.String)
	 */
	@Override
	public Double zincrby(String key, double score, String member) {
		// TODO Auto-generated method stub
		return super.zincrby(key, score, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrank(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zrank(String key, String member) {
		// TODO Auto-generated method stub
		return super.zrank(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrank(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zrevrank(String key, String member) {
		// TODO Auto-generated method stub
		return super.zrevrank(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrange(java.lang.String, long, long)
	 */
	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.zrevrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeWithScores(java.lang.String, long, long)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.zrangeWithScores(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeWithScores(java.lang.String, long, long)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.zrevrangeWithScores(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zcard(java.lang.String)
	 */
	@Override
	public Long zcard(String key) {
		// TODO Auto-generated method stub
		return super.zcard(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zscore(java.lang.String, java.lang.String)
	 */
	@Override
	public Double zscore(String key, String member) {
		// TODO Auto-generated method stub
		return super.zscore(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#watch(java.lang.String[])
	 */
	@Override
	public String watch(String... keys) {
		// TODO Auto-generated method stub
		return super.watch(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String)
	 */
	@Override
	public List<String> sort(String key) {
		// TODO Auto-generated method stub
		return super.sort(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String, redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		// TODO Auto-generated method stub
		return super.sort(key, sortingParameters);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#blpop(int, java.lang.String[])
	 */
	@Override
	public List<String> blpop(int timeout, String... keys) {
		// TODO Auto-generated method stub
		return super.blpop(timeout, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String, redis.clients.jedis.SortingParams, java.lang.String)
	 */
	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		// TODO Auto-generated method stub
		return super.sort(key, sortingParameters, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sort(String key, String dstkey) {
		// TODO Auto-generated method stub
		return super.sort(key, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#brpop(int, java.lang.String[])
	 */
	@Override
	public List<String> brpop(int timeout, String... keys) {
		// TODO Auto-generated method stub
		return super.brpop(timeout, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#auth(java.lang.String)
	 */
	@Override
	public String auth(String password) {
		// TODO Auto-generated method stub
		return super.auth(password);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#subscribe(redis.clients.jedis.JedisPubSub, java.lang.String[])
	 */
	@Override
	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		// TODO Auto-generated method stub
		super.subscribe(jedisPubSub, channels);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#publish(java.lang.String, java.lang.String)
	 */
	@Override
	public Long publish(String channel, String message) {
		// TODO Auto-generated method stub
		return super.publish(channel, message);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#psubscribe(redis.clients.jedis.JedisPubSub, java.lang.String[])
	 */
	@Override
	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		// TODO Auto-generated method stub
		super.psubscribe(jedisPubSub, patterns);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zcount(java.lang.String, double, double)
	 */
	@Override
	public Long zcount(String key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zcount(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zcount(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long zcount(String key, String min, String max) {
		// TODO Auto-generated method stub
		return super.zcount(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Set<String> zrangeByScore(String key, String min, String max,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min,
			double max, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min,
			String max, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zremrangeByRank(java.lang.String, long, long)
	 */
	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		// TODO Auto-generated method stub
		return super.zremrangeByRank(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zremrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		// TODO Auto-generated method stub
		return super.zremrangeByScore(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zremrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		// TODO Auto-generated method stub
		return super.zremrangeByScore(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zunionstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long zunionstore(String dstkey, String... sets) {
		// TODO Auto-generated method stub
		return super.zunionstore(dstkey, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zunionstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String[])
	 */
	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		// TODO Auto-generated method stub
		return super.zunionstore(dstkey, params, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zinterstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public Long zinterstore(String dstkey, String... sets) {
		// TODO Auto-generated method stub
		return super.zinterstore(dstkey, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#zinterstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String[])
	 */
	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		// TODO Auto-generated method stub
		return super.zinterstore(dstkey, params, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#strlen(java.lang.String)
	 */
	@Override
	public Long strlen(String key) {
		// TODO Auto-generated method stub
		return super.strlen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#lpushx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long lpushx(String key, String string) {
		// TODO Auto-generated method stub
		return super.lpushx(key, string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#persist(java.lang.String)
	 */
	@Override
	public Long persist(String key) {
		// TODO Auto-generated method stub
		return super.persist(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#rpushx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long rpushx(String key, String string) {
		// TODO Auto-generated method stub
		return super.rpushx(key, string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#echo(java.lang.String)
	 */
	@Override
	public String echo(String string) {
		// TODO Auto-generated method stub
		return super.echo(string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#linsert(java.lang.String, redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String, java.lang.String)
	 */
	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		// TODO Auto-generated method stub
		return super.linsert(key, where, pivot, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#brpoplpush(java.lang.String, java.lang.String, int)
	 */
	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		// TODO Auto-generated method stub
		return super.brpoplpush(source, destination, timeout);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#setbit(java.lang.String, long, boolean)
	 */
	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		// TODO Auto-generated method stub
		return super.setbit(key, offset, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#getbit(java.lang.String, long)
	 */
	@Override
	public Boolean getbit(String key, long offset) {
		// TODO Auto-generated method stub
		return super.getbit(key, offset);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#setrange(java.lang.String, long, java.lang.String)
	 */
	@Override
	public Long setrange(String key, long offset, String value) {
		// TODO Auto-generated method stub
		return super.setrange(key, offset, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#getrange(java.lang.String, long, long)
	 */
	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		// TODO Auto-generated method stub
		return super.getrange(key, startOffset, endOffset);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#configGet(java.lang.String)
	 */
	@Override
	public List<String> configGet(String pattern) {
		// TODO Auto-generated method stub
		return super.configGet(pattern);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#configSet(java.lang.String, java.lang.String)
	 */
	@Override
	public String configSet(String parameter, String value) {
		// TODO Auto-generated method stub
		return super.configSet(parameter, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#eval(java.lang.String, int, java.lang.String[])
	 */
	@Override
	public Object eval(String script, int keyCount, String... params) {
		// TODO Auto-generated method stub
		return super.eval(script, keyCount, params);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#eval(java.lang.String, java.util.List, java.util.List)
	 */
	@Override
	public Object eval(String script, List<String> keys, List<String> args) {
		// TODO Auto-generated method stub
		return super.eval(script, keys, args);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#eval(java.lang.String)
	 */
	@Override
	public Object eval(String script) {
		// TODO Auto-generated method stub
		return super.eval(script);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#evalsha(java.lang.String)
	 */
	@Override
	public Object evalsha(String script) {
		// TODO Auto-generated method stub
		return super.evalsha(script);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#evalsha(java.lang.String, java.util.List, java.util.List)
	 */
	@Override
	public Object evalsha(String sha1, List<String> keys, List<String> args) {
		// TODO Auto-generated method stub
		return super.evalsha(sha1, keys, args);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#evalsha(java.lang.String, int, java.lang.String[])
	 */
	@Override
	public Object evalsha(String sha1, int keyCount, String... params) {
		// TODO Auto-generated method stub
		return super.evalsha(sha1, keyCount, params);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#scriptExists(java.lang.String)
	 */
	@Override
	public Boolean scriptExists(String sha1) {
		// TODO Auto-generated method stub
		return super.scriptExists(sha1);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#scriptExists(java.lang.String[])
	 */
	@Override
	public List<Boolean> scriptExists(String... sha1) {
		// TODO Auto-generated method stub
		return super.scriptExists(sha1);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#scriptLoad(java.lang.String)
	 */
	@Override
	public String scriptLoad(String script) {
		// TODO Auto-generated method stub
		return super.scriptLoad(script);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#slowlogGet()
	 */
	@Override
	public List<Slowlog> slowlogGet() {
		// TODO Auto-generated method stub
		return super.slowlogGet();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#slowlogGet(long)
	 */
	@Override
	public List<Slowlog> slowlogGet(long entries) {
		// TODO Auto-generated method stub
		return super.slowlogGet(entries);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#objectRefcount(java.lang.String)
	 */
	@Override
	public Long objectRefcount(String string) {
		// TODO Auto-generated method stub
		return super.objectRefcount(string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#objectEncoding(java.lang.String)
	 */
	@Override
	public String objectEncoding(String string) {
		// TODO Auto-generated method stub
		return super.objectEncoding(string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.Jedis#objectIdletime(java.lang.String)
	 */
	@Override
	public Long objectIdletime(String string) {
		// TODO Auto-generated method stub
		return super.objectIdletime(string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#set(byte[], byte[])
	 */
	@Override
	public String set(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		return super.set(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#get(byte[])
	 */
	@Override
	public byte[] get(byte[] key) {
		// TODO Auto-generated method stub
		return super.get(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#exists(byte[])
	 */
	@Override
	public Boolean exists(byte[] key) {
		// TODO Auto-generated method stub
		return super.exists(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#del(byte[][])
	 */
	@Override
	public Long del(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.del(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#type(byte[])
	 */
	@Override
	public String type(byte[] key) {
		// TODO Auto-generated method stub
		return super.type(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#keys(byte[])
	 */
	@Override
	public Set<byte[]> keys(byte[] pattern) {
		// TODO Auto-generated method stub
		return super.keys(pattern);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#randomBinaryKey()
	 */
	@Override
	public byte[] randomBinaryKey() {
		// TODO Auto-generated method stub
		return super.randomBinaryKey();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#rename(byte[], byte[])
	 */
	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		// TODO Auto-generated method stub
		return super.rename(oldkey, newkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#renamenx(byte[], byte[])
	 */
	@Override
	public Long renamenx(byte[] oldkey, byte[] newkey) {
		// TODO Auto-generated method stub
		return super.renamenx(oldkey, newkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#dbSize()
	 */
	@Override
	public Long dbSize() {
		// TODO Auto-generated method stub
		return super.dbSize();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#expire(byte[], int)
	 */
	@Override
	public Long expire(byte[] key, int seconds) {
		// TODO Auto-generated method stub
		return super.expire(key, seconds);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#expireAt(byte[], long)
	 */
	@Override
	public Long expireAt(byte[] key, long unixTime) {
		// TODO Auto-generated method stub
		return super.expireAt(key, unixTime);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#ttl(byte[])
	 */
	@Override
	public Long ttl(byte[] key) {
		// TODO Auto-generated method stub
		return super.ttl(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#move(byte[], int)
	 */
	@Override
	public Long move(byte[] key, int dbIndex) {
		// TODO Auto-generated method stub
		return super.move(key, dbIndex);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#getSet(byte[], byte[])
	 */
	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		return super.getSet(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#mget(byte[][])
	 */
	@Override
	public List<byte[]> mget(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.mget(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#setnx(byte[], byte[])
	 */
	@Override
	public Long setnx(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		return super.setnx(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#setex(byte[], int, byte[])
	 */
	@Override
	public String setex(byte[] key, int seconds, byte[] value) {
		// TODO Auto-generated method stub
		return super.setex(key, seconds, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#mset(byte[][])
	 */
	@Override
	public String mset(byte[]... keysvalues) {
		// TODO Auto-generated method stub
		return super.mset(keysvalues);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#msetnx(byte[][])
	 */
	@Override
	public Long msetnx(byte[]... keysvalues) {
		// TODO Auto-generated method stub
		return super.msetnx(keysvalues);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#decrBy(byte[], long)
	 */
	@Override
	public Long decrBy(byte[] key, long integer) {
		// TODO Auto-generated method stub
		return super.decrBy(key, integer);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#decr(byte[])
	 */
	@Override
	public Long decr(byte[] key) {
		// TODO Auto-generated method stub
		return super.decr(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#incrBy(byte[], long)
	 */
	@Override
	public Long incrBy(byte[] key, long integer) {
		// TODO Auto-generated method stub
		return super.incrBy(key, integer);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#incr(byte[])
	 */
	@Override
	public Long incr(byte[] key) {
		// TODO Auto-generated method stub
		return super.incr(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#append(byte[], byte[])
	 */
	@Override
	public Long append(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		return super.append(key, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#substr(byte[], int, int)
	 */
	@Override
	public byte[] substr(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.substr(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hset(byte[], byte[], byte[])
	 */
	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		// TODO Auto-generated method stub
		return super.hset(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hget(byte[], byte[])
	 */
	@Override
	public byte[] hget(byte[] key, byte[] field) {
		// TODO Auto-generated method stub
		return super.hget(key, field);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hsetnx(byte[], byte[], byte[])
	 */
	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		// TODO Auto-generated method stub
		return super.hsetnx(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hmset(byte[], java.util.Map)
	 */
	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		// TODO Auto-generated method stub
		return super.hmset(key, hash);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hmget(byte[], byte[][])
	 */
	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		// TODO Auto-generated method stub
		return super.hmget(key, fields);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hincrBy(byte[], byte[], long)
	 */
	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		// TODO Auto-generated method stub
		return super.hincrBy(key, field, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hexists(byte[], byte[])
	 */
	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		// TODO Auto-generated method stub
		return super.hexists(key, field);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hdel(byte[], byte[][])
	 */
	@Override
	public Long hdel(byte[] key, byte[]... fields) {
		// TODO Auto-generated method stub
		return super.hdel(key, fields);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hlen(byte[])
	 */
	@Override
	public Long hlen(byte[] key) {
		// TODO Auto-generated method stub
		return super.hlen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hkeys(byte[])
	 */
	@Override
	public Set<byte[]> hkeys(byte[] key) {
		// TODO Auto-generated method stub
		return super.hkeys(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hvals(byte[])
	 */
	@Override
	public List<byte[]> hvals(byte[] key) {
		// TODO Auto-generated method stub
		return super.hvals(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#hgetAll(byte[])
	 */
	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		// TODO Auto-generated method stub
		return super.hgetAll(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#rpush(byte[], byte[][])
	 */
	@Override
	public Long rpush(byte[] key, byte[]... strings) {
		// TODO Auto-generated method stub
		return super.rpush(key, strings);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lpush(byte[], byte[][])
	 */
	@Override
	public Long lpush(byte[] key, byte[]... strings) {
		// TODO Auto-generated method stub
		return super.lpush(key, strings);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#llen(byte[])
	 */
	@Override
	public Long llen(byte[] key) {
		// TODO Auto-generated method stub
		return super.llen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lrange(byte[], int, int)
	 */
	@Override
	public List<byte[]> lrange(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.lrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#ltrim(byte[], int, int)
	 */
	@Override
	public String ltrim(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.ltrim(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lindex(byte[], int)
	 */
	@Override
	public byte[] lindex(byte[] key, int index) {
		// TODO Auto-generated method stub
		return super.lindex(key, index);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lset(byte[], int, byte[])
	 */
	@Override
	public String lset(byte[] key, int index, byte[] value) {
		// TODO Auto-generated method stub
		return super.lset(key, index, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lrem(byte[], int, byte[])
	 */
	@Override
	public Long lrem(byte[] key, int count, byte[] value) {
		// TODO Auto-generated method stub
		return super.lrem(key, count, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lpop(byte[])
	 */
	@Override
	public byte[] lpop(byte[] key) {
		// TODO Auto-generated method stub
		return super.lpop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#rpop(byte[])
	 */
	@Override
	public byte[] rpop(byte[] key) {
		// TODO Auto-generated method stub
		return super.rpop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#rpoplpush(byte[], byte[])
	 */
	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		// TODO Auto-generated method stub
		return super.rpoplpush(srckey, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sadd(byte[], byte[][])
	 */
	@Override
	public Long sadd(byte[] key, byte[]... members) {
		// TODO Auto-generated method stub
		return super.sadd(key, members);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#smembers(byte[])
	 */
	@Override
	public Set<byte[]> smembers(byte[] key) {
		// TODO Auto-generated method stub
		return super.smembers(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#srem(byte[], byte[][])
	 */
	@Override
	public Long srem(byte[] key, byte[]... member) {
		// TODO Auto-generated method stub
		return super.srem(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#spop(byte[])
	 */
	@Override
	public byte[] spop(byte[] key) {
		// TODO Auto-generated method stub
		return super.spop(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#smove(byte[], byte[], byte[])
	 */
	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		// TODO Auto-generated method stub
		return super.smove(srckey, dstkey, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#scard(byte[])
	 */
	@Override
	public Long scard(byte[] key) {
		// TODO Auto-generated method stub
		return super.scard(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sismember(byte[], byte[])
	 */
	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		// TODO Auto-generated method stub
		return super.sismember(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sinter(byte[][])
	 */
	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sinter(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sinterstore(byte[], byte[][])
	 */
	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sinterstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sunion(byte[][])
	 */
	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sunion(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sunionstore(byte[], byte[][])
	 */
	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sunionstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sdiff(byte[][])
	 */
	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sdiff(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sdiffstore(byte[], byte[][])
	 */
	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return super.sdiffstore(dstkey, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#srandmember(byte[])
	 */
	@Override
	public byte[] srandmember(byte[] key) {
		// TODO Auto-generated method stub
		return super.srandmember(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zadd(byte[], double, byte[])
	 */
	@Override
	public Long zadd(byte[] key, double score, byte[] member) {
		// TODO Auto-generated method stub
		return super.zadd(key, score, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zadd(byte[], java.util.Map)
	 */
	@Override
	public Long zadd(byte[] key, Map<Double, byte[]> scoreMembers) {
		// TODO Auto-generated method stub
		return super.zadd(key, scoreMembers);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrange(byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrange(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.zrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrem(byte[], byte[][])
	 */
	@Override
	public Long zrem(byte[] key, byte[]... members) {
		// TODO Auto-generated method stub
		return super.zrem(key, members);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zincrby(byte[], double, byte[])
	 */
	@Override
	public Double zincrby(byte[] key, double score, byte[] member) {
		// TODO Auto-generated method stub
		return super.zincrby(key, score, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrank(byte[], byte[])
	 */
	@Override
	public Long zrank(byte[] key, byte[] member) {
		// TODO Auto-generated method stub
		return super.zrank(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrank(byte[], byte[])
	 */
	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		// TODO Auto-generated method stub
		return super.zrevrank(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrange(byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrevrange(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.zrevrange(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeWithScores(byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.zrangeWithScores(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeWithScores(byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.zrevrangeWithScores(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zcard(byte[])
	 */
	@Override
	public Long zcard(byte[] key) {
		// TODO Auto-generated method stub
		return super.zcard(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zscore(byte[], byte[])
	 */
	@Override
	public Double zscore(byte[] key, byte[] member) {
		// TODO Auto-generated method stub
		return super.zscore(key, member);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#multi()
	 */
	@Override
	public Transaction multi() {
		// TODO Auto-generated method stub
		return super.multi();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#multi(redis.clients.jedis.TransactionBlock)
	 */
	@Override
	public List<Object> multi(TransactionBlock jedisTransaction) {
		// TODO Auto-generated method stub
		return super.multi(jedisTransaction);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#checkIsInMulti()
	 */
	@Override
	protected void checkIsInMulti() {
		// TODO Auto-generated method stub
		super.checkIsInMulti();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#connect()
	 */
	@Override
	public void connect() {
		// TODO Auto-generated method stub
		super.connect();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		super.disconnect();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#watch(byte[][])
	 */
	@Override
	public String watch(byte[]... keys) {
		// TODO Auto-generated method stub
		return super.watch(keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#unwatch()
	 */
	@Override
	public String unwatch() {
		// TODO Auto-generated method stub
		return super.unwatch();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[])
	 */
	@Override
	public List<byte[]> sort(byte[] key) {
		// TODO Auto-generated method stub
		return super.sort(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[], redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		// TODO Auto-generated method stub
		return super.sort(key, sortingParameters);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#blpop(int, byte[][])
	 */
	@Override
	public List<byte[]> blpop(int timeout, byte[]... keys) {
		// TODO Auto-generated method stub
		return super.blpop(timeout, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[], redis.clients.jedis.SortingParams, byte[])
	 */
	@Override
	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		// TODO Auto-generated method stub
		return super.sort(key, sortingParameters, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[], byte[])
	 */
	@Override
	public Long sort(byte[] key, byte[] dstkey) {
		// TODO Auto-generated method stub
		return super.sort(key, dstkey);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#brpop(int, byte[][])
	 */
	@Override
	public List<byte[]> brpop(int timeout, byte[]... keys) {
		// TODO Auto-generated method stub
		return super.brpop(timeout, keys);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#pipelined(redis.clients.jedis.PipelineBlock)
	 */
	@Override
	public List<Object> pipelined(PipelineBlock jedisPipeline) {
		// TODO Auto-generated method stub
		return super.pipelined(jedisPipeline);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#pipelined()
	 */
	@Override
	public Pipeline pipelined() {
		// TODO Auto-generated method stub
		return super.pipelined();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zcount(byte[], double, double)
	 */
	@Override
	public Long zcount(byte[] key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zcount(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zcount(byte[], byte[], byte[])
	 */
	@Override
	public Long zcount(byte[] key, byte[] min, byte[] max) {
		// TODO Auto-generated method stub
		return super.zcount(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], double, double)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], byte[], byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScore(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[], double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[], byte[], byte[])
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
			double max, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[], byte[], byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
			byte[] max, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrangeByScoreWithScores(key, min, max, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], double, double)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], byte[], byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min,
			int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScore(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[], double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[], byte[], byte[])
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[], byte[], byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min, int offset, int count) {
		// TODO Auto-generated method stub
		return super.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zremrangeByRank(byte[], int, int)
	 */
	@Override
	public Long zremrangeByRank(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return super.zremrangeByRank(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zremrangeByScore(byte[], double, double)
	 */
	@Override
	public Long zremrangeByScore(byte[] key, double start, double end) {
		// TODO Auto-generated method stub
		return super.zremrangeByScore(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zremrangeByScore(byte[], byte[], byte[])
	 */
	@Override
	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		// TODO Auto-generated method stub
		return super.zremrangeByScore(key, start, end);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zunionstore(byte[], byte[][])
	 */
	@Override
	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		// TODO Auto-generated method stub
		return super.zunionstore(dstkey, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zunionstore(byte[], redis.clients.jedis.ZParams, byte[][])
	 */
	@Override
	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		// TODO Auto-generated method stub
		return super.zunionstore(dstkey, params, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zinterstore(byte[], byte[][])
	 */
	@Override
	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		// TODO Auto-generated method stub
		return super.zinterstore(dstkey, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#zinterstore(byte[], redis.clients.jedis.ZParams, byte[][])
	 */
	@Override
	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		// TODO Auto-generated method stub
		return super.zinterstore(dstkey, params, sets);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#save()
	 */
	@Override
	public String save() {
		// TODO Auto-generated method stub
		return super.save();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#bgsave()
	 */
	@Override
	public String bgsave() {
		// TODO Auto-generated method stub
		return super.bgsave();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#bgrewriteaof()
	 */
	@Override
	public String bgrewriteaof() {
		// TODO Auto-generated method stub
		return super.bgrewriteaof();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lastsave()
	 */
	@Override
	public Long lastsave() {
		// TODO Auto-generated method stub
		return super.lastsave();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#shutdown()
	 */
	@Override
	public String shutdown() {
		// TODO Auto-generated method stub
		return super.shutdown();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#info()
	 */
	@Override
	public String info() {
		// TODO Auto-generated method stub
		return super.info();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#monitor(redis.clients.jedis.JedisMonitor)
	 */
	@Override
	public void monitor(JedisMonitor jedisMonitor) {
		// TODO Auto-generated method stub
		super.monitor(jedisMonitor);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slaveof(java.lang.String, int)
	 */
	@Override
	public String slaveof(String host, int port) {
		// TODO Auto-generated method stub
		return super.slaveof(host, port);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slaveofNoOne()
	 */
	@Override
	public String slaveofNoOne() {
		// TODO Auto-generated method stub
		return super.slaveofNoOne();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#configGet(byte[])
	 */
	@Override
	public List<byte[]> configGet(byte[] pattern) {
		// TODO Auto-generated method stub
		return super.configGet(pattern);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#configResetStat()
	 */
	@Override
	public String configResetStat() {
		// TODO Auto-generated method stub
		return super.configResetStat();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#configSet(byte[], byte[])
	 */
	@Override
	public byte[] configSet(byte[] parameter, byte[] value) {
		// TODO Auto-generated method stub
		return super.configSet(parameter, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#isConnected()
	 */
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return super.isConnected();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#strlen(byte[])
	 */
	@Override
	public Long strlen(byte[] key) {
		// TODO Auto-generated method stub
		return super.strlen(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#sync()
	 */
	@Override
	public void sync() {
		// TODO Auto-generated method stub
		super.sync();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#lpushx(byte[], byte[])
	 */
	@Override
	public Long lpushx(byte[] key, byte[] string) {
		// TODO Auto-generated method stub
		return super.lpushx(key, string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#persist(byte[])
	 */
	@Override
	public Long persist(byte[] key) {
		// TODO Auto-generated method stub
		return super.persist(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#rpushx(byte[], byte[])
	 */
	@Override
	public Long rpushx(byte[] key, byte[] string) {
		// TODO Auto-generated method stub
		return super.rpushx(key, string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#echo(byte[])
	 */
	@Override
	public byte[] echo(byte[] string) {
		// TODO Auto-generated method stub
		return super.echo(string);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#linsert(byte[], redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	@Override
	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		// TODO Auto-generated method stub
		return super.linsert(key, where, pivot, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#debug(redis.clients.jedis.DebugParams)
	 */
	@Override
	public String debug(DebugParams params) {
		// TODO Auto-generated method stub
		return super.debug(params);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#getClient()
	 */
	@Override
	public Client getClient() {
		// TODO Auto-generated method stub
		return super.getClient();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#brpoplpush(byte[], byte[], int)
	 */
	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		// TODO Auto-generated method stub
		return super.brpoplpush(source, destination, timeout);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#setbit(byte[], long, byte[])
	 */
	@Override
	public Boolean setbit(byte[] key, long offset, byte[] value) {
		// TODO Auto-generated method stub
		return super.setbit(key, offset, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#getbit(byte[], long)
	 */
	@Override
	public Boolean getbit(byte[] key, long offset) {
		// TODO Auto-generated method stub
		return super.getbit(key, offset);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#setrange(byte[], long, byte[])
	 */
	@Override
	public Long setrange(byte[] key, long offset, byte[] value) {
		// TODO Auto-generated method stub
		return super.setrange(key, offset, value);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#getrange(byte[], long, long)
	 */
	@Override
	public String getrange(byte[] key, long startOffset, long endOffset) {
		// TODO Auto-generated method stub
		return super.getrange(key, startOffset, endOffset);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#publish(byte[], byte[])
	 */
	@Override
	public Long publish(byte[] channel, byte[] message) {
		// TODO Auto-generated method stub
		return super.publish(channel, message);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#subscribe(redis.clients.jedis.BinaryJedisPubSub, byte[][])
	 */
	@Override
	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		// TODO Auto-generated method stub
		super.subscribe(jedisPubSub, channels);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#psubscribe(redis.clients.jedis.BinaryJedisPubSub, byte[][])
	 */
	@Override
	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		// TODO Auto-generated method stub
		super.psubscribe(jedisPubSub, patterns);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#getDB()
	 */
	@Override
	public Long getDB() {
		// TODO Auto-generated method stub
		return super.getDB();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#eval(byte[], java.util.List, java.util.List)
	 */
	@Override
	public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		// TODO Auto-generated method stub
		return super.eval(script, keys, args);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#eval(byte[], byte[], byte[][])
	 */
	@Override
	public Object eval(byte[] script, byte[] keyCount, byte[][] params) {
		// TODO Auto-generated method stub
		return super.eval(script, keyCount, params);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#scriptFlush()
	 */
	@Override
	public byte[] scriptFlush() {
		// TODO Auto-generated method stub
		return super.scriptFlush();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#scriptExists(byte[][])
	 */
	@Override
	public List<Long> scriptExists(byte[]... sha1) {
		// TODO Auto-generated method stub
		return super.scriptExists(sha1);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#scriptLoad(byte[])
	 */
	@Override
	public byte[] scriptLoad(byte[] script) {
		// TODO Auto-generated method stub
		return super.scriptLoad(script);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#scriptKill()
	 */
	@Override
	public byte[] scriptKill() {
		// TODO Auto-generated method stub
		return super.scriptKill();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slowlogReset()
	 */
	@Override
	public byte[] slowlogReset() {
		// TODO Auto-generated method stub
		return super.slowlogReset();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slowlogLen()
	 */
	@Override
	public long slowlogLen() {
		// TODO Auto-generated method stub
		return super.slowlogLen();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slowlogGetBinary()
	 */
	@Override
	public List<byte[]> slowlogGetBinary() {
		// TODO Auto-generated method stub
		return super.slowlogGetBinary();
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#slowlogGetBinary(long)
	 */
	@Override
	public List<byte[]> slowlogGetBinary(long entries) {
		// TODO Auto-generated method stub
		return super.slowlogGetBinary(entries);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#objectRefcount(byte[])
	 */
	@Override
	public Long objectRefcount(byte[] key) {
		// TODO Auto-generated method stub
		return super.objectRefcount(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#objectEncoding(byte[])
	 */
	@Override
	public byte[] objectEncoding(byte[] key) {
		// TODO Auto-generated method stub
		return super.objectEncoding(key);
	}



	/* (non-Javadoc)
	 * @see redis.clients.jedis.BinaryJedis#objectIdletime(byte[])
	 */
	@Override
	public Long objectIdletime(byte[] key) {
		// TODO Auto-generated method stub
		return super.objectIdletime(key);
	}



	private void returnJedis(Jedis jedis) {
		try {
			if (jedis.isConnected()) {
				jedis.ping();
				jedisPool.returnResource(jedis);
			} else {
				jedisPool.returnBrokenResource(jedis);
			}
		} catch (JedisException e) {
			jedisPool.returnBrokenResource(jedis);
		}
	}

	private MetricName buildMetricName(Method m, String scope) {
		Class<?> clazz = m.getDeclaringClass();
		String metricName = m.getName();
		return new MetricName(clazz, metricName, scope);
	}

	private Timer buildTimer(MetricsRegistry registry, MetricName metricName) {
		return registry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
	}

	//====================================================================
	// End of all inherited methods instrumented with metrics
	//====================================================================

}
