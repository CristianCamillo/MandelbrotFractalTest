package p1;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;

public class FeatherEngine implements KeyListener, MouseListener
{
	// Cristian Camillo, 2020-01-17
	
	/*********************************************************************/
	/* Settings                                                          */
	/*********************************************************************/
	
	private int width;
	private int height;
	private String title;
	private int FPSCap;
	private boolean showFPS;
	private boolean fullscreen;
	
	/*********************************************************************/
	/* Graphics                                                          */
	/*********************************************************************/

	private JFrame frame;	
	private Canvas canvas;
	
	private BufferStrategy bs;
	private Graphics g;
	
	private BufferedImage buffer;
	private byte[] frameBuffer;
	
	/*********************************************************************/
	/* Game Loop variables                                               */
	/*********************************************************************/
	
	private boolean running = true;
	
	private long frameDuration = 0;
	private int syncDelay = 500_000;
	
	private long elapsedTime = 1;
	
	private float FPS = 0;
	private long FPSUpdate = 0;
	
	/*********************************************************************/
	/* Keyboard and Mouse Inputs                                         */
	/*********************************************************************/
	
	private final boolean[] key = new boolean[1024];
	private final boolean[] mouse = new boolean[MouseInfo.getNumberOfButtons()];
	
	private final boolean[] keyToggle = new boolean[key.length];
	private final boolean[] mouseToggle = new boolean[mouse.length];
	
	private KeyEvent lastKeyPressed = null;
	private MouseEvent lastMouseInput = null;
	
	/*********************************************************************/
	/* Constants                                                         */
	/*********************************************************************/
	
	private final static int FPS_UPDATE_PERIOD = 500_000_000;
	
	/*********************************************************************/
	/* Constructor                                                       */
	/*********************************************************************/
	
	public FeatherEngine(int width, int height, String title, int FPSCap, boolean showFPS, boolean fullscreen)
	{		
		this.width = width;
		this.height = height;
		this.title = title != null ? title : "";
		setFPSCap(FPSCap);
		this.showFPS = showFPS;
		this.fullscreen = fullscreen;
	}
	
	public void start()
	{
		setSize(width, height, fullscreen);
		
		while(running)
		{
			long start = System.nanoTime();
			
			update(elapsedTime / 1_000_000_000f);
			render(buffer.getGraphics(), frameBuffer);						
			
			if(showFPS)
				frame.setTitle(getTitle() + " - " + String.format("%.2f", getFPS()));
			
			g.drawImage(buffer, 0, 0, null);
			bs.show();
							
			if(FPSCap > 0)
			{
				long timeLeft = frameDuration - (System.nanoTime() - start) - syncDelay;
				
				if(timeLeft > 0)
					try{
						Thread.sleep(timeLeft / 1_000_000, (int)(timeLeft % 1_000_000));
					}catch(InterruptedException e) { System.err.println(e); System.exit(1); }
				
				while(frameDuration > System.nanoTime() - start);
			}
			
			elapsedTime = System.nanoTime() - start;		
		}
	
		frame.dispose();
	}
	
	/*********************************************************************/
	/* Misc                                                              */
	/*********************************************************************/
	
	protected void update(float elapsedTime) {}
	protected void render(Graphics g, byte[] comps) {}

	public final void drawPixel(int x, int y, byte r, byte g, byte b)
	{
		int index = (x + y * width) * 3;	
		
		frameBuffer[index]	 = b;
		frameBuffer[index + 1] = g;
		frameBuffer[index + 2] = r;
	}
	
	public final void stop()
	{
		running = false;
	}
	
	private final boolean toggle(int code, boolean[] baseArray, boolean[] toggleArray)
	{
		if(baseArray[code])
		{
			if(!toggleArray[code])
			{			
				toggleArray[code] = true;							
				return true;
			}
		}
		else
				toggleArray[code] = false;			
		
		return false;
	}
	
	/*********************************************************************/
	/* Getters                                                           */
	/*********************************************************************/
	
	public final int getWidth() 		 	   { return width; }	
	public final int getHeight() 	 	       { return height; }
	public final String getTitle() 		 	   { return title; }
	public final int getFPSCap() 		 	   { return FPSCap; }	
	public final boolean isShowingFPS() 	   { return showFPS; }
	public final boolean isFullscreen() 	   { return fullscreen; }
	public final boolean key(int code) 		   { return key[code]; }	
	public final boolean mouse(int code) 	   { return mouse[code]; }
	public final boolean keyToggle(int code)   { return toggle(code, key, keyToggle); }	
	public final boolean mouseToggle(int code) { return toggle(code, mouse, mouseToggle); }
	public final KeyEvent lastKeyPressed() 	   { return lastKeyPressed; }
	public final MouseEvent lastMouseInput()   { return lastMouseInput; }
	public final int getMouseX() 			   { return (int)(MouseInfo.getPointerInfo().getLocation().getX() - canvas.getLocationOnScreen().getX()); }
	public final int getMouseY() 			   { return (int)(MouseInfo.getPointerInfo().getLocation().getY() - canvas.getLocationOnScreen().getY()); }
	
	public final float getFPS()
	{
		long now = System.nanoTime();
		if(now >= FPSUpdate + FPS_UPDATE_PERIOD)
		{
			FPSUpdate = now;
			FPS = (1_000_000_000f / elapsedTime);
		}
		
		return FPS;
	}	
	
	/*********************************************************************/
	/* Setters                                                           */
	/*********************************************************************/
	
	public final void setShowFPS(boolean showFPS) { this.showFPS = showFPS; }
	
	public final void setSize(int width, int height, boolean fullscreen)
	{	
		if(frame != null)
		{
			if(this.fullscreen && fullscreen || !fullscreen && this.width == width && this.height == height)
				return;
			else
				frame.dispose();
		}		
		
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		
		if(fullscreen)
		{
			this.width = screenWidth;
			this.height = screenHeight;
		}
		else
		{			
			if(width < 1 || height < 1 || width > screenWidth || height > screenHeight)
				throw new IllegalArgumentException("Resolution " + width + "x" + height + " not compatible with the screen resolution.");
			
			this.width = width;
			this.height = height;
		}
		
		this.fullscreen = fullscreen;
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(title);
		frame.setResizable(false);	
		
		canvas = new Canvas();			
		canvas.setSize(this.width, this.height);
		
		frame.add(canvas);
		
		if(fullscreen)
			frame.setUndecorated(true);

		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);
		
		canvas.createBufferStrategy(2);
		
		bs = canvas.getBufferStrategy();
		g = (Graphics)bs.getDrawGraphics();
		
		buffer = new BufferedImage(this.width, this.height, BufferedImage.TYPE_3BYTE_BGR);
		frameBuffer = ((DataBufferByte)buffer.getRaster().getDataBuffer()).getData();
		
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
	}
	
	public final void setTitle(String title)
	{
		this.title = title != null ? title : "";
		frame.setTitle(this.title);
	}	
	
	public final void setFPSCap(int FPSCap)
	{
		if(FPSCap < 0)
		{
			this.FPSCap = 0;
			return;
		}
		
		this.FPSCap = FPSCap;
		if(this.FPSCap > 0)
			frameDuration = (long)(1f / this.FPSCap * 1_000_000_000l);
	}
	
	/*********************************************************************/
	/* Events                                                            */
	/*********************************************************************/

	public final void keyPressed(KeyEvent e)
	{
		lastKeyPressed = e;
		key[e.getKeyCode()] = true;
	}

	public final void keyReleased(KeyEvent e)
	{
		key[e.getKeyCode()] = false;
	}
	
	public final void mousePressed(MouseEvent e)
	{
		lastMouseInput = e;
		mouse[e.getButton()] = true;
	}

	public final void mouseReleased(MouseEvent e)
	{
		mouse[e.getButton()] = false;
	}
	
	public final void keyTyped(KeyEvent e){}
	public final void mouseClicked(MouseEvent e){}
	public final void mouseEntered(MouseEvent e){}
	public final void mouseExited(MouseEvent e){}	
}