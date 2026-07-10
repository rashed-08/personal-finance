package io.rashed.finance.domain.funds;

import io.rashed.finance.common.enums.FundType;

import java.util.List;
import java.util.Optional;

public interface FundRepository {

    Fund save(Fund fund);

    Optional<Fund> findById(FundId id);

    Optional<Fund> findByName(String name);

    List<Fund> findByType(FundType fundType);

    List<Fund> findAll();

    List<Fund> findActive();

    boolean existsByName(String name);

    void delete(FundId id);
}