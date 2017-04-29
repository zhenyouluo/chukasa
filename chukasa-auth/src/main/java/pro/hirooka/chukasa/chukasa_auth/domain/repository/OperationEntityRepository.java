package pro.hirooka.chukasa.chukasa_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.chukasa.chukasa_auth.domain.entity.OperationEntity;

import java.util.UUID;

@Repository
public interface OperationEntityRepository extends JpaRepository<OperationEntity, UUID> {
    OperationEntity findOneByName(String name);
}
