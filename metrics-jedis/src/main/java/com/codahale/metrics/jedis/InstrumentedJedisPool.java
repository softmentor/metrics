package com.codahale.metrics.jedis;

import static com.codahale.metrics.MetricRegistry.name;

import java.net.URI;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

/**
 * This class helps to instrument the underlying {@link GenericObjectPool} and expose key metrics.
 * It also exposes bunch of other information exposed by redis via INFO command. It also instruments
 * metrics for the following {@link JedisFactory#makeObject()} ,
 * {@link JedisFactory#destroyObject(Object)}, {@link JedisFactory#validateObject(Object)} using
 * yammer metrics annotations {@link Timed}, {@link Metered}, {@link ExceptionMetered}
 * <p>
 * Below is the list of all the metrics that are currently instrumented.
 * <table>
 * <tr>
 * <td>{@code max-active}</td>
 * <td>Returns the maximum number of objects that can be allocated by the pool (checked out to
 * clients, or idle awaiting checkout) at a given time. When non-positive, there is no limit to the
 * number of objects that can be managed by the pool at one time.</td>
 * </tr>
 * <tr>
 * <td>{@code max-idle}</td>
 * <td>Returns the cap on the number of "idle" instances in the pool.</td>
 * </tr>
 * <tr>
 * <td>{@code min-idle}</td>
 * <td>Returns the minimum number of objects allowed in the pool before the evictor thread (if
 * active) spawns new objects. (Note no objects are created when: numActive + numIdle >= maxActive)</td>
 * </tr>
 * <tr>
 * <td>{@code num-active}</td>
 * <td>Return the number of instances currently borrowed from this pool.</td>
 * </tr>
 * <tr>
 * <td>{@code num-idle}</td>
 * <td>Return the number of instances currently idle in this pool.</td>
 * </tr>
 * <tr>
 * <td>{@code percent-idle}</td>
 * <td>Return percentage of currently idle instances in this pool. Assuming total instances =
 * {@value num-active} + {@value num-idle}</td>
 * </tr>
 * </table>
 * 
 * <br>
 * <i>Note:</i> This class would extend from {@link JedisPool} in future versions, 2.2.x onwards.
 * Since {@link Pool} currently(2.1.x) has its instance variable {@link GenericObjectPool} defined
 * as private.
 * <p>
 * In future(2.2.x) we would be able to access the {@link GenericObjectPool} in this class, as it
 * would be in protected scope and gather useful metrics regarding the pool.
 * 
 * @author softmentor
 * 
 */
public class InstrumentedJedisPool extends InstrumentedPool<InstrumentedJedis> {

  private MetricRegistry registry = null;
  private Counter poolObjectsCounter = null;

  public static final String MAX_ACTIVE = "max-active";
  public static final String MAX_IDLE = "max-idle";
  public static final String MIN_IDLE = "min-idle";
  public static final String NUM_ACTIVE = "num-active";
  public static final String NUM_IDLE = "num-idle";
  public static final String PERCENT_IDLE = "percent-idle";
  public static final String POOL_OBJECTS_COUNT = "pool-objects-count";

  /**
   * @param poolConfig
   * @param host
   */
  public InstrumentedJedisPool(final Config poolConfig, final String host, MetricRegistry metricRegistry) {

    this(poolConfig, host, Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, null,
        Protocol.DEFAULT_DATABASE, metricRegistry);
  }

  /**
   * @param host
   * @param port
   */
  public InstrumentedJedisPool(String host, int port, MetricRegistry metricRegistry) {
    this(new Config(), host, port, Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE, metricRegistry);
  }

  /**
   * @param host
   */
  public InstrumentedJedisPool(final String h) {
    URI uri = URI.create(h);
    // Init with default values first
    JedisFactory jedisFactory = null;
    String host = h;
    int port = Protocol.DEFAULT_PORT;
    String password = null;
    int database = Protocol.DEFAULT_DATABASE;
    int timeout = Protocol.DEFAULT_TIMEOUT;
    // If uri is of redis scheme override the default values and set them
    if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
      host = uri.getHost();
      port = uri.getPort();
      password = uri.getUserInfo().split(":", 2)[1];
      database = Integer.parseInt(uri.getPath().split("/", 2)[1]);
    }

    jedisFactory = new JedisFactory(host, port, timeout, password, database);
    jedisFactory.setPoolObjectsCounter(poolObjectsCounter);
    this.internalPool = new GenericObjectPool(jedisFactory, new Config());
    instrument(null,this.internalPool);
  }

  /**
   * @param uri
   */
  public InstrumentedJedisPool(final URI uri) {
    String h = uri.getHost();
    int port = uri.getPort();
    String password = uri.getUserInfo().split(":", 2)[1];
    int database = Integer.parseInt(uri.getPath().split("/", 2)[1]);
    this.internalPool =
        new GenericObjectPool(new JedisFactory(h, port, Protocol.DEFAULT_TIMEOUT, password,
            database), new Config());
    instrument(null,this.internalPool);
  }

  /**
   * @param poolConfig
   * @param host
   * @param port
   * @param timeout
   * @param password
   */
  public InstrumentedJedisPool(final Config poolConfig, final String host, int port, int timeout,
      final String password, MetricRegistry metricRegistry) {
    this(poolConfig, host, port, timeout, password, Protocol.DEFAULT_DATABASE, metricRegistry);
  }

  /**
   * @param poolConfig
   * @param host
   * @param port
   */
  public InstrumentedJedisPool(final Config poolConfig, final String host, final int port, MetricRegistry metricRegistry) {
    this(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE, metricRegistry);
  }

  /**
   * @param poolConfig
   * @param host
   * @param port
   * @param timeout
   */
  public InstrumentedJedisPool(final Config poolConfig, final String host, final int port,
      final int timeout, MetricRegistry metricRegistry) {
    this(poolConfig, host, port, timeout, null, Protocol.DEFAULT_DATABASE, metricRegistry);
  }

  /**
   * @param poolConfig
   * @param host
   * @param port
   * @param timeout
   * @param password
   * @param database
   */
  public InstrumentedJedisPool(final Config poolConfig, final String host, int port, int timeout,
      final String password, final int database, MetricRegistry metricRegistry) {
    super(poolConfig, new JedisFactory(host, port, timeout, password, database));
    instrument(metricRegistry, this.internalPool);
  }

  /**
   * @param resource
   */
  public void returnBrokenResource(final BinaryJedis resource) {
    returnBrokenResourceObject(resource);
  }

  /**
   * @param resource
   */
  public void returnResource(final BinaryJedis resource) {
    returnResourceObject(resource);
  }

  /**
   * @return the registry
   */
  public MetricRegistry getRegistry() {
    return this.registry;
  }

  /**
   * This would set only if registry is null and has not be set.
   * 
   * @param registry the registry to set
   */
  public void setRegistry(String registryName) {

    this.registry = SharedMetricRegistries.getOrCreate(registryName);

  }

  /**
   * @param pool
   */
  private void instrument(MetricRegistry metricsRegistry, final GenericObjectPool pool) {
    // Assign values to instance scope
    if (null == metricsRegistry) {
      metricsRegistry = SharedMetricRegistries.getOrCreate("default");
    }
    this.registry = metricsRegistry;
    
    final String prefix = name(pool.getClass());

    System.out.println("Before registrying :" + registry.getNames());
    // Start capturing all Gauge metrics
    registry.register(name(prefix, MAX_ACTIVE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return pool.getMaxActive();
      }
    });
    registry.register(name(prefix, MAX_IDLE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return pool.getMaxIdle();
      }
    });
    registry.register(name(prefix, MIN_IDLE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return pool.getMinIdle();
      }
    });
    registry.register(name(prefix, NUM_ACTIVE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return pool.getNumActive();
      }
    });
    registry.register(name(prefix, NUM_IDLE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return pool.getNumIdle();
      }
    });
    registry.register(name(prefix, PERCENT_IDLE), new RatioGauge() {
      @Override
      protected Ratio getRatio() {
        return Ratio.of(pool.getNumIdle(), pool.getNumIdle() + pool.getNumActive());
      }
    });


    // Add metric counter to start counting the number of tasks submitted
    poolObjectsCounter = registry.counter(name(prefix, POOL_OBJECTS_COUNT));
  }

  /**
   * 
   * PoolableObjectFactory custom impl.
   */
  private static class JedisFactory extends BasePoolableObjectFactory {
    private final String host;
    private final int port;
    private final int timeout;
    private final String password;
    private final int database;

    private Counter poolObjectsCounter;

    /**
     * @param host
     * @param port
     * @param timeout
     * @param password
     * @param database
     */
    public JedisFactory(final String host, final int port, final int timeout,
        final String password, final int database) {
      super();
      this.host = host;
      this.port = port;
      this.timeout = timeout;
      this.password = password;
      this.database = database;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
     */
    @Override
    @Timed
    @Metered
    @ExceptionMetered
    public Object makeObject() throws Exception {
      final InstrumentedJedis jedis = new InstrumentedJedis(this.host, this.port, this.timeout);

      try {
        jedis.connect();
      } catch (JedisConnectionException e) {
        System.out.println("Not able to connect to host=" + this.host + " port=" + this.port);
        throw e;
      }
      if (null != this.password) {
        jedis.auth(this.password);
      }
      if (database != 0) {
        jedis.select(database);
      }

      if (null != getPoolObjectsCounter()) {
        getPoolObjectsCounter().inc();
      }
      System.out.println("Checkout jedis Id-" + jedis.hashCode());
      return jedis;
    }

    /*
     * Note: Not sure if this method works {@link
     * JedisFactory#destroyobject} if the client is connected to redis
     * database other than default (db=0). Please check {@link
     * BinaryClient#quit} it has db hard-coded as 0. (non-Javadoc)
     * 
     * @see
     * org.apache.commons.pool.BasePoolableObjectFactory#destroyObject(java
     * .lang.Object)
     */
    @Override
    @Timed
    @Metered
    @ExceptionMetered
    public void destroyObject(final Object obj) throws Exception {
      if (obj instanceof InstrumentedJedis) {
        final InstrumentedJedis jedis = (InstrumentedJedis) obj;
        if (jedis.isConnected()) {
          try {
            try {
              jedis.quit();
            } catch (Exception e) {

            }
            jedis.disconnect();
          } catch (Exception e) {

          } finally {
            if (null != getPoolObjectsCounter()) {
              getPoolObjectsCounter().dec();
            }
            System.out.println("Destroy jedis Id-" + jedis.hashCode());
          }
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.commons.pool.BasePoolableObjectFactory#validateObject(
     * java.lang.Object)
     */
    @Override
    @Timed
    @Metered
    public boolean validateObject(final Object obj) {
      if (obj instanceof InstrumentedJedis) {
        final InstrumentedJedis jedis = (InstrumentedJedis) obj;
        System.out.println("Validate jedis Id-" + jedis.hashCode());
        try {
          return jedis.isConnected() && jedis.ping().equals("PONG");
        } catch (final Exception e) {
          return false;
        }
      } else {
        return false;
      }
    }

    /**
     * @return the poolObjectsCounter
     */
    public Counter getPoolObjectsCounter() {
      return poolObjectsCounter;
    }

    /**
     * @param poolObjectsCounter the poolObjectsCounter to set
     */
    public void setPoolObjectsCounter(Counter poolObjectsCounter) {
      this.poolObjectsCounter = poolObjectsCounter;
    }

  }
}
