package limiter;

import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class TokenBucket extends AbstractLimiter{

	private final ScheduledExecutorService threadPool =
			Executors.newSingleThreadScheduledExecutor();

	public TokenBucket(Integer interval, Integer count){
		super(interval, count);
	}

	@Override
	public boolean check() {
		Jedis client = jedisPool.getResource();
		try {
			String token = client.lpop(getTokenBucketKey());

			if( token == null ){
				System.out.println("rate limited(token bucket)");
				return false;
			} else {
				System.out.println("succeed in getting token: " + token);
				return true;
			}
		} finally {
			client.close();
		}
	}

	@Override
	public void start() {
		//create token and put token to bucket periodically.
		threadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Jedis client = jedisPool.getResource();
				try {
					for (int i = 0; i < getLimitCount(); i++) {
						long capacity = client.rpush(getTokenBucketKey(), createToken());

						//allow a little burst
						if( capacity > getLimitCount() + (getLimitCount() / getInterval()) ){
							client.rpop(getTokenBucketKey());
							System.out.println("token bucket is full: " + (capacity-1));
							break;
						}
					}
				} finally {
					client.close();
				}
			}
		}, 0, getInterval(), TimeUnit.SECONDS);
	}

	private String getTokenBucketKey(){
		return "rate_limiter_token_bucket";
	}

	private String createToken(){
		//use UUID to indicate a request.
		return UUID.randomUUID().toString();
	}
}
