package pawtner_core.pawtner_care_api.repository;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    java.util.List<Event> findAllByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscTimeAsc(
        LocalDate startDate,
        LocalDate endDate
    );

    java.util.List<Event> findAllByOrderByStartDateAscTimeAsc();
}
