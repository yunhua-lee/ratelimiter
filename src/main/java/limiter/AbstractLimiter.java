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
	private final Integer count;

	protected final JedisPool pool;

	public AbstractLimiter(Integer interval, Integer count){
		this.interval = interval;
		this. count = count;

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(10);
		config.setBlockWhenExhausted(false);

		pool = new JedisPool(config,"127.0.0.1",6379);
	}

	public Integer getInterval() {
		return interval;
	}

	public Integer getCount() {
		return count;
	}
}
