package com.bgeradz.multiplexer;

import java.io.IOException;

public interface DataSource {
	TrackedInputStream open() throws IOException;
}
