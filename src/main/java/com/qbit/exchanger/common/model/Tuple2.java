package com.qbit.exchanger.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Александр
 */
public class Tuple2<A, B> implements Serializable {
	
	private final A a;
	private final B b;

	public Tuple2(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return a;
	}

	public B getB() {
		return b;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.a);
		hash = 29 * hash + Objects.hashCode(this.b);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
		if (!Objects.equals(this.a, other.a)) {
			return false;
		}
		if (!Objects.equals(this.b, other.b)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Tuple2{" + "a=" + a + ", b=" + b + '}';
	}
}
