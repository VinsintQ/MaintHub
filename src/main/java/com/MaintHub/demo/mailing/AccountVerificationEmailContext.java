package com.MaintHub.demo.mailing;



import com.MaintHub.demo.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountVerificationEmailContext extends AbstractEmailContext {
    private String token;

    @Override
    public <T> void init(T context){
        //System.out.println("email entered ------>" + fromEmail);
        User user = (User) context;
        put("username", user.getUserName());
        setTemplateLocation("mailing/email-verification");
        setSubject("Complete your registration");
        setTo(user.getEmailAddress());
    }

    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    public void buildVerificationUrl(final String baseURL, final String token){
        final String url= UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/auth/users/register/verify").queryParam("token", token).toUriString();
        put("verificationURL", url);
    }
}