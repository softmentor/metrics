package com.yammer.metrics.jedis.health;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.yammer.metrics.core.HealthCheck;

/**
 * @author jiny
 *
 */
public class JedisPoolHealthCheck extends HealthCheck {

	  private JedisPool jedisPool;

	  public JedisPoolHealthCheck(JedisPool jedisPool) {
	    super("JedisPoolCheck");
	    this.jedisPool = jedisPool;
	  }

	  @Override
	  protected Result check() throws Exception {
	    Jedis jedis = jedisPool.getResource();
	    try {
	      long start = System.currentTimeMillis();
	      String pong = jedis.ping();
	      if (pong.equalsIgnoreCase("pong")) {
	        return Result.healthy("Check Latency: " + (System.currentTimeMillis() - start) + " ms");
	      } else {
	        return Result.unhealthy(pong);
	      }
	    } finally {
	    	jedisPool.returnResource(jedis);
	    }
	  }

	}