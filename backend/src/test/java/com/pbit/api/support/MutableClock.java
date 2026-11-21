package com.pbit.api.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

// 테스트 중에 시간을 앞으로 돌릴 수 있는 Clock
public class MutableClock extends Clock {

	private Instant now;

	public MutableClock(Instant now) {
		this.now = now;
	}

	public void advance(Duration duration) {
		now = now.plus(duration);
	}

	@Override
	public Instant instant() {
		return now;
	}

	@Override
	public ZoneId getZone() {
		return ZoneOffset.UTC;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return this;
	}

}
