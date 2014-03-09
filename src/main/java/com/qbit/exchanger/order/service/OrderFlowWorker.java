package com.qbit.exchanger.order.service;

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

	@Override
	public void run() {
		List<OrderInfo> activeOrders = orderDAO.findActiveAndNotInProcess();
		if (activeOrders != null) {
			for (OrderInfo activeOrder : activeOrders) {
				try {
					processActiveOrder(activeOrder);
				} catch (Exception ex) {
					Logger.getLogger(OrderFlowWorker.class.getName()).log(Level.SEVERE,
						ex.getMessage(), ex);
				}
			}
		}
	}

	private void processActiveOrder(OrderInfo activeOrder) throws Exception {
		if (!activeOrder.isValid()) {
			throw new IllegalArgumentException("Order #" + activeOrder.getId() + " is inconsistent.");
		}
		if (activeOrder.isInProcess()) {
			return;
		}
		switch (activeOrder.getStatus()) {
			case INITIAL:
				processInTransfer(activeOrder);
				break;
			case PAYED:
				processOutTransfer(activeOrder);
				break;
		}
	}

	private void processInTransfer(final OrderInfo activeOrder) throws Exception {
		final String orderId = activeOrder.getId();
		Transfer inTransfer = activeOrder.getInTransfer();
		final Rate rate = exchange.getRate(inTransfer.getCurrency(),
				activeOrder.getOutTransfer().getCurrency());
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

	private void processOutTransfer(final OrderInfo activeOrder) {
		final String orderId = activeOrder.getId();
		final Transfer outTransfer = activeOrder.getOutTransfer();
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
