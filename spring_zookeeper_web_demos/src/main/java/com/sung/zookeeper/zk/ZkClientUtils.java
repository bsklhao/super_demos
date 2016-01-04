package com.sung.zookeeper.zk;

import com.sung.zookeeper.zk.exception.ZkInterruptedException;

import java.io.IOException;
import java.net.*;

public class ZkClientUtils {
	private ZkClientUtils() {
	}

	public static enum ZkVersion {
		V33, V34
	}

	public static final ZkVersion zkVersion;

	static {
		ZkVersion version = null;
		try {
			Class.forName("org.apache.zookeeper.OpResult");
			version = ZkVersion.V34;
		} catch (ClassNotFoundException e) {
			version = ZkVersion.V33;
		} finally {
			zkVersion = version;
		}

	}

	public static RuntimeException convertToRuntimeException(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		retainInterruptFlag(e);
		return new RuntimeException(e);
	}

	/**
	 * This sets the interrupt flag if the catched exception was an
	 * {@link InterruptedException}. Catching such an exception always clears
	 * the interrupt flag.
	 * 
	 * @param catchedException
	 *            The catched exception.
	 */
	public static void retainInterruptFlag(Throwable catchedException) {
		if (catchedException instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
	}

	public static void rethrowInterruptedException(Throwable e)
			throws InterruptedException {
		if (e instanceof InterruptedException) {
			throw (InterruptedException) e;
		}
		if (e instanceof ZkInterruptedException) {
			throw (ZkInterruptedException) e;
		}
	}

	public static String leadingZeros(long number, int numberOfLeadingZeros) {
		return String.format("%0" + numberOfLeadingZeros + "d", number);
	}

	public final static String OVERWRITE_HOSTNAME_SYSTEM_PROPERTY = "zkclient.hostname.overwritten";

	public static boolean isPortFree(int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress("localhost", port), 200);
			socket.close();
			return false;
		} catch (SocketTimeoutException e) {
			return true;
		} catch (ConnectException e) {
			return true;
		} catch (SocketException e) {
			if (e.getMessage().equals("Connection reset by peer")) {
				return true;
			}
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getLocalhostName() {
		String property = System
				.getProperty(OVERWRITE_HOSTNAME_SYSTEM_PROPERTY);
		if (property != null && property.trim().length() > 0) {
			return property;
		}
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			throw new RuntimeException("unable to retrieve localhost name");
		}
	}
}
