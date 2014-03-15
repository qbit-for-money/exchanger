package com.qbit.exchanger.order.service;

import com.qbit.exchanger.dao.util.DAOExecutor;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Rate;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowWorker implements Runnable {

	@Inject
	private OrderDAO orderDAO;

	@Inject
	private MoneyServiceProvider moneyServiceProvider;
	
	@Inject
	private Exchange exchange;
	
	@Inject
	private DAOExecutor databaseExecutor;

	@Override
	public void run() {
		List<OrderInfo> ordersUnderWork = orderDAO.findByFullStatus(
				Arrays.asList(OrderStatus.INITIAL, OrderStatus.PAYED), false);
		if (ordersUnderWork != null) {
			for (OrderInfo orderUnderWork : ordersUnderWork) {
				try {
					processOrderUnderWork(orderUnderWork);
				} catch (Exception ex) {
					Logger.getLogger(OrderFlowWorker.class.getName()).log(Level.SEVERE,
						ex.getMessage(), ex);
				}
			}
		}
	}

	private void processOrderUnderWork(OrderInfo orderUnderWork) throws Exception {
		switch (orderUnderWork.getStatus()) {
			case INITIAL:
				processInTransfer(orderUnderWork);
				break;
			case PAYED:
				processOutTransfer(orderUnderWork);
				break;
		}
	}

	private void processInTransfer(final OrderInfo orderUnderWork) throws Exception {
		if (!orderUnderWork.isValid()) {
			throw new IllegalArgumentException("Order #" + orderUnderWork.getId() + " is inconsistent.");
		}
		final String orderId = orderUnderWork.getId();
		Transfer inTransfer = orderUnderWork.getInTransfer();
		final Rate rate = exchange.getRate(inTransfer.getCurrency(),
				orderUnderWork.getOutTransfer().getCurrency());
		if ((rate == null) || !rate.isValid()) {
			throw new IllegalStateException();
		}
		orderDAO.changeStatus(orderId, OrderStatus.INITIAL, true);
		MoneyService moneyService = moneyServiceProvider.get(inTransfer);
		moneyService.process(inTransfer, new MoneyTransferCallback() {

			@Override
			public void success(Amount inAmount) {
				orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, false,
						inAmount, rate.mul(inAmount));
			}

			@Override
			public void error(String msg) {
				orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED, false);
			}
		});
	}

	private void processOutTransfer(final OrderInfo orderUnderWork) {
		final String orderId = orderUnderWork.getId();
		final Transfer outTransfer = orderUnderWork.getOutTransfer();
		orderDAO.changeStatus(orderId, OrderStatus.PAYED, true);
		MoneyService moneyService = moneyServiceProvider.get(outTransfer);
		moneyService.process(outTransfer, new MoneyTransferCallback() {

			@Override
			public void success(Amount amount) {
				orderDAO.changeStatusAndOutAmount(orderId, OrderStatus.SUCCESS, false,
						amount);
			}

			@Override
			public void error(String msg) {
				orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED, false);
			}
		});
	}
}
