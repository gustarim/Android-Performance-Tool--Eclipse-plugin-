package ch.usi.inf.gustarim.apt.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

public class PerformanceToolTest extends Activity {
	
	//test for private outer class fields/methods access from inner
	protected final class Foo {
		public Foo() {
			//should be found as private in the outer
			aList.add("to outer list from foo");
			bar();
			bar(7);
			bar(2, 4f);
			something();
		}
		
		private void something() {
			//should be found as private in the outer
			anOtherList.add(new Float(3));
			bar();
			bar(7);
			bar(2, 4f);
		}
	}
	
	protected final class Foo2 {
		public Foo2() {
			//should be found as private in the outer
			aList.add("to outer list from foo2");
			bar();
			bar(7);
			bar(2, 4f);
			something();
		}
		
		private void something() {
			//should be found as private in the outer
			anOtherList.add(new Float(3));
			bar();
			bar(7);
			bar(2, 4f);
			//enum test
	        int one = SomeEnum.ONE.ordinal();
	        SomeEnum two = SomeEnum.TWO;
	        int a = EnumTest.A.ordinal();
	        EnumTest b = EnumTest.B;
	        setContentView(R.layout.main);
		}
	}
	
	//test for enum
	public enum SomeEnum {ONE, TWO, THREE}
	//test for constants
	static int intVal = 42;
	static final String strVal = "Hello, world!";
	static final String[] strArr = {"one", "two", "three"};
	static String strVal2 = "Other Hello, world! Not the final one..";
	
	private static int sAnInt;
	private int anInt;
	private Object[] anArray;
	private final List<String> aList;
	private final LinkedList<Float> anOtherList;
	
	public PerformanceToolTest() {
		aList = new LinkedList<String>();
		anOtherList = new LinkedList<Float>();
		sAnInt = 0;
		anInt = 0;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //test for invokeinterface call
        aList.add("bla");//interface
        anOtherList.add(new Float(5));//virtual
        //test floats
        Float f = new Float(7.5f);
        float f2 = 3.6f;
        float f3 = f + f2;
        f = f3 - 0.9f;
        //test internal get and set
        //object
        getAnInt();
        setAnInt(1);
        getAnArray();
        setAnArray(new Object[3]);
        //static
        getsAnInt();
        setsAnInt(1);
        //enum test
        int one = SomeEnum.ONE.ordinal();
        SomeEnum two = SomeEnum.TWO;
        int a = EnumTest.A.ordinal();
        EnumTest b = EnumTest.B;
        setContentView(R.layout.main);
        //methods that are not static but they must be
        shouldBeStatic(392);
        shouldBeStaticToo();
        //test foreach loops
        foreachWithArray();
        foreachWithArrayList();
        foreachWithLinkedList();
        //can be a false positive for for loops analysis
        iterationWithExplicitIterator();
    }
    
    
    //methods for multiple tests
    public int getAnInt() {
		return anInt;
	}
    
    public void setAnInt(final int anInt) {
		this.anInt = anInt;
	}
    
    public Object[] getAnArray() {
		return anArray;
	}
    
    public void setAnArray(Object[] anArray) {
		this.anArray = anArray;
	}
    
    public static int getsAnInt() {
		return sAnInt;
	}
    
    public static void setsAnInt(int sAnInt) {
		PerformanceToolTest.sAnInt = sAnInt;
	}
    
    protected void bar() {
    	
    }
    
    private void bar(final int i) {
    	
    }
    
    private void bar(final int i, final Float f) {
    	
    }
    
    private int shouldBeStatic(final int i) {
    	int a = 10;
    	int b = i * 2;
    	int c = b + a;
    	return c;
    }
    
    private int shouldBeStaticToo() {
    	return 1343;
    }
    
    private void foreachWithArray() {
    	final int[] array = new int[100];
    	for (int i : array) {
			i++;
		}
    }
    
    private void foreachWithLinkedList() {
    	final LinkedList<Integer> linked = new LinkedList<Integer>();
    	for (Integer i : linked) {
			i++;
		}
    }
    
    private void foreachWithArrayList() {
    	final ArrayList<Integer> linked = new ArrayList<Integer>();
    	for (Integer i : linked) {
			i++;
		}
    }
    
    private void iterationWithExplicitIterator() {
    	final ArrayList<Integer> linked = new ArrayList<Integer>();
    	final Iterator<Integer> iterator = linked.iterator();
    	while(iterator.hasNext()) {
    		iterator.next();
    	}
    }
}