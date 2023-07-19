package limiter;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class TokenBucket extends AbstractLimiter{

	public TokenBucket(Integer interval, Integer count){
		super(interval, count);
	}

	@Override
	public boolean check() {
		return false;
	}

	@Override
	public void start() {

	}
}
