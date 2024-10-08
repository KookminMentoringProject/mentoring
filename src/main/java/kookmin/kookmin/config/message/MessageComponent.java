package kookmin.kookmin.config.message;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Getter
@Setter
public class MessageComponent {
    private String EMAIL_CHECK_FAILED;
    private String PWD_CHECK_FAILED;
    private String PWD_IS_NOT_EQUAL;
    private String NICKNAME_IS_NOT_KOREAN;
    private String SIGNUP_SUCCESS;
    private String SIGNUP_FAIL;
    private String COWEEF_MAIL_ADDRESS;
    private String AUTH_MAIL_SEND;
    private String AUTH_CODE;
}
