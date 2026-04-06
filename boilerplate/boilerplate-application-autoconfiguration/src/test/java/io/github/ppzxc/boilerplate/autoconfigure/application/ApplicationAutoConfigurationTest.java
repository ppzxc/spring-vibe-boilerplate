package io.github.ppzxc.boilerplate.autoconfigure.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      ApplicationAutoConfiguration.class,
      ApplicationAutoConfigurationTest.TestConfig.class
    })
class ApplicationAutoConfigurationTest {

  @Autowired ApplicationContext context;

  @TestConfiguration
  static class TestConfig {

    @Bean
    SaveTodoPort saveTodoPort() {
      return mock(SaveTodoPort.class);
    }

    @Bean
    FindTodoPort findTodoPort() {
      return mock(FindTodoPort.class);
    }

    @Bean
    DeleteTodoPort deleteTodoPort() {
      return mock(DeleteTodoPort.class);
    }

    @Bean
    LoadTodoSummariesPort loadTodoSummariesPort() {
      return mock(LoadTodoSummariesPort.class);
    }

    @Bean
    SaveTagPort saveTagPort() {
      return mock(SaveTagPort.class);
    }

    @Bean
    DeleteTagPort deleteTagPort() {
      return mock(DeleteTagPort.class);
    }

    @Bean
    FindTagPort findTagPort() {
      return mock(FindTagPort.class);
    }

    @Bean
    PlatformTransactionManager transactionManager() {
      return mock(PlatformTransactionManager.class);
    }
  }

  @Test
  void all_use_case_beans_are_registered() {
    assertThat(context.getBean(CreateTodoUseCase.class)).isNotNull();
    assertThat(context.getBean(UpdateTodoUseCase.class)).isNotNull();
    assertThat(context.getBean(DeleteTodoUseCase.class)).isNotNull();
    assertThat(context.getBean(FindTodoQuery.class)).isNotNull();
    assertThat(context.getBean(FindTodoSummariesQuery.class)).isNotNull();
    assertThat(context.getBean(CreateTagUseCase.class)).isNotNull();
    assertThat(context.getBean(DeleteTagUseCase.class)).isNotNull();
    assertThat(context.getBean(FindTagQuery.class)).isNotNull();
    assertThat(context.getBean(PublishEventPort.class)).isNotNull();
  }

  @Test
  void use_case_beans_are_jdk_proxies() {
    assertThat(Proxy.isProxyClass(context.getBean(CreateTodoUseCase.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(UpdateTodoUseCase.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(DeleteTodoUseCase.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(FindTodoQuery.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(FindTodoSummariesQuery.class).getClass()))
        .isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(CreateTagUseCase.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(DeleteTagUseCase.class).getClass())).isTrue();
    assertThat(Proxy.isProxyClass(context.getBean(FindTagQuery.class).getClass())).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_create_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "createTodoUseCase",
            SaveTodoPort.class,
            PublishEventPort.class,
            PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_find_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "findTodoQuery", FindTodoPort.class, PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_update_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "updateTodoUseCase",
            FindTodoPort.class,
            SaveTodoPort.class,
            PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_delete_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "deleteTodoUseCase", DeleteTodoPort.class, PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_find_summaries_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "findTodoSummariesQuery",
            LoadTodoSummariesPort.class,
            PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_create_tag_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "createTagUseCase", SaveTagPort.class, PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_delete_tag_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "deleteTagUseCase", DeleteTagPort.class, PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }

  @Test
  void conditional_on_missing_bean_annotation_is_present_on_find_tag_bean() throws Exception {
    var method =
        ApplicationAutoConfiguration.class.getDeclaredMethod(
            "findTagQuery", FindTagPort.class, PlatformTransactionManager.class);
    assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
  }
}
