package com.example.demo;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.PostDto;
import com.example.demo.dto.RegisterDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── 헬퍼 ──────────────────────────────────────────────────────

    /** 회원가입 후 로그인해서 Access Token 반환 */
    private String registerAndLogin(String username) throws Exception {
        RegisterDto reg = new RegisterDto();
        reg.setUsername(username);
        reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginDto login = new LoginDto();
        login.setUsername(username);
        login.setPassword("password123");
        String res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).path("data").path("accessToken").asText();
    }

    /** 게시글 생성 후 id 반환 */
    private long createPost(String token) throws Exception {
        PostDto dto = new PostDto();
        dto.setTitle("테스트 제목");
        dto.setContent("테스트 내용");
        String res = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).path("data").path("id").asLong();
    }

    // ── 회원가입 ──────────────────────────────────────────────────

    @Test
    void 회원가입_성공() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("u_reg_ok");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 회원가입_username_빈값_400() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("   ");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void 회원가입_username_2자_400() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("ab");  // min 3
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입_password_7자_400() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("u_pw_short");
        dto.setPassword("1234567");  // min 8

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입_중복_409() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("u_dup");
        dto.setPassword("password123");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(json));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ── 로그인 ────────────────────────────────────────────────────

    @Test
    void 로그인_성공() throws Exception {
        RegisterDto reg = new RegisterDto();
        reg.setUsername("u_login_ok");
        reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginDto dto = new LoginDto();
        dto.setUsername("u_login_ok");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void 로그인_없는사용자_401() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setUsername("ghost_user");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void 로그인_비밀번호불일치_401() throws Exception {
        RegisterDto reg = new RegisterDto();
        reg.setUsername("u_wrong_pw");
        reg.setPassword("correctpw1");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginDto dto = new LoginDto();
        dto.setUsername("u_wrong_pw");
        dto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ── 게시글 ────────────────────────────────────────────────────

    @Test
    void 게시글_작성_성공() throws Exception {
        String token = registerAndLogin("u_post_ok");
        PostDto dto = new PostDto();
        dto.setTitle("제목입니다");
        dto.setContent("내용입니다");

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("제목입니다"))
                .andExpect(jsonPath("$.data.authorUsername").value("u_post_ok"));
    }

    @Test
    void 게시글_작성_인증없음_403() throws Exception {
        PostDto dto = new PostDto();
        dto.setTitle("제목");
        dto.setContent("내용");

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 게시글_작성_제목_빈값_400() throws Exception {
        String token = registerAndLogin("u_post_blank");
        PostDto dto = new PostDto();
        dto.setTitle("   ");
        dto.setContent("내용");

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 게시글_작성_제목_201자_400() throws Exception {
        String token = registerAndLogin("u_post_long");
        PostDto dto = new PostDto();
        dto.setTitle("a".repeat(201));  // max 200
        dto.setContent("내용");

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 게시글_수정_작성자_아님_403() throws Exception {
        String ownerToken = registerAndLogin("u_owner");
        String otherToken = registerAndLogin("u_other");
        long postId = createPost(ownerToken);

        PostDto dto = new PostDto();
        dto.setTitle("수정 시도");
        dto.setContent("수정 내용");

        mockMvc.perform(put("/api/posts/" + postId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 게시글_없는_id_404() throws Exception {
        mockMvc.perform(get("/api/posts/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── 댓글 ──────────────────────────────────────────────────────

    @Test
    void 댓글_작성_성공() throws Exception {
        String token = registerAndLogin("u_cmt_ok");
        long postId = createPost(token);
        CommentDto dto = new CommentDto();
        dto.setContent("댓글 내용입니다.");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("댓글 내용입니다."));
    }

    @Test
    void 댓글_작성_내용_빈값_400() throws Exception {
        String token = registerAndLogin("u_cmt_blank");
        long postId = createPost(token);
        CommentDto dto = new CommentDto();
        dto.setContent("");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 댓글_작성_내용_1001자_400() throws Exception {
        String token = registerAndLogin("u_cmt_long");
        long postId = createPost(token);
        CommentDto dto = new CommentDto();
        dto.setContent("a".repeat(1001));  // max 1000

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
