package me.line.notify;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author P-C Lin (a.k.a 高科技黑手)
 */
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentResponseBody {

	private int status;

	private String message;

	public SentResponseBody() {
		status = Integer.MIN_VALUE;
	}
}
