class Element {
    protected def config

    Element(conf) {
        config = conf
    }

    def call(closure) {
        closure.delegate = this
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure()
        config.validate()
    }

    /* never called
    def propertyMissing(String name) {
        println "" + this +".propertyMissing: $name"
    }
    */

    /*
    def invokeMethod(String name, args) {
        println "" + this +".invokeMethod: $name"
        config."$name"(args)
    }
    */

    @Override
    Object getProperty(String name) {
        if (metaClass.hasProperty(this, name)) {
            return metaClass.getProperty(this, name)
        } else {
            return config."$name"
        }
    }

    @Override
    void setProperty(String name, Object value) {
        if (metaClass.hasProperty(this, name)) {
            metaClass.setProperty(this, name, value)
        } else {
            config."$name" = value
        }
    }
}

class DataSourceElement extends Element {
    DataSourceElement(config) {
        super(config)
    }
}

class UrlDataSourceElement extends DataSourceElement {
    UrlDataSourceElement() {
        super(new com.bgeradz.multiplexer.UrlDataSource.Config())
    }
}
class ProcessOutputDataSourceElement extends DataSourceElement {
    ProcessOutputDataSourceElement() {
        super(new com.bgeradz.multiplexer.ProcessOutputDataSource.Config())
    }
}

class HandlerElement extends Element {
    String path

    HandlerElement(config) {
        super(config)
    }
}

class StreamHandlerElement extends HandlerElement {
    DataSourceElement dataSource

    void setDataSource(DataSourceElement ds) {
        dataSource = ds
        config.setDataSource(ds.config)
    }

    StreamHandlerElement(config) {
        super(config)
    }

    DataSourceElement url(closure) {
        UrlDataSourceElement ds = new UrlDataSourceElement()
        ds.call(closure)
        return ds
    }

    DataSourceElement processOutput(closure) {
        ProcessOutputDataSourceElement ds = new ProcessOutputDataSourceElement()
        ds.call(closure)
        return ds
    }
}

class ReplicateHandlerElement extends StreamHandlerElement {
    ReplicateHandlerElement() {
        super(new com.bgeradz.multiplexer.ReplicatingStreamRequestHandler.Config())
    }
}

class SpawnHandlerElement extends StreamHandlerElement {
    SpawnHandlerElement() {
        super(new com.bgeradz.multiplexer.SimpleStreamRequestHandler.Config())
    }
}


class ProxyHandlerElement extends HandlerElement {
    ProxyHandlerElement() {
        super(new com.bgeradz.multiplexer.ProxyRequestHandler.Config())
    }
}

class StatusHandlerElement extends HandlerElement {
    StatusHandlerElement() {
        super(new com.bgeradz.multiplexer.StatusRequestHandler.Config())
    }
}

class CommandHandlerElement extends HandlerElement {
    CommandHandlerElement() {
        super(new com.bgeradz.multiplexer.CommandRequestHandler.Config())
    }
}

class Mux extends Element {
    Mux() {
        super(new com.bgeradz.multiplexer.Launcher.Config())
    }

    private void addHandler(HandlerElement handler, closure) {
        handler.call(closure);
        config.addHandler(handler.path, handler.config);
    }

    void replicate(closure) {
        addHandler(new ReplicateHandlerElement(), closure)
    }
    void spawn(closure) {
        addHandler(new SpawnHandlerElement(), closure)
    }
    void proxy(closure) {
        addHandler(new ProxyHandlerElement(), closure)
    }
    void command(closure) {
        addHandler(new CommandHandlerElement(), closure)
    }
    void status(closure) {
        addHandler(new StatusHandlerElement(), closure)
    }

}

static void start(closure) {
    Mux mux = new Mux()
    mux.call(closure)
    mux.config.build()
}

