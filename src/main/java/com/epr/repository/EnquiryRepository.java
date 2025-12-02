    package com.epr.repository;
    import com.epr.entity.Enquiry;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;
    import java.util.Optional;


    public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

        Optional<Enquiry> findByIdAndDeleteStatus(Long id, int deleteStatus);

        List<Enquiry> findByDeleteStatusOrderByIdDesc(int deleteStatus);

        Optional<Enquiry> findByEmailAndDeleteStatus(String email, int deleteStatus);

        Optional<Enquiry> findByMobileAndDeleteStatus(String mobile, int deleteStatus);


    }