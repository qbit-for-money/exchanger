package com.qbit.exchanger.services.core;

/**
 * Transaction money bean
 *
 * @author Ivan_Rakitnyh
 */
public class Money {

    private String address;
    private int coins;
    private int cents;

    private Money() {}

	public Money(String address, int coins, int cents) {
		this.address = address;
		this.coins = coins;
		this.cents = cents;
	}

	public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getCents() {
        return cents;
    }

    public void setCents(int cents) {
        this.cents = cents;
    }
}
