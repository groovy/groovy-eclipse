/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Combines sequential small writes into larger writes and that ensures that writes don't happen faster than a certain
 * maximum rate.
 */
public class ChunkWriter {
	private double maxBytesPerMillisecond;
	private long lastWritePosition;
	private long bufferStartPosition;
	private byte[] buffer;
	private WriteCallback writeCallback;
	private long bytesWrittenSinceLastSleep;
	private long totalWriteTimeMs;
	private long totalBytesWritten;
	private SleepCallback sleepFunction = Thread::sleep;

	/**
	 * Interface used to perform the uninterruptable writes when the buffer fills up.
	 */
	@FunctionalInterface 
	public interface WriteCallback {
		/**
		 * Performs an uninterruptable write of the given bytes to the given file position.
		 * 
		 * @param buffer
		 *            the bytes to write
		 * @param position
		 *            the file position to write them to
		 * @return true iff an attempt was made to interrupt the write via {@link Thread#interrupt()}. Note that the
		 *         write must succeed regardless of the return value.
		 * @throws IOException
		 *             if unable to perform the write
		 */
		boolean write(ByteBuffer buffer, long position) throws IOException;
	}

	@FunctionalInterface
	public interface SleepCallback {
		/**
		 * Sleeps the caller for the given time (in milliseconds) 
		 */
		void sleep(long millisecond) throws InterruptedException;
	}

	/**
	 * Constructs a new {@link ChunkWriter}
	 * 
	 * @param bufferSize
	 *            size of the write buffer (the maximum number of bytes that will be written in a single write).
	 * @param maxBytesPerMillisecond
	 *            the maximum number of bytes that will be written per second. If an attempt is made to write more
	 *            rapidly than this, the thread will be put to sleep.
	 * @param callback
	 *            will be invoked to perform the writes
	 */
	public ChunkWriter(int bufferSize, double maxBytesPerMillisecond, WriteCallback callback) {
		this.buffer = new byte[bufferSize];
		this.lastWritePosition = 0;
		this.bufferStartPosition = 0;
		this.maxBytesPerMillisecond = maxBytesPerMillisecond;
		this.writeCallback = callback;
	}

	/**
	 * Writes the given bytes to the given file position.
	 * 
	 * @return true iff any attempt was made to interrupt the thread using {@link Thread#interrupt()}. The write
	 *         succeeds regardless of the return value.
	 * @throws IOException if unable to perform the write
	 */
	public boolean write(long position, byte[] data) throws IOException {
		if (position == this.lastWritePosition) {
			int bufferPosition = (int) (this.lastWritePosition - this.bufferStartPosition);
			if (bufferPosition + data.length <= this.buffer.length) {
				System.arraycopy(data, 0, this.buffer, bufferPosition, data.length);
				this.lastWritePosition = position + data.length;
				return false;
			}
		}

		boolean wasInterrupted = flush();
		System.arraycopy(data, 0, this.buffer, 0, data.length);
		this.bufferStartPosition = position;
		this.lastWritePosition = position + data.length;
		return wasInterrupted;
	}

	/**
	 * Flushes any outstanding writes to disk immediately.
	 * 
	 * @return true iff any attempt was made to interrupt the thread using {@link Thread#interrupt()}. The write
	 *         succeeds regardless of the return value.
	 * @throws IOException if unable to perform the write
	 */
	public boolean flush() throws IOException {
		int bytesToWrite = (int) (this.lastWritePosition - this.bufferStartPosition);
		if (bytesToWrite == 0) {
			return false;
		}
		long startTimeMs = System.currentTimeMillis();
		boolean result = this.writeCallback.write(ByteBuffer.wrap(this.buffer, 0, bytesToWrite),
				this.bufferStartPosition);
		long elapsedTimeMs = System.currentTimeMillis() - startTimeMs;

		this.totalBytesWritten += bytesToWrite;
		this.totalWriteTimeMs += elapsedTimeMs;
		this.bytesWrittenSinceLastSleep = Math.max(0,
				this.bytesWrittenSinceLastSleep + bytesToWrite - (long) (elapsedTimeMs * this.maxBytesPerMillisecond));
		long desiredSleepTime = (long) (this.bytesWrittenSinceLastSleep / this.maxBytesPerMillisecond);

		// If we're writing too fast, sleep to create backpressure and prevent us from overloading
		// the disk's I/O bandwidth.
		if (desiredSleepTime > 0) {
			try {
				this.sleepFunction.sleep(desiredSleepTime);
				this.bytesWrittenSinceLastSleep -= this.maxBytesPerMillisecond * desiredSleepTime;
			} catch (InterruptedException e) {
				result = true;
			}
		}
		this.bufferStartPosition = this.lastWritePosition;
		return result;
	}

	/**
	 * Overrides the sleep callback function.
	 */
	public void setSleepFunction(SleepCallback callback) {
		this.sleepFunction = callback;
	}

	/**
	 * Returns the total number of bytes written
	 */
	public long getBytesWritten() {
		return this.totalBytesWritten;
	}

	/**
	 * Returns the total time spent in calls to {@link WriteCallback#write(ByteBuffer, long)}.
	 */
	public long getTotalWriteTimeMs() {
		return this.totalWriteTimeMs;
	}
}
