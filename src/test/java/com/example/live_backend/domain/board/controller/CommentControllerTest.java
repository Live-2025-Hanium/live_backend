package com.example.live_backend.domain.board.controller;

import com.example.live_backend.config.WithMockPrincipalDetails;
import com.example.live_backend.domain.auth.jwt.JwtTokenValidator;
import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.service.CommentService;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.memeber.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
@Import(com.example.live_backend.global.error.exception.GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CommentController 테스트")
class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	@MockBean
	private MemberRepository memberRepository;
	
	@MockBean
	private JwtTokenValidator jwtTokenValidator;

	@Autowired
	private ObjectMapper objectMapper;

	private Member testMember;

	@BeforeEach
	void setUp() throws Exception {

		testMember = Member.builder()
			.oauthId("test-oauth-id")
			.email("test@example.com")
			.role(Role.USER)
			.profile(Profile.builder()
				.nickname("testUser")
				.profileImageUrl("http://test.com/profile.jpg")
				.build())
			.build();

		java.lang.reflect.Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(testMember, 2L);

		when(memberRepository.findById(2L)).thenReturn(Optional.of(testMember));
	}

	@Test
	@DisplayName("누구나 댓글 목록 조회 가능 (@PublicApi)")
	void anyoneCanViewComments() throws Exception {
		when(commentService.getCommentsByBoardId(anyLong(), any())).thenReturn(List.of());

		mockMvc.perform(get("/api/boards/1/comments"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(commentService).getCommentsByBoardId(eq(1L), isNull());
	}

	@Test
	@DisplayName("로그인한 사용자만 댓글 작성 가능")
	@WithMockPrincipalDetails
	void onlyAuthenticatedUserCanCreateComment() throws Exception {
		CommentCreateRequestDto dto = new CommentCreateRequestDto("새로운 댓글");
		when(commentService.createComment(anyLong(), any(), anyLong())).thenReturn(1L);

		mockMvc.perform(post("/api/boards/1/comments")
				.with(csrf())
				.header("Authorization", "Bearer user-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").value(1));

		verify(commentService).createComment(eq(1L), any(), eq(2L));
	}

	@Test
	@DisplayName("비로그인 사용자는 댓글 작성 불가")
	@WithAnonymousUser
	void unauthenticatedUserCannotCreateComment() throws Exception {
		CommentCreateRequestDto dto = new CommentCreateRequestDto("새로운 댓글");

		mockMvc.perform(post("/api/boards/1/comments")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isUnauthorized());

		verify(commentService, never()).createComment(anyLong(), any(), anyLong());
	}

	@Test
	@DisplayName("로그인한 사용자만 대댓글 작성 가능")
	@WithMockPrincipalDetails
	void onlyAuthenticatedUserCanCreateReply() throws Exception {
		CommentCreateRequestDto dto = new CommentCreateRequestDto("새로운 대댓글");
		when(commentService.createReply(anyLong(), anyLong(), any(), anyLong())).thenReturn(2L);

		mockMvc.perform(post("/api/boards/1/comments/1/replies")
				.with(csrf())
				.header("Authorization", "Bearer user-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").value(2));

		verify(commentService).createReply(eq(1L), eq(1L), any(), eq(2L));
	}

	@Test
	@DisplayName("로그인한 사용자만 댓글 수정 가능")
	@WithMockPrincipalDetails
	void onlyAuthenticatedUserCanUpdateComment() throws Exception {
		CommentUpdateRequestDto dto = new CommentUpdateRequestDto("수정된 댓글");
		doNothing().when(commentService).updateComment(anyLong(), any(), anyLong());

		mockMvc.perform(put("/api/comments/1")
				.with(csrf())
				.header("Authorization", "Bearer user-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(commentService).updateComment(eq(1L), any(), eq(2L));
	}

	@Test
	@DisplayName("로그인한 사용자만 댓글 삭제 가능")
	@WithMockPrincipalDetails
	void onlyAuthenticatedUserCanDeleteComment() throws Exception {
		doNothing().when(commentService).deleteComment(anyLong(), anyLong());

		mockMvc.perform(delete("/api/comments/1")
				.with(csrf())
				.header("Authorization", "Bearer user-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(commentService).deleteComment(eq(1L), eq(2L));
	}

	@Test
	@DisplayName("로그인한 사용자만 댓글 좋아요 토글 가능")
	@WithMockPrincipalDetails
	void onlyAuthenticatedUserCanToggleCommentLike() throws Exception {
		doNothing().when(commentService).toggleCommentLike(anyLong(), anyLong());

		mockMvc.perform(post("/api/comments/1/likes")
				.with(csrf())
				.header("Authorization", "Bearer user-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(commentService).toggleCommentLike(eq(1L), eq(2L));
	}

	@Test
	@DisplayName("비로그인 사용자는 댓글 좋아요 토글 불가")
	@WithAnonymousUser
	void unauthenticatedUserCannotToggleCommentLike() throws Exception {
		mockMvc.perform(post("/api/comments/1/likes")
				.with(csrf()))
			.andExpect(status().isUnauthorized());

		verify(commentService, never()).toggleCommentLike(anyLong(), anyLong());
	}

	@Test
	@DisplayName("댓글 내용이 비어있으면 요청 실패")
	@WithMockPrincipalDetails
	void emptyContentShouldFailValidation() throws Exception {
		CommentCreateRequestDto dto = new CommentCreateRequestDto("");

		mockMvc.perform(post("/api/boards/1/comments")
				.with(csrf())
				.header("Authorization", "Bearer user-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());

		verify(commentService, never()).createComment(anyLong(), any(), anyLong());
	}
}