package io.github.ppzxc.boilerplate.autoconfigure.application;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTodoPort;
import io.github.ppzxc.boilerplate.application.service.command.CreateTodoService;
import io.github.ppzxc.boilerplate.application.service.command.DeleteTodoService;
import io.github.ppzxc.boilerplate.application.service.command.UpdateTodoService;
import io.github.ppzxc.boilerplate.application.service.query.FindTodoService;
import java.util.Properties;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@AutoConfiguration
public class ApplicationAutoConfiguration {

  @Bean
  CreateTodoUseCase createTodoUseCase(
      SaveTodoPort saveTodoPort, PlatformTransactionManager txManager) {
    return txProxy(new CreateTodoService(saveTodoPort), CreateTodoUseCase.class, txManager, false);
  }

  @Bean
  UpdateTodoUseCase updateTodoUseCase(
      FindTodoPort findTodoPort, SaveTodoPort saveTodoPort, PlatformTransactionManager txManager) {
    return txProxy(
        new UpdateTodoService(findTodoPort, saveTodoPort),
        UpdateTodoUseCase.class,
        txManager,
        false);
  }

  @Bean
  DeleteTodoUseCase deleteTodoUseCase(
      DeleteTodoPort deleteTodoPort, PlatformTransactionManager txManager) {
    return txProxy(
        new DeleteTodoService(deleteTodoPort), DeleteTodoUseCase.class, txManager, false);
  }

  @Bean
  FindTodoQuery findTodoQuery(FindTodoPort findTodoPort, PlatformTransactionManager txManager) {
    return txProxy(new FindTodoService(findTodoPort), FindTodoQuery.class, txManager, true);
  }

  private <T> T txProxy(
      T target, Class<T> iface, PlatformTransactionManager txManager, boolean readOnly) {
    TransactionInterceptor interceptor = new TransactionInterceptor();
    interceptor.setTransactionManager(txManager);
    Properties attrs = new Properties();
    attrs.setProperty("*", readOnly ? "PROPAGATION_REQUIRED,readOnly" : "PROPAGATION_REQUIRED");
    interceptor.setTransactionAttributes(attrs);

    ProxyFactory factory = new ProxyFactory(target);
    factory.addInterface(iface);
    factory.addAdvice(interceptor);
    return iface.cast(factory.getProxy());
  }
}
