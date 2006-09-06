package org.jmock.test.unit.lib;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsSame;
import org.jmock.core.Invocation;
import org.jmock.lib.InvocationExpectation;
import org.jmock.test.unit.support.MethodFactory;
import org.jmock.test.unit.support.MockAction;


public class InvocationExpectationTests extends TestCase {
	MethodFactory methodFactory = new MethodFactory();
	InvocationExpectation expectation = new InvocationExpectation();
	Object targetObject = "targetObject";
	Method method = methodFactory.newMethod("method");
	
	public <T> Matcher<T> mockMatcher(final T expected, final boolean result) {
		return new Matcher<T>() {
			public boolean match(T actual) {
				assertTrue(
					"expected " + expected + ", was " + actual,
					IsEqual.eq(expected).match(actual));
				return result;
			}
			public void describeTo(Description description) {
			}
		};
	}
	
	public void testMatchesAnythingByDefault() {
		assertTrue("should match", expectation.matches(
				new Invocation(new Object(), methodFactory.newMethod("method"), Invocation.NO_PARAMETERS)));

		assertTrue("should match", expectation.matches(
				new Invocation(new Object(), methodFactory.newMethod("anotherMethod"), 
						       new Object[]{1,2,3,4})));
	}
	
	public void testCanConstrainTargetObject() {
		Object anotherObject = "anotherObject";
		
		expectation.setObjectMatcher(IsSame.same(targetObject));
		
		assertTrue("should match", expectation.matches(new Invocation(targetObject, method, Invocation.NO_PARAMETERS)));
		assertTrue("should not match", !expectation.matches(new Invocation(anotherObject, method, Invocation.NO_PARAMETERS)));
	}
	
	public void testCanConstrainMethod() {
		Method anotherMethod = methodFactory.newMethod("anotherMethod");
		
		expectation.setMethodMatcher(IsEqual.eq(method));
		
		assertTrue("should match", expectation.matches(new Invocation(targetObject, method, Invocation.NO_PARAMETERS)));
		assertTrue("should not match", !expectation.matches(new Invocation(targetObject, anotherMethod, Invocation.NO_PARAMETERS)));
	}
	
	public void testCanConstrainArguments() {
		Object[] args = {1,2,3,4};
		Object[] differentArgs = {5,6,7,8};
		Object[] differentArgCount = {1,2,3};
		Object[] noArgs = null;
		
		expectation.setParametersMatcher(IsEqual.eq(args));
		
		assertTrue("should match", expectation.matches(new Invocation(targetObject, method, args)));
		assertTrue("should not match", !expectation.matches(new Invocation(targetObject, method, differentArgs)));
		assertTrue("should not match", !expectation.matches(new Invocation(targetObject, method, differentArgCount)));
		assertTrue("should not match", !expectation.matches(new Invocation(targetObject, method, noArgs)));
	}
	
	public void testDoesNotMatchIfMatchingCountMatcherDoesNotMatch() throws Throwable {
		Invocation invocation = new Invocation("targetObject", methodFactory.newMethod("method"), Invocation.NO_PARAMETERS);
		
		int maxInvocationCount = 3;
		expectation.setMaxInvocationCount(maxInvocationCount);
		
		int i;
		for (i = 0; i < maxInvocationCount; i++) {
			assertTrue("should match after " + i +" invocations", expectation.matches(invocation));
			expectation.invoke(invocation);
		}
		assertFalse("should not match after " + i + " invocations", expectation.matches(invocation));
	}
	
	public void testMustMeetTheRequiredInvocationCountButContinuesToMatch() throws Throwable {
		Invocation invocation = new Invocation("targetObject", methodFactory.newMethod("method"), null);
		
		int requiredInvocationCount = 3;
		expectation.setRequiredInvocationCount(requiredInvocationCount);
		
		int i;
		for (i = 0; i < requiredInvocationCount; i++) {
			assertTrue("should match after " + i +" invocations", 
					expectation.matches(invocation));
			assertTrue("should not be satisfied after " + i +" invocations",
					!expectation.isSatisfied());
			
			expectation.invoke(invocation);
		}

		assertTrue("should match after " + i +" invocations", 
				expectation.matches(invocation));
		assertTrue("should be satisfied after " + i +" invocations",
				expectation.isSatisfied());
	}
    
    public void testPerformsActionWhenInvoked() throws Throwable {
        Invocation invocation = new Invocation(targetObject, method, Invocation.NO_PARAMETERS);
        MockAction action = new MockAction();
        
        action.expectInvoke = true;
        action.expectedInvocation = invocation;
        action.result = "result";
        
        expectation.setAction(action);
        
        Object actualResult = expectation.invoke(invocation);
        
        assertSame("actual result", action.result, actualResult);
        assertTrue("action1 was invoked", action.wasInvoked);
    }
    
    public void testReturnsNullIfHasNoActionsWhenInvoked() throws Throwable {
        Invocation invocation = new Invocation(targetObject, method, Invocation.NO_PARAMETERS);
        
        Object actualResult = expectation.invoke(invocation);
        
        assertNull("should have returned null", actualResult);
    }
}