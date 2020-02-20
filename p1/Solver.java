package p1;

public class Solver implements Runnable
{
	private int xStart;
	private int yStart;
	private int xEnd;
	private int yEnd;
	private int width;
	
	private float[] reals;
	private float[] imags;
	
	private float[] buffer;
	
	private boolean running;	
	private boolean updated;
	
	private volatile boolean abort;
	
	public Solver()
	{
		running = false;
		updated = false;
		abort = false;
	}
		
	public boolean isUpdated() { return updated; }
	public boolean isRunning() { return running; }
	
	public void setData(int xStart, int yStart, int xEnd, int yEnd, int width, float[] reals, float[] imags, float[] buffer)
	{
		abort = true;
		
		synchronized(this)
		{
			this.xStart = xStart;
			this.yStart = yStart;
			this.xEnd = xEnd;
			this.yEnd = yEnd;
			this.width = width;
		
			this.reals = reals;
			this.imags = imags;		
			
			this.buffer = buffer;
			
			updated = false;
			abort = false;
		}
	}
	
	public void stop() { running = false; }
	
	@Override
	public void run()
	{
		running = true;
		
		while(running)
		{
			if(updated)
				try{
					Thread.sleep(1);
				}catch(Exception e) {}
			else
				synchronized(this)
				{
					int x = 0, y = 0;
					for(y = yStart; y < yEnd; y++)
						for(x = xStart; x < xEnd; x++)
							if(!abort)
								buffer[x + y * width] = mandelbrot(reals[x], imags[y], 100);
							else
							{
								x = xEnd;
								y = yEnd;								
							}
					
					updated = true;
				}
		}
	}
	
	private int mandelbrot(float real, float imag, int maxIter)
	{
		Complex c = new Complex(real, imag);
		Complex z = new Complex(0, 0);
		
		for(int i = 0; i < maxIter; i++)
		{
			z = z.times(z).plus(c);
			if(z.re() * z.re() + z.im() * z.im() >= 4)
				return i;
		}
		
		return maxIter;
	}
}