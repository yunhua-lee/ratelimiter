package limiter;

import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class SlideTimeWindow extends AbstractLimiter{

	private final Integer step; //window slide step

	private final ScheduledExecutorService threadPool =
			Executors.newSingleThreadScheduledExecutor();

	public SlideTimeWindow(Integer interval, Integer count){
		super(interval, count);
		step = interval/2;
	}

	@Override
	public boolean check() {
		Jedis client = jedisPool.getResource();
		try{
			Long time = System.currentTimeMillis() / 1000;
			String uuid = getUUID32();

			client.zadd(getWindowKey(), time, uuid);

			Long windowBegin = (time / step - 1) * step;
			Long windowEnd = (time / step + 1) * step;

			Long realCount = client.zcount(getWindowKey(), windowBegin, windowEnd);

			if( realCount > getLimitCount() ){
				System.out.println("rate limited(slide time window), real count: " + realCount +
						", limit count: " + getLimitCount() + ", request: " + uuid);
				return false;
			} else {
				return true;
			}

		} finally {
			client.close();
		}
	}

	@Override
	public void start() {
		threadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Jedis client = jedisPool.getResource();
				try{
					Long time = System.currentTimeMillis() / 1000;
					Long windowLeft = (time / step - 2) * step;

					Long remCount = client.zremrangeByScore(getWindowKey(), 0.0, windowLeft);
					System.out.println("clear unused data in redis, key: " + getWindowKey() +
							", count: " + remCount);
				} finally {
					client.close();
				}
			}
		}, getInterval(), getInterval(), TimeUnit.SECONDS);
	}

	private String getWindowKey(){
		return "rate_limiter_set";
	}

	private String getUUID32(){
		//use UUID to indicate a request.
		return UUID.randomUUID().toString();
	}
}
