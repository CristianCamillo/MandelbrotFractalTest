package p1;

public class Solver implements Runnable
{
	private int xStart;
	private int yStart;
	private int xEnd;
	private int yEnd;
	private int width;
	
	private double[] reals;
	private double[] imags;
	
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
	
	public void setData(int xStart, int yStart, int xEnd, int yEnd, int width, double[] reals, double[] imags, float[] buffer)
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
								buffer[x + y * width] = mandelbrot(reals[x], imags[y], 255);
							else
							{
								x = xEnd;
								y = yEnd;
							}
					
					updated = true;
				}
		}
	}
	
	private static int mandelbrot(double real, double imag, int maxIter)
	{
		double cReal = real;
		double cImag = imag;
		
		double zReal = 0;
		double zImag = 0;
		
		for(int i = 0; i < maxIter; i++)
		{
			double newReal = zReal * zReal - zImag * zImag + cReal;
			double newImag = zReal * zImag + zImag * zReal + cImag;
			
			zReal = newReal;
			zImag = newImag;
			
			if(zReal * zReal + zImag * zImag >= 4)
			{
				//if(i > 100)
				//	System.out.println(i);
				return i;			
			}
		}
		
		return maxIter;
	}
}
