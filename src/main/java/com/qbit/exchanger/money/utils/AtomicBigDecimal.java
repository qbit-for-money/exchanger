package com.qbit.exchanger.money.utils;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Alexander_Sergeev
 */
public final class AtomicBigDecimal {

	private final AtomicReference<BigDecimal> valueHolder = new AtomicReference<>();

	public AtomicBigDecimal(BigDecimal value) {
		valueHolder.set(value);
	}

	public BigDecimal getValue() {
		return valueHolder.get();
	}

	public BigDecimal addAndGet(BigDecimal value) {
		while (true) {
			BigDecimal current = valueHolder.get();
			BigDecimal next = current.add(value);
			if (valueHolder.compareAndSet(current, next)) {
				return next;
			}
		}
	}
}
