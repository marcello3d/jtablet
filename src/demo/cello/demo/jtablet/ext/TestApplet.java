package cello.demo.jtablet.ext;

import javax.swing.JApplet;

/**
 * @author marcello
 *
 */
public class TestApplet extends JApplet {
	@Override
	public void init() {
		System.out.println("OtherClass.getString() = "+OtherClass.getString());
		try {
			System.out.println("Have ExtClass = "+Class.forName("cello.demo.jtablet.ext.ExtClass").toString());
		} catch (ClassNotFoundException e) {
			System.out.println("Don't have ExtClass");
		}
		try {
			System.out.println("Have ExtClass = "+Class.forName("cello.demo.jtablet.ext.ExtClass").toString());
		} catch (ClassNotFoundException e) {
			System.out.println("Don't have ExtClass");
		}
		try {
			System.out.println("Have ExtClass = "+Class.forName("cello.demo.jtablet.ext.ExtClass").toString());
		} catch (ClassNotFoundException e) {
			System.out.println("Don't have ExtClass");
		}
//		System.out.println("UnusedClass.getString() = "+ExtLoadingClass.getString());
	}
	public static void main(String args[]) {
		new TestApplet().init();
	}
}
