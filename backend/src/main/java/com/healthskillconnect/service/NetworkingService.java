package com.healthskillconnect.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NetworkingService {

    private final PostRepository postRepository;
    private final PostReactionRepository reactionRepository;
    private final PostCommentRepository commentRepository;
    private final ConnectionRepository connectionRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    public NetworkingService(
            PostRepository postRepository,
            PostReactionRepository reactionRepository,
            PostCommentRepository commentRepository,
            ConnectionRepository connectionRepository,
            MentorshipRequestRepository mentorshipRequestRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.connectionRepository = connectionRepository;
        this.mentorshipRequestRepository = mentorshipRequestRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> getFeed(String currentUserId, String category) {
        List<Post> posts = (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category))
                ? postRepository.findByCategoryOrderByCreatedAtDesc(category.toUpperCase().trim())
                : postRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Post p : posts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.id);
            map.put("authorId", p.authorId);
            map.put("authorName", p.authorName);
            map.put("authorRole", p.authorRole);
            map.put("authorSpecialization", p.authorSpecialization);
            map.put("authorAvatar", p.authorAvatar);
            map.put("title", p.title);
            map.put("content", p.content);
            map.put("category", p.category);
            map.put("tags", parseTags(p.tagsJson));
            map.put("likesCount", p.likesCount);
            map.put("commentsCount", p.commentsCount);
            map.put("createdAt", p.createdAt.toString());
            map.put("likedByMe", currentUserId != null && reactionRepository.findByPostIdAndUserId(p.id, currentUserId).isPresent());
            map.put("comments", commentRepository.findByPostIdOrderByCreatedAtAsc(p.id));
            list.add(map);
        }
        return list;
    }

    @Transactional
    public Post createPost(AppUser author, String title, String content, String category, List<String> tags) {
        if (content == null || content.trim().length() < 5) {
            throw new IllegalArgumentException("Post content must contain at least 5 characters.");
        }
        String tagsJson = "";
        try {
            tagsJson = objectMapper.writeValueAsString(tags != null ? tags : Collections.emptyList());
        } catch (Exception ignored) {}

        Post p = new Post(
                author.id,
                author.fullName,
                author.role,
                author.specialization,
                author.avatarInitials,
                title != null ? title.trim() : "",
                content.trim(),
                category != null ? category.toUpperCase().trim() : "CLINICAL_INSIGHT",
                tagsJson
        );
        return postRepository.save(p);
    }

    @Transactional
    public Map<String, Object> toggleReaction(Long postId, String userId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found."));

        Optional<PostReaction> existing = reactionRepository.findByPostIdAndUserId(postId, userId);
        boolean liked;
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            p.likesCount = Math.max(0, p.likesCount - 1);
            liked = false;
        } else {
            reactionRepository.save(new PostReaction(postId, userId));
            p.likesCount++;
            liked = true;
        }
        postRepository.save(p);
        return Map.of("liked", liked, "likesCount", p.likesCount);
    }

    @Transactional
    public PostComment addComment(Long postId, AppUser author, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty.");
        }
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found."));

        PostComment comment = new PostComment(
                postId,
                author.id,
                author.fullName,
                author.role,
                author.avatarInitials,
                content.trim()
        );
        PostComment saved = commentRepository.save(comment);
        p.commentsCount++;
        postRepository.save(p);
        return saved;
    }

    // Connections
    public List<Map<String, Object>> getConnectionsForUser(String userId) {
        List<Connection> connections = connectionRepository.findAllForUser(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Connection c : connections) {
            String partnerId = c.requesterId.equals(userId) ? c.recipientId : c.requesterId;
            AppUser partner = userRepository.findById(partnerId).orElse(null);
            if (partner != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("connectionId", c.id);
                item.put("status", c.status);
                item.put("isRequester", c.requesterId.equals(userId));
                item.put("partner", partner.publicMap());
                item.put("createdAt", c.createdAt.toString());
                result.add(item);
            }
        }
        return result;
    }

    @Transactional
    public Connection sendConnectionRequest(String requesterId, String recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new IllegalArgumentException("Cannot connect with yourself.");
        }
        Optional<Connection> existing = connectionRepository.findBetween(requesterId, recipientId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Connection c = new Connection(requesterId, recipientId, "PENDING");
        return connectionRepository.save(c);
    }

    @Transactional
    public Connection updateConnectionStatus(Long connectionId, String currentUserId, String action) {
        Connection c = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NoSuchElementException("Connection request not found."));

        if (!c.recipientId.equals(currentUserId) && !c.requesterId.equals(currentUserId)) {
            throw new IllegalArgumentException("Unauthorized to modify this connection.");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            c.status = "ACCEPTED";
        } else if ("DECLINE".equalsIgnoreCase(action)) {
            c.status = "DECLINED";
        }
        c.updatedAt = LocalDateTime.now();
        return connectionRepository.save(c);
    }

    // Mentorship
    @Transactional
    public MentorshipRequest requestMentorship(AppUser student, String mentorId, String subject, String goals, String message) {
        AppUser mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new NoSuchElementException("Mentor not found."));

        MentorshipRequest req = new MentorshipRequest(
                student.id,
                student.fullName,
                student.specialization,
                student.avatarInitials,
                mentor.id,
                subject != null ? subject.trim() : "Career & Academic Guidance",
                goals,
                message
        );
        return mentorshipRequestRepository.save(req);
    }

    public List<MentorshipRequest> getMentorshipRequestsForUser(String userId, boolean isMentor) {
        return isMentor
                ? mentorshipRequestRepository.findByMentorIdOrderByCreatedAtDesc(userId)
                : mentorshipRequestRepository.findByStudentIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public MentorshipRequest updateMentorshipStatus(Long id, String mentorId, String status, String notes) {
        MentorshipRequest mr = mentorshipRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mentorship request not found."));
        if (!mr.mentorId.equals(mentorId)) {
            throw new IllegalArgumentException("Unauthorized.");
        }
        mr.status = status.toUpperCase().trim();
        if (notes != null) mr.notes = notes;
        return mentorshipRequestRepository.save(mr);
    }

    public List<Map<String, Object>> getDiscoverableMembers(String currentUserId, String query, String roleFilter) {
        List<AppUser> all = userRepository.findAll();
        List<Connection> userConnections = (currentUserId != null) ? connectionRepository.findAllForUser(currentUserId) : Collections.emptyList();
        Map<String, String> connStatus = new HashMap<>();
        for (Connection c : userConnections) {
            String partner = c.requesterId.equals(currentUserId) ? c.recipientId : c.requesterId;
            connStatus.put(partner, c.status);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (AppUser u : all) {
            if (currentUserId != null && u.id.equals(currentUserId)) continue;
            if (roleFilter != null && !roleFilter.isBlank() && !"ALL".equalsIgnoreCase(roleFilter) && !u.role.equalsIgnoreCase(roleFilter)) {
                continue;
            }
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                boolean matchesName = u.fullName.toLowerCase().contains(q);
                boolean matchesSpec = u.specialization != null && u.specialization.toLowerCase().contains(q);
                boolean matchesRole = u.role.toLowerCase().contains(q);
                if (!matchesName && !matchesSpec && !matchesRole) continue;
            }

            Map<String, Object> map = u.publicMap();
            map.put("connectionStatus", connStatus.getOrDefault(u.id, "NONE"));
            result.add(map);
        }
        return result;
    }

    private List<String> parseTags(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, STRING_LIST_TYPE);
            }
        } catch (Exception ignored) {}
        return Arrays.stream(json.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
