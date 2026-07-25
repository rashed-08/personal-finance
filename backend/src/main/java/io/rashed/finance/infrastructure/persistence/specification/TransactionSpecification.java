package io.rashed.finance.infrastructure.persistence.specification;


import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.infrastructure.persistence.entity.TransactionEntity;

import org.springframework.data.jpa.domain.Specification;


public final class TransactionSpecification {


    private TransactionSpecification() {
    }


    public static Specification<TransactionEntity> withFilter(
            TransactionFilter filter
    ) {


        return (root, query, cb) -> {


            var predicates =
                    cb.conjunction();


            if (filter.fromDate() != null) {

                predicates =
                        cb.and(
                            predicates,
                            cb.greaterThanOrEqualTo(
                                    root.get("transactionDate"),
                                    filter.fromDate()
                            )
                        );
            }


            if (filter.toDate() != null) {

                predicates =
                        cb.and(
                            predicates,
                            cb.lessThanOrEqualTo(
                                    root.get("transactionDate"),
                                    filter.toDate()
                            )
                        );
            }



            if (filter.transactionType() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("transactionType"),
                                        filter.transactionType()
                                )
                        );
            }



            if (filter.transactionStatus() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("transactionStatus"),
                                        filter.transactionStatus()
                                )
                        );
            }



            if (filter.categoryId() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("categoryId"),
                                        filter.categoryId().getValue()
                                )
                        );
            }



            if (filter.salaryCycleId() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("salaryCycleId"),
                                        filter.salaryCycleId().getValue()
                                )
                        );
            }



            if (filter.fundId() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("fundId"),
                                        filter.fundId().getValue()
                                )
                        );
            }



            if (filter.loanId() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("loanId"),
                                        filter.loanId().getValue()
                                )
                        );
            }



            if (filter.accountId() != null) {


                predicates =
                        cb.and(
                                predicates,

                                cb.or(

                                    cb.equal(
                                            root.get("fromAccountId"),
                                            filter.accountId().getValue()
                                    ),

                                    cb.equal(
                                            root.get("toAccountId"),
                                            filter.accountId().getValue()
                                    )
                                )
                        );
            }



            if (filter.migrationBatchId() != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get("migrationBatchId"),
                                        filter.migrationBatchId()
                                )
                        );
            }



            return predicates;
        };
    }
}