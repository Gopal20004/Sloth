package com.demo.sloth.community;

import com.demo.sloth.game.Game;
import com.demo.sloth.game.GameRepository;
import com.demo.sloth.game.PageResponse;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityService {

    private final GameRepository games;
    private final UserRepository users;
    private final CommunityPostRepository posts;
    private final CommunityReplyRepository replies;

    public CommunityService(GameRepository games, UserRepository users,
                            CommunityPostRepository posts, CommunityReplyRepository replies) {
        this.games = games;
        this.users = users;
        this.posts = posts;
        this.replies = replies;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> listPosts(String gameSlug, int page, int size) {
        findGame(gameSlug);
        return PageResponse.from(posts.findByGameSlugOrderByCreatedAtDesc(gameSlug,
                PageRequest.of(page, size)).map(PostResponse::from));
    }

    @Transactional
    public PostResponse createPost(String gameSlug, Long authorId, CreatePostRequest request) {
        Game game = findGame(gameSlug);
        User author = users.findById(authorId).orElseThrow();
        CommunityPost post = posts.save(new CommunityPost(game, author,
                request.title().trim(), request.body().trim()));
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return PostResponse.from(findPost(postId));
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = findPost(postId);
        requireAuthor(post.getAuthor().getId(), userId);
        posts.delete(post);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReplyResponse> listReplies(Long postId, int page, int size) {
        findPost(postId);
        return PageResponse.from(replies.findByPostIdOrderByCreatedAtAsc(postId,
                PageRequest.of(page, size)).map(ReplyResponse::from));
    }

    @Transactional
    public ReplyResponse createReply(Long postId, Long authorId, CreateReplyRequest request) {
        CommunityPost post = findPost(postId);
        User author = users.findById(authorId).orElseThrow();
        CommunityReply reply = replies.save(new CommunityReply(post, author, request.body().trim()));
        return ReplyResponse.from(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        CommunityReply reply = replies.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));
        requireAuthor(reply.getAuthor().getId(), userId);
        replies.delete(reply);
    }

    private Game findGame(String slug) {
        return games.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private CommunityPost findPost(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void requireAuthor(Long authorId, Long userId) {
        if (!authorId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this");
        }
    }
}
