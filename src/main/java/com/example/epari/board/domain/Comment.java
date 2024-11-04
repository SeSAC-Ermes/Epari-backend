package com.example.epari.board.domain;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.base.BaseUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글을 관리하는 엔티티
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Lob
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answer_id")
	private Answer answer;  // 답변에 대한 댓글

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Comment parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private BaseUser writer;

	public static Comment createComment(String content, Answer answer, BaseUser writer) {
		Comment comment = new Comment();
		comment.content = content;
		comment.answer = answer;
		comment.writer = writer;
		return comment;
	}

	public static Comment createReply(String content, Comment parent, BaseUser writer) {
		Comment reply = new Comment();
		reply.content = content;
		reply.parent = parent;
		reply.answer = parent.getAnswer();
		reply.writer = writer;
		return reply;
	}

}
