package cello.tablettest;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.applet.*;
import java.lang.reflect.*;

import cello.lui.*;

public final class JTablettest extends Applet
				implements	Runnable {
	private Thread mainThread,loadThread;
	boolean initialized=false;
	boolean error=false;

	Font font;
	FontMetrics fm;
	int fontheight,fontdescent;
	int position=0;
	private static final int max_position = 10;

	float loadrotation = 0f;

	Surface surface;
	private static final String DISTNAME = "JTablet";
	private static final String APPNAMEVERSION = "JTablet Test v0.3";

	String extra = "x";
	String extra2 = "y";

	public void init() {
		surface = new Surface();
		surface.setBackground(lUIDefaults.baseColor);
		surface.setForeground(lUIDefaults.textColor);
		setLayout(new BorderLayout());
		extra = "step 1";
		add("Center",surface);
		extra = "step 2";
		validate();
		extra = "step 3";
		font = new Font("Verdana",Font.BOLD,10);
		extra = "step 4";
		setWaitingCursor();
		extra = "step 5";
	}
	
	lLabel version_label;

	public void initialize() {
		extra = "step 6";

		try {
			extra = "step 7";
			percentincrease();
			extra = "step 8";
			
			lPanel p = new lRootPanel();
			extra = "step 9";
			JTabletSurface jts;
			extra = "step 10";
			p.add(jts = new JTabletSurface(),BorderLayout.CENTER);
			extra = "step 11";
			
			String s = APPNAMEVERSION;
			extra = "step 11.1";
			s += " (plugin v.:"+jts.jtablet_version+")";
			
			extra = "step 11.2";
			version_label = new lLabel(s);
			extra = "step 11.3";
			p.add(version_label,BorderLayout.NORTH);
			extra = "step 12";
			
			
			
			remove(surface);
			extra = "step 13";
			add(p,BorderLayout.CENTER);
			extra = "step 14";
			
			setBackground(lUIDefaults.baseColor);
			extra = "step 15";
			
			validate();
			extra = "step 16";
			
			percentincrease();
			extra = "step 17";
			loadThread = null;
			extra = "step 18";
			initialized=true;
			extra = "step 19";

		} catch (Throwable e) { 
			extra2 = "error A=" + e.toString();
			e.printStackTrace(); 
			extra2 = "error B=" + e.toString();
			error=true; 
			extra2 = "error C=" + e.toString();
			repaint(); 
			extra2 = "error D=" + e.toString();
		}
		setNormalCursor();
	}

	void percentincrease() {
		position++;
		surface.repaint();
		try { Thread.sleep(10); } catch (Exception e) {}
	}
	public void update(Graphics g) {
		paint(g);
	}
	public void paint(Graphics g) {
		super.paint(g);
		if (surface!=null) { surface.repaint(); return; }
		paintload(g);
	}
	public void paintload(Graphics g) {
		int swidth = getSize().width;
		int sheight = getSize().height;
		g.setColor(lUIDefaults.baseColor);
		g.fillRect(0,0,swidth,sheight);
		if (!initialized) {
			if (fm==null) {
				fm = g.getFontMetrics(font);
				fontheight = fm.getHeight();
				fontdescent = fm.getDescent();
			}
			g.setFont(font);
			g.setColor(lUIDefaults.textColor);
			
			String string = (error?"error initializing!":"loading...") + "{" + extra + "," + extra2 + "}";
			int width = fm.stringWidth(string);
			int ystart = (sheight-(fontheight+8))>>1;
			float xd = (float)Math.cos(loadrotation)*(swidth>>1);
			float yd =  (float)Math.sin(loadrotation)*(sheight>>2);
			int x = (int)(xd * 0.877583f - yd * 0.479426f)+swidth>>1;
			int y = (int)(xd * 0.479426f + yd * 0.877583f)+sheight>>1;
			if (yd<0) {
				g.setColor(lUIDefaults.captionColor);
				g.fillOval(x-4,y-4,8,8);
				g.setColor(lUIDefaults.textColor);
				g.drawOval(x-4,y-4,8,8);
			}
			g.drawRect(swidth>>2,ystart+fontheight-3,swidth>>1,6);
			g.drawString(string,(swidth-width)>>1,ystart+fontdescent);
			
			g.setColor(lUIDefaults.captionColor);
			g.fillRect((swidth>>2)+1,ystart+fontheight-2,position*((swidth>>1)-1)/max_position,5);
			if (yd>=0) {
				g.setColor(lUIDefaults.captionColor);
				g.fillOval(x-4,y-4,8,8);
				g.setColor(lUIDefaults.textColor);
				g.drawOval(x-4,y-4,8,8);
			}
			g.setColor(lUIDefaults.textColor);
			g.drawString(DISTNAME,swidth-fm.stringWidth(DISTNAME)-4,sheight-fontdescent-fontheight);
			g.drawString(APPNAMEVERSION,swidth-fm.stringWidth(APPNAMEVERSION)-4,sheight-fontdescent);
		}
	}

	public void start() {
		if (mainThread == null) mainThread = new Thread(this);
		if (loadThread == null) loadThread = new Thread(this);
		loadThread.start();
		mainThread.start();
	}

	public void stop() {
		mainThread = null;
		loadThread = null;
	}









	public void run() {
		long lastTime = System.currentTimeMillis();
		long lastTime2 = lastTime;
		float time = 0;
		Thread currentThread = Thread.currentThread();
		if (currentThread==mainThread) {
			if (!initialized) initialize();
		}
		while (currentThread==loadThread) {
			time = (float)(System.currentTimeMillis() - lastTime2)/50;
			lastTime2 = System.currentTimeMillis();
			loadrotation+=0.1f*time;
			surface.repaint();
			repaint();
			Thread.yield();
		}
	}
	class Surface extends Canvas {
		Graphics offGraphics;
		Dimension offDimension;
		Image offImage;
		public Dimension getPreferredSize() {
			return getSize();
		}
		public Dimension getMinimumSize() {
			return new Dimension(30, 30);
		}

		public void paint(Graphics g) {
			this.update(g);
		}
		public void update(Graphics g) {
			Dimension size = getSize();
			if ( (offGraphics == null)
			  || (size.width != offDimension.width)
			  || (size.height != offDimension.height) ) {
				offDimension = size;
				offImage = createImage(size.width, size.height);
				offGraphics = offImage.getGraphics();
			}
			paintload(offGraphics);
			if (initialized&&!error) {
				offGraphics.setFont(font);
				offGraphics.setColor(Color.gray);
			}
			g.drawImage(offImage,0,0,this);
		}
	}

	

	public Dimension getPreferredSize() {
		return getSize();
	}
	private void setMovingCursor() { surface.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); }
	private void setWaitingCursor() { surface.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); }
	private void setNormalCursor() { surface.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); }
	private void setHandCursor() { surface.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
	public static void main(String args[]) {
		Frame frame = new Frame(APPNAMEVERSION);
		JTablettest app = new JTablettest();
		frame.add(app, BorderLayout.CENTER);
		app.init();
		app.start();
		frame.addWindowListener(new WindowAdapter() {
			void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.setSize(new Dimension(400,300));
		frame.show();
	}
}


class JTabletSurface extends lComponent {

	private MemoryImageSource mis;
	IndexColorModel icm;
	byte[] pixels;
	Image memimg;
	int lastSize_height = 0;
	int value = 0;
	
	protected boolean pressed=false;
	
	Object  tablet;
	Method tablet_poll,tablet_getPressure,tablet_getPressureExtent,tablet_getAngle,tablet_getOrientation,
	//tablet_getX,tablet_getY,tablet_getZ,
	tablet_getButtons;
	
	String error = null;
	
	boolean tablet_driver_installed=false;
	
	String jtablet_version = "uninitialized";
	
	protected void initialize_tablet() {
try {
	// Get a class object through Reflection API
	Class jtablet = Class.forName("cello.tablet.JTablet");
	// Create an instance of the object
	tablet = jtablet.newInstance();
	// Find functions
	// poll()
	tablet_poll = jtablet.getMethod("poll",null);
	// getPressure()
	tablet_getPressure = jtablet.getMethod("getPressure",null);
	// getAngle()
	tablet_getAngle = jtablet.getMethod("getAngle",null);
	// getPressureExtent()
	tablet_getPressureExtent = jtablet.getMethod("getPressureExtent",null);
	// getOrientation()
	tablet_getOrientation = jtablet.getMethod("getOrientation",null);
	//tablet_getX = jtablet.getMethod("getX",null);
	//tablet_getY = jtablet.getMethod("getY",null);
	//tablet_getZ = jtablet.getMethod("getZ",null);
	tablet_getButtons = jtablet.getMethod("getButtons",null);

	// Assume the driver is installed if we got this far
	tablet_driver_installed=true;
	try {
		// getVersion() was added in 0.2 BETA 2
		// You can safely assume this function will exist, since the 0.1 BETA
		// was distributed to a select group of users.
		// If the user is using 0.1 BETA, you aren't required to support them
		// so you can simply recommend an upgrade.
		Method tablet_getVersion = jtablet.getMethod("getVersion",null);

		// Invoke function
		jtablet_version = (String)tablet_getVersion.invoke(tablet,null);
	} catch (Exception e) {
		// If the method doesn't exist, they are using the old 0.1 beta version
		jtablet_version = "old 0.1 beta";
	}
// Print out errors based on exception, this is more for debugging purposes
// than anything else.
} catch (ClassNotFoundException e) {
	error = "JTablet class not found - Did you install JTabletSetup.exe?";
	e.printStackTrace();
} catch (ExceptionInInitializerError e) {
	error = "JTablet Initialization error";
	e.printStackTrace();
} catch (InstantiationException e) {
	error = "JTablet Instantiation error";
	e.printStackTrace();

} catch (IllegalAccessException e) {
	error = "JTablet Illegal Access error";
	e.printStackTrace();
} catch (SecurityException e) {
	error = "JTablet Security error";
	e.printStackTrace();
} catch (LinkageError e) {
	error = "JTablet Link error";
	e.printStackTrace();
} catch (Exception e) {
	error = "JTablet error:"+e.getMessage();
	e.printStackTrace();
}
if (!tablet_driver_installed)
	jtablet_version = "couldn't load";
}
	int freehand_pressure = 0xFF,last_freehand_pressure;
	String tablet_data = null;
protected void read_tablet() {
	if (!tablet_driver_installed) return;
	try {
		// Poll the tablet and check the return value
		if (((Boolean)tablet_poll.invoke(tablet,null)).booleanValue() ) {
			// Get orientation
			int orientation = ((Integer)tablet_getOrientation.invoke(tablet,null)).intValue();
			// Get angle
			int tablet_angle = ((Integer)tablet_getAngle.invoke(tablet,null)).intValue();
			tablet_data = "ori="+orientation+" ang="+tablet_angle;
			// If the orientation is negative, we're erasing
			erase = orientation<0;
			// Get Pressure
			freehand_pressure = ((Integer)tablet_getPressure.invoke(tablet,null)).intValue();
			tablet_data += " pre="+freehand_pressure;
			// Factor pressure
			freehand_pressure *= 0xFF;
			// Get Pressure extent
			int extent = ((Integer)tablet_getPressureExtent.invoke(tablet,null)).intValue();
			tablet_data += " ext="+extent;
			//int a= ((Integer)tablet_getX.invoke(tablet,null)).intValue();
			//tablet_data += " x="+a;
			//a= ((Integer)tablet_getY.invoke(tablet,null)).intValue();
			//tablet_data += " y="+a;
			//a= ((Integer)tablet_getZ.invoke(tablet,null)).intValue();
			//tablet_data += " z="+a;
			int a= ((Integer)tablet_getButtons.invoke(tablet,null)).intValue();
			tablet_data += " buttons="+a;

			// Divide extent
			if (extent>0) freehand_pressure/=extent;
			// Clip pressure (this should never be necessary unless something is wrong)
			if (freehand_pressure<0) freehand_pressure=0;
			if (freehand_pressure>0xFF) freehand_pressure=0xFF;
		}
	} catch (Exception e) {
		error = "Error reading tablet";
		e.printStackTrace();
	}
}
	public JTabletSurface() {
		initialize_tablet();
		makeBrush(5,5);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	public Dimension getMinimumSize() {
		return new Dimension(15,15);
	}
	Dimension lastSize;
	int imgw=0,imgh=0;
	protected void regenerateMIS() {
		if (lastSize==null) return;
		int w=lastSize.width, h=lastSize.height;
		imgw=w;imgh=h;
		if (w*h<=0) return;
		pixels = new byte[w*h];
		setupICM();
		mis = new MemoryImageSource(w, h, icm, pixels, 0, w);
		mis.setAnimated(true);
		memimg = createImage(mis);
	}
	boolean erase=false;
	
	
	protected void setupICM() {
		if (icm!=null) return;

		byte s1[] = new byte[256];
		s1[0] = (byte)0xFF;
		icm = new IndexColorModel(8,256,s1,s1,s1);
	}
	protected Rectangle lastRect=null;

	public void paintContents(Graphics g, Dimension size) {
		Dimension size2 = new Dimension(size.width,size.height);
		if (lastSize==null||!size2.equals(lastSize)) {
			lastSize=size2;
			regenerateMIS();
		}
		Rectangle clipB = g.getClipBounds();
		
		drawClippedScaledImage(g, clipB, memimg, size.width, size.height, size.width, size.height);
		
		if (error!=null||tablet_data!=null) {
			g.setFont(getFont());
			g.setColor(Color.black);
			if (error!=null) g.drawString(error,0,size.height-5);
			if (tablet_data!=null) g.drawString(tablet_data,0,size.height-20);
		}
	}
	private void pressure_brush() {
		read_tablet();
		if (freehand_pressure!=last_freehand_pressure) {
			int i = erase? 30 : 20;
			if (freehand_pressure==0xFF) {
				makeBrush(i,i);
			} else {
				int w=i;
				w=(w*freehand_pressure)>>8;
				if (w<1) w=1;
				makeBrush(w,w);
			}
		}
		last_freehand_pressure=freehand_pressure;
		
	}
	int lastx,lasty,mousex,mousey;
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (enabled)
		switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				lastx=e.getX();
				lasty=e.getY();
				last_freehand_pressure=freehand_pressure=0xFF;
				pressure_brush();
				break;
			case MouseEvent.MOUSE_RELEASED:
				break;
		}
	}
	public void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);
		if (enabled)
		switch (e.getID()) {
			case MouseEvent.MOUSE_MOVED:
				pressure_brush();
				break;
			case MouseEvent.MOUSE_DRAGGED:
				mousex=e.getX();
				mousey=e.getY();
				pressure_brush();
				Rectangle r = line(mousex, mousey, lastx, lasty);
				if (r!=null)
					mis.newPixels(r.x,r.y,r.width,r.height);
				lastx=mousex;
				lasty=mousey;
				break;
		}
	}
	public byte[] shape={ 1 };
	public int width=1;
	public int height=1;
	public int halfwidth=0;
	public int halfheight=0;
	
	private final void makeBrush(int width, int height) {
		this.width=width;
		this.height=height;
		this.halfwidth=width>>1;
		this.halfheight=height>>1;
		if (this.width*this.height==1) {
			shape = new byte[]{1};
			return;
		}
		
		float rx=width/2.0f,ry=height/2.0f;

		float y;
		float x;
		
		shape = new byte[width*height];

		for(int yy=this.height; --yy>=0;) {
			y=yy-ry+0.5f;
			for(int xx=this.width; --xx>=0;) {
				x=xx-rx+0.5f;
				if (y*y*rx/ry + x*x*ry/rx - rx*ry + 0.5f <= 0)
					shape[xx+yy*this.width]=1;
			}
		}
	}
	
	private final void drawPixel(final int x, final int y) {
		if (x<0||y<0||x>=imgw||y>=imgh) return;
		pixels[x+y*imgw]=(byte)(erase?0:1);
	}
	public final boolean draw(int x, int y) {
		if (width==1&&height==1) {
			drawPixel(x,y);
			return true;
		}
		x-=halfwidth;
		y-=halfheight;
		if (x+width<0||y+height<0||x>imgw||y>imgh) return false;
		for(int yy=height; --yy>=0;)
			for(int xx=width; --xx>=0;)
				if (shape[xx+yy*width]!=0) drawPixel(xx+x,yy+y);
		return true;
	}

	public final Rectangle drawR(int x, int y) {
		if (draw(x,y))
			return new Rectangle(x-halfwidth,y-halfheight,width,height);
		return null;
	}

	public final Rectangle line(int ax, int ay, int bx, int by) {
		int x,y;
		if (ax==bx && ay==by) {
			return drawR(ax,ay);
		} else {
			Rectangle r,r2,rt;
			r2=drawR(ax,ay);
				
			// basically checks if we drew the 'first pixel' in the group
			r=null;

			// These should allow us to find the visible end-points of the line
			// even if the real end-points aren't visible
			boolean b1 = r==null;
			boolean b2 = r2==null;
			/*
			* the following line drawing routine
			* essentially works by finding the slope
			* in a high-level integer (16 bits up)
			* 
			* the x/y position is incremented by 1
			* while the opposite dimension is incremented
			* by the slope
			*
			* the slope is then reduced back to a regular
			* integer (x+0x8000)>>16 (the addition is to
			* round the calculation and make everyone happier)
			*/
			int dx=Math.abs(ax-bx), dy=Math.abs(ay-by),t;
			boolean flip=false;
			if (dx>dy) {
				// swap values
				if (ax>bx){ t=ax; ax=bx; bx=t; t=ay; ay=by; by=t; flip=true; }
				// calculate y slope (if it exists)
				if (dx>0) dy=((by-ay)<<16)/dx;
				// shift y position to high-level integer
				y=ay<<16;
				// we skip the first and last pixels due to initial checks
				for(x=ax+1; x<bx; x++) {
					y+=dy;
					rt=drawR(x,(y+0x8000)>>16);
					if (flip) {
						if (r==null) r=rt;
						if (b2 && rt!=null) r2=rt;
					} else {
						if (r2==null) r2=rt;
						if (b1 && rt!=null) r=rt;
					}
				}
			} else {
				// swap values
				if (ay>by){ t=ax; ax=bx; bx=t; t=ay; ay=by; by=t; flip=true; }
				// calculate x slope (if it exists)
				if (dy>0) dx=((bx-ax)<<16)/dy;
				// shift x position to high-level integer
				x=ax<<16;
				// we skip the first and last pixels due to initial checks
				for(y=ay+1; y<by; y++) {
					x+=dx;
					rt=drawR((x+0x8000)>>16,y);
					if (flip) {
						if (r==null) r=rt;
						if (b2 && rt!=null) r2=rt;
					} else {
						if (r2==null) r2=rt;
						if (b1 && rt!=null) r=rt;
					}
				}
			}
			if (r==null) return r2;
			if (r2==null) return r;
			return r.union(r2);
		}
	}


}