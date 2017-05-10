package pro.hirooka.chukasa.domain.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import pro.hirooka.chukasa.chukasa_auth.domain.service.IChukasaUserDetailsService;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    IChukasaUserDetailsService chukasaUserDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.chukasaUserDetailsService);//.passwordEncoder(passwordEncoder()); // TODO:
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().loginPage("/").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin().defaultSuccessUrl("/menu").permitAll();
        http.logout().permitAll();
    }

    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers("/images/**", "/webjars/**");
    }

}

