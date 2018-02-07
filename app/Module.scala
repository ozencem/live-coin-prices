import com.google.inject.AbstractModule
import config.SSLConfig

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SSLConfig]).asEagerSingleton()
  }
}
