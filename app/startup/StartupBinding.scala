package startup

import com.google.inject.AbstractModule;

class StartModule extends AbstractModule {
    override def configure() {
        bind(classOf[Startup]).asEagerSingleton();
    }
}