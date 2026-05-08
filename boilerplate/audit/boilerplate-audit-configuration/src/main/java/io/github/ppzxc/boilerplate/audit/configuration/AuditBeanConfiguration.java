package io.github.ppzxc.boilerplate.audit.configuration;

import io.github.ppzxc.boilerplate.audit.adapter.output.persist.AuditLogPersistenceAdapter;
import io.github.ppzxc.boilerplate.audit.adapter.output.persist.AuditLogQueryAdapter;
import io.github.ppzxc.boilerplate.audit.application.port.in.FindAuditLogsBySubjectUseCase;
import io.github.ppzxc.boilerplate.audit.application.port.in.ListRecentAuditLogsUseCase;
import io.github.ppzxc.boilerplate.audit.application.port.in.RecordUserRegisteredAuditUseCase;
import io.github.ppzxc.boilerplate.audit.application.service.FindAuditLogsBySubjectService;
import io.github.ppzxc.boilerplate.audit.application.service.ListRecentAuditLogsService;
import io.github.ppzxc.boilerplate.audit.application.service.RecordUserRegisteredAuditService;
import java.time.Clock;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
class AuditBeanConfiguration {

  @Bean
  Clock auditClock() {
    return Clock.systemUTC();
  }

  // --- AI_ANCHOR: ADD_NEW_USECASE_BEANS_HERE ---

  @Bean
  RecordUserRegisteredAuditUseCase recordUserRegisteredAuditUseCase(
      AuditLogPersistenceAdapter adapter, Clock auditClock, PlatformTransactionManager txManager) {
    var service = new RecordUserRegisteredAuditService(adapter, auditClock);
    return createTxProxy(service, RecordUserRegisteredAuditUseCase.class, txManager);
  }

  @Bean
  FindAuditLogsBySubjectUseCase findAuditLogsBySubjectUseCase(
      AuditLogQueryAdapter queryAdapter, PlatformTransactionManager txManager) {
    var service = new FindAuditLogsBySubjectService(queryAdapter);
    return createTxProxy(service, FindAuditLogsBySubjectUseCase.class, txManager);
  }

  @Bean
  ListRecentAuditLogsUseCase listRecentAuditLogsUseCase(
      AuditLogQueryAdapter queryAdapter, PlatformTransactionManager txManager) {
    var service = new ListRecentAuditLogsService(queryAdapter);
    return createTxProxy(service, ListRecentAuditLogsUseCase.class, txManager);
  }

  private <T> T createTxProxy(Object target, Class<T> iface, PlatformTransactionManager txManager) {
    var source = new MatchAlwaysTransactionAttributeSource();
    var interceptor = new TransactionInterceptor(txManager, source);
    var factory = new ProxyFactory(target);
    factory.addInterface(iface);
    factory.addAdvice(interceptor);
    return iface.cast(factory.getProxy());
  }
}
