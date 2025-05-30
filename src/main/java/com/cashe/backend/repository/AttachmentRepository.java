package com.cashe.backend.repository;

import com.cashe.backend.domain.Attachment;
import com.cashe.backend.domain.Transaction;
import com.cashe.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTransaction(Transaction transaction);

    List<Attachment> findByUser(User user); // Para buscar todos los adjuntos de un usuario

    Optional<Attachment> findByIdAndUser(Long id, User user);

    List<Attachment> findByTransactionAndUser(Transaction transaction, User user);

    Optional<Attachment> findByIdAndTransaction(Long id, Transaction transaction);

}