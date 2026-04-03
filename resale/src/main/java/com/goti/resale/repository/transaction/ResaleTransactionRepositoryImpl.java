package com.goti.resale.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.resale.domain.entity.resale.QResaleListingEntity;
import com.goti.resale.domain.entity.resale.QResaleOrderEntity;
import com.goti.resale.domain.entity.resale.QResaleTransactionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleTransactionRepositoryImpl implements ResaleTransactionRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<ResaleTransactionEntity> findListings(List<UUID> orderIds) {
		QResaleTransactionEntity transaction = QResaleTransactionEntity.resaleTransactionEntity;
		QResaleOrderEntity resaleOrder = QResaleOrderEntity.resaleOrderEntity;
		QResaleListingEntity listing = QResaleListingEntity.resaleListingEntity;

		return queryFactory
			.selectFrom(transaction)
			.join(transaction.resaleOrder, resaleOrder).fetchJoin()
			.join(transaction.listing, listing).fetchJoin()
			.where(resaleOrder.id.in(orderIds))
			.orderBy(resaleOrder.createdAt.desc(), transaction.createdAt.asc())
			.fetch();
	}
}
