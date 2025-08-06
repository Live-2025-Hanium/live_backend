package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.board.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

	private final CommentRepository commentRepository;

	/**
	 * 게시글의 댓글 구조 조회
	 */
	public CommentStructure getCommentStructure(Long boardId) {
		List<Comment> parentComments = commentRepository.findParentCommentsByBoardId(boardId);

		if (parentComments.isEmpty()) {
			return new CommentStructure(List.of(), Map.of(), List.of());
		}
		List<Long> parentCommentIds = parentComments.stream()
			.map(Comment::getId)
			.toList();

		List<Comment> allReplies = parentCommentIds.stream()
			.flatMap(parentId -> commentRepository.findRepliesByParentCommentId(parentId).stream())
			.toList();

		Map<Long, List<Comment>> repliesMap = allReplies.stream()
			.collect(Collectors.groupingBy(reply -> reply.getParentComment().getId()));
		List<Long> allCommentIds = parentComments.stream()
			.map(Comment::getId)
			.collect(Collectors.toList());
		allCommentIds.addAll(allReplies.stream().map(Comment::getId).toList());

		return new CommentStructure(parentComments, repliesMap, allCommentIds);
	}

	public static class CommentStructure {
		private final List<Comment> parentComments;
		private final Map<Long, List<Comment>> repliesMap;
		private final List<Long> allCommentIds;

		public CommentStructure(List<Comment> parentComments,
			Map<Long, List<Comment>> repliesMap,
			List<Long> allCommentIds) {
			this.parentComments = parentComments;
			this.repliesMap = repliesMap;
			this.allCommentIds = allCommentIds;
		}

		public List<Comment> getParentComments() {
			return parentComments;
		}

		public List<Comment> getReplies(Long parentCommentId) {
			return repliesMap.getOrDefault(parentCommentId, List.of());
		}

		public List<Long> getAllCommentIds() {
			return allCommentIds;
		}

		public boolean isEmpty() {
			return parentComments.isEmpty();
		}
	}
} 