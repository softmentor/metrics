package com.codahale.metrics.jedis;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;



/**
 * 
 * @author softmentor
 * 
 */
public class JedisProxy implements InvocationHandler {

  private final InstrumentedJedisPool jedisPool;

  public JedisProxy(InstrumentedJedisPool pool) {
    this.jedisPool = pool;
  }

  public IJedisProxy newInstance() {
    IJedisProxy jedis =
        (IJedisProxy) Proxy.newProxyInstance(IJedisProxy.class.getClassLoader(),
            new Class[] {IJedisProxy.class}, new JedisProxy(this.jedisPool));
    return jedis;
  }

  @Override
  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    Object result;
    InstrumentedJedis jedis = obtainJedis();
    // String metricScope = jedisPool.getMetricScope();
    MetricRegistry registry = jedisPool.getRegistry();
    Timer jedisCmdTimer = null;
    Timer.Context timerContext = null;
    try {
      // MetricName jedisCmdMetrics = buildMetricName(m, metricScope);
      // System.out.println("metricName="+ jedisCmdMetrics);
      jedisCmdTimer = buildTimer(registry, buildMetricName(m));
      timerContext = jedisCmdTimer.time();
      result = m.invoke(jedis, args);

    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      e.printStackTrace();
      throw new JedisException("Unexpected proxy invocation exception: " + e.getMessage(), e);

    } finally {
      returnJedis(jedis);
      if (null != timerContext) {
        timerContext.stop();
      }
    }
    return result;
  }

  private InstrumentedJedis obtainJedis() {
    InstrumentedJedis jedis;
    try {
      jedis = jedisPool.getResource();
      // if (jedis instanceof Jedis){
      // System.out.println("class = " + jedis.getClass() + "checked out-" + jedis.hashCode());
      // }
    } catch (JedisConnectionException e) {
      throw new JedisException("Unable to get Jedis resource before timeout.", e);
    }
    return jedis;
  }

  private void returnJedis(InstrumentedJedis jedis) {
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

  private String buildMetricName(Method m) {
    Class<?> clazz = m.getDeclaringClass();
    String metricName = m.getName();
    return name(clazz.getClass(), metricName);
  }

  private Timer buildTimer(MetricRegistry registry, String metricName) {
    Timer metricTimer = null;
    if (null == registry.getTimers().get(metricName)) {
      registry.register(metricName, new Timer());
    }
    metricTimer = registry.getTimers().get(metricName);
    return metricTimer;
  }
}
