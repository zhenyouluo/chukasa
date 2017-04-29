package pro.hirooka.chukasa.chukasa_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.chukasa.chukasa_auth.domain.entity.UserDetailsEntity;

import java.util.UUID;

@Repository
public interface UserDetailsEntityRepository extends JpaRepository<UserDetailsEntity, UUID> {
    UserDetailsEntity findOneByUsername(String username);
}
