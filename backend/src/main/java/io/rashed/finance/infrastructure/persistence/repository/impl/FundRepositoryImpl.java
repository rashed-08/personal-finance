package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.infrastructure.persistence.mapper.FundEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.FundJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FundRepositoryImpl implements FundRepository {

    private final FundJpaRepository jpaRepository;

    @Override
    public Fund save(Fund fund) {

        return FundEntityMapper.toDomain(
                jpaRepository.save(
                        FundEntityMapper.toEntity(fund)
                )
        );
    }

    @Override
    public Optional<Fund> findById(FundId id) {

        return jpaRepository.findById(id.getValue())
                .map(FundEntityMapper::toDomain);
    }

    @Override
    public Optional<Fund> findByName(String name) {

        return jpaRepository.findByNameIgnoreCase(name)
                .map(FundEntityMapper::toDomain);
    }

    @Override
    public List<Fund> findByType(FundType fundType) {

        return jpaRepository.findByFundTypeOrderByNameAsc(fundType)
                .stream()
                .map(FundEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Fund> findAll() {

        return jpaRepository.findAllByOrderByNameAsc()
                .stream()
                .map(FundEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Fund> findActive() {

        return jpaRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(FundEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {

        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public void delete(FundId id) {

        jpaRepository.deleteById(id.getValue());
    }
}
