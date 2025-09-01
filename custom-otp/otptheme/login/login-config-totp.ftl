<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("configureTotpNotAllowed")}
    <#elseif section = "form">
	<div id="lhmlogo"></div>
	<div id="lhmdigimarke"></div>

    <b>${msg("pageExpiredMsg1")}</b><br/>
	<a id="loginRestartLink" href="${url.loginRestartFlowUrl}">${msg("restartLogin")}</a><p/>
	<p/><b>${msg("configureTotpHint1")}</b><br/>
	${msg("configureTotpHint2")}	
    </#if>
</@layout.registrationLayout>
