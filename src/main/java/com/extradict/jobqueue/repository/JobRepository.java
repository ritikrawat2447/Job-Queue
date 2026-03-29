package com.extradict.jobqueue.repository;

import com.extradict.jobqueue.entity.Job;
import com.extradict.jobqueue.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job,UUID>{

    // Spring writes the SQL for these automatically
    // based on method name — no SQL needed from you

    // SELECT * FROM jobs WHERE status = ?
    List<Job> findByStatus(JobStatus status);

    // SELECT * FROM jobs WHERE submitted_by = ?
    List<Job> findBySubmittedBy(String submittedBy);

    // SELECT COUNT(*) FROM jobs WHERE status = ?
    long countByStatus(JobStatus status);

}
