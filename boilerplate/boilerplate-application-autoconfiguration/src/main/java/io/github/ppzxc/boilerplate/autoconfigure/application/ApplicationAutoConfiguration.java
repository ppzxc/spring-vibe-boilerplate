package io.github.ppzxc.boilerplate.autoconfigure.application;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTagQuery;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoSummariesQuery;
import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTagPort;
import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTagPort;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTagPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.query.LoadTodoSummariesPort;
import io.github.ppzxc.boilerplate.application.port.output.shared.PublishEventPort;
import io.github.ppzxc.boilerplate.application.service.command.CreateTagService;
import io.github.ppzxc.boilerplate.application.service.command.CreateTodoService;
import io.github.ppzxc.boilerplate.application.service.command.DeleteTagService;
import io.github.ppzxc.boilerplate.application.service.command.DeleteTodoService;
import io.github.ppzxc.boilerplate.application.service.command.UpdateTodoService;
import io.github.ppzxc.boilerplate.application.service.query.FindTagService;
import io.github.ppzxc.boilerplate.application.service.query.FindTodoService;
import io.github.ppzxc.boilerplate.application.service.query.FindTodoSummariesService;
import java.util.Properties;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/** Application AutoConfiguration — UseCase Bean을 트랜잭션 프록시로 등록. */
@AutoConfiguration
public class ApplicationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  PublishEventPort publishEventPort(ApplicationEventPublisher publisher) {
    return publisher::publishEvent;
  }

  @Bean
  @ConditionalOnMissingBean
  CreateTodoUseCase createTodoUseCase(
      SaveTodoPort saveTodoPort,
      PublishEventPort publishEventPort,
      PlatformTransactionManager txManager) {
    return txProxy(
        new CreateTodoService(saveTodoPort, publishEventPort),
        CreateTodoUseCase.class,
        txManager,
        false);
  }

  @Bean
  @ConditionalOnMissingBean
  UpdateTodoUseCase updateTodoUseCase(
      FindTodoPort findTodoPort, SaveTodoPort saveTodoPort, PlatformTransactionManager txManager) {
    return txProxy(
        new UpdateTodoService(findTodoPort, saveTodoPort),
        UpdateTodoUseCase.class,
        txManager,
        false);
  }

  @Bean
  @ConditionalOnMissingBean
  DeleteTodoUseCase deleteTodoUseCase(
      DeleteTodoPort deleteTodoPort, PlatformTransactionManager txManager) {
    return txProxy(
        new DeleteTodoService(deleteTodoPort), DeleteTodoUseCase.class, txManager, false);
  }

  @Bean
  @ConditionalOnMissingBean
  FindTodoQuery findTodoQuery(FindTodoPort findTodoPort, PlatformTransactionManager txManager) {
    return txProxy(new FindTodoService(findTodoPort), FindTodoQuery.class, txManager, true);
  }

  @Bean
  @ConditionalOnMissingBean
  FindTodoSummariesQuery findTodoSummariesQuery(
      LoadTodoSummariesPort loadTodoSummariesPort, PlatformTransactionManager txManager) {
    return txProxy(
        new FindTodoSummariesService(loadTodoSummariesPort),
        FindTodoSummariesQuery.class,
        txManager,
        true);
  }

  @Bean
  @ConditionalOnMissingBean
  CreateTagUseCase createTagUseCase(SaveTagPort saveTagPort, PlatformTransactionManager txManager) {
    return txProxy(new CreateTagService(saveTagPort), CreateTagUseCase.class, txManager, false);
  }

  @Bean
  @ConditionalOnMissingBean
  DeleteTagUseCase deleteTagUseCase(
      DeleteTagPort deleteTagPort, PlatformTransactionManager txManager) {
    return txProxy(new DeleteTagService(deleteTagPort), DeleteTagUseCase.class, txManager, false);
  }

  @Bean
  @ConditionalOnMissingBean
  FindTagQuery findTagQuery(FindTagPort findTagPort, PlatformTransactionManager txManager) {
    return txProxy(new FindTagService(findTagPort), FindTagQuery.class, txManager, true);
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
