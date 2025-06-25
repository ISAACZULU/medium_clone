package user.repository;

import com.medium_clone.user.entity.Collection;
import com.medium_clone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByOwner(User owner);
    List<Collection> findByCollaboratorsContaining(User collaborator);
    List<Collection> findByIsPublicTrue();
} 