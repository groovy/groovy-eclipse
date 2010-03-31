/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.testplugin.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Display;

/**
 * Runs the event loop of the given display until {@link #condition()} becomes
 * <code>true</code> or no events have occurred for the supplied timeout.
 * Between running the event loop, {@link Display#sleep()} is called.
 * <p>
 * There is a caveat: the given timeouts must be long enough that the calling
 * thread can enter <code>Display.sleep()</code> before the timeout elapses,
 * otherwise, the waiter may time out before <code>sleep</code> is called and
 * the sleeping thread may never be waken up.
 * </p>
 *
 * @since 3.1
 */
public abstract class DisplayHelper {

	/**
	 * Controls if the timeout is used. For debugging use true, false otherwise
	 */
	private static final boolean DISABLE_TIMEOUT= false;

	/**
	 * Creates a new instance.
	 */
	protected DisplayHelper() {
	}

	/**
	 * Until {@link #condition()} becomes <code>true</code> or the timeout
	 * elapses, call {@link Display#sleep()} and run the event loop.
	 * <p>
	 * If <code>timeout &lt; 0</code>, the event loop is never driven and
	 * only the condition is checked. If <code>timeout == 0</code>, the event
	 * loop is driven at most once, but <code>Display.sleep()</code> is never
	 * invoked.
	 * </p>
	 *
	 * @param display the display to run the event loop of
	 * @param timeout the timeout in milliseconds
	 * @return <code>true</code> if the condition became <code>true</code>,
	 *         <code>false</code> if the timeout elapsed
	 */
	public final boolean waitForCondition(Display display, long timeout) {
		// if the condition already holds, succeed
		if (condition())
			return true;

		if (timeout < 0)
			return false;

		// if driving the event loop once makes the condition hold, succeed
		// without spawning a thread.
		driveEventQueue(display);
		if (condition())
			return true;

		// if the timeout is negative or zero, fail
		if (timeout == 0)
			return false;

		// repeatedly sleep until condition becomes true or timeout elapses
		DisplayWaiter waiter= new DisplayWaiter(display);
		DisplayWaiter.Timeout timeoutState= waiter.start(timeout);
		boolean condition;
		try {
			do {
				if (display.sleep())
					driveEventQueue(display);
				condition= condition();
			} while (!condition && !timeoutState.hasTimedOut());
		} finally {
			waiter.stop();
		}
		return condition;
	}

	/**
	 * Call {@link Display#sleep()} and run the event loop until the given
	 * timeout has elapsed.
	 * <p>
	 * If <code>timeout &lt; 0</code>, nothing happens. If
	 * <code>timeout == 0</code>, the event loop is driven exactly once, but
	 * <code>Display.sleep()</code> is never invoked.
	 * </p>
	 *
	 * @param display the display to run the event loop of
	 * @param millis the timeout in milliseconds
	 */
	public static void sleep(Display display, long millis) {
		new DisplayHelper() {
			public boolean condition() {
				return false;
			}
		}.waitForCondition(display, millis);
	}

	/**
	 * The condition which has to be met in order for {@link #waitForCondition(Display, long)} to
	 * return before the timeout elapses.
	 * 
	 * @return <code>true</code> if the condition is met, <code>false</code> if the event loop
	 *         should be driven some more
	 */
	protected abstract boolean condition();

	/**
	 * Runs the event loop on the given display.
	 *
	 * @param display the display
	 * @return if <code>display.readAndDispatch</code> returned
	 *         <code>true</code> at least once
	 */
	private static boolean driveEventQueue(Display display) {
		boolean events= false;
		while (display.readAndDispatch()) {
			events= true;
		}
		return events;
	}

	/**
	 * Until {@link #condition()} becomes <code>true</code> or the timeout
	 * elapses, call {@link Display#sleep()} and run the event loop.
	 * <p>
	 * If <code>timeout &lt; 0</code>, the event loop is never driven and
	 * only the condition is checked. If <code>timeout == 0</code>, the event
	 * loop is driven at most once, but <code>Display.sleep()</code> is never
	 * invoked.
	 * </p>
	 * <p>
	 * The condition gets rechecked every <code>interval</code> milliseconds, even
	 * if no events were read from the queue.
	 * </p>
	 *
	 * @param display the display to run the event loop of
	 * @param timeout the timeout in milliseconds
	 * @param interval the interval to re-check the condition in milliseconds
	 * @return <code>true</code> if the condition became <code>true</code>,
	 *         <code>false</code> if the timeout elapsed
	 */
	public final boolean waitForCondition(Display display, long timeout, long interval) {
		// if the condition already holds, succeed
		if (condition())
			return true;

		if (timeout < 0)
			return false;

		// if driving the event loop once makes the condition hold, succeed
		// without spawning a thread.
		driveEventQueue(display);
		if (condition())
			return true;

		// if the timeout is negative or zero, fail
		if (timeout == 0)
			return false;

		// repeatedly sleep until condition becomes true or timeout elapses
		DisplayWaiter waiter= new DisplayWaiter(display, true);
		long currentTimeMillis= System.currentTimeMillis();
		long finalTimeout= timeout + currentTimeMillis;
		if (finalTimeout < currentTimeMillis)
			finalTimeout= Long.MAX_VALUE;
		boolean condition;
		try {
			do {
				waiter.restart(interval);
				if (display.sleep())
					driveEventQueue(display);
				condition= condition();
			} while (!condition && (DISABLE_TIMEOUT || finalTimeout > System.currentTimeMillis()));
		} finally {
			waiter.stop();
		}
		return condition;
	}

}

/**
 * Implements the thread that will wait for the timeout and wake up the display
 * so it does not wait forever. The thread may be restarted after it was stopped
 * or timed out.
 *
 * @since 3.1
 */
final class DisplayWaiter {
	/**
	 * Timeout state of a display waiter thread.
	 */
	public final class Timeout {
		private boolean fTimeoutState= false;
		/**
		 * Returns <code>true</code> if the timeout has been reached,
		 * <code>false</code> if not.
		 *
		 * @return <code>true</code> if the timeout has been reached,
		 *         <code>false</code> if not
		 */
		public boolean hasTimedOut() {
			synchronized (fMutex) {
				return fTimeoutState;
			}
		}
		void setTimedOut(boolean timedOut) {
			fTimeoutState= timedOut;
		}
		Timeout(boolean initialState) {
			fTimeoutState= initialState;
		}
	}

	// configuration
	private final Display fDisplay;
	private final Object fMutex= new Object();
	private final boolean fKeepRunningOnTimeout;

	/* State -- possible transitions:
	 *
	 * STOPPED   -> RUNNING
	 * RUNNING   -> STOPPED
	 * RUNNING   -> IDLE
	 * IDLE      -> RUNNING
	 * IDLE      -> STOPPED
	 */
	private static final int RUNNING= 1 << 1;
	private static final int STOPPED= 1 << 2;
	private static final int IDLE= 1 << 3;

	/** The current state. */
	private int fState;
	/** The time in milliseconds (see Date) that the timeout will occur. */
	private long fNextTimeout;
	/** The thread. */
	private Thread fCurrentThread;
	/** The timeout state of the current thread. */
	private Timeout fCurrentTimeoutState;

	/**
	 * Creates a new instance on the given display and timeout.
	 *
	 * @param display the display to run the event loop of
	 */
	public DisplayWaiter(Display display) {
		this(display, false);
	}

	/**
	 * Creates a new instance on the given display and timeout.
	 *
	 * @param display the display to run the event loop of
	 * @param keepRunning <code>true</code> if the thread should be kept
	 *        running after timing out
	 */
	public DisplayWaiter(Display display, boolean keepRunning) {
		Assert.assertNotNull(display);
		fDisplay= display;
		fState= STOPPED;
		fKeepRunningOnTimeout= keepRunning;
	}

	/**
	 * Starts the timeout thread if it is not currently running. Nothing happens
	 * if a thread is already running.
	 *
	 * @param delay the delay from now in milliseconds
	 * @return the timeout state which can be queried for its timed out status
	 */
	public Timeout start(long delay) {
		Assert.assertTrue(delay > 0);
		synchronized (fMutex) {
			switch (fState) {
				case STOPPED:
					startThread();
					setNextTimeout(delay);
					break;
				case IDLE:
					unhold();
					setNextTimeout(delay);
					break;
			}

			return fCurrentTimeoutState;
		}
	}

	/**
	 * Sets the next timeout to <em>current time</em> plus <code>delay</code>.
	 *
	 * @param delay the delay until the next timeout occurs in milliseconds from
	 *        now
	 */
	private void setNextTimeout(long delay) {
		long currentTimeMillis= System.currentTimeMillis();
		long next= currentTimeMillis + delay;
		if (next > currentTimeMillis)
			fNextTimeout= next;
		else
			fNextTimeout= Long.MAX_VALUE;
	}

	/**
	 * Starts the thread if it is not currently running; resets the timeout if
	 * it is.
	 *
	 * @param delay the delay from now in milliseconds
	 * @return the timeout state which can be queried for its timed out status
	 */
	public Timeout restart(long delay) {
		Assert.assertTrue(delay > 0);
		synchronized (fMutex) {
			switch (fState) {
				case STOPPED:
					startThread();
					break;
				case IDLE:
					unhold();
					break;
			}
			setNextTimeout(delay);

			return fCurrentTimeoutState;
		}
	}

	/**
	 * Stops the thread if it is running. If not, nothing happens. Another
	 * thread may be started by calling {@link #start(long)} or
	 * {@link #restart(long)}.
	 */
	public void stop() {
		synchronized (fMutex) {
			if (tryTransition(RUNNING | IDLE, STOPPED))
				fMutex.notifyAll();
		}
	}

	/**
	 * Puts the reaper thread on hold but does not stop it. It may be restarted
	 * by calling {@link #start(long)} or {@link #restart(long)}.
	 */
	public void hold() {
		synchronized (fMutex) {
			// nothing to do if there is no thread
			if (tryTransition(RUNNING, IDLE))
				fMutex.notifyAll();
		}
	}

	/**
	 * Transition to <code>RUNNING</code> and clear the timed out flag. Assume
	 * current state is <code>IDLE</code>.
	 */
	private void unhold() {
		checkedTransition(IDLE, RUNNING);
		fCurrentTimeoutState= new Timeout(false);
		fMutex.notifyAll();
	}

	/**
	 * Start the thread. Assume the current state is <code>STOPPED</code>.
	 */
	private void startThread() {
		checkedTransition(STOPPED, RUNNING);
		fCurrentTimeoutState= new Timeout(false);
		fCurrentThread= new Thread() {
			/**
			 * Exception thrown when a thread notices that it has been stopped
			 * and a new thread has been started.
			 */
			final class ThreadChangedException extends Exception {
				private static final long serialVersionUID= 1L;
			}

			/*
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				try {
					run2();
				} catch (InterruptedException e) {
					// ignore and end the thread - we never interrupt ourselves,
					// so it must be an external entity that interrupted us
					Logger.global.log(Level.FINE, "", e);
				} catch (ThreadChangedException e) {
					// the thread was stopped and restarted before we got out
					// of a wait - we're no longer used
					// we might have been notified instead of the current thread,
					// so wake it up
					Logger.global.log(Level.FINE, "", e);
					synchronized (fMutex) {
						fMutex.notifyAll();
					}
				}
			}

			/**
			 * Runs the thread.
			 *
			 * @throws InterruptedException if the thread was interrupted
			 * @throws ThreadChangedException if the thread changed
			 */
			private void run2() throws InterruptedException, ThreadChangedException {
				synchronized (fMutex) {
					checkThread();
					tryHold(); // wait / potential state change
					assertStates(STOPPED | RUNNING);

					while (isState(RUNNING)) {
						waitForTimeout(); // wait / potential state change

						if (isState(RUNNING))
							timedOut(); // state change
						assertStates(STOPPED | IDLE);

						tryHold(); // wait / potential state change
						assertStates(STOPPED | RUNNING);
					}
					assertStates(STOPPED);
				}
			}

			/**
			 * Check whether the current thread is this thread, throw an
			 * exception otherwise.
			 *
			 * @throws ThreadChangedException if the current thread changed
			 */
			private void checkThread() throws ThreadChangedException {
				if (fCurrentThread != this)
					throw new ThreadChangedException();
			}

			/**
			 * Waits until the next timeout occurs.
			 *
			 * @throws InterruptedException if the thread was interrupted
			 * @throws ThreadChangedException if the thread changed
			 */
			private void waitForTimeout() throws InterruptedException, ThreadChangedException {
				long delta;
				while (isState(RUNNING) && (delta = fNextTimeout - System.currentTimeMillis()) > 0) {
					delta= Math.max(delta, 50); // wait at least 50ms in order to avoid timing out before the display is going to sleep
					Logger.global.finest("sleeping for " + delta + "ms");
					fMutex.wait(delta);
					checkThread();
				}
			}

			/**
			 * Sets the timed out flag and wakes up the display. Transitions to
			 * <code>IDLE</code> (if in keep-running mode) or
			 * <code>STOPPED</code>.
			 */
			private void timedOut() {
				Logger.global.finer("timed out");
				fCurrentTimeoutState.setTimedOut(true);
				fDisplay.wake(); // wake up call!
				if (fKeepRunningOnTimeout)
					checkedTransition(RUNNING, IDLE);
				else
					checkedTransition(RUNNING, STOPPED);
			}

			/**
			 * Waits while the state is <code>IDLE</code>, then returns. The
			 * state must not be <code>RUNNING</code> when calling this
			 * method. The state is either <code>STOPPED</code> or
			 * <code>RUNNING</code> when the method returns.
			 *
			 * @throws InterruptedException if the thread was interrupted
			 * @throws ThreadChangedException if the thread has changed while on
			 *         hold
			 */
			private void tryHold() throws InterruptedException, ThreadChangedException {
				while (isState(IDLE)) {
					fMutex.wait(0);
					checkThread();
				}
				assertStates(STOPPED | RUNNING);
			}
		};

		fCurrentThread.start();
	}

	/**
	 * Transitions to <code>nextState</code> if the current state is one of
	 * <code>possibleStates</code>. Returns <code>true</code> if the
	 * transition happened, <code>false</code> otherwise.
	 *
	 * @param possibleStates the states which trigger a transition
	 * @param nextState the state to transition to
	 * @return <code>true</code> if the transition happened,
	 *         <code>false</code> otherwise
	 */
	private boolean tryTransition(int possibleStates, int nextState) {
		if (isState(possibleStates)) {
			Logger.global.finer(name(fState) + " > " + name(nextState) + " (" + name(possibleStates) + ")");
			fState= nextState;
			return true;
		}
		Logger.global.finest("noTransition" + name(fState) + " !> " + name(nextState) + " (" + name(possibleStates) + ")");
		return false;
	}

	/**
	 * Checks the <code>possibleStates</code> and throws an assertion if it is
	 * not met, then transitions to <code>nextState</code>.
	 *
	 * @param possibleStates the allowed states
	 * @param nextState the state to transition to
	 */
	private void checkedTransition(int possibleStates, int nextState) {
		assertStates(possibleStates);
		Logger.global.finer(name(fState) + " > " + name(nextState));
		fState= nextState;
	}

	/**
	 * Implements state consistency checking.
	 *
	 * @param states the allowed states
	 * @throws junit.framework.AssertionFailedError if the current state is not
	 *         in <code>states</code>
	 */
	private void assertStates(int states) {
		Assert.assertTrue("illegal state", isState(states));
	}

	/**
	 * Answers <code>true</code> if the current state is in the given
	 * <code>states</code>.
	 *
	 * @param states the possible states
	 * @return <code>true</code> if the current state is in the given states,
	 *         <code>false</code> otherwise
	 */
	private boolean isState(int states) {
		return (states & fState) == fState;
	}

	/**
	 * Pretty print the given states.
	 *
	 * @param states the states
	 * @return a string representation of the states
	 */
	private String name(int states) {
		StringBuffer buf= new StringBuffer();
		boolean comma= false;
		if ((states & RUNNING) == RUNNING) {
			buf.append("RUNNING");
			comma= true;
		}
		if ((states & STOPPED) == STOPPED) {
			if (comma)
				buf.append(",");
			buf.append("STOPPED");
			comma= true;
		}
		if ((states & IDLE) == IDLE) {
			if (comma)
				buf.append(",");
			buf.append("IDLE");
		}
		return buf.toString();
	}

}
