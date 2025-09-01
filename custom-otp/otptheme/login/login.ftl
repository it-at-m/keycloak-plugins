<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <strong>${msg("loginAccountTitle")}</strong><br/>
		${msg("fromExternal")}		
    <#elseif section = "form">
	<div id="lhmlogo"></div>
	<div id="lhmdigimarke"></div>
    <div id="kc-form">
      <div id="kc-form-wrapper">
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">					
                    <label for="username" class="${properties.kcLabelClass!} lhmLoginLabel"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>

                    <#if usernameEditDisabled??>
                        <input tabindex="1" id="username" class="${properties.kcInputClass!} lhmLoginLabel" name="username" value="${(login.username!'')}" type="text" disabled />
                    <#else>
						<div class="lhmLoginPic">
						<#if !login.username?? || login.username?length == 0>
							<input tabindex="1" id="username" class="${properties.kcInputClass!} lhmLoginInput" name="username" value="lhm"  type="text" autofocus autocomplete="off" onfocus="positionAtEnd()"
								   aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
							/>
						<#else>
							<input tabindex="1" id="username" class="${properties.kcInputClass!} lhmLoginInput" name="username" value="${login.username}" type="text" autofocus autocomplete="off" onfocus="positionAtEnd()"
								   aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
							/>
						</#if>
						</div>
						

                        <#if messagesPerField.existsError('username','password')>
							<br/><br/>
                            <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                            </span>
                        </#if>
                    </#if>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    <label for="password" class="${properties.kcLabelClass!} lhmLoginLabel">${msg("password")}</label>

					<div class="lhmLoginKombi">
                    <input tabindex="2" id="password" class="${properties.kcInputClass!} lhmLoginPassword" name="password" type="password" autocomplete="off"
                           aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                    />
					<div class="lhmtooltip lhmTogglePassword">
					<button type = "button" id="toggle" onclick="togglePassword()">
						<svg
							viewBox="0 0 24 24"
							style="fill: none; height: 28px; stroke: #3A5368; stroke-linecap: round; stroke-linejoin: round; stroke-width: 1;"
						>
							<path d="M23.5,12c0,0-5.148,6.5-11.5,6.5S0.5,12,0.5,12S5.648,5.5,12,5.5S23.5,12,23.5,12z M12,8c2.209,0,4,1.791,4,4 s-1.791,4-4,4s-4-1.791-4-4S9.791,8,12,8z M12,10c1.105,0,2,0.895,2,2s-0.895,2-2,2s-2-0.895-2-2" />
							
						</svg>
						<span class="tooltiptext">${msg("passwordtooltip_eye")}</span>
					</button>
					</div>
					</div>
                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="kc-form-options">
                        <#if realm.rememberMe && !usernameEditDisabled??>
                            <div class="checkbox">
                                <label>
                                    <#if login.rememberMe??>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                    <#else>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                                    </#if>
                                </label>
                            </div>
                        </#if>
                        </div>
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                            </#if>
                        </div>

                  </div>

                  <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                      <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                      <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                  </div>
				  
				  <div>
					<b>${msg("passwordtip_part1")}</b>
					<p/>
					<span>${msg("passwordtip_part2")}<p/>
						${msg("passwordtip_part3")}<nobr><a href="https://passwort.muenchen.de/" target="_blank">${msg("passwordtip_link1")}</a></nobr><p/>
						${msg("passwordtip_part4")}<nobr><a href="https://pwdext.muenchen.de/pwm/public/forgottenpassword" target="_blank">${msg("passwordtip_link2")}</a></nobr>
					</span>
					<p/><br/>
					
					<span><b>${msg("usernametip_part0")}</b><p/>
					${msg("usernametip_part1")}					
					<p/>
					<nobr><a href="https://pwdext.muenchen.de/pwm/public/forgottenusername" target="_blank">${msg("usernametip_link")}</a></nobr>
					</span>						
					<p/><br/>
					
					<span>
					<b>${msg("helptip_part1")}</b><p/>
					${msg("helptip_part2")}
					</span>					
				  </div>
				  
            </form>
        </#if>
        </div>

        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
                <hr/>
                <h4>${msg("identity-provider-login-label")}</h4>

                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                    <#list social.providers as p>
                        <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                                type="button" href="${p.loginUrl}">
                            <#if p.iconClasses?has_content>
                                <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${p.displayName!}</span>
                            <#else>
                                <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                            </#if>
                        </a>
                    </#list>
                </ul>
            </div>
        </#if>

    </div>	
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="6"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    </#if>

	
</@layout.registrationLayout>
