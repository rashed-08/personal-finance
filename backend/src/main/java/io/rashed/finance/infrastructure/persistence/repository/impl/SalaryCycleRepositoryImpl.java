package io.rashed.finance.infrastructure.persistence.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.infrastructure.persistence.mapper.SalaryCycleEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.SalaryCycleJpaRepository;

@Repository
public class SalaryCycleRepositoryImpl implements SalaryCycleRepository {

    private final SalaryCycleJpaRepository jpaRepository;

    public SalaryCycleRepositoryImpl(SalaryCycleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SalaryCycle save(SalaryCycle salaryCycle) {

        return SalaryCycleEntityMapper.toDomain(
                jpaRepository.save(SalaryCycleEntityMapper.toEntity(salaryCycle))
        );
    }

    @Override
    public Optional<SalaryCycle> findById(SalaryCycleId id) {

        return jpaRepository.findById(id.getValue())
                .map(SalaryCycleEntityMapper::toDomain);
    }

    @Override
    public Optional<SalaryCycle> findCurrent() {

        return findByDate(LocalDate.now());
    }

    @Override
    public Optional<SalaryCycle> findByName(String name) {

        return jpaRepository.findByCycleName(name)
                .map(SalaryCycleEntityMapper::toDomain);
    }

    @Override
    public Optional<SalaryCycle> findByDate(LocalDate date) {

        return jpaRepository.findContaining(date)
                .map(SalaryCycleEntityMapper::toDomain);
    }

    @Override
    public Optional<SalaryCycle> findOpen() {

        return jpaRepository.findByCycleEndDateIsNull()
                .map(SalaryCycleEntityMapper::toDomain);
    }

    @Override
    public Optional<SalaryCycle> findPrevious(LocalDate beforeStartDate) {

        return jpaRepository.findPrevious(beforeStartDate, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(SalaryCycleEntityMapper::toDomain);
    }

    @Override
    public List<SalaryCycle> findAll() {

        return jpaRepository.findAll()
                .stream()
                .map(SalaryCycleEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {

        return jpaRepository.existsByCycleName(name);
    }

    @Override
    public void delete(SalaryCycleId id) {

        jpaRepository.deleteById(id.getValue());
    }
}
