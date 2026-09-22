package com.jobprocessor.repository;

import com.jobprocessor.domain.JobEntity;
import com.jobprocessor.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, String> {

    List<JobEntity> findByStatus(JobStatus status);

    List<JobEntity> findByStatusIn(List<JobStatus> statuses);

    long countByStatus(JobStatus status);

    @Query("SELECT j FROM JobEntity j WHERE j.status = 'RUNNING' AND (j.lastHeartbeat IS NULL OR j.lastHeartbeat < :staleTime)")
    List<JobEntity> findStaleRunningJobs(@Param("staleTime") LocalDateTime staleTime);
}
