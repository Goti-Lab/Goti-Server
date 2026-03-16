package com.goti.infra.sms;

import com.goti.config.properties.sms.SmsProperties;

import lombok.RequiredArgsConstructor;

import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsProvider {
	private final SmsProperties smsProperties;
	private final DefaultMessageService messageService;

	public void send(String to, String code) {
		String content = getMessage(code);
		Message message = new Message();
		message.setFrom(smsProperties.sender());
		message.setTo(to);
		message.setText(content);

		messageService.sendOne(new SingleMessageSendingRequest(message));
	}

	private String getMessage(String code) {
		return smsProperties.content().replace("{code}", code);
	}

}
