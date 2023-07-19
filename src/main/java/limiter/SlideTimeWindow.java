package limiter;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class SlideTimeWindow extends AbstractLimiter{

	public SlideTimeWindow(Integer interval, Integer count){
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
