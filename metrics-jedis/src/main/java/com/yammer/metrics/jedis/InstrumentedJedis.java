package com.yammer.metrics.jedis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * 
 * @author softmentor
 * 
 */
public class InstrumentedJedis implements InvocationHandler {

	private final InstrumentedJedisPool jedisPool;

	public InstrumentedJedis(InstrumentedJedisPool pool) {
		this.jedisPool = pool;
	}

	public static JedisCommands newInstance(InstrumentedJedisPool pool) {
		JedisCommands jedisCmd = (JedisCommands) Proxy.newProxyInstance(
				Jedis.class.getClassLoader(), Jedis.class.getInterfaces(),
				new InstrumentedJedis(pool));
		return jedisCmd;
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		Object result;
		Jedis jedis = obtainJedis();
		String metricScope = jedisPool.getMetricScope();
		MetricsRegistry registry = jedisPool.getRegistry();
		Timer jedisCmdTimer = null;
		TimerContext timerContext = null;
		try {
			MetricName jedisCmdMetrics = buildMetricName(m, metricScope);
			jedisCmdTimer = buildTimer(registry, jedisCmdMetrics);
			timerContext = jedisCmdTimer.time();
			result = m.invoke(jedis, args);
			
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (Exception e) {
			throw new JedisException("Unexpected proxy invocation exception: "
					+ e.getMessage(), e);
		} finally {
			returnJedis(jedis);
			timerContext.stop();
		}
		return result;
	}

	private Jedis obtainJedis() {
		Jedis jedis;
		try {
			jedis = jedisPool.getResource();
		} catch (JedisConnectionException e) {
			throw new JedisException(
					"Unable to get Jedis resource before timeout.", e);
		}
		return jedis;
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
}
