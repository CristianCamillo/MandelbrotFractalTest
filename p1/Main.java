package p1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public final class Main
{	
	private final int width = 1600;
	private final int height = 900;
	private final String title = "Mandlebrot Fractal";
	private final int FPSCap = 60;
	private final boolean showFPS = true;
	private final boolean fullscreen = false;
		
	private float[] buffer;
	private float[] toDraw;
	private boolean valid = false;
	
	private float x = -0.5f;
	private float y = 0f;
	private float zoom = 1f;
	
	private Solver s1;
	private Solver s2;
	private Solver s3;
	private Solver s4;
	
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
				
				float checkZoom = zoom;
				float checkX = x;
				float checkY = y;
				
				if(key(KeyEvent.VK_W))
					zoom += elapsedTime * zoom;
				if(key(KeyEvent.VK_S))
					zoom -= elapsedTime * zoom;
				
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
				g.setColor(Color.WHITE);
				g.fillRect(- getWidth() / 2, - getHeight() / 2, getWidth(), getHeight());
				
				if(!valid)
				{
					buffer = new float[getWidth() * getHeight()];
				
					float ratio = getWidth() * 1f / getHeight();
					
					float[] reals = linearSpace(- ratio / zoom + x, ratio / zoom + x, getWidth());
					
					float[] imags = linearSpace(-1f / zoom + y, 1f / zoom + y, getHeight());
					
					s1.setData(0, 			   0, 			    getWidth() / 2, getHeight() / 2, getWidth(), reals, imags, buffer);
					s2.setData(getWidth() / 2, 0, 			    getWidth(),     getHeight() / 2, getWidth(), reals, imags, buffer);
					s3.setData(0, 			   getHeight() / 2, getWidth() / 2, getHeight(),     getWidth(), reals, imags, buffer);
					s4.setData(getWidth() / 2, getHeight() / 2, getWidth(),     getHeight(),     getWidth(), reals, imags, buffer);

					// remove if enabling multithread
//					for(int y = 0; y < getHeight(); y++)
	//					for(int x = 0; x < getWidth(); x++)
		//					toDraw[x + y * getWidth()] = mandelbrot(reals[x], imags[y], 100);
					
					valid = true;
				}
				
				
				// use if you don't want the frame chopped
			//	if(s1.isUpdated() && s2.isUpdated() && s3.isUpdated() && s4.isUpdated())
					toDraw = buffer;
				
				for(int y = 0; y < getHeight(); y++)
					for(int x = 0; x < getWidth(); x++)
					{						
						int value = (int)(normalize(toDraw[x + y * getWidth()], 100) * 255 + 0.5f);
						drawPixel(x, y, (byte)((value >> 16)&0xff), (byte)((value >> 8)&0xff), (byte)(value&0xff));
					}				
			}			
		};
		
		toDraw = new float[fe.getWidth() * fe.getHeight()];
		
		s1 = new Solver();
		s2 = new Solver();
		s3 = new Solver();
		s4 = new Solver();
		
		Thread t1 = new Thread(s1);
		Thread t2 = new Thread(s2);
		Thread t3 = new Thread(s3);
		Thread t4 = new Thread(s4);		
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
						
		fe.start();
		
		s1.stop();
		s2.stop();
		s3.stop();
		s4.stop();
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
	
	private float[] linearSpace(float start, float end, int intervals)
	{
		if(intervals == 0)
			throw new IllegalArgumentException("Number of intervals cannot be 0");
		
		float[] result = new float[intervals];
		
		float step = (end - start) / (intervals - 1);
		
		for(int i = 0; i < intervals; i++)
			result[i] = start + i * step;
		
		return result;
	}
	
	private float normalize(float value, float maxValue)
	{
		return value / maxValue;
	}
}
