package com.bgeradz.multiplexer;

import java.io.Closeable;
import java.io.IOException;

public class Util {
	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {}
		}
	}
}
