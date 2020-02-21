package p1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public final class Main
{	
	private final int width = 1600;
	private final int height = 900;
	private final String title = "Mandlebrot Fractal";
	private final int FPSCap = 60;
	private final boolean showFPS = true;
	private final boolean fullscreen = false;
		
	private int[] buffer;
	private int[] toDraw;
	private boolean valid = false;
	
	private double x = -0.5f;
	private double y = 0f;
	private double zoom = 1f;
	
	private Solver[] s;
	
	private final static int N_SOLVER = 4;
	
	private ThreadPoolExecutor executor;
	
	public Main()
	{						
		FeatherEngine fe = new FeatherEngine(width, height, title, FPSCap, showFPS, fullscreen)
		{
			@Override
			public void update(float elapsedTime)
			{
				if(key(KeyEvent.VK_ESCAPE))
					stop();
				
				if(keyToggle(KeyEvent.VK_F1))
					setFPSCap(getFPSCap() == 0 ? FPSCap : 0);
				
				if(keyToggle(KeyEvent.VK_F2))
				{
					setSize(width, height, !isFullscreen());
					valid = false;
				}
				
				double checkZoom = zoom;
				double checkX = x;
				double checkY = y;
				
				if(key(KeyEvent.VK_W))
					zoom += elapsedTime * zoom;
				if(key(KeyEvent.VK_S))
				{
					zoom -= elapsedTime * zoom;
					if(zoom <= 0.1f)
						zoom = 0.1f;
				}
				
				if(key(KeyEvent.VK_UP))
					y -= elapsedTime / zoom;
				if(key(KeyEvent.VK_DOWN))
					y += elapsedTime / zoom;
				
				if(key(KeyEvent.VK_LEFT))
					x -= elapsedTime / zoom;
				if(key(KeyEvent.VK_RIGHT))
					x += elapsedTime / zoom;
				
				if(checkZoom != zoom || checkX != x || checkY != y)
					valid = false;
			}
			
			@Override
			public void render(Graphics g, byte[] frameBuffer)
			{				
				if(!valid)
				{
					if(buffer.length != getWidth() * getHeight())
						buffer = new int[getWidth() * getHeight()];
				
					double ratio = getWidth() * 1.0 / getHeight();

					double[] reals = linearSpace(- ratio * 1.0 / zoom + x, ratio * 1.0 / zoom + x, getWidth());					
					double[] imags = linearSpace(- 1.0 / zoom + y, 1f / zoom + y, getHeight());
			
					for(int i = 0; i < N_SOLVER; i++)
					{
						int height = getHeight() / N_SOLVER;
						s[i].setData(0, height * i, getWidth(), height * (i + 1), getWidth(), reals, imags, buffer);
					}
					
					
				/*	SimpleSolver ss1 = new SimpleSolver(0, 			    0, 			     getWidth() / 2, getHeight() / 2, getWidth(), reals, imags, buffer);
					SimpleSolver ss2 = new SimpleSolver(getWidth() / 2, 0, 			     getWidth(),     getHeight() / 2, getWidth(), reals, imags, buffer);
					SimpleSolver ss3 = new SimpleSolver(0, 			    getHeight() / 2, getWidth() / 2, getHeight(),     getWidth(), reals, imags, buffer);
					SimpleSolver ss4 = new SimpleSolver(getWidth() / 2, getHeight() / 2, getWidth(),     getHeight(),     getWidth(), reals, imags, buffer);
					
					executor.execute(ss1);
					executor.execute(ss2);
					executor.execute(ss3);
					executor.execute(ss4);*/
					
					valid = true;
				}
								
				
				for(int y = 0; y < getHeight(); y++)
					for(int x = 0; x < getWidth(); x++)
					{						
						int value = buffer[x + y * getWidth()];
						drawPixel(x, y, (byte)((value >> 16)&0xff), (byte)((value >> 8)&0xff), (byte)(value&0xff));
					}
				
				g.setColor(Color.RED);
				g.drawString(getFPS() + "", 10, 20);
				
				g.drawString("x = " + x, 10, 40);
				g.drawString("y = " + y, 10, 60);
				g.drawString("zoom = " + zoom, 10, 80);
			}			
		};
		
		/*executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		executor.allowCoreThreadTimeOut(false);
		executor.*/
		
		buffer = new int[fe.getWidth() * fe.getHeight()];
		
		s = new Solver[N_SOLVER];
		for(int i = 0; i < N_SOLVER; i++)
			s[i] = new Solver();
		
		Thread[] t = new Thread[N_SOLVER];
		for(int i = 0; i < N_SOLVER; i++)
			t[i] = new Thread(s[i]);
		
		for(int i = 0; i < N_SOLVER; i++)
			t[i].start();
						
		fe.start();
		
		for(int i = 0; i < N_SOLVER; i++)
			s[i].stop();
		
		//executor.shutdown();
	}
	
	private static double[] linearSpace(double start, double end, int intervals)
	{
		if(intervals == 0)
			throw new IllegalArgumentException("Number of intervals cannot be 0");
		
		double[] result = new double[intervals];
		
		double step = (end - start) / (intervals - 1);
		
		for(int i = 0; i < intervals; i++)
			result[i] = start + i * step;
		
		return result;
	}
}
