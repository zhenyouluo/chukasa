package pro.hirooka.chukasa.chukasa_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.chukasa.chukasa_auth.domain.entity.RoleEntity;

import java.util.UUID;

@Repository
public interface RoleEntityRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findOneByName(String name);
}