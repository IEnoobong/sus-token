package co.enoobong.services.security;

import co.enoobong.services.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class MultiHttpSecurityConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Configuration
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public static class RestSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.securityMatcher(new AntPathRequestMatcher("/api/**"))
          .cors(AbstractHttpConfigurer::disable)
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/api/**"))
                  .permitAll());
      return http.build();
    }
  }

  @Configuration
  @Order(2)
  public static class VaadinSecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.authorizeHttpRequests(
          authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/images/*.png"))
              .permitAll());

      // Icons from the line-awesome addon
      http.authorizeHttpRequests(authorize -> authorize
          .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll());

      super.configure(http);
      setLoginView(http, LoginView.class);
    }
  }
}
