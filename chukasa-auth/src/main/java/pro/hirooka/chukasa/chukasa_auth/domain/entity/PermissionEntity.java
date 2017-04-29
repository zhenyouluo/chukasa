package pro.hirooka.chukasa.chukasa_auth.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
public class PermissionEntity extends AbstractEntity {

    @Getter
    @Setter
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "permissionEntitySet", fetch = FetchType.EAGER)
    private Set<RoleEntity> roleEntitySet;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "permission_operation")
    private Set<OperationEntity> operationEntitySet;
}
