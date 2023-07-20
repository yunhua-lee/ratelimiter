package limiter;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
abstract public class AbstractLimiter implements Limiter{

	private final Integer interval;
	private final Integer limitCount;

	protected final JedisPool jedisPool;

	public AbstractLimiter(Integer interval, Integer count){
		this.interval = interval;
		this.limitCount = count;

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(10);
		config.setBlockWhenExhausted(false);

		jedisPool = new JedisPool(config,"127.0.0.1",6379);
	}

	public Integer getInterval() {
		return interval;
	}

	public Integer getLimitCount() {
		return limitCount;
	}
}
