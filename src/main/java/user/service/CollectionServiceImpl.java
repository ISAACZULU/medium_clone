package user.service;

import com.medium_clone.user.dto.CollectionRequest;
import com.medium_clone.user.dto.CollectionResponse;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.Collection;
import com.medium_clone.user.entity.User;
import user.repository.ArticleRepository;
import user.repository.CollectionRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepository, UserRepository userRepository, ArticleRepository articleRepository) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public CollectionResponse createCollection(String userEmail, CollectionRequest request) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<User> collaborators = new HashSet<>();
        if (request.getCollaboratorIds() != null) {
            for (Long id : request.getCollaboratorIds()) {
                User collaborator = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Collaborator not found: " + id));
                collaborators.add(collaborator);
            }
        }
        Collection collection = Collection.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .collaborators(collaborators)
                .build();
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollectionResponse updateCollection(String userEmail, Long collectionId, CollectionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own collections");
        }
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        collection.setPublic(request.isPublic());
        Set<User> collaborators = new HashSet<>();
        if (request.getCollaboratorIds() != null) {
            for (Long id : request.getCollaboratorIds()) {
                User collaborator = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Collaborator not found: " + id));
                collaborators.add(collaborator);
            }
        }
        collection.setCollaborators(collaborators);
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCollection(String userEmail, Long collectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own collections");
        }
        collectionRepository.delete(collection);
    }

    @Override
    @Transactional
    public CollectionResponse addArticleToCollection(String userEmail, Long collectionId, Long articleId) {
        Collection collection = getCollectionForEdit(userEmail, collectionId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        collection.getArticles().add(article);
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollectionResponse removeArticleFromCollection(String userEmail, Long collectionId, Long articleId) {
        Collection collection = getCollectionForEdit(userEmail, collectionId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        collection.getArticles().remove(article);
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollectionResponse addCollaborator(String userEmail, Long collectionId, Long collaboratorId) {
        Collection collection = getCollectionForEdit(userEmail, collectionId);
        User collaborator = userRepository.findById(collaboratorId)
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));
        collection.getCollaborators().add(collaborator);
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollectionResponse removeCollaborator(String userEmail, Long collectionId, Long collaboratorId) {
        Collection collection = getCollectionForEdit(userEmail, collectionId);
        User collaborator = userRepository.findById(collaboratorId)
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));
        collection.getCollaborators().remove(collaborator);
        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getCollectionsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return collectionRepository.findByOwner(user).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getCollectionsForCollaborator(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return collectionRepository.findByCollaboratorsContaining(user).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getPublicCollections() {
        return collectionRepository.findByIsPublicTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Collection getCollectionForEdit(String userEmail, Long collectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        boolean isOwner = collection.getOwner().getId().equals(user.getId());
        boolean isCollaborator = collection.getCollaborators().stream().anyMatch(u -> u.getId().equals(user.getId()));
        if (!isOwner && !isCollaborator) {
            throw new IllegalArgumentException("You must be the owner or a collaborator to modify this collection");
        }
        return collection;
    }

    private CollectionResponse toResponse(Collection collection) {
        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .isPublic(collection.isPublic())
                .ownerId(collection.getOwner().getId())
                .ownerUsername(collection.getOwner().getUsername())
                .collaboratorIds(collection.getCollaborators().stream().map(User::getId).collect(Collectors.toSet()))
                .collaboratorUsernames(collection.getCollaborators().stream().map(User::getUsername).collect(Collectors.toSet()))
                .articleIds(collection.getArticles().stream().map(Article::getId).collect(Collectors.toSet()))
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }
} 