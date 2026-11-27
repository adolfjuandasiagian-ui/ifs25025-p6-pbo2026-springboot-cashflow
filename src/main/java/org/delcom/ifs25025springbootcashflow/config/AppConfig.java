package org.delcom.ifs25025springbootcashflow.config;

import org.delcom.ifs25025springbootcashflow.adapter.ai.OpenAiFinanceService;
import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25025springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25025springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.LoadingIndicatorInitListener;
import org.delcom.ifs25025springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

    @Bean
    public LoadingIndicatorInitListener loadingIndicatorInitListener() {
        return new LoadingIndicatorInitListener();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean(initMethod = "initialize")
    public SchemaInitializer schemaInitializer(
            JdbcTemplate jdbcTemplate,
            IUserRepository userRepository,
            PasswordHasher passwordHasher,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword) {
        return new SchemaInitializer(
                jdbcTemplate,
                userRepository,
                passwordHasher,
                rootUsername,
                rootPassword);
    }

    @Bean
    public ITransactionRepository transactionRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcTransactionRepository(jdbcTemplate);
    }

    @Bean
    public IUserRepository userRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcUserRepository(jdbcTemplate);
    }

    @Bean
    public IFinanceAiService financeAiService(
            @Value("${OPENAI_ENDPOINT}") String endpoint,
            @Value("${OPENAI_KEY}") String apiKey,
            @Value("${OPENAI_MODEL}") String model) {
        return new OpenAiFinanceService(endpoint, apiKey, model);
    }

    @Bean
    public AuthSession authSession(IUserRepository userRepository) {
        return new AuthSession(userRepository);
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDirectory) {
        return new FileStorage(uploadDirectory);
    }

    @Bean
    public AuthUseCase authUseCase(
            IUserRepository userRepository,
            PasswordHasher passwordHasher) {
        return new AuthUseCase(userRepository, passwordHasher);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinanceAiUseCase financeAiUseCase(IFinanceAiService financeAiService) {
        return new FinanceAiUseCase(financeAiService);
    }

    @Bean
    public UserUseCase userUseCase(
            IUserRepository userRepository,
            PasswordHasher passwordHasher) {
        return new UserUseCase(userRepository, passwordHasher);
    }

    @Bean
    public WebMvcConfigurer uploadResourceHandler(FileStorage fileStorage) {
        String uploadLocation = fileStorage.getUploadDirectory().toUri().toString();
        final String location = uploadLocation.endsWith("/")
                ? uploadLocation
                : uploadLocation + "/";

        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(location);
            }
        };
    }
}